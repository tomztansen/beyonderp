package com.vaadinerp.report.render;

import java.io.File;
import java.util.List;
import java.util.Map;

/** Konteks render satu report: template + data + parameter + info halaman. */
public record ReportContext(
        String reportCode,
        String engineType,
        File template,
        List<Map<String, Object>> data,
        Map<String, Object> params,
        String pageSize,
        String orientation) {}
