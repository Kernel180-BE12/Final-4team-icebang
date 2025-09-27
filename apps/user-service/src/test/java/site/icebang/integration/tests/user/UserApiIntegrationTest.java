package site.icebang.integration.tests.user;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
      "classpath:sql/data/01-insert-internal-users.sql",
      "classpath:sql/data/02-insert-external-users.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class UserApiIntegrationTest extends IntegrationTestSupport {

  @Test
  @DisplayName("유저 자신의 정보 조회 성공")
  @WithUserDetails("admin@icebang.site")
  void getUserProfile_success() throws Exception {
    // when & then
    mockMvc
        .perform(
            get(getApiUrlForDocs("/v0/users/me"))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.id").exists())
        .andExpect(jsonPath("$.data.email").value("admin@icebang.site"))
        .andExpect(jsonPath("$.data.name").exists())
        .andExpect(jsonPath("$.data.roles").exists())
        .andExpect(jsonPath("$.data.status").value("ACTIVE"))
        .andDo(
            document(
                "user-profile",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("User")
                        .summary("사용자 프로필 조회")
                        .description("현재 로그인한 사용자의 프로필 정보를 조회합니다")
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data").type(JsonFieldType.OBJECT).description("사용자 정보"),
                            fieldWithPath("data.id")
                                .type(JsonFieldType.NUMBER)
                                .description("사용자 ID"),
                            fieldWithPath("data.email")
                                .type(JsonFieldType.STRING)
                                .description("사용자 이메일"),
                            fieldWithPath("data.name")
                                .type(JsonFieldType.STRING)
                                .description("사용자 이름"),
                            fieldWithPath("data.roles")
                                .type(JsonFieldType.ARRAY)
                                .description("사용자 권한 목록"),
                            fieldWithPath("data.status")
                                .type(JsonFieldType.STRING)
                                .description("사용자 상태 (ACTIVE, INACTIVE)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("이메일 중복 검사 성공 - 사용 가능한 이메일")
  @WithUserDetails("admin@icebang.site")
  void checkEmailAvailable_success() throws Exception {
    String requestBody =
        """
        {
          "email": "newuser@example.com"
        }
        """;

    // when & then
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/users/check-email"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다."))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.available").value(true))
        .andDo(
            document(
                "check-email-available",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("User")
                        .summary("이메일 중복 검사")
                        .description("사용자 회원가입 전 이메일 중복 여부를 확인합니다")
                        .requestFields(
                            fieldWithPath("email")
                                .type(JsonFieldType.STRING)
                                .description("검사할 이메일 주소"))
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                            fieldWithPath("data.available")
                                .type(JsonFieldType.BOOLEAN)
                                .description("이메일 사용 가능 여부 (true: 사용 가능, false: 이미 사용 중)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("이메일 중복 검사 성공 - 이미 사용 중인 이메일")
  @WithUserDetails("admin@icebang.site")
  void checkEmailAvailable_alreadyExists() throws Exception {
    String requestBody =
        """
        {
          "email": "admin@icebang.site"
        }
        """;

    // when & then
    mockMvc
        .perform(
            post(getApiUrlForDocs("/v0/users/check-email"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.status").value("OK"))
        .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.available").value(false))
        .andDo(
            document(
                "check-email-unavailable",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("User")
                        .summary("이메일 중복 검사 - 사용 불가")
                        .description("이미 가입된 이메일에 대한 중복 검사 결과")
                        .requestFields(
                            fieldWithPath("email")
                                .type(JsonFieldType.STRING)
                                .description("검사할 이메일 주소"))
                        .responseFields(
                            fieldWithPath("success")
                                .type(JsonFieldType.BOOLEAN)
                                .description("요청 성공 여부"),
                            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                            fieldWithPath("data.available")
                                .type(JsonFieldType.BOOLEAN)
                                .description("이메일 사용 가능 여부 (true: 사용 가능, false: 이미 사용 중)"),
                            fieldWithPath("message")
                                .type(JsonFieldType.STRING)
                                .description("응답 메시지"),
                            fieldWithPath("status")
                                .type(JsonFieldType.STRING)
                                .description("HTTP 상태"))
                        .build())));
  }

  @Test
  @DisplayName("이메일 중복 검사 실패 - 잘못된 이메일 형식")
  @WithUserDetails("admin@icebang.site")
  void checkEmailAvailable_invalidFormat() throws Exception {
    String requestBody = """
        {
          "email": "invalid-email"
        }
        """;

    // when & then
    mockMvc
        .perform(
            post("/v0/users/check-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Origin", "https://admin.icebang.site")
                .header("Referer", "https://admin.icebang.site/"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }
}
