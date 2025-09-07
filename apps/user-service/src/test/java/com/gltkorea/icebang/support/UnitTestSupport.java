package com.gltkorea.icebang.support;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("unit")
@SpringBootTest
@ActiveProfiles("test-unit")
public abstract class UnitTestSupport {

  // 공통 유틸리티 메서드들
  protected void assertCommonValidation() {
    // 공통 검증 로직
  }
}
