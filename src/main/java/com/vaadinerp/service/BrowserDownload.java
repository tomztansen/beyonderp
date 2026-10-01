package com.vaadinerp.service;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

/**
 * Memicu unduhan ke browser dari kode server. Satu Anchor tersembunyi dipakai ulang per UI (hanya
 * href-nya yang diganti), jadi berapa pun unduhan tidak menambah komponen maupun resource yang
 * tertahan di sesi. Konsekuensinya: SATU unduhan per respons -- unduhan kedua pada respons yang
 * sama mengganti handler sebelum browser mengambilnya.
 */
public final class BrowserDownload {

    private static final String ANCHOR_KEY = "scriptDownloadAnchor";

    private BrowserDownload() {
    }

    /** Handler yang menyajikan {@code bytes} saat browser memintanya. Byte tertahan sampai diganti. */
    public static DownloadHandler bytes(byte[] bytes, String fileName, String contentType) {
        return DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new java.io.ByteArrayInputStream(bytes), fileName, contentType, bytes.length));
    }

    /** Ganti href anchor tersembunyi ke handler ini lalu klik. Panggil dari dalam {@code ui.access}. */
    public static void trigger(UI ui, DownloadHandler handler) {
        Anchor anchor = anchor(ui);
        anchor.setHref(handler);
        anchor.getElement().executeJs("this.click()");
    }

    private static Anchor anchor(UI ui) {
        Object existing = ComponentUtil.getData(ui, ANCHOR_KEY);
        if (existing instanceof Anchor a && a.isAttached()) {
            return a;
        }
        Anchor a = new Anchor();
        a.setDownload(true);
        a.setRouterIgnore(true); // router Vaadin mencegat anchor berhref relatif
        a.getStyle().set("display", "none");
        ui.add(a);
        ComponentUtil.setData(ui, ANCHOR_KEY, a);
        return a;
    }
}
