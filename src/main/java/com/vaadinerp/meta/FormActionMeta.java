package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import lombok.EqualsAndHashCode;

@Entity
@Table(name = "meta_form_action", schema = "public")
@EntityListeners(com.vaadinerp.service.AuditEntityListener.class)
@Data
@EqualsAndHashCode(of = {"id", "actionCode"})
@ToString(exclude = "formMeta")
public class FormActionMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "form_code", nullable = true)
    private FormMeta formMeta;

    @Column(name = "action_code", length = 50, nullable = false)
    private String actionCode;

    @Column(name = "action_label", length = 100, nullable = false)
    private String actionLabel;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "button_style", length = 50)
    private String buttonStyle; // e.g. "PRIMARY", "SUCCESS", "#10b981", etc.

    @Column(name = "target_scope", length = 50)
    private String targetScope; // e.g. "MASTER_TOOLBAR", "DETAIL_TOOLBAR"

    @Column(name = "action_type", length = 50)
    private String actionType = "POPUP_PICKER"; // e.g. "POPUP_PICKER"

    @Column(name = "source_lov_code", length = 50)
    private String sourceLovCode; // e.g. "MASTER_ITEM"

    @Column(name = "filter_mapping", length = 1000)
    private String filterMapping; // JSON mapping e.g. {"status": "'Active'"} or {"customer_id": "header.customer_code"}

    @Column(name = "target_mapping", length = 1000)
    private String targetMapping; // JSON mapping e.g. {"item_code": "item_code", "price": "unit_price"}

    @Column(name = "copy_source_lov_code", length = 50)
    private String copySourceLovCode; // e.g. "BOM_DETAIL"

    @Column(name = "copy_filter_mapping", length = 1000)
    private String copyFilterMapping; // e.g. {"item_id": "picked.id"}

    @Column(name = "menu_group", length = 100)
    private String menuGroup; // e.g. "Release Options" (jika diisi, dirender sebagai Dropdown/MenuBar)

    @Column(name = "script_content", length = 5000)
    private String scriptContent; // Groovy/DSL script untuk action_type = GROOVY_SCRIPT
}
