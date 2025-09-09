package com.gltkorea.icebang.e2e.support;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.gltkorea.icebang.e2e.annotation.E2eTest;
import com.gltkorea.icebang.e2e.config.E2eTestConfiguration;

@Import(E2eTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(RestDocumentationExtension.class)
@E2eTest
public abstract class E2eTestSupport {
  @Autowired protected ObjectMapper objectMapper;

  @LocalServerPort protected int port;

  @Autowired protected WebApplicationContext webApplicationContext;

  protected MockMvc mockMvc;

  @BeforeEach
  void setUp(RestDocumentationContextProvider restDocumentation) {
    // MockMvc 설정 (MockMvc 기반 테스트용)
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(
                documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint()))
            .build();
  }

  protected String getBaseUrl() {
    return "http://localhost:" + port;
  }

  protected String getApiUrl(String path) {
    return getBaseUrl() + path;
  }

  /** REST Docs용 API URL 생성 (path parameter 포함) */
  protected String getApiUrlForDocs(String path) {
    return path;
  }
}
