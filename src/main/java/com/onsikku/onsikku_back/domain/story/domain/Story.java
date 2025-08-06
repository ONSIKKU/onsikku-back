package com.onsikku.onsikku_back.domain.story.domain;



import com.onsikku.onsikku_back.domain.story.dto.StoryRequestDto;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "story")
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Long userId;
    private String title;
    private String content;
    private boolean majorEvent;

    private List<String> images;

    private Integer year;            // 4자리 연도
    private Integer month;           // 1~12 (null 가능)
    private Integer day;             // 1~31 (null 가능)


    @Builder
    private Story(String title, String content, boolean majorEvent, List<String> images, Integer year, Integer month, Integer day) {
        this.title = title;
        this.content = content;
        this.majorEvent = majorEvent;
        this.images = images;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static Story of(StoryRequestDto dto) {
        return Story.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .images(dto.getImages())
                .majorEvent(dto.isMajorEvent())
                .year(dto.getYear())
                .month(dto.getMonth())
                .day(dto.getDay())
                .build();
    }

    public void updateStory(StoryRequestDto dto) {
        title = dto.getTitle();
        content = dto.getContent();
        images = dto.getImages();
        majorEvent = dto.isMajorEvent();
        year = dto.getYear();
        month = dto.getMonth();
        day = dto.getDay();
    }

}
