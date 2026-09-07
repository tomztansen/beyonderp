package com.vaadinerp.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializableConsumer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Editor kode berbasis CodeMirror 5 (dimuat dari CDN) yang bisa dipakai ulang di
 * dialog mana pun.
 *
 * Sebelumnya seluruh JavaScript-nya ditulis inline di dalam satu method dialog di
 * FormActionBuilderView, sehingga tidak bisa dipakai di dialog script lain tanpa
 * menyalin ~90 baris JS. Semua detail itu sekarang tinggal di sini.
 *
 * Nilai editor hidup di sisi klien, jadi {@link #getValue(SerializableConsumer)}
 * bersifat asinkron — Java tidak bisa membaca isinya secara langsung.
 */
public class CodeEditorField extends Div {

    private static final String CM_VERSION = "5.65.16";
    private static final AtomicLong SEQ = new AtomicLong();

    /** Nama mode CodeMirror, mis. "groovy" atau "text/x-sql". */
    private final String mode;
    /** Berkas mode yang perlu dimuat dari CDN, mis. "groovy/groovy". */
    private final String modeScript;
    /** Kunci instance CodeMirror pada objek window browser. */
    private final String cmVar;
    /** Id elemen host di DOM. Dipisah dari cmVar supaya keduanya tidak tertukar. */
    private final String elementId;

    private String pendingValue = "";
    private boolean pendingReadOnly = false;

    public CodeEditorField(String mode, String modeScript) {
        this.mode = mode;
        this.modeScript = modeScript;
        this.cmVar = "_cm_" + SEQ.incrementAndGet() + "_" + System.currentTimeMillis();
        this.elementId = "cm-host-" + cmVar;
        setId(elementId);
        // Default: isi tinggi kontainer induknya (dialog). Bisa ditimpa pemanggil.
        getStyle().set("flex", "1 1 0").set("min-height", "0").set("overflow", "hidden");
    }

    public static CodeEditorField groovy() {
        return new CodeEditorField("groovy", "groovy/groovy");
    }

    public static CodeEditorField sql() {
        return new CodeEditorField("text/x-sql", "sql/sql");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Dialog memasang isinya saat dibuka dan melepasnya saat ditutup, jadi method
        // ini bisa terpanggil berkali-kali untuk instance yang sama.
        attachEvent.getUI().getPage().executeJs(
                """
                        (function(editorId, cmVar, initVal, mode, modeScript, ver, readOnly) {
                            function init() {
                                var el = document.getElementById(editorId);
                                if (!el) {
                                    // Jangan gagal diam-diam: editor kosong tanpa jejak sulit dilacak.
                                    console.warn('CodeEditorField: host element not found', editorId);
                                    return;
                                }
                                el.innerHTML = '';
                                // Kunci font monospace, ukuran, dan tinggi baris agar konsisten di semua baris
                                var styleId = 'cmstyle-' + editorId;
                                if (!document.getElementById(styleId)) {
                                    var s = document.createElement('style');
                                    s.id = styleId;
                                    s.textContent = '#' + editorId + ' .CodeMirror { font-family: Consolas, "Fira Code", Monaco, "Courier New", monospace !important; font-size: 13px !important; line-height: 20px !important; }' +
                                                    '#' + editorId + ' .CodeMirror pre.CodeMirror-line, #' + editorId + ' .CodeMirror pre.CodeMirror-line-like { font-family: inherit !important; font-size: inherit !important; line-height: 20px !important; }' +
                                                    '#' + editorId + ' .CodeMirror-lines { padding: 4px 0 !important; }' +
                                                    '#' + editorId + ' .CodeMirror-cursor { height: 20px !important; border-left: 2px solid #000 !important; }' +
                                                    '#' + editorId + ' .CodeMirror-scroll { overflow-x: hidden !important; }';
                                    document.head.appendChild(s);
                                }
                                var cm = CodeMirror(el, {
                                    value: initVal,
                                    mode: mode,
                                    theme: 'eclipse',
                                    lineNumbers: true,
                                    indentWithTabs: false,
                                    indentUnit: 4,
                                    tabSize: 4,
                                    lineWrapping: true,
                                    readOnly: readOnly,
                                    extraKeys: {Tab: 'indentMore', 'Shift-Tab': 'indentLess'}
                                });
                                window[cmVar] = cm;

                                function syncLayout() {
                                    if (!el || !document.getElementById(editorId)) return;
                                    var h = el.clientHeight;
                                    cm.setSize('100%', h > 50 ? h : 400);
                                    cm.refresh();
                                    placeCursor(cm);
                                }

                                // 1. Sinkronisasi awal di frame pertama
                                requestAnimationFrame(function() {
                                    requestAnimationFrame(syncLayout);
                                });

                                // 2. Sinkronisasi bertahap untuk menunggu animasi dialog Vaadin selesai stabil
                                setTimeout(syncLayout, 150);
                                setTimeout(syncLayout, 300);
                                setTimeout(syncLayout, 500);

                                // 3. Sinkronisasi saat font browser selesai dimuat
                                if (document.fonts && document.fonts.ready) {
                                    document.fonts.ready.then(syncLayout);
                                }

                                // 4. Segarkan saat menerima fokus
                                cm.on('focus', function() {
                                    cm.refresh();
                                });

                                // 5. Segarkan saat jendela diubah ukurannya
                                var onResize = function() {
                                    if (document.getElementById(editorId)) {
                                        var h = el.clientHeight;
                                        cm.setSize('100%', h > 50 ? h : 400);
                                        cm.refresh();
                                    }
                                };
                                window.addEventListener('resize', onResize);
                                window[cmVar + '_resize'] = onResize;
                            }
                            // Fokuskan editor begitu terbuka supaya user tidak perlu klik
                            // dulu. Script kosong mulai di baris pertama; script yang sudah
                            // ada dilanjutkan dari akhir, tempat orang biasanya menambah kode.
                            function placeCursor(cm) {
                                cm.refresh();
                                if (cm.getValue().trim() === '') {
                                    cm.setCursor({line: 0, ch: 0});
                                } else {
                                    var last = cm.lastLine();
                                    cm.setCursor({line: last, ch: cm.getLine(last).length});
                                }
                                if (!cm.getOption('readOnly')) cm.focus();
                                cm.scrollIntoView(null);
                            }
                            function loadScript(src, cb) {
                                var existing = document.querySelector('script[src="' + src + '"]');
                                if (existing) {
                                    if (existing.dataset.loaded === '1') { cb(); }
                                    else { existing.addEventListener('load', cb); }
                                    return;
                                }
                                var s = document.createElement('script');
                                s.src = src;
                                s.onload = function() { s.dataset.loaded = '1'; cb(); };
                                document.head.appendChild(s);
                            }
                            function loadCss(href, id) {
                                if (document.getElementById(id)) return;
                                var l = document.createElement('link');
                                l.id = id; l.rel = 'stylesheet'; l.href = href;
                                document.head.appendChild(l);
                            }
                            var base = 'https://cdnjs.cloudflare.com/ajax/libs/codemirror/' + ver + '/';
                            loadCss(base + 'codemirror.min.css', 'cm5-css');
                            loadCss(base + 'theme/eclipse.min.css', 'cm5-theme-css');
                            function withMode() {
                                loadScript(base + 'mode/' + modeScript + '.min.js', init);
                            }
                            if (typeof CodeMirror !== 'undefined') { withMode(); return; }
                            loadScript(base + 'codemirror.min.js', withMode);
                        })($0, $1, $2, $3, $4, $5, $6)
                        """,
                elementId, cmVar, pendingValue, mode, modeScript, CM_VERSION, pendingReadOnly);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // Bersihkan seluruh jejak di browser. Tanpa ini, setiap kali dialog dibuka
        // akan tertinggal satu properti window, satu <style>, dan satu pohon DOM
        // CodeMirror yang tidak pernah bisa di-GC.
        detachEvent.getUI().getPage().executeJs(
                """
                        (function(editorId, cmVar) {
                            var el = document.getElementById(editorId);
                            if (el) el.innerHTML = '';
                            var s = document.getElementById('cmstyle-' + editorId);
                            if (s && s.parentNode) s.parentNode.removeChild(s);
                            if (window[cmVar + '_resize']) {
                                window.removeEventListener('resize', window[cmVar + '_resize']);
                                try { delete window[cmVar + '_resize']; } catch (e) { window[cmVar + '_resize'] = undefined; }
                            }
                            try { delete window[cmVar]; } catch (e) { window[cmVar] = undefined; }
                        })($0, $1)
                        """,
                elementId, cmVar);
        super.onDetach(detachEvent);
    }

    /**
     * Isi editor. Aman dipanggil sebelum komponen terpasang — nilainya disimpan dan
     * dipakai saat editor dibuat.
     */
    public void setValue(String value) {
        this.pendingValue = value != null ? value : "";
        // setValue menaruh kursor di akhir dokumen dan ikut menggulir ke sana, sehingga
        // script yang baru dimuat terbuka di tengah-tengah. Kembalikan ke baris pertama.
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                """
                        if (window[$0]) {
                            var cm = window[$0];
                            cm.setValue($1);
                            cm.refresh();
                            if (cm.getValue().trim() === '') {
                                cm.setCursor({line: 0, ch: 0});
                            } else {
                                var last = cm.lastLine();
                                cm.setCursor({line: last, ch: cm.getLine(last).length});
                            }
                            if (!cm.getOption('readOnly')) cm.focus();
                            cm.scrollIntoView(null);
                        }
                        """,
                cmVar, pendingValue));
    }

    /**
     * Ambil isi editor. Asinkron karena nilainya ada di browser; callback dipanggil
     * dengan string kosong bila editor belum sempat terbentuk.
     */
    public void getValue(SerializableConsumer<String> callback) {
        getUI().ifPresent(ui -> ui.getPage()
                .executeJs("return window[$0] ? window[$0].getValue() : ''", cmVar)
                .then(String.class, callback::accept));
    }

    /**
     * Kunci editor agar hanya bisa dibaca. Disimpan juga sebagai state supaya tetap
     * berlaku bila editor dibuat ulang saat dialog dibuka kembali.
     */
    public void setReadOnly(boolean readOnly) {
        this.pendingReadOnly = readOnly;
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "if (window[$0]) window[$0].setOption('readOnly', $1)", cmVar, readOnly));
    }

    /** Rapikan indentasi seluruh isi editor memakai indentAuto milik CodeMirror. */
    public void format() {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                """
                        if (window[$0]) {
                            var cm = window[$0];
                            var cursor = cm.getCursor();
                            cm.execCommand('selectAll');
                            cm.execCommand('indentAuto');
                            cm.setCursor(cursor);
                            cm.focus();
                        }
                        """,
                cmVar));
    }

    /** Sisipkan teks tepat di posisi kursor — cocok untuk picker nama variabel. */
    public void insertAtCursor(String text) {
        if (text == null || text.isEmpty())
            return;
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                """
                        if (window[$0]) {
                            var cm = window[$0];
                            cm.replaceSelection($1);
                            cm.focus();
                        }
                        """,
                cmVar, text));
    }

    /** Sisipkan potongan kode di akhir editor, dipisahkan baris kosong bila perlu. */
    public void appendSnippet(String snippet) {
        if (snippet == null || snippet.isEmpty())
            return;
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                """
                        if (window[$0]) {
                            var cm = window[$0];
                            var ins = cm.getValue().trim() === '' ? $1 : '\\n\\n' + $1;
                            cm.replaceRange(ins, {line: cm.lastLine(), ch: cm.getLine(cm.lastLine()).length});
                            cm.focus();
                        }
                        """,
                cmVar, snippet));
    }
}
