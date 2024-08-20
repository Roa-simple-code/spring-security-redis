package com.roa.security_redis.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AdminController {

    @GetMapping("/admin")
    public String admin(){
        return "hello admin";
    }
    @GetMapping("/user")
    public String user(){
        return "hello user";
    }
}
