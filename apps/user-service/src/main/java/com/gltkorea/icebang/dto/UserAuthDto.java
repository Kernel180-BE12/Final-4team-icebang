package com.gltkorea.icebang.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserAuthDto {
  private String userId; // 기존 필드 (String ID)
  private String name; // 기존 필드
  private String email; // 기존 필드
  private String password; // 추가: Default 로그인용
  private String provider; // 추가: "default", "kakao", "naver"
  private String providerId; // 추가: OAuth ID
  private String status; // 추가: 사용자 상태
  // ... 필요한 다른 필드들
}
