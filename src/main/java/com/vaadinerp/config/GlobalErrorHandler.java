package com.vaadinerp.config;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Global Error Handler untuk aplikasi Vaadin ERP.
 * Mencegah aplikasi menjadi hang/freeze atau memunculkan overlay merah blocking saat terjadi unhandled exception.
 * Menampilkan notifikasi yang ramah pengguna dan menjaga agar menu serta navigasi lain tetap dapat digunakan.
 */
@Component
public class GlobalErrorHandler implements VaadinServiceInitListener {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(sessionEvent -> {
            sessionEvent.getSession().setErrorHandler(new ErrorHandler() {
                @Override
                public void error(ErrorEvent errorEvent) {
                    Throwable throwable = errorEvent.getThrowable();

                    // Log error secara detail ke server / console
                    log.error("Unhandled Vaadin UI Exception: ", throwable);

                    // Cari root cause pesan error
                    Throwable root = throwable;
                    while (root.getCause() != null && root != root.getCause()) {
                        root = root.getCause();
                    }
                    String errMsg = root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();

                    // Tampilkan notifikasi error yang ramah pengguna dan TIDAK memblokir layar (non-blocking)
                    UI ui = UI.getCurrent();
                    if (ui != null) {
                        ui.access(() -> {
                            Notification notification = Notification.show(
                                    "⚠️ Terjadi Kesalahan: " + errMsg + ". Silakan coba lagi atau pilih menu lain.",
                                    5000,
                                    Notification.Position.TOP_CENTER
                            );
                            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        });
                    }
                }
            });
        });
    }
}
