package com.example.gateway.controller;

import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final WebClient webClient;

    @Value("${keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public AuthController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=password&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&username=" + request.getUsername() +
                        "&password=" + request.getPassword())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(response -> log.info("🔓 Login exitoso para usuario '{}'", request.getUsername()))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.warn("❌ Fallo de login para '{}': {}", request.getUsername(), e.getMessage());
                    return Mono.just(ResponseEntity
                            .status(401)
                            .body(Map.of("error", "Unauthorized")));
                });
    }


    @Getter
    public static class LoginRequest {
        private String username;
        private String password;

    }
}
