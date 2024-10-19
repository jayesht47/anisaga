package com.anisaga.user_service.controllers;

import com.anisaga.user_service.entities.User;
import com.anisaga.user_service.exceptions.DuplicateUserNameException;
import com.anisaga.user_service.services.UserService;
import com.anisaga.user_service.util.JwtUtil;
import lombok.experimental.PackagePrivate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/{userId}")
    public User getUserByUserId(@PathVariable("userId") String userId) {
        Optional<User> user = userService.getUserByUserId(userId);
        if (user.isPresent()) return user.get();
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid userId");
    }

    @DeleteMapping("/user/{userId}")
    public void deleteUserByUserId(@PathVariable("userId") String userId) {
        Optional<User> user = userService.getUserByUserId(userId);
        if (user.isPresent()) userService.deleteUser(user.get());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid userId");
    }
}
