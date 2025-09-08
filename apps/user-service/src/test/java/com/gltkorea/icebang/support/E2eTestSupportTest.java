package com.gltkorea.icebang.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "classpath:sql/schema.sql")
class E2eTestSupportTest extends E2eTestSupport {

  @Test
  void shouldStartWithRandomPort() {
    // 포트가 제대로 할당되었는지 확인
    assertThat(port).isGreaterThan(0);
    assertThat(getBaseUrl()).startsWith("http://localhost:");
    assertThat(getApiUrl("/test")).contains("/api/test");
  }

  @Test
  void shouldHaveRestTemplate() {
    // RestTemplate이 주입되었는지 확인
    assertThat(restTemplate).isNotNull();
  }

  @Test
  void shouldConnectToMariaDBContainer() {
    // 실제 DB 연결 확인
    String response = restTemplate.getForObject(getApiUrl("/health"), String.class);
    // health check endpoint가 있다면 사용, 없으면 간단한 컨트롤러 만들어서 테스트
  }
}
