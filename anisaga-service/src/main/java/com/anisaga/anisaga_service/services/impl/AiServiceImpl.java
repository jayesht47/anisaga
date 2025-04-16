package com.anisaga.anisaga_service.services.impl;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.exceptions.BadRequestException;
import com.anisaga.anisaga_service.services.AiService;
import com.anisaga.anisaga_service.services.AnimeService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private RestTemplate restTemplate;
    @Value("${genai.url}")
    private String genAIURL;

    @Autowired
    private AnimeService animeService;

    @Autowired
    public AiServiceImpl(RestTemplateBuilder builder) {
        this.restTemplate = builder.
                setConnectTimeout(Duration.ofMinutes(3)).
                defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json").
                build();
    }

    @Override
    public List<Anime> getAnimeRecommendations(List<String> likes) throws BadRequestException {
        List<Anime> result = new ArrayList<>();
        String finalURL = genAIURL + "/recommendation";
        JsonObject requestBody = new JsonObject();
        JsonArray likesArr = new JsonArray();
        likes.forEach(likesArr::add);
        requestBody.add("likes", likesArr);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody.toString());
        ResponseEntity<String> response = restTemplate.exchange(finalURL, HttpMethod.POST, httpEntity, String.class);
        JsonArray responseArray = JsonParser.parseString(response.getBody()).getAsJsonArray();
        log.info("responseArray is {}", responseArray);
        List<String> recommendationAnimeNames = new ArrayList<>();
        responseArray.forEach(e -> {
            JsonObject anime = e.getAsJsonObject();
            if (anime != null && anime.get("anime") != null)
                recommendationAnimeNames.add(anime.get("anime").getAsString());
        });
        for (String animeName : recommendationAnimeNames) {
            result.add(animeService.searchAnimeByName(animeName).get(0));
        }
        return result;
    }
}
