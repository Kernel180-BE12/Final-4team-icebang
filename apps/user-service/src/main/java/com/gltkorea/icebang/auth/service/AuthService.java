package com.gltkorea.icebang.auth.service;

import com.gltkorea.icebang.auth.dto.LoginRequest;
import com.gltkorea.icebang.auth.dto.external.AuthResponse;
import com.gltkorea.icebang.domain.user.model.Users;

public interface AuthService {
  AuthResponse login(LoginRequest request);

  void logout(String token);

  Users loadUser(String identifier);
}
