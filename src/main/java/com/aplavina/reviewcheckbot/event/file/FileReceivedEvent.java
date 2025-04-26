package com.aplavina.reviewcheckbot.event.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileReceivedEvent {
    private String fileKey;
    private String chatId;
}

