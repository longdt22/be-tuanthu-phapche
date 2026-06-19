package com.nms.evaluation.checklist.repository;

import com.nms.evaluation.checklist.entity.Checklist;
import com.nms.evaluation.checklist.enums.ChecklistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long>, JpaSpecificationExecutor<Checklist> {
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT COUNT(c) > 0 FROM Checklist c JOIN c.criterias cr " +
           "WHERE c.id <> :id " +
           "AND c.name = :name " +
           "AND cr.domain IN :domains " +
           "AND c.status IN :statuses")
    boolean existsDuplicateChecklist(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("domains") Collection<String> domains,
            @Param("statuses") Collection<ChecklistStatus> statuses);
}
