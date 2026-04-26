package com.platform.gateway.config.security;

import com.platform.gateway.GatewayApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = GatewayApplication.class)
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Test
    void testSecurityConfiguration() {

    }
}
