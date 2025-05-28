package com.example.gateway.config;


import com.example.gateway.filter.CorrelationIdFilter;
import com.example.gateway.filter.RedisCacheFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutesConfig {
    private final CorrelationIdFilter correlationIdFilter;
    private final RedisCacheFilter cachingFilter;

    public RoutesConfig(CorrelationIdFilter correlationIdFilter, RedisCacheFilter cachingFilter) {
        this.correlationIdFilter = correlationIdFilter;
        this.cachingFilter = cachingFilter;
    }

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("payment_route", r ->
                        r.path("/api/payments/**").filters(f -> f.filter(correlationIdFilter)).uri("lb://payment-service"))
                .route("inventory_route", r ->
                        r.path("/api/inventories/**").filters(f -> f.filter(correlationIdFilter)).uri("lb://inventory-service"))
                .route("product_route", r ->
                        r.path("/api/products/**").filters(f -> f
                                .filter(correlationIdFilter)
                                .filter(cachingFilter)).uri("lb://product-service"))
                .route("order_route", r ->
                        r.path("/api/orders/**").filters(f -> f.filter(correlationIdFilter)).uri("lb://order-service"))
                .build();
    }
}
