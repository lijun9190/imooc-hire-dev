package com.imooc.service;

import com.imooc.pojo.UserPassport;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 风间影月
 * @since 2022-08-04
 */
public interface UserPassportService {

    /**
     * @Description: 查询用户信息
     */
    public UserPassport queryUserInfo(String userId, String pwd);

}
