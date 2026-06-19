package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "meta_field")
@Data
@ToString(exclude = "formMeta")
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

    @Column(name = "is_detail")
    private Boolean isDetail;

    @Column(name = "formula", length = 255)
    private String formula;

    @Column(name = "save_on_insert")
    private Boolean saveOnInsert = true;

    @Column(name = "save_on_update")
    private Boolean saveOnUpdate = true;

    public boolean isDetail() {
        return isDetail != null && isDetail;
    }

    public void setDetail(Boolean isDetail) {
        this.isDetail = isDetail;
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

    @OneToMany(mappedBy = "fieldMeta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private java.util.List<FieldLovTargetMeta> lovTargets;

    @OneToMany(mappedBy = "fieldMeta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private java.util.List<FieldFilterMeta> filters;
}
