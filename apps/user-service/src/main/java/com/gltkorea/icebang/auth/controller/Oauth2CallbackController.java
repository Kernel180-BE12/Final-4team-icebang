package com.gltkorea.icebang.auth.controller;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v0/oauth2/callback")
@RequiredArgsConstructor
public class Oauth2CallbackController {

  @GetMapping("/kakao")
  public void handleKakaoCallback(@RequestParam String code) {
    //        OAuth2RequestWrapper wrapper = new OAuth2RequestWrapper(new
    // Oauth2CallbackContent("kakao", code));
    //        OAuth2AuthProvider provider = (OAuth2AuthProvider)
    // authProviderFactory.getProvider(providerKey);
    //        Authentication auth = provider.authenticate(wrapper);
  }
}
