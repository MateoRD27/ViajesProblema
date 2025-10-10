package com.unimag.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;

import java.util.Objects;

@Configuration
public class ApiKeyResolverConfig {
    @Bean
    public KeyResolver apiKeyResolver() {
        // Usa el header X-Api-Key para limitar por usuario
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-Api-Key"))
        );
    }
}
