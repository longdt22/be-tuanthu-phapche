package com.nms.evaluation.checklist.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nms.evaluation.checklist.enums.ChecklistStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChecklistResponse {
    private Long id;
    private String name;
    private String description;
    private ChecklistStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private Integer createdByUnit;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String evaluationComment;
    private List<CriteriaResponse> criteria;
}
