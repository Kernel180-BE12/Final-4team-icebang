package com.gltkorea.icebang.config.security;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
                    .requestMatchers("/auth/login", "/auth/logout")
                    .permitAll()
                    .requestMatchers(SecurityEndpoints.DATA_ADMIN.getMatchers())
                    .hasAuthority("SUPER_ADMIN")
                    .requestMatchers(SecurityEndpoints.DATA_ENGINEER.getMatchers())
                    .hasAnyAuthority(
                        "SUPER_ADMIN", "ADMIN", "SENIOR_DATA_ENGINEER", "DATA_ENGINEER")
                    .requestMatchers(SecurityEndpoints.ANALYST.getMatchers())
                    .hasAnyAuthority(
                        "SUPER_ADMIN",
                        "ADMIN",
                        "SENIOR_DATA_ENGINEER",
                        "DATA_ENGINEER",
                        "SENIOR_DATA_ANALYST",
                        "DATA_ANALYST",
                        "VIEWER")
                    .requestMatchers(SecurityEndpoints.OPS.getMatchers())
                    .hasAnyAuthority(
                        "SUPER_ADMIN", "ADMIN", "SENIOR_DATA_ENGINEER", "DATA_ENGINEER")
                    .requestMatchers(SecurityEndpoints.USER.getMatchers())
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(
            logout -> logout.logoutUrl("/auth/logout").logoutSuccessUrl("/auth/login").permitAll())
        .csrf(AbstractHttpConfigurer::disable) // API 사용을 위해 CSRF 비활성화
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
