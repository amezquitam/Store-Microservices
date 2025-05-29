package com.example.gateway.filter;

import com.example.gateway.components.ReactiveCacheManager;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class CachingFilter implements GatewayFilter, Ordered {

    private final ReactiveCacheManager cacheManager;

    public CachingFilter(ReactiveCacheManager  cacheManager) {
        this.cacheManager = cacheManager;
    }

    private static final Duration TTL = Duration.ofMinutes(5);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Solo cacheamos GETs
        if (!HttpMethod.GET.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        String cacheKey = "CACHE::" + request.getURI().toString();

        return cacheManager.get(cacheKey)
                .flatMap(cachedBody -> {
                    // Si hay respuesta cacheada, devolverla directamente
                    byte[] bytes = cachedBody.getBytes(StandardCharsets.UTF_8);
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    ServerHttpResponse originalResponse = exchange.getResponse();
                    DataBufferFactory bufferFactory = originalResponse.bufferFactory();

                    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                        @Override
                        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                            if (body instanceof Flux) {
                                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                                return super.writeWith(
                                        fluxBody.map(dataBuffer -> {
                                            byte[] content = new byte[dataBuffer.readableByteCount()];
                                            dataBuffer.read(content);
                                            DataBufferUtils.release(dataBuffer);

                                            // Cacheamos la respuesta
                                            String bodyStr = new String(content, StandardCharsets.UTF_8);

                                            cacheManager.set(cacheKey, bodyStr, TTL).subscribe();
                                            return bufferFactory.wrap(content);
                                        })
                                );
                            }
                            return super.writeWith(body);
                        }
                    };

                    ServerWebExchange mutatedExchange = exchange.mutate().response(decoratedResponse).build();
                    return chain.filter(mutatedExchange);
                }));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}