package site.icebang.global.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    // 1. SimpleClientHttpRequestFactory 객체를 직접 생성
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

    // 2. 타임아웃 설정 (이 메서드들은 deprecated 아님)
    requestFactory.setConnectTimeout(Duration.ofSeconds(30000));
    requestFactory.setReadTimeout(Duration.ofSeconds(30000));

    // 3. 빌더에 직접 생성한 requestFactory를 설정
    return builder.requestFactory(() -> requestFactory).build();
  }
}
