package com.nms.evaluation.checklist.controller;

import com.nms.evaluation.checklist.dto.ApiResponseWrapper;
import com.nms.evaluation.checklist.dto.CriteriaRequest;
import com.nms.evaluation.checklist.dto.CriteriaResponse;
import com.nms.evaluation.checklist.dto.ChecklistRequest;
import com.nms.evaluation.checklist.dto.ChecklistResponse;
import com.nms.evaluation.checklist.dto.EvaluationRequest;
import com.nms.evaluation.checklist.dto.PageResponse;
import com.nms.evaluation.checklist.enums.ChecklistStatus;
import com.nms.evaluation.checklist.service.ChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/checklists")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;

    private String resolveUser(String header) {
        return (header != null && !header.trim().isEmpty()) ? header.trim() : "system";
    }

    @GetMapping("/import-template")
    @Operation(summary = "Tải file template excel mẫu để import tiêu chí")
    public ResponseEntity<Resource> downloadTemplate() {
        Resource resource = checklistService.getImportTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"template_checklist.xlsx\"")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import tiêu chí từ file Excel và trả về danh sách đã parse")
    public ResponseEntity<ApiResponseWrapper<List<CriteriaResponse>>> importCriteria(
            @RequestParam("file") MultipartFile file) {

        List<CriteriaResponse> response = checklistService.importCriteria(file);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponseWrapper<ChecklistResponse>> createChecklist(
            @Valid @RequestBody ChecklistRequest request,
            @RequestHeader(value = "X-User", required = false) String userHeader) {

        String user = resolveUser(userHeader);
        ChecklistResponse response = checklistService.createChecklist(request, user);
        return new ResponseEntity<>(ApiResponseWrapper.success(response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<ChecklistResponse>> getChecklistById(@PathVariable Long id) {
        ChecklistResponse response = checklistService.getChecklistById(id);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseWrapper<PageResponse<ChecklistResponse>>> searchChecklists(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ChecklistStatus status,
            @RequestParam(required = false) Integer createdByUnit,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ChecklistResponse> response = checklistService.searchChecklists(keyword, status, createdByUnit, pageable);
        return ResponseEntity.ok(ApiResponseWrapper.success(PageResponse.from(response)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<ChecklistResponse>> updateChecklist(
            @PathVariable Long id,
            @Valid @RequestBody ChecklistRequest request,
            @RequestHeader(value = "X-User", required = false) String userHeader) {

        String user = resolveUser(userHeader);
        ChecklistResponse response = checklistService.updateChecklist(id, request, user);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseWrapper<ChecklistResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam ChecklistStatus status,
            @RequestHeader(value = "X-User", required = false) String userHeader) {

        String user = resolveUser(userHeader);
        ChecklistResponse response = checklistService.changeStatus(id, status, user);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @PostMapping("/{id}/submit-evaluation")
    @Operation(summary = "Gửi checklist lên bộ phận Pháp chế thẩm định")
    public ResponseEntity<ApiResponseWrapper<ChecklistResponse>> submitEvaluation(
            @PathVariable Long id,
            @RequestHeader(value = "X-User", required = false) String userHeader) {

        String user = resolveUser(userHeader);
        ChecklistResponse response = checklistService.submitEvaluation(id, user);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @PostMapping("/{id}/evaluate")
    @Operation(summary = "Thẩm định checklist (APPROVE đạt / REJECT từ chối)")
    public ResponseEntity<ApiResponseWrapper<ChecklistResponse>> evaluateChecklist(
            @PathVariable Long id,
            @Valid @RequestBody EvaluationRequest request,
            @RequestHeader(value = "X-User", required = false) String userHeader) {

        String user = resolveUser(userHeader);
        ChecklistResponse response = checklistService.evaluateChecklist(id, request, user);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteChecklist(
            @PathVariable Long id,
            @RequestHeader(value = "X-User", required = false) String userHeader) {

        String user = resolveUser(userHeader);
        checklistService.deleteChecklist(id, user);
        return ResponseEntity.ok(ApiResponseWrapper.success(null));
    }

    // Individual Criteria APIs

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponseWrapper<CriteriaResponse>> createItem(
            @PathVariable Long id,
            @Valid @RequestBody CriteriaRequest request) {

        CriteriaResponse response = checklistService.createItem(id, request);
        return new ResponseEntity<>(ApiResponseWrapper.success(response), HttpStatus.CREATED);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponseWrapper<CriteriaResponse>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CriteriaRequest request) {

        CriteriaResponse response = checklistService.updateItem(itemId, request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteItem(@PathVariable Long itemId) {
        checklistService.deleteItem(itemId);
        return ResponseEntity.ok(ApiResponseWrapper.success(null));
    }
}
