package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AuthFilter implements GatewayFilterFactory<AuthFilter.Config> {

    public static class Config {
        // config para roles dinamicos
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.info("Usuario no autenticado");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            Jwt jwt = (Jwt) authentication.getPrincipal();
            List<String> roles = jwt.getClaimAsStringList("realm_access.roles");

            if (roles == null || !roles.contains("ADMIN")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                log.info("Usuario no administrador");
                return exchange.getResponse().setComplete();
            }
            log.info("Usuario autenticado");
            return chain.filter(exchange);
        };
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public Config newConfig() {
        return new Config();
    }

    @Override
    public String name() {
        return "AuthenticationFilter";
    }
}

