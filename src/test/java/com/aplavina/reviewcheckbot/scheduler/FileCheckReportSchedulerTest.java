package com.aplavina.reviewcheckbot.scheduler;

import com.aplavina.reviewcheckbot.model.FileCheck;
import com.aplavina.reviewcheckbot.model.ReviewCheck;
import com.aplavina.reviewcheckbot.repository.FileCheckRepository;
import com.aplavina.reviewcheckbot.service.bot.TelegramBotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileCheckReportSchedulerTest {
    private FileCheckRepository fileCheckRepository;
    private TelegramBotService telegramBotService;
    private FileCheckReportScheduler scheduler;

    @BeforeEach
    void setUp() {
        fileCheckRepository = mock(FileCheckRepository.class);
        telegramBotService = mock(TelegramBotService.class);
        scheduler = new FileCheckReportScheduler(fileCheckRepository, telegramBotService);
    }

    @Test
    void testSendReports_withReviewChecks() {
        ReviewCheck r1 = new ReviewCheck();
        r1.setFakeScorePercentage(30.0f);
        ReviewCheck r2 = new ReviewCheck();
        r2.setFakeScorePercentage(0.0f);
        ReviewCheck r3 = new ReviewCheck();
        r3.setFakeScorePercentage(90.0f);

        FileCheck fileCheck = new FileCheck();
        fileCheck.setFileKey("file1.csv");
        fileCheck.setChatId("123456789");
        fileCheck.setReviewChecks(Arrays.asList(r1, r2, r3));

        when(fileCheckRepository.findChecked()).thenReturn(List.of(fileCheck));

        scheduler.sendReports();
        verify(telegramBotService).sendMessage(eq("123456789"),
                eq("Fake score: 40.00%\nPercent of fake reviews: 66.67%"));
        verify(fileCheckRepository).delete(fileCheck);
    }

    @Test
    void testSendReports_withNoReviewChecks() {
        FileCheck fileCheck = new FileCheck();
        fileCheck.setFileKey("file2.csv");
        fileCheck.setChatId("123456789");
        fileCheck.setReviewChecks(Collections.emptyList());

        when(fileCheckRepository.findChecked()).thenReturn(List.of(fileCheck));

        scheduler.sendReports();

        verify(telegramBotService).sendMessage(eq("123456789"),
                eq("Fake score: 0.00%\nPercent of fake reviews: 0.00%"));
        verify(fileCheckRepository).delete(fileCheck);
    }

    @Test
    void testSendReports_withNoCheckedFiles() {
        when(fileCheckRepository.findChecked()).thenReturn(Collections.emptyList());

        scheduler.sendReports();

        verify(telegramBotService, never()).sendMessage(anyString(), anyString());
        verify(fileCheckRepository, never()).delete(any());
    }
}