package com.example.api.user.controller;

import com.example.api.user.entity.User;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public abstract class UserControllerDef {
    @GetMapping("/user")
    public abstract List<User> hola();
}
