package com.threedfly.productservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response structure for API error responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    
    /**
     * Creates an ErrorResponse from any BaseException.
     */
    public static ErrorResponse from(BaseException ex) {
        return ex.toErrorResponse();
    }
}