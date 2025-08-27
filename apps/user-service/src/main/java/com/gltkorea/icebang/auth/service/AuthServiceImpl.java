package com.gltkorea.icebang.auth.service;

/*
비밀번호 암호화: PasswordEncoder 사용
중복 검증: 회원가입 시 이메일 중복 체크
UUID 생성: 고유한 userId 자동 생성
예외 처리: 명확한 에러 메시지
상태 관리: ACTIVE 상태 체크
 */

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gltkorea.icebang.auth.dto.LoginDto;
import com.gltkorea.icebang.auth.dto.SignUpDto;
import com.gltkorea.icebang.domain.user.UserStatus;
import com.gltkorea.icebang.dto.UserAuthDto;
import com.gltkorea.icebang.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder; // 비밀번호 암호화용

  /**
   * 회원가입 처리
   *
   * @param signUpDto 회원가입 정보
   * @return 생성된 사용자 정보
   */
  @Override
  @Transactional
  public UserAuthDto signUp(SignUpDto signUpDto) {
    // 1. 이메일 중복 체크
    Optional<UserAuthDto> existingUser = userMapper.findByEmail(signUpDto.getEmail());
    if (existingUser.isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + signUpDto.getEmail());
    }

    // 2. 새 사용자 객체 생성
    UserAuthDto newUser =
        UserAuthDto.builder()
            .userId(UUID.randomUUID().toString())
            .email(signUpDto.getEmail())
            .password(passwordEncoder.encode(signUpDto.getPassword()))
            .provider("default")
            .providerId(null)
            .status(UserStatus.ONBOARDING.name())
            .name(signUpDto.getEmail().split("@")[0]) // 이메일 앞부분을 임시 이름으로 @TODO:: fix to name
            .build();

    // 3. DB에 저장
    int result = userMapper.insertUser(newUser);
    if (result != 1) {
      throw new RuntimeException("회원가입 실패");
    }

    // 4. 생성된 사용자 정보 반환
    return newUser;
  }

  /**
   * 로그인 처리
   *
   * @param loginDto 로그인 정보
   * @return 인증된 사용자 정보
   */
  @Override
  public UserAuthDto login(LoginDto loginDto) {
    // 1. 이메일로 사용자 조회
    Optional<UserAuthDto> userOpt = userMapper.findByEmail(loginDto.getEmail());
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("존재하지 않는 이메일입니다: " + loginDto.getEmail());
    }

    UserAuthDto user = userOpt.get();

    // 2. 비밀번호 검증
    if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
    }

    // 3. 계정 상태 체크
    if (!"ACTIVE".equals(user.getStatus())) {
      throw new IllegalStateException("비활성화된 계정입니다");
    }

    return user;
  }

  /**
   * 사용자 조회 (이메일 또는 userId로)
   *
   * @param identifier 이메일 또는 userId
   * @return 사용자 정보
   */
  @Override
  public UserAuthDto loadUser(String identifier) {
    // 1. 이메일 형식인지 확인 (@ 포함 여부)
    if (identifier.contains("@")) {
      return userMapper
          .findByEmail(identifier)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + identifier));
    } else {
      return userMapper
          .findByUserId(identifier)
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + identifier));
    }
  }
}
