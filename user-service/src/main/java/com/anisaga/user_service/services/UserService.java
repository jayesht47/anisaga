package com.anisaga.user_service.services;

import com.anisaga.user_service.entities.User;
import com.anisaga.user_service.exceptions.DuplicateUserNameException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {

    User upsertUser(User user) throws DuplicateUserNameException;

    Optional<User> getUserByUserId(String userId);

    void deleteUser(User user);


    User getUserByUserName(String userName);
}
