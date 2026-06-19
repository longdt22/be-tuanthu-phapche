package com.nms.evaluation.checklist.service.impl;

import com.nms.evaluation.checklist.dto.CriteriaRequest;
import com.nms.evaluation.checklist.dto.CriteriaResponse;
import com.nms.evaluation.checklist.dto.ChecklistRequest;
import com.nms.evaluation.checklist.dto.ChecklistResponse;
import com.nms.evaluation.checklist.dto.EvaluationRequest;
import com.nms.evaluation.checklist.entity.Checklist;
import com.nms.evaluation.checklist.enums.ChecklistStatus;
import com.nms.evaluation.checklist.entity.Criteria;
import com.nms.evaluation.checklist.enums.ApplicabilityLevel;
import com.nms.evaluation.checklist.exception.BusinessException;
import com.nms.evaluation.checklist.exception.ExcelImportException;
import com.nms.evaluation.checklist.mapper.ChecklistMapper;
import com.nms.evaluation.checklist.repository.ChecklistRepository;
import com.nms.evaluation.checklist.repository.CriteriaRepository;
import com.nms.evaluation.checklist.service.ChecklistService;
import com.nms.evaluation.checklist.specification.ChecklistSpecification;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
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
    public Resource getImportTemplate() {
        Resource resource = new ClassPathResource("template/import/template_checklist.xlsx");
        if (!resource.exists()) {
            throw BusinessException.notFound("Tệp tin mẫu template_checklist.xlsx không tồn tại");
        }
        return resource;
    }

    @Override
    public List<CriteriaResponse> importCriteria(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int startRow = 3; // Dữ liệu bắt đầu từ dòng 4 (chỉ số 3)
            int lastRow = sheet.getLastRowNum();
            // Create cell styles for result column
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // Add borders to header style
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);

            CellStyle resultCellStyle = workbook.createCellStyle();
            resultCellStyle.setBorderTop(BorderStyle.THIN);
            resultCellStyle.setBorderBottom(BorderStyle.THIN);
            resultCellStyle.setBorderLeft(BorderStyle.THIN);
            resultCellStyle.setBorderRight(BorderStyle.THIN);
            resultCellStyle.setWrapText(true);

            if (lastRow < startRow) {
                throw new ExcelImportException("File không có dữ liệu để import", null);
            }

            List<CriteriaResponse> resultList = new ArrayList<>();
            boolean hasError = false;

            for (int i = startRow; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                List<String> rowErrors = new ArrayList<>();

                // Cột A (1) - Lĩnh vực: bắt buộc, tối đa 500 ký tự
                String domain = getCellStringValue(row, 0);
                validateRequired(domain, 1, rowErrors);
                validateMaxLength(domain, 500, 1, rowErrors);

                // Cột B (2) - Nhóm văn bản: bắt buộc, số Long
                String documentGroupStr = getCellStringValue(row, 1);
                Long documentGroup = validateRequiredLong(documentGroupStr, 2, rowErrors);

                // Cột C (3) - Nhóm nghĩa vụ tổng quát: bắt buộc, số Long
                String generalObligationGroupStr = getCellStringValue(row, 2);
                Long generalObligationGroup = validateRequiredLong(generalObligationGroupStr, 3, rowErrors);

                // Cột D (4) - Nhóm nghĩa vụ chi tiết: bắt buộc, số Long
                String detailObligationGroupStr = getCellStringValue(row, 3);
                Long detailObligationGroup = validateRequiredLong(detailObligationGroupStr, 4, rowErrors);

                // Cột E (5) - Đơn vị đánh giá: bắt buộc, số Long
                String evaluationUnitStr = getCellStringValue(row, 4);
                Long evaluationUnit = validateRequiredLong(evaluationUnitStr, 5, rowErrors);

                // Cột F (6) - Nội dung nghĩa vụ tuân thủ: tối đa 500 ký tự
                String complianceObligation = getCellStringValue(row, 5);
                validateMaxLength(complianceObligation, 500, 6, rowErrors);

                // Cột G (7) - Căn cứ pháp lý: tối đa 500 ký tự
                String obligationLegalBasis = getCellStringValue(row, 6);
                validateMaxLength(obligationLegalBasis, 500, 7, rowErrors);

                // Cột H (8) - Đánh giá áp dụng: bắt buộc, enum ApplicabilityLevel
                String applicabilityLevelStr = getCellStringValue(row, 7);
                ApplicabilityLevel applicabilityLevel = null;
                if (applicabilityLevelStr == null || applicabilityLevelStr.trim().isEmpty()) {
                    rowErrors.add("cột [8] không được bỏ trống");
                } else {
                    try {
                        applicabilityLevel = ApplicabilityLevel.fromValue(applicabilityLevelStr.trim());
                    } catch (IllegalArgumentException e) {
                        rowErrors.add("cột [8] giá trị không hợp lệ");
                    }
                }

                // Cột I (9) - Giấy phép: tối đa 500 ký tự
                String license = getCellStringValue(row, 8);
                validateMaxLength(license, 500, 9, rowErrors);

                // Cột J (10) - Chế tài: tối đa 500 ký tự
                String sanction = getCellStringValue(row, 9);
                validateMaxLength(sanction, 500, 10, rowErrors);

                // Cột K (11) - Hình phạt: tối đa 500 ký tự
                String sanctionLegalBasis = getCellStringValue(row, 10);
                validateMaxLength(sanctionLegalBasis, 500, 11, rowErrors);

                // Ghi lỗi vào cột L (chỉ số 11) nếu có
                if (!rowErrors.isEmpty()) {
                    hasError = true;
                    Cell errorCell = row.createCell(11);
                    String bulletJoined = "• " + String.join("\n• ", rowErrors);
                    errorCell.setCellValue(bulletJoined);
                    errorCell.setCellStyle(resultCellStyle);
                }

                // Nếu dòng hợp lệ, tạo CriteriaResponse
                if (rowErrors.isEmpty()) {
                    CriteriaResponse response = CriteriaResponse.builder()
                            .domain(domain)
                            .documentGroup(documentGroup)
                            .generalObligationGroup(generalObligationGroup)
                            .detailObligationGroup(detailObligationGroup)
                            .evaluationUnit(evaluationUnit)
                            .complianceObligation(complianceObligation)
                            .obligationLegalBasis(obligationLegalBasis)
                            .applicabilityLevel(applicabilityLevel)
                            .license(license)
                            .sanction(sanction)
                            .sanctionLegalBasis(sanctionLegalBasis)
                            .build();
                    resultList.add(response);
                }
            }

            if (hasError) {
                // Thêm tiêu đề cột Kết quả vào dòng header (dòng 3, chỉ số 2)
                Row headerRow = sheet.getRow(2);
                if (headerRow != null) {
                    Cell headerErrorCell = headerRow.createCell(11);
                    headerErrorCell.setCellValue("Kết quả");
                    headerErrorCell.setCellStyle(headerCellStyle);
                }

                // Xuất workbook ra Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                workbook.write(baos);
                String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                throw new ExcelImportException("File import có lỗi dữ liệu, vui lòng kiểm tra lại", base64);
            }

            return resultList;
        } catch (ExcelImportException e) {
            throw e; // Re-throw our custom exception
        } catch (IOException e) {
            throw new ExcelImportException("Không thể đọc file Excel: " + e.getMessage(), null);
        } catch (Exception e) {
            throw new ExcelImportException("Lỗi xử lý file Excel: " + e.getMessage(), null);
        }
    }

    // ==================== Import Helper Methods ====================

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i <= 10; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellStringValue(row, i);
                if (val != null && !val.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCellStringValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double numVal = cell.getNumericCellValue();
                if (numVal == Math.floor(numVal) && !Double.isInfinite(numVal)) {
                    return String.valueOf((long) numVal);
                }
                return String.valueOf(numVal);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        double fVal = cell.getNumericCellValue();
                        if (fVal == Math.floor(fVal) && !Double.isInfinite(fVal)) {
                            return String.valueOf((long) fVal);
                        }
                        return String.valueOf(fVal);
                    } catch (Exception e2) {
                        return null;
                    }
                }
            case BLANK:
            default:
                return null;
        }
    }

    private void validateRequired(String value, int colIndex, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add("cột [" + colIndex + "] không được bỏ trống");
        }
    }

    private void validateMaxLength(String value, int maxLength, int colIndex, List<String> errors) {
        if (value != null && value.length() > maxLength) {
            errors.add("Số ký tự cột [" + colIndex + "] không hợp lệ");
        }
    }

    private Long validateRequiredLong(String value, int colIndex, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add("cột [" + colIndex + "] không được bỏ trống");
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            errors.add("cột [" + colIndex + "] không đúng định dạng số");
            return null;
        }
    }

    @Override
    @Transactional
    public ChecklistResponse createChecklist(ChecklistRequest request, String currentUser) {
        // Business Rule 1: Checklist name must be unique
        if (checklistRepository.existsByName(request.getName())) {
            throw BusinessException.badRequest("Tên check list tồn tại vui lòng thử lại");
        }

        Checklist checklist = Checklist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdByUnit(request.getCreatedByUnit())
                .status(request.getStatus() != null ? request.getStatus() : ChecklistStatus.DRAFT)
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
                .orElseThrow(() -> BusinessException.notFound("Checklist with ID " + id + " not found"));
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
                .orElseThrow(() -> BusinessException.notFound("Checklist with ID " + id + " not found"));

        // Name uniqueness check (excluding self)
        if (checklistRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw BusinessException.badRequest("tên check list tồn tại vui lòng thử lại");
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
                .orElseThrow(() -> BusinessException.notFound("Checklist with ID " + id + " not found"));

        checklist.setStatus(status);
        checklist.setUpdatedBy(currentUser);

        Checklist saved = checklistRepository.save(checklist);
        return ChecklistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ChecklistResponse submitEvaluation(Long id, String currentUser) {
        Checklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Checklist with ID " + id + " not found"));

        if (checklist.getStatus() != ChecklistStatus.DRAFT) {
            throw BusinessException.badRequest("Checklist không ở trạng thái Nháp");
        }

        List<Criteria> criterias = checklist.getCriterias();
        if (criterias == null || criterias.isEmpty()) {
            throw BusinessException.badRequest("Checklist phải có ít nhất 1 tiêu chí");
        }

        Set<String> domains = criterias.stream()
                .map(Criteria::getDomain)
                .filter(d -> d != null && !d.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toSet());

        if (!domains.isEmpty()) {
            boolean isDuplicate = checklistRepository.existsDuplicateChecklist(
                    id,
                    checklist.getName(),
                    domains,
                    List.of(ChecklistStatus.PENDING_APPROVAL, ChecklistStatus.APPLIED));
            if (isDuplicate) {
                throw BusinessException.badRequest("Trùng tên + lĩnh vực với checklist khác đã tồn tại");
            }
        }

        checklist.setStatus(ChecklistStatus.PENDING_APPROVAL);
        checklist.setUpdatedBy(currentUser);

        Checklist saved = checklistRepository.save(checklist);
        return ChecklistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ChecklistResponse evaluateChecklist(Long id, EvaluationRequest request, String currentUser) {
        Checklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Checklist with ID " + id + " not found"));

        if (checklist.getStatus() != ChecklistStatus.PENDING_APPROVAL) {
            throw BusinessException.badRequest("Checklist không ở trạng thái Chờ thẩm định");
        }

        if ("APPROVE".equalsIgnoreCase(request.getResult())) {
            checklist.setStatus(ChecklistStatus.PENDING_SIGNATURE);
        } else {
            checklist.setStatus(ChecklistStatus.DRAFT);
        }

        checklist.setEvaluationComment(request.getComment());
        checklist.setUpdatedBy(currentUser);

        Checklist saved = checklistRepository.save(checklist);
        return ChecklistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteChecklist(Long id, String currentUser) {
        Checklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Checklist with ID " + id + " not found"));

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
                .orElseThrow(() -> BusinessException.notFound("Checklist with ID " + checklistId + " not found"));

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
                .orElseThrow(() -> BusinessException.notFound("Criteria with ID " + itemId + " not found"));

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
                .orElseThrow(() -> BusinessException.notFound("Criteria with ID " + itemId + " not found"));

        Checklist checklist = item.getChecklistId();
        if (checklist != null) {
            checklist.removeCriteria(item); // Bidirectional sync and cascade handling
        }
        criteriaRepository.delete(item);
    }
}
