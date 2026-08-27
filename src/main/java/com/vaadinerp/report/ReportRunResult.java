package com.vaadinerp.report;

import com.vaadinerp.report.render.ReportOutput;

/** Hasil run report: pakai web viewer Stimulsoft (URL) atau output ter-render (Standard/Jasper). */
public record ReportRunResult(boolean stimulsoftViewer, String viewerUrl, ReportOutput output) {
    public static ReportRunResult stimulsoft(String url) { return new ReportRunResult(true, url, null); }
    public static ReportRunResult rendered(ReportOutput out) { return new ReportRunResult(false, null, out); }
}
