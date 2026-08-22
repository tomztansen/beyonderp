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

    private final AppUserRepository userRepository;
    private final RoleMenuPermissionRepository permissionRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SessionSecurityService(AppUserRepository userRepository, RoleMenuPermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Login menggunakan BCrypt hash.
     */
    public boolean login(String username, String password) {
        if (username == null || password == null)
            return false;

        Optional<AppUser> opt = userRepository.findByUsernameIgnoreCaseAndIsActiveTrue(username.trim());
        if (opt.isEmpty())
            return false;

        AppUser u = opt.get();
        String storedHash = u.getPasswordHash();
        boolean matched = storedHash != null && passwordEncoder.matches(password, storedHash);

        if (!matched)
            return false;

        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            // Proteksi session fixation: reinitialize session ID setelah login berhasil.
            // Hanya dilakukan jika ada request context aktif — kalau tidak ada,
            // JANGAN invalidate session manual (itu penyebab bug lama), cukup lewati.
            if (VaadinService.getCurrentRequest() != null) {
                try {
                    VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
                } catch (Exception ex) {
                    // Reinit gagal (jarang terjadi) — lanjut tanpa reinit
                    // daripada membuat state session rusak.
                }
            }
            session.setAttribute(SESSION_USER_KEY, u);
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
            return "User tidak ditemukan!";
        if (oldPassword == null)
            return "Password lama tidak boleh kosong!";
        if (newPassword == null || newPassword.isBlank())
            return "Password baru tidak boleh kosong!";

        String storedHash = user.getPasswordHash();
        boolean oldMatched = storedHash != null && passwordEncoder.matches(oldPassword, storedHash);

        if (!oldMatched)
            return "Password lama tidak sesuai!";

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
            session.setAttribute(SESSION_USER_KEY, null);
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
        
        for (String role : user.getRoles()) {
            Optional<RoleMenuPermission> perm = permissionRepository.findByRoleCodeAndMenuCode(role, menuCode);
            if (perm.isPresent()) {
                auth.canAccessScreen = true; // Karcis masuk tersedia
                RoleMenuPermission p = perm.get();
                if (Boolean.TRUE.equals(p.getCanAdd())) auth.canAdd = true;
                if (Boolean.TRUE.equals(p.getCanEdit())) auth.canEdit = true;
                if (Boolean.TRUE.equals(p.getCanDelete())) auth.canDelete = true;
                if (Boolean.TRUE.equals(p.getCanPrint())) auth.canPrint = true;
                if (Boolean.TRUE.equals(p.getCanView())) auth.canView = true;
            }
        }
        
        return auth;
    }
}
