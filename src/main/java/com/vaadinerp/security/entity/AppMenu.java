package com.vaadinerp.security.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_menus", schema = "public")
@Data
public class AppMenu {
    @Id
    @Column(name = "menu_code", length = 50)
    private String menuCode;

    @Column(name = "menu_title", length = 100)
    private String menuTitle;

    @Column(name = "route_path", length = 100)
    private String routePath;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "parent_menu_code", length = 50)
    private String parentMenuCode;

    @Column(name = "display_order")
    private Integer displayOrder = 10;

    @Column(name = "menu_type", length = 20)
    private String menuType = "ITEM"; // "GROUP" (folder/parent) or "ITEM" (leaf/clickable)
}
