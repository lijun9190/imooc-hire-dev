package com.imooc.controller;


import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.PayMethod;
import com.imooc.enums.PaymentStatus;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.MerchantOrders;
import com.imooc.pojo.bo.MerchantOrdersBO;
import com.imooc.resource.WXPayResource;
import com.imooc.service.PaymentOrderService;
import com.imooc.wx.entity.PreOrderResult;
import com.imooc.wx.service.WxOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("payment")
public class PaymentController extends BaseInfoProperties {

    @Autowired
    private PaymentOrderService paymentOrderService;

    @Autowired
    private WXPayResource wxPayResource;

    @Autowired
    private WxOrderService wxOrderService;

    /**
     * 接受商户订单信息，保存到自己的数据库
     */
    @PostMapping("createMerchantOrder")
    public GraceJSONResult createMerchantOrder(@RequestBody MerchantOrdersBO merchantOrdersBO) throws Exception {

        String merchantOrderId = merchantOrdersBO.getMerchantOrderId();     // 订单id
        String merchantUserId = merchantOrdersBO.getMerchantUserId();       // 用户id
        String merchantCompanyId = merchantOrdersBO.getMerchantCompanyId();        // 用户所在企业id，可以为空
        Integer amount = merchantOrdersBO.getAmount();                      // 实际支付订单金额
        Integer payMethod = merchantOrdersBO.getPayMethod();          	    // 支付方式
        String returnUrl = merchantOrdersBO.getReturnUrl();           	    // 支付成功后的回调地址（学生自定义）
        String comeFrom = merchantOrdersBO.getComeFrom();           	    // 支付来源

        if (StringUtils.isBlank(merchantOrderId)) {
            return GraceJSONResult.errorMsg("参数[orderId]不能为空");
        }
        if (StringUtils.isBlank(merchantUserId)) {
            return GraceJSONResult.errorMsg("参数[userId]不能为空");
        }
        if (StringUtils.isBlank(merchantCompanyId)) {
            return GraceJSONResult.errorMsg("参数[merchantCompanyId]不能为空");
        }
        if (amount == null || amount < 1) {
            return GraceJSONResult.errorMsg("参数[realPayAmount]不能为空并且不能小于1");
        }
        if (payMethod == null) {
            return GraceJSONResult.errorMsg("参数[payMethod]不能为空并且不能小于1");
        }
        if (payMethod != PayMethod.WEIXIN.type && payMethod != PayMethod.ALIPAY.type) {
            return GraceJSONResult.errorMsg("参数[payMethod]目前只支持微信支付或支付宝支付");
        }
        if (StringUtils.isBlank(returnUrl)) {
            return GraceJSONResult.errorMsg("参数[returnUrl]不能为空");
        }
        if (StringUtils.isBlank(comeFrom)) {
            return GraceJSONResult.errorMsg("参数[comeFrom]不能为空");
        }

        // 保存传来的商户订单信息
        boolean isSuccess = false;
        try {
            isSuccess = paymentOrderService.createPaymentOrder(merchantOrdersBO);
        } catch (Exception e) {
            e.printStackTrace();
            GraceJSONResult.errorMsg(e.getMessage());
        }

        if (isSuccess) {
            return GraceJSONResult.ok("商户订单创建成功！");
        } else {
            return GraceJSONResult.errorMsg("商户订单创建失败，请重试...");
        }
    }

    /**
     * 提供给大家查询的方法，用于查询订单信息
     * @param merchantOrderId
     * @return
     */
    @PostMapping("getMerchantOrderInfo")
    public GraceJSONResult getMerchantOrderInfo(String merchantOrderId) {

        if (StringUtils.isBlank(merchantOrderId)) {
            return GraceJSONResult.errorMsg("查询参数[merchantOrderId]不能为空！");
        }

        MerchantOrders orderInfo = paymentOrderService.queryOrderInfo(merchantOrderId,
                                                        null);

        return GraceJSONResult.ok(orderInfo);
    }


    /******************************************  以下方法开始支付流程   ******************************************/

    @PostMapping(value="/getWXPayQRCode")
    public GraceJSONResult getWXPayQRCode(@RequestBody MerchantOrdersBO merchantOrdersBO) throws Exception{

        String merchantOrderId = merchantOrdersBO.getMerchantOrderId();
        if (StringUtils.isBlank(merchantOrderId)) {
            return GraceJSONResult.errorMsg("查询参数[merchantOrderId]不能为空！");
        }

        // 查询商户订单详情
        MerchantOrders waitPayOrder = paymentOrderService.queryOrderInfo(merchantOrderId, PaymentStatus.WAIT_PAY);

        if (waitPayOrder != null) {
            // 商品描述
            String body = waitPayOrder.getComeFrom() + "VIP充值 - 付款单号[" + merchantOrderId + "]";
            // 商户订单号
            String out_trade_no = merchantOrderId;
            // 从redis中去获得这笔订单的微信支付二维码，如果订单状态没有支付没有就放入，这样的做法防止用户频繁刷新而调用微信接口

            String qrCodeUrl = redis.get(wxPayResource.getQrcodeKey() + ":" + merchantOrderId);

            if (StringUtils.isEmpty(qrCodeUrl)) {
                // 订单总金额，单位为分
                String total_fee = String.valueOf(waitPayOrder.getAmount());
//				String total_fee = "1";	// 测试用 1分钱

                // 统一下单
                PreOrderResult preOrderResult = wxOrderService.placeOrder(body, out_trade_no, total_fee);
                if (preOrderResult.getReturn_code().equalsIgnoreCase("FAIL")) {
                    return GraceJSONResult.errorMsg(preOrderResult.getReturn_msg());
                }
                qrCodeUrl = preOrderResult.getCode_url();
                System.out.println("qrCodeUrl = " + qrCodeUrl);
            }

//            PaymentInfoVO paymentInfoVO = new PaymentInfoVO();
//            paymentInfoVO.setAmount(waitPayOrder.getAmount());
//            paymentInfoVO.setMerchantOrderId(merchantOrderId);
//            paymentInfoVO.setMerchantUserId(merchantUserId);
//            paymentInfoVO.setQrCodeUrl(qrCodeUrl);

            redis.set(wxPayResource.getQrcodeKey() + ":" + merchantOrderId, qrCodeUrl, wxPayResource.getQrcodeExpire());

            return GraceJSONResult.ok(qrCodeUrl);
        } else {
            return GraceJSONResult.errorMsg("该订单不存在，或已经支付");
        }
    }

}

