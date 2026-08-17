package com.vaadinerp.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user_favorite_menus", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username", "menu_code"})
})
@Getter
@Setter
public class AppUserFavoriteMenu extends com.vaadinerp.meta.BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Column(name = "menu_code", length = 50, nullable = false)
    private String menuCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
