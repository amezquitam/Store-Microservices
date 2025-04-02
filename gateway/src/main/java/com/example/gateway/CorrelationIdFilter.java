package com.example.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CorrelationIdFilter extends AbstractGatewayFilterFactory<CorrelationIdFilter.Config> {

    public static class Config {
    }

    public CorrelationIdFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            HttpHeaders headers = exchange.getRequest().getHeaders();
            headers.add("X-Correlation-Id", UUID.randomUUID().toString());

            return chain.filter(exchange);
        };
    }
}