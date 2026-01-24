package com.onsikku.onsikku_back.domain.ai.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "family_report")
public class FamilyReport {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "family_id", nullable = false)
  private Family family;

  @Enumerated(EnumType.STRING)
  @Column(name = "report_type", nullable = false)
  private ReportType reportType;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "summary", columnDefinition = "text", nullable = false)
  private String summary;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "data", columnDefinition = "jsonb", nullable = false)
  private JsonNode data;    // 하이라이트 데이터
}
