package com.vaadinerp.components;

import com.vaadin.flow.component.html.Span;
import com.vaadinerp.service.EnvironmentInfo;

/** Badge DEV/STAGING/ENV NOT SET untuk header dan halaman login. */
public final class EnvironmentBadge {

    private EnvironmentBadge() {
    }

    /** @return badge baru, atau null di PROD (tidak ada yang ditampilkan). */
    public static Span create(EnvironmentInfo env) {
        if (env == null || env.isProd()) {
            return null;
        }
        Span badge = new Span(env.badgeText());
        // Teks "DEV" selalu ada, jadi tidak bergantung pada warna saja (pengguna buta warna).
        badge.getElement().setAttribute("title", "Server: " + env.badgeText() + " - bukan data produksi");
        badge.getStyle()
                .set("background", env.color())
                .set("color", "#fff")
                .set("font-weight", "800")
                .set("font-size", "0.75rem")
                .set("letter-spacing", "0.06em")
                .set("padding", "3px 10px")
                .set("border-radius", "6px")
                .set("white-space", "nowrap")
                .set("user-select", "none");
        return badge;
    }
}
