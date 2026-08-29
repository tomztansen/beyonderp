package com.vaadinerp.report.render;

import com.vaadinerp.meta.ReportElementMeta;

import java.io.File;
import java.util.List;
import java.util.Map;

/** Konteks render satu report: template + data + parameter + info halaman + band. */
public record ReportContext(
        String reportCode,
        String engineType,
        File template,
        List<Map<String, Object>> data,
        Map<String, Object> params,
        String pageSize,
        String orientation,
        String reportTitle,
        List<ReportElementMeta> elements) {

    public ReportContext {
        elements = (elements == null) ? List.of() : List.copyOf(elements);
    }
}
