package com.example.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import reactor.core.publisher.Mono;

@Component
public class RedisCachingGatewayFilterFactory extends AbstractGatewayFilterFactory<RedisCachingGatewayFilterFactory.Config> {

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
            // Obtener el ID del producto desde la URL de la solicitud
            String productId = exchange.getRequest().getPath().toString().split("/")[3];

            if (productId != null) {
                // Verificar si el producto está en el cache de Redis
                Cache cache = cacheManager.getCache("productsCache");

                if (cache != null) {
                    // Intentar obtener el producto del cache
                    Object cachedProduct = cache.get(productId);

                    if (cachedProduct != null) {
                        // Si el producto está en cache, devolverlo directamente
                        return exchange.getResponse().writeWith(Mono.just(
                                exchange.getResponse().bufferFactory().wrap(cachedProduct.toString().getBytes())
                        ));
                    }
                }
            }

            // Si el producto no está en cache, hacer una llamada al servicio de productos
            return chain.filter(exchange).doOnTerminate(() -> {
                if (productId != null) {
                    // Crear el WebClient usando el Builder
                    WebClient webClient = webClientBuilder.baseUrl("http://product-service/api/products")  // Usamos el nombre del servicio registrado en Eureka
                            .build();  // Construimos el WebClient

                    // Realizar una solicitud GET al servicio de productos
                    webClient.get()
                            .uri("/" + productId)  // Pasamos el ID del producto
                            .retrieve()
                            .bodyToMono(String.class)  // Suponemos que el producto se devuelve como un String (ajustar según el tipo de respuesta)
                            .doOnTerminate(() -> {
                                // Después de obtener el producto, guardarlo en Redis para futuras peticiones
                                webClient.get()
                                        .uri("/" + productId)
                                        .retrieve()
                                        .bodyToMono(String.class)
                                        .subscribe(product -> {
                                            Cache cache = cacheManager.getCache("productsCache");
                                            if (product != null) {
                                                // Guardar el producto en el cache de Redis
                                                cache.put(productId, product);
                                            }
                                        });
                            }).subscribe();
                }
            });
        };
    }

    public static class Config {
        // Aquí puedes agregar parámetros para la configuración de tu filtro, si es necesario
    }
}
