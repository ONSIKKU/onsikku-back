package com.onsikku.onsikku_back.domain.story.controller;



import com.onsikku.onsikku_back.domain.story.dto.StoryRequestDto;
import com.onsikku.onsikku_back.domain.story.domain.Story;
import com.onsikku.onsikku_back.domain.story.service.StoryService;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @GetMapping("/")
    public BaseResponse<List<Story>> getStories(@AuthenticationPrincipal UserDetails userDetails) {
        // todo: jwt 토큰 userId 추가
        return new BaseResponse<>(storyService.findAllStoriesByUserId(1L));
    }


    @PostMapping("/")
    public BaseResponse<Story> createStory(@RequestPart(value = "file", required = false) List<MultipartFile> multipartFile,
                                           @RequestBody StoryRequestDto dto,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        //dto.setImages(imageService.saveMultipartList(););
        return new BaseResponse<>(storyService.createStory(dto));
    }


    @PutMapping("/")
    public BaseResponse<Story> updateStory(@RequestPart(value = "file", required = false) List<MultipartFile> multipartFile,
                                           @RequestBody StoryRequestDto dto,
                                           @RequestParam String storyId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        dto.setStoryId(storyId);
        return new BaseResponse<>(storyService.updateStory(dto));
    }

    @DeleteMapping("/")
    public BaseResponse<Story> deleteStory(@RequestBody StoryRequestDto dto,
                                           @RequestParam String storyId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        dto.setStoryId(storyId);
        storyService.deleteStory(dto);
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }
}
