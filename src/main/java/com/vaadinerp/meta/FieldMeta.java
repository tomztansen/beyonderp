package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import lombok.EqualsAndHashCode;

@Entity
@Table(name = "meta_field", schema = "public")
@Data
@EqualsAndHashCode(of = {"id", "fieldName"})
@ToString(exclude = {"formMeta", "lovTargets"})
public class FieldMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "form_code")
    private FormMeta formMeta;

    @Column(name = "field_name", length = 50)
    private String fieldName;

    @Column(name = "field_label", length = 100)
    private String fieldLabel;

    @Column(name = "component_type", length = 50)
    private String componentType;

    @Column(name = "row_group")
    private Integer rowGroup;

    @Column(name = "col_order")
    private Integer colOrder;

    @Column(name = "is_required")
    private boolean isRequired;

    @Column(name = "is_readonly")
    private boolean isReadonly;

    @Column(name = "lov_code", length = 50)
    private String lovCode;

    @Column(name = "show_in_grid")
    private boolean showInGrid;

    @Column(name = "hide_in_form")
    private Boolean hideInForm = false;

    @Column(name = "is_detail")
    private Boolean isDetail;

    @Column(name = "is_sortable")
    private Boolean isSortable = true;

    @Column(name = "formula", length = 255)
    private String formula;

    @Column(name = "save_on_insert")
    private Boolean saveOnInsert = true;

    @Column(name = "save_on_update")
    private Boolean saveOnUpdate = true;

    @Column(name = "validation_rule", length = 100)
    private String validationRule;

    @Column(name = "display_format", length = 50)
    private String displayFormat;

    @Column(name = "sequence_code", length = 50)
    private String sequenceCode;

    @Column(name = "is_audit_log")
    private Boolean isAuditLog = false;

    public boolean isDetail() {
        return isDetail != null && isDetail;
    }

    public void setDetail(Boolean isDetail) {
        this.isDetail = isDetail;
    }

    public boolean isSortable() {
        return isSortable == null || isSortable;
    }

    public void setSortable(Boolean sortable) {
        this.isSortable = sortable;
    }

    public boolean isHideInForm() {
        return hideInForm != null && hideInForm;
    }

    public void setHideInForm(Boolean hideInForm) {
        this.hideInForm = hideInForm;
    }

    public boolean isSaveOnInsert() {
        return saveOnInsert == null || saveOnInsert;
    }

    public void setSaveOnInsert(Boolean saveOnInsert) {
        this.saveOnInsert = saveOnInsert;
    }

    public boolean isSaveOnUpdate() {
        return saveOnUpdate == null || saveOnUpdate;
    }

    public void setSaveOnUpdate(Boolean saveOnUpdate) {
        this.saveOnUpdate = saveOnUpdate;
    }

    public boolean isAuditLog() {
        return isAuditLog != null && isAuditLog;
    }

    public void setAuditLog(Boolean auditLog) {
        this.isAuditLog = auditLog;
    }

    @OneToMany(mappedBy = "fieldMeta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private java.util.List<FieldLovTargetMeta> lovTargets;

    @OneToMany(mappedBy = "fieldMeta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private java.util.List<FieldFilterMeta> filters;
}
