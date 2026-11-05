package com.vaadinerp.components;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.Span;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormattedBigDecimalField extends CustomField<BigDecimal> {
    private final TextField textField = new TextField();
    private String displayFormat;
    private boolean isReadOnly = false;
    private boolean isRequired = false;
    private SignToggle signToggle;

    public FormattedBigDecimalField(String label) {
        setLabel(label);
        textField.setWidthFull();
        textField.setAllowedCharPattern("[0-9\\-+.,eE]");
        add(textField);

        textField.addBlurListener(e -> {
            updateValue();
            formatDisplay();
        });
        
        textField.addValueChangeListener(e -> {
            updateValue();
        });
    }

    public void setDisplayFormat(String displayFormat) {
        this.displayFormat = displayFormat;
        if (displayFormat != null && !displayFormat.trim().isEmpty() && !"NONE".equalsIgnoreCase(displayFormat)) {
            String clean = displayFormat.trim();
            if (clean.toUpperCase().startsWith("RP ") || clean.toUpperCase().startsWith("RP")) {
                Span prefix = new Span("Rp ");
                prefix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
                textField.setPrefixComponent(prefix);
            } else if (clean.startsWith("$") || clean.toUpperCase().startsWith("USD")) {
                Span prefix = new Span("$ ");
                prefix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
                textField.setPrefixComponent(prefix);
            }
            if (clean.endsWith("%")) {
                Span suffix = new Span("%");
                suffix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
                textField.setSuffixComponent(suffix);
            } else if (clean.toUpperCase().endsWith(" KG")) {
                Span suffix = new Span(" kg");
                suffix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
                textField.setSuffixComponent(suffix);
            } else if (clean.toUpperCase().endsWith(" PCS")) {
                Span suffix = new Span(" pcs");
                suffix.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-weight", "500");
                textField.setSuffixComponent(suffix);
            }
            textField.setPlaceholder("");
        }
    }

    /** Pasang tombol +/-. Dipanggil sesudah setDisplayFormat supaya prefix Rp/$ ikut dipertahankan. */
    public void enableSignToggle() {
        if (signToggle != null)
            return;
        signToggle = new SignToggle(textField, this::updateValue);
        textField.setAllowedCharPattern("[0-9.,]");
        signToggle.setEnabled(!isReadOnly);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
        textField.setReadOnly(readOnly);
        if (signToggle != null)
            signToggle.setEnabled(!readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return isReadOnly;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        this.isRequired = requiredIndicatorVisible;
        textField.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return isRequired;
    }

    @Override
    protected BigDecimal generateModelValue() {
        String text = textField.getValue();
        if (text == null || text.trim().isEmpty()) return null;
        try {
            String clean = text.trim()
                .replaceAll("(?i)Rp\\s*", "")
                .replaceAll("(?i)USD\\s*", "")
                .replace("$", "")
                .replace("%", "")
                .replaceAll("(?i)kg", "")
                .replaceAll("(?i)pcs", "")
                .trim();
            clean = clean.replace(".", "").replace(",", ".");
            BigDecimal parsed = new BigDecimal(clean);
            if (signToggle == null)
                return parsed;
            return signToggle.isNegative() ? parsed.abs().negate() : parsed.abs();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void setPresentationValue(BigDecimal newPresentationValue) {
        if (newPresentationValue == null) {
            if (signToggle != null)
                signToggle.setNegative(false);
            textField.setValue("");
            return;
        }
        if (signToggle != null)
            signToggle.setNegative(newPresentationValue.signum() < 0);
        formatValue(newPresentationValue);
    }

    private void formatDisplay() {
        BigDecimal val = generateModelValue();
        if (val != null) {
            formatValue(val);
        }
    }

    private void formatValue(BigDecimal val) {
        if (signToggle != null)
            val = val.abs(); // tanda ditampilkan oleh tombol, bukan di kotak
        try {
            String pattern = displayFormat != null && !displayFormat.trim().isEmpty() && !"NONE".equalsIgnoreCase(displayFormat) 
                ? displayFormat.trim() : "#,##0.00";
            if (pattern.toUpperCase().startsWith("RP ")) pattern = pattern.substring(3).trim();
            else if (pattern.toUpperCase().startsWith("RP")) pattern = pattern.substring(2).trim();
            if (pattern.startsWith("$")) pattern = pattern.substring(1).trim();
            if (pattern.endsWith("%")) pattern = pattern.substring(0, pattern.length() - 1).trim();
            else if (pattern.toUpperCase().endsWith(" KG")) pattern = pattern.substring(0, pattern.length() - 3).trim();
            else if (pattern.toUpperCase().endsWith(" PCS")) pattern = pattern.substring(0, pattern.length() - 4).trim();

            if (!pattern.contains("#") && !pattern.contains("0")) pattern = "#,##0.00";

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("id", "ID"));
            DecimalFormat df = new DecimalFormat(pattern, symbols);
            textField.setValue(df.format(val));
        } catch (Exception e) {
            textField.setValue(val.toString());
        }
    }
}
