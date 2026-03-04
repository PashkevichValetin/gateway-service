package com.platform.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.anyExchange;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        // Публичные эндпоинты
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/api/public/**").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()

                        // Swagger/OpenAPI
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Сервисы требуют аутентификации
                        .pathMatchers("/api/v1/unifier/**").hasRole("USER")
                        .pathMatchers("/api/stocks/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/api/monitoring/**").hasRole("ADMIN")

                        // Все остальное требует аутентификации
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(String.valueOf(Arrays.asList("http://localhost:3000", "http://localhost:8080")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("**/", configuration);
        return source;
    }
}



























