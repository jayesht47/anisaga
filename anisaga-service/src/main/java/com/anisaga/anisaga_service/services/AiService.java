package com.anisaga.anisaga_service.services;

import com.anisaga.anisaga_service.entities.Anime;
import com.anisaga.anisaga_service.exceptions.BadRequestException;

import java.util.List;

public interface AiService {

    public List<Anime> getAnimeRecommendations(List<String> likes) throws BadRequestException;

}
