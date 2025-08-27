package com.gltkorea.icebang.domain.user.model;

import java.time.LocalDateTime;

import com.gltkorea.icebang.domain.user.UserStatus;

public class Users {
  private Long id;
  private String email;
  private String password; // Default 로그인시에만 사용 (OAuth는 null)
  private String provider; // "default", "kakao", "naver", "google"
  private String providerId; // Default: null, OAuth: 소셜 ID
  private UserStatus status;
  private LocalDateTime createdAt;
}
