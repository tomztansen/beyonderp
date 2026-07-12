package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_standard_format", schema = "public")
@EntityListeners(com.vaadinerp.service.AuditEntityListener.class)
@Data
public class AppStandardFormat {
    @Id
    @Column(name = "component_type", length = 50, nullable = false)
    private String componentType;

    @Column(name = "format_pattern", length = 100, nullable = false)
    private String formatPattern;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
