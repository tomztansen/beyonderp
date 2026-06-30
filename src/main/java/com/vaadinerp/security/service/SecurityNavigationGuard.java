package com.vaadinerp.security.service;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadinerp.security.entity.AppUser;
import org.springframework.stereotype.Component;

/**
 * Navigation Guard Interceptor.
 * Mencegat seluruh request navigasi Vaadin Flow. Jika pengguna belum login dan rute bukan halaman 'login',
 * pengguna akan dipaksa berpindah ke halaman Login.
 */
@Component
public class SecurityNavigationGuard implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().addBeforeEnterListener(enterEvent -> {
                String location = enterEvent.getLocation().getPath();
                
                // Rute halaman 'login' dikecualikan dari pemeriksaan sesi
                if ("login".equalsIgnoreCase(location)) {
                    return;
                }

                VaadinSession session = VaadinSession.getCurrent();
                boolean isLoggedIn = false;
                if (session != null) {
                    Object u = session.getAttribute(SessionSecurityService.SESSION_USER_KEY);
                    isLoggedIn = u instanceof AppUser;
                }

                if (!isLoggedIn) {
                    enterEvent.forwardTo("login");
                }
            });
        });
    }
}
