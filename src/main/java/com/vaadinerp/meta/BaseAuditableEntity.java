package com.vaadinerp.meta;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Optional;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseAuditableEntity {

    @Column(name = "inputby", length = 255, updatable = false)
    private String inputby;

    @Column(name = "inputdt", updatable = false)
    private LocalDateTime inputdt;

    @Column(name = "updateby", length = 255)
    private String updateby;

    @Column(name = "updatedt")
    private LocalDateTime updatedt;

    @Version
    @Column(name = "version")
    private Integer version = 0;

    @PrePersist
    public void onPrePersistAudit() {
        this.inputby = fetchCurrentUser();
        this.inputdt = LocalDateTime.now();
        if (this.version == null) {
            this.version = 0;
        }
    }

    @PreUpdate
    public void onPreUpdateAudit() {
        this.updateby = fetchCurrentUser();
        this.updatedt = LocalDateTime.now();
    }

    private String fetchCurrentUser() {
        try {
            if (com.vaadin.flow.server.VaadinSession.getCurrent() != null) {
                Object obj = com.vaadin.flow.server.VaadinSession.getCurrent()
                        .getAttribute(com.vaadinerp.security.service.SessionSecurityService.SESSION_USER_KEY);
                if (obj != null) {
                    // Pakai reflection agar kebal dari masalah Spring Boot DevTools ClassLoader
                    java.lang.reflect.Method getUsernameMethod = obj.getClass().getMethod("getUsername");
                    String username = (String) getUsernameMethod.invoke(obj);
                    if (username != null && !username.trim().isEmpty()) {
                        return username.trim();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "system";
    }
}
