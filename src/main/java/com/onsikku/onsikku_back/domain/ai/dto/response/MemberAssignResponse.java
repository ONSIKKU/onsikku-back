package com.onsikku.onsikku_back.domain.ai.dto.response;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class MemberAssignResponse {
  private List<UUID> memberIds;
  private String version;
}
