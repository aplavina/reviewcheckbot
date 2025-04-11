package com.aplavina.reviewcheckbot.service.bot;

import com.aplavina.reviewcheckbot.event.file.FileReceivedEvent;
import com.aplavina.reviewcheckbot.producer.file.FileReceivedProducer;
import com.aplavina.reviewcheckbot.service.review.CheckReviewService;
import com.aplavina.reviewcheckbot.service.s3.S3Service;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    private final CheckReviewService checkReviewService;
    private final RestTemplate restTemplate;
    private final S3Service s3Service;
    private final FileReceivedProducer fileReceivedProducer;

    @Value("${bot.token}")
    private String botToken;

    public TelegramBotService(
            DefaultBotOptions options,
            String botToken,
            CheckReviewService checkReviewService,
            RestTemplate restTemplate,
            S3Service s3Service,
            FileReceivedProducer fileReceivedProducer) {
        super(options, botToken);
        this.checkReviewService = checkReviewService;
        this.restTemplate = restTemplate;
        this.s3Service = s3Service;
        this.fileReceivedProducer = fileReceivedProducer;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = checkReviewService.checkReview(update.getMessage().getText());
            SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), text);
            sendApiMethod(sendMessage);
        } else if (update.hasMessage() && update.getMessage().hasDocument()) {
            InputStream fileStream = downloadFileFromTelegram(update.getMessage().getDocument().getFileId());
            String key = UUID.randomUUID().toString();
            s3Service.uploadFile(fileStream, "text/csv", key);
            String chatId = update.getMessage().getChatId().toString();
            FileReceivedEvent fileReceivedEvent = new FileReceivedEvent(key, chatId);
            fileReceivedProducer.publish(fileReceivedEvent);
            SendMessage sendMessage = new SendMessage(chatId, "File received. Will send you the report soon.");
            sendApiMethod(sendMessage);
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        try {
            sendApiMethod(sendMessage);
        } catch (Exception e) {
            log.error("Failed to send message", e);
            throw new IllegalStateException("Failed to send message", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "Default Username";
    }

    public InputStream downloadFileFromTelegram(String fileId) {
        String getFileUrl = "https://api.telegram.org/bot" + botToken + "/getFile?file_id=" + fileId;
        ResponseEntity<Map> response = restTemplate.getForEntity(getFileUrl, Map.class);
        String filePath = (String) ((Map) (response.getBody().get("result"))).get("file_path");
        String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
        RequestCallback requestCallback = request -> request.getHeaders()
                .setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        ResponseExtractor<InputStream> responseExtractor = clientHttpResponse -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            clientHttpResponse.getBody().transferTo(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        };
        return restTemplate.execute(fileUrl, HttpMethod.GET, requestCallback, responseExtractor);
    }
}


