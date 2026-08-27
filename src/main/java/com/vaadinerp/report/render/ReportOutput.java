package com.vaadinerp.report.render;

import java.nio.charset.StandardCharsets;

/** Hasil render: tipe konten + byte. */
public record ReportOutput(String contentType, byte[] bytes) {

    public static ReportOutput pdf(byte[] b) {
        return new ReportOutput("application/pdf", b);
    }

    public static ReportOutput html(String html) {
        return new ReportOutput("text/html;charset=UTF-8", html.getBytes(StandardCharsets.UTF_8));
    }
}
