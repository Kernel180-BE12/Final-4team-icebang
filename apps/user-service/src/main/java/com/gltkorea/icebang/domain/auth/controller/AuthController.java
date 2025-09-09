package com.gltkorea.icebang.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.gltkorea.icebang.common.dto.ApiResponse;
import com.gltkorea.icebang.domain.auth.dto.LoginRequestDto;
import com.gltkorea.icebang.domain.auth.dto.RegisterDto;
import com.gltkorea.icebang.domain.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v0/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final AuthenticationManager authenticationManager;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> register(@Valid @RequestBody RegisterDto registerDto) {
    authService.registerUser(registerDto);
    return ApiResponse.success(null);
  }

  @PostMapping("/login")
  public ApiResponse<?> login(@RequestBody LoginRequestDto request) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

    Authentication auth = authenticationManager.authenticate(token);
    return ApiResponse.success(auth);
  }
}
