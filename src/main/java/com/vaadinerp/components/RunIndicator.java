package com.vaadinerp.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tanda "report sedang berjalan": memakai ulang bar loading yang sudah ada di tema ({@code .v-loading-indicator}, bar
 * tipis di atas halaman dengan spinner di pojok, sama dengan yang terlihat saat membuka form), ditambah penjaga supaya
 * report yang sama tidak dijalankan dua kali bersamaan.
 *
 * <p>Bar bawaan Vaadin hanya tampil selama ada permintaan ke server yang sedang berjalan, padahal report dijalankan di
 * thread latar sehingga permintaan klik selesai seketika. Kelas ini memasang elemen dengan kelas yang sama dan
 * menjaganya tampil sampai report selesai.
 *
 * <p>Sengaja TIDAK mengubah status enabled/disabled tombol mana pun: SafeButton mengatur {@code disabled}-nya sendiri
 * lewat {@code setEnabled}, dan menumpang mekanisme itu pernah membuat tombol Print macet. Klik kedua saat masih
 * berjalan cukup diabaikan oleh pemanggil (nilai balik {@code null}).
 *
 * <p>Tahapan tampilan (muncul, lalu lebih panjang) ditiru di browser dengan SATU pemanggilan {@code executeJs}, dengan
 * jeda yang sama seperti pengaturan loading indicator aplikasi (lihat {@code Application.java}): tanpa timer atau
 * polling di server, dan report yang cepat tidak menampilkan apa pun.
 */
public final class RunIndicator {

    /** Kelas bar loading yang sudah didefinisikan di tema (frontend/themes/vaadinerp/styles.css). */
    public static final String CSS_CLASS = "v-loading-indicator";

    // Sama dengan getLoadingIndicatorConfiguration() di Application.java (first/second/third delay).
    private static final String STAGES_JS = """
            const el = $0;
            setTimeout(() => { if (el.isConnected) { el.style.display = ''; el.classList.add('first'); } }, 150);
            setTimeout(() => { if (el.isConnected) el.classList.replace('first', 'second'); }, 1000);
            setTimeout(() => { if (el.isConnected) el.classList.replace('second', 'third'); }, 3000);
            """;

    private volatile boolean running;

    /**
     * Mulai satu run. Mengembalikan pemulih (aman dipanggil berkali-kali; panggilan ganda dari run yang sudah selesai
     * tidak menghentikan run berikutnya), atau {@code null} bila masih ada run yang berjalan. {@code ui} null berarti
     * tanpa bar (hanya penjaga).
     */
    public Runnable tryStart(UI ui) {
        if (running) {
            return null;
        }
        running = true;
        Div bar = null;
        if (ui != null) {
            bar = new Div();
            bar.addClassName(CSS_CLASS);
            bar.getStyle().set("display", "none"); // dimunculkan browser setelah jeda (STAGES_JS)
            ui.add(bar);
            bar.getElement().executeJs(STAGES_JS, bar.getElement());
        }
        final Div shown = bar;
        final AtomicBoolean finished = new AtomicBoolean(false);
        return () -> {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            if (shown != null) {
                shown.removeFromParent();
            }
            running = false;
        };
    }
}
