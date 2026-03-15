package com.platform.gateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private TokenService tokenService;
    private final String testSecret = "myTestSecretKeyForJUnitTests12345678901234567890";
    private final long expirationMs = 3600000L;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(testSecret);
        ReflectionTestUtils.setField(tokenService, "expiration", expirationMs);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // GIVEN
        String username = "testuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");
        claims.put("userId", "12345");
        claims.put("email", "test@example.com");

        // WHEN
        String token = tokenService.generateToken(username, claims).block();

        // THEN
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject()
                .equals(username)
        );
    }

    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        // GIVEN
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
                .compact();

        // WHEN
        Boolean isValid = tokenService.validateToken(token).block();

        // THEN
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnFalse() {
        // GIVEN
        String invalidToken = "invalid.jwt.token";

        // WHEN
        Boolean isValid = tokenService.validateToken(invalidToken).block();

        // THEN
        assertFalse(isValid);
    }

    @Test
    void extractUsername_ValidToken_ShouldReturnUsername() {
        // GIVEN
        String token = Jwts.builder()
                .setSubject("testuser")
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
                .compact();

        // WHEN
        String username = tokenService.extractUsername(token).block();

        // THEN
        assertEquals("testuser", username);
    }

    @Test
    void extractUsername_InvalidToken_ShouldThrowException() {
        // GIVEN
        String invalidToken = "invalid.jwt.token";

        // WHEN & THEN
        assertThrows(Exception.class, () -> {
            tokenService.extractUsername(invalidToken).block();
        });
    }
}