package site.icebang.e2e.scenario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import site.icebang.e2e.setup.annotation.E2eTest;
import site.icebang.e2e.setup.support.E2eTestSupport;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Sql(
        value = {"classpath:sql/00-truncate.sql", "classpath:sql/01-insert-internal-users.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("워크플로우 생성 플로우 E2E 테스트")
@E2eTest
class WorkflowCreateE2eTest extends E2eTestSupport {

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("사용자가 새 워크플로우를 생성하는 전체 플로우")
  void completeWorkflowCreateFloㅈ() throws Exception {
    logStep(1, "사용자 로그인");

    // 1. 사용자 로그인
    performUserLogin();

    logStep(2, "네이버 블로그 워크플로우 생성");

    // 2. 네이버 블로그 워크플로우 생성

    // 3. 티스토리 블로그 워크플로우 생성 (블로그명 필수)

    // 4. 포스팅 없는 검색 전용 워크플로우 (추후 예정)

  }

  /** 사용자 로그인을 수행하는 헬퍼 메서드 */
  private void performUserLogin() {
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("email", "admin@icebang.site");
    loginRequest.put("password", "qwer1234!A");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Origin", "https://admin.icebang.site");
    headers.set("Referer", "https://admin.icebang.site/");

    HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<Map> response =
            restTemplate.postForEntity(getV0ApiUrl("/auth/login"), entity, Map.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      logError("사용자 로그인 실패: " + response.getStatusCode());
      throw new RuntimeException("User login failed");
    }

    logSuccess("사용자 로그인 완료");
  }
}
