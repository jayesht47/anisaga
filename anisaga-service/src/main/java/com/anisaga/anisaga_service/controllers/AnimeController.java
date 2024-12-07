package com.anisaga.anisaga_service.controllers;

import com.anisaga.anisaga_service.entities.Anime;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
        Anime anime = new Anime();
        anime.setName(dataAttr.get("titles").getAsJsonObject().get("en").getAsString());
        anime.setSlug(dataAttr.get("slug").getAsString());
        anime.setSynopsis(dataAttr.get("synopsis").getAsString());
        anime.setStartDate(dataAttr.get("startDate").isJsonNull() ? null : dataAttr.get("startDate").getAsString());
        anime.setEndDate(dataAttr.get("endDate").isJsonNull() ? null : dataAttr.get("endDate").getAsString());
        anime.setEpisodeCount(dataAttr.get("episodeCount").isJsonNull() ? null : Integer.parseInt(dataAttr.get("episodeCount").getAsString()));
        anime.setAverageRating(dataAttr.get("averageRating").isJsonNull() ? null : dataAttr.get("averageRating").getAsString());
        anime.setYoutubeVideoId(dataAttr.get("youtubeVideoId").isJsonNull() ? null : dataAttr.get("youtubeVideoId").getAsString());
        return anime;
    }


}
