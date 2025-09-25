package site.icebang.integration.tests.auth;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpSession;
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

  @Test
  @DisplayName("사용자 로그아웃 성공")
  void logout_success() throws Exception {
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

  @Test
  @DisplayName("세션 확인 - 인증된 사용자")
  void checkSession_authenticated_success() throws Exception {
    // given - 먼저 로그인하여 세션 생성
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

    // when & then - 세션 확인 수행
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/auth/check-session"))
                .session(session)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data").value(true))
        .andDo(
            document(
                "auth-check-session-authenticated",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Authentication")
                        .summary("세션 확인 - 인증된 상태")
                        .description("현재 사용자의 인증 세션이 유효한지 확인합니다 (인증된 경우)")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data")
                                .type(JsonFieldType.BOOLEAN)
                                .description("세션 유효 여부 (인증된 경우 true)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("세션 확인 - 미인증 사용자")
  void checkSession_unauthenticated_returns_unauthorized() throws Exception {
    // given - 세션 없이 요청 (미인증 상태)

    // when & then - 세션 확인 수행
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/auth/check-session"))
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("Authentication required"))
        .andExpect(jsonPath("$.data").isEmpty())
        .andDo(
            document(
                "auth-check-session-unauthenticated",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Authentication")
                        .summary("세션 확인 - 미인증 상태")
                        .description("인증되지 않은 상태에서 세션 확인 시 401 Unauthorized를 반환합니다")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부 (미인증 시 false)"),
                            fieldWithPath("data")
                                .type(JsonFieldType.NULL) // BOOLEAN -> NULL로 변경
                                .description("응답 데이터 (미인증 시 null)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지 (Authentication required)"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태 (UNAUTHORIZED)"))
                        .build())));
  }

  @Test
  @DisplayName("권한 정보 조회 - 인증된 사용자")
  void getPermissions_authenticated_success() throws Exception {
    // given - 먼저 로그인하여 세션 생성
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

    // when & then - 권한 정보 조회 수행
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/auth/permissions"))
                .session(session)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data.email").value("admin@icebang.site"))
        .andDo(
            document(
                "auth-get-permissions-authenticated",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Authentication")
                        .summary("권한 정보 조회 - 인증된 상태")
                        .description("현재 인증된 사용자의 상세 정보와 권한을 조회합니다")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data")
                                .type(JsonFieldType.OBJECT)
                                .description("사용자 인증 정보"),
                            fieldWithPath("data.id")
                                .type(JsonFieldType.NUMBER)
                                .description("사용자 고유 ID"),
                            fieldWithPath("data.email")
                                .type(JsonFieldType.STRING)
                                .description("사용자 이메일 주소"),
                            fieldWithPath("data.password")
                                .type(JsonFieldType.STRING)
                                .description("사용자 비밀번호"),
                            fieldWithPath("data.status")
                                .type(JsonFieldType.STRING)
                                .description("사용자 계정 상태"),
                            fieldWithPath("data.roles")
                                .type(JsonFieldType.ARRAY)
                                .description("사용자 권한 목록"),
                            fieldWithPath("data.enabled")
                                .type(JsonFieldType.BOOLEAN)
                                .description("계정 활성화 여부"),
                            fieldWithPath("data.username")
                                .type(JsonFieldType.STRING)
                                .description("사용자명 (이메일과 동일)"),
                            fieldWithPath("data.authorities")
                                .type(JsonFieldType.ARRAY)
                                .description("Spring Security 권한 목록"),
                            fieldWithPath("data.authorities[].authority")
                                .type(JsonFieldType.STRING)
                                .description("개별 권한"),
                            fieldWithPath("data.credentialsNonExpired")
                                .type(JsonFieldType.BOOLEAN)
                                .description("자격증명 만료 여부"),
                            fieldWithPath("data.accountNonExpired")
                                .type(JsonFieldType.BOOLEAN)
                                .description("계정 만료 여부"),
                            fieldWithPath("data.accountNonLocked")
                                .type(JsonFieldType.BOOLEAN)
                                .description("계정 잠금 여부"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("권한 정보 조회 - 미인증 사용자")
  void getPermissions_unauthenticated_returns_unauthorized() throws Exception {
    // given - 세션 없이 요청 (미인증 상태)

    // when & then - 권한 정보 조회 수행 (401 Unauthorized 응답 예상)
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/auth/permissions"))
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("Authentication required"))
        .andExpect(jsonPath("$.data").isEmpty())
        .andDo(
            document(
                "auth-get-permissions-unauthenticated",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Authentication")
                        .summary("권한 정보 조회 - 미인증 상태")
                        .description("인증되지 않은 상태에서 권한 정보 조회 시 401 Unauthorized를 반환합니다")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부 (미인증 시 false)"),
                            fieldWithPath("data")
                                .type(JsonFieldType.NULL)
                                .description("응답 데이터 (미인증 시 null)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지 (Authentication required)"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태 (UNAUTHORIZED)"))
                        .build())));
  }
}
