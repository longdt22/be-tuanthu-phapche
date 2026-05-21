package com.nms.evaluation.checklist.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicabilityLevel {
    REQUIRED("Bắt buộc"),
    NONE("Không"),
    RECOMMENDED("Khuyến nghị");

    private final String value;

    ApplicabilityLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ApplicabilityLevel fromValue(String value) {
        for (ApplicabilityLevel level : values()) {
            if (level.value.equalsIgnoreCase(value) || level.name().equalsIgnoreCase(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown applicability level: " + value);
    }
}
