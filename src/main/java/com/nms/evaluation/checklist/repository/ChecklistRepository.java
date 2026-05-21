package com.nms.evaluation.checklist.repository;

import com.nms.evaluation.checklist.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long>, JpaSpecificationExecutor<Checklist> {
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
}
