package com.nms.evaluation.checklist.entity;

import com.nms.evaluation.checklist.enums.ApplicabilityLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Criteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklistId;

    @Column(nullable = false, length = 500)
    private String domain;

    @Column(name = "document_group", nullable = false)
    private Long documentGroup;

    @Column(name = "general_obligation_group", nullable = false)
    private Long generalObligationGroup;

    @Column(name = "detail_obligation_group", nullable = false)
    private Long detailObligationGroup;

    @Column(name = "evaluation_unit", nullable = false, length = 500)
    private Long evaluationUnit;

    @Column(name = "compliance_obligation", length = 500)
    private String complianceObligation;

    @Column(name = "obligation_legal_basis", length = 500)
    private String obligationLegalBasis;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicability_level", nullable = false)
    private ApplicabilityLevel applicabilityLevel;

    @Column(name = "license", length = 500)
    private String license;

    @Column(name = "sanction", length = 500)
    private String sanction;

    @Column(name = "sanction_legal_basis", length = 500)
    private String sanctionLegalBasis;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
