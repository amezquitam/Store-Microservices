package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

@Component
public class CorrelationIdGatewayFilterFactory extends AbstractGatewayFilterFactory<CorrelationIdGatewayFilterFactory.Config> {

    private static final String CORRELATION_ID = "X-Correlation-ID";

    public CorrelationIdGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String correlationId = getOrGenerateCorrelationId(exchange);
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header(CORRELATION_ID, correlationId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    private String getOrGenerateCorrelationId(ServerWebExchange exchange) {
        String existing = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);
        return (existing == null || existing.isEmpty())
                ? UUID.randomUUID().toString()
                : existing;
    }

    public static class Config {
        // Vacío por ahora, pero puedes agregar configuraciones si lo deseas
    }
}