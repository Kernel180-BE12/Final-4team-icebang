// package com.gltkorea.icebang.e2e.scenario;
//
// import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
// import static com.epages.restdocs.apispec.ResourceDocumentation.*;
// import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
// import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
// import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
// import static org.springframework.restdocs.payload.PayloadDocumentation.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.Map;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.http.*;
// import org.springframework.restdocs.payload.JsonFieldType;
// import org.springframework.test.annotation.DirtiesContext;
// import org.springframework.test.context.jdbc.Sql;
//
// import com.epages.restdocs.apispec.ResourceSnippetParameters;
// import com.gltkorea.icebang.e2e.support.E2eTestSupport;
//
// @Sql("classpath:sql/01-insert-internal-users.sql")
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
// class UserRegistrationFlowE2eTest extends E2eTestSupport {
//
//    @Test
//    @DisplayName("조직 목록 조회 성공")
//    void getOrganizations_success() throws Exception {
//        mockMvc
//                .perform(
//                        get(getApiUrlForDocs("/v0/organizations"))
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .header("Origin", "https://admin.icebang.site")
//                                .header("Referer", "https://admin.icebang.site/"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.status").value("OK"))
//                .andExpect(jsonPath("$.message").value("OK"))
//                .andExpect(jsonPath("$.data").isArray())
//                .andDo(
//                        document(
//                                "organizations-list",
//                                preprocessRequest(prettyPrint()),
//                                preprocessResponse(prettyPrint()),
//                                resource(
//                                        ResourceSnippetParameters.builder()
//                                                .tag("Organization")
//                                                .summary("조직 목록 조회")
//                                                .description("시스템에 등록된 모든 조직의 목록을 조회합니다")
//                                                .responseFields(
//                                                        fieldWithPath("success")
//                                                                .type(JsonFieldType.BOOLEAN)
//                                                                .description("요청 성공 여부"),
//
// fieldWithPath("data[]").type(JsonFieldType.ARRAY).description("조직 목록"),
//                                                        fieldWithPath("data[].id")
//                                                                .type(JsonFieldType.NUMBER)
//                                                                .description("조직 ID"),
//                                                        fieldWithPath("data[].organizationName")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("조직명"),
//                                                        fieldWithPath("message")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("응답 메시지"),
//                                                        fieldWithPath("status")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("HTTP 상태"))
//                                                .build())));
//    }
//
//    @Test
//    @DisplayName("조직별 옵션 조회 성공")
//    void getOrganizationOptions_success() throws Exception {
//        mockMvc
//                .perform(
//                        get(getApiUrlForDocs("/v0/organizations/{orgId}/options"), 1)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .header("Origin", "https://admin.icebang.site")
//                                .header("Referer", "https://admin.icebang.site/"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.status").value("OK"))
//                .andExpect(jsonPath("$.message").value("OK"))
//                .andExpect(jsonPath("$.data.departments").isArray())
//                .andExpect(jsonPath("$.data.positions").isArray())
//                .andExpect(jsonPath("$.data.roles").isArray())
//                .andDo(
//                        document(
//                                "organization-options",
//                                preprocessRequest(prettyPrint()),
//                                preprocessResponse(prettyPrint()),
//                                resource(
//                                        ResourceSnippetParameters.builder()
//                                                .tag("Organization")
//                                                .summary("조직별 옵션 조회")
//                                                .description("특정 조직의 부서, 직급, 역할 정보를 조회합니다")
//                                                .responseFields(
//                                                        fieldWithPath("success")
//                                                                .type(JsonFieldType.BOOLEAN)
//                                                                .description("요청 성공 여부"),
//                                                        fieldWithPath("data")
//                                                                .type(JsonFieldType.OBJECT)
//                                                                .description("조직 옵션 데이터"),
//                                                        fieldWithPath("data.departments[]")
//                                                                .type(JsonFieldType.ARRAY)
//                                                                .description("부서 목록"),
//                                                        fieldWithPath("data.departments[].id")
//                                                                .type(JsonFieldType.NUMBER)
//                                                                .description("부서 ID"),
//                                                        fieldWithPath("data.departments[].name")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("부서명"),
//                                                        fieldWithPath("data.positions[]")
//                                                                .type(JsonFieldType.ARRAY)
//                                                                .description("직급 목록"),
//                                                        fieldWithPath("data.positions[].id")
//                                                                .type(JsonFieldType.NUMBER)
//                                                                .description("직급 ID"),
//                                                        fieldWithPath("data.positions[].title")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("직급명"),
//                                                        fieldWithPath("data.roles[]")
//                                                                .type(JsonFieldType.ARRAY)
//                                                                .description("역할 목록"),
//                                                        fieldWithPath("data.roles[].id")
//                                                                .type(JsonFieldType.NUMBER)
//                                                                .description("역할 ID"),
//                                                        fieldWithPath("data.roles[].name")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("역할 코드명"),
//                                                        fieldWithPath("data.roles[].description")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("역할 설명"),
//                                                        fieldWithPath("message")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("응답 메시지"),
//                                                        fieldWithPath("status")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("HTTP 상태"))
//                                                .build())));
//    }
//
//    @Test
//    @DisplayName("사용자 로그인 성공")
//    void login_success() throws Exception {
//        // given
//        Map<String, String> loginRequest = new HashMap<>();
//        loginRequest.put("email", "admin@icebang.site");
//        loginRequest.put("password", "qwer1234!A");
//
//        // MockMvc로 REST Docs + OpenAPI 생성
//        mockMvc
//                .perform(
//                        post(getApiUrlForDocs("/v0/auth/login"))
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .header("Origin", "https://admin.icebang.site")
//                                .header("Referer", "https://admin.icebang.site/")
//                                .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.status").value("OK"))
//                .andExpect(jsonPath("$.message").value("OK"))
//                .andExpect(jsonPath("$.data").isEmpty())
//                .andDo(
//                        document(
//                                "auth-login",
//                                preprocessRequest(prettyPrint()),
//                                preprocessResponse(prettyPrint()),
//                                resource(
//                                        ResourceSnippetParameters.builder()
//                                                .tag("Authentication")
//                                                .summary("사용자 로그인")
//                                                .description("이메일과 비밀번호로 사용자 인증을 수행합니다")
//                                                .requestFields(
//                                                        fieldWithPath("email")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("사용자 이메일 주소"),
//                                                        fieldWithPath("password")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("사용자 비밀번호"))
//                                                .responseFields(
//                                                        fieldWithPath("success")
//                                                                .type(JsonFieldType.BOOLEAN)
//                                                                .description("요청 성공 여부"),
//                                                        fieldWithPath("data")
//                                                                .type(JsonFieldType.NULL)
//                                                                .description("응답 데이터 (로그인 성공 시
// null)"),
//                                                        fieldWithPath("message")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("응답 메시지"),
//                                                        fieldWithPath("status")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("HTTP 상태"))
//                                                .build())));
//    }
//
//    @Test
//    @DisplayName("사용자 회원가입 성공")
//    void register_success() throws Exception {
//        // given - 먼저 로그인하여 인증 토큰 획득
//        Map<String, String> loginRequest = new HashMap<>();
//        loginRequest.put("email", "admin@icebang.site");
//        loginRequest.put("password", "qwer1234!A");
//
//        // 로그인 수행 (실제 환경에서는 토큰을 헤더에 추가해야 할 수 있음)
//        mockMvc
//                .perform(
//                        post("/v0/auth/login")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk());
//
//        // 회원가입 요청 데이터
//        Map<String, Object> registerRequest = new HashMap<>();
//        registerRequest.put("name", "김철수");
//        registerRequest.put("email", "kim.chulsoo@example.com");
//        registerRequest.put("orgId", 1);
//        registerRequest.put("deptId", 2);
//        registerRequest.put("positionId", 5);
//        registerRequest.put("roleIds", Arrays.asList(6, 7, 8));
//        registerRequest.put("password", null);
//
//        // when & then
//        mockMvc
//                .perform(
//                        post(getApiUrlForDocs("/v0/auth/register"))
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .header("Origin", "https://admin.icebang.site")
//                                .header("Referer", "https://admin.icebang.site/")
//                                .content(objectMapper.writeValueAsString(registerRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.status").value("OK"))
//                .andExpect(jsonPath("$.message").value("OK"))
//                .andDo(
//                        document(
//                                "auth-register",
//                                preprocessRequest(prettyPrint()),
//                                preprocessResponse(prettyPrint()),
//                                resource(
//                                        ResourceSnippetParameters.builder()
//                                                .tag("Authentication")
//                                                .summary("사용자 회원가입")
//                                                .description("새로운 사용자를 등록합니다. 관리자 로그인 후에만 사용
// 가능합니다.")
//                                                .requestFields(
//
// fieldWithPath("name").type(JsonFieldType.STRING).description("사용자 이름"),
//                                                        fieldWithPath("email")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("사용자 이메일 주소"),
//
// fieldWithPath("orgId").type(JsonFieldType.NUMBER).description("조직 ID"),
//
// fieldWithPath("deptId").type(JsonFieldType.NUMBER).description("부서 ID"),
//                                                        fieldWithPath("positionId")
//                                                                .type(JsonFieldType.NUMBER)
//                                                                .description("직급 ID"),
//                                                        fieldWithPath("roleIds[]")
//                                                                .type(JsonFieldType.ARRAY)
//                                                                .description("역할 ID 목록"),
//                                                        fieldWithPath("password")
//                                                                .type(JsonFieldType.NULL)
//                                                                .description("비밀번호 (null인 경우 시스템에서
// 자동 생성)")
//                                                                .optional())
//                                                .responseFields(
//                                                        fieldWithPath("success")
//                                                                .type(JsonFieldType.BOOLEAN)
//                                                                .description("요청 성공 여부"),
//                                                        fieldWithPath("data")
//                                                                .type(JsonFieldType.VARIES)
//                                                                .description("응답 데이터 (회원가입 결과
// 정보)"),
//                                                        fieldWithPath("message")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("응답 메시지"),
//                                                        fieldWithPath("status")
//                                                                .type(JsonFieldType.STRING)
//                                                                .description("HTTP 상태"))
//                                                .build())));
//    }
// }
