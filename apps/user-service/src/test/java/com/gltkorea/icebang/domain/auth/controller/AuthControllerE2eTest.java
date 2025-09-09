package com.gltkorea.icebang.domain.auth.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import com.gltkorea.icebang.support.E2eTestSupport;

// @Rollback
@Sql("classpath:sql/01-insert-internal-users.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthControllerE2eTest extends E2eTestSupport {

  @Test
  @DisplayName("사용자 로그인 성공")
  void login_success() throws Exception {
    // given
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("email", "admin@icebang.site");
    loginRequest.put("password", "qwer1234!A");

    // MockMvc로 REST Docs 생성
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
                requestHeaders(
                    headerWithName("Content-Type").description("요청 컨텐츠 타입"),
                    headerWithName("Origin").description("요청 Origin (CORS)").optional(),
                    headerWithName("Referer").description("요청 Referer").optional()),
                requestFields(
                    fieldWithPath("email").type(JsonFieldType.STRING).description("사용자 이메일 주소"),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("사용자 비밀번호")),
                responseHeaders(headerWithName("Content-Type").description("응답 컨텐츠 타입").optional()),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                    fieldWithPath("data")
                        .type(JsonFieldType.NULL)
                        .description("응답 데이터 (로그인 성공 시 null)"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("status").type(JsonFieldType.STRING).description("HTTP 상태"))));
  }
}
