package com.platform.gateway.config;

import com.platform.gateway.filter.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private CorrelationIdFilter correlationIdFilter;

    @Test
    void testCustomRouteLocator() {
        // Given
        RouteLocatorBuilder builderMock = mock(RouteLocatorBuilder.class, RETURNS_DEEP_STUBS);
        GatewayConfig gatewayConfig = new GatewayConfig(redisRateLimiter, correlationIdFilter);

        // When
        RouteLocator routeLocator = gatewayConfig.customRouteLocator(builderMock);

        // Then
        assertNotNull(routeLocator);
    }
}





















