package site.icebang.integration.tests.auth;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.epages.restdocs.apispec.ResourceSnippetParameters;

import site.icebang.integration.setup.support.IntegrationTestSupport;

@Sql(
    value = "classpath:sql/01-insert-internal-users.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class AuthApiIntegrationTest extends IntegrationTestSupport {
  @Test
  @DisplayName("사용자 로그인 성공")
  void login_success() throws Exception {
    // given
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("email", "admin@icebang.site");
    loginRequest.put("password", "qwer1234!A");

    // MockMvc로 REST Docs + OpenAPI 생성
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/auth/login"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/")
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data").isEmpty())
        .andDo(
            document(
                "auth-login",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Authentication")
                        .summary("사용자 로그인")
                        .description("이메일과 비밀번호로 사용자 인증을 수행합니다")
                        .requestFields(
                            fieldWithPath("email")
                                .type(JsonFieldType.STRING)
                                .description("사용자 이메일 주소"),
                            fieldWithPath("password")
                                .type(JsonFieldType.STRING)
                                .description("사용자 비밀번호"))
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data")
                                .type(JsonFieldType.NULL)
                                .description("응답 데이터 (로그인 성공 시 null)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }
}
