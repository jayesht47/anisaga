package com.anisaga.anisaga_service.services;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.exceptions.BadRequestException;

import java.util.List;

public interface AnimeService {

    Anime getAnimeById(String animeId);

    Anime getAnimeBySlug(String slug);

    List<Anime> getTrendingAnime();

    List<Anime> getAnimeListBySlugs(List<String> slugs);

    List<Anime> searchAnimeByName(String searchInput) throws BadRequestException;
}
