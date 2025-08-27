package com.gltkorea.icebang.auth.service;

import org.springframework.stereotype.Service;

import com.gltkorea.icebang.auth.dto.LoginDto;
import com.gltkorea.icebang.auth.dto.SignUpDto;
import com.gltkorea.icebang.domain.user.model.Users;

@Service
public class AuthServiceImpl implements AuthService {
  @Override
  public Users signUp(SignUpDto signUpDto) {
    return null;
  }

  @Override
  public Users login(LoginDto loginDto) {
    return null;
  }

  @Override
  public Users loadUser(String identifier) {
    return null;
  }
}
