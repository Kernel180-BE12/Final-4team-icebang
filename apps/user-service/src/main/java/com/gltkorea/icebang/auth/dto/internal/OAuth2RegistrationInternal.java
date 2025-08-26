package com.gltkorea.icebang.auth.dto.internal;

import com.gltkorea.icebang.auth.dto.LoginRequest;

import lombok.Getter;

@Getter
public abstract class OAuth2RegistrationInternal implements LoginRequest {
  private final String provider;
  private final String oauthId;
  private final String email;

  protected OAuth2RegistrationInternal(String provider, String oauthId, String email) {
    this.provider = provider;
    this.oauthId = oauthId;
    this.email = email;
  }

  @Override
  public String getIdentifier() {
    return oauthId; // OAuth는 ID로 식별
  }
}
