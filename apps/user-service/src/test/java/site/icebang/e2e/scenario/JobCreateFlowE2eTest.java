package site.icebang.e2e.scenario;

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
        value = {
                "classpath:sql/data/00-truncate.sql",
                "classpath:sql/data/01-insert-internal-users.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("Job 생성 플로우 E2E 테스트")
@E2eTest
class JobCreateFlowE2eTest extends E2eTestSupport {

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("사용자가 새로운 Job을 생성하는 전체 플로우")
    void completeJobCreateFlow() throws Exception {
        logStep(1, "사용자 로그인");
        performUserLogin();

        logStep(2, "기본 Job 생성");

        // Job 생성 요청
        Map<String, Object> jobRequest = new HashMap<>();
        jobRequest.put("name", "테스트 Job");
        jobRequest.put("description", "E2E 테스트용 Job입니다");
        jobRequest.put("isEnabled", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jobRequest, headers);

        ResponseEntity<Map> createResponse =
                restTemplate.postForEntity(getV0ApiUrl("/jobs"), entity, Map.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat((Boolean) createResponse.getBody().get("success")).isTrue();

        Map<String, Object> createdJobData = (Map<String, Object>) createResponse.getBody().get("data");
        assertThat(createdJobData).isNotNull();
        assertThat(createdJobData.get("id")).isNotNull();
        assertThat(createdJobData.get("name")).isEqualTo("테스트 Job");

        logSuccess("Job 생성 성공");

        logStep(3, "생성된 Job 조회");

        Long jobId = ((Number) createdJobData.get("id")).longValue();

        ResponseEntity<Map> getResponse =
                restTemplate.getForEntity(getV0ApiUrl("/jobs/" + jobId), Map.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((Boolean) getResponse.getBody().get("success")).isTrue();

        Map<String, Object> retrievedJobData = (Map<String, Object>) getResponse.getBody().get("data");
        assertThat(retrievedJobData.get("id")).isEqualTo(jobId.intValue());
        assertThat(retrievedJobData.get("name")).isEqualTo("테스트 Job");
        assertThat(retrievedJobData.get("description")).isEqualTo("E2E 테스트용 Job입니다");

        logSuccess("생성된 Job 조회 성공");

        logCompletion("Job 생성 플로우 완료");
    }

    @Test
    @DisplayName("필수 필드 누락 시 Job 생성 실패")
    void createJob_withMissingRequiredFields_shouldFail() {
        // 선행 조건: 로그인
        performUserLogin();

        logStep(1, "Job 이름 없이 생성 시도");

        // 이름 없는 요청
        Map<String, Object> noNameJob = new HashMap<>();
        noNameJob.put("description", "이름이 없는 Job");
        noNameJob.put("isEnabled", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(noNameJob, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(getV0ApiUrl("/jobs"), entity, Map.class);

        assertThat(response.getStatusCode())
                .isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNPROCESSABLE_ENTITY);

        logSuccess("필수 필드 검증 확인");
    }

    @Test
    @DisplayName("존재하지 않는 Job 조회 시 404 응답")
    void getJob_withNonExistentId_shouldReturn404() {
        // 선행 조건: 로그인
        performUserLogin();

        logStep(1, "존재하지 않는 Job ID로 조회 시도");

        Long nonExistentId = 99999L;

        ResponseEntity<Map> response =
                restTemplate.getForEntity(getV0ApiUrl("/jobs/" + nonExistentId), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((Boolean) response.getBody().get("success")).isFalse();

        logSuccess("존재하지 않는 Job 조회 시 404 응답 확인");
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