package com.example.gateway;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        exchange.mutate().request(
                builder -> builder.header("X-Correlation-Id", UUID.randomUUID().toString())
        );

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
