package com.vaadinerp.security.service;

import com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.entity.RoleMenuPermission;
import com.vaadinerp.security.repository.AppUserRepository;
import com.vaadinerp.security.repository.RoleMenuPermissionRepository;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SessionSecurityService {
    public static final String SESSION_USER_KEY = "LOGGED_IN_APP_USER";

    /** Password benar, tapi akun masih punya session aktif di tempat lain. */
    public static class AlreadyLoggedInException extends RuntimeException {
        private final String ip;

        public AlreadyLoggedInException() {
            this("This account is already logged in", null);
        }

        public AlreadyLoggedInException(String message, String ip) {
            super(message);
            this.ip = ip;
        }

        public String getIp() {
            return ip;
        }
    }

    /** Password benar, tapi akun sedang aktif di komputer / IP lain. */
    public static class DifferentIpActiveSessionException extends AlreadyLoggedInException {
        public DifferentIpActiveSessionException(String ip) {
            super("This account is actively logged in on another machine", ip);
        }
    }

    /**
     * Password benar, dan sesi aktif terdeteksi dari komputer / IP yang sama (bisa
     * diambil alih).
     */
    public static class SameIpTakeoverNeededException extends AlreadyLoggedInException {
        public SameIpTakeoverNeededException(String ip) {
            super("An active session was detected from this machine (IP: " + (ip != null ? ip : "unknown") + ").", ip);
        }
    }

    /** Password benar, tapi aplikasi sedang maintenance (akan restart). */
    public static class MaintenanceException extends RuntimeException {
        public MaintenanceException() {
            super("System is restarting, please try again in a few minutes");
        }
    }

    private final AppUserRepository userRepository;
    private final RoleMenuPermissionRepository permissionRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final LoginHistoryService loginHistory;
    private final MaintenanceService maintenance;

    public SessionSecurityService(AppUserRepository userRepository, RoleMenuPermissionRepository permissionRepository,
            LoginHistoryService loginHistory, MaintenanceService maintenance) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.loginHistory = loginHistory;
        this.maintenance = maintenance;
    }

    public static boolean isSameIp(String ip1, String ip2) {
        if (ip1 == null || ip2 == null)
            return false;
        if ("unknown".equalsIgnoreCase(ip1) || "unknown".equalsIgnoreCase(ip2))
            return false;
        return ip1.trim().equalsIgnoreCase(ip2.trim());
    }

    /**
     * Login menggunakan BCrypt hash.
     */
    public boolean login(String username, String password) {
        if (username == null || password == null)
            return false;

        Optional<AppUser> opt = userRepository.findByUsernameIgnoreCaseAndIsActiveTrue(username.trim());
        if (opt.isEmpty()) {
            loginHistory.recordLogin(username.trim(), false);
            return false;
        }

        AppUser u = opt.get();
        String storedHash = u.getPasswordHash();
        boolean matched = storedHash != null && passwordEncoder.matches(password, storedHash);

        if (!matched) {
            loginHistory.recordLogin(u.getUsername(), false);
            return false;
        }

        // Selama maintenance (menjelang restart) login baru ditahan; yang sudah login
        // tidak disentuh.
        if (maintenance.isActive()) {
            throw new MaintenanceException();
        }

        // Cek apakah akun memiliki sesi aktif
        String activeIp = loginHistory.findActiveSessionIp(u.getUsername());
        if (activeIp != null) {
            String currentIp = LoginHistoryService.clientIp();
            if (isSameIp(currentIp, activeIp)) {
                // Berasal dari mesin/IP yang sama -> berikan opsi ambil alih
                throw new SameIpTakeoverNeededException(activeIp);
            } else {
                // Berasal dari mesin/IP berbeda -> tolak login bersamaan
                throw new DifferentIpActiveSessionException(activeIp);
            }
        }

        return establishSession(u);
    }

    /**
     * Ambil alih sesi: putus paksa sesi lama pada username ini lalu langsung login.
     */
    public boolean takeoverAndLogin(String username, String password) {
        if (username == null || password == null)
            return false;

        Optional<AppUser> opt = userRepository.findByUsernameIgnoreCaseAndIsActiveTrue(username.trim());
        if (opt.isEmpty()) {
            loginHistory.recordLogin(username.trim(), false);
            return false;
        }

        AppUser u = opt.get();
        String storedHash = u.getPasswordHash();
        boolean matched = storedHash != null && passwordEncoder.matches(password, storedHash);
        if (!matched) {
            loginHistory.recordLogin(u.getUsername(), false);
            return false;
        }

        if (maintenance.isActive()) {
            throw new MaintenanceException();
        }

        // Putus semua sesi lama milik user ini
        loginHistory.kickUserSessions(u.getUsername(), "TAKEOVER", u.getUsername());

        return establishSession(u);
    }

    private boolean establishSession(AppUser u) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            // Proteksi session fixation: reinitialize session ID setelah login berhasil.
            if (VaadinService.getCurrentRequest() != null) {
                try {
                    VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
                } catch (Exception ex) {
                    // Reinit gagal (jarang terjadi) — lanjut tanpa reinit
                }
            }
            session.setAttribute(SESSION_USER_KEY, u);
            if (VaadinService.getCurrentRequest() != null) {
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("SPRING_MVC_USER", u);
            }
            loginHistory.recordLogin(u.getUsername(), true);
        }
        return true;
    }

    /**
     * Ganti password: verifikasi password lama, simpan hash BCrypt baru.
     *
     * @return pesan error atau null jika berhasil.
     */
    public String changePassword(AppUser user, String oldPassword, String newPassword) {
        if (user == null)
            return "User not found!";
        if (oldPassword == null)
            return "Current password cannot be empty!";
        if (newPassword == null || newPassword.isBlank())
            return "New password cannot be empty!";

        String storedHash = user.getPasswordHash();
        boolean oldMatched = storedHash != null && passwordEncoder.matches(oldPassword, storedHash);

        if (!oldMatched)
            return "Current password is incorrect!";

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Update session agar objek user terkini
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(SESSION_USER_KEY, user);
        }
        return null; // sukses
    }

    /**
     * Encode password baru (untuk pembuatan user baru atau reset password).
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Logout: hapus atribut user dan tutup session.
     * session.close() otomatis menginvalidate underlying HttpSession di akhir
     * request — TIDAK perlu (dan TIDAK boleh) invalidate manual sebelum ini,
     * karena bisa memicu IllegalStateException saat close() dipanggil.
     */
    public void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            loginHistory.recordLogout();
            session.setAttribute(SESSION_USER_KEY, null);
            if (VaadinService.getCurrentRequest() != null) {
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute("SPRING_MVC_USER", null);
            }
            session.close();
        }
    }

    public AppUser getCurrentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            Object obj = session.getAttribute(SESSION_USER_KEY);
            if (obj instanceof AppUser)
                return (AppUser) obj;
        }
        return null;
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public boolean isSuperAdmin() {
        AppUser user = getCurrentUser();
        return user != null && user.getRoles() != null && user.getRoles().contains("SUPER_ADMIN");
    }

    public boolean hasMenuAccess(String menuCode) {
        AppUser user = getCurrentUser();
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty())
            return false;
        if (user.getRoles().contains("SUPER_ADMIN"))
            return true;
        for (String role : user.getRoles()) {
            if (permissionRepository.findByRoleCodeAndMenuCode(role, menuCode).isPresent()) {
                return true;
            }
        }
        return false;
    }

    public MenuAccessAuthority getAuthorityForMenu(String menuCode) {
        AppUser user = getCurrentUser();
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return MenuAccessAuthority.readOnly();
        }
        if (user.getRoles().contains("SUPER_ADMIN")) {
            return MenuAccessAuthority.fullAccess();
        }

        MenuAccessAuthority auth = new MenuAccessAuthority();
        auth.canAccessScreen = false;
        auth.canAdd = false;
        auth.canEdit = false;
        auth.canDelete = false;
        auth.canPrint = false;
        auth.canView = false;
        auth.canEditDetail = false;

        for (String role : user.getRoles()) {
            Optional<RoleMenuPermission> perm = permissionRepository.findByRoleCodeAndMenuCode(role, menuCode);
            if (perm.isPresent()) {
                auth.canAccessScreen = true; // Karcis masuk tersedia
                RoleMenuPermission p = perm.get();
                if (Boolean.TRUE.equals(p.getCanAdd()))
                    auth.canAdd = true;
                if (Boolean.TRUE.equals(p.getCanEdit()))
                    auth.canEdit = true;
                if (Boolean.TRUE.equals(p.getCanDelete()))
                    auth.canDelete = true;
                if (Boolean.TRUE.equals(p.getCanPrint()))
                    auth.canPrint = true;
                if (Boolean.TRUE.equals(p.getCanView()))
                    auth.canView = true;
                if (!Boolean.FALSE.equals(p.getCanEditDetail()))
                    auth.canEditDetail = true;
            }
        }

        return auth;
    }
}
