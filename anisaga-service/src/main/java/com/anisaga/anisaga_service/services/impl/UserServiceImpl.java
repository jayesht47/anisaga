package com.anisaga.anisaga_service.services.impl;

import com.anisaga.anisaga_service.entities.User;
import com.anisaga.anisaga_service.exceptions.DuplicateUserNameException;
import com.anisaga.anisaga_service.repositories.UserRepository;
import com.anisaga.anisaga_service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Override
    public void addToLikes(String userName, String animeSlug) {
        User user = getUserByUserName(userName);
        List<String> updatedLikedAnimeList = user.getLikedAnime() != null ? user.getLikedAnime() : new ArrayList<>();
        if (!updatedLikedAnimeList.contains(animeSlug)) {
            updatedLikedAnimeList.add(animeSlug);
            user.setLikedAnime(updatedLikedAnimeList);
            userRepository.save(user);
        }
    }

    @Override
    public void removeFromLikes(String userName, String animeSlug) {
        User user = getUserByUserName(userName);
        List<String> updatedLikedAnimeList = user.getLikedAnime() != null ? user.getLikedAnime() : new ArrayList<>();
        updatedLikedAnimeList.remove(animeSlug);
        user.setLikedAnime(updatedLikedAnimeList);
        userRepository.save(user);
    }

    @Override
    public List<String> getLikes(String userName) {
        User user = getUserByUserName(userName);
        return user.getLikedAnime();
    }
}
