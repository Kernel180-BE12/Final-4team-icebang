//package com.gltkorea.icebang.config.batch;
//
//import java.time.LocalDateTime;
//
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.batch.MyBatisBatchItemWriter;
//import org.mybatis.spring.batch.MyBatisPagingItemReader;
//import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
//import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.support.SynchronizedItemStreamReader;
//import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.ResourceAccessException;
//import org.springframework.web.client.RestTemplate;
//
//import com.gltkorea.icebang.dto.FastApiCrawlingResponse;
//import com.gltkorea.icebang.dto.ProductCrawlingData;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * 외부 크롤링 API를 호출하여 상품 정보를 업데이트하는 배치(Batch) 작업을 설정 Job은 병렬 처리와 내결함성(Fault Tolerance) 정책을 적용해 안정적인 운영
// * 환경을 고려
// */
//@Slf4j
//@Configuration
//@RequiredArgsConstructor
//public class CrawlingBatchConfig {
//
//  // Spring Batch의 메타데이터(Job 실행 기록, 상태 등)를 관리하는 리포지토리
//  private final JobRepository jobRepository;
//  // 배치 Step 내에서 트랜잭션을 관리하기 위한 트랜잭션 매니저
//  private final PlatformTransactionManager transactionManager;
//  // MyBatis 쿼리를 실행하기 위한 SqlSessionFactory
//  private final SqlSessionFactory sqlSessionFactory;
//  // 외부 API(FastAPI)와 통신하기 위한 RestTemplate
//  private final RestTemplate restTemplate;
//
//  /** 'crawlingJob'이라는 이름의 Batch Job을 정의 Job은 배치 작업의 가장 큰 단위이며, 하나 이상의 Step으로 구성 */
//  @Bean
//  public Job crawlingJob() {
//    return new JobBuilder("crawlingJob", jobRepository)
//        .start(crawlingStep()) // 'crawlingStep'이라는 Step으로 Job을 시작
//        .build();
//  }
//
//  /**
//   * 'crawlingStep'이라는 이름의 Batch Step을 정의 Step은 Reader, Processor, Writer의 흐름으로 구성되며, 이 Step은 병렬 처리와
//   * 실패 처리 정책이 적용
//   */
//  @Bean
//  public Step crawlingStep() {
//    return new StepBuilder("crawlingStep", jobRepository)
//        // <읽어올 데이터 타입, 처리 후 데이터 타입>을 지정합니다.
//        // chunk(10): 10개의 아이템을 하나의 트랜잭션 단위로 묶어서 처리합니다.
//        .<ProductCrawlingData, ProductCrawlingData>chunk(10, transactionManager)
//        .reader(synchronizedProductReader()) // 데이터 읽기
//        .processor(crawlingProcessor()) // 데이터 가공
//        .writer(productWriter()) // 데이터 쓰기
//
//        // --- 실패 처리 정책 (Fault Tolerance) ---
//        .faultTolerant()
//        // 네트워크 오류(ResourceAccessException)는 아이템당 3번까지 재시도합니다.
//        .retry(ResourceAccessException.class)
//        .retryLimit(3)
//        // 4xx 에러(HttpClientErrorException)는 영구 실패로 간주하고 아이템당 100번까지 건너뜀
//        .skip(HttpClientErrorException.class)
//        .skipLimit(100)
//
//        // --- 병렬 처리 설정 ---
//        .taskExecutor(taskExecutor())
//        .build();
//  }
//
//  /** 병렬 처리를 위한 TaskExecutor(쓰레드 풀)를 정의 */
//  @Bean
//  public TaskExecutor taskExecutor() {
//    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//    executor.setCorePoolSize(10); // 동시에 실행할 기본 쓰레드 수
//    executor.setMaxPoolSize(10); // 최대 쓰레드 수
//    executor.setThreadNamePrefix("batch-thread-"); // 쓰레드 이름 접두사
//    executor.initialize();
//    return executor;
//  }
//
//  /**
//   * ItemReader를 스레드로부터 안전하게 만들기 위해 SynchronizedItemStreamReader로 감쌈 병렬 처리 시 여러 스레드가 Reader에 동시에
//   * 접근하는 것을 방지
//   */
//  @Bean
//  public SynchronizedItemStreamReader<ProductCrawlingData> synchronizedProductReader() {
//    // 실제 데이터를 읽는 Reader는 private 메서드로 정의하여 외부 노출을 최소화합니다.
//    MyBatisPagingItemReader<ProductCrawlingData> reader = productReader();
//
//    return new SynchronizedItemStreamReaderBuilder<ProductCrawlingData>().delegate(reader).build();
//  }
//
//  /**
//   * 실제 DB에서 크롤링할 상품 목록을 읽어오는 ItemReader MyBatisPagingItemReader는 MyBatis를 사용하여 페이징 기반으로 대용량 데이터를
//   * 안전하게 읽어옴
//   */
//  private MyBatisPagingItemReader<ProductCrawlingData> productReader() {
//    return new MyBatisPagingItemReaderBuilder<ProductCrawlingData>()
//        .sqlSessionFactory(sqlSessionFactory)
//        // CrawlingMapper.xml에 정의된 쿼리의 전체 경로(namespace + id)를 지정합니다.
//        .queryId("com.gltkorea.icebang.mapper.CrawlingMapper.findProductsToCrawl")
//        .pageSize(10) // 한 번에 DB에서 조회할 데이터 수. chunk 사이즈와 맞추는 것이 좋습니다.
//        .build();
//  }
//
//  /**
//   * ItemProcessor: Reader가 읽어온 각 아이템을 가공 여기서는 FastAPI 크롤링 서버를 호출하여 추가 정보를 얻어옴 예외를 직접
//   * 처리(try-catch)하지 않고 Step으로 던져서, Step의 재시도/건너뛰기 정책이 동작하도록 함
//   */
//  @Bean
//  public ItemProcessor<ProductCrawlingData, ProductCrawlingData> crawlingProcessor() {
//    return product -> {
//      log.info("Requesting crawl for product: {}", product.getName());
//
//      // FastAPI URL을 코드에 직접 작성 (추후 외부 설정으로 분리 권장)
//      String fastApiUrl = "http://your-fastapi-server.com/crawl?url=" + product.getUrlToCrawl();
//
//      // RestTemplate이 예외를 던지면 Step의 retry/skip 정책이 이를 감지하고 처리
//      ResponseEntity<FastApiCrawlingResponse> response =
//          restTemplate.getForEntity(fastApiUrl, FastApiCrawlingResponse.class);
//
//      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//        FastApiCrawlingResponse crawledData = response.getBody();
//
//        // API 응답 결과로 ProductCrawlingData 객체 업데이트
//        product.setPrice(crawledData.getPrice());
//        product.setStockStatus(crawledData.getStockStatus());
//        product.setLastCrawledAt(LocalDateTime.now());
//
//        return product; // 가공이 완료된 데이터를 Writer로 전달
//      }
//
//      // API 호출은 성공했으나, 응답이 비정상적인 경우 (예: body가 null)
//      log.warn(
//          "Crawling API call returned non-successful status for product {}: {}",
//          product.getName(),
//          response.getStatusCode());
//      return null; // 이 아이템만 건너뜀 (Writer로 전달되지 않음)
//    };
//  }
//
//  /**
//   * ItemWriter: 가공된 데이터 묶음(Chunk)을 DB에 일괄 저장 MyBatisBatchItemWriter는 내부적으로 JDBC Batch Update를 사용하여
//   * 성능이 좋음
//   */
//  @Bean
//  public MyBatisBatchItemWriter<ProductCrawlingData> productWriter() {
//    return new MyBatisBatchItemWriterBuilder<ProductCrawlingData>()
//        .sqlSessionFactory(sqlSessionFactory)
//        // CrawlingMapper.xml에 정의된 update 쿼리의 전체 경로를 지정
//        .statementId("com.gltkorea.icebang.mapper.CrawlingMapper.updateCrawledProduct")
//        .build();
//  }
//}
