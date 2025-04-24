package com.anisaga.anisaga_service.vo;

import lombok.Data;

import java.util.List;

@Data
public class NewCustomList {

    private String listName;

    private List<String> entries;

}
