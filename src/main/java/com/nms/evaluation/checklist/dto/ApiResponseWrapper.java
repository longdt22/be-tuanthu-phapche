package com.nms.evaluation.checklist.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseWrapper<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponseWrapper<T> success(T data) {
        return ApiResponseWrapper.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponseWrapper<T> error(String message) {
        return ApiResponseWrapper.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
