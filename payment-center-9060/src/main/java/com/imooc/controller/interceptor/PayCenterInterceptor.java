package com.imooc.controller.interceptor;

import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.UserPassport;
import com.imooc.service.UserPassportService;
import com.imooc.utils.LocalDateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PayCenterInterceptor implements HandlerInterceptor {

	@Autowired
	private UserPassportService userPassportService;

	/**
	 * 拦截请求，在controller调用之前
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object arg2) throws Exception {
		String imoocUserId = request.getHeader("imoocUserId");
		String password = request.getHeader("password");

		if (StringUtils.isNotBlank(imoocUserId) && StringUtils.isNotBlank(password)) {

			// 请求数据库查询用户是否存在
			UserPassport user = userPassportService.queryUserInfo(imoocUserId, password);
			if (user == null) {
				GraceException.display(ResponseStatusEnum.PAYMENT_USER_INFO_ERROR);
				return false;
			}

			long expireDays = LocalDateUtils.getChronoUnitBetween(LocalDate.now(), user.getEndDate(), ChronoUnit.DAYS, false);

			if (expireDays < 0) {
				GraceException.display(ResponseStatusEnum.PAYMENT_ACCOUT_EXPIRE_ERROR);
				return false;
			}

			// 判断限制访问次数
			/*Integer limit = user.getLimit();
			if (limit != -1) {
				// -1 代表访问无限次
			}*/

		} else {
			GraceException.display(ResponseStatusEnum.PAYMENT_HEADERS_ERROR);
			return false;
		}
		
		
		/**
		 * 返回 false：请求被拦截，返回
		 * 返回 true ：请求OK，可以继续执行，放行
		 */
		return true;
	}
	
	/**
	 * 请求controller之后，渲染视图之前
	 */
	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
	}
	
	/**
	 * 请求controller之后，视图渲染之后
	 */
	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
	}

}
