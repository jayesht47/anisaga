package com.anisaga.anisaga_service.controllers;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.entities.User;
import com.anisaga.anisaga_service.exceptions.BadRequestException;
import com.anisaga.anisaga_service.services.AnimeService;
import com.anisaga.anisaga_service.services.UserService;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AnimeService animeService;

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

    @PostMapping("/user/{userName}/like/anime/add")
    public void likeAnime(@PathVariable("userName") String userName, @RequestParam("slug") String animeSlug) throws BadRequestException {
        if (animeSlug == null || animeSlug.isBlank()) throw new BadRequestException("animeSlug is missing");
        userService.addToLikes(userName, animeSlug);
    }

    @PostMapping("/user/{userName}/like/anime/remove")
    public void removeFromLikeAnime(@PathVariable("userName") String userName, @RequestParam("slug") String animeSlug) throws BadRequestException {
        if (animeSlug == null || animeSlug.isBlank()) throw new BadRequestException("animeSlug is missing");
        userService.removeFromLikes(userName, animeSlug);
    }

    @GetMapping("/user/{userName}/like/anime/contains")
    public JsonObject checkIfIsLiked(@PathVariable("userName") String userName, @RequestParam("slug") String animeSlug) throws BadRequestException {
        JsonObject response = new JsonObject();
        if (animeSlug == null || animeSlug.isBlank()) throw new BadRequestException("animeSlug is missing");
        User user = getUserByUserName(userName);
        response.addProperty("error", false);
        response.addProperty("isLiked", user.getLikedAnime().contains(animeSlug));
        return response;
    }

    @GetMapping("/user/{userName}/like/anime/list")
    public List<Anime> getUserLikes(@PathVariable("userName")String userName){
        List<String> likedAnimeSlugs = userService.getLikes(userName);
        return animeService.getAnimeListBySlugs(likedAnimeSlugs);
    }
}
