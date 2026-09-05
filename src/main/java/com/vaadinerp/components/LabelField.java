package com.vaadinerp.components;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Span;

/**
 * Read-only static label component for LABEL component type.
 * Text comes from field.defaultValue; style from field.displayFormat:
 *   HEADING  → 1.05em bold, primary color
 *   BOLD     → 600 weight, body color
 *   (empty)  → secondary color, normal weight
 */
public class LabelField extends CustomField<String> {

    private final String staticText;

    public LabelField(String label, String text, String styleVariant) {
        this.staticText = text != null ? text : "";

        if (label != null && !label.isBlank()) {
            setLabel(label);
        }

        Span span = new Span(staticText);
        span.getStyle().set("display", "block").set("padding", "4px 0");
        applyStyle(span, styleVariant);
        add(span);

        setReadOnly(true);
    }

    private void applyStyle(Span span, String variant) {
        if ("HEADING".equalsIgnoreCase(variant)) {
            span.getStyle()
                    .set("font-size", "1.05em")
                    .set("font-weight", "700")
                    .set("color", "var(--lumo-body-text-color)");
        } else if ("BOLD".equalsIgnoreCase(variant)) {
            span.getStyle()
                    .set("font-weight", "600")
                    .set("color", "var(--lumo-body-text-color)");
        } else {
            span.getStyle()
                    .set("color", "var(--lumo-body-text-color)");
        }
    }

    @Override
    protected String generateModelValue() {
        return staticText;
    }

    @Override
    protected void setPresentationValue(String value) {
        // Static label — ignore binder updates
    }
}
