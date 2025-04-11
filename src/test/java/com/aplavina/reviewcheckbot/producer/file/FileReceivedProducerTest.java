package com.aplavina.reviewcheckbot.producer.file;

import com.aplavina.reviewcheckbot.event.file.FileReceivedEvent;
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
class FileReceivedProducerTest {
    private ObjectMapper objectMapper;
    private KafkaTemplate<String, String> kafkaTemplate;
    private FileReceivedProducer producer;

    private final String TOPIC_NAME = "received-file-topic";

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        producer = new FileReceivedProducer(objectMapper, kafkaTemplate);
        ReflectionTestUtils.setField(producer, "receivedFileTopicName", TOPIC_NAME);
    }

    @Test
    void testPublish_successful() throws Exception {
        FileReceivedEvent event = new FileReceivedEvent();
        String serialized = "{\"event\":\"file_received\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(serialized);

        producer.publish(event);

        verify(kafkaTemplate).send(eq(TOPIC_NAME), eq(serialized));
    }

    @Test
    void testPublish_jsonProcessingException() throws Exception {
        FileReceivedEvent event = new FileReceivedEvent();

        when(objectMapper.writeValueAsString(event))
                .thenThrow(new JsonMappingException(null, "Serialization failed"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            producer.publish(event);
        });

        assertEquals("Exception when converting FileReceivedEvent to json", exception.getMessage());
        verify(kafkaTemplate, never()).send(any(), any());
    }
}