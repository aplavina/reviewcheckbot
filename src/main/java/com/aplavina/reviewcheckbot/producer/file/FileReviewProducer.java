package com.aplavina.reviewcheckbot.producer.file;

import com.aplavina.reviewcheckbot.event.file.FileReviewEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileReviewProducer {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.file-review.name}")
    private String fileReviewTopicName;

    public void publish(FileReviewEvent event) {
        try {
            kafkaTemplate.send(fileReviewTopicName, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Exception when converting FileReviewEvent to json", e);
            throw new IllegalStateException("Exception when converting FileReviewEvent to json");
        }
    }
}
