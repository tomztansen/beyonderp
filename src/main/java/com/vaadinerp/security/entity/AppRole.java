package com.vaadinerp.security.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_roles", schema = "public")
@Data
public class AppRole {
    @Id
    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Column(name = "role_name", length = 100)
    private String roleName;

    @Column(name = "description", length = 255)
    private String description;
}
