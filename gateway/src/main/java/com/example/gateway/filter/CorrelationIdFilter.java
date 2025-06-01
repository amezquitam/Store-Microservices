package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdFilter implements GatewayFilter, Ordered {

    private static final String CORRELATION_ID = "X-Correlation-ID";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = getOrGenerateCorrelationId(exchange);
        log.info("CorrelationId: {}", correlationId);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CORRELATION_ID, correlationId)
                .build();

        // También añadir el header a la respuesta
        exchange.getResponse().getHeaders().add(CORRELATION_ID, correlationId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }


    private String getOrGenerateCorrelationId(ServerWebExchange exchange) {
        String existing = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);
        return (existing == null || existing.isEmpty())
                ? UUID.randomUUID().toString()
                : existing;
    }

    @Override
    public int getOrder() {
        return -6; // Ajusta la prioridad si es necesario
    }
}
