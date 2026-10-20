package com.vaadinerp.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Membaca file upload CSV / XLSX / XLS menjadi list Map untuk script Groovy ({@code readUpload}).
 * Baris pertama = judul kolom; nama kolom dinormalkan (huruf kecil, selain huruf/angka jadi "_").
 * Baris yang seluruhnya kosong dilewati. Nilai kosong menjadi null.
 * Sel Excel: angka bulat -> Long, desimal -> Double, tanggal -> teks "yyyy-MM-dd" (atau dengan jam bila ada),
 * rumus dievaluasi. CSV: semua nilai berupa String.
 */
public final class UploadFileReader {

    static final long MAX_BYTES = 10L * 1024 * 1024;
    static final int MAX_ROWS = 20000;

    private UploadFileReader() {
    }

    public static List<Map<String, Object>> read(Path file) {
        try {
            if (Files.size(file) > MAX_BYTES) {
                throw new IllegalArgumentException("File is larger than " + (MAX_BYTES / 1024 / 1024) + " MB");
            }
            String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
            if (name.endsWith(".csv")) {
                return readCsv(file);
            }
            if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                return readExcel(file);
            }
            throw new IllegalArgumentException("Only .csv, .xlsx and .xls files can be read");
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read file: " + e.getMessage(), e);
        }
    }

    static String normalizeHeader(String h, int index) {
        String n = h == null ? "" : h.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return n.isEmpty() ? "col" + (index + 1) : n;
    }

    private static List<Map<String, Object>> toMaps(List<String> headers, List<List<Object>> rows) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (List<Object> r : rows) {
            boolean empty = true;
            Map<String, Object> m = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                Object v = i < r.size() ? r.get(i) : null;
                if (v != null) {
                    empty = false;
                }
                m.putIfAbsent(headers.get(i), v);
            }
            if (!empty) {
                if (out.size() >= MAX_ROWS) {
                    throw new IllegalArgumentException("File has more than " + MAX_ROWS + " rows");
                }
                out.add(m);
            }
        }
        return out;
    }

    // ---- CSV ----------------------------------------------------------------------------------------------

    private static List<Map<String, Object>> readCsv(Path file) throws IOException {
        String text;
        try (InputStream in = Files.newInputStream(file);
                Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[8192];
            int n;
            while ((n = r.read(buf)) > 0) {
                sb.append(buf, 0, n);
            }
            text = sb.toString();
        }
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        int nl = text.indexOf('\n');
        String first = nl < 0 ? text : text.substring(0, nl);
        char sep = ',';
        int best = count(first, ',');
        if (count(first, ';') > best) {
            sep = ';';
            best = count(first, ';');
        }
        if (count(first, '\t') > best) {
            sep = '\t';
        }

        List<List<String>> table = parseCsv(text, sep);
        if (table.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> headers = new ArrayList<>();
        List<String> raw = table.get(0);
        for (int i = 0; i < raw.size(); i++) {
            headers.add(normalizeHeader(raw.get(i), i));
        }
        List<List<Object>> rows = new ArrayList<>();
        for (int i = 1; i < table.size(); i++) {
            List<Object> row = new ArrayList<>();
            for (String c : table.get(i)) {
                row.add(c == null || c.trim().isEmpty() ? null : c.trim());
            }
            rows.add(row);
        }
        return toMaps(headers, rows);
    }

    private static int count(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                n++;
            }
        }
        return n;
    }

    /** Parser CSV (RFC 4180): kutip ganda, "" di dalam kutip, pemisah/baris baru di dalam kutip. */
    static List<List<String>> parseCsv(String s, char sep) {
        List<List<String>> out = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == sep) {
                row.add(cell.toString());
                cell.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < s.length() && s.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(cell.toString());
                cell.setLength(0);
                out.add(row);
                row = new ArrayList<>();
            } else {
                cell.append(c);
            }
        }
        if (cell.length() > 0 || !row.isEmpty()) {
            row.add(cell.toString());
            out.add(row);
        }
        return out;
    }

    // ---- Excel --------------------------------------------------------------------------------------------

    private static List<Map<String, Object>> readExcel(Path file) throws IOException {
        try (Workbook wb = WorkbookFactory.create(file.toFile(), null, true)) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter(Locale.ROOT);
            FormulaEvaluator ev = wb.getCreationHelper().createFormulaEvaluator();
            Row head = sheet.getRow(sheet.getFirstRowNum());
            if (head == null) {
                return new ArrayList<>();
            }
            int cols = Math.max(head.getLastCellNum(), 0);
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                headers.add(normalizeHeader(cellValue(head.getCell(c), fmt, ev) == null ? null
                        : cellValue(head.getCell(c), fmt, ev).toString(), c));
            }
            List<List<Object>> rows = new ArrayList<>();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                List<Object> vals = new ArrayList<>();
                for (int c = 0; c < cols; c++) {
                    vals.add(row == null ? null : cellValue(row.getCell(c), fmt, ev));
                }
                rows.add(vals);
            }
            return toMaps(headers, rows);
        }
    }

    private static Object cellValue(Cell cell, DataFormatter fmt, FormulaEvaluator ev) {
        if (cell == null) {
            return null;
        }
        CellType t = cell.getCellType();
        if (t == CellType.FORMULA) {
            try {
                t = ev.evaluateFormulaCell(cell);
            } catch (RuntimeException e) {
                return null;
            }
        }
        switch (t) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    java.time.LocalDateTime d = cell.getLocalDateTimeCellValue();
                    return d.toLocalTime().equals(java.time.LocalTime.MIDNIGHT)
                            ? d.toLocalDate().toString()
                            : d.toString().replace('T', ' ');
                }
                BigDecimal bd = BigDecimal.valueOf(cell.getNumericCellValue());
                return bd.stripTrailingZeros().scale() <= 0 && bd.abs().compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) < 0
                        ? (Object) bd.longValue()
                        : (Object) bd.doubleValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                String s = fmt.formatCellValue(cell, ev).trim();
                return s.isEmpty() ? null : s;
            default:
                return null;
        }
    }
}
