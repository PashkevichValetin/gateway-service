package com.platform.gateway.config;

import com.platform.gateway.filter.CorrelationIdFilter;
import com.platform.gateway.filter.JwtAuthFilter;
import com.platform.gateway.filter.LoggingFilter;
import com.platform.gateway.filter.RateLimitingFilter;
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
    private final JwtAuthFilter jwtAuthFilter;
    private final LoggingFilter loggingFilter;
    private final CorrelationIdFilter correlationIdFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Data Unifier Service
                .route("data-unifier", r -> r
                        .path("/api/v1/unifier/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("dataUnifierCB")
                                        .setFallbackUri("forward:/fallback/unifier"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE))
                                .stripPrefix(2)
                                .addRequestHeader("X-Forwarded-By", "Gateway")
                                .addResponseHeader("X-Gateway-Version", "1.0"))
                        .uri("lb://data-unifier-service"))
                .route("reactor-adapter", r -> r
                        .path("/api/stocks/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("stocksCB")
                                        .setFallbackUri("forward:/fallback/stocks"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(exchange -> exchange.getRequest().getRemoteAddress() != null ?
                                                Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()) :
                                                Mono.just("unkniwn")))
                                .stripPrefix(1))
                        .uri("lb://reactor-adapter-service"))
                .route("monitoring", r -> r
                        .path("/api/monitoring/**")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config()))
                                .stripPrefix(1))
                        .uri("lb://monitoring-service"))
                .route("public", r -> r
                        .path("/api/public/**", "/actuator/heakth", "actuator/info")
                        .filters(f -> f
                                .filter(correlationIdFilter.apply(new CorrelationIdFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config())))
                        .uri("no://op"))
                .route("auth", r -> r
                        .path("/api/auth/**")
                        .uri("lb://keycloak"))
                .build();
    }
}
