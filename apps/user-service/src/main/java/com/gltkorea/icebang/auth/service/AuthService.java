package com.gltkorea.icebang.auth.service;

import com.gltkorea.icebang.auth.dto.LoginDto;
import com.gltkorea.icebang.auth.dto.SignUpDto;
import com.gltkorea.icebang.dto.UserAuthDto;

public interface AuthService {
  UserAuthDto signUp(SignUpDto signUpDto); // 변경!

  UserAuthDto login(LoginDto loginDto); // 변경!

  UserAuthDto loadUser(String identifier); // 변경!
}
