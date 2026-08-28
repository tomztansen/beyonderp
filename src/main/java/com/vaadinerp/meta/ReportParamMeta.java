package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "meta_report_param", schema = "public")
@EntityListeners(com.vaadinerp.service.AuditEntityListener.class)
@Getter
@Setter
public class ReportParamMeta extends BaseAuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "report_code", nullable = false)
    private ReportMeta reportMeta;

    @Column(name = "param_name", length = 50, nullable = false)
    private String paramName; // Contoh: :tanggal_awal, :cabang

    @Column(name = "param_label", length = 100)
    private String paramLabel; // Contoh: Tanggal Awal

    @Column(name = "param_type", length = 20)
    private String paramType = "STRING"; // STRING/TEXT, DATE, NUMBER, BOOLEAN, LOV

    @Column(name = "default_value", length = 255)
    private String defaultValue;

    @Column(name = "col_order")
    private Integer colOrder = 0;

    /** Kode LOV bila paramType = LOV. */
    @Column(name = "lov_code", length = 100)
    private String lovCode;

    /** FORM_FIELD | USER_INPUT | SYSTEM. */
    @Column(name = "source", length = 20)
    private String source = "USER_INPUT";

    /** nama field form (FORM_FIELD) atau keyword (SYSTEM, mis. $CURRENT_USER). */
    @Column(name = "source_key", length = 200)
    private String sourceKey;

    @Column(name = "required")
    private boolean required = false;

    /** Model B: kolom DB yang difilter. */
    @Column(name = "filter_column", length = 100)
    private String filterColumn;

    /** Model B: operator perbandingan (=, LIKE, ILIKE, IN, >=, <=, ...). */
    @Column(name = "operator", length = 20)
    private String operator;

    /** LOV filter (reusable LOV): batasi LOV generik, mis. custgroup = 'Exp_3rd'. */
    @Column(name = "lov_filter_column", length = 50)
    private String lovFilterColumn;

    @Column(name = "lov_filter_value", length = 255)
    private String lovFilterValue;

    @Column(name = "lov_filter_operator", length = 10)
    private String lovFilterOperator;

    /** Filter LOV multi (STATIC + FIELD/cascading). Menggantikan lov_filter_* datar. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "paramMeta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private java.util.List<ReportParamFilterMeta> filters = new java.util.ArrayList<>();
}
