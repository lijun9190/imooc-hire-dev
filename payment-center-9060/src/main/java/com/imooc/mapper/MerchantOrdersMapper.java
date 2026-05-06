package com.imooc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imooc.pojo.MerchantOrders;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 商户订单表，用于在支付中心存储查询，并且校验订单的支付状态 Mapper 接口
 * </p>
 *
 * @author 风间影月
 * @since 2022-08-04
 */
@Repository
public interface MerchantOrdersMapper extends BaseMapper<MerchantOrders> {

}
