package com.anisaga.anisaga_service.controllers;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.services.AnimeService;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/anime")
public class AnimeController {

    @Autowired
    AnimeService animeService;

    @GetMapping("/id/{animeId}")
    public Anime getAnimeById(@PathVariable("animeId") String animeId) {
        return animeService.getAnimeById(animeId);
    }

    @GetMapping("/slug/{animeSlug}")
    public Anime getAnimeBySlug(@PathVariable("animeSlug") String animeSlug) {
        return animeService.getAnimeBySlug(animeSlug);
    }

    @GetMapping("/trending")
    public List<Anime> getTrendingAnime() {
        return animeService.getTrendingAnime();
    }
}
