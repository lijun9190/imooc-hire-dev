package com.imooc.controller;

import com.imooc.grace.result.GraceJSONResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("pay")
public class HelloController {

//    @Autowired
//    private Snowflake snowflake;

    @GetMapping("hello")
    public GraceJSONResult hello() {
//        String id = snowflake.nextId();
//        System.out.println(id);

        return GraceJSONResult.ok("Hello payment-center-9060 ~");
    }

//    @Autowired
//    private TeacherFeignService teacherFeignService;
//
//    @GetMapping("getTeacherFeign")
//    public Object getTeacherFeign() {
//        GraceJSONResult result = teacherFeignService.queryTeacher();
//        return result;
//    }


}
