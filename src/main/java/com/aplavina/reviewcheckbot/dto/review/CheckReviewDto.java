package com.aplavina.reviewcheckbot.dto.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckReviewDto {
    private String text;

    @JsonProperty("is_fake")
    private Boolean isFake;

    @JsonProperty("fake_score_percentage")
    private Float fakeScorePercentage;
}
