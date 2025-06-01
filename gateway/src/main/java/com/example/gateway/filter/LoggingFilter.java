package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class LoggingFilter implements GatewayFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();
        String queryParams = request.getQueryParams().entrySet().stream()
                .map(entry -> "%s=[%s]".formatted(entry.getKey(), String.join(",", entry.getValue())))
                .collect(Collectors.joining("; ", "{", "}"));

        logger.info("📥 Solicitud recibida: [{}] {}", queryParams, path);

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> logger.info("📤 Respuesta enviada para: [{}] {}", queryParams, path));
    }

    @Override
    public int getOrder() {
        return -4; // Puedes ajustar el orden si lo necesitas
    }
}
