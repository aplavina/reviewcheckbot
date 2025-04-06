package com.aplavina.reviewcheckbot.config.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    @Value("${kafka.bootstrap-server}")
    private String bootstrapServer;

    @Value("${kafka.topics.received-file.name}")
    private String fileReceivedTopicName;
    @Value("${kafka.topics.received-file.partitions}")
    private int fileReceivedTopicPartitions;
    @Value("${kafka.topics.received-file.replication-factor}")
    private int fileReceivedTopicReplicationFactor;

    @Value("${kafka.topics.file-review.name}")
    private String fileReviewTopicName;
    @Value("${kafka.topics.file-review.partitions}")
    private int fileReviewTopicPartitions;
    @Value("${kafka.topics.file-review.replication-factor}")
    private int fileReviewTopicReplicationFactor;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic fileReceivedTopic() {
        return new NewTopic(fileReceivedTopicName, fileReceivedTopicPartitions, (short) fileReceivedTopicReplicationFactor);
    }

    @Bean
    public NewTopic fileReviewTopic() {
        return new NewTopic(fileReviewTopicName, fileReviewTopicPartitions, (short) fileReviewTopicReplicationFactor);
    }
}
