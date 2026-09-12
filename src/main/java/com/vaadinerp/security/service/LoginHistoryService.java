package com.vaadinerp.security.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinSessionState;

/**
 * Riwayat login (public.app_login_history) + daftar session yang masih hidup.
 *
 * Satu baris per login. logout_at NULL = masih online. Baris ditutup oleh:
 * LOGOUT (klik), TIMEOUT (idle, browser masih terbuka), BROWSER_CLOSED
 * (heartbeat berhenti), KICKED (admin), SERVER_RESTART (app start).
 *
 * Registry {@link #live} hanya memegang VaadinSession yang memang hidup di
 * container; entry dihapus di SessionDestroyListener sehingga ukurannya selalu
 * = jumlah user online.
 */
@Service
public class LoginHistoryService implements VaadinServiceInitListener {

    private static final Logger log = LoggerFactory.getLogger(LoginHistoryService.class);
    private static final String ATTR_HISTORY_ID = "LOGIN_HISTORY_ID";

    // ponytail: registry per-JVM; bila kelak multi-instance, kick hanya menjangkau instance pemegang session
    private final ConcurrentHashMap<Long, VaadinSession> live = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbc;

    public LoginHistoryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionDestroyListener(e -> onSessionDestroy(e.getSession()));
    }

    /** IP client: WebBrowser → X-Forwarded-For → remote addr. Dipakai juga oleh badge IP di PortalView. */
    public static String clientIp() {
        String ip = "unknown";
        try {
            VaadinSession s = VaadinSession.getCurrent();
            if (s != null && s.getBrowser() != null && s.getBrowser().getAddress() != null) {
                ip = s.getBrowser().getAddress();
            } else if (VaadinRequest.getCurrent() != null) {
                String xff = VaadinRequest.getCurrent().getHeader("X-Forwarded-For");
                ip = xff != null && !xff.isEmpty() ? xff.split(",")[0].trim()
                        : VaadinRequest.getCurrent().getRemoteAddr();
            }
            if ("0:0:0:0:0:0:0:1".equals(ip))
                ip = "127.0.0.1";
        } catch (Exception ignored) {
        }
        return ip;
    }

    /** Dipanggil dari SessionSecurityService.login(). */
    public void recordLogin(String username, boolean success) {
        try {
            VaadinSession s = VaadinSession.getCurrent();
            Long id = jdbc.queryForObject(
                    "INSERT INTO public.app_login_history (username, ip, success) VALUES (?,?,?) RETURNING id",
                    Long.class, username, clientIp(), success);
            if (success && s != null && id != null) {
                s.setAttribute(ATTR_HISTORY_ID, id);
                live.put(id, s);
            }
        } catch (Exception ex) {
            log.warn("Login history not recorded for {}: {}", username, ex.getMessage());
        }
    }

    /** Dipanggil dari SessionSecurityService.logout() sebelum session.close(). */
    public void recordLogout() {
        VaadinSession s = VaadinSession.getCurrent();
        if (s != null && s.getAttribute(ATTR_HISTORY_ID) instanceof Long id) {
            close(id, "LOGOUT", null);
        }
    }

    /** Admin menendang user: tutup baris + invalidate session-nya. Aman dipanggil dari session lain. */
    public void kick(Object historyId, String by) {
        Long id = historyId instanceof Number n ? n.longValue() : Long.valueOf(String.valueOf(historyId));
        close(id, "KICKED", by);
        VaadinSession s = live.get(id);
        if (s != null)
            invalidate(s);
    }

    /** Tiap menit: session yang semua UI-nya sudah 3x interval tanpa heartbeat = browser ditutup. */
    @Scheduled(fixedDelay = 60_000)
    public void sweep() {
        for (Map.Entry<Long, VaadinSession> e : live.entrySet()) {
            VaadinSession s = e.getValue();
            if (!s.getLockInstance().tryLock())
                continue; // sedang dipakai request user -> jelas masih hidup
            try {
                if (s.getState() == VaadinSessionState.OPEN && heartbeatStale(s)) {
                    close(e.getKey(), "BROWSER_CLOSED", null);
                    invalidate(s);
                }
            } catch (Exception ex) {
                log.debug("sweep skip session {}: {}", e.getKey(), ex.getMessage());
            } finally {
                s.getLockInstance().unlock();
            }
        }
    }

    /** Baris yang masih terbuka saat app start = session yang hilang bersama JVM sebelumnya. */
    @EventListener(ApplicationReadyEvent.class)
    public void closeOrphans() {
        try {
            int n = jdbc.update("UPDATE public.app_login_history SET logout_at = now(), logout_reason = 'SERVER_RESTART'"
                    + " WHERE logout_at IS NULL");
            if (n > 0)
                log.info("Closed {} orphan login sessions (SERVER_RESTART)", n);
        } catch (Exception ex) {
            // Tabel belum dibuat (sql/login_history.sql belum dijalankan) tidak boleh menjatuhkan app
            log.warn("Login history unavailable, run sql/login_history.sql: {}", ex.getMessage());
        }
    }

    private void onSessionDestroy(VaadinSession s) {
        if (!(s.getAttribute(ATTR_HISTORY_ID) instanceof Long id))
            return;
        live.remove(id);
        // Saat listener ini jalan UI-nya sudah dilepas Vaadin, jadi heartbeat tidak bisa dibaca lagi.
        // Browser-tutup sudah ditangkap sweep() jauh sebelum timeout 30 menit; LOGOUT/KICKED sudah
        // menutup barisnya (WHERE logout_at IS NULL) -> sisanya pasti idle timeout.
        close(id, "TIMEOUT", null);
    }

    private void close(Long id, String reason, String by) {
        try {
            jdbc.update("UPDATE public.app_login_history SET logout_at = now(), logout_reason = ?, logout_by = ?"
                    + " WHERE id = ? AND logout_at IS NULL", reason, by, id);
        } catch (Exception ex) {
            log.warn("Login history {} not closed ({}): {}", id, reason, ex.getMessage());
        }
    }

    /** Semua UI sudah > 3x heartbeat interval tanpa heartbeat. Harus dipanggil dengan lock session. */
    private static boolean heartbeatStale(VaadinSession s) {
        try {
            long interval = s.getService().getDeploymentConfiguration().getHeartbeatInterval() * 1000L;
            long last = 0;
            for (UI ui : s.getUIs())
                last = Math.max(last, ui.getInternals().getLastHeartbeatTimestamp());
            return last > 0 && System.currentTimeMillis() - last > 3 * interval;
        } catch (Exception ex) {
            return false;
        }
    }

    private static void invalidate(VaadinSession s) {
        try {
            s.getSession().invalidate(); // memicu SessionDestroyListener -> live.remove
        } catch (Exception ignored) {
            // sudah invalid
        }
    }
}
