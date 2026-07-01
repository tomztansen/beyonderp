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
