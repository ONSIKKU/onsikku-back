package com.onsikku.onsikku_back.domain.story.service;


import com.onsikku.onsikku_back.domain.story.dto.StoryRequestDto;
import com.onsikku.onsikku_back.domain.story.domain.Story;
import com.onsikku.onsikku_back.domain.story.repository.StoryRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryService {
    private StoryRepository storyRepository;


    public List<Story> findAllStoriesByUserId(Long userId) {
        return storyRepository.findAllByUserId(userId);

    }

    public Story createStory(StoryRequestDto dto) {
        return storyRepository.save(Story.of(dto));

    }

    public Story updateStory(StoryRequestDto dto) {
        Story story = storyRepository.findById(dto.getStoryId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_STORY));
        story.updateStory(dto);
        return storyRepository.save(story);

    }
    public void deleteStory(StoryRequestDto dto) {
        storyRepository.deleteById(dto.getStoryId());
    }




}
