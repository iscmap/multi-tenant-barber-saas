package com.barbersaas.availability.config.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAuthoritiesConverterTest {

  private final JwtAuthoritiesConverter converter = new JwtAuthoritiesConverter();

  @Test
  void shouldConvertRolesAndScopesToAuthorities() {
    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("customer-1")
            .claim("roles", List.of("CUSTOMER"))
            .claim("scope", "availability.read")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    Set<String> authorities =
        converter.convert(jwt).stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    assertTrue(authorities.contains("ROLE_CUSTOMER"));
    assertTrue(authorities.contains("SCOPE_availability.read"));
  }
}
