package com.example.gateway.config;

import com.example.gateway.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    SecurityConfig(JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/eureka/**").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        // Acceso solo para USER y ADMIN
                        .pathMatchers("/api/orders/**", "/api/payments/**").hasAnyRole("USER", "ADMIN")
                        // Acceso solo para ADMIN y SUPERVISOR
                        .pathMatchers("/api/inventories/**", "/api/products/**").hasAnyRole("ADMIN", "SUPERVISOR")
                        .pathMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(
                    oauth2 -> oauth2.jwt(
                        jwt-> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                    )
                )
                .build();
    }
}



