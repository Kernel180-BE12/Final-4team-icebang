package com.gltkorea.icebang.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gltkorea.icebang.domain.auth.dto.LoginRequest;
import com.gltkorea.icebang.domain.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
    authService.login(loginRequest.getUserName(), loginRequest.getPassword());

    request.getSession(true);
    return ResponseEntity.ok("success"); // @TODO:: 201로 변경
  }
}
