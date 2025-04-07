package com.aplavina.reviewcheckbot.config.multithreading;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class MultithreadingConfig {
    @Value("${multithreading.scheduler.pool-size}")
    private int schedulerPoolSize;

    @Bean
    public ExecutorService schedulerThreadPool() {
        return Executors.newFixedThreadPool(schedulerPoolSize);
    }
}
