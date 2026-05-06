package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.imooc.mapper.UserPassportMapper;
import com.imooc.pojo.UserPassport;
import com.imooc.service.UserPassportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserPassportServiceImpl implements UserPassportService {

    @Autowired
    private UserPassportMapper userPassportMapper;

    @Override
    public UserPassport queryUserInfo(String userId, String pwd) {
        return userPassportMapper.selectOne(new QueryWrapper<UserPassport>()
                .eq("imooc_user_id", userId)
                .eq("password", pwd)
        );
    }

}
