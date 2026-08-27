package com.vaadinerp.report.render;

/** Strategy render per engine. */
public interface ReportRenderer {

    /** Nilai engineType yang ditangani: STANDARD | STIMULSOFT | JASPER. */
    String engine();

    ReportOutput render(ReportContext ctx);

    ReportOutput export(ReportContext ctx, String format);
}
