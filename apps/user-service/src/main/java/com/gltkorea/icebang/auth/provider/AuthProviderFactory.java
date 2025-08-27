package com.gltkorea.icebang.auth.provider;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.gltkorea.icebang.auth.dto.AuthRequestWrapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthProviderFactory {

  private final Map<String, AuthProvider<? extends AuthRequestWrapper>> providers;

  /**
   * providerKey에 해당하는 AuthProvider 반환
   *
   * @param providerKey "google", "naver", "default" 등, enum으로 refactoring 필요
   * @return AuthProvider
   */
  public AuthProvider<? extends AuthRequestWrapper> getProvider(String providerKey) {
    AuthProvider<? extends AuthRequestWrapper> provider = providers.get(providerKey.toLowerCase());
    if (provider == null) {
      throw new IllegalArgumentException("Unknown auth provider: " + providerKey);
    }
    return provider;
  }

  /**
   * OAuth2 전용 Provider 반환
   *
   * @param providerKey OAuth2 provider key
   * @return OAuth2AuthProvider
   */
  public OAuth2AuthProvider getOAuth2Provider(String providerKey) {
    AuthProvider<? extends AuthRequestWrapper> provider = getProvider(providerKey);
    if (!(provider instanceof OAuth2AuthProvider oauthProvider)) {
      throw new IllegalArgumentException(providerKey + " is not an OAuth2 provider");
    }
    return oauthProvider;
  }
}
