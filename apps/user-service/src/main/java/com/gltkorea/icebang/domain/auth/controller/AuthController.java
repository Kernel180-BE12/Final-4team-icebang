package com.gltkorea.icebang.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gltkorea.icebang.common.dto.ApiResponse;
import com.gltkorea.icebang.domain.auth.dto.RegisterDto;
import com.gltkorea.icebang.domain.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v0/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  public ApiResponse<Void> register(@Valid @RequestBody RegisterDto registerDto) {
    authService.registerUser(registerDto);
    return ApiResponse.success(null);
  }
}
