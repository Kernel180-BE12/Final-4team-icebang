package com.gltkorea.icebang.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gltkorea.icebang.common.utils.RandomPasswordGenerator;
import com.gltkorea.icebang.domain.auth.dto.RegisterDto;
import com.gltkorea.icebang.mapper.AuthMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
  private final AuthMapper authMapper;
  private final RandomPasswordGenerator passwordGenerator;
  private final PasswordEncoder passwordEncoder;

  public void registerUser(RegisterDto registerDto) {
    String randomPassword = passwordGenerator.generate();
    String hashedPassword = passwordEncoder.encode(randomPassword);

    registerDto.setPassword(hashedPassword);

    // @TODO:: UserService 호출하여 사용자 등록
  }
}
