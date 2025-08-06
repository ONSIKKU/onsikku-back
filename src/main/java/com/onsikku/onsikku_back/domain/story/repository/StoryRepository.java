package com.onsikku.onsikku_back.domain.story.repository;

import com.onsikku.onsikku_back.domain.story.domain.Story;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StoryRepository extends MongoRepository<Story, String> {
    List<com.onsikku.onsikku_back.domain.story.domain.Story> findAllByUserId(Long userId);

}
