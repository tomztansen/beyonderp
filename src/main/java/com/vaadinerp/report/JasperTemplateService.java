package com.vaadinerp.report;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Muat/compile template Jasper. Menerima .jasper (load langsung) dan .jrxml
 * (compile). Hasil di-cache by path+mtime agar compile hanya sekali per versi file.
 */
@Service
public class JasperTemplateService {

    private final ConcurrentHashMap<String, JasperReport> cache = new ConcurrentHashMap<>();

    /** Compile .jrxml dari stream (dipakai validasi saat upload). */
    public JasperReport compileForUpload(InputStream jrxml) throws JRException {
        return JasperCompileManager.compileReport(jrxml);
    }

    /** .jasper → load; .jrxml → compile. Hasil di-cache by path+mtime. */
    public JasperReport loadCompiled(File template) throws JRException {
        String key = template.getAbsolutePath() + "#" + template.lastModified();
        JasperReport cached = cache.get(key);
        if (cached != null) return cached;

        JasperReport jr;
        if (template.getName().toLowerCase().endsWith(".jrxml")) {
            jr = JasperCompileManager.compileReport(template.getAbsolutePath());
        } else {
            jr = (JasperReport) JRLoader.loadObject(template);
        }
        cache.put(key, jr);
        return jr;
    }
}
