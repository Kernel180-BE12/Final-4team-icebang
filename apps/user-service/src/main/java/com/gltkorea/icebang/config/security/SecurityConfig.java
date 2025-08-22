package com.gltkorea.icebang.config.security;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.gltkorea.icebang.config.security.endpoints.SecurityEndpoints;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final Environment environment;

  @Bean
  public SecureRandom secureRandom() {
    return new SecureRandom();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(SecurityEndpoints.PUBLIC.getMatchers())
                    .permitAll()
                    .requestMatchers(SecurityEndpoints.ADMIN.getMatchers())
                    .hasRole("ADMIN")
                    .requestMatchers(SecurityEndpoints.USER.getMatchers())
                    .hasRole("USER")
                    .anyRequest()
                    .authenticated())
        .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/").permitAll())
        .logout(logout -> logout.logoutSuccessUrl("/login").permitAll())
        .build();
  }

  @Bean
  public PasswordEncoder bCryptPasswordEncoder() {
    String[] activeProfiles = environment.getActiveProfiles();

    for (String profile : activeProfiles) {
      if ("dev".equals(profile) || "test".equals(profile)) {
        return NoOpPasswordEncoder.getInstance();
      }
    }
    return new BCryptPasswordEncoder();
  }
}
