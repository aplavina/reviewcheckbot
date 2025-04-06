package com.aplavina.reviewcheckbot.event.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileReviewEvent {
    private String id;
    private String fileKey;
    private String text;
}
