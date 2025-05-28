package com.example.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class RedisCachingGatewayFilterFactory extends AbstractGatewayFilterFactory<RedisCachingGatewayFilterFactory.Config>{
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public RedisCachingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            String[] parts = path.split("/");

            if (parts.length < 6) {
                return chain.filter(exchange);
            }

            String productId = parts[5];
            Cache cache = cacheManager.getCache("productsCache");

            if (cache != null) {
                String cachedProduct = cache.get(productId, String.class);

                if (cachedProduct != null) {
                    DataBuffer buffer = exchange.getResponse().bufferFactory()
                            .wrap(cachedProduct.getBytes(StandardCharsets.UTF_8));
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                }
            }

            return webClientBuilder.build()
                    .get()
                    .uri("http://product-service/api/products/" + productId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(product -> {
                        if (product != null && cache != null) {
                            cache.put(productId, product);
                        }

                        DataBuffer buffer = exchange.getResponse().bufferFactory()
                                .wrap(product.getBytes(StandardCharsets.UTF_8));
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        return exchange.getResponse().writeWith(Mono.just(buffer));
                    });
        };
    }

    public static class Config {
    }
}
