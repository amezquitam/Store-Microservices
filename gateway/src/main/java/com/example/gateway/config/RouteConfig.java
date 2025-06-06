package com.example.gateway.config;

import com.example.gateway.filter.*;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    private final CorrelationIdFilter correlationIdFilter;
    private final CachingFilter cachingFilter;
    private final LoggingFilter loggingFilter;
    private final CircuitBreakerFilter circuitBreakerFilter;

    private final AuthFilter authFilter;

    public RouteConfig(CorrelationIdFilter correlationIdFilter,
                       CachingFilter cachingFilter,
                       LoggingFilter loggingFilter,
                       CircuitBreakerFilter circuitBreakerFilter,
                       AuthFilter authFilter) {
        this.correlationIdFilter = correlationIdFilter;
        this.cachingFilter = cachingFilter;
        this.loggingFilter = loggingFilter;
        this.circuitBreakerFilter = circuitBreakerFilter;
        this.authFilter = authFilter;
    }


    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("payment_route", r ->
                        r.path("/api/payments/**")
                                .filters(f -> f
                                        .filter(correlationIdFilter)
                                        .filter(loggingFilter)
                                        .filter(authFilter)
                                        .filter(circuitBreakerFilter))
                                .uri("lb://payment-service"))
                .route("inventory_route", r ->
                        r.path("/api/inventories/**")
                                .filters(f -> f
                                        .filter(correlationIdFilter)
                                        .filter(loggingFilter)
                                        .filter(authFilter)
                                        .filter(circuitBreakerFilter))
                                .uri("lb://inventory-service"))
                .route("product_route", r ->
                        r.path("/api/products/**")
                                .filters(f -> f
                                        .filter(correlationIdFilter)
                                        .filter(loggingFilter)
                                        .filter(authFilter)
                                        .filter(circuitBreakerFilter)
                                        .filter(cachingFilter))
                                .uri("lb://product-service"))
                .route("order_route", r ->
                        r.path("/api/orders/**")
                                .filters(f -> f
                                        .filter(correlationIdFilter)
                                        .filter(loggingFilter)
                                        .filter(authFilter)
                                        .filter(circuitBreakerFilter))
                                .uri("lb://order-service"))
                .build();
    }
}
