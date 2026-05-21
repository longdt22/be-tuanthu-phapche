package com.nms.evaluation.checklist.repository;

import com.nms.evaluation.checklist.entity.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CriteriaRepository extends JpaRepository<Criteria, Long> {
}
