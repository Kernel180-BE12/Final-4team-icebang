package com.gltkorea.icebang.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OAuth2RequestWrapper implements AuthRequestWrapper {
  private Oauth2CallbackContent callbackContent;
}
