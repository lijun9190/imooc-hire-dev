package com.imooc.controller;

import com.google.gson.Gson;
import com.imooc.api.intercept.JWTCurrentUserInterceptor;
import com.imooc.base.BaseInfoProperties;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.IMOOCJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Admin;
import com.imooc.pojo.Users;
import com.imooc.pojo.test.Stu;
import com.imooc.service.StuService;
import com.imooc.utils.MyInfo;
import com.imooc.utils.SMSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("u")
@Slf4j
public class HelloController extends BaseInfoProperties {

    @Autowired
    private StuService stuService;

    @Autowired
    private SMSUtils smsUtils;

    @GetMapping("sms")
    public Object sms() throws Exception {

        smsUtils.sendSMS(MyInfo.getMobile(), "9875");

        return "Send SMS OK~~~";
    }

    @Value("${server.port}")
    private String port;

    @GetMapping("stu")
    public Object stu() {

        com.imooc.pojo.Stu stu = new com.imooc.pojo.Stu();
//        stu.setId("1001");
        stu.setAge(18);
        stu.setName("慕课网 www.imooc.com");

        stuService.save(stu);

        return "OK";
    }

    @GetMapping("hello")
    public Object hello(HttpServletRequest request) {
        String userJson = request.getHeader(APP_USER_JSON);
        Users jwtUser = new Gson().fromJson(userJson, Users.class);
        log.info(jwtUser.toString());


        Users currentUser = JWTCurrentUserInterceptor.currentUser.get();
//        Admin adminUser = JWTCurrentUserInterceptor.adminUser.get();
        log.info("JWTCurrentUserInterceptor：" + currentUser.toString());
//        log.info("JWTCurrentUserInterceptor：" + adminUser.toString());


        Stu stu = new Stu(1001, "imooc", 18);

//        System.out.println(stu.toString());
        log.info("info：" + stu.toString());
        log.debug("debug：" + stu.toString());
        log.warn("warn：" + stu.toString());
        log.error("error：{}", stu.toString());

        log.info("lb测试，当前端口号为：" + port);

        return "Hello UserService~~~";
    }

    @GetMapping("hello2")
    public IMOOCJSONResult hello2() {
        Stu stu = new Stu(1001, "imooc", 18);

        return IMOOCJSONResult.ok(stu);
//        return IMOOCJSONResult.ok("添加成功！");
//        return IMOOCJSONResult.errorMsg("修改出错，请联系管理员");
//        return IMOOCJSONResult.errorMap(map)
//        return IMOOCJSONResult.errorUserTicket("用户会话校验失败！");
    }

    @GetMapping("hello3")
    public GraceJSONResult hello3() {
        Stu stu = new Stu(1001, "imooc", 18);

//        return GraceJSONResult.ok(stu);
//        return IMOOCJSONResult.ok("添加成功！");
//        return IMOOCJSONResult.errorMsg("修改出错，请联系管理员");
//        return IMOOCJSONResult.errorMap(map)
        return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_IO);
    }
}
