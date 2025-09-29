package com.onsikku.onsikku_back.domain.ai.dto;

import java.util.List;
import java.util.UUID;

public class MemberAssignResponse {
  private List<UUID> memberIds;   // Swagger엔 number/string 예시가 섞여있지만 실제는 UUID라고 가정
  private String version;
}
