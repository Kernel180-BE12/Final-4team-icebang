package com.gltkorea.icebang.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> register(@Valid @RequestBody RegisterDto registerDto) {
    authService.registerUser(registerDto);
    return ApiResponse.success(null);
  }
}
