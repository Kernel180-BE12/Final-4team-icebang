package com.gltkorea.icebang.support;

import org.springframework.boot.test.context.SpringBootTest;

import com.gltkorea.icebang.annotation.UnitTest;

@SpringBootTest
@UnitTest
public abstract class UnitTestSupport {

  // 공통 유틸리티 메서드들
  protected void assertCommonValidation() {
    // 공통 검증 로직
  }
}
