package com.vaadinerp.report.render;

import org.springframework.stereotype.Component;
import java.util.List;

/** Memilih {@link ReportRenderer} sesuai engineType (Spring meng-inject semua implementasi). */
@Component
public class ReportRendererRegistry {

    private final List<ReportRenderer> renderers;

    public ReportRendererRegistry(List<ReportRenderer> renderers) {
        this.renderers = renderers;
    }

    public ReportRenderer forEngine(String engineType) {
        String e = engineType == null ? "STANDARD" : engineType.trim();
        return renderers.stream()
                .filter(r -> r.engine().equalsIgnoreCase(e))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Engine tidak didukung: " + engineType));
    }
}
