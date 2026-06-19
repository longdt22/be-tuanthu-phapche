package com.nms.evaluation.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EvaluationRequest {

    @NotBlank(message = "Kết quả thẩm định không được bỏ trống")
    @Pattern(regexp = "APPROVE|REJECT", message = "Kết quả phải là APPROVE hoặc REJECT")
    private String result;

    private String comment;
}
