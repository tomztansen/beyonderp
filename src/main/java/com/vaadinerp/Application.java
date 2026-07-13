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
@Theme(value = "vaadinerp", variant = Lumo.LIGHT)
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET_XHR)
public class Application implements AppShellConfigurator {

    @org.springframework.context.annotation.Bean
    public com.vaadin.flow.server.VaadinServiceInitListener vaadinServiceInitListener() {
        return event -> event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().getLoadingIndicatorConfiguration().setFirstDelay(150);
            uiEvent.getUI().getLoadingIndicatorConfiguration().setSecondDelay(1000);
            uiEvent.getUI().getLoadingIndicatorConfiguration().setThirdDelay(3000);
        });
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
