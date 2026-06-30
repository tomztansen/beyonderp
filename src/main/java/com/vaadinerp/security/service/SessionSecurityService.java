package com.vaadinerp.security.service;

import com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.entity.RoleMenuPermission;
import com.vaadinerp.security.repository.AppUserRepository;
import com.vaadinerp.security.repository.RoleMenuPermissionRepository;
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
     * Login: mendukung plain-text lama dan BCrypt hash.
     * Jika password masih plain-text, otomatis di-migrasi ke BCrypt.
     */
    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        Optional<AppUser> opt = userRepository.findByUsernameAndIsActiveTrue(username.trim());
        if (opt.isPresent()) {
            AppUser u = opt.get();
            String storedHash = u.getPasswordHash();

            boolean matched = false;

            // Cek apakah hash tersimpan adalah BCrypt (diawali $2a$, $2b$, atau $2y$)
            if (storedHash != null && storedHash.startsWith("$2")) {
                // Password sudah di-hash BCrypt
                matched = passwordEncoder.matches(password, storedHash);
            } else {
                // Password masih plain-text (legacy) — bandingkan langsung
                matched = password.equals(storedHash);
                if (matched) {
                    // Auto-migrasi ke BCrypt
                    u.setPasswordHash(passwordEncoder.encode(password));
                    userRepository.save(u);
                }
            }

            if (matched) {
                if (VaadinSession.getCurrent() != null) {
                    VaadinSession.getCurrent().setAttribute(SESSION_USER_KEY, u);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Ganti password: verifikasi password lama, simpan hash BCrypt baru.
     * @return pesan error atau null jika berhasil.
     */
    public String changePassword(AppUser user, String oldPassword, String newPassword) {
        if (user == null) return "User tidak ditemukan!";

        String storedHash = user.getPasswordHash();
        boolean oldMatched;

        if (storedHash != null && storedHash.startsWith("$2")) {
            oldMatched = passwordEncoder.matches(oldPassword, storedHash);
        } else {
            oldMatched = oldPassword.equals(storedHash);
        }

        if (!oldMatched) return "Password lama tidak sesuai!";

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Update session agar objek user terkini
        if (VaadinSession.getCurrent() != null) {
            VaadinSession.getCurrent().setAttribute(SESSION_USER_KEY, user);
        }
        return null; // sukses
    }

    /**
     * Encode password baru (untuk pembuatan user baru atau reset password).
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public void logout() {
        if (VaadinSession.getCurrent() != null) {
            VaadinSession.getCurrent().setAttribute(SESSION_USER_KEY, null);
            VaadinSession.getCurrent().close();
        }
    }

    public AppUser getCurrentUser() {
        if (VaadinSession.getCurrent() != null) {
            Object obj = VaadinSession.getCurrent().getAttribute(SESSION_USER_KEY);
            if (obj instanceof AppUser) return (AppUser) obj;
        }
        return null;
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public boolean hasMenuAccess(String menuCode) {
        AppUser user = getCurrentUser();
        if (user == null) return false;
        if ("SUPER_ADMIN".equalsIgnoreCase(user.getRoleCode())) return true;
        return permissionRepository.findByRoleCodeAndMenuCode(user.getRoleCode(), menuCode).isPresent();
    }

    public MenuAccessAuthority getAuthorityForMenu(String menuCode) {
        AppUser user = getCurrentUser();
        if (user == null) {
            return MenuAccessAuthority.readOnly();
        }
        if ("SUPER_ADMIN".equalsIgnoreCase(user.getRoleCode())) {
            return MenuAccessAuthority.fullAccess();
        }
        Optional<RoleMenuPermission> perm = permissionRepository.findByRoleCodeAndMenuCode(user.getRoleCode(), menuCode);
        if (perm.isPresent()) {
            RoleMenuPermission p = perm.get();
            MenuAccessAuthority auth = new MenuAccessAuthority();
            auth.canAdd = Boolean.TRUE.equals(p.getCanAdd());
            auth.canEdit = Boolean.TRUE.equals(p.getCanEdit());
            auth.canDelete = Boolean.TRUE.equals(p.getCanDelete());
            auth.canPrint = Boolean.TRUE.equals(p.getCanPrint());
            return auth;
        }
        return MenuAccessAuthority.readOnly();
    }
}
