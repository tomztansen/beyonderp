package com.vaadinerp.report;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Apakah sebuah .jrxml punya query sendiri (bagian {@code <query>} Jasper 7 atau {@code <queryString>}
 * lama) yang tidak kosong. Hanya membaca teks file, tanpa meng-compile: dipanggil setiap report dijalankan.
 * Query di dalam subDataset tidak dihitung; hanya query tingkat atas report.
 */
public final class JrxmlQuery {

    private static final Pattern QUERY = Pattern.compile(
            "<(?:query|queryString)\\b[^>]*?(?:/>|>(.*?)</(?:query|queryString)>)", Pattern.DOTALL);
    private static final Pattern SUB_DATASET = Pattern.compile("<subDataset\\b.*?</subDataset>", Pattern.DOTALL);
    private static final Pattern CDATA = Pattern.compile("<!\\[CDATA\\[(.*?)\\]\\]>", Pattern.DOTALL);

    private JrxmlQuery() {
    }

    public static boolean hasOwnQuery(File jrxml) {
        if (jrxml == null || !jrxml.isFile()) {
            return false;
        }
        String xml;
        try {
            xml = Files.readString(jrxml.toPath());
        } catch (Exception e) {
            return false;
        }
        xml = SUB_DATASET.matcher(xml).replaceAll("");
        Matcher m = QUERY.matcher(xml);
        while (m.find()) {
            String body = m.group(1);
            if (body == null) {
                continue; // <query/> kosong
            }
            Matcher c = CDATA.matcher(body);
            String text = c.find() ? c.group(1) : body;
            if (!text.isBlank()) {
                return true;
            }
        }
        return false;
    }
}
