package com.vaadinerp.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadinerp.components.StandardActionToolbar.MenuAccessAuthority;
import com.vaadinerp.config.SpringContextHolder;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.security.service.SessionSecurityService;
import com.vaadinerp.views.PortalView;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tautan kecil "Add new" di bawah field LOV. Diatur per field lewat Form Builder (lov_add_form, lov_add_params).
 * Field tanpa setelan tidak diubah sama sekali. Ditaruh di helper slot supaya field tidak perlu dibungkus layout
 * (ComboBox Vaadin 24 hanya punya slot prefix, tidak ada suffix).
 */
public final class LovAddButton {

    private static final Logger log = LoggerFactory.getLogger(LovAddButton.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static final String NOT_ALLOWED = "You are not allowed to add this item.";

    private LovAddButton() {
    }

    /** Membuka tab form tujuan. Mengembalikan pesan galat, atau null bila berhasil. */
    @FunctionalInterface
    interface TabOpener {
        String open(Component from, String form, Map<String, Object> extra);
    }

    /** User boleh membuka form tujuan: punya akses layar dan hak tambah atau ubah. */
    static boolean allowedFor(MenuAccessAuthority a) {
        return a != null && a.canAccessScreen && (a.canAdd || a.canEdit);
    }

    static boolean isVisible(String lovAddForm, boolean readonly, boolean allowed) {
        return lovAddForm != null && !lovAddForm.isBlank() && !readonly && allowed;
    }

    /** JSON peta {"kunci":"nilai"}. Kosong atau rusak = peta kosong (galat dicatat, tidak dilempar). */
    static Map<String, Object> parseParams(String json) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (json == null || json.isBlank()) {
            return out;
        }
        try {
            Map<String, String> m = MAPPER.readValue(json, new TypeReference<Map<String, String>>() {
            });
            m.forEach((k, v) -> {
                if (k != null && v != null) {
                    out.put(k, v);
                }
            });
        } catch (Exception e) {
            log.warn("lov_add_params bukan JSON peta yang valid, diabaikan: {}", e.getMessage());
            out.clear();
        }
        return out;
    }

    public static void attach(Component component, FieldMeta field) {
        attachOpenForm(component, field, LovAddButton::authorityOf, LovAddButton::openTab, LovAddButton::toast);
    }

    static void attachOpenForm(Component component, FieldMeta field,
            Function<String, MenuAccessAuthority> authorityOf, TabOpener opener, Consumer<String> notify) {
        String configured = field.getLovAddForm();
        if (!(component instanceof HasHelper helper) || configured == null || configured.isBlank()) {
            return;
        }
        final String target = configured.trim();
        if (!isVisible(target, field.isReadonly(), allowedFor(authorityOf.apply(target)))) {
            return;
        }
        Button add = createButton();
        add.addClickListener(e -> {
            // Hak diperiksa lagi: tombol dibuat saat form dibuka, hak bisa berubah sesudahnya.
            if (!allowedFor(authorityOf.apply(target))) {
                notify.accept(NOT_ALLOWED);
                return;
            }
            String err = opener.open(component, target, parseParams(field.getLovAddParams()));
            if (err != null) {
                notify.accept(err);
            }
        });
        helper.setHelperComponent(add);
    }

    /** Tampilan minimal: tautan kecil berwarna primer dengan ikon plus, tanpa bingkai dan tanpa jarak tambahan. */
    private static Button createButton() {
        Icon plus = new Icon(VaadinIcon.PLUS);
        plus.getStyle().set("width", "0.85em").set("height", "0.85em");
        Button add = new Button("Add new", plus);
        add.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        add.addClassName("lov-add-button");
        add.setTooltipText("Add new item");
        return add;
    }

    private static MenuAccessAuthority authorityOf(String menuCode) {
        SessionSecurityService s = SpringContextHolder.getBean(SessionSecurityService.class);
        return s == null ? null : s.getAuthorityForMenu(menuCode);
    }

    private static String openTab(Component from, String form, Map<String, Object> extra) {
        FormMetaRepository forms = SpringContextHolder.getBean(FormMetaRepository.class);
        if (forms != null && !forms.existsById(form)) {
            return "Target form '" + form + "' was not found.";
        }
        PortalView portal = findPortal(from);
        if (portal == null) {
            return "Cannot open the form: portal not available.";
        }
        // tabId tetap per form: klik berulang hanya memilih tab yang sudah ada, tidak menumpuk tab.
        portal.openTabByCode(form, "LOVADD_" + form, null, extra);
        return null;
    }

    static PortalView findPortal(Component from) {
        for (Component c = from; c != null; c = c.getParent().orElse(null)) {
            if (c instanceof PortalView pv) {
                return pv;
            }
        }
        UI ui = UI.getCurrent();
        if (ui != null) {
            for (Component c : ui.getChildren().toList()) {
                if (c instanceof PortalView pv) {
                    return pv;
                }
            }
        }
        return null;
    }

    static void toast(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
    }
}
