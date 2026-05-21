package com.nms.evaluation.checklist.service;

import com.nms.evaluation.checklist.dto.CriteriaRequest;
import com.nms.evaluation.checklist.dto.CriteriaResponse;
import com.nms.evaluation.checklist.dto.ChecklistRequest;
import com.nms.evaluation.checklist.dto.ChecklistResponse;
import com.nms.evaluation.checklist.enums.ChecklistStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChecklistService {

    // Checklist Operations
    ChecklistResponse createChecklist(ChecklistRequest request, String currentUser);

    ChecklistResponse getChecklistById(Long id);

    Page<ChecklistResponse> searchChecklists(String keyword, ChecklistStatus status, Integer createdByUnit,
            Pageable pageable);

    ChecklistResponse updateChecklist(Long id, ChecklistRequest request, String currentUser);

    ChecklistResponse changeStatus(Long id, ChecklistStatus status, String currentUser);

    void deleteChecklist(Long id, String currentUser);

    // Criteria Operations
    CriteriaResponse createItem(Long checklistId, CriteriaRequest request);

    CriteriaResponse updateItem(Long itemId, CriteriaRequest request);

    void deleteItem(Long itemId);
}
