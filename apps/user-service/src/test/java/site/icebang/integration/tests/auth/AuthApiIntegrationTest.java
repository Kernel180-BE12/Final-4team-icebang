package site.icebang.integration.tests.auth;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
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
  void loginSuccess() throws Exception {
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

  @Test
  @DisplayName("사용자 등록 성공")
  @WithUserDetails("admin@icebang.site")
  void registerSuccess() throws Exception {
    // given
    Map<String, Object> registerRequest = new HashMap<>();
    registerRequest.put("name", "홍길동");
    registerRequest.put("email", "hong@icebang.site");
    registerRequest.put("orgId", 1);
    registerRequest.put("deptId", 1);
    registerRequest.put("positionId", 1);
    registerRequest.put("roleIds", Arrays.asList(1, 2));

    // MockMvc로 REST Docs + OpenAPI 생성
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/auth/register"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/")
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("CREATED"))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data").isEmpty())
        .andDo(
            document(
                "auth-register",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Authentication")
                        .summary("사용자 회원가입")
                        .description("새로운 사용자 계정을 생성합니다")
                        .requestFields(
                            fieldWithPath("name").type(JsonFieldType.STRING).description("사용자명"),
                            fieldWithPath("email")
                                .type(JsonFieldType.STRING)
                                .description("사용자 이메일 주소"),
                            fieldWithPath("orgId").type(JsonFieldType.NUMBER).description("조직 ID"),
                            fieldWithPath("deptId").type(JsonFieldType.NUMBER).description("부서 ID"),
                            fieldWithPath("positionId")
                                .type(JsonFieldType.NUMBER)
                                .description("직책 ID"),
                            fieldWithPath("roleIds")
                                .type(JsonFieldType.ARRAY)
                                .description("역할 ID 목록"))
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data")
                                .type(JsonFieldType.NULL)
                                .description("응답 데이터 (회원가입 성공 시 null)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("사용자 등록 실패 - 이메일 양식 오류")
  @WithUserDetails("admin@icebang.site")
  void registerFailureWhenInvalidEmail() throws Exception {
    // given
    Map<String, Object> registerRequest = new HashMap<>();
    registerRequest.put("name", "홍길동");
    registerRequest.put("email", "invalid-email"); // 잘못된 이메일 형식
    registerRequest.put("orgId", 1);
    registerRequest.put("deptId", 1);
    registerRequest.put("positionId", 1);
    registerRequest.put("roleIds", Arrays.asList(1));

    // when & then
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/auth/register"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/")
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
  }

  @Test
  @DisplayName("사용자 등록 실패 - 필수 필드 누락")
  @WithUserDetails("admin@icebang.site")
  void registerFailureWhenMissingRequiredFields() throws Exception {
    // given - 필수 필드 누락
    Map<String, Object> registerRequest = new HashMap<>();
    registerRequest.put("email", "test@icebang.site");
    // name, orgId, deptId, positionId, roleIds 누락

    // when & then
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/auth/register"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/")
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
  }

  @Disabled
  @DisplayName("사용자 세션 체크")
  void checkSession() throws Exception {}

  @Disabled
  @DisplayName("사용자 권한 요청")
  void userPermission() throws Exception {}

  @Test
  @DisplayName("사용자 로그아웃 성공")
  void logoutSuccess() throws Exception {
    // given - 먼저 로그인
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("email", "admin@icebang.site");
    loginRequest.put("password", "qwer1234!A");

    MockHttpSession session = new MockHttpSession();

    // 로그인 먼저 수행
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/auth/login"))
                .contentType(MediaType.APPLICATION_JSON)
                .session(session)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk());

    // when & then - 로그아웃 수행
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/auth/logout"))
                .contentType(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data").isEmpty())
        .andDo(
            document(
                "auth-logout",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Authentication")
                        .summary("사용자 로그아웃")
                        .description("현재 인증된 사용자의 세션을 무효화합니다")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data")
                                .type(JsonFieldType.NULL)
                                .description("응답 데이터 (로그아웃 성공 시 null)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }
}
