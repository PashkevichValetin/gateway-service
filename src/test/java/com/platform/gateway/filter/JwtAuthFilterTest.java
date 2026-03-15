package com.platform.gateway.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private ReactiveJwtDecoder jwtDecoder;

    @InjectMocks
    private JwtAuthFilter filter;

    private ServerWebExchange createExchange(String path, String authHeader) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get(path);
        if (authHeader != null) {
            builder.header("Authorization", authHeader);
        }
        return MockServerWebExchange.from(builder.build());
    }

    @Test
    void shouldAllowPublicPathsWithoutToken() {
        // GIVEN
        ServerWebExchange exchange = createExchange("/actuator/health", null);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthFilter.Config());

        // WHEN
        Mono<Void> result = gatewayFilter.filter(exchange, ext -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return Mono.empty();
        });

        // THEN
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldRejetRequestWithInvalidToken() {
        // GIVEN
        ServerWebExchange exchange = createExchange("/api/v1/unifier/test", null);

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthFilter.Config());

        // WHEN
        Mono<Void> result = gatewayFilter.filter(exchange, exc -> Mono.empty());

        // THEN
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectRequestWithInvalidToken() {
        // GIVEN
        ServerWebExchange exchange = createExchange("/api/v1/unifier/test", "Bearer invalid-token");

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.error(new RuntimeException("Invalid token")));

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthFilter.Config());

        // WHEN
        Mono<Void> result = gatewayFilter.filter(exchange, exc -> Mono.empty());

        // THEN
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(jwtDecoder).decode(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue()).isEqualTo("invalid-token");
    }

    @Test
    void shouldAllowRequestWithValidToken() {
        // GIVEN
        ServerWebExchange exchange = createExchange("/api/v1/unifier/test", "Bearer valid-token");

        Jwt jwt = mock(Jwt.class);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwt));

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthFilter.Config());

        // WHEN
        Mono<Void> result = gatewayFilter.filter(exchange, exc -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return  Mono.empty();
        });

        // THEN
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jwtDecoder).decode(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue()).isEqualTo("valid-token");
    }
}
