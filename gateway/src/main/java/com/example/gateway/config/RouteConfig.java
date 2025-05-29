package com.example.gateway.config;

import com.example.gateway.filter.CachingFilter;
import com.example.gateway.filter.CorrelationIdGatewayFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {
    @Autowired
    private final CorrelationIdGatewayFilterFactory correlationIdGatewayFilterFactory;
    private final CachingFilter cachingFilter;

    public RouteConfig(CorrelationIdGatewayFilterFactory correlationIdGatewayFilterFactory, CachingFilter cachingFilter) {
        this.correlationIdGatewayFilterFactory = correlationIdGatewayFilterFactory;
        this.cachingFilter = cachingFilter;
    }

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("payment_route", r ->
                        r.path("/api/payments/**").filters(f -> f.filter(correlationIdGatewayFilterFactory.apply(new CorrelationIdGatewayFilterFactory.Config()))).uri("lb://payment-service"))
                .route("inventory_route", r ->
                        r.path("/api/inventories/**").filters(f -> f.filter(correlationIdGatewayFilterFactory.apply(new CorrelationIdGatewayFilterFactory.Config()))).uri("lb://inventory-service"))
                .route("product_route", r ->
                        r.path("/api/products/**").filters(f -> f
                                .filter(correlationIdGatewayFilterFactory.apply(new CorrelationIdGatewayFilterFactory.Config()))
                                .filter(cachingFilter)).uri("lb://product-service"))
                .route("order_route", r ->
                        r.path("/api/orders/**").filters(f -> f.filter(correlationIdGatewayFilterFactory.apply(new CorrelationIdGatewayFilterFactory.Config()))).uri("lb://order-service"))
                .build();
    }
}