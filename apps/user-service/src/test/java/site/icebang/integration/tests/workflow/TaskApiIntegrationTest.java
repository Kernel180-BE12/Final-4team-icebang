package site.icebang.integration.tests.workflow;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.epages.restdocs.apispec.ResourceSnippetParameters;

import site.icebang.integration.setup.support.IntegrationTestSupport;

@Sql(
    value = {
      "classpath:sql/data/00-truncate.sql",
      "classpath:sql/data/01-insert-internal-users.sql",
      "classpath:sql/data/03-insert-workflow-h2.sql" // Task 테스트 데이터 포함 (10개 Task)
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
@DisplayName("Task API 통합 테스트")
public class TaskApiIntegrationTest extends IntegrationTestSupport {

  @Test
  @DisplayName("Task 생성 성공")
  @WithUserDetails("admin@icebang.site")
  void createTask_success() throws Exception {
    // given
    String taskRequestJson =
        """
                {
                  "name": "통합테스트 Task",
                  "type": "FastAPI",
                  "parameters": {
                    "endpoint": "/test/api",
                    "method": "POST"
                  }
                }
                """;

    // when & then
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/tasks"))
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskRequestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").exists())
        .andExpect(jsonPath("$.data.name").value("통합테스트 Task"))
        .andExpect(jsonPath("$.data.type").value("FastAPI"))
        .andExpect(jsonPath("$.data.parameters").exists())
        .andExpect(jsonPath("$.data.createdAt").exists())
        .andExpect(jsonPath("$.data.updatedAt").exists())
        .andDo(
            document(
                "task-create",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Task")
                        .summary("Task 생성")
                        .description("새로운 Task를 생성합니다")
                        .requestFields(
                            fieldWithPath("name")
                                .type(JsonFieldType.STRING)
                                .description("Task 이름 (필수)"),
                            fieldWithPath("type")
                                .type(JsonFieldType.STRING)
                                .description("Task 타입 (예: FastAPI, Shell 등)"),
                            fieldWithPath("parameters")
                                .type(JsonFieldType.OBJECT)
                                .description("Task 실행에 필요한 파라미터 (JSON 형태)")
                                .optional())
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data")
                                .type(JsonFieldType.OBJECT)
                                .description("생성된 Task 정보"),
                            fieldWithPath("data.id")
                                .type(JsonFieldType.NUMBER)
                                .description("Task ID"),
                            fieldWithPath("data.name")
                                .type(JsonFieldType.STRING)
                                .description("Task 이름"),
                            fieldWithPath("data.type")
                                .type(JsonFieldType.STRING)
                                .description("Task 타입"),
                            fieldWithPath("data.parameters")
                                .type(JsonFieldType.OBJECT)
                                .description("Task 파라미터")
                                .optional(),
                            fieldWithPath("data.executionOrder")
                                .type(JsonFieldType.NUMBER)
                                .description("실행 순서")
                                .optional(),
                            fieldWithPath("data.settings")
                                .type(JsonFieldType.OBJECT)
                                .description("Task 설정")
                                .optional(),
                            fieldWithPath("data.createdAt")
                                .type(JsonFieldType.STRING)
                                .description("생성 시간"),
                            fieldWithPath("data.updatedAt")
                                .type(JsonFieldType.STRING)
                                .description("수정 시간"))
                        .build())));
  }

  @Test
  @DisplayName("Task 조회 성공")
  @WithUserDetails("admin@icebang.site")
  void getTask_success() throws Exception {
    // given - 03-insert-workflow-h2.sql에서 생성된 Task ID 1 사용 (키워드 검색 태스크)
    Long taskId = 1L;

    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/tasks/{id}"), taskId)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(taskId.intValue()))
        .andExpect(jsonPath("$.data.name").value("키워드 검색 태스크"))
        .andExpect(jsonPath("$.data.type").value("FastAPI"))
        .andExpect(jsonPath("$.data.parameters").exists())
        .andExpect(jsonPath("$.data.createdAt").exists())
        .andExpect(jsonPath("$.data.updatedAt").exists())
        .andDo(
            document(
                "task-get",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Task")
                        .summary("Task 조회")
                        .description("Task ID로 Task 정보를 조회합니다")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data").type(JsonFieldType.OBJECT).description("Task 정보"),
                            fieldWithPath("data.id")
                                .type(JsonFieldType.NUMBER)
                                .description("Task ID"),
                            fieldWithPath("data.name")
                                .type(JsonFieldType.STRING)
                                .description("Task 이름"),
                            fieldWithPath("data.type")
                                .type(JsonFieldType.STRING)
                                .description("Task 타입"),
                            fieldWithPath("data.parameters")
                                .type(JsonFieldType.OBJECT)
                                .description("Task 파라미터")
                                .optional(),
                            fieldWithPath("data.executionOrder")
                                .type(JsonFieldType.NUMBER)
                                .description("실행 순서")
                                .optional(),
                            fieldWithPath("data.settings")
                                .type(JsonFieldType.OBJECT)
                                .description("Task 설정")
                                .optional(),
                            fieldWithPath("data.createdAt")
                                .type(JsonFieldType.STRING)
                                .description("생성 시간"),
                            fieldWithPath("data.updatedAt")
                                .type(JsonFieldType.STRING)
                                .description("수정 시간"))
                        .build())));
  }

  @Test
  @DisplayName("Task 이름 없이 생성 시 실패")
  @WithUserDetails("admin@icebang.site")
  void createTask_withoutName_shouldFail() throws Exception {
    // given
    String invalidTaskRequestJson =
        """
                {
                  "type": "FastAPI",
                  "parameters": {
                    "endpoint": "/test/api"
                  }
                }
                """;

    // when & then
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/tasks"))
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidTaskRequestJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("빈 문자열 이름으로 Task 생성 시 실패")
  @WithUserDetails("admin@icebang.site")
  void createTask_withBlankName_shouldFail() throws Exception {
    // given
    String blankNameTaskRequestJson =
        """
                {
                  "name": "   ",
                  "type": "FastAPI",
                  "parameters": {
                    "endpoint": "/test/api"
                  }
                }
                """;

    // when & then
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/tasks"))
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(blankNameTaskRequestJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("존재하지 않는 Task 조회 시 404 반환")
  @WithUserDetails("admin@icebang.site")
  void getTask_withNonExistentId_shouldReturn404() throws Exception {
    // given
    Long nonExistentId = 99999L;

    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/tasks/{id}"), nonExistentId)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("복잡한 JSON 파라미터로 Task 생성")
  @WithUserDetails("admin@icebang.site")
  void createTask_withComplexJsonParameters() throws Exception {
    // given
    String complexTaskRequestJson =
        """
                {
                  "name": "복잡한 파라미터 Task",
                  "type": "FastAPI",
                  "parameters": {
                    "endpoint": "/products/similarity",
                    "method": "POST",
                    "body": {
                      "keyword": "String",
                      "matched_products": "List",
                      "search_results": "List"
                    },
                    "timeout": 30,
                    "retries": 3
                  }
                }
                """;

    // when & then
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/tasks"))
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(complexTaskRequestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("복잡한 파라미터 Task"))
        .andExpect(jsonPath("$.data.parameters.endpoint").value("/products/similarity"))
        .andExpect(jsonPath("$.data.parameters.body.keyword").value("String"));
  }

  @Test
  @DisplayName("새로 추가된 OCR Task 조회 (Task ID 8)")
  @WithUserDetails("admin@icebang.site")
  void getOcrTask_success() throws Exception {
    // given - 새로 추가된 이미지 OCR 태스크 (ID: 8)
    Long ocrTaskId = 8L;

    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/tasks/{id}"), ocrTaskId)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(ocrTaskId.intValue()))
        .andExpect(jsonPath("$.data.name").value("이미지 OCR 태스크"))
        .andExpect(jsonPath("$.data.type").value("FastAPI"))
        .andExpect(jsonPath("$.data.parameters").exists())
        .andExpect(jsonPath("$.data.parameters.endpoint").value("/blogs/ocr/extract"));
  }

  @Test
  @DisplayName("업데이트된 블로그 발행 Task 조회 (Task ID 10)")
  @WithUserDetails("admin@icebang.site")
  void getBlogPublishTask_success() throws Exception {
    // given - 업데이트된 블로그 발행 태스크 (ID: 10, 기존 9에서 변경)
    Long publishTaskId = 10L;

    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/tasks/{id}"), publishTaskId)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(publishTaskId.intValue()))
        .andExpect(jsonPath("$.data.name").value("블로그 발행 태스크"))
        .andExpect(jsonPath("$.data.type").value("FastAPI"))
        .andExpect(jsonPath("$.data.parameters.endpoint").value("/blogs/publish"));
  }
}
