package com.aplavina.reviewcheckbot.consumer.file;

import com.aplavina.reviewcheckbot.client.CheckReviewServiceClient;
import com.aplavina.reviewcheckbot.dto.review.CheckReviewDto;
import com.aplavina.reviewcheckbot.event.file.FileReviewEvent;
import com.aplavina.reviewcheckbot.model.ReviewCheck;
import com.aplavina.reviewcheckbot.repository.FileCheckRepository;
import com.aplavina.reviewcheckbot.repository.ReviewCheckRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileReviewConsumer {
    private final ObjectMapper objectMapper;
    private final CheckReviewServiceClient checkReviewServiceClient;
    private final ReviewCheckRepository reviewCheckRepository;
    private final FileCheckRepository fileCheckRepository;

    @KafkaListener(topics = "${kafka.topics.file-review.name}",
            groupId = "${kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message, Acknowledgment ack) {
        try {
            FileReviewEvent fileReviewEvent = objectMapper.readValue(message, FileReviewEvent.class);
            CheckReviewDto checkResult = checkReviewServiceClient.checkReview(
                    CheckReviewDto.builder()
                            .text(fileReviewEvent.getText())
                            .build()
            );
            reviewCheckRepository.save(ReviewCheck.builder()
                    .id(fileReviewEvent.getId())
                    .text(fileReviewEvent.getText())
                    .fakeScorePercentage(checkResult.getFakeScorePercentage())
                    .isFake(checkResult.getIsFake())
                    .file(fileCheckRepository
                            .findById(fileReviewEvent.getFileKey())
                            .orElseThrow(() -> new IllegalStateException("File not found"))
                    )
                    .build());
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Could not deserialize FileReceivedEvent from json", e);
            throw new IllegalStateException("Could not deserialize FileReceivedEvent from json");
        }
    }
}
