package com.vaadinerp.report;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.report.render.ReportOutput;
import com.vaadinerp.views.PortalView;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Jalur bersama untuk menjalankan report dan menampilkan hasilnya sebagai tab aplikasi.
 * Dipakai Report Runner (report standalone) dan tombol Print di GenericFormView, supaya
 * keduanya punya perilaku output, penanganan error, dan eksekusi async yang identik.
 */
public final class ReportLauncher {

    private ReportLauncher() {}

    private static final ExecutorService RUN_EXEC = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "report-run");
        t.setDaemon(true);
        return t;
    });

    /** Nama berkas yang dilihat browser saat membuka output. */
    public static String outputFilename(String contentType) {
        if (contentType == null) return "report.bin";
        if (contentType.contains("pdf")) return "report.pdf";
        if (contentType.startsWith("text/html")) return "report.html";
        if (contentType.contains("spreadsheetml") || contentType.contains("excel") || contentType.contains("xls")) return "report.xlsx";
        return "report.bin";
    }

    /** Id tab portal untuk sebuah report; sama untuk pemanggil mana pun agar tab tidak berganda. */
    public static String tabId(String reportCode) {
        return "RPT_OUT_" + reportCode;
    }

    /** Bangun komponen output: viewer Stimulsoft, atau berkas hasil render di IFrame. */
    public static Component buildOutput(ReportRunResult res) {
        VerticalLayout box = new VerticalLayout();
        box.setSizeFull();
        box.setPadding(false);
        box.setSpacing(false);
        if (res.stimulsoftViewer()) {
            IFrame ifr = new IFrame(res.viewerUrl());
            ifr.setSizeFull();
            // min-height:0 wajib ikut: tanpa itu IFrame viewer Stimulsoft tidak mengecil
            // di dalam flex container. Perbaikan ini sudah ada di ReportRunnerView (commit
            // fc539ec) dan HARUS terbawa saat method ini dipindahkan ke sini.
            ifr.getStyle().set("border", "none").set("min-height", "0");
            box.add(ifr);
            box.setFlexGrow(1, ifr);
            return box;
        }
        // StreamResource menghindari pengiriman byte lewat WebSocket — browser mengambil via HTTP.
        ReportOutput out = res.output();
        byte[] bytes = out.bytes();
        StreamResource sr = new StreamResource(
                outputFilename(out.contentType()), () -> new ByteArrayInputStream(bytes));
        sr.setContentType(out.contentType());
        IFrame ifr = new IFrame();
        ifr.getElement().setAttribute("src", sr);
        ifr.setSizeFull();
        ifr.getStyle().set("border", "none");
        box.add(ifr);
        box.setFlexGrow(1, ifr);
        return box;
    }

    /**
     * Unduh hasil render sebagai berkas. Anchor tersembunyi + klik programatis: pola
     * download Vaadin yang tidak menavigasi SPA (window.location.href akan me-reload UI).
     */
    private static void download(UI ui, ReportOutput out) {
        if (out == null || out.bytes() == null) return;
        byte[] bytes = out.bytes();
        String name = outputFilename(out.contentType());
        Anchor a = new Anchor(DownloadHandler.fromInputStream(e -> new DownloadResponse(
                new ByteArrayInputStream(bytes), name, out.contentType(), bytes.length)), "");
        a.getElement().setAttribute("download", true);
        a.getStyle().set("display", "none");
        ui.add(a);
        a.getElement().executeJs("this.click(); setTimeout(() => this.remove(), 0);");
    }

    /** Cari PortalView pembungkus: naik lewat parent, lalu telusuri anak UI. */
    public static PortalView findPortal(Component origin) {
        Component c = origin;
        while (c != null) {
            if (c instanceof PortalView pv) return pv;
            c = c.getParent().orElse(null);
        }
        UI ui = UI.getCurrent();
        if (ui != null) {
            for (Component child : ui.getChildren().toList()) {
                if (child instanceof PortalView pv) return pv;
            }
        }
        return null;
    }

    /**
     * Jalankan report di thread latar lalu buka hasilnya sebagai tab. {@code onFinish}
     * dijalankan di thread UI setelah sukses maupun gagal, untuk memulihkan panel pemanggil.
     */
    public static void runAndOpenTab(Component origin, ReportRunService svc, ReportMeta report,
                                     Map<String, Object> values, String format, Runnable onFinish) {
        UI ui = UI.getCurrent();
        String title = report.getReportTitle() != null ? report.getReportTitle() : report.getReportCode();
        RUN_EXEC.submit(() -> {
            try {
                ReportRunResult res = svc.run(report, values, format, false);
                ui.access(() -> {
                    if ("EXCEL".equalsIgnoreCase(format) || "XLSX".equalsIgnoreCase(format)) {
                        download(ui, res.output());
                    } else {
                        PortalView portal = findPortal(origin);
                        if (portal == null) {
                            Notification.show("Cannot find app shell to open output tab.");
                        } else {
                            portal.openComponentTab(tabId(report.getReportCode()), title, buildOutput(res));
                        }
                    }
                    if (onFinish != null) onFinish.run();
                });
            } catch (org.springframework.dao.QueryTimeoutException te) {
                ui.access(() -> {
                    Notification.show("The report query took too long and was stopped. "
                            + "Please narrow your filter/parameters.");
                    if (onFinish != null) onFinish.run();
                });
            } catch (Exception ex) {
                ui.access(() -> {
                    Notification.show("Failed to run report: "
                            + (ex.getMessage() != null ? ex.getMessage() : ex.toString()));
                    if (onFinish != null) onFinish.run();
                });
            }
        });
    }
}
