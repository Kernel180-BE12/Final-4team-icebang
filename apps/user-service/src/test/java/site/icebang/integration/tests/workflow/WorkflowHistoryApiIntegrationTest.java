package site.icebang.integration.tests.workflow;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
      "classpath:sql/data/03-insert-workflow.sql",
      "classpath:sql/data/04-insert-workflow-history.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class WorkflowHistoryApiIntegrationTest extends IntegrationTestSupport {
  @Test
  @DisplayName("워크플로우 실행 상세 조회 성공")
  @WithUserDetails("admin@icebang.site")
  void getWorkflowRunDetail_success() throws Exception {
    // given
    Long runId = 1L;

    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/workflow-runs/{runId}"), runId)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("OK"))
        // traceId 확인
        .andExpect(jsonPath("$.data.traceId").value("3e3c832d-b51f-48ea-95f9-98f0ae6d3413"))
        // workflowRun 필드 확인
        .andExpect(jsonPath("$.data.workflowRun.id").value(1))
        .andExpect(jsonPath("$.data.workflowRun.workflowId").value(1))
        .andExpect(jsonPath("$.data.workflowRun.workflowName").value("상품 분석 및 블로그 자동 발행"))
        .andExpect(
            jsonPath("$.data.workflowRun.workflowDescription")
                .value("키워드 검색부터 상품 분석 후 블로그 발행까지의 자동화 프로세스"))
        .andExpect(jsonPath("$.data.workflowRun.runNumber").isEmpty())
        .andExpect(jsonPath("$.data.workflowRun.status").value("FAILED"))
        .andExpect(jsonPath("$.data.workflowRun.triggerType").isEmpty())
        .andExpect(jsonPath("$.data.workflowRun.startedAt").value("2025-09-22 18:18:43"))
        .andExpect(jsonPath("$.data.workflowRun.finishedAt").value("2025-09-22 18:18:44"))
        .andExpect(jsonPath("$.data.workflowRun.durationMs").value(1000))
        .andExpect(jsonPath("$.data.workflowRun.createdBy").isEmpty())
        .andExpect(jsonPath("$.data.workflowRun.createdAt").exists())
        // jobRuns 배열 확인
        .andExpect(jsonPath("$.data.jobRuns").isArray())
        .andExpect(jsonPath("$.data.jobRuns.length()").value(1))
        // jobRuns[0] 필드 확인
        .andExpect(jsonPath("$.data.jobRuns[0].id").value(1))
        .andExpect(jsonPath("$.data.jobRuns[0].workflowRunId").value(1))
        .andExpect(jsonPath("$.data.jobRuns[0].jobId").value(1))
        .andExpect(jsonPath("$.data.jobRuns[0].jobName").value("상품 분석"))
        .andExpect(jsonPath("$.data.jobRuns[0].jobDescription").value("키워드 검색, 상품 크롤링 및 유사도 분석 작업"))
        .andExpect(jsonPath("$.data.jobRuns[0].status").value("FAILED"))
        .andExpect(jsonPath("$.data.jobRuns[0].executionOrder").isEmpty())
        .andExpect(jsonPath("$.data.jobRuns[0].startedAt").value("2025-09-22 18:18:44"))
        .andExpect(jsonPath("$.data.jobRuns[0].finishedAt").value("2025-09-22 18:18:44"))
        .andExpect(jsonPath("$.data.jobRuns[0].durationMs").value(0))
        // taskRuns 배열 확인
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns").isArray())
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns.length()").value(1))
        // taskRuns[0] 필드 확인
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].id").value(1))
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].jobRunId").value(1))
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].taskId").value(1))
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].taskName").value("키워드 검색 태스크"))
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].taskDescription").isEmpty())
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].taskType").value("FastAPI"))
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].status").value("FAILED"))
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].executionOrder").isEmpty())
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].startedAt").value("2025-09-22 18:18:44"))
        .andExpect(
            jsonPath("$.data.jobRuns[0].taskRuns[0].finishedAt").value("2025-09-22 18:18:44"))
        .andExpect(jsonPath("$.data.jobRuns[0].taskRuns[0].durationMs").value(0))
        .andDo(
            document(
                "workflow-run-detail",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Workflow History")
                        .summary("워크플로우 실행 상세 조회")
                        .description("워크플로우 실행 ID로 상세 정보를 조회합니다")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                            fieldWithPath("data.traceId")
                                .type(JsonFieldType.STRING)
                                .description("워크플로우 실행 추적 ID"),
                            fieldWithPath("data.workflowRun")
                                .type(JsonFieldType.OBJECT)
                                .description("워크플로우 실행 정보"),
                            fieldWithPath("data.workflowRun.id")
                                .type(JsonFieldType.NUMBER)
                                .description("워크플로우 실행 ID"),
                            fieldWithPath("data.workflowRun.workflowId")
                                .type(JsonFieldType.NUMBER)
                                .description("워크플로우 설계 ID"),
                            fieldWithPath("data.workflowRun.workflowName")
                                .type(JsonFieldType.STRING)
                                .description("워크플로우 이름"),
                            fieldWithPath("data.workflowRun.workflowDescription")
                                .type(JsonFieldType.STRING)
                                .description("워크플로우 설명"),
                            fieldWithPath("data.workflowRun.runNumber")
                                .type(JsonFieldType.NULL)
                                .description("실행 번호"),
                            fieldWithPath("data.workflowRun.status")
                                .type(JsonFieldType.STRING)
                                .description("실행 상태"),
                            fieldWithPath("data.workflowRun.triggerType")
                                .type(JsonFieldType.NULL)
                                .description("트리거 유형"),
                            fieldWithPath("data.workflowRun.startedAt")
                                .type(JsonFieldType.STRING)
                                .description("시작 시간"),
                            fieldWithPath("data.workflowRun.finishedAt")
                                .type(JsonFieldType.STRING)
                                .description("완료 시간"),
                            fieldWithPath("data.workflowRun.durationMs")
                                .type(JsonFieldType.NUMBER)
                                .description("실행 시간(ms)"),
                            fieldWithPath("data.workflowRun.createdBy")
                                .type(JsonFieldType.NULL)
                                .description("생성자 ID"),
                            fieldWithPath("data.workflowRun.createdAt")
                                .type(JsonFieldType.STRING)
                                .description("생성 시간"),
                            fieldWithPath("data.jobRuns")
                                .type(JsonFieldType.ARRAY)
                                .description("Job 실행 목록"),
                            fieldWithPath("data.jobRuns[].id")
                                .type(JsonFieldType.NUMBER)
                                .description("Job 실행 ID"),
                            fieldWithPath("data.jobRuns[].workflowRunId")
                                .type(JsonFieldType.NUMBER)
                                .description("워크플로우 실행 ID"),
                            fieldWithPath("data.jobRuns[].jobId")
                                .type(JsonFieldType.NUMBER)
                                .description("Job 설계 ID"),
                            fieldWithPath("data.jobRuns[].jobName")
                                .type(JsonFieldType.STRING)
                                .description("Job 이름"),
                            fieldWithPath("data.jobRuns[].jobDescription")
                                .type(JsonFieldType.STRING)
                                .description("Job 설명"),
                            fieldWithPath("data.jobRuns[].status")
                                .type(JsonFieldType.STRING)
                                .description("Job 실행 상태"),
                            fieldWithPath("data.jobRuns[].executionOrder")
                                .type(JsonFieldType.NULL)
                                .description("실행 순서"),
                            fieldWithPath("data.jobRuns[].startedAt")
                                .type(JsonFieldType.STRING)
                                .description("Job 시작 시간"),
                            fieldWithPath("data.jobRuns[].finishedAt")
                                .type(JsonFieldType.STRING)
                                .description("Job 완료 시간"),
                            fieldWithPath("data.jobRuns[].durationMs")
                                .type(JsonFieldType.NUMBER)
                                .description("Job 실행 시간(ms)"),
                            fieldWithPath("data.jobRuns[].taskRuns")
                                .type(JsonFieldType.ARRAY)
                                .description("Task 실행 목록"),
                            fieldWithPath("data.jobRuns[].taskRuns[].id")
                                .type(JsonFieldType.NUMBER)
                                .description("Task 실행 ID"),
                            fieldWithPath("data.jobRuns[].taskRuns[].jobRunId")
                                .type(JsonFieldType.NUMBER)
                                .description("Job 실행 ID"),
                            fieldWithPath("data.jobRuns[].taskRuns[].taskId")
                                .type(JsonFieldType.NUMBER)
                                .description("Task 설계 ID"),
                            fieldWithPath("data.jobRuns[].taskRuns[].taskName")
                                .type(JsonFieldType.STRING)
                                .description("Task 이름"),
                            fieldWithPath("data.jobRuns[].taskRuns[].taskDescription")
                                .type(JsonFieldType.NULL)
                                .description("Task 설명"),
                            fieldWithPath("data.jobRuns[].taskRuns[].taskType")
                                .type(JsonFieldType.STRING)
                                .description("Task 유형"),
                            fieldWithPath("data.jobRuns[].taskRuns[].status")
                                .type(JsonFieldType.STRING)
                                .description("Task 실행 상태"),
                            fieldWithPath("data.jobRuns[].taskRuns[].executionOrder")
                                .type(JsonFieldType.NULL)
                                .description("Task 실행 순서"),
                            fieldWithPath("data.jobRuns[].taskRuns[].startedAt")
                                .type(JsonFieldType.STRING)
                                .description("Task 시작 시간"),
                            fieldWithPath("data.jobRuns[].taskRuns[].finishedAt")
                                .type(JsonFieldType.STRING)
                                .description("Task 완료 시간"),
                            fieldWithPath("data.jobRuns[].taskRuns[].durationMs")
                                .type(JsonFieldType.NUMBER)
                                .description("Task 실행 시간(ms)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }
}
