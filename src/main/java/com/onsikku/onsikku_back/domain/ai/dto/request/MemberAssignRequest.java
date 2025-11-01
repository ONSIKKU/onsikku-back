package com.onsikku.onsikku_back.domain.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAssignRequest {
  private UUID familyId;
  private List<MemberInfo> members;
  private int pickCount;
  //private Map<String, Object> options;
}