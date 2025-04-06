package com.aplavina.reviewcheckbot.consumer.file;

import com.aplavina.reviewcheckbot.event.file.FileReceivedEvent;
import com.aplavina.reviewcheckbot.event.file.FileReviewEvent;
import com.aplavina.reviewcheckbot.model.FileCheck;
import com.aplavina.reviewcheckbot.producer.file.FileReviewProducer;
import com.aplavina.reviewcheckbot.repository.FileCheckRepository;
import com.aplavina.reviewcheckbot.service.s3.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileReceivedConsumer {
    private final ObjectMapper objectMapper;
    private final S3Service s3Service;
    private final FileCheckRepository fileCheckRepository;
    private final FileReviewProducer fileReviewProducer;

    @KafkaListener(topics = "${kafka.topics.received-file.name}",
            groupId = "${kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message, Acknowledgment ack) {
        try {
            FileReceivedEvent fileReceivedEvent = objectMapper.readValue(message, FileReceivedEvent.class);
            InputStream fileInputStream = s3Service.getFileInputStream(fileReceivedEvent.getFileKey());
            String fileContent = new String(fileInputStream.readAllBytes());
            fileCheckRepository.save(
                    FileCheck.builder()
                            .fileKey(fileReceivedEvent.getFileKey())
                            .chatId(fileReceivedEvent.getChatId())
                            .build()
            );
            BufferedReader reader = new BufferedReader(new StringReader(fileContent));
            long countReviews = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                FileReviewEvent fileReviewEvent = new FileReviewEvent(
                        UUID.randomUUID().toString(),
                        fileReceivedEvent.getFileKey(),
                        line
                );
                fileReviewProducer.publish(fileReviewEvent);
                countReviews++;
            }
            fileCheckRepository.setCount(fileReceivedEvent.getFileKey(), countReviews);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Could not deserialize FileReceivedEvent from json", e);
            throw new IllegalStateException("Could not deserialize FileReceivedEvent from json");
        } catch (IOException e) {
            log.error("Could not download file content from S3", e);
            throw new IllegalStateException("Could not download file content from S3");
        }
    }
}
