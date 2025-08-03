package com.threedfly.productservice.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Base exception class that includes standard error response fields.
 * All custom exceptions should extend this class.
 */
@Getter
@Slf4j
public abstract class BaseException extends RuntimeException {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;

    protected BaseException(String message, HttpStatus status) {
        super(message);
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        logException();
    }

    /**
     * Creates an ErrorResponse from this exception.
     */
    public ErrorResponse toErrorResponse() {
        return new ErrorResponse(timestamp, status, error, message);
    }

    /**
     * Each exception type should implement its own logging behavior.
     * This allows for customized logging without modifying the global handler.
     */
    protected abstract void logException();
}