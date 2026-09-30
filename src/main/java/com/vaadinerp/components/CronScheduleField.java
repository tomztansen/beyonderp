package com.vaadinerp.components;

import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadinerp.scheduler.CronCodec;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Input jadwal visual; nilai tersimpan sebagai cron 6 field (lihat {@link CronCodec}).
 *
 * Nilai kosong tampil sebagai "Daily 09:00" di kontrol, tetapi nilai model tetap "" sampai
 * pengguna mengubah kontrol -- supaya field wajib-isi yang tak disentuh tetap ditolak.
 * Memuat nilai (setPresentationValue) tidak pernah memanggil updateValue(): cron lama yang
 * tidak persis pola bergambar dibuka di mode Advanced apa adanya, tanpa ditulis ulang.
 *
 * CustomField 24.10 TIDAK mengimplementasikan HasValidator, jadi dideklarasikan di sini
 * supaya Binder.forField(...) memasang getDefaultValidator() sebagai validator otomatis.
 */
public class CronScheduleField extends CustomField<String> implements HasValidator<String> {

    private static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE dd MMM yyyy HH:mm",
            Locale.ENGLISH);
    private static final String M_EVERY = "Every N minutes";
    private static final String M_DAILY = "Daily";
    private static final String M_MONTHLY = "Monthly";
    private static final String M_ADV = "Advanced (cron)";

    private final Select<String> repeat = new Select<>();
    private final Select<Integer> interval = new Select<>();
    private final TimePicker time = new TimePicker("At");
    private final CheckboxGroup<DayOfWeek> days = new CheckboxGroup<>();
    private final Select<Integer> dayOfMonth = new Select<>(); // 0 = hari terakhir
    private final TextField raw = new TextField("Cron (second minute hour day month weekday)");
    private final Div summary = new Div();
    private final Div nextRuns = new Div();
    private boolean updating;
    private String current = "";

    public CronScheduleField(String label) {
        // Default "" (bukan null) supaya asRequired Binder menganggap "" kosong; manualValueUpdate=true
        // supaya event "change" DOM dari kontrol dalam tidak memicu updateValue() di luar rebuild().
        super("", true);
        if (label != null && !label.isBlank()) {
            setLabel(label);
        }
        repeat.setLabel("Repeat");
        repeat.setItems(M_EVERY, M_DAILY, M_MONTHLY, M_ADV);

        interval.setLabel("Every (minutes)");
        interval.setItems(CronCodec.INTERVALS);

        time.setStep(Duration.ofMinutes(1));
        time.setLocale(Locale.UK); // format 24 jam, sama dengan cron

        days.setLabel("On days (none = every day)");
        days.setItems(DayOfWeek.values());
        days.setItemLabelGenerator(d -> d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        dayOfMonth.setLabel("Day of month");
        dayOfMonth.setItems(IntStream.rangeClosed(0, 28).boxed().collect(Collectors.toList()));
        dayOfMonth.setItemLabelGenerator(i -> i == 0 ? "Last day" : String.valueOf(i));

        raw.setWidthFull();
        raw.setValueChangeMode(ValueChangeMode.LAZY);

        // Satu baris kontrol; yang tampil ditentukan mode (lihat showModeControls).
        HorizontalLayout controls = new HorizontalLayout(interval, dayOfMonth, time, days);
        controls.setPadding(false);
        controls.setWrap(true);
        controls.setAlignItems(FlexComponent.Alignment.END);

        summary.getStyle().set("font-weight", "600");
        nextRuns.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color",
                "var(--lumo-secondary-text-color)");

        VerticalLayout box = new VerticalLayout(repeat, controls, raw, summary, nextRuns);
        box.setPadding(false);
        box.setSpacing(false);
        box.getStyle().set("gap", "8px");
        add(box);

        repeat.addValueChangeListener(e -> {
            if (updating) {
                return;
            }
            if (M_ADV.equals(e.getValue())) {
                // Awali kotak mentah dengan jadwal saat ini supaya bisa disunting, bukan ditulis dari nol.
                updating = true;
                try {
                    raw.setValue(current);
                } finally {
                    updating = false;
                }
            }
            showModeControls();
            rebuild();
        });
        interval.addValueChangeListener(e -> rebuild());
        time.addValueChangeListener(e -> rebuild());
        days.addValueChangeListener(e -> rebuild());
        dayOfMonth.addValueChangeListener(e -> rebuild());
        raw.addValueChangeListener(e -> rebuild());
        applyCron("");
    }

    /** Pilihan mode -> cron, atau (mode Advanced) teks mentah apa adanya. */
    private void rebuild() {
        if (updating) {
            return;
        }
        String cron;
        try {
            cron = switch (String.valueOf(repeat.getValue())) {
                case M_EVERY -> CronCodec.everyNMinutes(interval.getValue() != null ? interval.getValue() : 5);
                case M_DAILY -> CronCodec.daily(hour(), minute(), days.getValue());
                case M_MONTHLY -> CronCodec.monthly(dayOfMonth.getValue() != null ? dayOfMonth.getValue() : 1,
                        hour(), minute());
                default -> raw.getValue() != null ? raw.getValue().trim() : "";
            };
        } catch (IllegalArgumentException ex) {
            cron = "";
        }
        current = cron;
        refreshPreview();
        updateValue();
    }

    private int hour() {
        return time.getValue() != null ? time.getValue().getHour() : 9;
    }

    private int minute() {
        return time.getValue() != null ? time.getValue().getMinute() : 0;
    }

    /**
     * Isi kontrol dari cron yang ada. Hanya pola persis yang memakai mode bergambar; nilai kosong
     * tampil sebagai Daily 09:00. Tidak pernah memanggil updateValue().
     */
    private void applyCron(String cron) {
        updating = true;
        try {
            current = cron != null ? cron.trim() : "";
            interval.setValue(5);
            time.setValue(LocalTime.of(9, 0));
            days.clear();
            dayOfMonth.setValue(1);
            raw.setValue(current);
            if (current.isEmpty()) {
                repeat.setValue(M_DAILY);
            } else {
                CronCodec.Parsed p = CronCodec.parse(current);
                switch (p.mode()) {
                    case EVERY_N_MINUTES -> {
                        repeat.setValue(M_EVERY);
                        interval.setValue(p.intervalMinutes());
                    }
                    case DAILY -> {
                        repeat.setValue(M_DAILY);
                        time.setValue(LocalTime.of(p.hour(), p.minute()));
                        days.setValue(p.days().isEmpty() ? EnumSet.noneOf(DayOfWeek.class)
                                : EnumSet.copyOf(p.days()));
                    }
                    case MONTHLY -> {
                        repeat.setValue(M_MONTHLY);
                        time.setValue(LocalTime.of(p.hour(), p.minute()));
                        dayOfMonth.setValue(p.dayOfMonth());
                    }
                    case ADVANCED -> repeat.setValue(M_ADV);
                }
            }
            showModeControls();
            refreshPreview();
        } finally {
            updating = false;
        }
    }

    private void showModeControls() {
        String m = repeat.getValue();
        boolean daily = M_DAILY.equals(m);
        boolean monthly = M_MONTHLY.equals(m);
        interval.setVisible(M_EVERY.equals(m));
        dayOfMonth.setVisible(monthly);
        time.setVisible(daily || monthly); // jam:menit dipakai Daily dan Monthly
        days.setVisible(daily);
        raw.setVisible(M_ADV.equals(m));
    }

    private void refreshPreview() {
        if (current.isEmpty()) {
            summary.setText("");
            nextRuns.setText("");
            return;
        }
        var err = CronCodec.validate(current);
        if (err.isPresent()) {
            summary.setText("Invalid schedule");
            nextRuns.setText(err.get());
            return;
        }
        summary.setText(CronCodec.describe(current));
        List<ZonedDateTime> runs = CronCodec.nextRuns(current, ZonedDateTime.now(ZONE), 3);
        nextRuns.setText(runs.isEmpty() ? "No upcoming runs"
                : "Next: " + runs.stream().map(FMT::format).collect(Collectors.joining("  |  ")));
    }

    /**
     * Cron tidak valid ditolak Binder saat Save. Kosong dianggap lolos di sini: kewajiban isi
     * ditangani asRequired (Binder melewati validator bawaan bila field wajib dan kosong).
     */
    @Override
    public Validator<String> getDefaultValidator() {
        return (value, ctx) -> value == null || value.isBlank() ? ValidationResult.ok()
                : CronCodec.validate(value).map(ValidationResult::error).orElse(ValidationResult.ok());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // Bukan super.setReadOnly(): lihat catatan di QrScanField.setReadOnly (compiler Eclipse).
        getElement().setProperty("readonly", readOnly);
        for (HasEnabled c : List.<HasEnabled>of(repeat, interval, time, days, dayOfMonth, raw)) {
            c.setEnabled(!readOnly);
        }
    }

    @Override
    protected String generateModelValue() {
        return current;
    }

    @Override
    protected void setPresentationValue(String value) {
        applyCron(value);
    }
}
