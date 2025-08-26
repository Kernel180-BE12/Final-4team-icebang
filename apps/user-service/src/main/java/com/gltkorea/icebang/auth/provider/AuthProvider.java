package com.gltkorea.icebang.auth.provider;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.gltkorea.icebang.auth.dto.external.AuthRequest;

public interface AuthProvider {

  /**
   * 인증 수행
   *
   * @param request AuthRequest
   * @return Authentication 객체
   */
  Authentication authenticate(AuthRequest request) throws AuthenticationException;

  /**
   * 이 Provider가 요청을 처리할 수 있는지 여부
   *
   * <p>AuthRequest Dto 필드에 따라 처리 유무 return
   *
   * @param request AuthRequest
   * @return true: 처리 가능
   */
  boolean supports(AuthRequest request);
}
