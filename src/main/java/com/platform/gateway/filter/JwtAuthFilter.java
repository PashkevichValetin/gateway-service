package com.platform.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final ReactiveJwtDecoder jwtDecoder;

    @Autowired
    public JwtAuthFilter(ReactiveJwtDecoder jwtDecoder) {
        super(Config.class);
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            return jwtDecoder.decode(token)
                    .flatMap(jwt -> chain.filter(exchange))
                    .onErrorResume(e -> {
                        log.warn("JWT validation failed: {}", e .getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator/health") ||
                path.startsWith("/actuator/info") ||
                path.startsWith("/api/public") ||
                path.startsWith("/api/auth");
    }

    public static class Config {

    }
}