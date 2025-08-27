package com.gltkorea.icebang.auth.provider;

/*
@RequiredArgsConstructor 추가
AuthService 의존성 주입
supports() 메서드 로직 구현
authenticate() 메서드 로직 구현
createAuthentication() 헬퍼 메서드 추가
supports(): SignUpDto나 LoginDto 존재 여부로 처리 가능성 판단
authenticate(): 회원가입 후 자동 로그인 또는 단순 로그인 처리
createAuthentication(): Spring Security의 Authentication 객체 생성
ROLE_USER 기본 권한 부여
 */

import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.gltkorea.icebang.auth.dto.DefaultRequestWrapper;
import com.gltkorea.icebang.auth.service.AuthService;
import com.gltkorea.icebang.dto.UserAuthDto;

import lombok.RequiredArgsConstructor;

@Component("default")
@RequiredArgsConstructor
public class DefaultProvider implements AuthProvider<DefaultRequestWrapper> {

  private final AuthService authService;

  /**
   * 요청이 Default Provider로 처리 가능한지 검증
   *
   * @param request 요청 래퍼
   * @return 처리 가능 여부
   */
  @Override
  public boolean supports(DefaultRequestWrapper request) {
    // SignUpDto나 LoginDto 중 하나라도 있으면 처리 가능
    if (request.getLoginDto() == null && request.getSignUpDto() == null) {
      return false;
    }
    return true;
  }

  /**
   * 인증 처리 (회원가입 + 로그인 또는 로그인만)
   *
   * @param request 요청 래퍼
   * @return Authentication 객체
   */
  @Override
  public Authentication authenticate(DefaultRequestWrapper request) {
    UserAuthDto user;

    // 1. 회원가입 요청 처리
    if (request.getSignUpDto() != null) {
      // 회원가입 후 자동 로그인
      user = authService.signUp(request.getSignUpDto());
    }
    // 2. 로그인 요청 처리
    else if (request.getLoginDto() != null) {
      user = authService.login(request.getLoginDto());
    }
    // 3. 잘못된 요청
    else {
      throw new IllegalArgumentException("SignUpDto 또는 LoginDto가 필요합니다");
    }

    // 4. Authentication 객체 생성
    return createAuthentication(user);
  }

  /**
   * UserAuthDto를 Authentication 객체로 변환
   *
   * @param user 사용자 정보
   * @return Authentication 객체
   */
  private Authentication createAuthentication(UserAuthDto user) {
    // @TODO
    // 이미 auth dto는 데이터 베이스에서 읽어온 상태
    // 그럼 여기서 ROLE_USER가 아닌 실제 role을 넣어야함.
    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    return new UsernamePasswordAuthenticationToken(
        user.getEmail(), // principal (사용자 식별자)
        null, // credentials (비밀번호는 null로 - 이미 인증 완료)
        authorities // authorities (권한)
        );
  }
}
