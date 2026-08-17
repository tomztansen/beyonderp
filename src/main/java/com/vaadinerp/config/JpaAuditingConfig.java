package com.vaadinerp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                if (com.vaadin.flow.server.VaadinSession.getCurrent() != null) {
                    Object obj = com.vaadin.flow.server.VaadinSession.getCurrent()
                            .getAttribute(com.vaadinerp.security.service.SessionSecurityService.SESSION_USER_KEY);
                    if (obj instanceof com.vaadinerp.security.entity.AppUser user) {
                        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                            return Optional.of(user.getUsername().trim());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            return Optional.of("system");
        };
    }
}
