package com.nms.evaluation.checklist.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception cho toàn bộ business logic.
 * Mỗi exception tự mang theo HTTP status tương ứng.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    // --- Factory methods cho các case phổ biến ---

    public static BusinessException notFound(String message) {
        return new BusinessException(message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT);
    }
}
