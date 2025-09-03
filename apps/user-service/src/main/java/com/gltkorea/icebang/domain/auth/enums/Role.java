package com.gltkorea.icebang.domain.auth.enums;

import lombok.Getter;

@Getter
public enum Role {
  // 시스템 관리자
  SUPER_ADMIN("시스템 전체 관리 권한", 100),
  ADMIN("관리자 권한 (사용자 관리 제외)", 90),

  // 데이터 처리 담당자
  SENIOR_DATA_ENGINEER("고급 데이터 엔지니어 (파이프라인 설계/수정)", 80),
  DATA_ENGINEER("데이터 엔지니어 (전처리 작업 실행)", 70),

  // 품질 관리
  QA_ENGINEER("데이터 품질 검증 담당", 60),
  DATA_STEWARD("데이터 거버넌스 관리", 50),

  // 분석가
  SENIOR_DATA_ANALYST("수석 데이터 분석가", 40),
  DATA_ANALYST("데이터 분석가", 30),

  // 조회 권한
  VIEWER("읽기 전용 사용자", 20),
  GUEST("제한적 조회 권한", 10);

  private final String description;
  private final int level;

  private Role(String description, int level) {
    this.description = description;
    this.level = level;
  }

  /**
   * 권한 레벨 비교 (현재 역할이 요구 역할보다 높은 권한을 가지는지 확인)
   *
   * @param requiredRole 요구되는 최소 역할
   * @return 권한이 충분한지 여부
   */
  public boolean hasPermission(Role requiredRole) {
    return this.level >= requiredRole.level;
  }

  /**
   * 관리자 권한 여부 확인
   *
   * @return 관리자 권한 보유 여부
   */
  public boolean isAdmin() {
    return this.level >= ADMIN.level;
  }

  /**
   * 데이터 엔지니어 권한 여부 확인
   *
   * @return 데이터 엔지니어 권한 보유 여부
   */
  public boolean canExecuteDataProcessing() {
    return this.level >= DATA_ENGINEER.level;
  }
}
