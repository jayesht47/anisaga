package com.anisaga.anisaga_service.controllers;

import com.anisaga.anisaga_service.entities.Anime;
import com.google.gson.*;
import io.micrometer.common.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@RestController
@RequestMapping("/anime")
@Slf4j
public class AnimeController {

    private RestTemplate restTemplate;

    @Autowired
    public AnimeController(RestTemplateBuilder builder) {
        this.restTemplate = builder.
                setConnectTimeout(Duration.ofMinutes(3)).
                defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.api+json").
                additionalInterceptors(((request, body, execution) -> {
                    // Manually removing and setting 'Accept' Header as defaultHeader appends to default of application/json instead of replacing it
                    request.getHeaders().remove(HttpHeaders.ACCEPT);
                    request.getHeaders().add(HttpHeaders.ACCEPT, "application/vnd.api+json");
                    logger.info("Headers are :: {}", request.getHeaders());
                    return execution.execute(request, body);
                })).
                build();
    }

    private static final Logger logger = LoggerFactory.getLogger(AnimeController.class);

    @GetMapping("/{animeId}")
    public Anime getAnimeById(@PathVariable("animeId") String animeId) {
        ResponseEntity<String> response = restTemplate.getForEntity("https://kitsu.io/api/edge/anime/" + animeId, String.class);
        JsonObject respObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonObject dataAttr = respObject.get("data").getAsJsonObject().get("attributes").getAsJsonObject();
        return getAnimeFromJsonObject(dataAttr);
    }

    @GetMapping("/trending")
    public List<Anime> getTrendingAnime(){
        List<Anime> trendingAnime = new ArrayList<>();
        ResponseEntity<String> response = restTemplate.getForEntity("https://kitsu.io/api/edge/trending/anime", String.class);
        JsonObject respObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray animeArray = respObject.getAsJsonArray("data");
        for(JsonElement anime : animeArray){
            JsonObject animeObject = anime.getAsJsonObject().get("attributes").getAsJsonObject();
            trendingAnime.add(getAnimeFromJsonObject(animeObject));
        }
        return trendingAnime;
    }

    /**
     * A small utility method to get {@link Anime} Object from a {@link JsonObject}
     * pass the JsonObject "attributes" from Kitsu APIs response
     * @param animeObject
     * @return
     */
    private Anime getAnimeFromJsonObject(JsonObject animeObject){
        Anime anime = new Anime();
        anime.setSlug(animeObject.get("slug").getAsString());
        if (animeObject.get("canonicalTitle") != null) anime.setName(animeObject.get("canonicalTitle").getAsString());
        else {
            log.warn("Not able to get canonicalTitle using slug for {}",anime.getSlug());
            anime.setName(anime.getSlug().replace("-", " "));
        }
        anime.setSynopsis(animeObject.get("synopsis").getAsString());
        anime.setStartDate(animeObject.get("startDate").isJsonNull() ? null : animeObject.get("startDate").getAsString());
        anime.setEndDate(animeObject.get("endDate").isJsonNull() ? null : animeObject.get("endDate").getAsString());
        anime.setEpisodeCount(animeObject.get("episodeCount").isJsonNull() ? null : Integer.parseInt(animeObject.get("episodeCount").getAsString()));
        anime.setAverageRating(animeObject.get("averageRating").isJsonNull() ? null : animeObject.get("averageRating").getAsString());
        anime.setYoutubeVideoId(animeObject.get("youtubeVideoId").isJsonNull() ? null : animeObject.get("youtubeVideoId").getAsString());
        anime.setPosterImage(animeObject.has("posterImage")?animeObject.get("posterImage").getAsJsonObject().get("large").getAsString(): "");
        return anime;
    }


}
