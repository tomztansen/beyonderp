package com.vaadinerp.config;

import com.vaadin.flow.component.ReconnectDialogConfiguration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.CustomizedSystemMessages;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi System Messages dan Reconnect Dialog untuk aplikasi Vaadin ERP.
 * Mengelola perilaku saat server down, koneksi terputus, atau sesi berakhir
 * (session expired)
 * agar menampilkan pesan yang jelas dalam Bahasa Indonesia serta mencegah
 * pengguna melakukan spam klik saat UI tidak merespon.
 */
@Component
public class CustomSystemMessagesConfig implements VaadinServiceInitListener {

    private static final Logger log = LoggerFactory.getLogger(CustomSystemMessagesConfig.class);

    @Override
    public void serviceInit(ServiceInitEvent event) {
        log.info("Menginisialisasi Custom System Messages & Reconnect Configuration...");

        // 1. Kustomisasi pesan sistem Vaadin (Session Expired, Internal Error, Cookies
        // Disabled)
        event.getSource().setSystemMessagesProvider(serviceInitEvent -> {
            CustomizedSystemMessages messages = new CustomizedSystemMessages();

            // Saat Sesi Berakhir (Session Expired / Idle Timeout 30 menit)
            // Diatur NotificationEnabled = false & URL = "" (string kosong) agar browser
            // langsung memuat ulang halaman secara otomatis tanpa menampilkan peringatan
            // "Koneksi Terputus"
            messages.setSessionExpiredCaption(null);
            messages.setSessionExpiredMessage(null);
            messages.setSessionExpiredNotificationEnabled(false);
            messages.setSessionExpiredURL("");

            // Saat Terjadi Kesalahan Server yang Tidak Tertangani (Internal Error)
            messages.setInternalErrorCaption("Server Error");
            messages.setInternalErrorMessage(
                    "A technical problem occurred on the server or network. Click anywhere or press F5 to reload the page.");
            messages.setInternalErrorNotificationEnabled(true);
            messages.setInternalErrorURL(null);

            // Saat Cookie Dinonaktifkan di Browser
            messages.setCookiesDisabledCaption("Cookies Disabled");
            messages.setCookiesDisabledMessage(
                    "This application requires cookies to work properly. Please enable cookies in your browser.");
            messages.setCookiesDisabledNotificationEnabled(true);
            messages.setCookiesDisabledURL(null);

            return messages;
        });

        // 2. Kustomisasi Reconnect Dialog di level UI (banner saat koneksi terputus
        // sesaat/permanen)
        event.getSource().addUIInitListener(uiEvent -> {
            UI ui = uiEvent.getUI();
            ReconnectDialogConfiguration reconnectConfig = ui.getReconnectDialogConfiguration();

            // Pesan saat sedang mencoba menghubungkan kembali
            reconnectConfig.setDialogText("⚠️ Connection to the server was lost. Trying to reconnect...");

            // Pesan saat usaha reconnect gagal total / menyerah
            reconnectConfig.setDialogTextGaveUp(
                    "❌ Connection to the server was permanently lost, or the server is offline. Check your network and reload the page (F5).");

            // Interval percobaan reconnect (setiap 3 detik)
            reconnectConfig.setReconnectInterval(3000);

            // Maksimum jumlah percobaan reconnect (40 kali = ~2 menit total)
            reconnectConfig.setReconnectAttempts(40);
        });
    }
}
