package com.nms.evaluation.checklist.exception;

import com.nms.evaluation.checklist.dto.ApiResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponseWrapper.error(ex.getMessage()));
    }

    @ExceptionHandler(ExcelImportException.class)
    public ResponseEntity<ApiResponseWrapper<java.util.Map<String, String>>> handleExcelImport(ExcelImportException ex) {
        java.util.Map<String, String> data = java.util.Map.of("errorFile", ex.getErrorFileBase64());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseWrapper.error(ex.getMessage(), data));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseWrapper.error("Validation failed: " + errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleAll(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseWrapper.error("An unexpected error occurred: " + ex.getMessage()));
    }
}
