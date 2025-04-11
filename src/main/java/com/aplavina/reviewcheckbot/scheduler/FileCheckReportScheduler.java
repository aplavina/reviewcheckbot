package com.aplavina.reviewcheckbot.scheduler;

import com.aplavina.reviewcheckbot.model.FileCheck;
import com.aplavina.reviewcheckbot.model.ReviewCheck;
import com.aplavina.reviewcheckbot.repository.FileCheckRepository;
import com.aplavina.reviewcheckbot.service.bot.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Async("schedulerThreadPool")
    @Transactional
    @Scheduled(cron = "${scheduler.reports.cron}", zone = "${scheduler.reports.zone}")
    public void sendReports() {
        List<FileCheck> checkedFiles = fileCheckRepository.findChecked();
        for (FileCheck fileCheck : checkedFiles) {
            log.info("Preparing report for file check with file key: {}", fileCheck.getFileKey());
            List<ReviewCheck> reviewChecks = fileCheck.getReviewChecks();
            double avgFakeScore = reviewChecks.stream()
                    .mapToDouble(ReviewCheck::getFakeScorePercentage)
                    .average()
                    .orElse(0.0);
            double percentFake = 0;
            if (!reviewChecks.isEmpty()) {
                percentFake = reviewChecks.stream()
                        .filter(reviewCheck -> reviewCheck.getFakeScorePercentage() > 0)
                        .count() * 100.0 / reviewChecks.size();
            }
            String report = String.format("Fake score: %.2f%%\n" +
                            "Percent of fake reviews: %.2f%%",
                    avgFakeScore, percentFake);
            telegramBotService.sendMessage(fileCheck.getChatId(), report);
            fileCheckRepository.delete(fileCheck);
        }
    }
}
