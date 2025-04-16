package com.anisaga.anisaga_service.controllers;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.exceptions.BadRequestException;
import com.anisaga.anisaga_service.services.AiService;
import com.anisaga.anisaga_service.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AIController {

    @Autowired
    private AiService aiService;

    @Autowired
    private UserService userService;


    @GetMapping("/{userName}/recommendation")
    public List<Anime> getRecommendationByUserName(@PathVariable("userName") String userName) throws BadRequestException {

        List<String> likes = userService.getLikes(userName);
        log.info("likes for user {} are {}",userName,likes);
        List<Anime> suggestions = aiService.getAnimeRecommendations(likes);
        log.info("suggestions for user {} are {}",userName,suggestions);
        return suggestions;
    }

}
