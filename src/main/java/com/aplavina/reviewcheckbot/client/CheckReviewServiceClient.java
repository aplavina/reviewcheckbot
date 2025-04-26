package com.aplavina.reviewcheckbot.client;

import com.aplavina.reviewcheckbot.dto.review.CheckReviewDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "review-check-service", url = "${review-check-service.host}:${review-check-service.port}")
public interface CheckReviewServiceClient {
    @PostMapping("/receive-text")
    CheckReviewDto checkReview(@RequestBody CheckReviewDto checkReviewRequest);
}