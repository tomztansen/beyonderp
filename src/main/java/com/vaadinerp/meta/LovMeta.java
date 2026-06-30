package com.vaadinerp.meta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "meta_lov", schema = "public")
@Data
public class LovMeta {
    @Id
    @Column(name = "lov_code", length = 50)
    private String lovCode;

    @Column(name = "lov_name", length = 100)
    private String lovName;

    @Column(name = "table_name", length = 100)
    private String tableName;

    @Column(name = "value_column", length = 50)
    private String valueColumn;

    @Column(name = "label_column", length = 50)
    private String labelColumn;

    @Column(name = "search_column", length = 50)
    private String searchColumn;

    // Grid columns configuration: e.g. "dept_code:Kode:100px,dept_name:Nama:200px"
    @Column(name = "grid_columns", length = 500)
    private String gridColumns;

}
