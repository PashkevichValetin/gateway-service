package com.platform.gateway.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeycloakJwtAuthenticationConverterTest {

    @Test
    public void testConvertWithRoles() {
        // Arrange
        KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("USER", "ADMIN"));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("testuser");
        when(jwt.getTokenValue()).thenReturn("token123");
        when(jwt.getClaimAsMap("realm_access")).thenReturn(realmAccess);

        // Act
        Mono<AbstractAuthenticationToken> result = converter.convert(jwt);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testConvertWithoutRoles() {
        // Arrange
        KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("testuser");
        when(jwt.getTokenValue()).thenReturn("token123");
        when(jwt.getClaimAsMap("realm_access")).thenReturn(null);

        // Act
        Mono<AbstractAuthenticationToken> result = converter.convert(jwt);

        // Assert
        assertNotNull(result);
    }
}
