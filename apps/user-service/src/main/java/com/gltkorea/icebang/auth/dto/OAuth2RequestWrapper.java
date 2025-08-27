package com.gltkorea.icebang.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class OAuth2RequestWrapper implements AuthRequestWrapper {
  private Oauth2CallbackContent callbackContent;
}
