package com.barbersaas.booking.config.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(
                        "/actuator/health/**",
                        "/actuator/metrics/**",
                        "/actuator/prometheus",
                        "/livez",
                        "/readyz")
                    .permitAll()
                    .requestMatchers("/api/v1/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> {}))
        .build();
  }

  @Bean
  JwtDecoder jwtDecoder(@Value("${barbersaas.security.jwt.secret}") String jwtSecret) {
    SecretKey secretKey =
        new SecretKeySpec(
            jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");

    return NimbusJwtDecoder.withSecretKey(secretKey).build();
  }
}
