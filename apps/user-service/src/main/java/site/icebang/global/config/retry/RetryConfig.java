package site.icebang.global.config.retry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RetryConfig {

  @Bean
  public RetryTemplate taskExecutionRetryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();

    // 1. 재시도 정책 설정: 최대 3번 시도
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(3);
    retryTemplate.setRetryPolicy(retryPolicy);

    // 2. 재시도 간격 설정: 5초 고정 간격
    FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(5000L); // 5000ms = 5초
    retryTemplate.setBackOffPolicy(backOffPolicy);

    return retryTemplate;
  }
}
