package com.gltkorea.icebang.config.security.endpoints;

public enum SecurityEndpoints {
  PUBLIC(
      "/",
      "/login",
      "/register",
      "/api/public/**",
      "/health",
      "/css/**",
      "/js/**",
      "/images/**",
      "/v0/auth/**"), // 이 줄 추가!

  ADMIN("/admin/**", "/api/admin/**", "/management/**", "/actuator/**"),

  USER("/user/**", "/api/user/**", "/profile/**", "/dashboard");

  private final String[] patterns;

  SecurityEndpoints(String... patterns) {
    this.patterns = patterns.clone();
  }

  public String[] getMatchers() {
    return patterns.clone();
  }
}
