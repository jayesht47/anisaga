package com.anisaga.anisaga_service.entities;

import com.google.gson.JsonObject;
import lombok.Data;

import java.util.List;

@Data
public class Anime {

    private String id;

    private String name;

    private String slug;

    private String synopsis;

    private String averageRating;

    private String startDate;

    private String endDate;

    private Integer episodeCount;

    private String youtubeVideoId;

    private String posterImage;

    private JsonObject images;

    private List<String> genres;
}
