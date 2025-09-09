package com.gltkorea.icebang.integration.support;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.gltkorea.icebang.integration.annotation.IntegrationTest;

@IntegrationTest
@ExtendWith(RestDocumentationExtension.class)
public abstract class IntegrationTestSupport {
  protected MockMvc mockMvc;
}
