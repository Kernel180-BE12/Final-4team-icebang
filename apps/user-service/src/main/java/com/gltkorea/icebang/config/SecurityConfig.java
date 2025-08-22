package com.gltkorea.icebang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final Environment environment;

  @Bean
  public PasswordEncoder bCryptPasswordEncoder() {
    String[] activeProfiles = environment.getActiveProfiles();

    for (String profile : activeProfiles) {
      if ("dev".equals(profile)) {
        return NoOpPasswordEncoder.getInstance();
      }
    }
    return new BCryptPasswordEncoder();
  }
}
