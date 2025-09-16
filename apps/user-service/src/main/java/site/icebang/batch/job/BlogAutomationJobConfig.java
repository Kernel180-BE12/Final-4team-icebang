package site.icebang.batch.job; // 패키지 경로 수정

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

import site.icebang.batch.tasklet.*;

/** [배치 시스템 구현] 트렌드 기반 블로그 자동화 워크플로우를 구성하는 Job들을 정의합니다. */
@Configuration
@RequiredArgsConstructor
public class BlogAutomationJobConfig {

  // --- Tasklets ---
  private final ExtractTrendKeywordTasklet extractTrendKeywordTask;
  private final SearchProductsFromMallTasklet searchProductsFromMallTask;
  private final MatchProductWithKeywordTasklet matchProductWithKeywordTask;
  private final FindSimilarProductsTasklet findSimilarProductsTask;
  private final CrawlSelectedProductTasklet crawlSelectedProductTask;
  private final GenerateBlogContentTasklet generateBlogContentTask;
  private final PublishBlogPostTasklet publishBlogPostTask;

  /** Job 1: 상품 선정 및 정보 수집 키워드 추출부터 최종 상품 정보 크롤링까지의 과정을 책임집니다. */
  @Bean
  public Job productSelectionJob(
      JobRepository jobRepository,
      Step extractTrendKeywordStep,
      Step searchProductsFromMallStep,
      Step matchProductWithKeywordStep,
      Step findSimilarProductsStep,
      Step crawlSelectedProductStep) {
    return new JobBuilder("productSelectionJob", jobRepository)
        .start(extractTrendKeywordStep)
        .next(searchProductsFromMallStep)
        .next(matchProductWithKeywordStep)
        .next(findSimilarProductsStep)
        .next(crawlSelectedProductStep)
        .build();
  }

  /** Job 2: 콘텐츠 생성 및 발행 수집된 상품 정보로 블로그 콘텐츠를 생성하고 발행합니다. */
  @Bean
  public Job contentPublishingJob(
      JobRepository jobRepository, Step generateBlogContentStep, Step publishBlogPostStep) {
    return new JobBuilder("contentPublishingJob", jobRepository)
        .start(generateBlogContentStep)
        .next(publishBlogPostStep)
        .build();
  }

  // --- Steps for productSelectionJob ---
  @Bean
  public Step extractTrendKeywordStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("extractTrendKeywordStep", jobRepository)
        .tasklet(extractTrendKeywordTask, transactionManager)
        .build();
  }

  @Bean
  public Step searchProductsFromMallStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("searchProductsFromMallStep", jobRepository)
        .tasklet(searchProductsFromMallTask, transactionManager)
        .build();
  }

  @Bean
  public Step matchProductWithKeywordStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("matchProductWithKeywordStep", jobRepository)
        .tasklet(matchProductWithKeywordTask, transactionManager)
        .build();
  }

  @Bean
  public Step findSimilarProductsStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("findSimilarProductsStep", jobRepository)
        .tasklet(findSimilarProductsTask, transactionManager)
        .build();
  }

  @Bean
  public Step crawlSelectedProductStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("crawlSelectedProductStep", jobRepository)
        .tasklet(crawlSelectedProductTask, transactionManager)
        .build();
  }

  // --- Steps for contentPublishingJob ---
  @Bean
  public Step generateBlogContentStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("generateBlogContentStep", jobRepository)
        .tasklet(generateBlogContentTask, transactionManager)
        .build();
  }

  @Bean
  public Step publishBlogPostStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("publishBlogPostStep", jobRepository)
        .tasklet(publishBlogPostTask, transactionManager)
        .build();
  }
}
