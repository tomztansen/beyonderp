package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "meta_form", schema = "public")
@EntityListeners(com.vaadinerp.service.AuditEntityListener.class)
@Getter
@Setter
@EqualsAndHashCode(of = "formCode")
@ToString(exclude = "fields")
public class FormMeta {
    @Id
    @Column(name = "form_code", length = 50)
    private String formCode;

    @Column(name = "form_title", length = 100)
    private String formTitle;

    @Column(name = "table_name", length = 100)
    private String tableName;

    @Column(name = "view_table", columnDefinition = "TEXT")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.LONGVARCHAR)
    private String viewTable;

    @Column(name = "primary_key", length = 50)
    private String primaryKey;

    @Column(name = "label_width")
    private String labelWidth;

    @Column(name = "default_sort_field", length = 50)
    private String defaultSortField;

    @Column(name = "default_sort_direction", length = 10)
    private String defaultSortDirection; // "ASC" or "DESC"

    @Column(name = "form_type", length = 20)
    private String formType = "SINGLE"; // "SINGLE" or "MASTER_DETAIL"

    @Column(name = "detail_table_name", length = 100)
    private String detailTableName;

    @Column(name = "detail_primary_key", length = 50)
    private String detailPrimaryKey;

    @Column(name = "detail_foreign_key", length = 50)
    private String detailForeignKey;

    @Column(name = "extra_toolbars", columnDefinition = "TEXT")
    private String extraToolbars;

    @OneToMany(mappedBy = "formMeta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("rowGroup ASC, colOrder ASC")
    private List<FieldMeta> fields;
}
