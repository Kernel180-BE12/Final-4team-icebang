package com.gltkorea.icebang.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gltkorea.icebang.auth.dto.DefaultRequestWrapper;
import com.gltkorea.icebang.auth.dto.LoginDto;
import com.gltkorea.icebang.auth.dto.SignUpDto;
import com.gltkorea.icebang.auth.provider.AuthProvider;
import com.gltkorea.icebang.auth.provider.AuthProviderFactory;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v0/auth")
public class AuthController {
  private final AuthProviderFactory authProviderFactory;

  @PostMapping("/signup")
  public ResponseEntity<?> signUp(@RequestBody SignUpDto signUpDto) {
    // 1. Wrapper DTO 생성
    DefaultRequestWrapper wrapper = DefaultRequestWrapper.builder().signUpDto(signUpDto).build();

    // 2. Factory에서 Provider 선택
    @SuppressWarnings("unchecked")
    AuthProvider<DefaultRequestWrapper> provider =
        (AuthProvider<DefaultRequestWrapper>) authProviderFactory.getProvider("default");

    // 3. Provider에 인증 위임 (Provider 내부에서 signUp + login 처리)
    Authentication auth = provider.authenticate(wrapper);

    // 4. 결과 반환
    return ResponseEntity.status(201).body(auth);
  }

  @PostMapping("/signin")
  public ResponseEntity<?> signIn(@RequestBody LoginDto loginDto) {
    DefaultRequestWrapper wrapper = DefaultRequestWrapper.builder().loginDto(loginDto).build();
    @SuppressWarnings("unchecked")
    AuthProvider<DefaultRequestWrapper> provider =
        (AuthProvider<DefaultRequestWrapper>) authProviderFactory.getProvider("default");
    Authentication auth = provider.authenticate(wrapper);
    return ResponseEntity.ok(auth);
  }
}
