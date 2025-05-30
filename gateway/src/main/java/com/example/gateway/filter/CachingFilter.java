package com.example.gateway.filter;

import com.example.gateway.components.ReactiveCacheManager;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class CachingFilter implements GatewayFilter, Ordered {

    private final ReactiveCacheManager cacheManager;

    public CachingFilter(ReactiveCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private static final Duration TTL = Duration.ofMinutes(5);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Solo cachear GET
        if (!HttpMethod.GET.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        // Normalizar URI removiendo slash final
        String originalUri = request.getURI().toString();
        String normalizedUri = originalUri.endsWith("/") ? originalUri.substring(0, originalUri.length() - 1) : originalUri;

        // Construir clave de caché
        String cacheKey = "CACHE::" + normalizedUri;

        // Si se modificó la URI, construimos nuevo request
        ServerHttpRequest normalizedRequest = request;
        if (request.getPath().value().endsWith("/")) {
            normalizedRequest = request.mutate()
                    .path(request.getPath().value().replaceAll("/$", ""))
                    .build();
        }

        ServerWebExchange mutatedExchange = exchange.mutate().request(normalizedRequest).build();

        return cacheManager.get(cacheKey)
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
                                return DataBufferUtils.join(body)
                                        .flatMap(dataBuffer -> {
                                            byte[] content = new byte[dataBuffer.readableByteCount()];
                                            dataBuffer.read(content);
                                            DataBufferUtils.release(dataBuffer);

                                            String bodyStr = new String(content, StandardCharsets.UTF_8);
                                            return cacheManager.set(cacheKey, bodyStr, TTL)
                                                    .then(Mono.defer(() -> {
                                                        DataBuffer buffer = bufferFactory.wrap(content);
                                                        return super.writeWith(Mono.just(buffer));
                                                    }));
                                        });
                            }
                            return super.writeWith(body);
                        }
                    };

                    ServerWebExchange decoratedExchange = mutatedExchange.mutate().response(decoratedResponse).build();
                    return chain.filter(decoratedExchange);
                }));
    }


    @Override
    public int getOrder() {
        return -2;
    }
}
