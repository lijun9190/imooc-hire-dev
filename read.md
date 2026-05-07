# imooc-hire-dev 项目介绍与技术栈解析

## 项目概览

`imooc-hire-dev` 是一个基于 Spring Cloud 微服务体系构建的招聘平台后端工程。项目以 Maven 多模块方式组织，围绕招聘业务拆分出用户、企业、职位/简历、认证、资源、文件、支付、网关等服务，并通过 Nacos 完成服务注册发现和配置管理，通过 Spring Cloud Gateway 统一入口路由。

从模块命名和接口划分来看，系统主要面向招聘求职场景，覆盖用户登录、短信验证码、企业入驻与审核、职位发布、简历维护、举报处理、基础资源字典、文件上传、支付订单、微信支付回调等能力。

## 技术栈

### 基础框架

- Java 8
- Maven 多模块工程
- Spring Boot 2.6.11
- Spring Cloud 2021.0.4
- Spring Cloud Alibaba 2021.0.1.0

### 微服务与治理

- Nacos：服务注册发现、配置中心
- Spring Cloud Gateway：API 网关、统一路由
- OpenFeign：服务间远程调用
- LoadBalancer：客户端负载均衡
- Sentinel：限流、熔断、网关流控
- Seata：分布式事务
- Sleuth + Zipkin：链路追踪

### 数据访问与存储

- MySQL 8 驱动
- MyBatis-Plus 3.5.0
- PageHelper 分页
- Redis：缓存、验证码、登录态等场景支撑
- MongoDB：简历、职位或文档类数据扩展存储
- MinIO / 阿里云 OSS：对象存储、文件上传

### 消息与异步处理

- RabbitMQ / Spring AMQP
- RabbitMQ Java Client
- 延迟队列、死信队列相关配置

### 其他能力

- Spring Validation：参数校验
- Spring AOP：日志、切面增强
- Spring Retry：重试机制
- JJWT：JWT 登录令牌
- Jasypt：配置加密
- Knife4j：接口文档
- Redisson：分布式锁、Redis 高级客户端
- Zookeeper + Curator：分布式协调、锁能力
- Canal：数据同步监听
- JavaCV / FFmpeg：音视频处理能力
- Tencent Cloud SDK、Baidu AI SDK：第三方云能力接入
- Lombok：简化实体和样板代码

## 模块说明

| 模块 | 端口/类型 | 说明 |
| --- | --- | --- |
| `hire-common` | 公共模块 | 通用工具类、枚举、异常处理、统一响应、基础配置、公共依赖集合。 |
| `hire-pojo` | 公共实体模块 | 统一管理 POJO、BO、VO、DTO、Entity 等数据模型，并引入 MongoDB、校验相关能力。 |
| `hire-api` | 公共 API 模块 | Feign 客户端、通用配置、拦截器、MQ 配置、Seata/Sentinel/Redisson/Zookeeper 等跨服务基础能力。 |
| `gateway-8000` | 8000 | Spring Cloud Gateway 网关，负责路由到用户、企业、认证、资源、职位/简历、文件等服务，并包含 JWT 安全过滤、IP 限流、跨域配置。 |
| `auth-service-8111` | 8111 | 认证服务，提供用户登录、短信验证码、登出、管理员登录、扫码登录、MQ 测试等能力。 |
| `service-user-7001` | 7001 | 用户服务，处理用户资料、管理员账号、HR 与企业绑定、用户信息刷新等业务。 |
| `service-company-6001` | 6001 | 企业服务，处理企业信息、企业认证审核、交易订单、支付二维码生成、订单支付通知等业务。 |
| `service-work-3001` | 3001 | 职位与简历服务，处理职位发布/列表/详情、简历初始化与编辑、举报记录和处理等核心招聘业务。 |
| `server-resource-4001` | 4001 | 基础资源服务，提供行业、职位类型、数据字典、系统参数等公共资源。 |
| `service-file-5001` | 5001 | 文件服务，负责头像、文件上传以及对象存储接入。 |
| `payment-center-9060` | 9060 | 支付中心，负责商户订单、微信支付二维码、支付回调通知等支付流程。 |
| `rabbit-producer` | 独立示例模块 | RabbitMQ 生产者示例/实验模块。 |
| `rabbit-consumer` | 独立示例模块 | RabbitMQ 消费者示例/实验模块。 |

## 服务调用与架构关系

系统以 `gateway-8000` 作为统一入口，网关通过 Nacos 服务发现把请求转发到各业务服务：

- `user-service`：用户资料、HR、管理员信息
- `company-service`：企业与交易订单
- `auth-service`：认证、登录、短信验证码
- `resource-service`：行业、职位类型、字典、系统参数
- `work-service`：职位、简历、举报
- `file-service`：文件上传与对象存储

公共模块 `hire-common`、`hire-pojo`、`hire-api` 被业务服务复用，用于统一响应格式、异常处理、实体模型、Feign 调用、拦截器、分布式事务、消息队列、缓存和分布式锁等基础能力。

## 主要业务能力

- 用户端认证：短信验证码、登录、登出、扫码登录。
- 管理端认证：管理员登录、获取管理员信息、登出。
- 用户资料：用户信息修改、HR 与企业绑定、用户统计信息。
- 企业管理：企业创建、企业信息查询、企业审核。
- 招聘业务：职位发布、职位列表、职位详情、简历初始化、简历编辑、简历查询。
- 资源管理：行业树、职位类型树、数据字典、系统参数。
- 文件能力：头像和文件上传，支持 MinIO / OSS。
- 支付能力：商户订单、微信支付二维码、支付结果通知。
- 消息能力：RabbitMQ 异步消息、延迟队列、死信队列。

## 本地运行要点

1. 使用 Java 8 和 Maven 构建项目。
2. 先启动基础设施：Nacos、MySQL、Redis、RabbitMQ、Seata、Sentinel Dashboard、MongoDB、对象存储服务等。
3. 根据各模块 `application-dev.yml` 修改数据库、Redis、MQ、Nacos、Seata、MongoDB、对象存储等连接地址。
4. 按依赖关系启动服务，常见顺序为：基础服务 -> 业务服务 -> 网关。
5. 通过 `gateway-8000` 访问系统接口，具体路由规则可查看 `gateway-8000/src/main/resources/application-dev.yml`。

## 项目特点

- 采用清晰的 Maven 多模块结构，公共能力与业务服务拆分明确。
- 微服务组件较完整，覆盖注册发现、网关、限流、熔断、远程调用、链路追踪、分布式事务等常见治理能力。
- 业务边界按招聘平台领域拆分，用户、企业、职位/简历、资源、文件、支付相对独立。
- 基础设施依赖较多，适合学习或演示完整 Spring Cloud Alibaba 微服务体系。
- 配置中包含较多本地或局域网地址，部署到新环境时需要重点检查各服务的 `application-dev.yml`。
