package com.vaadinerp.security.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user_grid_preferences", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username", "form_code", "grid_id"})
})
@Data
public class AppUserGridPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "form_code", length = 100, nullable = false)
    private String formCode;

    @Column(name = "grid_id", length = 100, nullable = false)
    private String gridId;

    @Column(name = "column_order_json", columnDefinition = "TEXT")
    private String columnOrderJson;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
