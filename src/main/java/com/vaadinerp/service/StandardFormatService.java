package com.vaadinerp.service;

import com.vaadinerp.meta.AppStandardFormat;
import com.vaadinerp.meta.AppStandardFormatRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StandardFormatService {

    private static StandardFormatService instance;
    private final AppStandardFormatRepository repository;
    private final Map<String, String> formatCache = new ConcurrentHashMap<>();

    /** Locale angka bawaan: Indonesia (1.250.000,75) -- perilaku lama. */
    static final java.util.Locale DEFAULT_NUMBER_LOCALE = java.util.Locale.of("id", "ID");

    /** Baris app_standard_format yang menyimpan locale angka; diedit dari menu Standard Format lewat combobox. */
    public static final String NUMBER_LOCALE = "NUMBER_LOCALE";

    /**
     * Pilihan locale angka. Dibatasi pada locale yang pemisahnya titik/koma, karena kotak input hanya menerima
     * angka, '.', ',' dan tanda; locale berpemisah spasi/apostrof tidak bisa diketik dengan benar.
     */
    public static final List<String> SUPPORTED_NUMBER_LOCALES = List.of("id-ID", "en-US", "en-GB", "de-DE");

    /**
     * Locale pemisah angka (ribuan/desimal) untuk tampilan DAN pembacaan input. Diambil dari baris NUMBER_LOCALE
     * di cache (bukan query per tampilan); berubah langsung saat admin menyimpan. Tanggal/kalender tidak terpengaruh.
     */
    private static volatile java.util.Locale numberLocale = DEFAULT_NUMBER_LOCALE;

    public StandardFormatService(AppStandardFormatRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        instance = this;
        seedDefaults();
        refreshCache();
    }

    static java.util.Locale parseLocale(String tag) {
        if (tag == null || tag.isBlank()) {
            return DEFAULT_NUMBER_LOCALE;
        }
        java.util.Locale loc = java.util.Locale.forLanguageTag(tag.trim().replace('_', '-'));
        return loc.getLanguage().isEmpty() ? DEFAULT_NUMBER_LOCALE : loc;
    }

    public static java.util.Locale numberLocale() {
        return numberLocale;
    }

    /** Hanya untuk test: ganti locale angka. */
    static void setNumberLocaleForTest(java.util.Locale locale) {
        numberLocale = locale != null ? locale : DEFAULT_NUMBER_LOCALE;
    }

    /**
     * DecimalFormat untuk tampilan angka: simbol mengikuti NUMBER_LOCALE (Standard Format), pembulatan setengah ke atas
     * (1,125 -> 1,13). Bawaan Java HALF_EVEN membulatkan 1,125 ke 1,12, yang tidak lazim untuk angka bisnis.
     */
    public static java.text.DecimalFormat decimalFormat(String pattern) {
        java.text.DecimalFormat df = new java.text.DecimalFormat(pattern,
                java.text.DecimalFormatSymbols.getInstance(numberLocale));
        df.setRoundingMode(java.math.RoundingMode.HALF_UP);
        return df;
    }

    /**
     * Teks angka sesuai NUMBER_LOCALE (Standard Format) -> teks yang bisa dibaca BigDecimal (tanpa pemisah ribuan, titik
     * sebagai desimal). id-ID: "1.250.000,75" -> "1250000.75"; en-US: "1,250,000.75" -> "1250000.75".
     */
    public static String normalizeNumberText(String text) {
        if (text == null) {
            return null;
        }
        java.text.DecimalFormatSymbols sym = java.text.DecimalFormatSymbols.getInstance(numberLocale);
        String out = text.replace(String.valueOf(sym.getGroupingSeparator()), "");
        if (Character.isSpaceChar(sym.getGroupingSeparator())) {
            out = out.replace(" ", ""); // locale dengan spasi sebagai pemisah ribuan
        }
        return out.replace(sym.getDecimalSeparator(), '.');
    }

    public static StandardFormatService getInstance() {
        return instance;
    }

    public static String getStandardFormat(String componentType, String fallback) {
        if (instance != null) {
            return instance.getFormat(componentType, fallback);
        }
        return fallback;
    }

    private void seedDefaults() {
        seedIfMissing("DATEBOX", "dd/MM/yyyy", "Format tanggal standar (hari/bulan/tahun)");
        seedIfMissing("DATETIMEBOX", "dd/MM/yyyy HH:mm", "Format tanggal & waktu standar");
        seedIfMissing("TIMEBOX", "HH:mm", "Format waktu standar (jam:menit)");
        seedIfMissing("INTBOX", "#,##0", "Format bilangan bulat standar (dengan pemisah ribuan)");
        seedIfMissing("DECIMALBOX", "#,##0.00", "Format angka desimal standar (2 angka di belakang koma)");
        seedIfMissing("CURRENCY", "Rp #,##0.00", "Format mata uang standar");
        // id-ID = perilaku lama, jadi instance yang sudah berjalan tidak berubah tampilannya.
        seedIfMissing(NUMBER_LOCALE, "id-ID", "Number separators for display and input (thousands / decimal)");
    }

    private void seedIfMissing(String type, String pattern, String description) {
        if (!repository.existsById(type)) {
            AppStandardFormat fmt = new AppStandardFormat();
            fmt.setComponentType(type);
            fmt.setFormatPattern(pattern);
            fmt.setDescription(description);
            fmt.setUpdatedAt(LocalDateTime.now());
            repository.save(fmt);
        }
    }

    public void refreshCache() {
        formatCache.clear();
        try {
            List<AppStandardFormat> list = repository.findAll();
            for (AppStandardFormat fmt : list) {
                if (fmt.getFormatPattern() != null && !fmt.getFormatPattern().trim().isEmpty()) {
                    formatCache.put(fmt.getComponentType().toUpperCase(), fmt.getFormatPattern().trim());
                }
            }
        } catch (Exception ignored) {
        }
        numberLocale = parseLocale(formatCache.get(NUMBER_LOCALE));
    }

    public String getFormat(String componentType, String fallback) {
        if (componentType == null) {
            return fallback;
        }
        String pattern = formatCache.get(componentType.toUpperCase());
        return (pattern != null && !pattern.isEmpty()) ? pattern : fallback;
    }

    public List<AppStandardFormat> getAllFormats() {
        return repository.findAll();
    }

    @Transactional
    public AppStandardFormat saveFormat(String componentType, String pattern, String description) {
        if (NUMBER_LOCALE.equalsIgnoreCase(componentType)
                && (pattern == null || !SUPPORTED_NUMBER_LOCALES.contains(pattern.trim()))) {
            throw new IllegalArgumentException("Unsupported number locale: " + pattern);
        }
        AppStandardFormat fmt = repository.findById(componentType).orElse(new AppStandardFormat());
        fmt.setComponentType(componentType);
        fmt.setFormatPattern(pattern != null ? pattern.trim() : "");
        fmt.setDescription(description);
        fmt.setUpdatedAt(LocalDateTime.now());
        AppStandardFormat saved = repository.save(fmt);
        refreshCache();
        return saved;
    }
}
