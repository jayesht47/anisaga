package com.anisaga.user_service.services.impl;

import com.anisaga.user_service.entities.User;
import com.anisaga.user_service.exceptions.DuplicateUserNameException;
import com.anisaga.user_service.repositories.UserRepository;
import com.anisaga.user_service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User upsertUser(User user) throws DuplicateUserNameException {
        try {
            User existingUser = getUserByUserName(user.getUsername());
            if (existingUser != null)
                throw new DuplicateUserNameException(String.format("user already present with userName : %s", user.getUsername()));
        } catch (UsernameNotFoundException ignored) {
            // ignoring since in upsert if the user does not exist we will create it since call would be from register user controller
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserByUserId(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserByUserName(String userName) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUserName(userName);
        if (user.isPresent()) return user.get();
        throw new UsernameNotFoundException(String.format("userName not found for user %s", userName));
    }

}
