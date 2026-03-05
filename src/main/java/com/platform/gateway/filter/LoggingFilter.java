package com.platform.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Slf4j
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Делаем все переменные final или effectively final
            final long startTime = System.currentTimeMillis();

            String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
            final String requestId = correlationId != null ? correlationId : exchange.getRequest().getId();

            final String path = exchange.getRequest().getPath().value();
            final String method = exchange.getRequest().getMethod().toString();
            final String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

            log.info("Request [{}] {} {} from {} - started at {}",
                    requestId, method, path, clientIp, Instant.now());

            return chain.filter(exchange).doFinally(signalType -> {
                final long duration = System.currentTimeMillis() - startTime;
                final int statusCode = exchange.getResponse().getStatusCode() != null ?
                        exchange.getResponse().getStatusCode().value() : 0;

                log.info("Request [{}] {} {} completed with status {} in {}ms",
                        requestId, method, path, statusCode, duration);
            });
        };
    }

    public static class Config {
    }
}