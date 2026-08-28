package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "meta_report", schema = "public")
@EntityListeners(com.vaadinerp.service.AuditEntityListener.class)
@Getter
@Setter
public class ReportMeta extends BaseAuditableEntity {
    @Id
    @Column(name = "report_code", length = 50)
    private String reportCode;

    @Column(name = "report_title", length = 100)
    private String reportTitle;

    @Column(name = "table_name", length = 100)
    private String tableName;

    @Column(name = "page_size", length = 20)
    private String pageSize; // A4, LETTER

    @Column(name = "orientation", length = 20)
    private String orientation; // PORTRAIT, LANDSCAPE
    @Column(name = "engine_type", length = 20)
    private String engineType; // STANDARD, STIMULSOFT, JASPER

    @Column(name = "template_path", length = 255)
    private String templatePath; // Path for .mrt or .jrxml files

    @Column(name = "data_query", columnDefinition = "TEXT")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    private String dataQuery;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "description", length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meta_report_role", schema = "public",
            joinColumns = @JoinColumn(name = "report_code"))
    @Column(name = "role_code")
    private java.util.Set<String> allowedRoles = new java.util.HashSet<>();

    @OneToMany(mappedBy = "reportMeta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("colOrder ASC")
    private List<ReportParamMeta> params;

    @OneToMany(mappedBy = "reportMeta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("colOrder ASC")
    private List<ReportElementMeta> elements;
}
