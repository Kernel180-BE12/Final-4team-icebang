package com.gltkorea.icebang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.gltkorea.icebang.entity.Users;

/**
 * 사용자 데이터 접근을 위한 MyBatis 매퍼 인터페이스 - 실제 SQL은 UserMapper.xml에 작성 - Spring이 자동으로 구현체를 생성해줌 - 각 메서드는
 * XML의 동일한 id와 매핑됨
 */
@Mapper // MyBatis가 이 인터페이스를 구현체로 만들어줌
public interface UserMapper {

  // ========== 조회 관련 메서드 ==========

  /**
   * 사용자명으로 사용자 조회 (기본 정보만) - 로그인 처리 시 사용 - userName 필드로 검색
   *
   * @param username 검색할 사용자명
   * @return 찾은 사용자 정보, 없으면 null
   */
  Users findByUsername(@Param("username") String username);

  /**
   * 이메일로 사용자 조회 (기본 정보만) - 로그인 시 이메일을 ID로 사용하므로 중요 - userEmail 필드로 검색 - 이메일은 유니크하므로 결과는 0개 또는 1개
   *
   * @param email 검색할 이메일 주소
   * @return 찾은 사용자 정보, 없으면 null
   */
  Users findByEmail(@Param("email") String email);

  /**
   * 사용자 ID로 조회 (기본 정보만) - Primary Key로 조회하므로 가장 빠름 - 사용자 정보 수정/삭제 시 사용
   *
   * @param userId 검색할 사용자 ID
   * @return 찾은 사용자 정보, 없으면 null
   */
  Users findById(@Param("userId") Long userId);

  /**
   * 사용자와 조직 정보를 함께 조회 (JOIN 사용) - USERS + DEPARTMENT + POSITION + ORGANIZATION 테이블 조인 - 상세 정보가 필요한
   * 경우 사용 (마이페이지, 사용자 목록 등) - 조회 성능은 떨어지지만 한 번에 모든 정보 획득
   *
   * @param username 검색할 사용자명
   * @return 사용자 정보 + 부서명, 직급명, 조직명 포함
   */
  Users findByUsernameWithDetails(@Param("username") String username);

  /**
   * 이메일로 상세 정보 조회 (JOIN 사용) - 이메일 기반 로그인 후 상세 정보가 필요할 때 사용
   *
   * @param email 검색할 이메일 주소
   * @return 사용자 정보 + 부서명, 직급명, 조직명 포함
   */
  Users findByEmailWithDetails(@Param("email") String email);

  /**
   * 모든 사용자 목록 조회 (관리자용) - 관리자가 사용자 관리 화면에서 사용 - 조직 정보도 함께 조회 (JOIN 사용) - 성능을 위해 페이징 처리 고려 필요
   *
   * @return 모든 사용자 목록 (상세 정보 포함)
   */
  List<Users> findAllUsers();

  // ========== 권한 관련 조회 메서드 ==========

  /**
   * 사용자의 역할 목록 조회 - Spring Security에서 권한 검사 시 사용 - USERS_ROLE + ROLE 테이블 조인 - 결과: ["ADMIN",
   * "DATA_ENGINEER"] 형태
   *
   * @param userId 조회할 사용자 ID
   * @return 사용자가 가진 모든 역할명 목록
   */
  List<String> findRolesByUserId(@Param("userId") Long userId);

  /**
   * 사용자의 권한 목록 조회 - 역할을 통해 간접적으로 가진 권한들을 조회 - USERS_ROLE + ROLE_PERMISSION + PERMISSION 테이블 조인 -
   * 결과: ["USER", "DATA", "CONFIG"] 형태 (resource 기준)
   *
   * @param userId 조회할 사용자 ID
   * @return 사용자가 가진 모든 권한 리소스 목록
   */
  List<String> findPermissionsByUserId(@Param("userId") Long userId);

  // ========== 생성/수정/삭제 관련 메서드 ==========

  /**
   * 새 사용자 계정 생성 - 관리자가 신규 계정 생성 시 사용 - userId는 AUTO_INCREMENT로 자동 생성됨 - useGeneratedKeys=true로 생성된
   * ID를 다시 받아올 수 있음
   *
   * @param user 생성할 사용자 정보 (userId 제외)
   * @return 생성된 행의 수 (성공시 1)
   */
  int insertUser(Users user);

  /**
   * 사용자 정보 수정 - 마이페이지에서 사용자가 본인 정보 수정 - 관리자가 사용자 정보 수정 - 비밀번호는 별도 메서드로 처리 권장
   *
   * @param user 수정할 사용자 정보 (userId 필수)
   * @return 수정된 행의 수 (성공시 1, 대상 없으면 0)
   */
  int updateUser(Users user);

  /**
   * 사용자 비밀번호만 수정 - 보안상 비밀번호는 별도로 처리 - 기존 비밀번호 검증은 Service 레이어에서 처리
   *
   * @param userId 수정할 사용자 ID
   * @param newPassword 새로운 암호화된 비밀번호
   * @return 수정된 행의 수 (성공시 1)
   */
  int updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

  /**
   * 사용자 계정 상태만 변경 - 계정 활성화/정지 등에 사용 - ACTIVE, SUSPENDED, INACTIVE 등으로 변경
   *
   * @param userId 수정할 사용자 ID
   * @param status 새로운 계정 상태
   * @return 수정된 행의 수 (성공시 1)
   */
  int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);

  /**
   * 사용자 계정 삭제 - 실제 운영에서는 soft delete(상태 변경) 권장 - 외래키 제약 조건 때문에 USERS_ROLE 먼저 삭제 필요
   *
   * @param userId 삭제할 사용자 ID
   * @return 삭제된 행의 수 (성공시 1)
   */
  int deleteUser(@Param("userId") Long userId);

  // ========== 검증 관련 메서드 ==========

  /**
   * 이메일 중복 검사 - 신규 계정 생성 시 중복 확인용 - 이미 존재하면 true, 없으면 false
   *
   * @param email 검사할 이메일
   * @return 중복되면 true, 중복 안되면 false
   */
  boolean existsByEmail(@Param("email") String email);

  /**
   * 사용자명 중복 검사 - 사용자명 변경 시 중복 확인용
   *
   * @param username 검사할 사용자명
   * @return 중복되면 true, 중복 안되면 false
   */
  boolean existsByUsername(@Param("username") String username);
}
