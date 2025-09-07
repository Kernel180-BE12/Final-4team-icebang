package com.gltkorea.icebang.support;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.gltkorea.icebang.config.E2eTestConfiguration;

@Tag("e2e")
@Import(E2eTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-e2e")
public abstract class E2eTestSupport {

  @LocalServerPort protected int port;

  @Autowired protected TestRestTemplate restTemplate;

  protected String getBaseUrl() {
    return "http://localhost:" + port;
  }

  protected String getApiUrl(String path) {
    return getBaseUrl() + "/api" + path;
  }
}
