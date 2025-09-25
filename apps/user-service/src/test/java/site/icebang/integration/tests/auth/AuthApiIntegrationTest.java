package site.icebang.integration.tests.auth;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
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
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() throws Exception {
        // given - 먼저 로그인해서 인증된 세션 생성
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

        // 비밀번호 변경 요청 데이터
        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("currentPassword", "qwer1234!A");
        changePasswordRequest.put("newPassword", "newPassword123!A");
        changePasswordRequest.put("confirmPassword", "newPassword123!A");

        // when & then - 비밀번호 변경 수행
        mockMvc
                .perform(
                        patch(getApiUrlForDocs("/v0/auth/change-password"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header("Origin", "https://admin.icebang.site")
                                .header("Referer", "https://admin.icebang.site/")
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(
                        document(
                                "auth-change-password",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Authentication")
                                                .summary("비밀번호 변경")
                                                .description("현재 로그인된 사용자의 비밀번호를 변경합니다")
                                                .requestFields(
                                                        fieldWithPath("currentPassword")
                                                                .type(JsonFieldType.STRING)
                                                                .description("현재 비밀번호"),
                                                        fieldWithPath("newPassword")
                                                                .type(JsonFieldType.STRING)
                                                                .description("새 비밀번호 (최소 8자 이상)"),
                                                        fieldWithPath("confirmPassword")
                                                                .type(JsonFieldType.STRING)
                                                                .description("새 비밀번호 확인"))
                                                .responseFields(
                                                        fieldWithPath("success")
                                                                .type(JsonFieldType.BOOLEAN)
                                                                .description("요청 성공 여부"),
                                                        fieldWithPath("data")
                                                                .type(JsonFieldType.NULL)
                                                                .description("응답 데이터 (비밀번호 변경 성공 시 null)"),
                                                        fieldWithPath("message")
                                                                .type(JsonFieldType.STRING)
                                                                .description("응답 메시지"),
                                                        fieldWithPath("status")
                                                                .type(JsonFieldType.STRING)
                                                                .description("HTTP 상태"))
                                                .build())));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 잘못된 현재 비밀번호")
    void changePassword_fail_wrongCurrentPassword() throws Exception {
        // given - 로그인된 세션
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "admin@icebang.site");
        loginRequest.put("password", "qwer1234!A");

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(
                        post(getApiUrlForDocs("/v0/auth/login"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // 잘못된 현재 비밀번호
        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("currentPassword", "wrongPassword");
        changePasswordRequest.put("newPassword", "newPassword123!A");
        changePasswordRequest.put("confirmPassword", "newPassword123!A");

        // when & then
        mockMvc.perform(
                        patch(getApiUrlForDocs("/v0/auth/change-password"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 올바르지 않습니다"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호와 확인 비밀번호 불일치")
    void changePassword_fail_passwordMismatch() throws Exception {
        // given - 로그인된 세션
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "admin@icebang.site");
        loginRequest.put("password", "qwer1234!A");

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(
                        post(getApiUrlForDocs("/v0/auth/login"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // 새 비밀번호와 확인 비밀번호 불일치
        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("currentPassword", "qwer1234!A");
        changePasswordRequest.put("newPassword", "newPassword123!A");
        changePasswordRequest.put("confirmPassword", "differentPassword123!A");

        // when & then
        mockMvc.perform(
                        patch(getApiUrlForDocs("/v0/auth/change-password"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("새 비밀번호가 일치하지 않습니다"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 길이 부족")
    void changePassword_fail_passwordTooShort() throws Exception {
        // given - 로그인된 세션
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "admin@icebang.site");
        loginRequest.put("password", "qwer1234!A");

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(
                        post(getApiUrlForDocs("/v0/auth/login"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // 8자 미만의 새 비밀번호
        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("currentPassword", "qwer1234!A");
        changePasswordRequest.put("newPassword", "123!A"); // 5자
        changePasswordRequest.put("confirmPassword", "123!A");

        // when & then
        mockMvc.perform(
                        patch(getApiUrlForDocs("/v0/auth/change-password"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                // Validation 메시지는 @Size(message="...") 지정한 값과 동일하게 맞추면 됨
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("비밀번호")))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 미인증 사용자")
    void changePassword_fail_unauthorized() throws Exception {
        // given - 로그인하지 않은 상태
        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("currentPassword", "qwer1234!A");
        changePasswordRequest.put("newPassword", "newPassword123!A");
        changePasswordRequest.put("confirmPassword", "newPassword123!A");

        // when & then
        mockMvc.perform(
                        patch(getApiUrlForDocs("/v0/auth/change-password"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
    }