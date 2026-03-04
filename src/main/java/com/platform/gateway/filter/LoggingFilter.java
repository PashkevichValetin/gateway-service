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
            long startTime = System.currentTimeMillis();
            String requestId = exchange.getRequest().getId();
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethod().toString();
            String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

            log.info("Request [{}] {} {} from {} - started at {}",
                    requestId, method, path, clientIp, Instant.now());

            return chain.filter(exchange).doFinally(signalType -> {
                long duration = System.currentTimeMillis() - startTime;
                int statusCode = exchange.getResponse().getStatusCode() != null ?
                        exchange.getResponse().getStatusCode().value() : 0;

                log.info("Request [{}] {} {} completed with status {} in {}ms",
                        requestId, method, path, statusCode, duration);
            });
        };
    }

    public static class Config {

    }
}