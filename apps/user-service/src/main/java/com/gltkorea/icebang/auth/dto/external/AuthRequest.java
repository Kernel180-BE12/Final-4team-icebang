package com.gltkorea.icebang.auth.dto.external;

import com.gltkorea.icebang.auth.dto.LoginRequest;

/** 로그인 시 사용 */
public class AuthRequest implements LoginRequest {
  private String email;
  private String password;

  @Override
  public String getIdentifier() {
    return email;
  }
}
