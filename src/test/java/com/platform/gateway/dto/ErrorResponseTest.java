package com.platform.gateway.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;

import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ErrorResponseTest {

    @Test
    void testErrorResponse() {
        Instant now = Instant.now();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(404);
        errorResponse.setError("Not Found");
        errorResponse.setMessage("Resource not found"); // Исправлено: было "Response not found"
        errorResponse.setPath("/api/test");
        errorResponse.setTimestamp(now());

        assertEquals(404, errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getError());
        assertEquals("Resource not found", errorResponse.getMessage()); // Исправлено
        assertEquals("/api/test", errorResponse.getPath());
        assertEquals(now, errorResponse.getTimestamp());
    }
}