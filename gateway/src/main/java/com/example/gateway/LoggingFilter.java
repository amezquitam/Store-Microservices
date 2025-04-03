package com.example.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getQueryParams().entrySet().stream()
                .map(en -> "%s: [%s]".formatted(en.getKey(), String.join(",", en.getValue())))
                .collect(Collectors.joining("; ", "{", "}"));

        logger.info("Solicitud recibida: [{}] {}", method, path);

        return chain.filter(exchange).doOnSuccess(aVoid -> logger.info("Respuesta para: [{}] {}", method, path));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
