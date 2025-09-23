package site.icebang.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * FastAPI 서버 연동을 위한 설정값을 application.yml에서 타입-세이프(Type-safe)하게 바인딩하는 클래스입니다.
 *
 * <p>이 클래스는 {@code @ConfigurationProperties}를 통해 'api.fastapi' 경로의 설정값을 자동으로 주입받습니다.
 * {@code @Validated}와 {@code @NotBlank}를 사용하여, 필수 설정값(url)이 누락될 경우 애플리케이션 시작 시점에 즉시 에러를 발생시켜 설정 오류를
 * 방지합니다.
 *
 * <h2>사용 예제:</h2>
 *
 * <pre>{@code
 * @Component
 * @RequiredArgsConstructor
 * public class FastApiAdapter {
 * private final FastApiProperties properties;
 *
 * public void someMethod() {
 * String baseUrl = properties.getUrl(); // 설정된 URL 사용
 * // ...
 * }
 * }
 * }</pre>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@Getter
@Setter
@Component // Component로 등록하여 Spring이 Bean으로 관리하도록 함
@ConfigurationProperties(prefix = "api.fastapi") // yml의 "api.fastapi" 접두사를 가진 설정을 매핑
@Validated // 아래의 유효성 검사 어노테이션을 활성화
public class FastApiProperties {

  /**
   * FastAPI 서버의 기본 URL 주소입니다.
   *
   * <p>{@code @NotBlank} 어노테이션이 적용되어 있어, application.yml 파일에 반드시 값이 존재해야 합니다. (예:
   * "http://host.docker.internal:8000")
   */
  @NotBlank // 값이 비어있을 수 없음을 검증
  private String url;

  /**
   * API 호출 시 적용될 타임아웃 시간 (밀리초 단위)입니다.
   *
   * <p>별도로 설정하지 않을 경우 기본값으로 5000ms (5초)가 적용됩니다.
   */
  private int timeout = 5000; // 기본값 5초 설정
}
