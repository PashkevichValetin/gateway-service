package com.platform.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    @InjectMocks
    private RateLimitingFilter filter;

    private ServerWebExchange createExchangeWithIp(String ip) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .remoteAddress(new InetSocketAddress(ip, 8080))
                        .build()
        );
    }

    @Test
    @DisplayName("should allow request when under limit")
    void shouldAllowRequestWhenUnderLimit() {
        // given
        ServerWebExchange exchange = createExchangeWithIp("192.168.1.1");

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(eq("rate_limit:192.168.1.1"), any())).thenReturn(Mono.just(true));

        GatewayFilter gatewayFilter = filter.apply(new RateLimitingFilter.Config());

        // when
        Mono<Void> result = gatewayFilter.filter(exchange, chain -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return Mono.empty();
        });

        // then
        StepVerifier.create(result)
                .verifyComplete();

        verify(valueOps).increment("rate_limit:192.168.1.1");
        verify(redisTemplate).expire(eq("rate_limit:192.168.1.1"), any());
    }

    @Test
    @DisplayName("should reject request when limit exceeded")
    void shouldRejectRequestWhenLimitExceeded() {
        // given
        ServerWebExchange exchange = createExchangeWithIp("192.168.1.1");

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.just(101L));

        GatewayFilter gatewayFilter = filter.apply(new RateLimitingFilter.Config());

        // when
        Mono<Void> result = gatewayFilter.filter(exchange, chain -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return Mono.empty();
        });

        // then
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("should fallback to chain on Redis error")
    void shouldFallbackToChainOnRedisError() {
        // given
        ServerWebExchange exchange = createExchangeWithIp("192.168.1.1");

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.error(new RuntimeException("Redis down")));

        GatewayFilter gatewayFilter = filter.apply(new RateLimitingFilter.Config());

        // when
        Mono<Void> result = gatewayFilter.filter(exchange, chain -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return Mono.empty();
        });

        // then
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(valueOps).increment("rate_limit:192.168.1.1");
        verify(redisTemplate, never()).expire(anyString(), any());
    }
}




















