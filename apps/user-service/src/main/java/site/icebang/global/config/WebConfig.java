package site.icebang.global.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 애플리케이션의 웹 관련 설정을 담당하는 Java 기반 설정 클래스입니다.
 *
 * <p>이 클래스는 애플리케이션 전역에서 사용될 웹 관련 빈(Bean)들을 생성하고 구성합니다. 현재는 외부 API 통신을 위한 {@code RestTemplate} 빈을
 * 중앙에서 관리하는 역할을 합니다.
 *
 * <h2>주요 기능:</h2>
 *
 * <ul>
 *   <li>커넥션 및 읽기 타임아웃이 설정된 RestTemplate 빈 생성
 * </ul>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@Configuration
public class WebConfig {

  /**
   * 외부 API 통신을 위한 RestTemplate 빈을 생성하여 스프링 컨테이너에 등록합니다.
   *
   * <p>기본 {@code RestTemplateBuilder}를 사용하되, 커넥션 및 읽기 타임아웃을 각각 30초로 명시적으로 설정하기 위해 {@code
   * SimpleClientHttpRequestFactory}를 구성하여 주입합니다. 이렇게 생성된 RestTemplate 빈은 애플리케이션의 다른 컴포넌트에서 주입받아 외부
   * 시스템과의 HTTP 통신에 사용됩니다.
   *
   * @param builder Spring Boot가 자동으로 구성해주는 RestTemplateBuilder 객체
   * @return 타임아웃이 설정된 RestTemplate 인스턴스
   * @see RestTemplate
   * @see RestTemplateBuilder
   * @since v0.1.0
   */
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
