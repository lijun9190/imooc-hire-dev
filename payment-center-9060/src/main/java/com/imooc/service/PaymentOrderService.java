package com.imooc.service;

import com.imooc.enums.PaymentStatus;
import com.imooc.pojo.MerchantOrders;
import com.imooc.pojo.Orders;
import com.imooc.pojo.bo.MerchantOrdersBO;

public interface PaymentOrderService {

    /**
     * @Description: 创建支付中心的订单
     */
    public boolean createPaymentOrder(MerchantOrdersBO merchantOrdersBO);

    /**
     * @Description: 查询订单信息
     */
    public MerchantOrders queryOrderInfo(String merchantOrderId, PaymentStatus paymentStatus);

    /**
     * @Description: 修改订单状态为已支付
     */
    public String updateOrderPaid(String merchantOrderId);

}

