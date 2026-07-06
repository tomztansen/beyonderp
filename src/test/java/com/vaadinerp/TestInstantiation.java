package com.vaadinerp;

import com.vaadinerp.views.PortalView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
public class TestInstantiation {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testPortalViewCreation() {
        try {
            PortalView view = applicationContext.getAutowireCapableBeanFactory().createBean(PortalView.class);
            System.out.println("PortalView bean created successfully: " + view);
        } catch (Throwable e) {
            System.err.println("=== PORTALVIEW CREATION ERROR ===");
            Throwable cause = e;
            while (cause != null) {
                System.err.println("=== CAUSE ===");
                cause.printStackTrace();
                cause = cause.getCause();
            }
            throw e;
        }
    }
}
