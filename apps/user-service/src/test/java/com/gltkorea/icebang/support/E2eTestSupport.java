package com.gltkorea.icebang.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import com.gltkorea.icebang.annotation.E2eTest;
import com.gltkorea.icebang.config.E2eTestConfiguration;

@Import(E2eTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@E2eTest
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
