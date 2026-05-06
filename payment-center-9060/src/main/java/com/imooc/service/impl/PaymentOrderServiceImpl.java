package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.imooc.enums.PaymentStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.mapper.MerchantOrdersMapper;
import com.imooc.pojo.MerchantOrders;
import com.imooc.pojo.Orders;
import com.imooc.pojo.bo.MerchantOrdersBO;
import com.imooc.service.PaymentOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class PaymentOrderServiceImpl implements PaymentOrderService {

	@Autowired
	private MerchantOrdersMapper merchantOrdersMapper;

	@Transactional
	@Override
	public boolean createPaymentOrder(MerchantOrdersBO merchantOrdersBO) {
		MerchantOrders paymentOrder = new MerchantOrders();
		BeanUtils.copyProperties(merchantOrdersBO, paymentOrder);

		paymentOrder.setPayStatus(PaymentStatus.WAIT_PAY.type);
		paymentOrder.setIsDelete(YesOrNo.NO.type);
		paymentOrder.setCreatedTime(LocalDateTime.now());
		paymentOrder.setUpdatedTime(LocalDateTime.now());

		int result = merchantOrdersMapper.insert(paymentOrder);
		return result == 1 ? true : false;
	}

	@Override
	public MerchantOrders queryOrderInfo(String merchantOrderId, PaymentStatus paymentStatus) {

		QueryWrapper queryWrapper = new QueryWrapper<MerchantOrders>();
		queryWrapper.eq("merchant_order_id", merchantOrderId);

		if (paymentStatus != null) {
			queryWrapper.eq("pay_status", paymentStatus.type);
		}

		return merchantOrdersMapper.selectOne(queryWrapper);
	}

	@Transactional
	@Override
	public String updateOrderPaid(String merchantOrderId) {

		MerchantOrders merchantOrders = new MerchantOrders();
		merchantOrders.setPayStatus(PaymentStatus.PAID.type);
		merchantOrders.setUpdatedTime(LocalDateTime.now());

		QueryWrapper queryWrapper = new QueryWrapper<MerchantOrders>();
		queryWrapper.eq("merchant_order_id", merchantOrderId);
		merchantOrdersMapper.update(merchantOrders, queryWrapper);

		MerchantOrders vo = queryOrderInfo(merchantOrderId, null);
		return vo.getReturnUrl();
	}
}
