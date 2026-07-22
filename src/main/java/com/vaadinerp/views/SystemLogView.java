package com.vaadinerp.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadinerp.security.service.SessionSecurityService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

@Route("system-log-viewer")
public class SystemLogView extends VerticalLayout {

    private final SessionSecurityService securityService;
    private final TextField logFilePathField = new TextField("Lokasi File Log (Path)");
    private final TextField searchKeywordField = new TextField("Search Keyword (Keyword Filter)");
    private final Checkbox autoRefreshCheckbox = new Checkbox("Auto-Refresh Real-Time (3 detik)");
    private final Span statusBadge = new Span("Status: Standby");
    private final Pre logContainer = new Pre();

    public SystemLogView(SessionSecurityService securityService) {
        this.securityService = securityService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header Title
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        H2 title = new H2("📜 Server Log Viewer (Real-Time Web Console)");
        title.getStyle().set("margin", "0").set("color", "#1e293b").set("font-weight", "700");

        Span subtitle = new Span("Pantau file log server secara langsung tanpa perlu masuk SSH atau install tool eksternal.");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "14px");

        VerticalLayout titleBox = new VerticalLayout(title, subtitle);
        titleBox.setPadding(false);
        titleBox.setSpacing(false);
        header.add(titleBox);

        // Cari file log default yang ada di server/laptop
        String defaultPath = detectDefaultLogFile();
        logFilePathField.setValue(defaultPath);
        logFilePathField.setWidth("350px");

        searchKeywordField.setPlaceholder("Contoh: ERROR, Exception, atau isunique...");
        searchKeywordField.setWidth("250px");
        searchKeywordField.setClearButtonVisible(true);
        searchKeywordField.addValueChangeListener(e -> refreshLogContent());

        Button refreshBtn = new Button("🔄 Refresh Sekarang", new Icon(VaadinIcon.REFRESH));
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshBtn.addClickListener(e -> refreshLogContent());

        autoRefreshCheckbox.addValueChangeListener(e -> {
            UI ui = UI.getCurrent();
            if (ui != null) {
                if (e.getValue()) {
                    ui.setPollInterval(3000);
                    statusBadge.setText("🟢 Live Auto-Refresh Active");
                    statusBadge.getStyle().set("color", "#16a34a").set("font-weight", "bold");
                } else {
                    ui.setPollInterval(-1);
                    statusBadge.setText("⚪ Auto-Refresh Paused");
                    statusBadge.getStyle().set("color", "#64748b").set("font-weight", "normal");
                }
            }
        });

        // Tombol Download File Log (.log)
        Anchor downloadAnchor = new Anchor();
        downloadAnchor.getElement().setAttribute("download", true);
        Button downloadBtn = new Button("⬇️ Download Log File", new Icon(VaadinIcon.DOWNLOAD));
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        downloadAnchor.add(downloadBtn);
        downloadAnchor.setHref(DownloadHandler.fromInputStream(event -> {
            File f = new File(logFilePathField.getValue().trim());
            if (f.exists() && f.isFile()) {
                try {
                    return new DownloadResponse(
                            new java.io.FileInputStream(f),
                            "server-export.log",
                            "text/plain",
                            f.length()
                    );
                } catch (Exception ex) {
                    byte[] errBytes = ("Failed to read file: " + ex.getMessage()).getBytes(StandardCharsets.UTF_8);
                    return new DownloadResponse(
                            new ByteArrayInputStream(errBytes),
                            "error.log",
                            "text/plain",
                            errBytes.length
                    );
                }
            }
            byte[] notFoundBytes = "Log file not found at the specified path.".getBytes(StandardCharsets.UTF_8);
            return new DownloadResponse(
                    new ByteArrayInputStream(notFoundBytes),
                    "not_found.log",
                    "text/plain",
                    notFoundBytes.length
            );
        }));

        HorizontalLayout toolbar = new HorizontalLayout(logFilePathField, searchKeywordField, refreshBtn, autoRefreshCheckbox, downloadAnchor, statusBadge);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.getStyle().set("background", "#f8fafc").set("padding", "12px 16px").set("border-radius", "8px").set("border", "1px solid #e2e8f0");

        // Container Log (Dark Mode / Terminal Style)
        logContainer.setWidthFull();
        logContainer.getStyle()
                .set("background-color", "#0f172a")
                .set("color", "#38bdf8")
                .set("font-family", "'Consolas', 'Courier New', monospace")
                .set("font-size", "13px")
                .set("line-height", "1.5")
                .set("padding", "16px")
                .set("border-radius", "8px")
                .set("overflow-y", "auto")
                .set("overflow-x", "auto")
                .set("max-height", "680px")
                .set("box-shadow", "inset 0 2px 10px rgba(0,0,0,0.5)");

        add(header, toolbar, logContainer);

        // Polling Listener untuk Auto Refresh
        addAttachListener(e -> {
            e.getUI().addPollListener(pollEvent -> {
                if (autoRefreshCheckbox.getValue()) {
                    refreshLogContent();
                }
            });
        });

        addDetachListener(e -> {
            UI ui = e.getUI();
            if (ui != null) {
                ui.setPollInterval(-1);
            }
        });

        refreshLogContent();
    }

    private String detectDefaultLogFile() {
        String[] candidates = {
                "logs/server.log",
                "./logs/server.log",
                "/opt/vaadinerp/logs/server.log",
                "server.log",
                "application.log"
        };
        for (String path : candidates) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                return f.getAbsolutePath();
            }
        }
        return "logs/server.log";
    }

    private void refreshLogContent() {
        String path = logFilePathField.getValue();
        if (path == null || path.trim().isEmpty()) {
            logContainer.setText("⚠️ Log file path has not been set.");
            return;
        }

        File file = new File(path.trim());
        if (!file.exists() || !file.isFile()) {
            logContainer.setText("⚠️ Log file not found at path:\n" + file.getAbsolutePath() +
                    "\n\nTips: Ensure your Spring Boot app is configured to write logs to this file (e.g., logging.file.name=logs/server.log).");
            return;
        }

        String keyword = searchKeywordField.getValue() != null ? searchKeywordField.getValue().trim().toLowerCase() : "";

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            // Baca maksimal 250 KB terakhir agar cepat dan ringan di memori
            long startPos = Math.max(0, fileLength - (250 * 1024));
            raf.seek(startPos);

            byte[] buffer = new byte[(int) (fileLength - startPos)];
            raf.readFully(buffer);

            String text = new String(buffer, StandardCharsets.UTF_8);
            String[] lines = text.split("\\r?\\n");

            StringBuilder sb = new StringBuilder();
            sb.append("=== Showing last ").append(lines.length).append(" log lines from: ").append(file.getAbsolutePath()).append(" ===\n\n");

            int matchCount = 0;
            for (String line : lines) {
                if (keyword.isEmpty() || line.toLowerCase().contains(keyword)) {
                    sb.append(line).append("\n");
                    matchCount++;
                }
            }

            if (!keyword.isEmpty()) {
                sb.insert(0, "=== [Filter Aktif: '" + keyword + "' -> Ditemukan " + matchCount + " baris cocok] ===\n");
            }

            logContainer.setText(sb.toString());

            // Scroll otomatis ke bawah agar selalu melihat log terbaru
            UI.getCurrent().getPage().executeJs("arguments[0].scrollTop = arguments[0].scrollHeight;", logContainer.getElement());

        } catch (Exception ex) {
            logContainer.setText("❌ Failed to read log file: " + ex.getMessage());
        }
    }
}
