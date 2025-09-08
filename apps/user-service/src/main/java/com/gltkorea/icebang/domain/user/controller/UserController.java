package com.gltkorea.icebang.domain.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gltkorea.icebang.common.dto.ApiResponse;
import com.gltkorea.icebang.domain.user.dto.CheckEmailRequest;
import com.gltkorea.icebang.domain.user.dto.CheckEmailResponse;
import com.gltkorea.icebang.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v0/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping("/check-email")
  public ApiResponse<CheckEmailResponse> checkEmailAvailable(
      @Valid @RequestBody CheckEmailRequest request) {
    Boolean available = !userService.isExistEmail(request);
    String message = available.equals(Boolean.TRUE) ? "사용 가능한 이메일입니다." : "이미 가입된 이메일입니다.";

    return ApiResponse.success(CheckEmailResponse.builder().available(available).build(), message);
  }
}
