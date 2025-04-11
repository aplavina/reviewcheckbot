package com.aplavina.reviewcheckbot.repository;

import com.aplavina.reviewcheckbot.model.FileCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class FileCheckRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FileCheckRepository fileCheckRepository;

    @Container
    public static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER
            = new PostgreSQLContainer<>("postgres:13.3");

    @Container
    public static final RedisContainer REDIS_CONTAINER
            = new RedisContainer(DockerImageName.parse("redis/redis-stack:latest"));

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeAll
    public static void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) throws InterruptedException {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        String host = REDIS_CONTAINER.getHost();
        Integer port = REDIS_CONTAINER.getMappedPort(6379);
        System.setProperty("test.redis.host", host);
        System.setProperty("test.redis.port", String.valueOf(port));
        Thread.sleep(1000);
    }

    @BeforeEach
    void setupDatabase() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS review_check");
        jdbcTemplate.execute("DROP TABLE IF EXISTS file_check");

        jdbcTemplate.execute("""
                    CREATE TABLE file_check (
                        file_key VARCHAR NOT NULL,
                        chat_id VARCHAR NOT NULL,
                        count BIGINT DEFAULT NULL,
                        CONSTRAINT pk_file_check PRIMARY KEY (file_key)
                    );
                """);

        jdbcTemplate.execute("""
                    CREATE TABLE review_check (
                        id VARCHAR NOT NULL,
                        text VARCHAR NOT NULL,
                        is_fake BOOLEAN,
                        fake_score_percentage FLOAT,
                        file_id VARCHAR,
                        CONSTRAINT pk_review_check PRIMARY KEY (id),
                        CONSTRAINT FK_REVIEW_CHECK_ON_FILE FOREIGN KEY (file_id) REFERENCES file_check (file_key)
                    );
                """);

        jdbcTemplate.execute("""
                    INSERT INTO file_check (file_key, chat_id, count) VALUES 
                    ('file-1', 'chat-123', 2), 
                    ('file-2', 'chat-456', 1), 
                    ('file-3', 'chat-789', NULL);
                """);

        jdbcTemplate.execute("""
                    INSERT INTO review_check (id, text, is_fake, fake_score_percentage, file_id) VALUES 
                    ('rev-1', 'Review one', false, 10.5, 'file-1'), 
                    ('rev-2', 'Review two', true, 95.0, 'file-1'), 
                    ('rev-3', 'Review three', false, 20.0, 'file-2');
                """);
    }

    @Test
    void testSetCount_updatesCountCorrectly() {
        fileCheckRepository.setCount("file-3", 5);

        FileCheck updated = fileCheckRepository.findById("file-3").orElseThrow();
        assertThat(updated.getReviewsCount()).isEqualTo(5);
    }

    @Test
    void testFindChecked_returnsCorrectFiles() {
        List<FileCheck> checked = fileCheckRepository.findChecked();

        assertThat(checked).hasSize(2);
        assertThat(checked).extracting("fileKey").containsExactlyInAnyOrder("file-1", "file-2");
    }
}