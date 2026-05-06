package com.imooc.controller;

import com.google.gson.Gson;
import com.imooc.api.intercept.JWTCurrentUserInterceptor;
import com.imooc.base.BaseInfoProperties;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Admin;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.AdminBO;
import com.imooc.pojo.bo.RegistLoginBO;
import com.imooc.pojo.vo.AdminVO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.AdminService;
import com.imooc.service.UsersService;
import com.imooc.utils.IPUtil;
import com.imooc.utils.JWTUtils;
import com.imooc.utils.SMSUtils;
import io.jsonwebtoken.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("admin")
@Slf4j
public class AdminController extends BaseInfoProperties {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("login")
    public GraceJSONResult getSMSCode(@Valid @RequestBody AdminBO adminBO){

        // 执行登录判断用户是否存在
        boolean isExist = adminService.adminLogin(adminBO);
        if (!isExist)
            return GraceJSONResult.errorCustom(
                    ResponseStatusEnum.ADMIN_LOGIN_ERROR);

        // 登录成功之后获得admin信息
        Admin admin = adminService.getAdminInfo(adminBO);
        String adminToken = jwtUtils.createJWTWithPrefix(new Gson().toJson(admin),
                                    TOKEN_ADMIN_PREFIX);

        return GraceJSONResult.ok(adminToken);
    }

    @GetMapping("info")
    public GraceJSONResult info() {

        Admin admin = JWTCurrentUserInterceptor.adminUser.get();

        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);

        return GraceJSONResult.ok(adminVO);
    }

    @PostMapping("logout")
    public GraceJSONResult logout() {
        return GraceJSONResult.ok();
    }
}
