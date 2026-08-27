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
    private String paramType = "STRING"; // STRING, DATE, NUMBER

    @Column(name = "default_value", length = 255)
    private String defaultValue;
    
    @Column(name = "col_order")
    private Integer colOrder = 0;
}
