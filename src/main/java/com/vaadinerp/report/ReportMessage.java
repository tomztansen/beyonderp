package com.vaadinerp.report;

/**
 * Pesan notifikasi dari eksekusi script report (before/after script)
 * untuk ditampilkan ke UI pemanggil tanpa mengganggu rendering report.
 * Dirancang ringan (immutable record) untuk mencegah memory leak.
 */
public record ReportMessage(String text, Level level) {

    public enum Level {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    public ReportMessage {
        if (level == null) {
            level = Level.INFO;
        }
    }
}
