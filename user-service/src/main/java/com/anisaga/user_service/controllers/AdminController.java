package com.anisaga.user_service.controllers;

import com.anisaga.user_service.entities.User;
import com.anisaga.user_service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserService userService;

    @GetMapping("/get-all-users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

}
