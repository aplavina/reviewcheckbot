package com.aplavina.reviewcheckbot.service.review;

import com.aplavina.reviewcheckbot.client.CheckReviewServiceClient;
import com.aplavina.reviewcheckbot.dto.review.CheckReviewDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckReviewServiceTest {
    @Mock
    private CheckReviewServiceClient checkReviewServiceClient;
    @InjectMocks
    private CheckReviewService checkReviewService;

    @Test
    void testCheckReview_returnsExpectedFormattedString() {
        String reviewText = "This product is amazing!";
        CheckReviewDto inputDto = new CheckReviewDto();
        inputDto.setText(reviewText);

        CheckReviewDto responseDto = new CheckReviewDto();
        responseDto.setText(reviewText);
        responseDto.setIsFake(false);
        responseDto.setFakeScorePercentage(12.5f);

        when(checkReviewServiceClient.checkReview(any(CheckReviewDto.class))).thenReturn(responseDto);

        String result = checkReviewService.checkReview(reviewText);

        String expectedOutput = "Review: This product is amazing!\n" +
                "Fake: No\n" +
                "Fake Score Percentage: 12.5%\n";

        assertEquals(expectedOutput, result);
        verify(checkReviewServiceClient, times(1)).checkReview(any(CheckReviewDto.class));
    }
}