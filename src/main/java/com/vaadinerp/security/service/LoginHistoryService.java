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
    private final ConcurrentHashMap<Long, String> liveUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> liveIp = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbc;
    private final org.springframework.core.env.Environment env;

    public LoginHistoryService(JdbcTemplate jdbc, org.springframework.core.env.Environment env) {
        this.jdbc = jdbc;
        this.env = env;
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
            String ip = clientIp();
            Long id = jdbc.queryForObject(
                    "INSERT INTO public.app_login_history (username, ip, success) VALUES (?,?,?) RETURNING id",
                    Long.class, username, ip, success);
            if (success && s != null && id != null) {
                s.setAttribute(ATTR_HISTORY_ID, id);
                s.setAttribute("LOGIN_TIMESTAMP", System.currentTimeMillis());
                s.setAttribute("CLIENT_IP", ip);
                live.put(id, s);
                liveUser.put(id, username);
                liveIp.put(id, ip);
            }
        } catch (Exception ex) {
            log.warn("Login history not recorded for {}: {}", username, ex.getMessage());
        }
    }

    /** Snapshot session yang terdaftar hidup; dipakai MaintenanceService untuk broadcast. */
    public java.util.List<VaadinSession> liveSessions() {
        return new java.util.ArrayList<>(live.values());
    }

    /**
     * Menemukan IP dari sesi aktif milik username tertentu.
     * Bila sesi ditemukan sudah stale (melebihi toleransi heartbeat), sesi langsung dibersihkan dan mengembalikan null.
     */
    public String findActiveSessionIp(String username) {
        VaadinSession current = VaadinSession.getCurrent();
        for (Map.Entry<Long, String> e : liveUser.entrySet()) {
            if (!e.getValue().equalsIgnoreCase(username))
                continue;
            VaadinSession s = live.get(e.getKey());
            if (s == null) {
                liveUser.remove(e.getKey());
                liveIp.remove(e.getKey());
                continue;
            }
            if (s == current)
                continue;
            if (!s.getLockInstance().tryLock()) {
                return liveIp.getOrDefault(e.getKey(), "unknown"); // sedang melayani request -> jelas aktif
            }
            try {
                if (s.getState() != VaadinSessionState.OPEN) {
                    live.remove(e.getKey());
                    liveUser.remove(e.getKey());
                    liveIp.remove(e.getKey());
                    continue;
                }
                if (heartbeatStale(s)) {
                    close(e.getKey(), "BROWSER_CLOSED", null);
                    live.remove(e.getKey());
                    liveUser.remove(e.getKey());
                    liveIp.remove(e.getKey());
                    invalidate(s);
                    continue;
                }
                return liveIp.getOrDefault(e.getKey(), "unknown");
            } catch (Exception ignored) {
            } finally {
                s.getLockInstance().unlock();
            }
        }
        return null;
    }

    /**
     * Apakah username ini masih punya session hidup lain (browser masih mengirim heartbeat).
     * Session yang heartbeat-nya sudah basi tidak dihitung — sweep akan menutupnya.
     */
    public boolean hasLiveSession(String username) {
        return findActiveSessionIp(username) != null;
    }

    /**
     * Memutus paksa semua sesi aktif milik username (misalnya saat ambil alih / takeover sesi).
     */
    public void kickUserSessions(String username, String reason, String by) {
        for (Map.Entry<Long, String> e : liveUser.entrySet()) {
            if (e.getValue().equalsIgnoreCase(username)) {
                Long id = e.getKey();
                close(id, reason, by);
                liveUser.remove(id);
                liveIp.remove(id);
                VaadinSession s = live.remove(id);
                if (s != null) {
                    invalidate(s);
                }
            }
        }
    }

    /** Dipanggil dari SessionSecurityService.logout() sebelum session.close(). */
    public void recordLogout() {
        VaadinSession s = VaadinSession.getCurrent();
        if (s != null && s.getAttribute(ATTR_HISTORY_ID) instanceof Long id) {
            close(id, "LOGOUT", null);
            live.remove(id);
            liveUser.remove(id);
            liveIp.remove(id);
        }
    }

    /** Admin menendang user: tutup baris + invalidate session-nya. Aman dipanggil dari session lain. */
    public void kick(Object historyId, String by) {
        Long id = historyId instanceof Number n ? n.longValue() : Long.valueOf(String.valueOf(historyId));
        close(id, "KICKED", by);
        liveUser.remove(id);
        liveIp.remove(id);
        VaadinSession s = live.remove(id);
        if (s != null)
            invalidate(s);
    }

    /** Tiap menit: session yang semua UI-nya sudah 3x interval tanpa heartbeat = browser ditutup. */
    @Scheduled(fixedDelay = 60_000)
    public void sweep() {
        for (Map.Entry<Long, VaadinSession> e : live.entrySet()) {
            VaadinSession s = e.getValue();
            if (s == null) {
                live.remove(e.getKey());
                liveUser.remove(e.getKey());
                liveIp.remove(e.getKey());
                continue;
            }
            if (!s.getLockInstance().tryLock())
                continue; // sedang dipakai request user -> jelas masih hidup
            try {
                // getState() dan getUIs() WAJIB di bawah lock session (Vaadin melempar IllegalStateException bila tidak)
                if (s.getState() != VaadinSessionState.OPEN) {
                    live.remove(e.getKey()); // baris DB sudah ditutup oleh SessionDestroyListener
                    liveUser.remove(e.getKey());
                    liveIp.remove(e.getKey());
                    continue;
                }
                if (heartbeatStale(s)) {
                    close(e.getKey(), "BROWSER_CLOSED", null);
                    live.remove(e.getKey());
                    liveUser.remove(e.getKey());
                    liveIp.remove(e.getKey());
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
        // Instance dev memakai DB yang sama dengan server; jangan menutup session milik instance lain.
        if (env.acceptsProfiles(org.springframework.core.env.Profiles.of("dev"))) {
            log.info("Profil dev: lewati penutupan orphan login sessions");
            return;
        }
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
        Long id = null;
        try {
            if (s.getAttribute(ATTR_HISTORY_ID) instanceof Long attrId) {
                id = attrId;
            }
        } catch (Exception ignored) {
        }

        if (id != null) {
            live.remove(id);
            liveUser.remove(id);
            liveIp.remove(id);
            close(id, "TIMEOUT", null);
        } else {
            // Fallback jika container sudah menghapus attribute sebelum event ini
            live.entrySet().removeIf(entry -> {
                if (entry.getValue() == s) {
                    liveUser.remove(entry.getKey());
                    liveIp.remove(entry.getKey());
                    return true;
                }
                return false;
            });
        }
    }

    private void close(Long id, String reason, String by) {
        try {
            jdbc.update("UPDATE public.app_login_history SET logout_at = now(), logout_reason = ?, logout_by = ?"
                    + " WHERE id = ? AND logout_at IS NULL", reason, by, id);
        } catch (Exception ex) {
            log.warn("Login history {} not closed ({}): {}", id, reason, ex.getMessage());
        }
    }

    /**
     * Semua UI sudah > 3x heartbeat interval tanpa heartbeat. Harus dipanggil dengan lock session.
     *
     * Bug-fix: jika belum ada heartbeat sama sekali (last==0, browser ditutup sebelum
     * heartbeat pertama sempat terkirim), gunakan waktu login sebagai acuan
     * agar session tidak "menggantung" selamanya dan memblokir re-login.
     */
    private static boolean heartbeatStale(VaadinSession s) {
        try {
            long interval = s.getService().getDeploymentConfiguration().getHeartbeatInterval() * 1000L;
            long threshold = 3 * interval;
            long now = System.currentTimeMillis();

            long last = 0;
            for (UI ui : s.getUIs())
                last = Math.max(last, ui.getInternals().getLastHeartbeatTimestamp());

            if (last > 0) {
                // Normal: ada heartbeat tercatat → cek apakah sudah kedaluwarsa
                return now - last > threshold;
            }

            // last == 0: belum ada heartbeat yang pernah diterima.
            // Gunakan waktu login sebagai acuan — jika sudah lewat 3x interval
            // sejak login tanpa satupun heartbeat, browser pasti sudah ditutup
            // sebelum heartbeat pertama sempat terkirim.
            Object loginTs = s.getAttribute("LOGIN_TIMESTAMP");
            if (loginTs instanceof Long ts) {
                return now - ts > threshold;
            }

            // Tidak ada data sama sekali → anggap stale untuk mencegah lockout permanen
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static void invalidate(VaadinSession s) {
        try {
            if (s != null && s.getSession() != null) {
                s.getSession().invalidate(); // memicu SessionDestroyListener -> live.remove
            }
        } catch (Exception ignored) {
            // sudah invalid
        }
    }
}
