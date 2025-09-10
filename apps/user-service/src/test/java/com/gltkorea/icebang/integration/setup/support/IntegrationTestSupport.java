package com.gltkorea.icebang.integration.setup.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gltkorea.icebang.integration.setup.annotation.IntegrationTest;
import com.gltkorea.icebang.integration.setup.config.RestDocsConfiguration;

@IntegrationTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public abstract class IntegrationTestSupport {

  @Autowired protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  @LocalServerPort protected int port;

  /** RestDocs에서 실제 API 호출 주소를 표기할 때 사용 */
  protected String getApiUrlForDocs(String path) {
    if (path.startsWith("/")) {
      return "http://localhost:" + port + path;
    }
    return "http://localhost:" + port + "/" + path;
  }
}
