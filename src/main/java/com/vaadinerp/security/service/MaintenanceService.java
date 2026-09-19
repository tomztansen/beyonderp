package com.vaadinerp.security.service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Maintenance mode: banner hitung mundur ke semua user online + tolak login baru.
 * State hanya di memori — hilang saat restart, jadi tidak mungkin "terkunci" setelah app hidup lagi.
 */
@Service
public class MaintenanceService implements VaadinServiceInitListener {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);
    private static final String BANNER_ID = "grp-maintenance-banner";
    private static final String STYLE_ID = "grp-maintenance-style";

    private final LoginHistoryService loginHistory;

    private volatile boolean active;
    private volatile String message = "";
    private volatile long deadlineEpochMs;

    public MaintenanceService(LoginHistoryService loginHistory) {
        this.loginHistory = loginHistory;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        // UI baru (refresh/navigasi) saat maintenance aktif langsung dapat banner --
        // tapi hanya untuk session yang sudah login. Halaman login publik tidak perlu
        // tahu jadwal restart internal, dan pesan "save your work" tidak relevan buat
        // orang yang belum masuk sama sekali.
        event.getSource().addUIInitListener(e -> {
            if (!active)
                return;
            if (e.getUI().getSession().getAttribute(SessionSecurityService.SESSION_USER_KEY) == null)
                return;
            try {
                attachBanner(e.getUI());
            } catch (Exception ex) {
                // Banner gagal tidak boleh menggagalkan pembukaan halaman
                log.warn("maintenance banner gagal dipasang di UI baru: {}", ex.getMessage());
            }
        });
    }

    /** Restart yang tidak kunjung datang tidak boleh menahan login selamanya. */
    private static final long GRACE_AFTER_DEADLINE_MS = 3 * 60_000L;

    public boolean isActive() {
        if (active && System.currentTimeMillis() > deadlineEpochMs + GRACE_AFTER_DEADLINE_MS) {
            log.warn("Maintenance mode kedaluwarsa tanpa restart; dinonaktifkan otomatis");
            stop();
        }
        return active;
    }

    public Map<String, Object> status() {
        boolean on = isActive();
        long remaining = on ? Math.max(0, (deadlineEpochMs - System.currentTimeMillis()) / 1000) : 0;
        return Map.of("active", on, "message", message, "remainingSeconds", remaining,
                "onlineSessions", loginHistory.liveSessions().size());
    }

    public void start(String msg, int seconds) {
        this.message = msg != null ? msg : "Application will restart shortly, please save your work.";
        this.deadlineEpochMs = System.currentTimeMillis() + Math.max(0, seconds) * 1000L;
        this.active = true;
        log.info("Maintenance mode START ({} s): {}", seconds, this.message);
        forEachUi(this::attachBanner);
    }

    public void stop() {
        this.active = false;
        log.info("Maintenance mode STOP");
        forEachUi(ui -> {
            ui.getChildren()
                    .filter(c -> BANNER_ID.equals(c.getId().orElse("")))
                    .toList()
                    .forEach(ui::remove);
            ui.getPage().executeJs(
                    "const st=document.getElementById('" + STYLE_ID + "'); if(st){st.remove();}");
        });
    }

    public void broadcast(String msg) {
        log.info("Broadcast: {}", msg);
        forEachUi(ui -> {
            Notification n = new Notification();
            n.setPosition(Notification.Position.TOP_CENTER);
            n.setDuration(0); // tetap tampil sampai diklik OK
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            Button ok = new Button("OK", e -> n.close());
            ok.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            n.add(new Span(msg), ok);
            n.open();
        });
    }

    /** Jalankan aksi di setiap UI dari setiap session hidup, di bawah lock session (ui.access). */
    private void forEachUi(Consumer<UI> action) {
        for (VaadinSession session : loginHistory.liveSessions()) {
            try {
                session.access(() -> {
                    for (UI ui : session.getUIs()) {
                        if (ui.isClosing())
                            continue;
                        // Wajib: Notification.open() dan sejenisnya butuh UI.getCurrent() terisi
                        // untuk tahu ke UI mana overlay-nya melekat -- session.access() sendiri
                        // TIDAK menyetelnya (beda dengan ui.access(), yang melakukan ini otomatis).
                        // Tanpa ini Notification.open() melempar exception yang tertelan diam-diam
                        // oleh catch di bawah (level debug, tidak tampil di log produksi/INFO).
                        UI.setCurrent(ui);
                        try {
                            action.accept(ui);
                        } catch (Exception ex) {
                            log.warn("maintenance action gagal untuk ui {}: {}", ui.getUIId(), ex.toString());
                        } finally {
                            UI.setCurrent(null);
                        }
                    }
                });
            } catch (Exception ex) {
                // session sudah ditutup di antara snapshot dan access -> lewati
                log.debug("maintenance skip session: {}", ex.getMessage());
            }
        }
    }

    private void attachBanner(UI ui) {
        // Kalau start() dipanggil lagi (mis. perpanjang waktu) sementara banner lama masih
        // tampil, buang dulu supaya teks & hitung mundur ikut deadline yang baru -- bukan
        // dibiarkan jalan dengan angka lama.
        ui.getChildren()
                .filter(c -> BANNER_ID.equals(c.getId().orElse("")))
                .toList()
                .forEach(ui::remove);

        Span icon = new Span("⚠️");
        icon.getStyle()
                .set("margin-right", "8px")
                .set("font-size", "0.95rem")
                .set("display", "inline-flex")
                .set("align-items", "center");

        Span text = new Span(message);
        text.getStyle()
                .set("font-weight", "500")
                .set("font-size", "0.85rem")
                .set("color", "#ffffff");

        Span countdown = new Span();
        countdown.getStyle()
                .set("font-weight", "700")
                .set("font-size", "0.82rem")
                .set("margin-left", "10px")
                .set("background", "rgba(0, 0, 0, 0.28)")
                .set("padding", "3px 10px")
                .set("border-radius", "9999px")
                .set("letter-spacing", "0.4px")
                .set("color", "#fef08a");

        Div banner = new Div(icon, text, countdown);
        banner.setId(BANNER_ID);
        banner.getStyle()
                .set("position", "fixed")
                .set("top", "8px")
                .set("left", "50%")
                .set("transform", "translateX(-50%)")
                .set("z-index", "100000")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("background", "linear-gradient(135deg, #dc2626 0%, #b91c1c 100%)")
                .set("color", "white")
                .set("padding", "6px 16px")
                .set("border-radius", "9999px")
                .set("box-shadow", "0 10px 25px -5px rgba(220, 38, 38, 0.5), 0 4px 6px -2px rgba(0, 0, 0, 0.2)")
                .set("border", "1px solid rgba(255, 255, 255, 0.25)")
                .set("white-space", "nowrap")
                .set("max-width", "85vw")
                .set("box-sizing", "border-box")
                .set("pointer-events", "auto")
                .set("animation", "grpBannerSlideDown 0.4s cubic-bezier(0.16, 1, 0.3, 1)");
        ui.add(banner);

        // Hitung mundur murni di browser dari sisa detik saat diterima
        int remaining = (int) Math.max(0, (deadlineEpochMs - System.currentTimeMillis()) / 1000);
        ui.getPage().executeJs(
                "const el=$0; const end=Date.now()+$1*1000;"
                        + "if(!document.getElementById('" + STYLE_ID + "')){"
                        + "const st=document.createElement('style'); st.id='" + STYLE_ID + "';"
                        + "st.textContent='@keyframes grpBannerSlideDown{from{transform:translate(-50%,-100%);opacity:0;}to{transform:translate(-50%,0);opacity:1;}}';"
                        + "document.head.appendChild(st);}"
                        // Penanda versi global: kalau start() dipanggil lagi sebelum timer lama
                        // selesai, timer lama mendeteksi dirinya usang lewat versi ini dan berhenti
                        // diam-diam -- tidak menghapus banner baru yang kebetulan pakai ID sama.
                        + "window.__grpMaintVer=(window.__grpMaintVer||0)+1; const myVer=window.__grpMaintVer;"
                        + "const grace=$2*1000;"
                        + "const tick=()=>{if(window.__grpMaintVer!==myVer) return;"
                        + "const s=Math.max(0,Math.ceil((end-Date.now())/1000));"
                        + "if(s>0){el.textContent='Restart in '+Math.floor(s/60)+':'+String(s%60).padStart(2,'0');}"
                        + "else if(Date.now()<end+grace){el.textContent='Restarting now…';}"
                        + "else{const b=document.getElementById('" + BANNER_ID + "'); if(b){b.remove();}"
                        + "const st=document.getElementById('" + STYLE_ID + "'); if(st){st.remove();} return;}"
                        + "setTimeout(tick,1000);};"
                        + "tick();",
                countdown.getElement(), remaining, (int) (GRACE_AFTER_DEADLINE_MS / 1000));
    }
}
