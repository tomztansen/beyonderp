package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "meta_report_element", schema = "public")
@Data
@ToString(exclude = "reportMeta")
public class ReportElementMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_code")
    private ReportMeta reportMeta;

    @Column(name = "band_type", length = 30)
    private String bandType; // TITLE, PAGE_HEADER, COLUMN_HEADER, DETAIL, PAGE_FOOTER, SUMMARY

    @Column(name = "element_type", length = 20)
    private String elementType; // LABEL, FIELD, SYSTEM

    @Column(name = "element_value", length = 250)
    private String elementValue; // Static text, column name, or system function

    @Column(name = "column_width", length = 20)
    private String columnWidth; // e.g. 120px or 15%

    @Column(name = "alignment", length = 20)
    private String alignment; // LEFT, CENTER, RIGHT

    @Column(name = "font_weight", length = 20)
    private String fontWeight; // NORMAL, BOLD

    @Column(name = "col_order")
    private Integer colOrder;

    @Column(name = "format_pattern", length = 50)
    private String formatPattern;
}
