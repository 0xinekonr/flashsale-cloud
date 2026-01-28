package com.axin.flashsale.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("/users")
public class UserController {

    @GetMapping("/ping")
    public String ping() {
        return "user-service ok";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable Long id) {
        return "user#" + id;
    }
}
