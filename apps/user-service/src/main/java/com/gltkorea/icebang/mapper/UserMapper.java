package com.gltkorea.icebang.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.gltkorea.icebang.dto.UserAuthDto;

@Mapper
public interface UserMapper {
  /**
   * 이메일로 사용자 조회
   *
   * @param email 사용자 이메일
   * @return 사용자 정보 (Optional)
   */
  Optional<UserAuthDto> findByEmail(String email);

  /**
   * 새 사용자 생성 (회원가입)
   *
   * @param user 생성할 사용자 정보
   * @return 생성된 행 수 (성공시 1)
   */
  int insertUser(UserAuthDto user);

  /**
   * 이메일과 비밀번호로 사용자 조회 (로그인)
   *
   * @param email 사용자 이메일
   * @param password 비밀번호
   * @return 사용자 정보 (Optional)
   */
  Optional<UserAuthDto> findByEmailAndPassword(
      @Param("email") String email, @Param("password") String password);

  /**
   * 사용자 ID로 조회
   *
   * @param userId 사용자 ID
   * @return 사용자 정보 (Optional)
   */
  Optional<UserAuthDto> findByUserId(String userId);
}
