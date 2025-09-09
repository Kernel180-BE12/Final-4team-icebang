package com.gltkorea.icebang.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gltkorea.icebang.domain.auth.dto.RegisterDto;
import com.gltkorea.icebang.domain.user.dto.CheckEmailRequest;
import com.gltkorea.icebang.entity.Users;
import com.gltkorea.icebang.mapper.UserMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserMapper userMapper;

  public void registerUser(RegisterDto registerDto) {
    Users user =
        Users.builder()
            .name(registerDto.getName())
            .email(registerDto.getEmail())
            .password(registerDto.getPassword())
            .status("PENDING")
            .build();
  }

  @Transactional(readOnly = true)
  public Boolean isExistEmail(@Valid CheckEmailRequest request) {
    return userMapper.existsByEmail(request.getEmail());
  }
}
