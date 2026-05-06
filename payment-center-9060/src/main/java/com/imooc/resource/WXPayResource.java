package com.imooc.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

//@Configuration
@Component
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix="wxpay")
@PropertySource("classpath:wxpay.properties")
public class WXPayResource {

	private String qrcodeKey;
	private long qrcodeExpire;

	private String appId;
	private String merchantId;
	private String secrectKey;

	private String spbillCreateIp;
	private String notifyUrl;

	private String tradeType;
	private String placeOrderUrl;

}
