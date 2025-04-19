package com.anisaga.anisaga_service.services.impl;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.exceptions.BadRequestException;
import com.anisaga.anisaga_service.services.AnimeService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AnimeServiceImpl implements AnimeService {

    private RestTemplate restTemplate;

    @Autowired
    public AnimeServiceImpl(RestTemplateBuilder builder) {
        this.restTemplate = builder.
                setConnectTimeout(Duration.ofMinutes(3)).
                defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.api+json").
                additionalInterceptors(((request, body, execution) -> {
                    // Manually removing and setting 'Accept' Header as defaultHeader appends to default of application/json instead of replacing it
                    request.getHeaders().remove(HttpHeaders.ACCEPT);
                    request.getHeaders().add(HttpHeaders.ACCEPT, "application/vnd.api+json");
                    return execution.execute(request, body);
                })).
                build();
    }

    @Override
    public Anime getAnimeById(String animeId) {
        ResponseEntity<String> response = restTemplate.getForEntity("https://kitsu.io/api/edge/anime/" + animeId, String.class);
        JsonObject respObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonObject dataAttr = respObject.get("data").getAsJsonObject();
        return getAnimeFromJsonObject(dataAttr, true);
    }

    @Override
    public Anime getAnimeBySlug(String slug) {
        ResponseEntity<String> response = restTemplate.getForEntity("https://kitsu.io/api/edge/anime?filter[slug]=" + slug, String.class);
        JsonObject respObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonObject dataAttr = respObject.get("data").getAsJsonArray().get(0).getAsJsonObject();
        return getAnimeFromJsonObject(dataAttr, true);
    }

    @Override
    public List<Anime> getTrendingAnime() {
        List<Anime> trendingAnime = new ArrayList<>();
        ResponseEntity<String> response = restTemplate.getForEntity("https://kitsu.io/api/edge/trending/anime", String.class);
        JsonObject respObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray responseArray = respObject.getAsJsonArray("data");
        for (JsonElement responseObject : responseArray) {
            trendingAnime.add(getAnimeFromJsonObject(responseObject.getAsJsonObject(), false));
        }
        return trendingAnime;
    }

    @Override
    public List<Anime> getAnimeListBySlugs(List<String> slugs) {
        List<Anime> animeList = new ArrayList<>();
        StringBuilder sb = new StringBuilder("https://kitsu.io/api/edge/anime?filter[slug]=");
        slugs.forEach(e -> sb.append(e + ","));
        sb.deleteCharAt(sb.length() - 1);
        log.info("Generated url for getAnimeListBySlugs : {}", sb);
        ResponseEntity<String> response = restTemplate.getForEntity(sb.toString(), String.class);
        JsonObject respObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray responseArray = respObject.getAsJsonArray("data");
        for (JsonElement responseObject : responseArray) {
            animeList.add(getAnimeFromJsonObject(responseObject.getAsJsonObject(), false));
        }
        return animeList;
    }

    /**
     * A small utility method to get {@link Anime} Object from a {@link JsonObject}
     * pass the JsonObject "attributes" from Kitsu APIs response
     *
     * @param responseObject
     * @return
     */
    private Anime getAnimeFromJsonObject(JsonObject responseObject, Boolean genresRequired) {
        JsonObject animeObject = responseObject.get("attributes").getAsJsonObject();
        Anime anime = new Anime();
        anime.setSlug(animeObject.get("slug").getAsString());
        if (animeObject.get("canonicalTitle") != null) anime.setName(animeObject.get("canonicalTitle").getAsString());
        else {
            log.warn("Not able to get canonicalTitle using slug for {}", anime.getSlug());
            anime.setName(anime.getSlug().replace("-", " "));
        }
        anime.setId(responseObject.get("id").getAsString());
        anime.setSynopsis(animeObject.get("synopsis").getAsString());
        anime.setStartDate(animeObject.get("startDate").isJsonNull() ? null : animeObject.get("startDate").getAsString());
        anime.setEndDate(animeObject.get("endDate").isJsonNull() ? null : animeObject.get("endDate").getAsString());
        anime.setEpisodeCount(animeObject.get("episodeCount").isJsonNull() ? null : Integer.parseInt(animeObject.get("episodeCount").getAsString()));
        anime.setAverageRating(animeObject.get("averageRating").isJsonNull() ? null : animeObject.get("averageRating").getAsString());
        anime.setYoutubeVideoId(animeObject.get("youtubeVideoId").isJsonNull() ? null : animeObject.get("youtubeVideoId").getAsString());
        JsonObject images = new JsonObject();
        images.addProperty("largePosterImage", animeObject.has("posterImage") ? animeObject.get("posterImage").getAsJsonObject().get("large").getAsString() : "");
        images.addProperty("originalPosterImage", animeObject.has("posterImage") ? animeObject.get("posterImage").getAsJsonObject().get("original").getAsString() : "");
        anime.setImages(images);
        if (Boolean.TRUE.equals(genresRequired)) anime.setGenres(getGenresFromAnimeId(anime.getId()));
        return anime;
    }


    private List<String> getGenresFromAnimeId(String animeId) {
        List<String> genres = new ArrayList<>();
        String categoriesUrl = "https://kitsu.io/api/edge/anime/" + animeId + "/genres";
        ResponseEntity<String> response = restTemplate.getForEntity(categoriesUrl, String.class);
        JsonObject respObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray genresArray = respObject.get("data").getAsJsonArray();
        genresArray.forEach(element -> {
            JsonObject category = element.getAsJsonObject().get("attributes").getAsJsonObject();
            genres.add(category.has("name") ? category.get("name").getAsString() : "");
        });
        return genres;
    }

    @Override
    public List<Anime> searchAnimeByName(String searchInput) throws BadRequestException {
        if (searchInput == null) throw new BadRequestException("SearchInput not provided");
        if (searchInput.isBlank()) throw new BadRequestException("SearchInput cannot be blank");
        if (searchInput.strip().length() < 3)
            throw new BadRequestException("SearchInput must be atleast 3 characters long");
        List<Anime> animeList = new ArrayList<>();
        String searchUrl = "https://kitsu.io/api/edge/anime?filter[text]=" + searchInput;
        ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);
        JsonObject respObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray animeArray = respObject.get("data").getAsJsonArray();
        for (JsonElement animeObject : animeArray) {
            animeList.add(getAnimeFromJsonObject(animeObject.getAsJsonObject(), false));
        }
        return animeList;
    }
}
