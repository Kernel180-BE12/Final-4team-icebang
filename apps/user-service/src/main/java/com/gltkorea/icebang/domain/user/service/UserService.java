package com.gltkorea.icebang.domain.user.service;

import org.springframework.stereotype.Service;

import com.gltkorea.icebang.domain.auth.dto.RegisterDto;
import com.gltkorea.icebang.entity.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  //    private final UserMapper userMapper;

  public void registerUser(RegisterDto registerDto) {
    Users user =
        Users.builder()
            .name(registerDto.getUserName())
            .email(registerDto.getEmail())
            .password(registerDto.getPassword())
            .status("PENDING")
            .build();
  }
}
