package com.platform.gateway.config;

import com.platform.gateway.filter.CorrelationIdFilter;
import com.platform.gateway.filter.JwtAuthFilter;
import com.platform.gateway.filter.LoggingFilter;
import com.platform.gateway.filter.RateLimitingFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class GatewayConfigTest {

    @Mock
    private RedisRateLimiter redisRateLimiter;

    @Mock
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private LoggingFilter loggingFilter;

    @Mock
    private CorrelationIdFilter correlationIdFilter;

    @Mock
    private RateLimitingFilter rateLimitingFilter;

    @InjectMocks
    private GatewayConfig gatewayConfig;

    @Test
    void testCustomRouteLocator() {
        // Given
        RouteLocatorBuilder builderMock = mock(RouteLocatorBuilder.class, RETURNS_DEEP_STUBS);

        // When
        RouteLocator routeLocator = gatewayConfig.customRouteLocator(builderMock);

        // Then
        assertNotNull(routeLocator);
    }
}































