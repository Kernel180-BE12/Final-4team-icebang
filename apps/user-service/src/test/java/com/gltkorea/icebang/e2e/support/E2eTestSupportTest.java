package com.gltkorea.icebang.e2e.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class E2eTestSupportTest extends E2eTestSupport {

  @Test
  void shouldStartWithRandomPort() {
    // 포트가 제대로 할당되었는지 확인
    assertThat(port).isGreaterThan(0);
    assertThat(getBaseUrl()).startsWith("http://localhost:");
  }
}
