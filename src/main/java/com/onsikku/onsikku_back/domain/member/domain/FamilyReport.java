package com.onsikku.onsikku_back.domain.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "family_report")
public class FamilyReport {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private Family family;

  @Column(nullable = false)
  private String context;

  @Enumerated(EnumType.STRING)
  @Column(name = "report_type", nullable = false)
  private ReportType reportType;
}
