package com.vaadinerp.meta;

import jakarta.persistence.*;

@Entity
@Table(name = "meta_report_param")
public class ReportParamMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_code", nullable = false, length = 50)
    private String reportCode;

    @Column(name = "param_name", nullable = false, length = 50)
    private String paramName;

    @Column(name = "param_label", length = 100)
    private String label;

    /** TEXT | NUMBER | DATE | BOOLEAN | LOV */
    @Column(name = "param_type", length = 20)
    private String dataType = "TEXT";

    @Column(name = "lov_code", length = 100)
    private String lovCode;

    /** FORM_FIELD | USER_INPUT | SYSTEM */
    @Column(name = "source", length = 20)
    private String source = "USER_INPUT";

    /** nama field form (FORM_FIELD) atau keyword (SYSTEM, mis. $CURRENT_USER) */
    @Column(name = "source_key", length = 200)
    private String sourceKey;

    @Column(name = "default_value", length = 255)
    private String defaultValue;

    @Column(name = "required")
    private boolean required = false;

    @Column(name = "col_order")
    private Integer colOrder = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
    public String getParamName() { return paramName; }
    public void setParamName(String paramName) { this.paramName = paramName; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getLovCode() { return lovCode; }
    public void setLovCode(String lovCode) { this.lovCode = lovCode; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public Integer getColOrder() { return colOrder; }
    public void setColOrder(Integer colOrder) { this.colOrder = colOrder; }
}
