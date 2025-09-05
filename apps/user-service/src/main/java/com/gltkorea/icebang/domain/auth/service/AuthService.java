package com.gltkorea.icebang.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gltkorea.icebang.common.utils.RandomPasswordGenerator;
import com.gltkorea.icebang.domain.auth.dto.RegisterDto;
import com.gltkorea.icebang.domain.email.dto.EmailRequest;
import com.gltkorea.icebang.domain.email.service.EmailService;
import com.gltkorea.icebang.mapper.AuthMapper;

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
    String randomPassword = passwordGenerator.generate();
    String hashedPassword = passwordEncoder.encode(randomPassword);

    registerDto.setPassword(hashedPassword);

    // @TODO:: Auth mapper 호출하여 insert

    EmailRequest emailRequest =
        EmailRequest.builder()
            .to(registerDto.getEmail())
            .subject("[ice-bang] 비밀번호")
            .body(randomPassword)
            .build();

    emailService.send(emailRequest);
  }
}
