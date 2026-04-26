package com.platform.gateway.filter;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    @DisplayName("should generate correlation ID if missing")
    void shouldGenerateCorrelationIfMissing() {
        // GIVEN
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build()
        );

        GatewayFilter gatewayFilter = filter.apply(new CorrelationIdFilter.Config());

        // WHEN
        Mono<Void> result = gatewayFilter.filter(exchange, exc -> Mono.empty());

        // THEN
        StepVerifier.create(result)
                .verifyComplete();

        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        assertThat(correlationId).isNotNull();
        assertThat(UUID.fromString(correlationId)).isNotNull();

        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"))
                .isEqualTo(correlationId);
    }

    @Test
    @DisplayName("should preserve existing correlation ID")
    void shouldPreserveExistingCorrelationId() {
        // GIVEN
        String existing = "existing-123";
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("X-Correlation-ID", existing)
                        .build()
        );

        GatewayFilter gatewayFilter = filter.apply(new CorrelationIdFilter.Config());

        // WHEN
        Mono<Void> result = gatewayFilter.filter(exchange, exc -> Mono.empty());

        // THEN
        StepVerifier.create(result)
                .verifyComplete();

        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        assertThat(correlationId).isEqualTo(existing);

        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"))
                .isEqualTo(existing);
    }
}

















