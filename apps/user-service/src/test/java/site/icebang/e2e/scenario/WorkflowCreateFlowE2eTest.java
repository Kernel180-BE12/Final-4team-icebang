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
class WorkflowCreateFlowE2eTest extends E2eTestSupport {

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("사용자가 새 워크플로우를 생성하는 전체 플로우")
  void completeWorkflowCreateFlow() throws Exception {
    logStep(1, "사용자 로그인");

    // 1. 로그인 (세션에 userId 저장)
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("email", "admin@icebang.site");
    loginRequest.put("password", "qwer1234!A");

    HttpHeaders loginHeaders = new HttpHeaders();
    loginHeaders.setContentType(MediaType.APPLICATION_JSON);
    loginHeaders.set("Origin", "https://admin.icebang.site");
    loginHeaders.set("Referer", "https://admin.icebang.site/");

    HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, loginHeaders);

    ResponseEntity<Map> loginResponse =
            restTemplate.postForEntity(getV0ApiUrl("/auth/login"), loginEntity, Map.class);

    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat((Boolean) loginResponse.getBody().get("success")).isTrue();

    logSuccess("사용자 로그인 성공 - 세션 쿠키 자동 저장됨");
    logDebug("현재 세션 쿠키: " + getSessionCookies());

    logStep(2, "네이버 블로그 워크플로우 생성");

    // 2. 네이버 블로그 워크플로우 생성
    Map<String, Object> naverBlogWorkflow = new HashMap<>();
    naverBlogWorkflow.put("name", "상품 분석 및 네이버 블로그 자동 발행");
    naverBlogWorkflow.put("description", "키워드 검색부터 상품 분석 후 네이버 블로그 발행까지의 자동화 프로세스");
    naverBlogWorkflow.put("search_platform", "naver");
    naverBlogWorkflow.put("posting_platform", "naver_blog");
    naverBlogWorkflow.put("posting_account_id", "test_naver_blog");
    naverBlogWorkflow.put("posting_account_password", "naver_password123");
    naverBlogWorkflow.put("is_enabled", true);

    HttpHeaders workflowHeaders = new HttpHeaders();
    workflowHeaders.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> naverEntity =
            new HttpEntity<>(naverBlogWorkflow, workflowHeaders);

    ResponseEntity<Map> naverResponse =
            restTemplate.postForEntity(getV0ApiUrl("/workflows"), naverEntity, Map.class);

    assertThat(naverResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat((Boolean) naverResponse.getBody().get("success")).isTrue();

    logSuccess("네이버 블로그 워크플로우 생성 성공");

    logStep(3, "티스토리 블로그 워크플로우 생성 (블로그명 포함)");

    // 3. 티스토리 블로그 워크플로우 생성 (블로그명 필수)
    Map<String, Object> tstoryWorkflow = new HashMap<>();
    tstoryWorkflow.put("name", "티스토리 자동 발행 워크플로우");
    tstoryWorkflow.put("description", "티스토리 블로그 자동 포스팅");
    tstoryWorkflow.put("search_platform", "naver");
    tstoryWorkflow.put("posting_platform", "tstory_blog");
    tstoryWorkflow.put("posting_account_id", "test_tstory");
    tstoryWorkflow.put("posting_account_password", "tstory_password123");
    tstoryWorkflow.put("blog_name", "my-tech-blog");  // 티스토리는 블로그명 필수
    tstoryWorkflow.put("is_enabled", true);

    HttpEntity<Map<String, Object>> tstoryEntity =
            new HttpEntity<>(tstoryWorkflow, workflowHeaders);

    ResponseEntity<Map> tstoryResponse =
            restTemplate.postForEntity(getV0ApiUrl("/workflows"), tstoryEntity, Map.class);

    assertThat(tstoryResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat((Boolean) tstoryResponse.getBody().get("success")).isTrue();

    logSuccess("티스토리 워크플로우 생성 성공");

    logStep(4, "검색만 하는 워크플로우 생성 (포스팅 없음)");

    // 4. 포스팅 없는 검색 전용 워크플로우 (추후 예정)
    Map<String, Object> searchOnlyWorkflow = new HashMap<>();
    searchOnlyWorkflow.put("name", "검색 전용 워크플로우");
    searchOnlyWorkflow.put("description", "상품 검색 및 분석만 수행");
    searchOnlyWorkflow.put("search_platform", "naver");
    searchOnlyWorkflow.put("is_enabled", true);
    // posting_platform, posting_account_id, posting_account_password는 선택사항

    HttpEntity<Map<String, Object>> searchOnlyEntity =
            new HttpEntity<>(searchOnlyWorkflow, workflowHeaders);

    ResponseEntity<Map> searchOnlyResponse =
            restTemplate.postForEntity(getV0ApiUrl("/workflows"), searchOnlyEntity, Map.class);

    assertThat(searchOnlyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat((Boolean) searchOnlyResponse.getBody().get("success")).isTrue();

    logSuccess("검색 전용 워크플로우 생성 성공");

    logCompletion("워크플로우 생성 플로우 완료");
  }

  @Test
  @DisplayName("중복된 이름으로 워크플로우 생성 시도 시 실패")
  void createWorkflow_withDuplicateName_shouldFail() {
    // 선행 조건: 로그인
    performUserLogin();

    logStep(1, "첫 번째 워크플로우 생성");

    // 첫 번째 워크플로우 생성
    Map<String, Object> firstWorkflow = new HashMap<>();
    firstWorkflow.put("name", "중복테스트워크플로우");
    firstWorkflow.put("search_platform", "naver");
    firstWorkflow.put("is_enabled", true);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> firstEntity = new HttpEntity<>(firstWorkflow, headers);

    ResponseEntity<Map> firstResponse =
            restTemplate.postForEntity(getV0ApiUrl("/workflows"), firstEntity, Map.class);

    assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    logSuccess("첫 번째 워크플로우 생성 성공");

    logStep(2, "동일한 이름으로 두 번째 워크플로우 생성 시도");

    // 동일한 이름으로 다시 생성 시도
    Map<String, Object> duplicateWorkflow = new HashMap<>();
    duplicateWorkflow.put("name", "중복테스트워크플로우");  // 동일한 이름
    duplicateWorkflow.put("search_platform", "naver_store");
    duplicateWorkflow.put("is_enabled", true);

    HttpEntity<Map<String, Object>> duplicateEntity =
            new HttpEntity<>(duplicateWorkflow, headers);

    ResponseEntity<Map> duplicateResponse =
            restTemplate.postForEntity(getV0ApiUrl("/workflows"), duplicateEntity, Map.class);

    // 중복 이름 처리 확인 (400 또는 409 예상)
    assertThat(duplicateResponse.getStatusCode())
            .isIn(HttpStatus.BAD_REQUEST, HttpStatus.CONFLICT, HttpStatus.INTERNAL_SERVER_ERROR);

    logSuccess("중복 이름 워크플로우 생성 차단 확인");
  }

  @Test
  @DisplayName("필수 필드 누락 시 워크플로우 생성 실패")
  void createWorkflow_withMissingRequiredFields_shouldFail() {
    // 선행 조건: 로그인
    performUserLogin();

    logStep(1, "워크플로우 이름 없이 생성 시도");

    // 이름 없는 요청
    Map<String, Object> noNameWorkflow = new HashMap<>();
    noNameWorkflow.put("search_platform", "naver");
    noNameWorkflow.put("is_enabled", true);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(noNameWorkflow, headers);

    ResponseEntity<Map> response =
            restTemplate.postForEntity(getV0ApiUrl("/workflows"), entity, Map.class);

    assertThat(response.getStatusCode())
            .isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNPROCESSABLE_ENTITY);

    logSuccess("필수 필드 검증 확인");
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
