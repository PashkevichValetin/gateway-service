package com.platform.gateway.config;

import com.platform.gateway.filter.CorrelationIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final RedisRateLimiter redisRateLimiter;
    private final CorrelationIdFilter correlationIdFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("data-unifier", r -> r
                        .path("/api/v1/unifier/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("dataUnifierCB")
                                        .setFallbackUri("forward:/fallback/unifier"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE))
                                .requestRateLimiter(limiter -> limiter
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(exchange -> Mono.just(
                                                exchange.getRequest().getRemoteAddress() != null ?
                                                        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() :
                                                        "unknown")))
                                .stripPrefix(2)
                                .addRequestHeader("X-Forwarded-By", "Gateway")
                                .addResponseHeader("X-Gateway-Version", "1.0"))
                        .uri("lb://data-unifier-service"))
                .route("reactor-adapter", r -> r
                        .path("/api/stocks/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("stocksCB")
                                        .setFallbackUri("forward:/fallback/stocks"))
                                .requestRateLimiter(limiter -> limiter
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(exchange -> Mono.just(
                                                exchange.getRequest().getRemoteAddress() != null ?
                                                        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() :
                                                        "unknown")))
                                .stripPrefix(1))
                        .uri("lb://reactor-adapter-service"))
                .route("monitoring", r -> r
                        .path("/api/monitoring/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .stripPrefix(1))
                        .uri("lb://monitoring-service"))
                .route("public", r -> r
                        .path("/api/public/**", "/actuator/health", "/actuator/info")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config())))
                        .uri("http://data-unifier:8081"))   // или другой сервис
                .route("auth", r -> r
                        .path("/api/auth/**")
                        .uri("http://keycloak:8080"))
                .build();
    }
}