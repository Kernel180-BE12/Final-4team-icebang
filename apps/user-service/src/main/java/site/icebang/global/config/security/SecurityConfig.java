package site.icebang.global.config.security;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.auth.service.AuthCredentialAdapter;
import site.icebang.global.config.security.endpoints.SecurityEndpoints;
import site.icebang.global.handler.exception.RestAccessDeniedHandler;
import site.icebang.global.handler.exception.RestAuthenticationEntryPoint;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final Environment environment;
  private final AuthCredentialAdapter userDetailsService;
  private final ObjectMapper objectMapper;
  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
  private final RestAccessDeniedHandler restAccessDeniedHandler;

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
                    .requestMatchers("/v0/auth/check-session")
                    .authenticated()
                    .requestMatchers(SecurityEndpoints.DATA_ADMIN.getMatchers())
                    .hasRole("SUPER_ADMIN") // hasAuthority -> hasRole
                    .requestMatchers(SecurityEndpoints.DATA_ENGINEER.getMatchers())
                    .hasAnyRole(
                        "SUPER_ADMIN",
                        "SYSTEM_ADMIN",
                        "AI_ENGINEER",
                        "DATA_SCIENTIST",
                        "CRAWLING_ENGINEER",
                        "TECH_LEAD",
                        "DEVOPS")
                    .requestMatchers(SecurityEndpoints.ANALYST.getMatchers())
                    .hasAnyRole(
                        "SUPER_ADMIN",
                        "SYSTEM_ADMIN",
                        "ORG_ADMIN",
                        "DATA_SCIENTIST",
                        "MARKETING_ANALYST",
                        "QA_ENGINEER",
                        "PROJECT_MANAGER",
                        "PRODUCT_OWNER",
                        "USER")
                    .requestMatchers(SecurityEndpoints.OPS.getMatchers())
                    .hasAnyRole(
                        "SUPER_ADMIN",
                        "SYSTEM_ADMIN",
                        "WORKFLOW_ADMIN",
                        "OPERATIONS_MANAGER",
                        "DEVOPS",
                        "TECH_LEAD")
                    .requestMatchers(SecurityEndpoints.USER.getMatchers())
                    .hasAnyRole("SUPER_ADMIN", "SYSTEM_ADMIN", "ORG_ADMIN", "USER")
                    .anyRequest()
                    .authenticated())
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(
            logout -> logout.logoutUrl("/auth/logout").logoutSuccessUrl("/auth/login").permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler))
        .build();
  }

  @Bean
  public PasswordEncoder bCryptPasswordEncoder() {
    String[] activeProfiles = environment.getActiveProfiles();

    for (String profile : activeProfiles) {
      if ("develop".equals(profile) || profile.contains("test") || "production".equals(profile)) {
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
