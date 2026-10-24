package com.vaadinerp.service;

/**
 * Dilempar script yang berjalan ATOMIK (scope ON_SAVE) untuk membatalkan seluruh penyimpanan.
 * RuntimeException, jadi transaksi Spring otomatis di-rollback; pesannya ditampilkan apa adanya
 * ke user oleh handler Save di form ("Failed to Save: ...").
 */
public class ScriptAbortException extends RuntimeException {

    public ScriptAbortException(String message) {
        super(message);
    }

    public ScriptAbortException(String message, Throwable cause) {
        super(message, cause);
    }
}
