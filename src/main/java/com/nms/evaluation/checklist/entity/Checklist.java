package com.nms.evaluation.checklist.entity;

import com.nms.evaluation.checklist.enums.ChecklistStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checklist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChecklistStatus status;

    @Column(nullable = false, length = 100)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by_unit", nullable = false)
    private Integer createdByUnit;

    private String updatedBy;

    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "checklistId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Criteria> criterias = new ArrayList<>();

    // Bidirectional helper methods
    public void addCriteria(Criteria criteria) {
        if (criterias == null) {
            criterias = new ArrayList<>();
        }
        criterias.add(criteria);
        criteria.setChecklistId(this);
    }

    public void removeCriteria(Criteria criteria) {
        if (criterias != null) {
            criterias.remove(criteria);
            criteria.setChecklistId(null);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ChecklistStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
