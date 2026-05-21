# Kế hoạch Thực hiện: Xây dựng Module Checklist & Xóa Code Evaluation Cũ

Chào bạn, theo tài liệu skill checklist mới trong `.antigravity/.skills/checklist-skill.md`, tôi đã lập kế hoạch chi tiết để loại bỏ các tính năng `evaluation` cũ và xây dựng toàn diện module **Checklist** đạt chuẩn doanh nghiệp (Enterprise Standard).

---

## 1. Dọn dẹp mã nguồn cũ (Xóa bỏ hoàn toàn nghiệp vụ Evaluation)
Chúng ta sẽ xóa các file liên quan đến Evaluation để tránh xung đột package và giữ dự án sạch sẽ:
- **Entities**: Xóa `Evaluation.java`
- **Repositories**: Xóa `EvaluationRepository.java`
- **Services**: Xóa `EvaluationService.java` và `EvaluationServiceImpl.java`
- **Controllers**: Xóa `StatusController.java`
- **DTOs**: Xóa `EvaluationRequest.java`

---

## 2. Triển khai cấu trúc thư mục & package cho Module Checklist
Toàn bộ mã nguồn mới sẽ nằm dưới package `com.nms.evaluation.checklist` với cấu trúc chuẩn modular:
```text
com.nms.evaluation.checklist
├── controller      # ChecklistController.java
├── service         # ChecklistService.java, ChecklistServiceImpl.java
├── repository      # ChecklistRepository.java, ChecklistItemRepository.java
├── dto             # ChecklistRequest, ChecklistResponse, ChecklistItemRequest, ChecklistItemResponse, ApiResponseWrapper
├── entity          # Checklist.java, ChecklistItem.java
├── mapper          # ChecklistMapper.java (Manual Mapper tối ưu hiệu năng)
├── exception       # Các custom Exception và GlobalExceptionHandler.java
└── specification   # ChecklistSpecification.java (Dynamic Query cho bộ lọc)
```

---

## 3. Chi tiết các thành phần chính sẽ triển khai

### A. Thực thể (Entities) & Quan hệ JPA
- Lớp **`Checklist`**:
  - Các thuộc tính: `id` (BIGINT), `name` (VARCHAR), `description` (VARCHAR), `status` (0=draft, 1=applied, 2=cancelled), `createdBy`, `createdAt`, `createdByUnit` (Organization Unit), `updatedBy`, `updatedAt`.
  - Quan hệ `@OneToMany` với `ChecklistItem` (sử dụng `cascade = CascadeType.ALL` và `orphanRemoval = true` để chống bản ghi mồ côi - orphan records).
- Lớp **`ChecklistItem`**:
  - Các thuộc tính chi tiết: `domain`, `documentGroup`, `generalObligationGroup`, `detailedObligationGroup`, `assessmentUnit`, `complianceObligation` (TEXT), `legalBasis1`, `applicability`, `requiredLicenseApproval`, `sanction`, `legalBasis2`, `createdAt`, `updatedAt`.
  - Quan hệ `@ManyToOne(fetch = FetchType.LAZY)` ngược lại `Checklist`.

### B. Quy tắc Nghiệp vụ (Business Rules)
1. **Tự động điền Audit**: Điền thông tin `createdAt`, `updatedAt` thông qua phương thức tự động của thực thể (`@PrePersist`, `@PreUpdate`).
2. **Ngăn trùng tên**: Kiểm tra tên `Checklist` phải là duy nhất trong cùng một Đơn vị tổ chức (`createdByUnit`).
3. **Mã hóa trạng thái**: Checklist khi tạo mới sẽ mặc định ở trạng thái **draft (0)**.
4. **Xóa mềm (Soft Delete)**: Khi yêu cầu xóa checklist, nếu trạng thái là `applied (1)`, hệ thống sẽ thực hiện xóa mềm bằng cách cập nhật `status = cancelled (2)` để tuân thủ quy tắc bảo toàn dữ liệu.

### C. Thiết kế API chuẩn RESTful (`/api/v1/checklists`)
- `POST /api/v1/checklists` - Tạo mới Checklist (cùng danh sách Checklist Items).
- `GET /api/v1/checklists/{id}` - Lấy chi tiết Checklist và các Items tương ứng.
- `GET /api/v1/checklists` - Tìm kiếm Checklist (hỗ trợ phân trang `page`/`size`, sắp xếp `sort`, từ khóa `keyword` khớp theo tên/mô tả, lọc theo `status` và `createdByUnit`).
- `PUT /api/v1/checklists/{id}` - Cập nhật Checklist và danh sách Items đi kèm.
- `PATCH /api/v1/checklists/{id}/status` - Thay đổi trạng thái Checklist (chuyển đổi giữa draft/applied/cancelled).
- `DELETE /api/v1/checklists/{id}` - Xóa mềm Checklist.
- `POST /api/v1/checklists/{id}/items` - Thêm mới một Item vào Checklist có sẵn.
- `PUT /api/v1/checklists/items/{itemId}` - Cập nhật thông tin chi tiết một Item.
- `DELETE /api/v1/checklists/items/{itemId}` - Xóa một Item cụ thể khỏi Checklist.

### D. Định dạng Response chuẩn Wrapper
Tất cả API sẽ trả về dữ liệu được bọc trong lớp Wrapper:
```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

---

## 4. Kế hoạch Xác thực (Verification Plan)
- Chạy lệnh `mvn clean compile` để đảm bảo hệ thống xây dựng thành công và không xảy ra bất kỳ lỗi cú pháp hoặc biên dịch nào trên môi trường Java 21 & Spring Boot 3.2.11.
