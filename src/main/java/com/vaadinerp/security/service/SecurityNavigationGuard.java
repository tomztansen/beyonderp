package com.vaadinerp.security.service;

import java.util.List;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadinerp.security.entity.AppMenu;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.repository.AppMenuRepository;
import org.springframework.stereotype.Component;

/**
 * Penjaga navigasi URL (BeforeEnter):
 * 1. belum login -> /login;
 * 2. sudah login tapi tidak berhak ke rute itu -> kembali ke portal + notifikasi.
 *
 * Pemetaan rute -> menu: "generic/{form}" dan "masterdetail/{form}" -> kode form;
 * rute lain -> app_menus.route_path yang sama dengan segmen pertama URL.
 * Rute yang tidak punya menu (mis. scheduler, system-log-viewer) hanya untuk SUPER_ADMIN.
 * Hak dicek lewat SessionSecurityService.hasMenuAccess (SUPER_ADMIN lolos semua).
 * Tab yang dibuka dari menu PortalView tidak lewat sini (komponen, bukan rute) --
 * di sana hak sudah disaring saat membangun menu.
 */
@Component
public class SecurityNavigationGuard implements VaadinServiceInitListener {

    private final SessionSecurityService securityService;
    private final AppMenuRepository appMenuRepository;

    public SecurityNavigationGuard(SessionSecurityService securityService, AppMenuRepository appMenuRepository) {
        this.securityService = securityService;
        this.appMenuRepository = appMenuRepository;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().addBeforeEnterListener(enterEvent -> {
                String location = enterEvent.getLocation().getPath();

                // Rute halaman 'login' dan 'actuator' dikecualikan dari pemeriksaan sesi
                if ("login".equalsIgnoreCase(location) || location.startsWith("actuator")) {
                    return;
                }

                VaadinSession session = VaadinSession.getCurrent();
                AppUser user = null;
                if (session != null && session.getAttribute(SessionSecurityService.SESSION_USER_KEY) instanceof AppUser u) {
                    user = u;
                }
                if (user == null) {
                    enterEvent.forwardTo("login");
                    return;
                }

                // Portal (rute kosong) selalu boleh; isinya disaring per menu
                if (location.isBlank()) {
                    return;
                }

                if (!allowed(user, location)) {
                    Notification.show("Access denied. You do not have permission to open this page.", 4000,
                            Notification.Position.TOP_CENTER);
                    enterEvent.rerouteTo("");
                }
            });
        });
    }

    private boolean allowed(AppUser user, String location) {
        boolean superAdmin = user.getRoles() != null && user.getRoles().contains("SUPER_ADMIN");
        if (superAdmin) {
            return true;
        }
        String[] seg = location.split("/");
        String first = seg[0];
        // Form dinamis: hak = menu dengan kode form (alias menu diperlakukan sama seperti PortalView)
        if (("generic".equalsIgnoreCase(first) || "masterdetail".equalsIgnoreCase(first))) {
            return seg.length > 1 && !seg[1].isBlank() && securityService.hasMenuAccess(seg[1]);
        }
        // Rute statis: cocokkan dengan app_menus.route_path
        List<AppMenu> menus = appMenuRepository.findAll();
        for (AppMenu m : menus) {
            String route = m.getRoutePath();
            if (route != null && route.trim().equalsIgnoreCase(first)) {
                return securityService.hasMenuAccess(m.getMenuCode());
            }
        }
        // Tidak ada menu untuk rute ini -> hanya SUPER_ADMIN (sudah lolos di atas)
        return false;
    }
}
