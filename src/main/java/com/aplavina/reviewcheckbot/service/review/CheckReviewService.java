package com.aplavina.reviewcheckbot.service.review;

import com.aplavina.reviewcheckbot.client.CheckReviewServiceClient;
import com.aplavina.reviewcheckbot.dto.review.CheckReviewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckReviewService {
    private final CheckReviewServiceClient checkReviewServiceClient;

    public String checkReview(String review) {
        CheckReviewDto checkReviewDto = new CheckReviewDto();
        checkReviewDto.setText(review);
        CheckReviewDto checkedReview = checkReviewServiceClient.checkReview(checkReviewDto);
        return "Review: " + checkedReview.getText() + "\n" +
                "Fake: " + (checkedReview.getIsFake() ? "Yes" : "No") + "\n" +
                "Fake Score Percentage: " + checkedReview.getFakeScorePercentage() + "%\n";
    }
}
