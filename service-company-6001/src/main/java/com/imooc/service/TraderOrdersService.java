package com.imooc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imooc.enums.OrderStatus;
import com.imooc.enums.PayMethod;
import com.imooc.pojo.Orders;
import com.imooc.utils.PagedGridResult;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
public interface TraderOrdersService {

    /**
     * 创建订单
     * @return
     */
    public String createOrder(String userId,
                              String companyId,
                              String itemName,
                              PayMethod payMethod,
                              Integer totalAmount);

    /**
     * 修改订单状态
     * @param orderId
     * @param orderStatus
     */
    public void updateOrderStatus(String orderId, OrderStatus orderStatus);

    /**
     * 查询分页订单
     * @param companyId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryOrderListPaged(String companyId,
                                               Integer page,
                                               Integer pageSize);

}
