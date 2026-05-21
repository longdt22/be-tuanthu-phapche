package com.nms.evaluation.checklist.dto;

import com.nms.evaluation.checklist.enums.ApplicabilityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaRequest {

    private Long id; // Optional, used when updating items/criteria in a checklist

    @NotBlank(message = "Lĩnh vực bắt buộc nhập")
    @Size(max = 500, message = "Lĩnh vực tối đa 500 ký tự")
    private String domain;

    @NotNull(message = "Nhóm văn bản bắt buộc chọn")
    private Long documentGroup;

    @NotNull(message = "Nhóm nghĩa vụ tổng quát bắt buộc chọn")
    private Long generalObligationGroup;

    @NotNull(message = "Nhóm nghĩa vụ chi tiết bắt buộc chọn")
    private Long detailObligationGroup;

    private Long evaluationUnit;

    @Size(max = 500, message = "Nội dung nghĩa vụ tuân thủ tối đa 500 ký tự")
    private String complianceObligation;

    @Size(max = 500, message = "Căn cứ pháp lý tối đa 500 ký tự")
    private String obligationLegalBasis;

    @NotNull(message = "Đánh giá áp dụng bắt buộc chọn")
    private ApplicabilityLevel applicabilityLevel;

    @Size(max = 500, message = "Yêu cầu phê duyệt giấy phép bắt buộc tối đa 500 ký tự")
    private String license;

    @Size(max = 500, message = "Chế tài tối đa 500 ký tự")
    private String sanction;

    @Size(max = 500, message = "Căn cứ pháp lý chế tài tối đa 500 ký tự")
    private String sanctionLegalBasis;
}
