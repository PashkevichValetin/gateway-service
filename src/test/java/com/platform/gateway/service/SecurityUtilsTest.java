package com.platform.gateway.service;

import com.platform.gateway.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

    @Test
    public void testSecurityUtils() {
        // Тест для вспомогательных методов безопасности
        // В данном случае реализация будет зависеть от конкретных требований

        // Проверка, что класс не выбрасывает ошибки при создании
        SecurityUtils securityUtils = new SecurityUtils();
        assertNotNull(securityUtils);
    }
}
