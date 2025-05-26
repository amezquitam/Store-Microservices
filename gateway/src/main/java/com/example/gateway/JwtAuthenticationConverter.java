package com.example.gateway;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        System.err.println("=== JWT Claims ===");
        jwt.getClaims().forEach((k, v) -> System.err.println(k + ": " + v));

        Object realmAccess = jwt.getClaim("realm_access");
        System.err.println("realm_access: " + realmAccess);

        List<String> roles = Collections.emptyList();
        Object realmAccessObj = jwt.getClaimAsMap("realm_access");
        if (realmAccessObj != null) {
            Object rolesObj = ((java.util.Map<?, ?>) realmAccessObj).get("roles");
            if (rolesObj instanceof List<?>) {
                roles = ((List<?>) rolesObj).stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .collect(Collectors.toList());
            }
        }

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        AbstractAuthenticationToken token = new JwtAuthenticationToken(
                jwt,
                authorities,
                jwt.getClaimAsString("preferred_username"));

        return Mono.just(token);
    }
}
