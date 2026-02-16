package com.onsikku.onsikku_back.domain.answer.repository;

import com.onsikku.onsikku_back.domain.answer.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
}