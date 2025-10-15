package com.unimag.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RedisCachingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RedisCachingFilter.class);
    private static final String CACHE_PREFIX = "cache::";

    private final ReactiveRedisTemplate<String, byte[]> redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("#{'${gateway.cache.paths:/api/public/itinerary/search}'.split(',')}")
    private List<String> cacheablePaths;

    @Value("${gateway.cache.ttl-seconds:45}")
    private long cacheTtlSeconds;

    public RedisCachingFilter(ReactiveRedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        boolean isCacheable = request.getMethod() == HttpMethod.GET &&
                cacheablePaths.stream().anyMatch(pattern -> pathMatcher.match(pattern.trim(), path));

        if (!isCacheable) {
            return chain.filter(exchange);
        }

        String cacheKey = CACHE_PREFIX + request.getURI();
        log.debug("Intercepting cacheable request: {} -> key {}", path, cacheKey);

        // Intentar obtener desde Redis
        return redisTemplate.opsForValue().get(cacheKey)
                .flatMap(cachedBytes -> {
                    log.info("Cache HIT for key: {}", cacheKey);
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    response.getHeaders().add("X-Cache-Status", "HIT");
                    DataBuffer buffer = response.bufferFactory().wrap(cachedBytes);
                    return response.writeWith(Flux.just(buffer));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Cache MISS for key: {}", cacheKey);
                    ServerHttpResponse originalResponse = exchange.getResponse();
                    originalResponse.getHeaders().add("X-Cache-Status", "MISS");

                    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                        private final StringBuilder bodyBuilder = new StringBuilder();

                        @Override
                        public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                            if (body instanceof Flux) {
                                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                                return super.writeWith(fluxBody
                                        .map(dataBuffer -> {
                                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                            dataBuffer.read(bytes);
                                            DataBufferUtils.release(dataBuffer);

                                            // Almacenar contenido leído para cache
                                            bodyBuilder.append(new String(bytes, StandardCharsets.UTF_8));

                                            // Reinyectar los mismos bytes para que lleguen al cliente
                                            return originalResponse.bufferFactory().wrap(bytes);
                                        })
                                        .doOnComplete(() -> {
                                            HttpStatus status = (HttpStatus) getStatusCode();
                                            if (status != null && status.is2xxSuccessful()) {
                                                byte[] responseBytes = bodyBuilder.toString().getBytes(StandardCharsets.UTF_8);
                                                log.debug("Caching key: {} (TTL {}s, size {} bytes)",
                                                        cacheKey, cacheTtlSeconds, responseBytes.length);

                                                redisTemplate.opsForValue()
                                                        .set(cacheKey, responseBytes, Duration.ofSeconds(cacheTtlSeconds))
                                                        .subscribe(
                                                                success -> log.debug("Successfully cached {}", cacheKey),
                                                                error -> log.error("Error caching {}: {}", cacheKey, error.getMessage())
                                                        );
                                            } else {
                                                log.warn("Skipping cache for {} (status: {})", cacheKey, status);
                                            }
                                        })
                                );
                            }
                            return super.writeWith(body);
                        }
                    };

                    return chain.filter(exchange.mutate().response(decoratedResponse).build());
                }));
    }

    @Override
    public int getOrder() {
        return -2; // Antes de escribir la respuesta final
    }
}
