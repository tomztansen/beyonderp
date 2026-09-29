package com.vaadinerp.report;

import com.vaadinerp.report.render.ReportOutput;

/**
 * Hasil run report: pakai web viewer Stimulsoft (URL) atau output ter-render (Standard/Jasper).
 * dataEmpty = query report menghasilkan 0 baris -- dipakai buat notifikasi "Data report is
 * empty" di pemanggil (tetap tampilkan output-nya, tidak diblokir). STIMULSOFT selalu false di
 * sini karena data-nya di-fetch belakangan oleh StimulsoftJavaController lewat request browser
 * terpisah, bukan lewat jalur ini.
 */
public record ReportRunResult(boolean stimulsoftViewer, String viewerUrl, ReportOutput output, boolean dataEmpty) {
    public static ReportRunResult stimulsoft(String url) { return new ReportRunResult(true, url, null, false); }
    public static ReportRunResult rendered(ReportOutput out, boolean dataEmpty) {
        return new ReportRunResult(false, null, out, dataEmpty);
    }
}
