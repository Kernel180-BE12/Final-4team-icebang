package com.gltkorea.icebang.config.security;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.gltkorea.icebang.config.security.endpoints.SecurityEndpoints;
import com.gltkorea.icebang.domain.auth.service.AuthCredentialAdapter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final Environment environment;
  private final AuthCredentialAdapter userDetailsService;

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(bCryptPasswordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

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
      if ("develop".equals(profile) || "test".equals(profile)) {
        return NoOpPasswordEncoder.getInstance();
      }
    }
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOrigin("http://localhost:3000"); // 프론트 주소
    config.addAllowedOrigin("https://admin.icebang.site"); // 프론트 주소
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.setAllowCredentials(true); // 세션 쿠키 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsFilter(source);
  }
}
