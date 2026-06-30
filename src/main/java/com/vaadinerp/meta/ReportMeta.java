package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "meta_report", schema = "public")
@Data
public class ReportMeta {
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

    @OneToMany(mappedBy = "reportMeta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("colOrder ASC")
    private List<ReportElementMeta> elements;
}
