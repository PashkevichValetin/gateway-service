package com.platform.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Slf4j
@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RateLimitingFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

            String key = "rate_limit:" + clientIp;

            return redisTemplate.opsForValue().increment(key)
                    .flatMap(count -> {
                        if (count == 1) {
                            return redisTemplate.expire(key, Duration.ofMinutes(1))
                                    .thenReturn(count);
                        }
                        return Mono.just(count);
                    })
                    .flatMap(count -> {
                        if (count > 100) {
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(e -> {
                        log.warn("Rate limiting failed due to Redis error: {}", e.getMessage());
                        return chain.filter(exchange);
                    });
        };
    }

    public static class Config {

    }
}