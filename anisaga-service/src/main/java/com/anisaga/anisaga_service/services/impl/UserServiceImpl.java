package com.anisaga.anisaga_service.services.impl;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.entities.User;
import com.anisaga.anisaga_service.exceptions.BadRequestException;
import com.anisaga.anisaga_service.exceptions.DuplicateUserNameException;
import com.anisaga.anisaga_service.repositories.UserRepository;
import com.anisaga.anisaga_service.services.AiService;
import com.anisaga.anisaga_service.services.AnimeService;
import com.anisaga.anisaga_service.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiService aiService;

    @Autowired
    private AnimeService animeService;

    static final String LIST_NAME_MISSING = "provided listName does not exist";

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

    private boolean validateNewCustomerList(String newListName, List<String> entries) {
        try {
            if (newListName == null || newListName.isBlank() || entries.isEmpty()) {
                log.warn("validation failed for newListName {}", newListName);
                return false;
            }
            if (entries.stream().anyMatch(String::isEmpty)) {
                log.warn("validation failed for entries {} as any one of them is empty", entries);
                return false;
            }
        } catch (Exception e) {
            log.error("Exception occurred in validateNewCustomerList", e);
            return false;
        }
        return true;
    }

    @Override
    public void createNewCustomUserList(String userName, String newListName, List<String> entries) throws BadRequestException {
        User user = getUserByUserName(userName);
        HashMap<String, List<String>> userCustomLists = user.getCustomUserLists();
        if (userCustomLists == null) userCustomLists = new HashMap<>();
        if (!validateNewCustomerList(newListName, entries)) {
            throw new BadRequestException("validation failed for newList");
        }
        if (userCustomLists.containsKey(newListName))
            throw new BadRequestException("list already exists");
        userCustomLists.put(newListName, entries);
        user.setCustomUserLists(userCustomLists);
        userRepository.save(user);
    }

    @Override
    public void addToExistingCustomUserList(String userName, String listName, String newEntry) throws BadRequestException {

        User user = getUserByUserName(userName);
        HashMap<String, List<String>> userCustomLists = user.getCustomUserLists();
        if (userCustomLists == null || listName == null ||
                listName.isBlank() || newEntry == null || newEntry.isBlank())
            throw new BadRequestException("validation failed for listName or newEntry");

        if (!userCustomLists.containsKey(listName))
            throw new BadRequestException(LIST_NAME_MISSING);

        userCustomLists.get(listName).add(newEntry);
        userRepository.save(user);
    }

    @Override
    public void removeFromExistingCustomUserList(String userName, String listName, String entryToRemove) throws BadRequestException {
        User user = getUserByUserName(userName);
        HashMap<String, List<String>> userCustomLists = user.getCustomUserLists();
        if (userCustomLists == null || listName == null ||
                listName.isBlank() || entryToRemove == null || entryToRemove.isBlank())
            throw new BadRequestException("validation failed");

        if (!userCustomLists.containsKey(listName))
            throw new BadRequestException(LIST_NAME_MISSING);

        List<String> existingList = userCustomLists.get(listName);
        existingList.remove(entryToRemove);
        userCustomLists.put(listName, existingList);
        userRepository.save(user);
    }

    @Override
    public List<String> getExistinCustomUserList(String userName, String listName) throws BadRequestException {
        User user = getUserByUserName(userName);
        HashMap<String, List<String>> userCustomLists = user.getCustomUserLists();
        if (listName == null || listName.isBlank())
            throw new BadRequestException("validation failed for listName");

        if (!userCustomLists.containsKey(listName))
            throw new BadRequestException(LIST_NAME_MISSING);

        return userCustomLists.get(listName);
    }

    @Override
    public void deleteExistingCustomUserList(String userName, String listName) throws BadRequestException {
        User user = getUserByUserName(userName);
        HashMap<String, List<String>> userCustomLists = user.getCustomUserLists();
        if (listName == null || listName.isBlank())
            throw new BadRequestException("validation failed for listName");

        if (!userCustomLists.containsKey(listName))
            throw new BadRequestException(LIST_NAME_MISSING);

        userCustomLists.remove(listName);
        userRepository.save(user);
    }

}
