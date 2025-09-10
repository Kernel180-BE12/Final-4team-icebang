package site.icebang.e2e.setup.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import site.icebang.e2e.setup.annotation.E2eTest;
import site.icebang.e2e.setup.config.E2eTestConfiguration;

@Import(E2eTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@E2eTest
public abstract class E2eTestSupport {
  @Autowired protected TestRestTemplate restTemplate;

  @Autowired protected ObjectMapper objectMapper;

  @LocalServerPort protected int port;

  @Autowired protected WebApplicationContext webApplicationContext;

  protected MockMvc mockMvc;

  protected String getBaseUrl() {
    return "http://localhost:" + port;
  }

  protected String getApiUrl(String path) {
    return getBaseUrl() + path;
  }

  protected String getV0ApiUrl(String path) {
    return getBaseUrl() + "/v0" + path;
  }

  /** 테스트 시나리오 단계별 로깅을 위한 유틸리티 메서드 */
  protected void logStep(int stepNumber, String description) {
    System.out.println(String.format("📋 Step %d: %s", stepNumber, description));
  }

  /** 테스트 성공 로깅을 위한 유틸리티 메서드 */
  protected void logSuccess(String message) {
    System.out.println("✅ " + message);
  }

  /** 테스트 실패 로깅을 위한 유틸리티 메서드 */
  protected void logError(String message) {
    System.out.println("❌ " + message);
  }

  /** 테스트 완료 로깅을 위한 유틸리티 메서드 */
  protected void logCompletion(String scenario) {
    System.out.println(String.format("🎉 %s 시나리오 완료!", scenario));
  }
}
