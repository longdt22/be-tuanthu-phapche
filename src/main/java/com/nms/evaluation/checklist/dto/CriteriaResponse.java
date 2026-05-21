package com.nms.evaluation.checklist.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nms.evaluation.checklist.enums.ApplicabilityLevel;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CriteriaResponse {
    private Long id;
    private String domain;
    private Long documentGroup;
    private Long generalObligationGroup;
    private Long detailObligationGroup;
    private Long evaluationUnit;
    private String complianceObligation;
    private String obligationLegalBasis;
    private ApplicabilityLevel applicabilityLevel;
    private String license;
    private String sanction;
    private String sanctionLegalBasis;
}
