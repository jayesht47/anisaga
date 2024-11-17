package com.anisaga.anisaga_service.controllers;

import com.anisaga.anisaga_service.entities.User;
import com.anisaga.anisaga_service.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/user/{userName}")
    public User getUserByUserName(@PathVariable("userName") String userName) {
        return userService.getUserByUserName(userName);
    }


    @DeleteMapping("/user/{userId}")
    public void deleteUserByUserId(@PathVariable("userId") String userId) {
        Optional<User> user = userService.getUserByUserId(userId);
        if (user.isPresent()) userService.deleteUser(user.get());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid userId");
    }
}
