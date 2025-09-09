package com.gltkorea.icebang.domain.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.gltkorea.icebang.common.dto.ApiResponse;
import com.gltkorea.icebang.domain.auth.model.AuthCredential;
import com.gltkorea.icebang.domain.user.dto.CheckEmailRequest;
import com.gltkorea.icebang.domain.user.dto.CheckEmailResponse;
import com.gltkorea.icebang.domain.user.dto.UserProfileResponseDto;
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

  @GetMapping("/me")
  public ApiResponse<UserProfileResponseDto> getUserProfile(
      @AuthenticationPrincipal AuthCredential user) {
    return ApiResponse.success(UserProfileResponseDto.from(user));
  }
}
