package com.gltkorea.icebang.auth.service;

import com.gltkorea.icebang.auth.dto.LoginDto;
import com.gltkorea.icebang.auth.dto.SignUpDto;
import com.gltkorea.icebang.domain.user.model.Users;

public interface AuthService {
  Users signUp(SignUpDto signUpDto);

  Users login(LoginDto loginDto);

  Users loadUser(String identifier);
}
