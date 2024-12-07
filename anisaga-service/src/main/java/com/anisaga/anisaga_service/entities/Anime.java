package com.anisaga.anisaga_service.entities;

import lombok.Data;

@Data
public class Anime {

    private String name;

    private String slug;

    private String synopsis;

    private String averageRating;

    private String startDate;

    private String endDate;

    private Integer episodeCount;

    private String youtubeVideoId;


}
