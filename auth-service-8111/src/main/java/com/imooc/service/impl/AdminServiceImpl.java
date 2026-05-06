package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imooc.mapper.AdminMapper;
import com.imooc.pojo.Admin;
import com.imooc.pojo.bo.AdminBO;
import com.imooc.service.AdminService;
import com.imooc.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 慕聘网运营管理系统的admin账户表，仅登录，不提供注册 服务实现类
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public boolean adminLogin(AdminBO adminBO) {

        // 根据用户名获得盐slat
//        Admin admin = adminMapper.selectOne(
//                new QueryWrapper<Admin>()
//                        .eq("username", adminBO.getUsername())
//        );
        Admin admin = getSelfAdmin(adminBO.getUsername());

        // 判断admin是否为空来返回true或false
        if (admin == null) {
            return false;
        } else {
            String slat = admin.getSlat();
            String md5Str = MD5Utils.encrypt(adminBO.getPassword(), slat);
            if (md5Str.equalsIgnoreCase(admin.getPassword())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Admin getAdminInfo(AdminBO adminBO) {
        return this.getSelfAdmin(adminBO.getUsername());
    }

    private Admin getSelfAdmin(String username) {
        Admin admin = adminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .eq("username", username)
        );
        return admin;
    }
}
