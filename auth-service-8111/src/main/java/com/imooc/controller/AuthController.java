package com.imooc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("a")
@Slf4j
public class AuthController {

    @GetMapping("hello")
    public Object hello() {
        return "Hello Auth Service~~~";
    }

}
