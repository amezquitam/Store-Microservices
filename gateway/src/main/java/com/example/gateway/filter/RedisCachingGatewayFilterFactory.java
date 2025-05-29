package com.example.gateway.filter;

import com.example.gateway.components.ReactiveCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
public class RedisCachingGatewayFilterFactory extends AbstractGatewayFilterFactory<RedisCachingGatewayFilterFactory.Config> {

    @Autowired
    private final ReactiveCacheManager reactiveCacheManager;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    public RedisCachingGatewayFilterFactory(ReactiveCacheManager reactiveCacheManager) {
        super(Config.class);
        this.reactiveCacheManager = reactiveCacheManager;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            String[] parts = path.split("/");

            log.info("======= Path: {}", path);

            if (parts.length == 3) {
                String cacheKey = "all-products";
                Duration ttl = Duration.ofMinutes(config.getTtlMinutes());

                // Verificar si los datos están en caché
                return reactiveCacheManager.get(cacheKey)
                        .flatMap(cachedProducts -> {
                            if (cachedProducts != null && !cachedProducts.isEmpty()) {
                                // Si está en caché, devolver la respuesta desde la caché
                                DataBuffer buffer = exchange.getResponse().bufferFactory()
                                        .wrap(cachedProducts.getBytes(StandardCharsets.UTF_8));
                                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                return exchange.getResponse().writeWith(Mono.just(buffer));
                            }

                            // Si no está en caché, hacer la llamada al microservicio
                            return webClientBuilder.build()
                                    .get()
                                    .uri("lb://product-service/api/products")  // Llamada al microservicio
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .onErrorResume(error -> {
                                        log.error("Error fetching products from product-service: {}", error.getMessage());
                                        return Mono.just("[]"); // Devolver un JSON vacío en caso de error
                                    })
                                    .flatMap(products ->
                                            reactiveCacheManager.set(cacheKey, products, ttl)  // Guardar la respuesta en caché
                                                    .then(Mono.defer(() -> {
                                                        DataBuffer buffer = exchange.getResponse().bufferFactory()
                                                                .wrap(products.getBytes(StandardCharsets.UTF_8));
                                                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                                        return exchange.getResponse().writeWith(Mono.just(buffer));
                                                    }))
                                    );
                        });
            }

            // La ruta esperada es: /api/products/{id}, significa que parts[5] = id
            if (parts.length < 4) {
                return chain.filter(exchange); // Ruta no válida, seguir sin cache
            }

            String productId = parts[3];
            Duration ttl = Duration.ofMinutes(config.getTtlMinutes());


            // Usar ReactiveCacheManager para obtener el valor en caché
            return reactiveCacheManager.get(productId)
                    .flatMap(cachedProduct -> {
                        if (cachedProduct != null && !cachedProduct.isEmpty()) {
                            DataBuffer buffer = exchange.getResponse().bufferFactory()
                                    .wrap(cachedProduct.getBytes(StandardCharsets.UTF_8));
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            return exchange.getResponse().writeWith(Mono.just(buffer));
                        }

                        // Si no está en caché, llamar al microservicio
                        return webClientBuilder.build()
                                .get()
                                .uri("lb://product-service/api/products/" + productId)
                                .retrieve()
                                .bodyToMono(String.class)
                                .flatMap(product ->
                                        reactiveCacheManager.set(productId, product, ttl)
                                                .then(Mono.defer(() -> {
                                                    DataBuffer buffer = exchange.getResponse().bufferFactory()
                                                            .wrap(product.getBytes(StandardCharsets.UTF_8));
                                                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                                    return exchange.getResponse().writeWith(Mono.just(buffer));
                                                }))
                                );
                    });
        };
    }

    public static class Config {
        private long ttlMinutes = 5;

        public long getTtlMinutes() {
            return ttlMinutes;
        }

        public void setTtlMinutes(long ttlMinutes) {
            this.ttlMinutes = ttlMinutes;
        }
    }
}