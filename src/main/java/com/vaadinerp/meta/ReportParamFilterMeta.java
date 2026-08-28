package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** Filter LOV per parameter report (reuse pola FieldFilterMeta). STATIC = nilai tetap, FIELD = nilai parameter lain (cascading). */
@Entity
@Table(name = "meta_report_param_filter", schema = "public")
@EntityListeners(com.vaadinerp.service.AuditEntityListener.class)
@Getter
@Setter
@ToString(exclude = "paramMeta")
public class ReportParamFilterMeta extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "param_id", nullable = false)
    private ReportParamMeta paramMeta;

    /** Kolom di tabel LOV yang difilter, mis. custgroup. */
    @Column(name = "filter_column", length = 50, nullable = false)
    private String filterColumn;

    /** "STATIC" | "FIELD". */
    @Column(name = "source_type", length = 50, nullable = false)
    private String sourceType = "STATIC";

    /** STATIC: nilai tetap (Exp_3rd). FIELD: nama parameter lain. */
    @Column(name = "source_name", length = 255, nullable = false)
    private String sourceName;

    @Column(name = "comparison_operator", length = 10)
    private String comparisonOperator = "=";

    @Column(name = "logical_operator", length = 10)
    private String logicalOperator = "AND";
}
