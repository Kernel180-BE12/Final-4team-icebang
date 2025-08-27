package com.gltkorea.icebang.domain.user;

public enum UserStatus {
  ONBOARDING, // email, password만 된 경우
  ACTIVE, // 완전히 활성화됨
  SUSPENDED, // 일시 정지
  DELETED // 삭제됨
}
