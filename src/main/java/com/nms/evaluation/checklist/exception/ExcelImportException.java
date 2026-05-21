package com.nms.evaluation.checklist.exception;

import lombok.Getter;

@Getter
public class ExcelImportException extends RuntimeException {
    
    private final String errorFileBase64;

    public ExcelImportException(String message, String errorFileBase64) {
        super(message);
        this.errorFileBase64 = errorFileBase64;
    }
}
