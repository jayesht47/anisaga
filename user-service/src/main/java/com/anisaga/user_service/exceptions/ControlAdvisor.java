package com.anisaga.user_service.exceptions;

import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControlAdvisor {


    @ExceptionHandler(value = UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> userNotFoundExceptionHandler(UsernameNotFoundException e) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("error", false);
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
    }

    @ExceptionHandler(value = DuplicateUserNameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> duplicateUserNameFoundExceptionHandler(DuplicateUserNameException e) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("error", true);
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> badCredentialsExceptionHandler(BadCredentialsException e) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("error", true);
        responseMap.put("message", "Invalid Credentials");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
    }

    @ExceptionHandler(value = ResponseStatusException.class)
    public ResponseEntity<?> responseStatusExceptionHandler(ResponseStatusException e) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("error", true);
        if (e.getStatusCode().equals(HttpStatus.NOT_FOUND))
            responseMap.put("message", e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(responseMap);
    }

    @ExceptionHandler(value = SignatureException.class)
    public ResponseEntity<?> signatureExceptionHandler(SignatureException e) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("error", true);
        responseMap.put("message", "The Token Signature verification failed");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMap);
    }

}
