package com.example.api.user.controller;
import com.example.api.user.entity.User;
import com.example.api.user.service.UserService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController extends UserControllerDef{
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @Override
    public List<User> hola() {
        return userService.findAll();

    }
}