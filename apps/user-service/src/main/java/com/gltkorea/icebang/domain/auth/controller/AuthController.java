package com.gltkorea.icebang.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import com.gltkorea.icebang.common.dto.ApiResponse;
import com.gltkorea.icebang.domain.auth.dto.LoginRequestDto;
import com.gltkorea.icebang.domain.auth.dto.RegisterDto;
import com.gltkorea.icebang.domain.auth.model.AuthCredential;
import com.gltkorea.icebang.domain.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
  public ApiResponse<?> login(
      @RequestBody LoginRequestDto request, HttpServletRequest httpRequest) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

    Authentication auth = authenticationManager.authenticate(token);

    SecurityContextHolder.getContext().setAuthentication(auth);

    HttpSession session = httpRequest.getSession(true);
    session.setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        SecurityContextHolder.getContext());

    return ApiResponse.success(auth);
  }

  @GetMapping("/check-session")
  public ApiResponse<Boolean> checkSession(@AuthenticationPrincipal AuthCredential user) {
    return ApiResponse.success(user != null);
  }

  @GetMapping("/permissions")
  public ApiResponse<AuthCredential> getPermissions(@AuthenticationPrincipal AuthCredential user) {
    return ApiResponse.success(user);
  }
}
