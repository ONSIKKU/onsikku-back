package com.onsikku.onsikku_back.domain.story.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StoryRequestDto {
    private String storyId;
    private Long userId;
    private String title;
    private String content;
    private boolean majorEvent;

    private List<String> images;

    private Integer year;            // 4자리 연도
    private Integer month;           // 1~12 (null 가능)
    private Integer day;             // 1~31 (null 가능)

}
