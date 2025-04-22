package com.anisaga.anisaga_service.services.impl;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.entities.User;
import com.anisaga.anisaga_service.exceptions.BadRequestException;
import com.anisaga.anisaga_service.exceptions.DuplicateUserNameException;
import com.anisaga.anisaga_service.repositories.UserRepository;
import com.anisaga.anisaga_service.services.AiService;
import com.anisaga.anisaga_service.services.AnimeService;
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

    @Autowired
    private AiService aiService;

    @Autowired
    private AnimeService animeService;

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

    @Override
    public List<Anime> getRecommendations(String userName) {
        User user = getUserByUserName(userName);
        List<String> recommendations = user.getRecommendations();
        if (recommendations != null && !recommendations.isEmpty())
            return animeService.getAnimeListBySlugs(user.getRecommendations());
        return new ArrayList<>();
    }


    @Override
    public void updateRecommendations(String userName) throws BadRequestException {
        User user = getUserByUserName(userName);
        List<String> likes = user.getLikedAnime();
        List<String> recommendationSlugs = new ArrayList<>();
        List<Anime> recommendations = aiService.getAnimeRecommendations(likes);
        recommendations.forEach(anime -> recommendationSlugs.add(anime.getSlug()));
        user.setRecommendations(recommendationSlugs);
        userRepository.save(user);
    }
}
