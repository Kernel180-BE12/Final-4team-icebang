package site.icebang.integration.tests.workflow;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.epages.restdocs.apispec.ResourceSnippetParameters;

import site.icebang.integration.setup.support.IntegrationTestSupport;

@Sql(
    value = {
      "classpath:sql/data/01-insert-internal-users.sql",
      "classpath:sql/data/06-insert-execution-log-h2.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class ExecutionLogApiIntegrationTest extends IntegrationTestSupport {

  @Test
  @DisplayName("실행 로그 조회 성공 - 전체 조회")
  @WithUserDetails("admin@icebang.site")
  void getTaskExecutionLog_success() throws Exception {
    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/workflow-runs/logs"))
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(36))
        // 첫 번째 로그 검증
        .andExpect(jsonPath("$.data[0].logLevel").exists())
        .andExpect(jsonPath("$.data[0].logMessage").exists())
        .andExpect(jsonPath("$.data[0].executedAt").exists())
        .andDo(
            document(
                "execution-log-all",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Workflow History")
                        .summary("실행 로그 전체 조회")
                        .description("워크플로우 실행 로그를 상세 정보와 함께 조회합니다")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data").type(JsonFieldType.ARRAY).description("실행 로그 목록"),
                            fieldWithPath("data[].logLevel")
                                .type(JsonFieldType.STRING)
                                .description("로그 레벨 (INFO, ERROR, WARN, DEBUG)"),
                            fieldWithPath("data[].logMessage")
                                .type(JsonFieldType.STRING)
                                .description("로그 메시지"),
                            fieldWithPath("data[].executedAt")
                                .type(JsonFieldType.STRING)
                                .description("실행 시간 (UTC ISO-8601)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("실행 로그 조회 성공 - traceId 필터링")
  @WithUserDetails("admin@icebang.site")
  void getTaskExecutionLog_withTraceId_success() throws Exception {
    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/workflow-runs/logs"))
                .param("traceId", "68d60b8a2f4cd59a880cf71f189b4ca5")
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(36))
        .andDo(
            document(
                "execution-log-by-trace-id",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Workflow History")
                        .summary("실행 로그 조회 - traceId 필터")
                        .description("특정 traceId로 워크플로우 실행 로그를 필터링하여 조회합니다")
                        .queryParameters(
                            parameterWithName("traceId").description("추적 ID").optional())
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data").type(JsonFieldType.ARRAY).description("실행 로그 목록"),
                            fieldWithPath("data[].logLevel")
                                .type(JsonFieldType.STRING)
                                .description("로그 레벨 (INFO, ERROR, WARN, DEBUG)"),
                            fieldWithPath("data[].logMessage")
                                .type(JsonFieldType.STRING)
                                .description("로그 메시지"),
                            fieldWithPath("data[].executedAt")
                                .type(JsonFieldType.STRING)
                                .description("실행 시간 (UTC ISO-8601)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("실행 로그 조회 성공 - executionType 필터링")
  @WithUserDetails("admin@icebang.site")
  void getTaskExecutionLog_withExecutionType_success() throws Exception {
    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/workflow-runs/logs"))
                .param("executionType", "TASK")
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(27)) // TASK 타입 로그만
        .andDo(
            document(
                "execution-log-by-execution-type",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Workflow History")
                        .summary("실행 로그 조회 - executionType 필터")
                        .description("특정 executionType으로 워크플로우 실행 로그를 필터링하여 조회합니다")
                        .queryParameters(
                            parameterWithName("executionType")
                                .description("실행 타입 (WORKFLOW, JOB, TASK)")
                                .optional())
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data").type(JsonFieldType.ARRAY).description("실행 로그 목록"),
                            fieldWithPath("data[].logLevel")
                                .type(JsonFieldType.STRING)
                                .description("로그 레벨 (INFO, ERROR, WARN, DEBUG)"),
                            fieldWithPath("data[].logMessage")
                                .type(JsonFieldType.STRING)
                                .description("로그 메시지"),
                            fieldWithPath("data[].executedAt")
                                .type(JsonFieldType.STRING)
                                .description("실행 시간 (UTC ISO-8601)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("실행 로그 조회 실패 - 잘못된 executionType")
  @WithUserDetails("admin@icebang.site")
  void getTaskExecutionLog_withInvalidExecutionType_fail() throws Exception {
    // when & then
    mockMvc
        .perform(
            get("/v0/workflow-runs/logs")
                .param("executionType", "INVALID_TYPE")
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }
}
