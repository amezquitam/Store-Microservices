package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class.getName());

    public static class Config {
        private String serviceName;

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    }

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();
            String method = exchange.getRequest().getQueryParams()
                    .entrySet()
                    .stream()
                    .map(en -> "%s: [%s]".formatted(
                            en.getKey(),
                            String.join(",", en.getValue())
                    ))
                    .collect(Collectors.joining("; ", "{", "}"));

            logger.info("Solicitud recibida: [{}] {}", method, path);

            return chain.filter(exchange)
                    .doOnSuccess(aVoid -> logger.info("Respuesta para: [{}] {}", method, path));
        };
    }
}
