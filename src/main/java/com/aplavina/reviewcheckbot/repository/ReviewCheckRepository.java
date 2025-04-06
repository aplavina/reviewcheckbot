package com.aplavina.reviewcheckbot.repository;

import com.aplavina.reviewcheckbot.model.ReviewCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCheckRepository extends JpaRepository<ReviewCheck, Long> {
}