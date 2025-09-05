package com.gltkorea.icebang.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gltkorea.icebang.common.dto.ApiResponse;
import com.gltkorea.icebang.domain.auth.dto.RegisterDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v0/auth")
@RequiredArgsConstructor
public class AuthController {

  @PostMapping("/register")
  public ApiResponse<String> register(@Valid @RequestBody RegisterDto registerDto) {

    throw new RuntimeException("Not implemented");
  }
}
