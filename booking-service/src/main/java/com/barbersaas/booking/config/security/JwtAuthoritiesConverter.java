package com.barbersaas.booking.config.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  private static final String ROLES_CLAIM = "roles";
  private static final String ROLE_PREFIX = "ROLE_";

  private final JwtGrantedAuthoritiesConverter scopeAuthoritiesConverter =
      new JwtGrantedAuthoritiesConverter();

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();

    Collection<GrantedAuthority> scopeAuthorities = scopeAuthoritiesConverter.convert(jwt);

    if (scopeAuthorities != null) {
      authorities.addAll(scopeAuthorities);
    }

    List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);

    if (roles != null) {
      roles.stream()
          .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
          .forEach(authorities::add);
    }

    return authorities;
  }
}
