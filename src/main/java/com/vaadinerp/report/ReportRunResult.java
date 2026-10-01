package com.vaadinerp.report;

import com.vaadinerp.report.render.ReportOutput;
import java.util.List;

/**
 * Hasil run report: pakai web viewer Stimulsoft (URL) atau output ter-render (Standard/Jasper).
 * dataEmpty = query report menghasilkan 0 baris -- dipakai buat notifikasi "Data report is
 * empty" di pemanggil (tetap tampilkan output-nya, tidak diblokir). STIMULSOFT selalu false di
 * sini karena data-nya di-fetch belakangan oleh StimulsoftJavaController lewat request browser
 * terpisah, bukan lewat jalur ini.
 * messages = daftar pesan informasi/sukses yang dihasilkan oleh before/after script untuk UI.
 */
public record ReportRunResult(boolean stimulsoftViewer, String viewerUrl, ReportOutput output, boolean dataEmpty, List<ReportMessage> messages) {

    public ReportRunResult(boolean stimulsoftViewer, String viewerUrl, ReportOutput output, boolean dataEmpty) {
        this(stimulsoftViewer, viewerUrl, output, dataEmpty, List.of());
    }

    public static ReportRunResult stimulsoft(String url) {
        return new ReportRunResult(true, url, null, false, List.of());
    }

    public static ReportRunResult stimulsoft(String url, List<ReportMessage> messages) {
        return new ReportRunResult(true, url, null, false, messages != null ? List.copyOf(messages) : List.of());
    }

    public static ReportRunResult rendered(ReportOutput out, boolean dataEmpty) {
        return new ReportRunResult(false, null, out, dataEmpty, List.of());
    }

    public static ReportRunResult rendered(ReportOutput out, boolean dataEmpty, List<ReportMessage> messages) {
        return new ReportRunResult(false, null, out, dataEmpty, messages != null ? List.copyOf(messages) : List.of());
    }
}
