package com.vaadinerp.report.render;

import java.nio.charset.StandardCharsets;

/**
 * Hasil render: tipe konten + byte.
 *
 * noData: true = renderer sendiri menyatakan hasilnya tanpa halaman (Jasper dengan query
 * internal yang tidak menghasilkan baris), false = ada isi, null = renderer tidak tahu.
 * Dipakai ReportRunService untuk notifikasi "Data report is empty" bila aplikasi tidak
 * memegang data-nya sendiri.
 */
public record ReportOutput(String contentType, byte[] bytes, Boolean noData) {

    public ReportOutput(String contentType, byte[] bytes) {
        this(contentType, bytes, null);
    }

    public static ReportOutput pdf(byte[] b) {
        return new ReportOutput("application/pdf", b);
    }

    public static ReportOutput html(String html) {
        return new ReportOutput("text/html;charset=UTF-8", html.getBytes(StandardCharsets.UTF_8));
    }
}
