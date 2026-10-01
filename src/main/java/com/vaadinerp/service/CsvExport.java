package com.vaadinerp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Penyusun CSV untuk downloadCsv() di script Groovy. Murni (tanpa Vaadin/Spring) supaya aturannya
 * bisa diuji: kutip dan escape yang benar, BOM UTF-8 (Excel membaca UTF-8 dengan benar), baris
 * CRLF, nilai null jadi kosong, angka tanpa notasi ilmiah, tanggal/waktu ISO, dan sel teks yang
 * diawali karakter rumus (= + - @ tab CR) diberi awalan ' supaya tidak dieksekusi Excel (CSV injection).
 * Batas jumlah baris menolak, bukan memotong diam-diam.
 */
public final class CsvExport {

    public static final int MAX_ROWS = 50_000;

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Pattern NUMERIC_TEXT = Pattern.compile("[-+]?\\d+(\\.\\d+)?");

    private CsvExport() {
    }

    /**
     * @param rows          baris data (mis. hasil db.queryForList); null/kosong ditolak kecuali ada peta judul
     * @param headers       null = kolom dan judul dari baris pertama; berisi = pilih, urutkan, dan beri judul
     *                      kolom (kunci = nama kolom, nilai = judul; judul null = nama kolom)
     * @param includeHeader tulis baris judul
     * @return isi file (BOM + CSV UTF-8)
     * @throws IllegalArgumentException tanpa data, atau lebih dari {@code maxRows} baris
     */
    public static byte[] build(List<? extends Map<String, ?>> rows, Map<String, String> headers,
                               boolean includeHeader, char delimiter, int maxRows) {
        int count = rows == null ? 0 : rows.size();
        if (count > maxRows) {
            throw new IllegalArgumentException("Too many rows (" + count + "); the limit is " + maxRows
                    + ". Narrow the query or use a report.");
        }

        List<String> columns = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                columns.add(e.getKey());
                titles.add(e.getValue() != null ? e.getValue() : e.getKey());
            }
        } else {
            if (count == 0) {
                throw new IllegalArgumentException("No data to export");
            }
            for (String k : rows.get(0).keySet()) {
                if (k != null) {
                    columns.add(k);
                    titles.add(k);
                }
            }
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(1024, count * 64));
        bytes.write(0xEF);
        bytes.write(0xBB);
        bytes.write(0xBF);
        try (Writer w = new OutputStreamWriter(bytes, StandardCharsets.UTF_8)) {
            if (includeHeader) {
                writeLine(w, titles.stream().map(CsvExport::text).toList(), delimiter);
            }
            List<Cell> cells = new ArrayList<>(columns.size());
            if (rows != null) {
                for (Map<String, ?> row : rows) {
                    cells.clear();
                    for (String col : columns) {
                        cells.add(cell(row.get(col)));
                    }
                    writeLine(w, cells, delimiter);
                }
            }
        } catch (IOException e) {
            // ByteArrayOutputStream tidak melempar IOException; hanya untuk memenuhi tanda tangan Writer.
            throw new IllegalStateException(e);
        }
        return bytes.toByteArray();
    }

    /** Pemisah kolom: kosong = titik koma (Excel regional Indonesia); hanya , ; tab | yang diizinkan. */
    public static char delimiterOf(String s) {
        if (s == null || s.isEmpty()) {
            return ';';
        }
        if (s.length() != 1 || ",;\t|".indexOf(s.charAt(0)) < 0) {
            throw new IllegalArgumentException("Delimiter must be one of: comma, semicolon, tab, pipe");
        }
        return s.charAt(0);
    }

    /** Nama unduhan yang aman, selalu berakhiran .csv. */
    public static String fileName(String name) {
        String n = name == null ? "" : name.trim().replaceAll("[^A-Za-z0-9._ -]", "_");
        n = n.replaceAll("^\\.+", "");
        if (n.toLowerCase().endsWith(".csv")) {
            n = n.substring(0, n.length() - 4);
        }
        n = n.trim();
        return (n.isEmpty() ? "export" : n) + ".csv";
    }

    private static void writeLine(Writer w, List<Cell> cells, char delimiter) throws IOException {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                w.write(delimiter);
            }
            w.write(quote(neutralize(cells.get(i)), delimiter));
        }
        w.write("\r\n");
    }

    /** Teks sel untuk satu nilai; hanya String dan objek lain (bukan angka/tanggal) yang dinetralkan. */
    private static Cell cell(Object v) {
        if (v == null) {
            return text("");
        }
        if (v instanceof BigDecimal bd) {
            return safe(bd.toPlainString());
        }
        if (v instanceof Double d) {
            return safe(d.isNaN() || d.isInfinite() ? d.toString() : BigDecimal.valueOf(d).toPlainString());
        }
        if (v instanceof Float f) {
            return safe(f.isNaN() || f.isInfinite() ? f.toString()
                    : new BigDecimal(Float.toString(f)).toPlainString());
        }
        if (v instanceof Number || v instanceof Boolean) {
            return safe(v.toString());
        }
        if (v instanceof java.sql.Timestamp ts) {
            return safe(ts.toLocalDateTime().format(DATE_TIME));
        }
        if (v instanceof java.sql.Date d) {
            return safe(d.toLocalDate().toString());
        }
        if (v instanceof java.sql.Time t) {
            return safe(t.toLocalTime().format(TIME));
        }
        if (v instanceof java.util.Date d) {
            return safe(new java.sql.Timestamp(d.getTime()).toLocalDateTime().format(DATE_TIME));
        }
        if (v instanceof LocalDateTime dt) {
            return safe(dt.format(DATE_TIME));
        }
        if (v instanceof java.time.OffsetDateTime odt) {
            return safe(odt.toLocalDateTime().format(DATE_TIME));
        }
        if (v instanceof java.time.ZonedDateTime zdt) {
            return safe(zdt.toLocalDateTime().format(DATE_TIME));
        }
        if (v instanceof java.time.Instant i) {
            return safe(LocalDateTime.ofInstant(i, ZoneId.systemDefault()).format(DATE_TIME));
        }
        if (v instanceof LocalDate d) {
            return safe(d.toString());
        }
        if (v instanceof LocalTime t) {
            return safe(t.format(TIME));
        }
        return text(String.valueOf(v));
    }

    /** Satu sel: teks + apakah sudah pasti aman (angka/tanggal/boolean) sehingga tidak perlu dinetralkan. */
    private record Cell(String text, boolean safe) {
    }

    private static Cell safe(String s) {
        return new Cell(s, true);
    }

    private static Cell text(String s) {
        return new Cell(s, false);
    }

    /** Sel teks yang diawali karakter rumus diberi awalan '; angka/tanggal dan teks berbentuk angka dibiarkan. */
    private static String neutralize(Cell cell) {
        String s = cell.text();
        if (cell.safe() || s.isEmpty()) {
            return s;
        }
        char c = s.charAt(0);
        if ((c == '=' || c == '+' || c == '-' || c == '@' || c == 9 || c == 13) && !NUMERIC_TEXT.matcher(s).matches()) { // 9 = tab, 13 = CR
            return "'" + s;
        }
        return s;
    }

    private static String quote(String s, char delimiter) {
        boolean needs = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == delimiter || c == '"' || c == '\n' || c == '\r') {
                needs = true;
                break;
            }
        }
        return needs ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }
}
