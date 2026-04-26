package com.platform.gateway.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SecurityUtils {

    public Mono<Boolean> hasRole(String role) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getAuthorities)
                .map(auths -> auths.stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role)))
                .defaultIfEmpty(false);
    }
}