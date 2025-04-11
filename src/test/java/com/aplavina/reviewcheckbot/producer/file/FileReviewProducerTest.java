package com.aplavina.reviewcheckbot.producer.file;

import com.aplavina.reviewcheckbot.event.file.FileReviewEvent;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileReviewProducerTest {
    private ObjectMapper objectMapper;
    private KafkaTemplate<String, String> kafkaTemplate;
    private FileReviewProducer producer;

    private final String TOPIC_NAME = "file-review-topic";

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        producer = new FileReviewProducer(objectMapper, kafkaTemplate);
        ReflectionTestUtils.setField(producer, "fileReviewTopicName", TOPIC_NAME);
    }

    @Test
    void testPublish_successful() throws Exception {
        FileReviewEvent event = new FileReviewEvent();
        String serializedEvent = "{\"dummy\":true}";

        when(objectMapper.writeValueAsString(event)).thenReturn(serializedEvent);

        producer.publish(event);

        verify(kafkaTemplate).send(eq(TOPIC_NAME), eq(serializedEvent));
    }

    @Test
    void testPublish_throwsJsonException() throws Exception {
        FileReviewEvent event = new FileReviewEvent();

        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonMappingException(null, "Test JSON exception"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> producer.publish(event));

        assertEquals("Exception when converting FileReviewEvent to json", exception.getMessage());
        verify(kafkaTemplate, never()).send(any(), any());
    }
}