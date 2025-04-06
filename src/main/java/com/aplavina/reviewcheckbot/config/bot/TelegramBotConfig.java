package com.aplavina.reviewcheckbot.config.bot;

import com.aplavina.reviewcheckbot.producer.file.FileReceivedProducer;
import com.aplavina.reviewcheckbot.service.bot.TelegramBotService;
import com.aplavina.reviewcheckbot.service.review.CheckReviewService;
import com.aplavina.reviewcheckbot.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {
    private final CheckReviewService checkReviewService;
    private final RestTemplate restTemplate;
    private final S3Service s3Service;
    private final FileReceivedProducer fileReceivedProducer;

    @Bean
    @SneakyThrows
    public TelegramBotService telegramBot(@Value("${bot.token}") String token,
                                          TelegramBotsApi telegramBotsApi) {
        DefaultBotOptions botOptions = new DefaultBotOptions();
        TelegramBotService bot = new TelegramBotService(
                botOptions,
                token,
                checkReviewService,
                restTemplate,
                s3Service,
                fileReceivedProducer
        );
        telegramBotsApi.registerBot(bot);
        return bot;
    }

    @Bean
    @SneakyThrows
    public TelegramBotsApi telegramBotsApi() {
        return new TelegramBotsApi(DefaultBotSession.class);
    }
}
