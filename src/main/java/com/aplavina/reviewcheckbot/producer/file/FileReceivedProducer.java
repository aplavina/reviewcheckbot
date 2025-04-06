package com.aplavina.reviewcheckbot.producer.file;

import com.aplavina.reviewcheckbot.event.file.FileReceivedEvent;
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
public class FileReceivedProducer {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.received-file.name}")
    private String receivedFileTopicName;

    public void publish(FileReceivedEvent event) {
        try {
            kafkaTemplate.send(receivedFileTopicName, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Exception when converting FileReceivedEvent to json", e);
            throw new IllegalStateException("Exception when converting FileReceivedEvent to json");
        }
    }
}
