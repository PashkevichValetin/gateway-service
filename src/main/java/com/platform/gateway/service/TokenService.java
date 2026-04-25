package com.platform.gateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class TokenService {

    private final Key key;

    @Value("${jwt.expiration:3600000}")
    private long expiration;

    public TokenService(@Value("${jwt.secret:secret}") String secret) {
        byte[] keyBytes = secret.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Mono<String> generateToken(String username, Map<String, Object> claims) {
        return Mono.fromCallable(() ->
                Jwts.builder()
                        .setClaims(claims)
                        .setSubject(username)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + expiration))
                        .signWith(key)
                        .compact()
        );
    }

    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    public Mono<String> extractUsername(String token) {
        return Mono.fromCallable(() ->
                Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }
}