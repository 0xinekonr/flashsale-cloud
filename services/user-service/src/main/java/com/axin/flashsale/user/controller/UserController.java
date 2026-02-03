package com.axin.flashsale.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RefreshScope
public class UserController {

    @Value("${users.greeting:Default Local Hello}")
    private String greeting;

    @GetMapping("/ping")
    public String ping() {
        return "user-service ok";
    }

    @GetMapping("/config-test")
    public String configTest(){
        return greeting;
    }

    @GetMapping("/{id}")
    public String getUser(@PathVariable String id) {
        return "user#" + id;
    }
}
