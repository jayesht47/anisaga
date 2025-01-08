package com.anisaga.anisaga_service.services;

import com.anisaga.anisaga_service.entities.Anime;
import java.util.List;

public interface AnimeService {

    Anime getAnimeById(String animeId);

    Anime getAnimeBySlug(String slug);

    List<Anime> getTrendingAnime();

    List<Anime> getAnimeListBySlugs(List<String> slugs);
}
