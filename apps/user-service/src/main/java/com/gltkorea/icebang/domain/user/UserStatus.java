package com.gltkorea.icebang.domain.user;

enum UserStatus {
  PENDING, // 기본 정보만 입력됨
  PROFILE_INCOMPLETE, // 프로필 사진 등 추가 정보 필요
  ACTIVE, // 완전히 활성화됨
  SUSPENDED, // 일시 정지
  DELETED // 삭제됨
}
