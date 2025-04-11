package com.aplavina.reviewcheckbot.consumer.file;

import com.aplavina.reviewcheckbot.event.file.FileReceivedEvent;
import com.aplavina.reviewcheckbot.event.file.FileReviewEvent;
import com.aplavina.reviewcheckbot.producer.file.FileReviewProducer;
import com.aplavina.reviewcheckbot.repository.FileCheckRepository;
import com.aplavina.reviewcheckbot.service.s3.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileReceivedConsumerTest {
    private ObjectMapper objectMapper;
    private S3Service s3Service;
    private FileCheckRepository fileCheckRepository;
    private FileReviewProducer fileReviewProducer;
    private FileReceivedConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        s3Service = mock(S3Service.class);
        fileCheckRepository = mock(FileCheckRepository.class);
        fileReviewProducer = mock(FileReviewProducer.class);

        consumer = new FileReceivedConsumer(objectMapper, s3Service, fileCheckRepository, fileReviewProducer);
    }

    @Test
    void testConsume_successful() throws Exception {
        String fileKey = "file.csv";
        String chatId = "12345";
        String content = "review1\nreview2\nreview3";

        FileReceivedEvent event = new FileReceivedEvent();
        event.setFileKey(fileKey);
        event.setChatId(chatId);

        Acknowledgment ack = mock(Acknowledgment.class);

        when(objectMapper.readValue(anyString(), eq(FileReceivedEvent.class))).thenReturn(event);
        InputStream fakeInputStream = new ByteArrayInputStream(content.getBytes());
        when(s3Service.getFileInputStream(fileKey)).thenReturn(fakeInputStream);

        consumer.consume("fake-json", ack);

        verify(fileCheckRepository).save(argThat(fileCheck ->
                fileCheck.getFileKey().equals(fileKey) && fileCheck.getChatId().equals(chatId)));

        verify(fileReviewProducer, times(3)).publish(any(FileReviewEvent.class));
        verify(fileCheckRepository).setCount(fileKey, 3L);
        verify(ack).acknowledge();
    }

    @Test
    void testConsume_invalidJson_throwsException() throws Exception {
        when(objectMapper.readValue(anyString(), eq(FileReceivedEvent.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {
                });
        Acknowledgment ack = mock(Acknowledgment.class);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            consumer.consume("invalid-json", ack);
        });

        verifyNoInteractions(s3Service, fileReviewProducer, fileCheckRepository);
        verify(ack, never()).acknowledge();
    }
}