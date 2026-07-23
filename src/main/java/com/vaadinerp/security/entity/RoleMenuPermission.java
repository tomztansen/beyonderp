package com.vaadinerp.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_role_menu_permissions", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role_code", "menu_code"})
})
@Getter
@Setter
public class RoleMenuPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", length = 50, nullable = false)
    private String roleCode;

    @Column(name = "menu_code", length = 50, nullable = false)
    private String menuCode;

    @Column(name = "can_add")
    private Boolean canAdd = true;

    @Column(name = "can_edit")
    private Boolean canEdit = true;

    @Column(name = "can_delete")
    private Boolean canDelete = true;

    @Column(name = "can_print")
    private Boolean canPrint = true;
}
