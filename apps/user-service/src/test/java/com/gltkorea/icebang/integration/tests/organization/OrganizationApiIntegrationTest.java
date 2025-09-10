package com.gltkorea.icebang.integration.tests.organization;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.gltkorea.icebang.integration.setup.support.IntegrationTestSupport;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql(
    value = {
      "classpath:sql/01-insert-internal-users.sql",
      "classpath:sql/02-insert-external-users.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OrganizationApiIntegrationTest extends IntegrationTestSupport {

  @Test
  @DisplayName("조직 목록 조회 성공")
  void getOrganizations_success() throws Exception {
    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/organizations"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").exists())
        .andExpect(jsonPath("$.data[0].organizationName").exists())
        .andDo(
            document(
                "organizations-list",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Organization")
                        .summary("조직 목록 조회")
                        .description("시스템에 등록된 모든 조직의 목록을 조회합니다")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data[]").type(JsonFieldType.ARRAY).description("조직 목록"),
                            fieldWithPath("data[].id")
                                .type(JsonFieldType.NUMBER)
                                .description("조직 ID"),
                            fieldWithPath("data[].organizationName")
                                .type(JsonFieldType.STRING)
                                .description("조직명"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("조직 옵션 정보 조회 성공")
  void getOrganizationOptions_success() throws Exception {
    // given
    Long organizationId = 1L;

    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/organizations/{organizationId}/options"), organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data.departments").isArray())
        .andExpect(jsonPath("$.data.positions").isArray())
        .andExpect(jsonPath("$.data.roles").isArray())
        .andExpect(jsonPath("$.data.departments[0].id").exists())
        .andExpect(jsonPath("$.data.departments[0].name").exists())
        .andExpect(jsonPath("$.data.positions[0].id").exists())
        .andExpect(jsonPath("$.data.positions[0].title").exists())
        .andExpect(jsonPath("$.data.roles[0].id").exists())
        .andExpect(jsonPath("$.data.roles[0].name").exists())
        .andExpect(jsonPath("$.data.roles[0].description").exists())
        .andDo(
            document(
                "organizations-options",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Organization")
                        .summary("조직 옵션 정보 조회")
                        .description("특정 조직의 부서, 직급, 역할 옵션 정보를 조회합니다")
                        .pathParameters(parameterWithName("organizationId").description("조직 ID"))
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data").type(JsonFieldType.OBJECT).description("옵션 데이터"),
                            fieldWithPath("data.departments[]")
                                .type(JsonFieldType.ARRAY)
                                .description("부서 목록"),
                            fieldWithPath("data.departments[].id")
                                .type(JsonFieldType.NUMBER)
                                .description("부서 ID"),
                            fieldWithPath("data.departments[].name")
                                .type(JsonFieldType.STRING)
                                .description("부서명"),
                            fieldWithPath("data.positions[]")
                                .type(JsonFieldType.ARRAY)
                                .description("직급 목록"),
                            fieldWithPath("data.positions[].id")
                                .type(JsonFieldType.NUMBER)
                                .description("직급 ID"),
                            fieldWithPath("data.positions[].title")
                                .type(JsonFieldType.STRING)
                                .description("직급명"),
                            fieldWithPath("data.roles[]")
                                .type(JsonFieldType.ARRAY)
                                .description("역할 목록"),
                            fieldWithPath("data.roles[].id")
                                .type(JsonFieldType.NUMBER)
                                .description("역할 ID"),
                            fieldWithPath("data.roles[].name")
                                .type(JsonFieldType.STRING)
                                .description("역할명"),
                            fieldWithPath("data.roles[].description")
                                .type(JsonFieldType.STRING)
                                .description("역할 설명"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }
}
