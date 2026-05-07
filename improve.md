# imooc-hire-dev 优化建议

## 分析摘要

本项目是一个 Spring Cloud Alibaba 微服务招聘平台后端，使用 Maven 多模块组织，包含网关、认证、用户、企业、职位/简历、资源、文件、支付、RabbitMQ 示例等模块。当前模块边界基本清晰，公共能力集中在 `hire-common`、`hire-pojo`、`hire-api` 中，已经具备 Nacos、Gateway、Feign、Redis、RabbitMQ、Seata、Sentinel、Zipkin、MongoDB、MinIO/OSS 等完整微服务学习型能力。

本次检查重点覆盖父 POM、各模块 POM、配置文件、网关认证过滤器、JWT 工具、用户上下文拦截器、短信登录、文件上传、MQ 消费、本地消息表、测试覆盖和构建状态。当前 `mvn -q -DskipTests compile` 可以通过，说明基础编译链路可用。

## 优先级最高的优化

### 1. 立即处理配置和密钥泄露风险

现状：

- 多个 `application-dev.yml` 中提交了 MySQL、Redis、RabbitMQ、Nacos、Canal、MinIO、OSS 等连接地址和账号密码。
- `service-file-5001/src/main/resources/application-dev.yml` 中存在对象存储访问密钥。
- `payment-center-9060/src/main/resources/application-prod.yml` 中也存在生产环境连接信息和凭据。
- `hire-api/src/main/java/com/imooc/api/RedissonConfig.java` 直接硬编码 Redis 地址和密码。
- `hire-common/src/main/java/com/imooc/base/BaseInfoProperties.java` 中硬编码支付中心地址、回调地址和支付中心调用 header。

建议：

- 立即轮换已经进入仓库的数据库、Redis、MQ、Nacos、Canal、MinIO、OSS、支付中心相关密钥。
- 将真实凭据从 Git 仓库移出，改为环境变量、Nacos 加密配置、Kubernetes Secret、CI/CD Secret 或本机 `application-local.yml`。
- 仓库中只保留 `application-example.yml` 或脱敏后的 `application-dev.yml` 模板。
- Jasypt 的解密口令也不能写在配置文件中，应由启动参数或环境变量注入。
- 对 Git 历史中的敏感信息做清理，至少要确认这些密钥已经失效。

收益：

- 降低数据库、对象存储和支付配置被误用的风险。
- 让项目可以安全公开、协作和部署到多环境。

### 2. 修复 JWT 和登录安全问题

现状：

- `hire-common/src/main/java/com/imooc/utils/JWTUtils.java` 在日志中打印 JWT key 和生成后的 token。
- `JWTUtils#createJWTWithPrefix(String body, String prefix)` 默认生成无过期时间的 token。
- `auth-service-8111/src/main/java/com/imooc/controller/PassportController.java` 短信验证码使用 `Math.random()` 生成，并在日志中打印验证码。
- 登录接口保留了固定验证码 `123456` 的绕过逻辑。
- `logout` 只返回成功，没有真正失效 token；旧的 Redis token 会话方案已被注释。
- token subject 中直接放完整用户 JSON，网关再透传到下游 header，容易带来过大 header、敏感字段扩散和用户信息陈旧问题。

建议：

- 删除 JWT key、JWT token、短信验证码等敏感日志。
- 强制所有 token 设置过期时间，区分 app、saas、admin 的有效期。
- 使用 `SecureRandom` 或短信服务端能力生成验证码，并限制手机号、IP、设备维度的发送频率。
- 移除固定验证码绕过逻辑；如果必须保留测试入口，应只在 test profile 中启用。
- 引入 token 版本号、黑名单、短期 access token + refresh token，或者恢复 Redis 会话校验以支持登出和强制下线。
- JWT 中只放最小 claims，例如 `userId`、`role`、`tenantId`、`tokenVersion`，下游需要完整用户信息时通过缓存或用户服务查询。

收益：

- 避免长期有效 token 和日志泄露导致的横向风险。
- 支持真实登出、踢下线和权限变化即时生效。

### 3. 收紧网关 CORS 和认证上下文

现状：

- `gateway-8000/src/main/java/com/imooc/CorsConfig.java` 与 `gateway-8000/src/main/resources/application-dev.yml` 都配置了跨域。
- 当前跨域使用 `allowedOriginPatterns: "*"` 且允许 credentials。
- `gateway-8000/src/main/java/com/imooc/filter/SecurityFilterJWT.java` 将用户 JSON 编码后放入内部 header。
- `hire-api/src/main/java/com/imooc/api/intercept/JWTCurrentUserInterceptor.java` 中 app 用户 header 的解析逻辑被注释，只处理 saas 用户和 admin 用户，普通 app token 经过网关后可能无法在下游被正确设置到 `currentUser`。

建议：

- CORS 只保留一种配置方式，并按环境白名单化前端域名。
- 对携带 cookie 或凭据的请求禁止使用全域通配。
- 下游服务不要信任外部客户端传入的 `app-user-json`、`saas-user-json`、`admin-user-json` header，建议网关先清理同名 header，再写入可信内部 header。
- 修复 `JWTCurrentUserInterceptor` 的 app 用户解析逻辑，统一处理 app、saas、admin 三类身份。
- 用 `X-User-Id`、`X-User-Role` 等最小身份 header 替代完整 JSON。

收益：

- 降低跨域误开放和 header 伪造风险。
- 修复普通用户上下文缺失导致的业务权限判断问题。

## 业务正确性优化

### 4. 文件上传需要补强校验和对象命名

现状：

- `service-file-5001/src/main/java/com/imooc/controller/FileController.java` 多处直接使用原始文件名拼接对象名。
- `uploadFace` 使用 `userId + File.separator + filename`，对象存储 key 不建议依赖本地文件分隔符。
- `uploadLogo` 对同一个 `MultipartFile` 的 input stream 调用了两次上传，第一次上传结果未使用，第二次读取可能受流状态影响。
- 文件类型、扩展名、MIME、图片尺寸、病毒扫描、文件名清洗等检查不足。
- 本地临时文件写入 `/temp` 后缺少清理策略。

建议：

- 统一实现 `FileStorageService`，屏蔽 MinIO、OSS、本地临时文件差异。
- 对上传文件做白名单校验：大小、MIME、扩展名、图片解码检查。
- 对象 key 使用业务目录 + UUID + 标准扩展名，避免保留用户原始文件名。
- 对 base64 上传增加长度限制和解码异常处理。
- 临时文件使用 JDK 临时目录或专用目录，并在 finally 中清理。
- 修复 `uploadLogo` 的重复上传问题。

收益：

- 避免恶意文件、路径污染、对象 key 不兼容和存储脏数据。

### 5. MQ 消息可靠性和幂等需要完善

现状：

- `service-work-3001/src/main/java/com/imooc/mq/InitResumeMQConsumer.java` 中 `basicAck(..., true)` 和 `basicNack(..., true, true)` 使用了 multiple=true，可能批量确认或批量拒绝同 channel 上之前的消息。
- 消费异常时直接 `requeue=true`，如果业务一直失败，可能形成无限重试。
- 异常使用 `printStackTrace()`。
- `auth-service-8111/src/main/java/com/imooc/mq/InitResumeMQProducerHandler.java` 使用 `ThreadLocal` 保存本地消息 id，但未看到 remove，且自定义事务管理器 `MyTransactionManager` 当前未启用。
- 本地消息表方案和 Seata、RabbitMQ confirm/return、消费端幂等之间的边界还不够清晰。

建议：

- 单条消息确认使用 `basicAck(deliveryTag, false)`。
- 失败消息进入重试队列或死信队列，设置最大重试次数，避免无限 requeue。
- 以 `msgId` 为幂等键，消费端先查状态再执行初始化简历。
- 本地消息表采用事务同步回调或可靠 outbox 调度任务，发送成功后更新状态。
- 清理 `ThreadLocal`，避免线程复用导致消息 id 泄漏。
- 用结构化日志记录异常和消息 id，替代 `printStackTrace()`。

收益：

- 提升注册后初始化简历等异步流程的可靠性。
- 降低重复消费、消息丢失和无限重试风险。

### 6. 支付中心调用应从公共常量迁移为服务化配置

现状：

- `BaseInfoProperties` 中硬编码支付中心 URL、支付回调 URL 和调用 header。
- 企业服务和支付中心之间既有 HTTP 地址常量，也有微服务体系，边界不统一。
- 支付回调地址使用临时穿透域名，容易在部署时遗漏。

建议：

- 将支付中心地址、回调地址、认证信息迁移到配置类，例如 `PayCenterProperties`。
- 内部调用优先使用 Feign + 服务发现，外部支付回调地址由环境配置注入。
- 支付回调增加签名验签、幂等更新、状态机校验和回调日志。
- 将支付中心认证 header 改为签名或服务间 token，不要使用硬编码固定值。

收益：

- 降低部署误配和支付回调伪造风险。
- 让支付流程更符合生产可运维要求。

## 工程结构优化

### 7. 拆分公共模块职责，降低依赖传染

现状：

- `hire-common` 同时承载工具类、异常、统一响应、Redis、Web、AOP、Nacos、Feign、Zipkin、MyBatis、JWT、腾讯云等依赖。
- `hire-pojo` 依赖 `hire-common`，业务服务依赖 `hire-api`，最终容易把很多不需要的 starter 传递到所有模块。
- Gateway 作为 WebFlux 应用，也可能因为公共依赖引入 Servlet Web 相关依赖，增加排查复杂度。

建议：

- 将公共模块拆成更小边界：
  - `hire-core`：枚举、异常、结果模型、基础工具，不依赖重型 starter。
  - `hire-web-starter`：MVC 拦截器、异常处理、Validation。
  - `hire-security-starter`：JWT、用户上下文、鉴权相关能力。
  - `hire-data-starter`：MyBatis、Redis、分页等数据访问配置。
  - `hire-mq-starter`：RabbitMQ 交换机、队列、消息可靠性基础设施。
- 业务模块按需依赖，避免“公共模块变成万能包”。

收益：

- 降低启动时间、依赖冲突和模块间隐式耦合。
- 后续升级 Spring Boot / Spring Cloud 更可控。

### 8. 统一配置治理和环境切换

现状：

- 各服务 `application.yml` 中固定 `spring.profiles.active: dev`。
- Nacos、Redis、Zipkin、ZK、Seata 配置在多个模块重复。
- dev/prod 配置存在不完整和重复，生产配置只有部分模块有实际内容。

建议：

- 移除代码仓库中的固定 active profile，改用启动参数 `SPRING_PROFILES_ACTIVE`。
- 抽取共享配置，例如 `common-dev.yaml`、`common-prod.yaml`、`jwt_config.yaml`、`observability.yaml`。
- 每个服务只保留自身差异配置。
- 增加配置模板和本地启动说明，明确最小依赖集合。
- 对生产 profile 做完整性校验，避免只有部分服务可部署。

收益：

- 减少环境迁移成本。
- 避免新增服务时复制粘贴旧地址和旧密码。

### 9. 梳理微服务治理组件的取舍

现状：

- 项目同时使用 Nacos、Gateway、Sentinel、Seata、Sleuth/Zipkin、Zookeeper/Curator、Canal、Redis、RabbitMQ、MongoDB、MinIO/OSS。
- 对学习项目来说覆盖面完整，但对实际业务来说基础设施较重。
- Zookeeper 与 Redisson 都提供分布式锁能力，存在技术选型重叠。

建议：

- 按真实业务路径保留必需组件，演示组件移动到 `examples` 或独立分支。
- 分布式锁建议优先统一到 Redisson 或数据库乐观锁，除非确实需要 ZK 的强一致协调语义。
- Canal、MongoDB、RabbitMQ 等能力要有明确业务场景、失败补偿和监控指标。
- 对每个中间件补充健康检查、连接池、超时、重试、降级策略。

收益：

- 降低本地启动和生产部署复杂度。
- 让团队更容易定位问题。

## 代码质量优化

### 10. 控制器层需要减薄，业务逻辑下沉

现状：

- `PassportController`、`SaasPassportController`、`FileController` 等控制器包含较多业务流程、Redis key 操作、消息构造、文件处理细节。

建议：

- 控制器只负责参数校验、调用应用服务、返回结果。
- 登录、扫码登录、短信验证码、文件上传等流程分别下沉到 application service。
- Redis key 构造封装到专门组件，避免到处字符串拼接。
- 复杂流程增加状态枚举和领域方法，减少魔法字符串。

收益：

- 更容易单元测试。
- 业务流程变动时不影响接口层。

### 11. 日志规范需要收口

现状：

- MyBatis 多个模块启用了 `org.apache.ibatis.logging.stdout.StdOutImpl`。
- 认证和 JWT 相关日志过于敏感。
- 存在 `printStackTrace()`。
- Gateway 对每个请求打印 URL，量大时噪声较高。

建议：

- SQL 日志按 profile 开关，生产环境关闭 stdout。
- 日志中禁止输出密钥、token、验证码、完整用户 JSON、身份证、手机号等敏感信息。
- 引入 traceId、userId、requestId 等结构化字段。
- 异常统一使用 `log.warn/error`，保留上下文但脱敏。

收益：

- 提高问题定位效率，同时降低日志泄露风险。

### 12. 线程池配置需要参数化和可观测

现状：

- `hire-api/src/main/java/com/imooc/api/thread/MyThreadPool.java` 固定 core=3、max=10、queue=5000。
- 没有自定义线程名，不利于排查。
- 拒绝策略使用 `AbortPolicy`，但业务侧未看到统一兜底。

建议：

- 将线程池参数迁移到配置。
- 自定义线程名前缀。
- 增加队列长度、活跃线程数、拒绝次数指标。
- 对关键异步任务设置超时、降级和失败告警。

收益：

- 避免流量变化时线程池成为黑盒。

## 测试和交付优化

### 13. 补齐自动化测试基线

现状：

- 主代码约 281 个 Java 文件，测试约 20 个 Java 文件。
- `hire-api`、`hire-pojo`、`payment-center-9060`、`service-user-7001`、`service-work-3001` 等关键模块测试覆盖不足。
- 现有测试中有不少线程、ZK、MinIO、Jasypt 等实验性质用例，业务回归能力有限。

建议：

- 建立三层测试：
  - 单元测试：JWT、Redis key、文件名处理、枚举状态机、工具类。
  - Web 层测试：登录、验证码、网关认证失败响应、文件上传参数错误。
  - 集成测试：使用 Testcontainers 或本地 profile 启动 MySQL、Redis、RabbitMQ、MinIO 的关键链路。
- 对高风险修复先补测试：JWT 过期、固定验证码移除、app 用户上下文解析、MQ 幂等、文件上传校验。
- CI 至少执行 `mvn -DskipTests compile` 和关键模块测试。

收益：

- 给后续重构和依赖升级提供安全网。

### 14. 增加 CI、质量门禁和依赖扫描

现状：

- 仓库中未看到 CI 配置。
- 依赖版本中存在较多手工固定的老版本，例如 Jackson、Guava、commons-fileupload、XStream、JavaCV/FFmpeg、Zookeeper 等，需要统一扫描风险。

建议：

- 增加 GitHub Actions、GitLab CI 或 Jenkins 流水线。
- 质量门禁包含编译、测试、Checkstyle/Spotless、SpotBugs、依赖漏洞扫描、重复代码检查。
- 优先使用 Spring Boot dependency management，不要随意覆盖 Boot 管理的 Jackson 等核心版本。
- 制定依赖升级策略：先补测试，再小步升级 Spring Boot 2.6.x 到更安全的 2.7.x；长期评估迁移到 Spring Boot 3.x 和 Spring Cloud 2022/2023，但这会涉及 Java 17、Jakarta 包名和生态适配。

收益：

- 提前发现安全漏洞、格式漂移和构建失败。

## 推荐落地路线

### 第一阶段：1-2 天内处理

- 轮换并移除已提交的敏感密钥。
- 删除 JWT key、token、短信验证码日志。
- 移除固定验证码 `123456` 的生产绕过。
- 修复 `JWTCurrentUserInterceptor` app 用户上下文解析。
- 修复 `uploadLogo` 重复读取 input stream。
- 将 Gateway CORS 收敛到单一配置，并配置域名白名单。

### 第二阶段：1 周内处理

- 为 JWT、短信登录、网关认证、文件上传、MQ 消费补单元测试和 Web 层测试。
- 将 Redisson、支付中心、对象存储、ZK、Nacos 等配置迁移到属性类和环境变量。
- MQ 消费改为单条 ack，引入重试上限和死信队列。
- 建立基础 CI：编译、关键测试、依赖扫描。
- 梳理 dev/prod profile，补齐配置模板。

### 第三阶段：2-4 周内处理

- 拆分 `hire-common` / `hire-api` 的公共能力边界。
- 重构认证、扫码登录、文件上传、支付回调为应用服务。
- 完善支付回调验签、幂等和状态机。
- 统一分布式锁选型，减少 ZK/Redisson 重叠。
- 增加服务健康检查、指标、告警和日志脱敏规范。

### 第四阶段：长期演进

- 在测试覆盖稳定后升级依赖版本。
- 评估 Java 17、Spring Boot 3.x、Spring Cloud 新版本迁移。
- 对招聘核心业务建立领域模型和状态机，例如企业审核、职位状态、简历投递、订单支付。
- 将 RabbitMQ 示例模块迁移到文档或独立 examples，避免与业务服务混在同一构建主线。

## 建议优先拆出的任务

1. `security-config-cleanup`：密钥迁移、配置模板、密钥轮换清单。
2. `jwt-hardening`：JWT 过期、最小 claims、日志脱敏、登出失效。
3. `gateway-auth-context-fix`：CORS 白名单、内部 header 清理、app 用户上下文修复。
4. `file-upload-hardening`：文件校验、对象 key 标准化、临时文件清理。
5. `mq-reliability`：单条 ack、死信队列、幂等消费、本地消息表闭环。
6. `test-baseline`：关键链路测试和 CI。
7. `common-module-split`：公共模块依赖瘦身和 starter 化。

## 本次检查记录

- 已读取父 POM、各模块 POM、主要配置文件、网关过滤器、JWT 工具、用户上下文拦截器、短信登录、文件上传、MQ 消费和本地消息表相关代码。
- 已统计主代码和测试代码规模：主代码约 281 个 Java 文件，测试约 20 个 Java 文件。
- 已执行 `mvn -q -DskipTests compile`，编译通过。
