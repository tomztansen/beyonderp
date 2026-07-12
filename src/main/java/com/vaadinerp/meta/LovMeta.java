package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "meta_lov", schema = "public")
@EntityListeners(com.vaadinerp.service.AuditEntityListener.class)
@Data
public class LovMeta {
    @Id
    @Column(name = "lov_code", length = 50)
    private String lovCode;

    @Column(name = "lov_name", columnDefinition = "TEXT")
    private String lovName;

    @Column(name = "table_name", columnDefinition = "TEXT")
    private String tableName;

    @Column(name = "value_column", columnDefinition = "TEXT")
    private String valueColumn;

    @Column(name = "label_column", columnDefinition = "TEXT")
    private String labelColumn;

    @Column(name = "search_column", columnDefinition = "TEXT")
    private String searchColumn;

    // Grid columns configuration: e.g. "dept_code:Kode:100px,dept_name:Nama:200px"
    @Column(name = "grid_columns", columnDefinition = "TEXT")
    private String gridColumns;

}
