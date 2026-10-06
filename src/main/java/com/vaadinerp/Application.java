package com.vaadinerp;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
@Theme(value = "vaadinerp", variant = Lumo.LIGHT)
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET_XHR)
public class Application implements AppShellConfigurator {

    @org.springframework.context.annotation.Bean
    public com.vaadin.flow.server.VaadinServiceInitListener vaadinServiceInitListener(
            com.vaadinerp.service.EnvironmentInfo env) {
        return event -> event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().getLoadingIndicatorConfiguration().setFirstDelay(150);
            uiEvent.getUI().getLoadingIndicatorConfiguration().setSecondDelay(1000);
            uiEvent.getUI().getLoadingIndicatorConfiguration().setThirdDelay(3000);
            // Awalan judul tab + favicon dev; dijalankan di browser, null di PROD.
            String script = env.pageScript();
            if (script != null) {
                uiEvent.getUI().getPage().executeJs(script, env.titlePrefix(), env.faviconPath());
            }
        });
    }

    @Override
    public void configurePage(com.vaadin.flow.server.AppShellSettings settings) {
        // File: META-INF/resources/icons/favicon.ico (16/24/32 px). Bukan @PWA: tidak perlu manifest/service worker.
        settings.addFavIcon("icon", "icons/favicon.ico", "16x16 24x24 32x32");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
