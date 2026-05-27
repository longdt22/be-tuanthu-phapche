package com.nms.evaluation.checklist.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistRequest {

    @NotBlank(message = "Tên checklist bắt buộc nhập")
    @Size(max = 500, message = "Tên checklist tối đa 500 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @NotNull(message = "Đơn vị đánh giá bắt buộc chọn")
    private Integer createdByUnit;

    @Valid
    private List<CriteriaRequest> criteria;

    private com.nms.evaluation.checklist.enums.ChecklistStatus status;
}
