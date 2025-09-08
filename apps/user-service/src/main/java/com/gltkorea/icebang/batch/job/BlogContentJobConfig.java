package com.gltkorea.icebang.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.gltkorea.icebang.batch.tasklet.ContentGenerationTasklet;
import com.gltkorea.icebang.batch.tasklet.KeywordExtractionTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BlogContentJobConfig {

  // 변경점 1: Factory 대신 실제 Tasklet만 필드로 주입받습니다.
  private final KeywordExtractionTasklet keywordExtractionTasklet;
  private final ContentGenerationTasklet contentGenerationTasklet;

  @Bean
  public Job blogContentJob(
      JobRepository jobRepository, Step keywordExtractionStep, Step contentGenerationStep) {
    return new JobBuilder("blogContentJob", jobRepository) // 변경점 2: JobBuilder를 직접 생성합니다.
        .start(keywordExtractionStep)
        .next(contentGenerationStep)
        .build();
  }

  @Bean
  public Step keywordExtractionStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("keywordExtractionStep", jobRepository) // 변경점 3: StepBuilder를 직접 생성합니다.
        .tasklet(
            keywordExtractionTasklet,
            transactionManager) // 변경점 4: tasklet에 transactionManager를 함께 전달합니다.
        .build();
  }

  @Bean
  public Step contentGenerationStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("contentGenerationStep", jobRepository)
        .tasklet(contentGenerationTasklet, transactionManager)
        .build();
  }
}
