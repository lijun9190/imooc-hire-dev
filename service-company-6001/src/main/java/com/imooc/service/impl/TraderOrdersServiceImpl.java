package com.imooc.service.impl;

import com.a3test.component.idworker.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.OrderStatus;
import com.imooc.enums.PayMethod;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.mapper.OrdersMapper;
import com.imooc.pojo.Company;
import com.imooc.pojo.Orders;
import com.imooc.pojo.bo.MerchantOrdersBO;
import com.imooc.service.CompanyService;
import com.imooc.service.TraderOrdersService;
import com.imooc.utils.LocalDateUtils;
import com.imooc.utils.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
@Service
public class TraderOrdersServiceImpl extends BaseInfoProperties implements TraderOrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private Snowflake snowflake;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CompanyService companyService;

    @Transactional
    @Override
    public String createOrder(String userId,
                              String companyId,
                              String itemName,
                              PayMethod payMethod,
                              Integer totalAmount) {

        // 我们这里把order表的主键id作为订单变化[orderNum]
        String prefix = LocalDateUtils.format(LocalDateTime.now(),
                                              LocalDateUtils.DATETIME_PATTERN_3);
        String sid = snowflake.nextId();
        String orderNum = prefix + sid;




        // 1. 创建本地的用户订单
        Orders newOrder = new Orders();
        newOrder.setId(orderNum);

        newOrder.setUserId(userId);
        newOrder.setCompanyId(companyId);
        newOrder.setItemName(itemName);
        newOrder.setPayMethod(payMethod.type);
        newOrder.setTotalAmount(totalAmount);
        newOrder.setRealPayAmount(totalAmount);
        newOrder.setPostAmount(0);
        newOrder.setStatus(OrderStatus.WAIT_PAY.type);

        newOrder.setCreatedTime(LocalDateTime.now());
        newOrder.setUpdatedTime(LocalDateTime.now());

        ordersMapper.insert(newOrder);

        // 2. 创建本地订单以后，向支付中心发起这个订单，保存订单中的主要数据，作为商户(预交易)订单信息
        MerchantOrdersBO merchantOrdersBO = new MerchantOrdersBO();
        merchantOrdersBO.setMerchantOrderId(orderNum);
        merchantOrdersBO.setMerchantUserId(userId);
        merchantOrdersBO.setMerchantCompanyId(companyId);
        merchantOrdersBO.setAmount(totalAmount);
        merchantOrdersBO.setPayMethod(payMethod.type);
        merchantOrdersBO.setReturnUrl(PAY_RETURN_URL);    // 设置回调地址，用于支付成功后修改订单状态
        merchantOrdersBO.setComeFrom("慕聘网 by:风间影月");

        HttpHeaders headers = getHeadersForWxPay();

        // 远程通信：不适用feign，因为支付中心和本地项目不在一个地方，没有使用同一个注册中心，所以只能使用rest形式发送请求
        HttpEntity<MerchantOrdersBO> entity = new HttpEntity<>(merchantOrdersBO, headers);
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate
                                .postForEntity(PAYMENT_URL_CREATE_MERCHANT_ORDER,
                                               entity,
                                               GraceJSONResult.class);

        GraceJSONResult paymentResult = responseEntity.getBody();
        System.out.println(paymentResult.getMsg());
        if (paymentResult.getStatus() != 200) {
            GraceException.display(ResponseStatusEnum.PAYMENT_ORDER_CREATE_ERROR);
        }

        return orderNum;
    }

    @Transactional
    @Override
    public void updateOrderStatus(String orderId,
                                  OrderStatus orderStatus) {
        Orders paidOrder = new Orders();
        paidOrder.setId(orderId);
        paidOrder.setStatus(orderStatus.type);
        paidOrder.setUpdatedTime(LocalDateTime.now());

        ordersMapper.updateById(paidOrder);

        // 判断当前企业是否vip：
        // 如果是vip，则在此基础上延长1个月
        // 如果不是vip，则把当前的时间延长1个月进行设置
        Orders companyOrder = ordersMapper.selectById(orderId);
        String companyId = companyOrder.getCompanyId();
        Company company = companyService.getById(companyId);

        boolean isVip = companyService.getIsVip(companyId);

        LocalDate vipExpireDate = LocalDate.now();
        if (isVip) {
            vipExpireDate = company.getVipExpireDate();
        }
        LocalDate newExpireDate = LocalDateUtils.plus(vipExpireDate,
                                                    31,
                                                    ChronoUnit.DAYS);

        companyService.setCompanyVip(companyId, newExpireDate);

        // 充值完毕以后立刻生效，删除redis中缓存数据
        redis.del(REDIS_COMPANY_IS_VIP + ":" + companyId);
    }

    @Override
    public PagedGridResult queryOrderListPaged(String companyId,
                                               Integer page,
                                               Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        List<Orders> ordersList = ordersMapper.selectList(
                                        new QueryWrapper<Orders>()
                                            .eq("company_id", companyId)
        );

        return setterPagedGrid(ordersList, page);
    }
}
