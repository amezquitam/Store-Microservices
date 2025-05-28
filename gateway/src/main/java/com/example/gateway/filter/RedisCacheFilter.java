package com.example.gateway.filter;

import com.example.gateway.RedisCacheService;
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
public class RedisCacheFilter implements GatewayFilter, Ordered {

    private final RedisCacheService cacheService;

    public RedisCacheFilter(RedisCacheService cacheService) {
        this.cacheService = cacheService;
    }

    private static final Duration TTL = Duration.ofMinutes(5);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();


        if (!HttpMethod.GET.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        String cacheKey = "CACHE::" + request.getURI().toString();

        return cacheService.get(cacheKey)
                .flatMap(cachedBody -> {
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

                                            String bodyStr = new String(content, StandardCharsets.UTF_8);
                                            cacheService.set(cacheKey, bodyStr, TTL).subscribe();

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

