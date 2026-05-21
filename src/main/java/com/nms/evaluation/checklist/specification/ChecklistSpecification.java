package com.nms.evaluation.checklist.specification;

import com.nms.evaluation.checklist.entity.Checklist;
import com.nms.evaluation.checklist.enums.ChecklistStatus;
import org.springframework.data.jpa.domain.Specification;

public class ChecklistSpecification {

    public static Specification<Checklist> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Checklist> hasStatus(ChecklistStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Checklist> hasCreatedByUnit(Integer createdByUnit) {
        return (root, query, cb) -> {
            if (createdByUnit == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("createdByUnit"), createdByUnit);
        };
    }
}
