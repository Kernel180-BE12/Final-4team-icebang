package com.gltkorea.icebang.global.config.security.endpoints;

public enum SecurityEndpoints {
  PUBLIC(
      "/",
      "/ping",
      "/v0/auth/login",
      "/api/public/**",
      "/health",
      "/css/**",
      "/js/**",
      "/images/**",
      "/v0/organizations/**",
      "/v0/auth/register"),

  // 데이터 관리 관련 엔드포인트
  DATA_ADMIN("/admin/**", "/api/admin/**", "/management/**", "/actuator/**"),

  // 데이터 엔지니어 전용 엔드포인트
  DATA_ENGINEER("/api/preprocessing/**", "/api/pipeline/**", "/api/jobs/**"),

  // 분석가 전용 엔드포인트
  ANALYST("/api/analysis/**", "/api/reports/**", "/api/dashboard/**"),

  // 운영 관련 엔드포인트
  OPS("/api/scheduler/**", "/api/monitoring/**"),

  // 일반 사용자 엔드포인트
  USER("/user/**", "/profile/**", "/v0/auth/check-session");

  private final String[] patterns;

  SecurityEndpoints(String... patterns) {
    this.patterns = patterns.clone();
  }

  public String[] getMatchers() {
    return patterns.clone();
  }
}
