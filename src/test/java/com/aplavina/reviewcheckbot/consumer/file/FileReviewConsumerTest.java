package com.aplavina.reviewcheckbot.consumer.file;

import com.aplavina.reviewcheckbot.client.CheckReviewServiceClient;
import com.aplavina.reviewcheckbot.dto.review.CheckReviewDto;
import com.aplavina.reviewcheckbot.event.file.FileReviewEvent;
import com.aplavina.reviewcheckbot.model.FileCheck;
import com.aplavina.reviewcheckbot.repository.FileCheckRepository;
import com.aplavina.reviewcheckbot.repository.ReviewCheckRepository;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileReviewConsumerTest {
    private ObjectMapper objectMapper;
    private CheckReviewServiceClient checkReviewServiceClient;
    private ReviewCheckRepository reviewCheckRepository;
    private FileCheckRepository fileCheckRepository;
    private FileReviewConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        checkReviewServiceClient = mock(CheckReviewServiceClient.class);
        reviewCheckRepository = mock(ReviewCheckRepository.class);
        fileCheckRepository = mock(FileCheckRepository.class);

        consumer = new FileReviewConsumer(objectMapper, checkReviewServiceClient, reviewCheckRepository, fileCheckRepository);
    }

    @Test
    void testConsume_successful() throws Exception {
        String message = "valid-json";
        String reviewId = "abc123";
        String reviewText = "This product is great";
        String fileKey = "file.csv";

        FileReviewEvent event = new FileReviewEvent(reviewId, fileKey, reviewText);
        CheckReviewDto checkReviewDto = CheckReviewDto.builder()
                .text(reviewText)
                .fakeScorePercentage(85.0f)
                .isFake(true)
                .build();
        FileCheck fileCheck = FileCheck.builder().fileKey(fileKey).build();
        Acknowledgment ack = mock(Acknowledgment.class);

        when(objectMapper.readValue(message, FileReviewEvent.class)).thenReturn(event);
        when(checkReviewServiceClient.checkReview(any())).thenReturn(checkReviewDto);
        when(fileCheckRepository.findById(fileKey)).thenReturn(Optional.of(fileCheck));

        consumer.consume(message, ack);

        verify(reviewCheckRepository).save(argThat(review ->
                review.getId().equals(reviewId)
                        && review.getText().equals(reviewText)
                        && review.getFakeScorePercentage() == 85.0
                        && review.getIsFake()
                        && review.getFile().equals(fileCheck)
        ));
        verify(ack).acknowledge();
    }

    @Test
    void testConsume_deserializationFails() throws Exception {
        Acknowledgment ack = mock(Acknowledgment.class);
        when(objectMapper.readValue(anyString(), eq(FileReviewEvent.class)))
                .thenThrow(new JsonMappingException(null, "Invalid JSON"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            consumer.consume("bad-json", ack);
        });

        assertEquals("Could not deserialize FileReceivedEvent from json", exception.getMessage());
        verifyNoInteractions(checkReviewServiceClient, reviewCheckRepository, fileCheckRepository);
        verify(ack, never()).acknowledge();
    }

    @Test
    void testConsume_fileCheckNotFound() throws Exception {
        String message = "valid-json";
        FileReviewEvent event = new FileReviewEvent("id123", "missing-key", "text");
        Acknowledgment ack = mock(Acknowledgment.class);

        when(objectMapper.readValue(message, FileReviewEvent.class)).thenReturn(event);
        when(checkReviewServiceClient.checkReview(any())).thenReturn(
                CheckReviewDto.builder().text("text").fakeScorePercentage(50.0f).isFake(false).build());
        when(fileCheckRepository.findById("missing-key")).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            consumer.consume(message, ack);
        });

        assertEquals("File not found", exception.getMessage());
        verify(ack, never()).acknowledge();
    }

}