package com.gltkorea.icebang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.SecurityFilterChain;

import java.security.SecureRandom;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final Environment environment;

  @Bean
  public SecureRandom secureRandom() {
    return new SecureRandom();
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

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico").permitAll()

                    // 인증 관련 페이지들 허용
                    .requestMatchers("/login", "/signup", "/reset-password").permitAll()
                    .requestMatchers("/api/v0/auth/**").permitAll()

                    // 공개 페이지들 허용
                    .requestMatchers("/", "/home").permitAll()

                    // 에러 페이지 허용
                    .requestMatchers("/error", "/error/**").permitAll()

                    // 특정 보호가 필요한 경로만 인증 요구
                    .requestMatchers("/mypage/**").authenticated()         // 마이페이지
                    .requestMatchers("/api/v0/user/**").authenticated()       // 사용자 전용 API
                    .requestMatchers("/order/**").authenticated()          // 주문 관련
                    .requestMatchers("/api/v0/order/**").authenticated()      // 주문 API
                    .requestMatchers("/cart/**").authenticated()           // 장바구니
                    .requestMatchers("/api/v0/cart/**").authenticated()       // 장바구니 API
                    .requestMatchers("/review/write/**").authenticated()   // 리뷰 작성
                    .requestMatchers("/api/v0/review/write/**").authenticated() // 리뷰 작성 API

                    // 나머지는 모두 허용
                    .anyRequest().permitAll()
            )
            .formLogin(form -> form
                            .loginPage("/login")           // 로그인 페이지
//                        .loginProcessingUrl("/api/v0/auth/login")  // 로그인 처리 URL
                            .defaultSuccessUrl("/home", true)  // 성공 시 이동
                            .failureUrl("/login?error=true")        // 실패 시 이동
                            .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            )
            .sessionManagement(session -> session
                    .maximumSessions(1)           // 동시 세션 1개만
                    .maxSessionsPreventsLogin(false)  // 새 로그인이 기존 세션 만료
            )
            .csrf(csrf -> csrf.disable());

    return http.build();
  }
}
