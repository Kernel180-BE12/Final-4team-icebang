package site.icebang.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * FastAPI 연동을 위한 설정값을 application.yml에서 바인딩하는 클래스
 */
@Getter
@Setter
@Component // Component로 등록하여 Spring이 Bean으로 관리하도록 함
@ConfigurationProperties(prefix = "api.fastapi") // yml의 "api.fastapi" 접두사를 가진 설정을 매핑
@Validated // 아래의 유효성 검사 어노테이션을 활성화
public class FastApiProperties {

    /**
     * FastAPI 서버의 기본 URL
     */
    @NotBlank // 값이 비어있을 수 없음을 검증
    private String url;

    /**
     * API 호출 시 적용될 타임아웃 (밀리초 단위)
     */
    private int timeout = 5000; // 기본값 5초 설정
}
