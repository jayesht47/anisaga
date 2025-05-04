package com.anisaga.anisaga_service.services;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.entities.User;
import com.anisaga.anisaga_service.exceptions.BadRequestException;
import com.anisaga.anisaga_service.exceptions.DuplicateUserNameException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {

    User upsertUser(User user) throws DuplicateUserNameException;

    Optional<User> getUserByUserId(String userId);

    void deleteUser(User user);

    List<User> getAllUsers();

    User getUserByUserName(String userName);

    void addToLikes(String userName, String animeSlug);

    void removeFromLikes(String userName, String animeSlug);

    List<String> getLikes(String userName);

    List<Anime> getRecommendations(String userName);

    void updateRecommendations(String userName) throws BadRequestException;

    void createNewCustomUserList(String userName, String newListName, List<String> entries) throws BadRequestException;

    void addToExistingCustomUserList(String userName, String listName, String newEntry) throws BadRequestException;

    void removeFromExistingCustomUserList(String userName, String listName, String entryToRemove) throws BadRequestException;

    List<Anime> getExistinCustomUserList(String userName, String listName) throws BadRequestException;

    List<String> getCustomListNames(String userName);

    void deleteExistingCustomUserList(String userName, String listName) throws BadRequestException;
}
