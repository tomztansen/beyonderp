package com.vaadinerp.security.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_users", schema = "public")
@Data
public class AppUser {
    @Id
    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
