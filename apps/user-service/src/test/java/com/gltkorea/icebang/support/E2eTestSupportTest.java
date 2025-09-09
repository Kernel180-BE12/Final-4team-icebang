package com.gltkorea.icebang.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class E2eTestSupportTest extends E2eTestSupport {

  @Test
  void shouldStartWithRandomPort() {
    // 포트가 제대로 할당되었는지 확인
    assertThat(port).isGreaterThan(0);
    assertThat(getBaseUrl()).startsWith("http://localhost:");
    assertThat(getApiUrl("/test")).contains("/api/test");
  }
}
