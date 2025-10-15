package com.unimag.gateway.filter;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class RedisCachingFilter implements GlobalFilter, Ordered {

    private final StringRedisTemplate redisTemplate;

    private static final Duration TTL = Duration.ofSeconds(45);

    // Constructor para inyección de dependencia
    public RedisCachingFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        if (!method.equalsIgnoreCase("GET") || !path.contains("/api/public/itinerary/search")) {
            return chain.filter(exchange);
        }

        String cacheKey = "cache::" + path + "?" + exchange.getRequest().getURI().getQuery();
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            exchange.getResponse().getHeaders().add("X-Cache", "HIT");
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            byte[] bytes = cached.getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        }

        var originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        var decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Flux<? extends DataBuffer> fluxBody = Flux.from(body);

                return super.writeWith(fluxBody.map(dataBuffer -> {
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    DataBufferUtils.release(dataBuffer);

                    String bodyString = new String(content, StandardCharsets.UTF_8);
                    redisTemplate.opsForValue().set(cacheKey, bodyString, TTL);

                    return bufferFactory.wrap(content);
                }));
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return -2; // Se ejecuta antes de escribir la respuesta
    }
}

