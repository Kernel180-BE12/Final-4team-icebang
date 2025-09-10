package com.gltkorea.icebang.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gltkorea.icebang.common.utils.RandomPasswordGenerator;
import com.gltkorea.icebang.domain.auth.dto.RegisterDto;
import com.gltkorea.icebang.domain.auth.mapper.AuthMapper;
import com.gltkorea.icebang.domain.email.dto.EmailRequest;
import com.gltkorea.icebang.domain.email.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
  private final AuthMapper authMapper;
  private final RandomPasswordGenerator passwordGenerator;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  public void registerUser(RegisterDto registerDto) {
    if (authMapper.existsByEmail(registerDto.getEmail())) {
      throw new IllegalArgumentException("이미 가입된 이메일입니다.");
    }
    String randomPassword = passwordGenerator.generate();
    String hashedPassword = passwordEncoder.encode(randomPassword);

    registerDto.setPassword(hashedPassword);
    registerDto.setStatus("PENDING");

    authMapper.insertUser(registerDto);

    // 2. user_organizations insert → userOrgId 반환
    authMapper.insertUserOrganization(registerDto);

    // 3. user_roles insert (foreach)
    if (registerDto.getRoleIds() != null && !registerDto.getRoleIds().isEmpty()) {
      authMapper.insertUserRoles(registerDto);
    }

    EmailRequest emailRequest =
        EmailRequest.builder()
            .to(registerDto.getEmail())
            .subject("[ice-bang] 비밀번호")
            .body(randomPassword)
            .build();

    emailService.send(emailRequest);
  }
}
