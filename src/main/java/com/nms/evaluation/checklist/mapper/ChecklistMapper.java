package com.nms.evaluation.checklist.mapper;

import com.nms.evaluation.checklist.dto.CriteriaRequest;
import com.nms.evaluation.checklist.dto.CriteriaResponse;
import com.nms.evaluation.checklist.dto.ChecklistResponse;
import com.nms.evaluation.checklist.entity.Checklist;
import com.nms.evaluation.checklist.entity.Criteria;
import com.nms.evaluation.checklist.enums.ChecklistStatus;
import com.nms.evaluation.checklist.enums.ApplicabilityLevel;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ChecklistMapper {

    public static ChecklistResponse toResponse(Checklist checklist) {
        if (checklist == null)
            return null;

        return ChecklistResponse.builder()
                .id(checklist.getId())
                .name(checklist.getName())
                .description(checklist.getDescription())
                .status(checklist.getStatus())
                .createdBy(checklist.getCreatedBy())
                .createdAt(checklist.getCreatedAt())
                .createdByUnit(checklist.getCreatedByUnit())
                .updatedBy(checklist.getUpdatedBy())
                .updatedAt(checklist.getUpdatedAt())
                .evaluationComment(checklist.getEvaluationComment())
                .criteria(checklist.getCriterias() == null ? new ArrayList<>()
                        : checklist.getCriterias().stream()
                                .map(ChecklistMapper::toItemResponse)
                                .collect(Collectors.toList()))
                .build();
    }

    public static CriteriaResponse toItemResponse(Criteria item) {
        if (item == null)
            return null;

        return CriteriaResponse.builder()
                .id(item.getId())
                .domain(item.getDomain())
                .documentGroup(item.getDocumentGroup())
                .generalObligationGroup(item.getGeneralObligationGroup())
                .detailObligationGroup(item.getDetailObligationGroup())
                .evaluationUnit(item.getEvaluationUnit())
                .complianceObligation(item.getComplianceObligation())
                .obligationLegalBasis(item.getObligationLegalBasis())
                .applicabilityLevel(item.getApplicabilityLevel())
                .license(item.getLicense())
                .sanction(item.getSanction())
                .sanctionLegalBasis(item.getSanctionLegalBasis())
                .build();
    }

    public static Criteria toEntity(CriteriaRequest request) {
        if (request == null)
            return null;

        return Criteria.builder()
                .domain(request.getDomain())
                .documentGroup(request.getDocumentGroup())
                .generalObligationGroup(request.getGeneralObligationGroup())
                .detailObligationGroup(request.getDetailObligationGroup())
                .evaluationUnit(request.getEvaluationUnit())
                .complianceObligation(request.getComplianceObligation())
                .obligationLegalBasis(request.getObligationLegalBasis())
                .applicabilityLevel(request.getApplicabilityLevel())
                .license(request.getLicense())
                .sanction(request.getSanction())
                .sanctionLegalBasis(request.getSanctionLegalBasis())
                .build();
    }
}
