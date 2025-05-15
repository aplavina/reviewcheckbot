package com.aplavina.reviewcheckbot.scheduler;

import com.aplavina.reviewcheckbot.model.FileCheck;
import com.aplavina.reviewcheckbot.model.ReviewCheck;
import com.aplavina.reviewcheckbot.repository.FileCheckRepository;
import com.aplavina.reviewcheckbot.service.bot.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCheckReportScheduler {
    private final FileCheckRepository fileCheckRepository;
    private final TelegramBotService telegramBotService;

    @Value("${min-review-text-length}")
    private int minTextLength;

    @Async("schedulerThreadPool")
    @Transactional
    @Scheduled(cron = "${scheduler.reports.cron}", zone = "${scheduler.reports.zone}")
    public void sendReports() {
        List<FileCheck> checkedFiles = fileCheckRepository.findChecked();
        for (FileCheck fileCheck : checkedFiles) {
            log.info("Preparing report for file check with file key: {}", fileCheck.getFileKey());
            List<ReviewCheck> reviewChecks = fileCheck.getReviewChecks();
            double avgFakeScore = reviewChecks.stream()
                    .filter(check -> check.getText().split(" ").length >= minTextLength)
                    .mapToDouble(ReviewCheck::getFakeScorePercentage)
                    .average()
                    .orElse(0.0);
            double percentFake = 0;
            if (!reviewChecks.isEmpty()) {
                percentFake = reviewChecks.stream()
                        .filter(ReviewCheck::getIsFake)
                        .count() * 100.0 / reviewChecks.size();
            }
            String report = String.format("Fake score: %.2f%%\n" +
                            "Percent of fake reviews: %.2f%%\n" +
                            "Reviews with text length less than %d words were not used for statistics",
                    avgFakeScore, percentFake, minTextLength);
            telegramBotService.sendMessage(fileCheck.getChatId(), report);
            fileCheckRepository.delete(fileCheck);
        }
    }
}
