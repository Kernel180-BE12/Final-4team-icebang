package site.icebang.e2e.scenario;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import site.icebang.e2e.setup.annotation.E2eTest;
import site.icebang.e2e.setup.support.E2eTestSupport;

@Sql(
    value = "classpath:sql/01-insert-internal-users.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("사용자 로그아웃 플로우 E2E 테스트")
@E2eTest
class UserLogoutFlowE2eTest extends E2eTestSupport {

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("정상 로그아웃 전체 플로우 - TDD REd 단계")
  void completeUserRegistrationFlow_shouldFailBecauseApiNotImplemented() throws Exception {
    logStep(1, "관리자 로그인 (최우선)");

    // 1. 관리자 로그인으로 인증 상태 확립
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

    logSuccess("관리자 로그인 성공 - 인증 상태 확립 완료");

    logStep(2, "로그인 상태에서 보호된 리소스 접근 확인");

    // 로그인 응답에서 세션 쿠키 추출
    String sessionCookie = null;
    java.util.List<String> cookies = loginResponse.getHeaders().get("Set-Cookie");
    if (cookies != null) {
      for (String cookie : cookies) {
        if (cookie.startsWith("JSESSIONID")) {
          sessionCookie = cookie.split(";")[0]; // JSESSIONID=XXX 부분만 추출
          break;
        }
      }
    }

    // 2. 로그인된 상태에서 본인 프로필 조회로 인증 상태 확인
    // /v0/users/me는 인증된 사용자만 접근 가능한 일반적인 API
    HttpHeaders authenticatedHeaders = new HttpHeaders();
    if (sessionCookie != null) {
      authenticatedHeaders.set("Cookie", sessionCookie);
    }

    HttpEntity<Void> authenticatedEntity = new HttpEntity<>(authenticatedHeaders);
    ResponseEntity<Map> beforeLogoutResponse =
        restTemplate.exchange(
            getV0ApiUrl("/users/me"), HttpMethod.GET, authenticatedEntity, Map.class);

    assertThat(beforeLogoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat((Boolean) beforeLogoutResponse.getBody().get("success")).isTrue();
    assertThat(beforeLogoutResponse.getBody().get("data")).isNotNull();

    logSuccess("인증된 상태에서 본인 프로필 조회 성공");

    // 3. 로그아웃 API 호출
    HttpHeaders logoutHeaders = new HttpHeaders();
    logoutHeaders.setContentType(MediaType.APPLICATION_JSON);
    logoutHeaders.set("Origin", "https://admin.icebang.site");
    logoutHeaders.set("Referer", "https://admin.icebang.site/");

    // 로그아웃 요청에도 세션 쿠키 포함
    if (sessionCookie != null) {
      logoutHeaders.set("Cookie", sessionCookie);
    }

    HttpEntity<Map<String, Object>> logoutEntity = new HttpEntity<>(new HashMap<>(), logoutHeaders);

    try {
      ResponseEntity<Map> logoutResponse =
          restTemplate.postForEntity(getV0ApiUrl("/auth/logout"), logoutEntity, Map.class);
      logStep(4, "로그아웃 응답 검증 (API구현 돼있으면)");

      logSuccess("로그아웃 API 호출 성공");

      logStep(5, "로그아웃 후 인증 무효화 확인");

      // 5. 로그아웃 후 동일한 프로필 API 접근 시 인증 실패 확인
      HttpEntity<Void> afterLogoutEntity = new HttpEntity<>(authenticatedHeaders);
      ResponseEntity<Map> afterLogoutResponse =
          restTemplate.exchange(
              getV0ApiUrl("/users/me"), HttpMethod.GET, afterLogoutEntity, Map.class);

      // 핵심 검증: 로그아웃 후에는 인증 실패로 401 또는 403 응답이어야 함
      assertThat(afterLogoutResponse.getStatusCode())
          .withFailMessage(
              "로그아웃 후 프로필 접근이 차단되어야 합니다. 현재 상태코드: %s", afterLogoutResponse.getStatusCode())
          .isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
      logSuccess("로그아웃 후 프로필 접근 차단 확인 - 인증 무효화 성공");

      logCompletion("일반 사용자 로그아웃 플로우");

    } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
      logError("예상된 실패: 로그아웃 API가 구현되지 않음 (404 Not Found");
      logError("에러 메시지 : " + ex.getMessage());

      fail(
          "로그아웃 API (/v0/auth/logout)가 구현되지 않았습니다. "
              + "다음 단계에서 API를 구현해야 합니다. 에러: "
              + ex.getMessage());
    } catch (Exception ex) {
      logError("예상치 못한 오류 발생: " + ex.getClass().getSimpleName());
      logError("에러 메시지: " + ex.getMessage());

      // 기타 예상치 못한 에러도 기록
      fail("로그아웃 API 호출 중 예상치 못한 오류 발생: " + ex.getMessage());
    }
  }

  /** 일반 사용자 로그인을 수행하는 헬퍼 메서드 관리자가 아닌 콘텐츠팀장으로 로그인 */
  private void performRegularUserLogin() {
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("email", "viral.jung@icebang.site");
    loginRequest.put("password", "qwer1234!A"); // 실제 비밀번호 확인 필요

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Origin", "https://admin.icebang.site");
    headers.set("Referer", "https://admin.icebang.site/");

    HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

    ResponseEntity<Map> response =
        restTemplate.postForEntity(getV0ApiUrl("/auth/login"), entity, Map.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      logError("일반 사용자 로그인 실패: " + response.getStatusCode());
      throw new RuntimeException("Regular user login failed for logout test");
    }

    logSuccess("일반 사용자 로그인 완료 (로그아웃 테스트용)");
  }
}
