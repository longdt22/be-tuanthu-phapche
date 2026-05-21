package com.nms.evaluation.checklist.service.impl;

import com.nms.evaluation.checklist.dto.CriteriaRequest;
import com.nms.evaluation.checklist.dto.CriteriaResponse;
import com.nms.evaluation.checklist.dto.ChecklistRequest;
import com.nms.evaluation.checklist.dto.ChecklistResponse;
import com.nms.evaluation.checklist.entity.Checklist;
import com.nms.evaluation.checklist.enums.ChecklistStatus;
import com.nms.evaluation.checklist.entity.Criteria;
import com.nms.evaluation.checklist.exception.ChecklistNotFoundException;
import com.nms.evaluation.checklist.exception.DuplicateChecklistNameException;
import com.nms.evaluation.checklist.mapper.ChecklistMapper;
import com.nms.evaluation.checklist.repository.ChecklistRepository;
import com.nms.evaluation.checklist.repository.CriteriaRepository;
import com.nms.evaluation.checklist.service.ChecklistService;
import com.nms.evaluation.checklist.specification.ChecklistSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChecklistServiceImpl implements ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final CriteriaRepository criteriaRepository;

    @Override
    @Transactional
    public ChecklistResponse createChecklist(ChecklistRequest request, String currentUser) {
        // Business Rule 1: Checklist name must be unique
        if (checklistRepository.existsByName(request.getName())) {
            throw new DuplicateChecklistNameException("Tên check list tồn tại vui lòng thử lại");
        }

        Checklist checklist = Checklist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdByUnit(request.getCreatedByUnit())
                .status(ChecklistStatus.DRAFT) // starts as draft
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        if (request.getCriteria() != null) {
            for (CriteriaRequest itemReq : request.getCriteria()) {
                Criteria item = ChecklistMapper.toEntity(itemReq);
                item.setId(null); // Force ID to null for creation
                checklist.addCriteria(item); // Bidirectional sync
            }
        }

        Checklist saved = checklistRepository.save(checklist);
        return ChecklistMapper.toResponse(saved);
    }

    @Override
    public ChecklistResponse getChecklistById(Long id) {
        Checklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> new ChecklistNotFoundException("Checklist with ID " + id + " not found"));
        return ChecklistMapper.toResponse(checklist);
    }

    @Override
    public Page<ChecklistResponse> searchChecklists(String keyword, ChecklistStatus status, Integer createdByUnit,
            Pageable pageable) {
        Specification<Checklist> spec = Specification.where(ChecklistSpecification.hasKeyword(keyword))
                .and(ChecklistSpecification.hasStatus(status))
                .and(ChecklistSpecification.hasCreatedByUnit(createdByUnit));

        return checklistRepository.findAll(spec, pageable).map(ChecklistMapper::toResponse);
    }

    @Override
    @Transactional
    public ChecklistResponse updateChecklist(Long id, ChecklistRequest request, String currentUser) {
        Checklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> new ChecklistNotFoundException("Checklist with ID " + id + " not found"));

        // Name uniqueness check (excluding self)
        if (checklistRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateChecklistNameException("tên check list tồn tại vui lòng thử lại");
        }

        checklist.setName(request.getName());
        checklist.setDescription(request.getDescription());
        checklist.setCreatedByUnit(request.getCreatedByUnit());
        checklist.setUpdatedBy(currentUser);

        // Hibernate Parent-Child synchronization to prevent orphan records
        List<Criteria> existingItems = checklist.getCriterias();
        List<CriteriaRequest> requestedItems = request.getCriteria();

        Map<Long, Criteria> existingItemsMap = existingItems.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(Criteria::getId, Function.identity()));

        Set<Long> requestedItemIds = new HashSet<>();

        if (requestedItems != null) {
            for (CriteriaRequest itemReq : requestedItems) {
                if (itemReq.getId() != null && existingItemsMap.containsKey(itemReq.getId())) {
                    // Update existing item
                    Criteria item = existingItemsMap.get(itemReq.getId());
                    item.setDomain(itemReq.getDomain());
                    item.setDocumentGroup(itemReq.getDocumentGroup());
                    item.setGeneralObligationGroup(itemReq.getGeneralObligationGroup());
                    item.setDetailObligationGroup(itemReq.getDetailObligationGroup());
                    item.setEvaluationUnit(itemReq.getEvaluationUnit());
                    item.setComplianceObligation(itemReq.getComplianceObligation());
                    item.setApplicabilityLevel(itemReq.getApplicabilityLevel());
                    item.setLicense(itemReq.getLicense());
                    item.setSanction(itemReq.getSanction());
                    item.setSanctionLegalBasis(itemReq.getSanctionLegalBasis());
                    requestedItemIds.add(itemReq.getId());
                } else {
                    // Add new item
                    Criteria item = ChecklistMapper.toEntity(itemReq);
                    item.setId(null); // Force ID to null for new item insertion
                    checklist.addCriteria(item);
                }
            }
        }

        // Remove orphan child records
        existingItems.removeIf(item -> {
            if (item.getId() != null && !requestedItemIds.contains(item.getId())) {
                // item.setChecklistId(checklist);
                return true;
            }
            return false;
        });

        Checklist saved = checklistRepository.save(checklist);
        return ChecklistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ChecklistResponse changeStatus(Long id, ChecklistStatus status, String currentUser) {
        Checklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> new ChecklistNotFoundException("Checklist with ID " + id + " not found"));

        checklist.setStatus(status);
        checklist.setUpdatedBy(currentUser);

        Checklist saved = checklistRepository.save(checklist);
        return ChecklistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteChecklist(Long id, String currentUser) {
        Checklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> new ChecklistNotFoundException("Checklist with ID " + id + " not found"));

        // Rule: Applied checklist cannot be deleted physically.
        if (checklist.getStatus() == ChecklistStatus.APPLIED) {
            // Soft delete: status = cancelled (2)
            checklist.setStatus(ChecklistStatus.CANCELLED);
            checklist.setUpdatedBy(currentUser);
            checklistRepository.save(checklist);
        } else {
            // Physical delete for draft or already cancelled checklists
            checklistRepository.delete(checklist);
        }
    }

    @Override
    @Transactional
    public CriteriaResponse createItem(Long checklistId, CriteriaRequest request) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ChecklistNotFoundException("Checklist with ID " + checklistId + " not found"));

        Criteria item = ChecklistMapper.toEntity(request);
        item.setId(null); // Force ID to null for direct creation
        checklist.addCriteria(item); // Bidirectional reference handled

        Criteria saved = criteriaRepository.save(item);
        return ChecklistMapper.toItemResponse(saved);
    }

    @Override
    @Transactional
    public CriteriaResponse updateItem(Long itemId, CriteriaRequest request) {
        Criteria item = criteriaRepository.findById(itemId)
                .orElseThrow(() -> new ChecklistNotFoundException("Criteria with ID " + itemId + " not found"));

        item.setDomain(request.getDomain());
        item.setDocumentGroup(request.getDocumentGroup());
        item.setGeneralObligationGroup(request.getGeneralObligationGroup());
        item.setDetailObligationGroup(request.getDetailObligationGroup());
        item.setEvaluationUnit(request.getEvaluationUnit());
        item.setComplianceObligation(request.getComplianceObligation());
        item.setObligationLegalBasis(request.getObligationLegalBasis());
        item.setApplicabilityLevel(request.getApplicabilityLevel());
        item.setLicense(request.getLicense());
        item.setSanction(request.getSanction());
        item.setSanctionLegalBasis(request.getSanctionLegalBasis());

        Criteria saved = criteriaRepository.save(item);
        return ChecklistMapper.toItemResponse(saved);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        Criteria item = criteriaRepository.findById(itemId)
                .orElseThrow(() -> new ChecklistNotFoundException("Criteria with ID " + itemId + " not found"));

        Checklist checklist = item.getChecklistId();
        if (checklist != null) {
            checklist.removeCriteria(item); // Bidirectional sync and cascade handling
        }
        criteriaRepository.delete(item);
    }
}
