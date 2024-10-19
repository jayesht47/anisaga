package com.anisaga.user_service.controllers;

import com.anisaga.user_service.VO.LoginUser;
import com.anisaga.user_service.VO.RegisterUser;
import com.anisaga.user_service.entities.User;
import com.anisaga.user_service.exceptions.DuplicateUserNameException;
import com.anisaga.user_service.services.UserService;
import com.anisaga.user_service.services.impl.UserDetailService;
import com.anisaga.user_service.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserDetailService userDetailService;

    @Autowired
    UserService userService;

    @Autowired
    AuthenticationManager authenticationManager;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUser loginUser) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUser.getUserName(), loginUser.getPassword()));

            if (authentication.isAuthenticated()) {
                log.info("Logged In");
                User user = userService.getUserByUserName(loginUser.getUserName());
                String token = jwtUtil.generateToken(user);
                responseMap.put("error", false);
                responseMap.put("message", "Logged In");
                responseMap.put("token", token);
                return ResponseEntity.status(200).body(responseMap);
            } else {
                responseMap.put("error", true);
                responseMap.put("message", "Invalid Credentials");
                return ResponseEntity.status(401).body(responseMap);
            }
        } catch (DisabledException e) {
            responseMap.put("error", true);
            responseMap.put("message", "User is Disabled");
            log.error("User is Disabled", e);
            return ResponseEntity.status(500).body(responseMap);
        } catch (BadCredentialsException e) {
            responseMap.put("error", true);
            responseMap.put("message", "Invalid Login credentials");
            log.error("Invalid Login credentials", e);
            return ResponseEntity.status(401).body(responseMap);
        } catch (Exception e) {
            responseMap.put("error", true);
            responseMap.put("message", "Internal Application Error occurred");
            log.error("Internal Error occurred in login", e);
            return ResponseEntity.status(500).body(responseMap);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUser registerUser) throws DuplicateUserNameException {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            if (registerUser != null) {
                User user = new User();
                if (!registerUser.getUserName().isBlank() && !registerUser.getPassword().isBlank()) {
                    user.setUserName(registerUser.getUserName());
                    user.setPassword(new BCryptPasswordEncoder().encode(registerUser.getPassword()));
                    user.setEnabled(true);
                    user.setAccountNonExpired(true);
                    user.setAccountNonLocked(true);
                    user.setCredentialsNonExpired(true);
                    user.setAuthorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
                    userService.upsertUser(user);
                    String token = jwtUtil.generateToken(user);
                    responseMap.put("error", false);
                    responseMap.put("message", "User Registered Successfully");
                    responseMap.put("token", token);
                    return ResponseEntity.status(HttpStatus.OK).body(responseMap);

                } else {
                    throw new BadCredentialsException("Empty Username or Password");
                }
            } else {
                throw new IllegalArgumentException("Null registerUser received");
            }
        } catch (IllegalArgumentException e) {
            responseMap.put("error", true);
            responseMap.put("message", "Bad Request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }

    }
}
