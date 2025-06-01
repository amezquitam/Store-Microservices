package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AuthFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(authentication -> {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    String username = jwt.getClaimAsString("preferred_username");
                    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                    List<String> roles = realmAccess != null
                            ? (List<String>) realmAccess.get("roles")
                            : Collections.emptyList();
                    String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");

                    log.info("✅ Usuario autenticado: '{}' con roles {} [correlationId={}]", username, roles, correlationId);
                    return chain.filter(exchange);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    if (exchange.getResponse().isCommitted()) {
                        log.warn("⚠️ Intento de switch después de una respuesta ya enviada (committed)");
                        return Mono.empty();
                    }
                    log.warn("🔒 Usuario no autenticado desde IP: {}", exchange.getRequest().getRemoteAddress());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }));
    }

    @Override
    public int getOrder() {
        return -5;
    }
}


