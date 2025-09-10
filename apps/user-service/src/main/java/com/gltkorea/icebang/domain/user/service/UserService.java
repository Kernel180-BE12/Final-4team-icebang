package com.gltkorea.icebang.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gltkorea.icebang.domain.user.dto.CheckEmailRequest;
import com.gltkorea.icebang.domain.user.mapper.UserMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  public Boolean isExistEmail(@Valid CheckEmailRequest request) {
    return userMapper.existsByEmail(request.getEmail());
  }
}
