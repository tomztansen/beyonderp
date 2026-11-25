package com.vaadinerp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Pembacaan skema tabel/query (kolom dan detail tipe). Dipisah dari {@link DynamicDataService} (langkah A refactor):
 * hanya membaca, tanpa transaksi dan tanpa state, sehingga aman dipakai bersama oleh bagian LOV dan DDL tanpa
 * ketergantungan melingkar. Logika dipindah apa adanya; {@link DynamicDataService} meneruskan panggilan lewat
 * delegasi sehingga pemanggil lama tidak berubah. Penjaga SQL ({@code isCustomSelectQuery},
 * {@code validateAndSanitizeSelectQuery}) sengaja tetap di DynamicDataService dan dipanggil secara statis.
 */
public class TableSchemaReader {

    private static final Logger log = LoggerFactory.getLogger(TableSchemaReader.class);

    private final JdbcTemplate jdbcTemplate;

    public TableSchemaReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> fetchTableColumns(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            String rawName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;
            return jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_schema IN ('dynamic', 'public') AND table_name = ? ORDER BY ordinal_position",
                    String.class,
                    rawName.toLowerCase());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> fetchTableSchemaDetails(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String trimmed = tableName.trim();
        if (DynamicDataService.isCustomSelectQuery(trimmed) || trimmed.contains(" ")) {
            return fetchSchemaDetailsForQuery(trimmed);
        }
        try {
            String rawName = trimmed.contains(".") ? trimmed.substring(trimmed.indexOf(".") + 1) : trimmed;
            List<Map<String, Object>> res = jdbcTemplate.queryForList(
                    "SELECT column_name, data_type, character_maximum_length, numeric_precision, numeric_scale, is_nullable, column_default "
                            +
                            "FROM information_schema.columns " +
                            "WHERE table_schema IN ('dynamic', 'public') AND table_name = ? " +
                            "ORDER BY ordinal_position",
                    rawName.toLowerCase());
            if (!res.isEmpty()) {
                for (Map<String, Object> row : res) {
                    String dt = row.get("data_type") != null ? row.get("data_type").toString().toUpperCase() : "";
                    Object maxLen = row.get("character_maximum_length");
                    Object prec = row.get("numeric_precision");
                    Object scale = row.get("numeric_scale");
                    if (("CHARACTER VARYING".equals(dt) || "VARCHAR".equals(dt) || "CHAR".equals(dt)
                            || "CHARACTER".equals(dt)) && maxLen != null && !"null".equals(maxLen.toString())) {
                        row.put("formatted_type", "VARCHAR(" + maxLen + ")");
                    } else if ("NUMERIC".equals(dt) || "DECIMAL".equals(dt)) {
                        if (prec != null && !"null".equals(prec.toString())) {
                            row.put("formatted_type", "DECIMAL(" + prec + ","
                                    + (scale != null && !"null".equals(scale.toString()) ? scale : "0") + ")");
                        } else {
                            row.put("formatted_type", dt);
                        }
                    } else {
                        row.put("formatted_type", dt);
                    }
                }
                return res;
            }
            return fetchSchemaDetailsForQuery(trimmed);
        } catch (Exception e) {
            return fetchSchemaDetailsForQuery(trimmed);
        }
    }

    private List<Map<String, Object>> fetchSchemaDetailsForQuery(String queryOrTable) {
        try {
            String sql;
            if (DynamicDataService.isCustomSelectQuery(queryOrTable)) {
                String safeQuery = DynamicDataService.validateAndSanitizeSelectQuery(queryOrTable);
                sql = "SELECT * FROM ( " + safeQuery + " ) AS subquery LIMIT 1";
            } else {
                sql = "SELECT * FROM " + queryOrTable + " LIMIT 1";
            }
            return jdbcTemplate.query(sql, rs -> {
                List<Map<String, Object>> cols = new ArrayList<>();
                if (rs != null) {
                    java.sql.ResultSetMetaData rsmd = rs.getMetaData();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        Map<String, Object> col = new HashMap<>();
                        col.put("column_name", rsmd.getColumnLabel(i));
                        String typeName = rsmd.getColumnTypeName(i) != null ? rsmd.getColumnTypeName(i).toLowerCase()
                                : "text";
                        if (typeName.contains("int") || typeName.contains("serial") || typeName.contains("numeric")
                                || typeName.contains("decimal") || typeName.contains("float")
                                || typeName.contains("double")) {
                            col.put("data_type", "numeric");
                        } else if (typeName.contains("date") || typeName.contains("time")
                                || typeName.contains("timestamp")) {
                            col.put("data_type", "date");
                        } else if (typeName.contains("bool")) {
                            col.put("data_type", "boolean");
                        } else {
                            col.put("data_type", "text");
                        }
                        col.put("is_nullable",
                                rsmd.isNullable(i) == java.sql.ResultSetMetaData.columnNoNulls ? "NO" : "YES");
                        col.put("column_default", null);
                        cols.add(col);
                    }
                }
                return cols;
            });
        } catch (Exception e) {
            log.error("Gagal mengambil schema details untuk query/table: {}", queryOrTable, e);
            return new ArrayList<>();
        }
    }
}
