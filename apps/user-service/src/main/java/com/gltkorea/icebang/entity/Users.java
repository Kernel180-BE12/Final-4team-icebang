package com.gltkorea.icebang.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 사용자 엔티티 클래스 - DB의 USERS 테이블과 1:1 매핑 - MyBatis에서 조회/수정 시 사용하는 Java 객체 */
@Data // getter, setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성 (MyBatis 필수)
@AllArgsConstructor // 모든 필드를 받는 생성자
@Builder // Builder 패턴 지원 (객체 생성 시 편리함)
public class Users {

  // ========== 기본 사용자 정보 (USERS 테이블 컬럼과 동일) ==========

  /** 사용자 고유 ID (Primary Key) - DB에서 AUTO_INCREMENT로 자동 생성 - 시스템 내에서 사용자를 구분하는 유일한 값 */
  private Long userId;

  /** 사용자 이름 - 화면에 표시되는 이름 (예: "홍길동") - 관리자가 계정 생성 시 설정하거나, 사용자가 마이페이지에서 수정 */
  private String userName;

  /** 사용자 이메일 (로그인 ID로 사용) - 시스템 로그인 시 ID로 사용 - 유니크 값 (중복 불가) - 관리자가 계정 생성 시 설정 */
  private String userEmail;

  /** 사용자 비밀번호 (암호화된 상태로 저장) - BCrypt로 암호화되어 저장됨 - 관리자가 계정 생성 시 임시 비밀번호 설정 - 사용자가 마이페이지에서 변경 가능 */
  private String userPassword;

  /**
   * 사용자 계정 상태 가능한 값: - "ACTIVE": 정상 활성 상태 (로그인 가능) - "SUSPENDED": 계정 정지 (로그인 불가) - "INACTIVE": 비활성
   * 상태 (휴면 계정) - "PENDING": 대기 상태 (아직 활성화 안됨)
   */
  private String userStatus;

  /** 소속 부서 ID - DEPARTMENT 테이블의 dept_id와 연결 - 관리자가 계정 생성 시 설정 */
  private Long deptId;

  /** 직급 ID - POSITION 테이블의 position_id와 연결 - 관리자가 계정 생성 시 설정 */
  private Long positionId;

  // ========== 조회 시 조인된 데이터 (실제 DB 컬럼 X) ==========
  // MyBatis에서 JOIN 쿼리 결과를 받기 위한 필드들
  // INSERT/UPDATE 시에는 사용되지 않음

  /** 부서명 (조인 데이터) - DEPARTMENT 테이블에서 가져온 dept_name - 사용자 정보 조회 시 함께 표시하기 위해 사용 */
  private String deptName;

  /** 직급명 (조인 데이터) - POSITION 테이블에서 가져온 position_title - 사용자 정보 조회 시 함께 표시하기 위해 사용 */
  private String positionTitle;

  /** 조직명 (조인 데이터) - ORGANIZATION 테이블에서 가져온 org_name - 사용자 정보 조회 시 함께 표시하기 위해 사용 */
  private String orgName;

  // ========== 편의 메서드 ==========

  /**
   * 계정이 활성 상태인지 확인
   *
   * @return 활성 상태이면 true, 아니면 false
   */
  public boolean isActive() {
    return "ACTIVE".equals(this.userStatus);
  }

  /**
   * 계정이 정지 상태인지 확인
   *
   * @return 정지 상태이면 true, 아니면 false
   */
  public boolean isSuspended() {
    return "SUSPENDED".equals(this.userStatus);
  }

  /**
   * 이메일에서 사용자명 부분만 추출 예: "hong@company.com" → "hong"
   *
   * @return 이메일의 @ 앞부분
   */
  public String extractUsernameFromEmail() {
    if (userEmail == null || !userEmail.contains("@")) {
      return userEmail;
    }
    return userEmail.substring(0, userEmail.indexOf("@"));
  }

  /**
   * 화면 표시용 풀네임 반환 userName이 없으면 이메일에서 추출한 이름 사용
   *
   * @return 화면에 표시할 사용자 이름
   */
  public String getDisplayName() {
    if (userName != null && !userName.trim().isEmpty()) {
      return userName;
    }
    return extractUsernameFromEmail();
  }
}
