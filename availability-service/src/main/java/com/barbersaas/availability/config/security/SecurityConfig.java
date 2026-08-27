package com.barbersaas.availability.config.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

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
                    .requestMatchers("/api/v1/internal/**")
                    .access(
                        new WebExpressionAuthorizationManager(
                            "hasRole('SERVICE') and hasAuthority('SCOPE_internal')"))
                    .requestMatchers(
                        org.springframework.http.HttpMethod.GET, "/api/v1/availability/**")
                    .access(
                        new WebExpressionAuthorizationManager(
                            "(hasRole('CUSTOMER') or hasRole('BARBER') or hasRole('ADMIN')) "
                                + "and hasAuthority('SCOPE_availability.read')"))
                    .requestMatchers("/api/v1/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer.jwt(
                    jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
        .build();
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new JwtAuthoritiesConverter());

    return converter;
  }

  @Bean
  JwtDecoder jwtDecoder(@Value("${barbersaas.security.jwt.secret}") String jwtSecret) {
    SecretKey secretKey =
        new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    return NimbusJwtDecoder.withSecretKey(secretKey).build();
  }
}
