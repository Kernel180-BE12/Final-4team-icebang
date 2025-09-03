package com.gltkorea.icebang.config.security;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.gltkorea.icebang.config.security.endpoints.SecurityEndpoints;
import com.gltkorea.icebang.domain.auth.service.AuthUserDetailService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final Environment environment;
  // 우리가 만든 AuthUserDetailService를 주입받음
  private final AuthUserDetailService authUserDetailService;

  @Bean
  public SecureRandom secureRandom() {
    return new SecureRandom();
  }

  /**
   * HTTP 보안 설정 및 URL별 권한 설정
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth ->
                    auth
                            // 공개 접근 허용 경로들
                            .requestMatchers(SecurityEndpoints.PUBLIC.getMatchers())
                            .permitAll()

                            // 로그인/로그아웃 경로 허용
                            .requestMatchers("/auth/login", "/auth/logout")
                            .permitAll()

                            // 관리자 전용 경로 (사용자 관리 등)
                            .requestMatchers("/admin/users/**")
                            .hasAuthority("SUPER_ADMIN")

                            // 데이터 관리자 경로
                            .requestMatchers(SecurityEndpoints.DATA_ADMIN.getMatchers())
                            .hasAuthority("SUPER_ADMIN")

                            // 데이터 엔지니어 경로
                            .requestMatchers(SecurityEndpoints.DATA_ENGINEER.getMatchers())
                            .hasAnyAuthority("SUPER_ADMIN", "ADMIN", "SENIOR_DATA_ENGINEER", "DATA_ENGINEER")

                            // 분석가 경로
                            .requestMatchers(SecurityEndpoints.ANALYST.getMatchers())
                            .hasAnyAuthority("SUPER_ADMIN", "ADMIN", "SENIOR_DATA_ENGINEER", "DATA_ENGINEER",
                                    "SENIOR_DATA_ANALYST", "DATA_ANALYST", "VIEWER")

                            // 운영 관련 경로
                            .requestMatchers(SecurityEndpoints.OPS.getMatchers())
                            .hasAnyAuthority("SUPER_ADMIN", "ADMIN", "SENIOR_DATA_ENGINEER", "DATA_ENGINEER")

                            // 일반 사용자 경로
                            .requestMatchers(SecurityEndpoints.USER.getMatchers())
                            .authenticated()

                            // 그 외 모든 요청은 인증 필요
                            .anyRequest()
                            .authenticated()
            )
            .formLogin(AbstractHttpConfigurer::disable)  // 기본 로그인 폼 비활성화 (REST API 사용)
            .logout(logout ->
                    logout
                            .logoutUrl("/auth/logout")
                            .logoutSuccessUrl("/auth/login")
                            .permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable)  // REST API를 위해 CSRF 비활성화
            .build();
  }

  /**
   * 비밀번호 암호화 설정
   * - dev/test 환경: 평문 비밀번호 사용 (개발 편의성)
   * - 운영 환경: BCrypt 암호화 사용
   */
  @Bean
  public PasswordEncoder bCryptPasswordEncoder() {
    String[] activeProfiles = environment.getActiveProfiles();

    for (String profile : activeProfiles) {
      if ("dev".equals(profile) || "test".equals(profile)) {
        return NoOpPasswordEncoder.getInstance();  // 개발/테스트시 평문 비밀번호
      }
    }
    return new BCryptPasswordEncoder();  // 운영시 암호화
  }

  /**
   * 인증 제공자 설정
   * - 우리가 만든 AuthUserDetailService와 PasswordEncoder 연결
   * - Spring Security가 로그인 처리 시 이 설정을 사용
   */
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    // 사용자 정보 로드 서비스 설정
    authProvider.setUserDetailsService(authUserDetailService);

    // 비밀번호 암호화 방식 설정
    authProvider.setPasswordEncoder(bCryptPasswordEncoder());

    // 사용자를 찾을 수 없을 때 예외 정보 숨김 (보안상 이유)
    authProvider.setHideUserNotFoundExceptions(true);

    return authProvider;
  }

  /**
   * 인증 관리자 설정
   * - 로그인 처리를 위한 AuthenticationManager 생성
   * - Controller에서 수동 로그인 처리 시 사용
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
          throws Exception {
    return config.getAuthenticationManager();
  }
}