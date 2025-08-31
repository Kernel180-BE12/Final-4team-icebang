package com.gltkorea.icebang.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CrawlingScheduler {

    private final JobLauncher jobLauncher;
    private final Job crawlingJob; // CrawlingBatchConfig에 정의된 Job Bean 주입

    // 매일 오전 8시에 실행 -> 추 후 application.yml로 설정 이동 예정
    @Scheduled(cron = "0 0 8 * * *")
    public void runCrawlingJob() throws Exception {
        jobLauncher.run(crawlingJob, new JobParametersBuilder()
                .addString("runAt", LocalDateTime.now().toString())
                .toJobParameters());
    }
}