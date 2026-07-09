package com.vaadinerp.service;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.meta.LovMetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

@Service
public class DynamicDataService {

    private static final Logger log = LoggerFactory.getLogger(DynamicDataService.class);

    private final JdbcTemplate jdbcTemplate;
    private final LovMetaRepository lovMetaRepository;
    private final FormMetaRepository formMetaRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.vaadinerp.security.repository.AppUserGridPreferenceRepository userGridPreferenceRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.vaadinerp.security.service.SessionSecurityService securityService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.vaadinerp.meta.FormActionMetaRepository formActionMetaRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private FileStorageService fileStorageService;

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ScriptExecutorService scriptExecutorService;

    public ScriptExecutorService getScriptExecutorService() {
        return scriptExecutorService;
    }

    public DynamicDataService(JdbcTemplate jdbcTemplate, LovMetaRepository lovMetaRepository, FormMetaRepository formMetaRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.lovMetaRepository = lovMetaRepository;
        this.formMetaRepository = formMetaRepository;
    }

    public FormMetaRepository getFormMetaRepository() {
        return formMetaRepository;
    }

    public com.vaadinerp.meta.FormActionMetaRepository getFormActionMetaRepository() {
        return formActionMetaRepository;
    }

    public static void validateSqlIdentifier(String identifier, String type) {
        if (identifier == null || identifier.trim().isEmpty()) return;
        String clean = identifier.trim();
        if (!clean.matches("^[a-zA-Z0-9_.*]+$")) {
            throw new IllegalArgumentException("Invalid SQL identifier (" + type + "): " + identifier);
        }
    }

    /**
     * Validasi keamanan query SQL read-only dinamis untuk mencegah SQL Injection, stacked queries, dan DoS.
     */
    public static String validateAndSanitizeSelectQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("Query SQL tidak boleh kosong.");
        }
        String trimmed = sql.trim();
        String lower = trimmed.toLowerCase();

        // 1. Wajib berawal dari SELECT atau WITH (CTE)
        if (!lower.startsWith("select ") && !lower.startsWith("with ")) {
            throw new SecurityException("Akses ditolak: Hanya perintah SELECT read-only yang diizinkan!");
        }

        // 2. Tolak multi-statement / stacked query (semicolon di pertengahan atau akhir)
        while (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        if (trimmed.contains(";")) {
            throw new SecurityException("Akses ditolak: Multi-statement / stacked query tidak diizinkan!");
        }

        // 3. Tolak SQL comment injection (-- dan /* */)
        if (trimmed.contains("--") || trimmed.contains("/*")) {
            throw new SecurityException("Akses ditolak: Komentar SQL (-- atau /* */) tidak diizinkan dalam query dinamis!");
        }

        // 4. Blacklist kata kunci DML/DDL dan tabel sistem internal
        String[] prohibitedTokens = {
            "insert ", "update ", "delete ", "drop ", "alter ", "truncate ", "grant ", "revoke ",
            "create ", "replace ", "execute ", "exec ", "pg_sleep", "pg_terminate_backend",
            "pg_reload_conf", "pg_shadow", "pg_authid", "app_user", "role_menu_permission",
            "copy ", "lo_export", "lo_import", "pg_read_file", "pg_read_binary_file",
            "pg_ls_dir", "set role", "set session"
        };
        String padded = " " + lower.replaceAll("[\\(\\)\\[\\]\\{\\},\\+\\-\\*/=]", " ") + " ";
        for (String token : prohibitedTokens) {
            if (padded.contains(" " + token.trim() + " ") || lower.contains(token.trim() + "(")) {
                throw new SecurityException("Akses ditolak: Perintah SQL mengandung kata kunci atau tabel terlarang (" + token.trim() + ")!");
            }
        }
        return trimmed;
    }

    /**
     * Validasi keamanan body PL/pgSQL trigger untuk mencegah injeksi kode berbahaya.
     * Trigger body hanya boleh berisi logika bisnis PL/pgSQL yang aman.
     */
    public static void validateTriggerBody(String body) {
        if (body == null || body.trim().isEmpty()) return;
        String lower = body.toLowerCase();

        // Tolak multi-statement escape via dollar-quote termination
        if (lower.contains("$$ ") || lower.contains("$$;") || lower.contains("$body$")
                || lower.contains("$func$") || lower.contains("$tag$")) {
            throw new SecurityException("Akses ditolak: Trigger body mengandung dollar-quote escape yang tidak diizinkan!");
        }

        // Blacklist perintah DDL/sistem berbahaya dalam trigger body
        String[] prohibited = {
            "drop table", "drop schema", "drop database", "drop function", "drop trigger",
            "drop role", "drop user", "drop owned",
            "alter table", "alter schema", "alter database", "alter role", "alter user",
            "truncate ", "grant ", "revoke ",
            "create role", "create user", "create database",
            "pg_sleep", "pg_terminate_backend", "pg_cancel_backend",
            "pg_reload_conf", "pg_shadow", "pg_authid",
            "pg_read_file", "pg_read_binary_file", "pg_ls_dir",
            "lo_export", "lo_import",
            "copy ", "set role", "set session",
            "execute format", "execute '", "execute \"",
            "dblink", "pg_extension"
        };

        // Normalisasi: hapus extra whitespace untuk deteksi yang lebih ketat
        String normalized = lower.replaceAll("\\s+", " ").trim();

        for (String token : prohibited) {
            if (normalized.contains(token)) {
                throw new SecurityException("Akses ditolak: Trigger body mengandung perintah terlarang (" + token.trim() + ")!");
            }
        }
    }

    /**
     * Validasi bahwa comparison operator hanya berisi operator SQL yang aman (whitelist).
     */
    private static final java.util.Set<String> ALLOWED_COMPARISON_OPS = java.util.Set.of(
        "=", "!=", "<>", "<", ">", "<=", ">=", "LIKE", "ILIKE", "NOT LIKE", "NOT ILIKE", "IS NULL", "IS NOT NULL"
    );

    public static String validateComparisonOperator(String op) {
        if (op == null || op.trim().isEmpty()) return "=";
        String upper = op.trim().toUpperCase();
        if (!ALLOWED_COMPARISON_OPS.contains(upper)) {
            throw new SecurityException("Operator perbandingan tidak diizinkan: " + op);
        }
        return upper;
    }

    /**
     * Validasi bahwa logical operator hanya AND atau OR.
     */
    public static String validateLogicalOperator(String op) {
        if (op == null || op.trim().isEmpty()) return "AND";
        String upper = op.trim().toUpperCase();
        if (!"AND".equals(upper) && !"OR".equals(upper)) {
            throw new SecurityException("Operator logika tidak diizinkan: " + op);
        }
        return upper;
    }

    /**
     * Validasi bahwa trigger timing hanya BEFORE atau AFTER.
     */
    public static String validateTriggerTiming(String timing) {
        if (timing == null || timing.trim().isEmpty()) {
            throw new IllegalArgumentException("Trigger timing tidak boleh kosong!");
        }
        String upper = timing.trim().toUpperCase();
        if (!"BEFORE".equals(upper) && !"AFTER".equals(upper) && !"INSTEAD OF".equals(upper)) {
            throw new SecurityException("Trigger timing tidak diizinkan: " + timing);
        }
        return upper;
    }

    /**
     * Validasi bahwa trigger event hanya INSERT, UPDATE, atau DELETE.
     */
    public static void validateTriggerEvents(List<String> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Trigger events tidak boleh kosong!");
        }
        java.util.Set<String> allowed = java.util.Set.of("INSERT", "UPDATE", "DELETE");
        for (String event : events) {
            if (event == null || !allowed.contains(event.trim().toUpperCase())) {
                throw new SecurityException("Trigger event tidak diizinkan: " + event);
            }
        }
    }

    public String getQualifiedTableName(String tableName) {
        if (tableName == null) return null;
        String clean = tableName.trim();
        validateSqlIdentifier(clean, "table name");
        if ("global_master".equalsIgnoreCase(clean)) return "dynamic.global_category";
        if (clean.contains(".")) return clean;
        return "dynamic." + clean;
    }

    public String getLovQualifiedTableName(String tableName) {
        if (tableName == null) return null;
        String clean = tableName.trim();
        if ("global_master".equalsIgnoreCase(clean)) return "dynamic.global_category";
        if (clean.toLowerCase().startsWith("select")) return validateAndSanitizeSelectQuery(clean);
        validateSqlIdentifier(clean, "lov table name");
        if (clean.contains(".")) return clean;
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'dynamic' AND table_name = ?",
                Integer.class,
                clean.toLowerCase()
            );
            if (count != null && count > 0) {
                return "dynamic." + clean;
            }
        } catch (Exception ex) {
            log.debug("Schema lookup fallback untuk tabel '{}': {}", clean, ex.getMessage());
        }
        return clean;
    }

    public List<String> fetchDynamicTables() {
        try {
            return jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'dynamic' ORDER BY table_name",
                String.class
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
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
                rawName.toLowerCase()
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<String> getColumnsForQueryOrTable(String tableNameOrQuery) {
        if (tableNameOrQuery == null || tableNameOrQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String trimmed = tableNameOrQuery.trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            try {
                String safeQuery = validateAndSanitizeSelectQuery(trimmed);
                String sql = "SELECT * FROM ( " + safeQuery + " ) AS subquery LIMIT 1";
                return jdbcTemplate.query(sql, rs -> {
                    List<String> cols = new ArrayList<>();
                    if (rs != null) {
                        java.sql.ResultSetMetaData rsmd = rs.getMetaData();
                        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                            cols.add(rsmd.getColumnLabel(i));
                        }
                    }
                    return cols;
                });
            } catch (Exception e) {
                return new ArrayList<>();
            }
        } else {
            return fetchTableColumns(trimmed);
        }
    }

    public List<Map<String, Object>> fetchTableSchemaDetails(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String trimmed = tableName.trim();
        if (trimmed.toLowerCase().startsWith("select") || trimmed.contains(" ")) {
            return fetchSchemaDetailsForQuery(trimmed);
        }
        try {
            String rawName = trimmed.contains(".") ? trimmed.substring(trimmed.indexOf(".") + 1) : trimmed;
            List<Map<String, Object>> res = jdbcTemplate.queryForList(
                "SELECT column_name, data_type, is_nullable, column_default " +
                "FROM information_schema.columns " +
                "WHERE table_schema IN ('dynamic', 'public') AND table_name = ? " +
                "ORDER BY ordinal_position",
                rawName.toLowerCase()
            );
            if (!res.isEmpty()) {
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
            if (queryOrTable.toLowerCase().startsWith("select")) {
                String safeQuery = validateAndSanitizeSelectQuery(queryOrTable);
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
                        String typeName = rsmd.getColumnTypeName(i) != null ? rsmd.getColumnTypeName(i).toLowerCase() : "text";
                        if (typeName.contains("int") || typeName.contains("serial") || typeName.contains("numeric") || typeName.contains("decimal") || typeName.contains("float") || typeName.contains("double")) {
                            col.put("data_type", "numeric");
                        } else if (typeName.contains("date") || typeName.contains("time") || typeName.contains("timestamp")) {
                            col.put("data_type", "date");
                        } else if (typeName.contains("bool")) {
                            col.put("data_type", "boolean");
                        } else {
                            col.put("data_type", "text");
                        }
                        col.put("is_nullable", rsmd.isNullable(i) == java.sql.ResultSetMetaData.columnNoNulls ? "NO" : "YES");
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

    public List<Map<String, Object>> fetchTableTriggers(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            String rawName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;
            return jdbcTemplate.queryForList(
                "SELECT trigger_name, action_timing, string_agg(event_manipulation, ', ') AS event_manipulation " +
                "FROM information_schema.triggers " +
                "WHERE event_object_schema = 'dynamic' AND event_object_table = ? " +
                "GROUP BY trigger_name, action_timing",
                rawName.toLowerCase()
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String fetchTriggerBody(String tableName, String triggerName) {
        if (tableName == null || triggerName == null) return "";
        String rawName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;
        String funcName = "fn_" + rawName.toLowerCase() + "_" + triggerName.toLowerCase();
        try {
            String sql = "SELECT routine_definition FROM information_schema.routines WHERE routine_schema = 'dynamic' AND routine_name = ?";
            String body = jdbcTemplate.queryForObject(sql, String.class, funcName);
            if (body != null && !body.trim().isEmpty()) {
                return body;
            }
        } catch (Exception ex) {
            log.debug("Gagal membaca trigger body via information_schema untuk '{}': {}", funcName, ex.getMessage());
        }

        try {
            String sqlProc = "SELECT pg_get_functiondef(p.oid) FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid WHERE n.nspname = 'dynamic' AND p.proname = ?";
            String def = jdbcTemplate.queryForObject(sqlProc, String.class, funcName);
            if (def != null) {
                int idx = def.indexOf("AS $");
                if (idx != -1) {
                    int startTokenEnd = def.indexOf("\n", idx);
                    int lastTokenStart = def.lastIndexOf("$");
                    if (lastTokenStart > 0 && startTokenEnd != -1) {
                        int prevDollar = def.lastIndexOf("$", lastTokenStart - 1);
                        if (prevDollar > startTokenEnd) {
                            return def.substring(startTokenEnd + 1, prevDollar).trim();
                        }
                    }
                }
                return def;
            }
        } catch (Exception ex) {
            log.debug("Gagal membaca trigger body via pg_proc untuk '{}': {}", funcName, ex.getMessage());
        }

        return "-- Tulis kode PL/pgSQL trigger di sini\nBEGIN\n    RETURN NEW;\nEND;";
    }

    @Transactional
    public void addOrUpdateTableTrigger(String tableName, TriggerDefinition trigger) {
        if (trigger == null || trigger.getTriggerName() == null || trigger.getTriggerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama trigger tidak boleh kosong");
        }

        // Keamanan: hanya SUPER_ADMIN yang boleh membuat/mengubah trigger
        if (!isCurrentUserSuperAdmin()) {
            throw new SecurityException("Akses ditolak: Hanya SUPER_ADMIN yang boleh membuat atau mengubah database trigger!");
        }

        String qTableName = getQualifiedTableName(tableName);
        String rawTableName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;
        String triggerName = trigger.getTriggerName().trim();

        // Validasi nama trigger sebagai SQL identifier yang aman
        validateSqlIdentifier(triggerName, "trigger name");
        validateSqlIdentifier(rawTableName, "table name");

        // Validasi trigger body terhadap kode berbahaya
        validateTriggerBody(trigger.getTriggerBody());

        String functionName = "fn_" + rawTableName + "_" + triggerName;

        StringBuilder funcSql = new StringBuilder();
        funcSql.append("CREATE OR REPLACE FUNCTION dynamic.").append(functionName).append("()\n");
        funcSql.append("RETURNS TRIGGER AS $$\n");
        funcSql.append(trigger.getTriggerBody() != null ? trigger.getTriggerBody() : "").append("\n");
        funcSql.append("$$ LANGUAGE plpgsql;");

        jdbcTemplate.execute(funcSql.toString());

        jdbcTemplate.execute("DROP TRIGGER IF EXISTS " + triggerName + " ON " + qTableName);

        if (trigger.getEvents() != null && !trigger.getEvents().isEmpty()) {
            // Validasi timing dan events sebagai whitelist
            String safeTiming = validateTriggerTiming(trigger.getTiming());
            validateTriggerEvents(trigger.getEvents());
            String eventStr = String.join(" OR ", trigger.getEvents());

            StringBuilder triggerSql = new StringBuilder();
            triggerSql.append("CREATE TRIGGER ").append(triggerName).append("\n");
            triggerSql.append(safeTiming).append(" ").append(eventStr).append("\n");
            triggerSql.append("ON ").append(qTableName).append("\n");
            triggerSql.append("FOR EACH ROW EXECUTE FUNCTION dynamic.").append(functionName).append("();");

            jdbcTemplate.execute(triggerSql.toString());
        }
    }

    @Transactional
    public void dropTableTrigger(String tableName, String triggerName) {
        // Keamanan: hanya SUPER_ADMIN yang boleh menghapus trigger
        if (!isCurrentUserSuperAdmin()) {
            throw new SecurityException("Akses ditolak: Hanya SUPER_ADMIN yang boleh menghapus database trigger!");
        }
        // Validasi nama trigger sebagai SQL identifier yang aman
        validateSqlIdentifier(triggerName, "trigger name");

        String qTableName = getQualifiedTableName(tableName);
        String rawTableName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;
        validateSqlIdentifier(rawTableName, "table name");

        jdbcTemplate.execute("DROP TRIGGER IF EXISTS " + triggerName + " ON " + qTableName);
        jdbcTemplate.execute("DROP FUNCTION IF EXISTS dynamic.fn_" + rawTableName + "_" + triggerName + " CASCADE");
    }

    public List<Map<String, Object>> fetchTableConstraints(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            String rawName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;
            
            // 1. Fetch Key Constraints (PK, FK, Unique)
            String keySql = "SELECT DISTINCT " +
                    "  tc.constraint_name, " +
                    "  tc.constraint_type, " +
                    "  kcu.column_name, " +
                    "  ccu.table_name AS foreign_table, " +
                    "  ccu.column_name AS foreign_column " +
                    "FROM " +
                    "  information_schema.table_constraints AS tc " +
                    "  JOIN information_schema.key_column_usage AS kcu " +
                    "    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema " +
                    "  LEFT JOIN information_schema.constraint_column_usage AS ccu " +
                    "    ON ccu.constraint_name = tc.constraint_name AND ccu.table_schema = tc.table_schema " +
                    "WHERE tc.table_schema = 'dynamic' AND tc.table_name = ?";
            List<Map<String, Object>> keys = jdbcTemplate.queryForList(keySql, rawName.toLowerCase());

            // 2. Fetch Check Constraints
            String checkSql = "SELECT " +
                    "  tc.constraint_name, " +
                    "  'CHECK' AS constraint_type, " +
                    "  cc.check_clause AS check_expression " +
                    "FROM " +
                    "  information_schema.table_constraints AS tc " +
                    "  JOIN information_schema.check_constraints AS cc " +
                    "    ON tc.constraint_name = cc.constraint_name " +
                    "WHERE tc.table_schema = 'dynamic' AND tc.table_name = ?";
            List<Map<String, Object>> checks = jdbcTemplate.queryForList(checkSql, rawName.toLowerCase());

            List<Map<String, Object>> allConstraints = new ArrayList<>();
            allConstraints.addAll(keys);
            
            for (Map<String, Object> check : checks) {
                Map<String, Object> row = new HashMap<>(check);
                row.put("column_name", "");
                row.put("foreign_table", "");
                row.put("foreign_column", "");
                allConstraints.add(row);
            }
            return allConstraints;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional
    public void addTableConstraint(String tableName, String constraintName, String type, String col, String refTable, String refCol, String expr) {
        validateSqlIdentifier(constraintName, "constraint name");
        validateSqlIdentifier(col, "column name");
        String qTableName = getQualifiedTableName(tableName);
        StringBuilder sql = new StringBuilder("ALTER TABLE ").append(qTableName).append(" ADD CONSTRAINT ").append(constraintName).append(" ");
        
        if ("FOREIGN KEY".equalsIgnoreCase(type)) {
            validateSqlIdentifier(refCol, "reference column name");
            String qRefTable = getQualifiedTableName(refTable);
            sql.append("FOREIGN KEY (").append(col).append(") REFERENCES ").append(qRefTable).append(" (").append(refCol).append(") ON DELETE CASCADE");
        } else if ("UNIQUE".equalsIgnoreCase(type)) {
            sql.append("UNIQUE (").append(col).append(")");
        } else if ("CHECK".equalsIgnoreCase(type)) {
            sql.append("CHECK (").append(expr).append(")");
        } else {
            throw new IllegalArgumentException("Unsupported constraint type: " + type);
        }
        
        jdbcTemplate.execute(sql.toString());
    }

    @Transactional
    public void dropTableConstraint(String tableName, String constraintName) {
        validateSqlIdentifier(constraintName, "constraint name");
        String qTableName = getQualifiedTableName(tableName);
        String sql = "ALTER TABLE " + qTableName + " DROP CONSTRAINT " + constraintName;
        jdbcTemplate.execute(sql);
    }

    @Transactional
    public void addTableColumn(String tableName, String columnName, String dataType, boolean nullable, String defaultVal) {
        validateSqlIdentifier(columnName, "column name");
        String qTable = getQualifiedTableName(tableName);
        StringBuilder sql = new StringBuilder("ALTER TABLE ").append(qTable)
                .append(" ADD COLUMN ").append(columnName).append(" ").append(dataType);
        if (!nullable) {
            sql.append(" NOT NULL");
        }
        if (defaultVal != null && !defaultVal.trim().isEmpty()) {
            sql.append(" DEFAULT ").append(defaultVal.trim());
        }
        jdbcTemplate.execute(sql.toString());
    }

    @Transactional
    public void alterTableColumn(String tableName, String oldColName, String newColName, String newType, boolean nullable, String defaultVal) {
        validateSqlIdentifier(oldColName, "old column name");
        validateSqlIdentifier(newColName, "new column name");
        String qTable = getQualifiedTableName(tableName);

        if (!oldColName.equalsIgnoreCase(newColName)) {
            jdbcTemplate.execute("ALTER TABLE " + qTable + " RENAME COLUMN " + oldColName + " TO " + newColName);
        }

        String targetCol = newColName;

        if (newType != null && !newType.trim().isEmpty()) {
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ALTER COLUMN " + targetCol + " TYPE " + newType.trim() + " USING " + targetCol + "::" + newType.trim());
        }

        if (nullable) {
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ALTER COLUMN " + targetCol + " DROP NOT NULL");
        } else {
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ALTER COLUMN " + targetCol + " SET NOT NULL");
        }

        if (defaultVal != null && !defaultVal.trim().isEmpty()) {
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ALTER COLUMN " + targetCol + " SET DEFAULT " + defaultVal.trim());
        } else {
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ALTER COLUMN " + targetCol + " DROP DEFAULT");
        }
    }

    @Transactional
    public void dropTableColumn(String tableName, String columnName) {
        validateSqlIdentifier(columnName, "column name");
        String qTable = getQualifiedTableName(tableName);
        jdbcTemplate.execute("ALTER TABLE " + qTable + " DROP COLUMN " + columnName + " CASCADE");
    }

    @Transactional
    public void updateTableConstraint(String tableName, String oldConstraintName, String newConstraintName, String type, String col, String refTable, String refCol, String expr) {
        dropTableConstraint(tableName, oldConstraintName);
        addTableConstraint(tableName, newConstraintName, type, col, refTable, refCol, expr);
    }

    public Optional<LovMeta> getLovMeta(String lovCode) {
        if (lovCode == null) return Optional.empty();
        Optional<FormMeta> formOpt = formMetaRepository.findById(lovCode);
        Optional<LovMeta> existing = lovMetaRepository.findById(lovCode);
        if (formOpt.isPresent()) {
            FormMeta form = formOpt.get();
            String historicalGridCols = buildHistoricalGridCols(form);
            if (existing.isPresent()) {
                LovMeta lov = existing.get();
                lov.setGridColumns(historicalGridCols);
                return Optional.of(lov);
            }
            LovMeta lov = new LovMeta();
            lov.setLovCode(form.getFormCode());
            lov.setLovName(form.getFormTitle());
            
            // Reuse viewTable (custom query) if configured, else target tableName
            String srcTable = (form.getViewTable() != null && !form.getViewTable().trim().isEmpty()) 
                              ? form.getViewTable() 
                              : form.getTableName();
            lov.setTableName(srcTable);
            
            // Value column is the primary key (default to "id")
            String pk = form.getPrimaryKey() != null ? form.getPrimaryKey() : "id";
            lov.setValueColumn(pk);
            
            // Determine label and search column dynamically from form fields
            String labelCol = pk;
            List<String> searchCols = new ArrayList<>();
            if (form.getFields() != null && !form.getFields().isEmpty()) {
                // Try to find a field containing 'name', 'title', 'desc', 'label'
                FieldMeta bestField = null;
                for (FieldMeta field : form.getFields()) {
                    if (field.isDetail()) continue;
                    String fname = field.getFieldName().toLowerCase();
                    if (fname.contains("name") || fname.contains("title") || fname.contains("desc") || fname.contains("label")) {
                        bestField = field;
                        break;
                    }
                }
                if (bestField == null) {
                    // Fallback to the first non-pk field
                    for (FieldMeta field : form.getFields()) {
                        if (field.isDetail()) continue;
                        if (!field.getFieldName().equalsIgnoreCase(pk)) {
                            bestField = field;
                            break;
                        }
                    }
                }
                if (bestField != null) {
                    labelCol = bestField.getFieldName();
                }
                
                // Collect all non-detail field names for searchable columns
                for (FieldMeta field : form.getFields()) {
                    if (field.isDetail()) continue;
                    searchCols.add(field.getFieldName());
                }
            }
            
            lov.setLabelColumn(labelCol);
            if (searchCols.isEmpty()) {
                lov.setSearchColumn(labelCol);
            } else {
                lov.setSearchColumn(String.join(",", searchCols));
            }
            lov.setGridColumns(historicalGridCols);
            return Optional.of(lov);
        }
        return existing;
    }

    private String buildHistoricalGridCols(FormMeta form) {
        List<FieldMeta> sortedFields = form.getFields() != null ? new ArrayList<>(form.getFields()) : new ArrayList<>();
        sortedFields = sortedFields.stream()
                .filter(f -> !f.isDetail() && f.isShowInGrid())
                .collect(java.util.stream.Collectors.toList());
        sortedFields.sort((f1, f2) -> {
            Integer o1 = f1.getColOrder() != null ? f1.getColOrder() : Integer.MAX_VALUE;
            Integer o2 = f2.getColOrder() != null ? f2.getColOrder() : Integer.MAX_VALUE;
            return o1.compareTo(o2);
        });

        StringBuilder gridCols = new StringBuilder();
        if (!sortedFields.isEmpty()) {
            for (int i = 0; i < sortedFields.size(); i++) {
                FieldMeta field = sortedFields.get(i);
                if (i > 0) gridCols.append(",");
                String fName = field.getFieldName();
                String label = (field.getFieldLabel() != null && !field.getFieldLabel().trim().isEmpty())
                               ? field.getFieldLabel().trim() : fName;
                gridCols.append(fName).append(":").append(label).append(":150px");
            }
        } else {
            String pk = form.getPrimaryKey() != null ? form.getPrimaryKey() : "id";
            gridCols.append(pk).append(":").append(pk.toUpperCase()).append(":100px");
            String labelCol = pk;
            if (form.getFields() != null) {
                for (FieldMeta field : form.getFields()) {
                    if (field.isDetail()) continue;
                    if (!field.getFieldName().equalsIgnoreCase(pk)) {
                        labelCol = field.getFieldName();
                        break;
                    }
                }
            }
            if (!labelCol.equalsIgnoreCase(pk)) {
                gridCols.append(",").append(labelCol).append(":").append(labelCol.toUpperCase()).append(":250px");
            }
        }
        return gridCols.toString();
    }

    @Transactional
    public void saveData(FormMeta formMeta, Map<String, Object> data) {
        saveData(formMeta, data, null, null);
    }

    private int executeAndLogSql(String sql, List<Object> args) {
        System.out.println("=================================================");
        System.out.println("EXECUTING SQL: " + sql);
        System.out.println("PARAMETERS: " + args);
        if (args != null) {
            List<String> types = args.stream().map(o -> o == null ? "NULL" : o.getClass().getSimpleName() + "(" + o + ")").toList();
            System.out.println("PARAM TYPES: " + types);
        }
        System.out.println("=================================================");
        try {
            int rowsAffected;
            if (args != null) {
                rowsAffected = jdbcTemplate.update(sql, args.toArray());
            } else {
                rowsAffected = jdbcTemplate.update(sql);
            }
            return rowsAffected;
        } catch (Exception e) {
            System.err.println(">>> SQL ERROR EXECUTING: " + sql);
            System.err.println(">>> WITH PARAMS: " + args);
            System.err.println(">>> ROOT CAUSE: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void saveData(FormMeta formMeta, Map<String, Object> rawData, String fkColumn, Object fkValue) {
        if (rawData == null || rawData.isEmpty()) {
            throw new IllegalArgumentException("Data kosong, tidak ada yang disimpan!");
        }
        Map<String, Object> data = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        data.putAll(rawData);

        generatePhysicalTable(formMeta);

        if (fkColumn != null) {
            String tableName = getQualifiedTableName(formMeta.getTableName());
            try {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN IF NOT EXISTS " + fkColumn + " INTEGER");
            } catch (Exception ex) {
                log.warn("Gagal menambah kolom FK '{}' ke tabel '{}': {}", fkColumn, tableName, ex.getMessage());
            }
        }

        String pk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";
        pk = resolveExistingColumn(formMeta.getTableName(), pk);
        validateSqlIdentifier(pk, "primary key");
        if (fkColumn != null) validateSqlIdentifier(fkColumn, "foreign key column");
        boolean isUpdate = data.containsKey(pk) && data.get(pk) != null && !data.get(pk).toString().trim().isEmpty()
                && !"0".equals(data.get(pk).toString().trim())
                && !data.get(pk).toString().trim().startsWith("[AUTO")
                && !data.get(pk).toString().trim().startsWith("⚡");

        ensureAuditColumnsExist(formMeta.getTableName());

        Object masterId = data.get(pk);

        if (formMeta.getTableName() != null && !formMeta.getTableName().trim().isEmpty()) {
            if (isUpdate) {
                masterId = data.get(pk);
            Map<String, Object> oldRecord = null;
            try {
                String selectSql = "SELECT * FROM " + getQualifiedTableName(formMeta.getTableName()) + " WHERE CAST(" + pk + " AS text) = ?";
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, masterId != null ? masterId.toString().trim() : "");
                if (!rows.isEmpty()) {
                    oldRecord = rows.get(0);
                }
            } catch (Exception ex) {
                log.warn("Gagal mengambil record lama untuk audit (tabel: {}, pk: {}): {}", formMeta.getTableName(), masterId, ex.getMessage());
            }

            // UPDATE: Jika kolom primary key ada nilainya
            StringJoiner setClause = new StringJoiner(", ");
            List<Object> args = new ArrayList<>();

            for (FieldMeta field : formMeta.getFields()) {
                String fieldName = field.getFieldName();
                validateSqlIdentifier(fieldName, "column name");
                if (fieldName.equalsIgnoreCase(pk)) continue;
                if (fieldName.equalsIgnoreCase("inputby") || fieldName.equalsIgnoreCase("inputdt") ||
                    fieldName.equalsIgnoreCase("updateby") || fieldName.equalsIgnoreCase("updatedt") ||
                    fieldName.equalsIgnoreCase("version")) continue;
                if (!field.isSaveOnUpdate()) continue;
                if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) continue; // Skip subform grid columns
                if (data.containsKey(fieldName)) {
                    setClause.add(fieldName + " = ?");
                    args.add(sanitizeJdbcValue(formMeta.getTableName(), fieldName, data.get(fieldName)));
                }
            }

            if (fkColumn != null && fkValue != null) {
                setClause.add(fkColumn + " = ?");
                args.add(sanitizeJdbcValue(formMeta.getTableName(), fkColumn, fkValue));
            }

            String currentUser = getCurrentLoggedUser();
            java.sql.Timestamp nowTs = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
            setClause.add("updateby = ?");
            args.add(currentUser);
            setClause.add("updatedt = ?");
            args.add(nowTs);
            setClause.add("version = COALESCE(version, 0) + 1");
            data.put("updateby", currentUser);
            data.put("updatedt", nowTs);

            if (!args.isEmpty()) {
                StringBuilder whereClause = new StringBuilder(" WHERE CAST(" + pk + " AS text) = ?");
                args.add(data.get(pk) != null ? data.get(pk).toString().trim() : "");

                Object oldVersion = data.get("version");
                if (oldVersion != null && !oldVersion.toString().trim().isEmpty()) {
                    try {
                        int vNum = Integer.parseInt(oldVersion.toString().trim());
                        whereClause.append(" AND COALESCE(version, 0) = ?");
                        args.add(vNum);
                    } catch (NumberFormatException ex) {
                        log.trace("Version bukan angka valid, skip optimistic lock check: {}", oldVersion);
                    }
                }

                String sql = "UPDATE " + getQualifiedTableName(formMeta.getTableName()) + " SET " + setClause.toString() + whereClause.toString();
                int updatedRows = executeAndLogSql(sql, args);
                if (updatedRows == 0) {
                    throw new org.springframework.dao.OptimisticLockingFailureException("Konflik Data: Data ini telah diubah oleh pengguna lain beberapa saat lalu. Silakan muat ulang (refresh) data terbaru sebelum melakukan perubahan.");
                }

                if (oldVersion != null) {
                    try {
                        data.put("version", Integer.parseInt(oldVersion.toString().trim()) + 1);
                    } catch (Exception ex) {
                        log.trace("Gagal increment version, reset ke 1: {}", ex.getMessage());
                        data.put("version", 1);
                    }
                } else {
                    data.put("version", 1);
                }
            }

            if (oldRecord != null) {
                for (FieldMeta field : formMeta.getFields()) {
                    if (field.isAuditLog()) {
                        String fieldName = field.getFieldName();
                        Object oldVal = getCaseInsensitiveValue(oldRecord, fieldName);
                        Object newVal = data.get(fieldName);
                        recordFieldAuditLog(formMeta.getFormCode(), formMeta.getTableName(), masterId, "UPDATE", fieldName, oldVal, newVal, currentUser);
                    }
                }
            }
        } else {
            // INSERT: Jika primary key tidak ada nilainya
            if (formMeta.getFields() != null) {
                for (FieldMeta field : formMeta.getFields()) {
                    if (field.getSequenceCode() != null && !field.getSequenceCode().trim().isEmpty()) {
                        String fieldName = field.getFieldName();
                        Object val = data.get(fieldName);
                        if (val == null || val.toString().trim().isEmpty() || val.toString().startsWith("[AUTO") || val.toString().startsWith("⚡")) {
                            String genNum = generateNextSequence(field.getSequenceCode());
                            data.put(fieldName, genNum);
                            log.info("Auto-generated sequence '{}' for field '{}' -> {}", field.getSequenceCode(), fieldName, genNum);
                        }
                    }
                }
            }

            StringJoiner columns = new StringJoiner(", ");
            StringJoiner valuesParam = new StringJoiner(", ");
            List<Object> args = new ArrayList<>();

            for (FieldMeta field : formMeta.getFields()) {
                String fieldName = field.getFieldName();
                validateSqlIdentifier(fieldName, "column name");
                if (fieldName.equalsIgnoreCase(pk)) {
                    Object pkVal = data.get(fieldName);
                    if ((field.getSequenceCode() == null || field.getSequenceCode().trim().isEmpty()) &&
                        (pkVal == null || pkVal.toString().trim().isEmpty() || "0".equals(pkVal.toString().trim()) || pkVal.toString().trim().startsWith("[AUTO") || pkVal.toString().trim().startsWith("⚡"))) {
                        continue;
                    }
                }
                if (fieldName.equalsIgnoreCase("inputby") || fieldName.equalsIgnoreCase("inputdt") ||
                    fieldName.equalsIgnoreCase("updateby") || fieldName.equalsIgnoreCase("updatedt")) continue;
                if (!field.isSaveOnInsert()) continue;
                if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) continue; // Skip subform grid columns
                if (data.containsKey(fieldName)) {
                    columns.add(fieldName);
                    valuesParam.add("?");
                    args.add(sanitizeJdbcValue(formMeta.getTableName(), fieldName, data.get(fieldName)));
                }
            }

            if (fkColumn != null && fkValue != null) {
                columns.add(fkColumn);
                valuesParam.add("?");
                args.add(sanitizeJdbcValue(formMeta.getTableName(), fkColumn, fkValue));
            }

            String currentUser = getCurrentLoggedUser();
            java.sql.Timestamp nowTs = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
            columns.add("inputby");
            valuesParam.add("?");
            args.add(currentUser);
            columns.add("inputdt");
            valuesParam.add("?");
            args.add(nowTs);
            columns.add("updateby");
            valuesParam.add("?");
            args.add(currentUser);
            columns.add("updatedt");
            valuesParam.add("?");
            args.add(nowTs);
            columns.add("version");
            valuesParam.add("?");
            args.add(1);

            String sql = "INSERT INTO " + getQualifiedTableName(formMeta.getTableName()) + " (" + columns.toString() + ") VALUES (" + valuesParam.toString() + ")";
            executeAndLogSql(sql, args);

            if (formMeta.getFields() != null) {
                for (FieldMeta field : formMeta.getFields()) {
                    if (field.isAuditLog()) {
                        String fieldName = field.getFieldName();
                        Object newVal = data.get(fieldName);
                        recordFieldAuditLog(formMeta.getFormCode(), formMeta.getTableName(), masterId, "INSERT", fieldName, null, newVal, currentUser);
                    }
                }
            }
        }
        } else {
            log.debug("Target table untuk form '{}' kosong/null, melewati penyimpanan tabel utama dan langsung memproses subform/detail.", formMeta.getFormCode());
        }

        // Simpan data Subform Grid
        if (formMeta.getFields() != null) {
            for (FieldMeta field : formMeta.getFields()) {
                if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) {
                    String subformFieldName = field.getFieldName();
                    String childFormCode = field.getLovCode();
                    if (childFormCode == null || childFormCode.trim().isEmpty()) continue;
                    
                    FormMeta childFormMeta = formMetaRepository.findById(childFormCode).orElse(null);
                    if (childFormMeta == null) continue;

                    // 1. Delete removed rows
                    if (data.containsKey(subformFieldName + "_deleted") && data.get(subformFieldName + "_deleted") instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> deletedRows = (List<Map<String, Object>>) data.get(subformFieldName + "_deleted");
                        for (Map<String, Object> delRow : deletedRows) {
                            deleteData(childFormMeta, delRow);
                        }
                    }
                    
                    // 2. Save active rows
                    if (data.containsKey(subformFieldName) && data.get(subformFieldName) instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> activeRows = (List<Map<String, Object>>) data.get(subformFieldName);
                        String childFkField = field.getFormula(); // child FK column
                        for (Map<String, Object> activeRow : activeRows) {
                            saveData(childFormMeta, activeRow, childFkField, masterId);
                        }
                    }
                }
            }
        }
    }

    @Transactional
    public String generateNextSequence(String seqCode) {
        if (seqCode == null || seqCode.trim().isEmpty()) return "";
        String cleanCode = seqCode.trim();
        String selectSql = "SELECT * FROM dynamic.md_sequence WHERE seq_code = ? FOR UPDATE";
        List<Map<String, Object>> list;
        try {
            list = jdbcTemplate.queryForList(selectSql, cleanCode);
        } catch (Exception e) {
            log.warn("Gagal query FOR UPDATE pada md_sequence untuk code '{}': {}", cleanCode, e.getMessage());
            list = jdbcTemplate.queryForList("SELECT * FROM dynamic.md_sequence WHERE seq_code = ?", cleanCode);
        }
        if (list.isEmpty()) {
            list = jdbcTemplate.queryForList("SELECT * FROM dynamic.md_sequence WHERE LOWER(seq_code) = LOWER(?)", cleanCode);
            if (list.isEmpty()) {
                log.warn("Sequence code '{}' tidak ditemukan di tabel dynamic.md_sequence!", cleanCode);
                return cleanCode + "-" + (System.currentTimeMillis() % 100000);
            }
        }
        Map<String, Object> seq = list.get(0);
        String prefixFormat = seq.get("prefix_format") != null ? seq.get("prefix_format").toString() : "";
        long currentVal = 0;
        if (seq.get("current_val") != null) {
            currentVal = ((Number) seq.get("current_val")).longValue();
        }
        int paddingLen = 4;
        if (seq.get("padding_len") != null) {
            paddingLen = ((Number) seq.get("padding_len")).intValue();
        }
        String resetPeriod = seq.get("reset_period") != null ? seq.get("reset_period").toString() : "NEVER";
        java.sql.Date lastResetDate = null;
        if (seq.get("last_reset_date") instanceof java.sql.Date d) {
            lastResetDate = d;
        } else if (seq.get("last_reset_date") instanceof java.util.Date ud) {
            lastResetDate = new java.sql.Date(ud.getTime());
        }

        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate lastDate = lastResetDate != null ? lastResetDate.toLocalDate() : now;
        boolean needReset = false;

        if ("MONTHLY".equalsIgnoreCase(resetPeriod) && (now.getYear() != lastDate.getYear() || now.getMonthValue() != lastDate.getMonthValue())) {
            needReset = true;
        } else if ("YEARLY".equalsIgnoreCase(resetPeriod) && now.getYear() != lastDate.getYear()) {
            needReset = true;
        } else if ("DAILY".equalsIgnoreCase(resetPeriod) && !now.equals(lastDate)) {
            needReset = true;
        }

        long nextVal = needReset ? 1 : (currentVal + 1);

        try {
            jdbcTemplate.update("UPDATE dynamic.md_sequence SET current_val = ?, last_reset_date = ? WHERE seq_code = ?",
                    nextVal, java.sql.Date.valueOf(now), seq.get("seq_code"));
        } catch (Exception ex) {
            log.warn("Gagal update md_sequence: {}", ex.getMessage());
        }

        String formattedPrefix = prefixFormat
                .replace("{YYYY}", String.format("%04d", now.getYear()))
                .replace("{YY}", String.format("%02d", now.getYear() % 100))
                .replace("{MM}", String.format("%02d", now.getMonthValue()))
                .replace("{DD}", String.format("%02d", now.getDayOfMonth()));

        String numberPart = (paddingLen > 0) ? String.format("%0" + paddingLen + "d", nextVal) : String.valueOf(nextVal);
        return formattedPrefix + numberPart;
    }

    public List<String> getActiveSequenceCodes() {
        try {
            return jdbcTemplate.queryForList("SELECT seq_code FROM dynamic.md_sequence WHERE status = 'Active'", String.class);
        } catch (Exception e) {
            return List.of("PO_NO", "BOM_CODE", "INV_NO");
        }
    }

    private final Map<String, Map<String, String>> tableColumnTypeCache = new java.util.concurrent.ConcurrentHashMap<>();

    private String getColumnDataType(String tableName, String columnName) {
        if (tableName == null || columnName == null) return "";
        String rawName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1).toLowerCase() : tableName.toLowerCase();
        Map<String, String> colTypes = tableColumnTypeCache.computeIfAbsent(rawName, t -> {
            Map<String, String> map = new java.util.HashMap<>();
            try {
                List<Map<String, Object>> cols = fetchTableSchemaDetails(t);
                for (Map<String, Object> col : cols) {
                    Object cName = col.get("column_name");
                    Object dType = col.get("data_type");
                    if (cName != null && dType != null) {
                        map.put(cName.toString().toLowerCase(), dType.toString().toLowerCase());
                    }
                }
            } catch (Exception ex) {
                log.warn("Gagal mengambil schema kolom untuk tabel '{}': {}", t, ex.getMessage());
            }
            return map;
        });
        return colTypes.getOrDefault(columnName.toLowerCase(), "");
    }

    private Object sanitizeJdbcValue(String tableName, String columnName, Object val) {
        if (val == null) return null;
        if (val instanceof java.util.Collection<?> col) {
            val = col.stream().map(Object::toString).collect(java.util.stream.Collectors.joining(", "));
        } else if (val.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(val);
            java.util.StringJoiner sj = new java.util.StringJoiner(", ");
            for (int i = 0; i < len; i++) {
                Object item = java.lang.reflect.Array.get(val, i);
                if (item != null) sj.add(item.toString());
            }
            val = sj.toString();
        }

        if (val instanceof Boolean bVal && tableName != null && columnName != null) {
            String colType = getColumnDataType(tableName, columnName);
            if ("character varying".equals(colType) || "varchar".equals(colType) || "text".equals(colType) || "char".equals(colType)) {
                return bVal ? "Active" : "Non-Active";
            } else if ("integer".equals(colType) || "smallint".equals(colType)) {
                return bVal ? 1 : 0;
            }
        }

        if (val instanceof String strVal && tableName != null && columnName != null) {
            String trimmedStr = strVal.trim();
            if (!trimmedStr.isEmpty()) {
                String colType = getColumnDataType(tableName, columnName);
                if ("integer".equals(colType) || "smallint".equals(colType) || "serial".equals(colType)) {
                    try { return Integer.valueOf(trimmedStr); } catch (Exception ex) { log.trace("Konversi ke Integer gagal untuk '{}': {}", trimmedStr, ex.getMessage()); }
                } else if ("bigint".equals(colType) || "bigserial".equals(colType)) {
                    try { return Long.valueOf(trimmedStr); } catch (Exception ex) { log.trace("Konversi ke Long gagal untuk '{}': {}", trimmedStr, ex.getMessage()); }
                } else if ("numeric".equals(colType) || "decimal".equals(colType) || "real".equals(colType) || "double precision".equals(colType)) {
                    try { return new java.math.BigDecimal(trimmedStr); } catch (Exception ex) { log.trace("Konversi ke BigDecimal gagal untuk '{}': {}", trimmedStr, ex.getMessage()); }
                }
            }
        }
        return val;
    }

    @Transactional
    public void deleteData(FormMeta formMeta, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        String pk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";
        pk = resolveExistingColumn(formMeta.getTableName(), pk);
        if (data.containsKey(pk) && data.get(pk) != null && !data.get(pk).toString().trim().isEmpty()) {
            if ("MASTER_DETAIL".equalsIgnoreCase(formMeta.getFormType())) {
                String detailTable = formMeta.getDetailTableName();
                String detailFk = formMeta.getDetailForeignKey();
                if (detailTable != null && !detailTable.trim().isEmpty() && detailFk != null && !detailFk.trim().isEmpty()) {
                    try {
                        String sqlDtl = "DELETE FROM " + getQualifiedTableName(detailTable) + " WHERE CAST(" + detailFk + " AS text) = ?";
                        jdbcTemplate.update(sqlDtl, data.get(pk) != null ? data.get(pk).toString().trim() : "");
                    } catch (Exception ex) {
                        log.warn("Gagal cascade delete detail (tabel: {}, fk: {}, pk: {}): {}", detailTable, detailFk, data.get(pk), ex.getMessage());
                    }
                }
            }
            
            // Cascade delete for modular subforms
            for (FieldMeta field : formMeta.getFields()) {
                if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) {
                    FormMeta childForm = formMetaRepository.findById(field.getLovCode()).orElse(null);
                    if (childForm != null) {
                        String childTable = childForm.getTableName();
                        String childFk = field.getFormula();
                        if (childTable != null && !childTable.trim().isEmpty() && childFk != null && !childFk.trim().isEmpty()) {
                            try {
                                String sqlDtl = "DELETE FROM " + getQualifiedTableName(childTable) + " WHERE CAST(" + childFk + " AS text) = ?";
                                jdbcTemplate.update(sqlDtl, data.get(pk) != null ? data.get(pk).toString().trim() : "");
                            } catch (Exception ex) {
                                log.warn("Gagal cascade delete subform (tabel: {}, fk: {}, pk: {}): {}", childTable, childFk, data.get(pk), ex.getMessage());
                            }
                        }
                    }
                }
            }

            if (fileStorageService != null && formMeta.getFields() != null) {
                for (FieldMeta field : formMeta.getFields()) {
                    if ("FILE_UPLOAD".equalsIgnoreCase(field.getComponentType()) || "IMAGE_UPLOAD".equalsIgnoreCase(field.getComponentType())) {
                        Object val = data.get(field.getFieldName());
                        if (val != null && !val.toString().trim().isEmpty()) {
                            fileStorageService.deleteFilesByDelimitedString(val.toString());
                        }
                    }
                }
            }

            String sql = "DELETE FROM " + getQualifiedTableName(formMeta.getTableName()) + " WHERE CAST(" + pk + " AS text) = ?";
            jdbcTemplate.update(sql, data.get(pk) != null ? data.get(pk).toString().trim() : "");
        }
    }

    public void generatePhysicalTable(FormMeta formMeta) {
        if (formMeta == null || formMeta.getTableName() == null || formMeta.getTableName().trim().isEmpty()) {
            return;
        }
        if ("MASTER_DETAIL".equalsIgnoreCase(formMeta.getFormType())) {
            ensureMasterDetailTablesExist(formMeta);
        } else {
            ensureTableExists(formMeta);
        }
    }

    private void ensureTableExists(FormMeta formMeta) {
        if (formMeta == null || formMeta.getTableName() == null || formMeta.getTableName().trim().isEmpty()) {
            return;
        }
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS dynamic;");
        String tableName = getQualifiedTableName(formMeta.getTableName());
        String pk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";
        
        // 1. Buat tabel minimal jika belum ada
        boolean isTextPk = !pk.equalsIgnoreCase("id");
        if (formMeta.getFields() != null) {
            for (FieldMeta fm : formMeta.getFields()) {
                if (fm.getFieldName().equalsIgnoreCase(pk)) {
                    if (fm.getSequenceCode() != null || "TEXTBOX".equalsIgnoreCase(fm.getComponentType()) || "VARCHAR".equalsIgnoreCase(fm.getComponentType()) || "STRING".equalsIgnoreCase(fm.getComponentType()) || "TEXTFIELD".equalsIgnoreCase(fm.getComponentType())) {
                        isTextPk = true;
                    }
                    break;
                }
            }
        }
        StringBuilder createSql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        createSql.append(tableName).append(" (");
        createSql.append(pk).append(isTextPk ? " VARCHAR(100) PRIMARY KEY" : " SERIAL PRIMARY KEY");
        createSql.append(")");
        jdbcTemplate.execute(createSql.toString());

        // 2. Tambahkan kolom satu per satu jika belum ada (ALTER TABLE ADD COLUMN IF NOT EXISTS)
        for (FieldMeta field : formMeta.getFields()) {
            if (field.isDetail() && "MASTER_DETAIL".equalsIgnoreCase(formMeta.getFormType())) continue; // Skip detail columns for master table
            if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) continue; // Skip subform grid columns
            if (!field.isSaveOnInsert() && !field.isSaveOnUpdate()) continue; // Skip transient/custom view fields
            String colName = field.getFieldName();
            if (colName.equalsIgnoreCase(pk)) continue;

            String typeDef;
            if ("DECIMALBOX".equalsIgnoreCase(field.getComponentType())) {
                typeDef = "DECIMAL(19, 2)";
            } else if ("DATEBOX".equalsIgnoreCase(field.getComponentType())) {
                typeDef = "DATE";
            } else if ("DATETIMEBOX".equalsIgnoreCase(field.getComponentType())) {
                typeDef = "TIMESTAMP";
            } else if ("TIMEBOX".equalsIgnoreCase(field.getComponentType())) {
                typeDef = "TIME";
            } else if ("FILE_UPLOAD".equalsIgnoreCase(field.getComponentType()) || "IMAGE_UPLOAD".equalsIgnoreCase(field.getComponentType()) || "TEXTAREA".equalsIgnoreCase(field.getComponentType())) {
                typeDef = "TEXT";
            } else {
                typeDef = "VARCHAR(255)";
            }

            String alterSql = "ALTER TABLE " + tableName + " ADD COLUMN IF NOT EXISTS " + colName + " " + typeDef;
            try {
                jdbcTemplate.execute(alterSql);
            } catch (Exception e) {
                // Log atau abaikan jika dialek database tidak mendukung
            }
        }
        ensureAuditColumnsExist(formMeta.getTableName());
    }

    public void ensureAuditColumnsExist(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) return;
        String qTable = getQualifiedTableName(tableName);
        try {
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ADD COLUMN IF NOT EXISTS inputby VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ADD COLUMN IF NOT EXISTS inputdt TIMESTAMP");
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ADD COLUMN IF NOT EXISTS updateby VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ADD COLUMN IF NOT EXISTS updatedt TIMESTAMP");
            jdbcTemplate.execute("ALTER TABLE " + qTable + " ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0");
        } catch (Exception ex) {
            log.warn("Gagal menambahkan kolom audit ke tabel '{}': {}", qTable, ex.getMessage());
        }
    }

    private String getCurrentLoggedUser() {
        try {
            if (com.vaadin.flow.server.VaadinSession.getCurrent() != null) {
                Object obj = com.vaadin.flow.server.VaadinSession.getCurrent().getAttribute(com.vaadinerp.security.service.SessionSecurityService.SESSION_USER_KEY);
                if (obj instanceof com.vaadinerp.security.entity.AppUser) {
                    com.vaadinerp.security.entity.AppUser user = (com.vaadinerp.security.entity.AppUser) obj;
                    if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                        return user.getUsername().trim();
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("Tidak dapat mengambil user dari VaadinSession (mungkin konteks background): {}", ex.getMessage());
        }
        return "SYSTEM";
    }

    public boolean isCurrentUserSuperAdmin() {
        if (securityService != null && securityService.isSuperAdmin()) {
            return true;
        }
        try {
            if (com.vaadin.flow.server.VaadinSession.getCurrent() != null) {
                Object obj = com.vaadin.flow.server.VaadinSession.getCurrent().getAttribute(com.vaadinerp.security.service.SessionSecurityService.SESSION_USER_KEY);
                if (obj instanceof com.vaadinerp.security.entity.AppUser) {
                    com.vaadinerp.security.entity.AppUser user = (com.vaadinerp.security.entity.AppUser) obj;
                    return "SUPER_ADMIN".equalsIgnoreCase(user.getRoleCode());
                }
            }
        } catch (Exception ex) {
            log.debug("Tidak dapat cek role SUPER_ADMIN dari VaadinSession: {}", ex.getMessage());
        }
        return false;
    }

    public void ensureFieldAuditTableExists() {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_field_audit (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "form_code VARCHAR(50), " +
                "table_name VARCHAR(100), " +
                "record_id VARCHAR(100), " +
                "action_type VARCHAR(20), " +
                "field_name VARCHAR(100), " +
                "old_value TEXT, " +
                "new_value TEXT, " +
                "action_by VARCHAR(100), " +
                "action_dt TIMESTAMP)");
        } catch (Exception ex) {
            log.warn("Gagal membuat tabel sys_field_audit: {}", ex.getMessage());
        }
    }

    public void recordFieldAuditLog(String formCode, String tableName, Object recordId, String actionType, String fieldName, Object oldValue, Object newValue, String actionBy) {
        ensureFieldAuditTableExists();
        String oldStr = oldValue != null ? oldValue.toString() : null;
        String newStr = newValue != null ? newValue.toString() : null;
        if (oldStr != null && oldStr.equals(newStr)) return;
        try {
            String sql = "INSERT INTO sys_field_audit (form_code, table_name, record_id, action_type, field_name, old_value, new_value, action_by, action_dt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, formCode, tableName, recordId != null ? recordId.toString() : "", actionType, fieldName, oldStr, newStr, actionBy, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
        } catch (Exception ex) {
            log.warn("Gagal mencatat audit log (form: {}, tabel: {}, field: {}, recordId: {}): {}", formCode, tableName, fieldName, recordId, ex.getMessage());
        }
    }

    public org.springframework.jdbc.core.JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public List<Map<String, Object>> fetchFieldAuditLogs(String filterFormCode, String filterTableName, String filterRecordId, String filterFieldName, String filterActionBy, int limit) {
        ensureFieldAuditTableExists();
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_field_audit WHERE 1=1 ");
        List<Object> args = new ArrayList<>();

        if (filterFormCode != null && !filterFormCode.trim().isEmpty()) {
            sql.append("AND LOWER(form_code) LIKE ? ");
            args.add("%" + filterFormCode.trim().toLowerCase() + "%");
        }
        if (filterTableName != null && !filterTableName.trim().isEmpty()) {
            sql.append("AND LOWER(table_name) LIKE ? ");
            args.add("%" + filterTableName.trim().toLowerCase() + "%");
        }
        if (filterRecordId != null && !filterRecordId.trim().isEmpty()) {
            sql.append("AND record_id = ? ");
            args.add(filterRecordId.trim());
        }
        if (filterFieldName != null && !filterFieldName.trim().isEmpty()) {
            sql.append("AND LOWER(field_name) LIKE ? ");
            args.add("%" + filterFieldName.trim().toLowerCase() + "%");
        }
        if (filterActionBy != null && !filterActionBy.trim().isEmpty()) {
            sql.append("AND LOWER(action_by) LIKE ? ");
            args.add("%" + filterActionBy.trim().toLowerCase() + "%");
        }
        sql.append("ORDER BY action_dt DESC ");
        if (limit > 0) {
            sql.append("LIMIT ").append(limit);
        } else {
            sql.append("LIMIT 500");
        }

        try {
            return jdbcTemplate.queryForList(sql.toString(), args.toArray());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Object getCaseInsensitiveValue(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        if (map.containsKey(key)) return map.get(key);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (key.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        if (key.contains(".")) {
            String[] parts = key.split("\\.", 2);
            Object sub = getCaseInsensitiveValue(map, parts[0] + "_record");
            if (!(sub instanceof Map)) {
                sub = getCaseInsensitiveValue(map, parts[0]);
            }
            if (sub instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) sub;
                Object val = getCaseInsensitiveValue(subMap, parts[1]);
                if (val != null) return val;
            }
            Object scalarVal = getCaseInsensitiveValue(map, parts[0]);
            if (scalarVal != null && !(scalarVal instanceof Map)) {
                if ("id".equalsIgnoreCase(parts[1]) || "value".equalsIgnoreCase(parts[1]) || parts[0].equalsIgnoreCase(parts[1])) {
                    return scalarVal;
                }
            }
        }
        return null;
    }

    public String getLovDefaultOrderByClause(String lovCodeOrTableName) {
        if (lovCodeOrTableName == null || lovCodeOrTableName.trim().isEmpty()) return "";
        String code = lovCodeOrTableName.trim();
        if (code.toLowerCase().contains("order by")) return "";

        try {
            Optional<com.vaadinerp.meta.FormMeta> formOpt = formMetaRepository.findById(code);
            if (formOpt.isEmpty()) {
                formOpt = formMetaRepository.findAll().stream()
                        .filter(f -> code.equalsIgnoreCase(f.getTableName())
                                || code.equalsIgnoreCase(f.getFormCode())
                                || (f.getViewTable() != null && code.equalsIgnoreCase(f.getViewTable()))
                                || (f.getDetailTableName() != null && code.equalsIgnoreCase(f.getDetailTableName())))
                        .findFirst();
            }
            if (formOpt.isPresent()) {
                com.vaadinerp.meta.FormMeta form = formOpt.get();
                String sortField = form.getDefaultSortField();
                if (sortField != null && !sortField.trim().isEmpty()) {
                    String safeCol = sortField.trim();
                    validateSqlIdentifier(safeCol, "default sort field");
                    List<String> existingCols = getColumnsForQueryOrTable(code);
                    if (!existingCols.isEmpty() && existingCols.stream().noneMatch(c -> c.equalsIgnoreCase(safeCol))) {
                        return "";
                    }
                    String dir = form.getDefaultSortDirection();
                    if (dir == null || (!dir.equalsIgnoreCase("DESC") && !dir.equalsIgnoreCase("ASC"))) {
                        dir = "ASC";
                    }
                    return " ORDER BY " + safeCol + " " + dir.toUpperCase() + " ";
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public List<Map<String, Object>> fetchLovData(String lovCode, String searchBy, String searchTerm) {
        if (lovCode == null || lovCode.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder sql = new StringBuilder();
        String trimmed = lovCode.trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            sql.append("SELECT * FROM ( ").append(validateAndSanitizeSelectQuery(trimmed)).append(" ) AS subquery");
        } else {
            sql.append("SELECT * FROM ").append(getLovQualifiedTableName(trimmed));
        }
        
        List<Object> params = new ArrayList<>();
        
        if (searchBy == null || searchBy.trim().isEmpty()) {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                List<String> allCols = getColumnsForQueryOrTable(trimmed);
                if (!allCols.isEmpty()) {
                    searchBy = String.join(",", allCols);
                }
            }
        }

        if (searchBy != null && !searchBy.trim().isEmpty() && searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" WHERE ");
            String[] cols = searchBy.split(",");
            java.util.StringJoiner orJoiner = new java.util.StringJoiner(" OR ");
            for (String col : cols) {
                String safeCol = col.trim();
                validateSqlIdentifier(safeCol, "search column");
                orJoiner.add("CAST(" + safeCol + " AS text) ILIKE ?");
                params.add("%" + searchTerm + "%");
            }
            sql.append(orJoiner.toString());
        }
        
        sql.append(getLovDefaultOrderByClause(lovCode));
        sql.append(" LIMIT 5000");
        
        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> fetchLovDataWithFilters(String tableName, String searchBy, String searchTerm, java.util.Collection<com.vaadinerp.components.FilterCondition> filters) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder sql = new StringBuilder();
        String trimmed = tableName.trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            sql.append("SELECT * FROM ( ").append(validateAndSanitizeSelectQuery(trimmed)).append(" ) AS subquery");
        } else {
            sql.append("SELECT * FROM ").append(getLovQualifiedTableName(trimmed));
        }
        
        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;
        
        if (filters != null && !filters.isEmpty()) {
            StringBuilder filterBuilder = new StringBuilder();
            boolean isFirst = true;
            for (com.vaadinerp.components.FilterCondition condition : filters) {
                if (condition.getValue() != null && !condition.getValue().toString().trim().isEmpty()) {
                    if (!isFirst) {
                        String logOp = validateLogicalOperator(condition.getLogicalOperator());
                        filterBuilder.append(" ").append(logOp).append(" ");
                    }
                    isFirst = false;
                    
                    // Validasi column name dan operator sebelum concat ke SQL
                    String safeFilterCol = condition.getFilterColumn() != null ? condition.getFilterColumn().trim() : "";
                    validateSqlIdentifier(safeFilterCol, "filter column");
                    String compOp = validateComparisonOperator(condition.getComparisonOperator());
                    
                    if ("LIKE".equals(compOp) || "ILIKE".equals(compOp)) {
                        filterBuilder.append("CAST(").append(safeFilterCol).append(" AS text) ").append(compOp).append(" ?");
                        params.add("%" + condition.getValue() + "%");
                    } else {
                        filterBuilder.append("CAST(").append(safeFilterCol).append(" AS text) ").append(compOp).append(" ?");
                        params.add(condition.getValue() != null ? condition.getValue().toString().trim() : "");
                    }
                }
            }
            if (filterBuilder.length() > 0) {
                sql.append(" WHERE (").append(filterBuilder.toString()).append(")");
                hasWhere = true;
            }
        }
        
        if (searchBy == null || searchBy.trim().isEmpty()) {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                List<String> allCols = getColumnsForQueryOrTable(trimmed);
                if (!allCols.isEmpty()) {
                    searchBy = String.join(",", allCols);
                }
            }
        }

        if (searchBy != null && !searchBy.trim().isEmpty() && searchTerm != null && !searchTerm.trim().isEmpty()) {
            if (hasWhere) {
                sql.append(" AND (");
            } else {
                sql.append(" WHERE (");
                hasWhere = true;
            }
            String[] cols = searchBy.split(",");
            java.util.StringJoiner orJoiner = new java.util.StringJoiner(" OR ");
            for (String col : cols) {
                String safeCol = col.trim();
                validateSqlIdentifier(safeCol, "search column");
                orJoiner.add("CAST(" + safeCol + " AS text) ILIKE ?");
                params.add("%" + searchTerm + "%");
            }
            sql.append(orJoiner.toString()).append(")");
        }
        
        sql.append(getLovDefaultOrderByClause(tableName));
        sql.append(" LIMIT 5000");
        
        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> fetchLovDataPaged(String tableName, String searchBy, String searchTerm, java.util.Collection<com.vaadinerp.components.FilterCondition> filters, int offset, int limit) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder sql = new StringBuilder();
        String trimmed = tableName.trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            sql.append("SELECT * FROM ( ").append(validateAndSanitizeSelectQuery(trimmed)).append(" ) AS subquery");
        } else {
            sql.append("SELECT * FROM ").append(getLovQualifiedTableName(trimmed));
        }
        
        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;
        
        if (filters != null && !filters.isEmpty()) {
            StringBuilder filterBuilder = new StringBuilder();
            boolean isFirst = true;
            for (com.vaadinerp.components.FilterCondition condition : filters) {
                if (condition.getValue() != null && !condition.getValue().toString().trim().isEmpty()) {
                    if (!isFirst) {
                        String logOp = validateLogicalOperator(condition.getLogicalOperator());
                        filterBuilder.append(" ").append(logOp).append(" ");
                    }
                    isFirst = false;
                    
                    String safeFilterCol = condition.getFilterColumn() != null ? condition.getFilterColumn().trim() : "";
                    validateSqlIdentifier(safeFilterCol, "filter column");
                    String compOp = validateComparisonOperator(condition.getComparisonOperator());
                    
                    if ("LIKE".equals(compOp) || "ILIKE".equals(compOp)) {
                        filterBuilder.append("CAST(").append(safeFilterCol).append(" AS text) ").append(compOp).append(" ?");
                        params.add("%" + condition.getValue() + "%");
                    } else {
                        filterBuilder.append("CAST(").append(safeFilterCol).append(" AS text) ").append(compOp).append(" ?");
                        params.add(condition.getValue() != null ? condition.getValue().toString().trim() : "");
                    }
                }
            }
            if (filterBuilder.length() > 0) {
                sql.append(" WHERE (").append(filterBuilder.toString()).append(")");
                hasWhere = true;
            }
        }
        
        if (searchBy == null || searchBy.trim().isEmpty()) {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                List<String> allCols = getColumnsForQueryOrTable(trimmed);
                if (!allCols.isEmpty()) {
                    searchBy = String.join(",", allCols);
                }
            }
        }

        if (searchBy != null && !searchBy.trim().isEmpty() && searchTerm != null && !searchTerm.trim().isEmpty()) {
            if (hasWhere) {
                sql.append(" AND (");
            } else {
                sql.append(" WHERE (");
                hasWhere = true;
            }
            String[] cols = searchBy.split(",");
            java.util.StringJoiner orJoiner = new java.util.StringJoiner(" OR ");
            for (String col : cols) {
                String safeCol = col.trim();
                validateSqlIdentifier(safeCol, "search column");
                orJoiner.add("CAST(" + safeCol + " AS text) ILIKE ?");
                params.add("%" + searchTerm + "%");
            }
            sql.append(orJoiner.toString()).append(")");
        }
        
        sql.append(getLovDefaultOrderByClause(tableName));
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 50);
        params.add(Math.max(0, offset));
        
        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public int countLovData(String tableName, String searchBy, String searchTerm, java.util.Collection<com.vaadinerp.components.FilterCondition> filters) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return 0;
        }
        
        StringBuilder sql = new StringBuilder();
        String trimmed = tableName.trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            sql.append("SELECT COUNT(*) FROM ( ").append(validateAndSanitizeSelectQuery(trimmed)).append(" ) AS subquery");
        } else {
            sql.append("SELECT COUNT(*) FROM ").append(getLovQualifiedTableName(trimmed));
        }
        
        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;
        
        if (filters != null && !filters.isEmpty()) {
            StringBuilder filterBuilder = new StringBuilder();
            boolean isFirst = true;
            for (com.vaadinerp.components.FilterCondition condition : filters) {
                if (condition.getValue() != null && !condition.getValue().toString().trim().isEmpty()) {
                    if (!isFirst) {
                        String logOp = validateLogicalOperator(condition.getLogicalOperator());
                        filterBuilder.append(" ").append(logOp).append(" ");
                    }
                    isFirst = false;
                    
                    String safeFilterCol = condition.getFilterColumn() != null ? condition.getFilterColumn().trim() : "";
                    validateSqlIdentifier(safeFilterCol, "filter column");
                    String compOp = validateComparisonOperator(condition.getComparisonOperator());
                    
                    if ("LIKE".equals(compOp) || "ILIKE".equals(compOp)) {
                        filterBuilder.append("CAST(").append(safeFilterCol).append(" AS text) ").append(compOp).append(" ?");
                        params.add("%" + condition.getValue() + "%");
                    } else {
                        filterBuilder.append("CAST(").append(safeFilterCol).append(" AS text) ").append(compOp).append(" ?");
                        params.add(condition.getValue() != null ? condition.getValue().toString().trim() : "");
                    }
                }
            }
            if (filterBuilder.length() > 0) {
                sql.append(" WHERE (").append(filterBuilder.toString()).append(")");
                hasWhere = true;
            }
        }
        
        if (searchBy == null || searchBy.trim().isEmpty()) {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                List<String> allCols = getColumnsForQueryOrTable(trimmed);
                if (!allCols.isEmpty()) {
                    searchBy = String.join(",", allCols);
                }
            }
        }

        if (searchBy != null && !searchBy.trim().isEmpty() && searchTerm != null && !searchTerm.trim().isEmpty()) {
            if (hasWhere) {
                sql.append(" AND (");
            } else {
                sql.append(" WHERE (");
                hasWhere = true;
            }
            String[] cols = searchBy.split(",");
            java.util.StringJoiner orJoiner = new java.util.StringJoiner(" OR ");
            for (String col : cols) {
                String safeCol = col.trim();
                validateSqlIdentifier(safeCol, "search column");
                orJoiner.add("CAST(" + safeCol + " AS text) ILIKE ?");
                params.add("%" + searchTerm + "%");
            }
            sql.append(orJoiner.toString()).append(")");
        }
        
        try {
            Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String resolveExistingColumn(String tableName, String requestedCol) {
        if (tableName == null || requestedCol == null || requestedCol.isBlank()) return requestedCol != null && !requestedCol.isBlank() ? requestedCol : "id";
        List<String> cols = getColumnsForQueryOrTable(tableName);
        if (cols.isEmpty()) return requestedCol;
        for (String c : cols) {
            if (c.equalsIgnoreCase(requestedCol)) return c;
        }
        for (String c : cols) {
            if (c.equalsIgnoreCase("id") || c.equalsIgnoreCase("code") || c.equalsIgnoreCase("kode") || c.equalsIgnoreCase("no") || c.equalsIgnoreCase("nomor") || c.toLowerCase().endsWith("_id") || c.toLowerCase().endsWith("_code") || c.toLowerCase().endsWith("_no")) {
                return c;
            }
        }
        return cols.get(0);
    }

    public Map<String, Object> fetchLovRecord(String tableName, String valueColumn, Object value) {
        if (tableName == null || valueColumn == null || value == null || value.toString().trim().isEmpty()) {
            return null;
        }
        valueColumn = resolveExistingColumn(tableName, valueColumn);
        
        StringBuilder sql = new StringBuilder();
        String trimmed = tableName.trim();
        String sVal = value.toString().trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            sql.append("SELECT * FROM ( ").append(validateAndSanitizeSelectQuery(trimmed)).append(" ) AS subquery WHERE CAST(").append(valueColumn).append(" AS text) = ? LIMIT 1");
        } else {
            sql.append("SELECT * FROM ").append(getLovQualifiedTableName(trimmed)).append(" WHERE CAST(").append(valueColumn).append(" AS text) = ? LIMIT 1");
        }
        
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), sVal);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, Object>> fetchAllLovRecords(com.vaadinerp.meta.LovMeta lovMeta) {
        if (lovMeta == null || lovMeta.getTableName() == null || lovMeta.getTableName().isBlank()) {
            return new ArrayList<>();
        }
        String trimmed = lovMeta.getTableName().trim();
        String orderBy = getLovDefaultOrderByClause(lovMeta.getLovCode() != null ? lovMeta.getLovCode() : trimmed);
        String sql;
        if (trimmed.toLowerCase().startsWith("select")) {
            sql = "SELECT * FROM ( " + validateAndSanitizeSelectQuery(trimmed) + " ) AS subquery" + orderBy + " LIMIT 3000";
        } else {
            sql = "SELECT * FROM " + getLovQualifiedTableName(trimmed) + orderBy + " LIMIT 3000";
        }
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> fetchTableData(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String trimmed = tableName.trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            try {
                return jdbcTemplate.queryForList(validateAndSanitizeSelectQuery(trimmed));
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
        String sql = "SELECT * FROM " + getQualifiedTableName(trimmed);
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            // Jika tabel belum dibuat, kembalikan list kosong
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> fetchGridData(FormMeta formMeta) {
        if (formMeta == null) {
            return new ArrayList<>();
        }
        String viewTable = formMeta.getViewTable();
        if (viewTable != null && !viewTable.trim().isEmpty()) {
            String trimmed = viewTable.trim();
            if (trimmed.toLowerCase().startsWith("select")) {
                try {
                    return jdbcTemplate.queryForList(validateAndSanitizeSelectQuery(trimmed));
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            } else {
                String sql = "SELECT * FROM " + getQualifiedTableName(trimmed);
                try {
                    return jdbcTemplate.queryForList(sql);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }
        }
        return fetchTableData(formMeta.getTableName());
    }

    private String getBaseFromClause(FormMeta formMeta) {
        if (formMeta == null) return "FROM (SELECT 1) AS dummy_t ";
        String viewTable = formMeta.getViewTable();
        if (viewTable != null && !viewTable.trim().isEmpty()) {
            String trimmed = viewTable.trim();
            if (trimmed.toLowerCase().startsWith("select")) {
                try {
                    return "FROM (" + validateAndSanitizeSelectQuery(trimmed) + ") AS subquery ";
                } catch (Exception e) {
                    return "FROM (SELECT 1) AS dummy_t ";
                }
            } else {
                return "FROM " + getQualifiedTableName(trimmed) + " ";
            }
        }
        return "FROM " + getQualifiedTableName(formMeta.getTableName()) + " ";
    }

    private void buildWhereClause(StringBuilder where, List<Object> args, Map<String, ?> filterValues, FormMeta formMeta) {
        if (filterValues == null || filterValues.isEmpty()) return;
        for (Map.Entry<String, ?> entry : filterValues.entrySet()) {
            String colName = entry.getKey();
            Object criteriaObj = entry.getValue();
            if (colName == null || criteriaObj == null) continue;
            if (!colName.matches("^[a-zA-Z0-9_]+$")) continue; // SQL injection protection
            
            String op = "Contains";
            String val = "";
            try {
                java.lang.reflect.Field opField = criteriaObj.getClass().getField("operator");
                java.lang.reflect.Field valField = criteriaObj.getClass().getField("value");
                op = (String) opField.get(criteriaObj);
                val = (String) valField.get(criteriaObj);
            } catch (Exception e) {
                try {
                    java.lang.reflect.Field opField = criteriaObj.getClass().getDeclaredField("operator");
                    java.lang.reflect.Field valField = criteriaObj.getClass().getDeclaredField("value");
                    opField.setAccessible(true);
                    valField.setAccessible(true);
                    op = (String) opField.get(criteriaObj);
                    val = (String) valField.get(criteriaObj);
                } catch (Exception ex) {
                    continue;
                }
            }
            
            if ("Blank".equals(op)) {
                where.append(" AND (" + colName + " IS NULL OR TRIM(CAST(" + colName + " AS TEXT)) = '') ");
            } else if ("Not blank".equals(op)) {
                where.append(" AND (" + colName + " IS NOT NULL AND TRIM(CAST(" + colName + " AS TEXT)) != '') ");
            } else if (val != null && !val.trim().isEmpty()) {
                val = val.trim();
                appendConditionWithLov(where, args, colName, op, val, formMeta);
            }
        }
    }

    private void appendConditionWithLov(StringBuilder where, List<Object> args, String colName, String op, String val, FormMeta formMeta) {
        com.vaadinerp.meta.LovMeta lovMeta = null;
        if (formMeta != null && formMeta.getFields() != null) {
            for (com.vaadinerp.meta.FieldMeta f : formMeta.getFields()) {
                if (colName.equalsIgnoreCase(f.getFieldName()) && f.getLovCode() != null && !f.getLovCode().trim().isEmpty()) {
                    lovMeta = getLovMeta(f.getLovCode().trim()).orElse(null);
                    break;
                }
            }
        }

        if (lovMeta == null || lovMeta.getTableName() == null || lovMeta.getTableName().isBlank()) {
            switch (op) {
                case "Contains":
                    where.append(" AND CAST(" + colName + " AS TEXT) ILIKE ? ");
                    args.add("%" + val + "%");
                    break;
                case "Not contains":
                    where.append(" AND CAST(" + colName + " AS TEXT) NOT ILIKE ? ");
                    args.add("%" + val + "%");
                    break;
                case "Equals":
                    where.append(" AND CAST(" + colName + " AS TEXT) ILIKE ? ");
                    args.add(val);
                    break;
                case "Not equal":
                    where.append(" AND CAST(" + colName + " AS TEXT) NOT ILIKE ? ");
                    args.add(val);
                    break;
                case "Starts with":
                    where.append(" AND CAST(" + colName + " AS TEXT) ILIKE ? ");
                    args.add(val + "%");
                    break;
                case "Ends with":
                    where.append(" AND CAST(" + colName + " AS TEXT) ILIKE ? ");
                    args.add("%" + val);
                    break;
                default:
                    where.append(" AND CAST(" + colName + " AS TEXT) ILIKE ? ");
                    args.add("%" + val + "%");
                    break;
            }
            return;
        }

        String valCol = lovMeta.getValueColumn() != null && !lovMeta.getValueColumn().isBlank() ? lovMeta.getValueColumn().trim() : "id";
        if (!valCol.matches("^[a-zA-Z0-9_]+$")) valCol = "id";

        String lblCol = lovMeta.getLabelColumn() != null && !lovMeta.getLabelColumn().isBlank() ? lovMeta.getLabelColumn().trim() : valCol;
        String searchCol = lovMeta.getSearchColumn() != null && !lovMeta.getSearchColumn().isBlank() ? lovMeta.getSearchColumn().trim() : lblCol;

        java.util.Set<String> lovSearchCols = new java.util.LinkedHashSet<>();
        if (valCol.matches("^[a-zA-Z0-9_]+$")) lovSearchCols.add(valCol);
        if (lblCol.matches("^[a-zA-Z0-9_]+$")) lovSearchCols.add(lblCol);
        for (String sc : searchCol.split(",")) {
            String trimmedSc = sc.trim();
            if (trimmedSc.matches("^[a-zA-Z0-9_]+$")) {
                lovSearchCols.add(trimmedSc);
            }
        }

        String trimmedTable = lovMeta.getTableName().trim();
        String lovTableSql;
        if (trimmedTable.toLowerCase().startsWith("select")) {
            lovTableSql = " (" + validateAndSanitizeSelectQuery(trimmedTable) + ") AS lov_sub ";
        } else {
            lovTableSql = getLovQualifiedTableName(trimmedTable);
        }

        String directOp = "ILIKE";
        String subqueryOp = "ILIKE";
        String sqlVal = "%" + val + "%";
        boolean isNegative = false;
        switch (op) {
            case "Equals":
                directOp = "ILIKE";
                subqueryOp = "ILIKE";
                sqlVal = val;
                break;
            case "Not equal":
                directOp = "NOT ILIKE";
                subqueryOp = "ILIKE";
                sqlVal = val;
                isNegative = true;
                break;
            case "Starts with":
                directOp = "ILIKE";
                subqueryOp = "ILIKE";
                sqlVal = val + "%";
                break;
            case "Ends with":
                directOp = "ILIKE";
                subqueryOp = "ILIKE";
                sqlVal = "%" + val;
                break;
            case "Not contains":
                directOp = "NOT ILIKE";
                subqueryOp = "ILIKE";
                sqlVal = "%" + val + "%";
                isNegative = true;
                break;
            default: // Contains
                directOp = "ILIKE";
                subqueryOp = "ILIKE";
                sqlVal = "%" + val + "%";
                break;
        }

        StringBuilder lovWhere = new StringBuilder();
        for (String sc : lovSearchCols) {
            if (lovWhere.length() > 0) {
                lovWhere.append(" OR ");
            }
            lovWhere.append("CAST(").append(sc).append(" AS TEXT) ").append(subqueryOp).append(" ?");
        }

        if (isNegative) {
            where.append(" AND (CAST(").append(colName).append(" AS TEXT) ").append(directOp).append(" ? AND CAST(").append(colName)
                 .append(" AS TEXT) NOT IN (SELECT CAST(").append(valCol).append(" AS TEXT) FROM ").append(lovTableSql)
                 .append(" WHERE ").append(lovWhere).append(")) ");
        } else {
            where.append(" AND (CAST(").append(colName).append(" AS TEXT) ").append(directOp).append(" ? OR CAST(").append(colName)
                 .append(" AS TEXT) IN (SELECT CAST(").append(valCol).append(" AS TEXT) FROM ").append(lovTableSql)
                 .append(" WHERE ").append(lovWhere).append(")) ");
        }

        args.add(sqlVal);
        for (int i = 0; i < lovSearchCols.size(); i++) {
            args.add(sqlVal);
        }
    }

    public long countGridData(FormMeta formMeta, Map<String, ?> filterValues) {
        if (formMeta == null) return 0;
        String baseFrom = getBaseFromClause(formMeta);
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        buildWhereClause(where, args, filterValues, formMeta);
        
        String sql = "SELECT COUNT(*) " + baseFrom + where.toString();
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
            return count != null ? count : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public List<Map<String, Object>> fetchGridDataPaged(FormMeta formMeta, int offset, int limit, Map<String, ?> filterValues, String sortField, String sortDir) {
        if (formMeta == null) return new ArrayList<>();
        String baseFrom = getBaseFromClause(formMeta);
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        buildWhereClause(where, args, filterValues, formMeta);
        
        StringBuilder sql = new StringBuilder("SELECT * " + baseFrom + where.toString());
        
        if (sortField != null && !sortField.trim().isEmpty() && sortField.matches("^[a-zA-Z0-9_]+$")) {
            String dir = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
            sql.append(" ORDER BY ").append(sortField).append(" ").append(dir);
        } else if (formMeta.getPrimaryKey() != null && !formMeta.getPrimaryKey().trim().isEmpty() && formMeta.getPrimaryKey().matches("^[a-zA-Z0-9_]+$")) {
            sql.append(" ORDER BY ").append(formMeta.getPrimaryKey()).append(" DESC");
        } else {
            sql.append(" ORDER BY 1 DESC");
        }
        
        sql.append(" LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);
        
        try {
            return jdbcTemplate.queryForList(sql.toString(), args.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public long countTableDataPaged(String tableName, Map<String, ?> filterValues) {
        if (tableName == null || tableName.trim().isEmpty()) return 0L;
        String trimmed = tableName.trim();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        buildWhereClause(where, args, filterValues, null);
        String fromSql = trimmed.toLowerCase().startsWith("select")
                ? " (" + validateAndSanitizeSelectQuery(trimmed) + ") AS subquery "
                : getQualifiedTableName(trimmed);
        String sql = "SELECT COUNT(*) FROM " + fromSql + where.toString();
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public List<Map<String, Object>> fetchTableDataPaged(String tableName, int offset, int limit, Map<String, ?> filterValues, String sortField, String sortDir) {
        if (tableName == null || tableName.trim().isEmpty()) return new ArrayList<>();
        String trimmed = tableName.trim();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        buildWhereClause(where, args, filterValues, null);
        
        String fromSql = trimmed.toLowerCase().startsWith("select")
                ? " (" + validateAndSanitizeSelectQuery(trimmed) + ") AS subquery "
                : getQualifiedTableName(trimmed);
        StringBuilder sql = new StringBuilder("SELECT * FROM " + fromSql + where.toString());
        if (sortField != null && !sortField.trim().isEmpty() && sortField.matches("^[a-zA-Z0-9_]+$")) {
            String dir = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
            sql.append(" ORDER BY ").append(sortField).append(" ").append(dir);
        }
        sql.append(" LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);
        try {
            return jdbcTemplate.queryForList(sql.toString(), args.toArray());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Menyimpan urutan kolom baru hasil drag-and-drop dari Grid ke tabel meta_field.
     * orderedFieldNames adalah daftar nama field sesuai urutan tampil yang baru
     * (index 0 = kolom paling kiri, dst).
     */
    @Transactional
    public void saveColumnOrder(String formCode, List<String> orderedFieldNames) {
        if (formCode == null || orderedFieldNames == null || orderedFieldNames.isEmpty()) {
            return;
        }

        // Penyesuaian nama tabel menjadi meta_field dan kolom col_order berdasarkan FieldMeta.java
        String sql = "UPDATE meta_field SET col_order = ? WHERE form_code = ? AND field_name = ?";

        // Batch update: lebih efisien daripada loop single update satu-satu
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < orderedFieldNames.size(); i++) {
            String fieldName = orderedFieldNames.get(i);
            int newOrder = (i + 1) * 10;
            batchArgs.add(new Object[]{newOrder, formCode, fieldName});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Transactional
    public void saveUserGridOrder(String formCode, String gridId, List<String> orderedFieldNames) {
        if (formCode == null || gridId == null || orderedFieldNames == null || orderedFieldNames.isEmpty() || userGridPreferenceRepository == null || securityService == null) {
            return;
        }
        com.vaadinerp.security.entity.AppUser user = securityService.getCurrentUser();
        if (user == null || user.getUsername() == null) {
            return;
        }
        String username = user.getUsername();
        String json = String.join(",", orderedFieldNames);
        com.vaadinerp.security.entity.AppUserGridPreference pref = userGridPreferenceRepository
                .findByUsernameAndFormCodeAndGridId(username, formCode, gridId)
                .orElseGet(() -> {
                    com.vaadinerp.security.entity.AppUserGridPreference p = new com.vaadinerp.security.entity.AppUserGridPreference();
                    p.setUsername(username);
                    p.setFormCode(formCode);
                    p.setGridId(gridId);
                    return p;
                });
        pref.setColumnOrderJson(json);
        pref.setUpdatedAt(java.time.LocalDateTime.now());
        userGridPreferenceRepository.save(pref);
    }

    public List<String> getUserGridOrder(String formCode, String gridId) {
        if (formCode == null || gridId == null || userGridPreferenceRepository == null || securityService == null) {
            return new ArrayList<>();
        }
        com.vaadinerp.security.entity.AppUser user = securityService.getCurrentUser();
        if (user == null || user.getUsername() == null) {
            return new ArrayList<>();
        }
        return userGridPreferenceRepository
                .findByUsernameAndFormCodeAndGridId(user.getUsername(), formCode, gridId)
                .map(pref -> {
                    String json = pref.getColumnOrderJson();
                    if (json == null || json.trim().isEmpty()) return new ArrayList<String>();
                    return java.util.Arrays.stream(json.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                })
                .orElse(new ArrayList<>());
    }

    @Transactional
    public void resetUserGridOrder(String formCode, String gridId) {
        if (formCode == null || gridId == null || userGridPreferenceRepository == null || securityService == null) {
            return;
        }
        com.vaadinerp.security.entity.AppUser user = securityService.getCurrentUser();
        if (user != null && user.getUsername() != null) {
            userGridPreferenceRepository.deleteByUsernameAndFormCodeAndGridId(user.getUsername(), formCode, gridId);
        }
    }

    @lombok.Data
    public static class ColumnDefinition {
        private String columnName;
        private String dataType; // VARCHAR(255), INTEGER, DECIMAL(19,2), etc.
        private boolean nullable = true;
        private boolean primaryKey = false;
        private String defaultValue;
    }

    @lombok.Data
    public static class TriggerDefinition {
        private String triggerName;
        private String timing; // BEFORE, AFTER
        private List<String> events; // INSERT, UPDATE, DELETE
        private String triggerBody; // PL/pgSQL body
    }

    @Transactional
    public void createPhysicalTableAndTrigger(String tableName, List<ColumnDefinition> columns, TriggerDefinition trigger) {
        // 1. Create schema dynamic if not exists
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS dynamic;");

        String qTableName = getQualifiedTableName(tableName);
        String rawTableName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;

        // 2. Build CREATE TABLE SQL
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(qTableName).append(" (");
        
        StringJoiner colDefs = new StringJoiner(", ");
        for (ColumnDefinition col : columns) {
            StringBuilder colSql = new StringBuilder();
            colSql.append(col.getColumnName()).append(" ").append(col.getDataType());
            
            if (col.isPrimaryKey()) {
                colSql.append(" PRIMARY KEY");
            } else {
                if (!col.isNullable()) {
                    colSql.append(" NOT NULL");
                }
                if (col.getDefaultValue() != null && !col.getDefaultValue().trim().isEmpty()) {
                    colSql.append(" DEFAULT ").append(col.getDefaultValue());
                }
            }
            colDefs.add(colSql.toString());
        }
        sql.append(colDefs.toString()).append(")");

        jdbcTemplate.execute(sql.toString());

        // 3. Handle Trigger Function and Trigger
        if (trigger != null && trigger.getTriggerName() != null && !trigger.getTriggerName().trim().isEmpty()) {
            String triggerName = trigger.getTriggerName().trim();

            // Keamanan: validasi nama trigger, body, timing, dan events
            validateSqlIdentifier(triggerName, "trigger name");
            validateTriggerBody(trigger.getTriggerBody());

            String functionName = "fn_" + rawTableName + "_" + triggerName;
            
            // Build Function DDL
            StringBuilder funcSql = new StringBuilder();
            funcSql.append("CREATE OR REPLACE FUNCTION dynamic.").append(functionName).append("()\n");
            funcSql.append("RETURNS TRIGGER AS $$\n");
            funcSql.append(trigger.getTriggerBody()).append("\n");
            funcSql.append("$$ LANGUAGE plpgsql;");

            jdbcTemplate.execute(funcSql.toString());

            // Drop existing trigger to avoid duplicate error
            jdbcTemplate.execute("DROP TRIGGER IF EXISTS " + triggerName + " ON " + qTableName);

            // Build CREATE TRIGGER SQL — validasi timing dan events
            String safeTiming = validateTriggerTiming(trigger.getTiming());
            validateTriggerEvents(trigger.getEvents());
            String eventStr = String.join(" OR ", trigger.getEvents());
            
            StringBuilder triggerSql = new StringBuilder();
            triggerSql.append("CREATE TRIGGER ").append(triggerName).append("\n");
            triggerSql.append(safeTiming).append(" ").append(eventStr).append("\n");
            triggerSql.append("ON ").append(qTableName).append("\n");
            triggerSql.append("FOR EACH ROW EXECUTE FUNCTION dynamic.").append(functionName).append("();");

            jdbcTemplate.execute(triggerSql.toString());
        }
    }

    public List<Map<String, Object>> fetchDetailTableData(String detailTableName, String fkColumn, Object fkValue) {
        if (detailTableName == null || fkColumn == null || fkValue == null) {
            return new ArrayList<>();
        }
        String trimmed = detailTableName.trim();
        fkColumn = resolveExistingColumn(trimmed, fkColumn);
        String orderBy = getLovDefaultOrderByClause(trimmed);
        String sql;
        if (trimmed.toLowerCase().startsWith("select")) {
            sql = "SELECT * FROM ( " + validateAndSanitizeSelectQuery(trimmed) + " ) AS detail_sub WHERE CAST(" + fkColumn + " AS text) = ?" + orderBy;
        } else {
            sql = "SELECT * FROM " + getQualifiedTableName(trimmed) + " WHERE CAST(" + fkColumn + " AS text) = ?" + orderBy;
        }
        try {
            return jdbcTemplate.queryForList(sql, fkValue.toString().trim());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional
    public void saveMasterDetailData(FormMeta formMeta, Map<String, Object> rawMasterData,
                                     List<Map<String, Object>> detailsData,
                                     List<Map<String, Object>> deletedDetailsData) {
        if (rawMasterData == null || rawMasterData.isEmpty()) {
            throw new IllegalArgumentException("Data master kosong!");
        }
        Map<String, Object> masterData = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        masterData.putAll(rawMasterData);

        ensureMasterDetailTablesExist(formMeta);

        String masterPk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";
        masterPk = resolveExistingColumn(formMeta.getTableName(), masterPk);
        final String finalMasterPk = masterPk;
        String detailTableName = formMeta.getDetailTableName();
        String detailPk = formMeta.getDetailPrimaryKey() != null ? formMeta.getDetailPrimaryKey() : "id";
        String detailFk = formMeta.getDetailForeignKey();

        if (detailTableName == null || detailTableName.trim().isEmpty() || detailFk == null || detailFk.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama tabel detail dan foreign key harus didefinisikan!");
        }

        ensureAuditColumnsExist(formMeta.getTableName());
        ensureAuditColumnsExist(detailTableName);

        boolean isUpdate = masterData.containsKey(masterPk) && masterData.get(masterPk) != null 
                           && !masterData.get(masterPk).toString().trim().isEmpty();

        Object masterId = masterData.get(masterPk);

        // 1. Save or Update Master Record
        if (formMeta.getTableName() != null && !formMeta.getTableName().trim().isEmpty()) {
            if (isUpdate) {
                masterId = masterData.get(masterPk);
            Map<String, Object> oldMasterRecord = null;
            try {
                String selectSql = "SELECT * FROM " + getQualifiedTableName(formMeta.getTableName()) + " WHERE CAST(" + masterPk + " AS text) = ?";
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, masterId != null ? masterId.toString().trim() : "");
                if (!rows.isEmpty()) {
                    oldMasterRecord = rows.get(0);
                }
            } catch (Exception ignored) {}

            StringJoiner setClause = new StringJoiner(", ");
            List<Object> args = new ArrayList<>();

            for (FieldMeta field : formMeta.getFields()) {
                if (field.isDetail()) continue; // skip detail columns
                String fieldName = field.getFieldName();
                if (fieldName.equalsIgnoreCase(masterPk)) continue;
                if (fieldName.equalsIgnoreCase("inputby") || fieldName.equalsIgnoreCase("inputdt") ||
                    fieldName.equalsIgnoreCase("updateby") || fieldName.equalsIgnoreCase("updatedt") ||
                    fieldName.equalsIgnoreCase("version")) continue;
                if (!field.isSaveOnUpdate()) continue;
                if (masterData.containsKey(fieldName)) {
                    setClause.add(fieldName + " = ?");
                    args.add(sanitizeJdbcValue(formMeta.getTableName(), fieldName, masterData.get(fieldName)));
                }
            }

            String currentUser = getCurrentLoggedUser();
            java.sql.Timestamp nowTs = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
            setClause.add("updateby = ?");
            args.add(currentUser);
            setClause.add("updatedt = ?");
            args.add(nowTs);
            setClause.add("version = COALESCE(version, 0) + 1");
            masterData.put("updateby", currentUser);
            masterData.put("updatedt", nowTs);

            if (!args.isEmpty()) {
                StringBuilder whereClause = new StringBuilder(" WHERE CAST(" + masterPk + " AS text) = ?");
                args.add(masterId != null ? masterId.toString().trim() : "");

                Object oldVersion = masterData.get("version");
                if (oldVersion != null && !oldVersion.toString().trim().isEmpty()) {
                    try {
                        int vNum = Integer.parseInt(oldVersion.toString().trim());
                        whereClause.append(" AND COALESCE(version, 0) = ?");
                        args.add(vNum);
                    } catch (NumberFormatException ignored) {}
                }

                String sql = "UPDATE " + getQualifiedTableName(formMeta.getTableName()) + " SET " + setClause.toString() + whereClause.toString();
                int updatedRows = executeAndLogSql(sql, args);
                if (updatedRows == 0) {
                    throw new org.springframework.dao.OptimisticLockingFailureException("Konflik Data Master: Data master telah diubah oleh pengguna lain. Silakan muat ulang (refresh) data terbaru.");
                }

                if (oldVersion != null) {
                    try {
                        masterData.put("version", Integer.parseInt(oldVersion.toString().trim()) + 1);
                    } catch (Exception ignored) {
                        masterData.put("version", 1);
                    }
                } else {
                    masterData.put("version", 1);
                }
            }

            if (oldMasterRecord != null) {
                for (FieldMeta field : formMeta.getFields()) {
                    if (!field.isDetail() && field.isAuditLog()) {
                        String fieldName = field.getFieldName();
                        Object oldVal = getCaseInsensitiveValue(oldMasterRecord, fieldName);
                        Object newVal = masterData.get(fieldName);
                        recordFieldAuditLog(formMeta.getFormCode(), formMeta.getTableName(), masterId, "UPDATE", fieldName, oldVal, newVal, currentUser);
                    }
                }
            }
        } else {
            // INSERT Master with KeyHolder
            StringJoiner columns = new StringJoiner(", ");
            StringJoiner valuesParam = new StringJoiner(", ");
            List<Object> args = new ArrayList<>();

            for (FieldMeta field : formMeta.getFields()) {
                if (field.isDetail()) continue; // skip detail columns
                String fieldName = field.getFieldName();
                if (fieldName.equalsIgnoreCase(masterPk)) continue;
                if (fieldName.equalsIgnoreCase("inputby") || fieldName.equalsIgnoreCase("inputdt") ||
                    fieldName.equalsIgnoreCase("updateby") || fieldName.equalsIgnoreCase("updatedt")) continue;
                if (!field.isSaveOnInsert()) continue;
                if (masterData.containsKey(fieldName)) {
                    columns.add(fieldName);
                    valuesParam.add("?");
                    args.add(sanitizeJdbcValue(formMeta.getTableName(), fieldName, masterData.get(fieldName)));
                }
            }

            String currentUser = getCurrentLoggedUser();
            java.sql.Timestamp nowTs = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
            columns.add("inputby");
            valuesParam.add("?");
            args.add(currentUser);
            columns.add("inputdt");
            valuesParam.add("?");
            args.add(nowTs);
            columns.add("version");
            valuesParam.add("0");
            masterData.put("inputby", currentUser);
            masterData.put("inputdt", nowTs);
            masterData.put("version", 0);

            String qMasterTable = getQualifiedTableName(formMeta.getTableName());
            String sql = "INSERT INTO " + qMasterTable + " (" + columns.toString() + ") VALUES (" + valuesParam.toString() + ")";
            org.springframework.jdbc.support.GeneratedKeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
            
            System.out.println("=================================================");
            System.out.println("EXECUTING MASTER INSERT SQL: " + sql);
            System.out.println("PARAMETERS: " + args);
            System.out.println("=================================================");
            jdbcTemplate.update(connection -> {
                java.sql.PreparedStatement ps = connection.prepareStatement(sql, new String[]{finalMasterPk});
                for (int i = 0; i < args.size(); i++) {
                    ps.setObject(i + 1, args.get(i));
                }
                return ps;
            }, keyHolder);
            
            masterId = keyHolder.getKey();
            masterData.put(masterPk, masterId); // update local bean state

            for (FieldMeta field : formMeta.getFields()) {
                if (!field.isDetail() && field.isAuditLog()) {
                    String fieldName = field.getFieldName();
                    if (masterData.containsKey(fieldName)) {
                        recordFieldAuditLog(formMeta.getFormCode(), formMeta.getTableName(), masterId, "INSERT", fieldName, null, masterData.get(fieldName), currentUser);
                    }
                }
            }
        }
        } else {
            log.debug("Target table master untuk form '{}' kosong/null, melewati penyimpanan tabel master dan langsung memproses detail.", formMeta.getFormCode());
        }

        // 2. Delete removed detail rows
        if (deletedDetailsData != null && !deletedDetailsData.isEmpty()) {
            String qDetailTable = getQualifiedTableName(detailTableName);
            for (Map<String, Object> row : deletedDetailsData) {
                if (row.containsKey(detailPk) && row.get(detailPk) != null && !row.get(detailPk).toString().trim().isEmpty()) {
                    String sql = "DELETE FROM " + qDetailTable + " WHERE CAST(" + detailPk + " AS text) = ?";
                    jdbcTemplate.update(sql, row.get(detailPk).toString().trim());
                }
            }
        }

        // 3. Save or Update Detail rows
        if (detailsData != null) {
            String qDetailTable = getQualifiedTableName(detailTableName);
            for (Map<String, Object> rawRow : detailsData) {
                Map<String, Object> row = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                row.putAll(rawRow);
                row.put(detailFk, masterId);

                boolean isDetailUpdate = row.containsKey(detailPk) && row.get(detailPk) != null 
                                         && !row.get(detailPk).toString().trim().isEmpty();

                if (isDetailUpdate) {
                    Object detailId = row.get(detailPk);
                    Map<String, Object> oldDetailRecord = null;
                    try {
                        String selectSql = "SELECT * FROM " + qDetailTable + " WHERE CAST(" + detailPk + " AS text) = ?";
                        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, detailId != null ? detailId.toString().trim() : "");
                        if (!rows.isEmpty()) {
                            oldDetailRecord = rows.get(0);
                        }
                    } catch (Exception ignored) {}

                    // UPDATE detail row
                    StringJoiner setClause = new StringJoiner(", ");
                    List<Object> args = new ArrayList<>();

                    for (FieldMeta field : formMeta.getFields()) {
                        if (!field.isDetail()) continue; // skip master fields
                        String fieldName = field.getFieldName();
                        if (fieldName.equalsIgnoreCase(detailPk)) continue;
                        if (fieldName.equalsIgnoreCase("inputby") || fieldName.equalsIgnoreCase("inputdt") ||
                            fieldName.equalsIgnoreCase("updateby") || fieldName.equalsIgnoreCase("updatedt") ||
                            fieldName.equalsIgnoreCase("version")) continue;
                        if (!field.isSaveOnUpdate()) continue;
                        if (row.containsKey(fieldName)) {
                            setClause.add(fieldName + " = ?");
                            args.add(sanitizeJdbcValue(detailTableName, fieldName, row.get(fieldName)));
                        }
                    }

                    String currentUser = getCurrentLoggedUser();
                    java.sql.Timestamp nowTs = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
                    setClause.add("updateby = ?");
                    args.add(currentUser);
                    setClause.add("updatedt = ?");
                    args.add(nowTs);
                    setClause.add("version = COALESCE(version, 0) + 1");
                    row.put("updateby", currentUser);
                    row.put("updatedt", nowTs);

                    setClause.add(detailFk + " = ?");
                    args.add(sanitizeJdbcValue(detailTableName, detailFk, masterId));

                    if (!args.isEmpty()) {
                        StringBuilder whereClause = new StringBuilder(" WHERE CAST(" + detailPk + " AS text) = ?");
                        args.add(row.get(detailPk) != null ? row.get(detailPk).toString().trim() : "");

                        Object oldVersion = row.get("version");
                        if (oldVersion != null && !oldVersion.toString().trim().isEmpty()) {
                            try {
                                int vNum = Integer.parseInt(oldVersion.toString().trim());
                                whereClause.append(" AND COALESCE(version, 0) = ?");
                                args.add(vNum);
                            } catch (NumberFormatException ignored) {}
                        }

                        String sql = "UPDATE " + qDetailTable + " SET " + setClause.toString() + whereClause.toString();
                        int updatedRows = executeAndLogSql(sql, args);
                        if (updatedRows == 0) {
                            throw new org.springframework.dao.OptimisticLockingFailureException("Konflik Data Rincian: Baris rincian telah diubah oleh pengguna lain. Silakan muat ulang (refresh) data terbaru.");
                        }

                        if (oldVersion != null) {
                            try {
                                row.put("version", Integer.parseInt(oldVersion.toString().trim()) + 1);
                            } catch (Exception ignored) {
                                row.put("version", 1);
                            }
                        } else {
                            row.put("version", 1);
                        }
                    }

                    if (oldDetailRecord != null) {
                        for (FieldMeta field : formMeta.getFields()) {
                            if (field.isDetail() && field.isAuditLog()) {
                                String fieldName = field.getFieldName();
                                Object oldVal = getCaseInsensitiveValue(oldDetailRecord, fieldName);
                                Object newVal = row.get(fieldName);
                                recordFieldAuditLog(formMeta.getFormCode(), detailTableName, detailId, "UPDATE", fieldName, oldVal, newVal, currentUser);
                            }
                        }
                    }
                } else {
                    // INSERT detail row
                    StringJoiner columns = new StringJoiner(", ");
                    StringJoiner valuesParam = new StringJoiner(", ");
                    List<Object> args = new ArrayList<>();

                    for (FieldMeta field : formMeta.getFields()) {
                        if (!field.isDetail()) continue; // skip master fields
                        String fieldName = field.getFieldName();
                        if (fieldName.equalsIgnoreCase(detailPk)) {
                            Object pkVal = row.get(fieldName);
                            if ((field.getSequenceCode() == null || field.getSequenceCode().trim().isEmpty()) &&
                                (pkVal == null || pkVal.toString().trim().isEmpty() || "0".equals(pkVal.toString().trim()) || pkVal.toString().trim().startsWith("[AUTO") || pkVal.toString().trim().startsWith("⚡"))) {
                                continue;
                            }
                        }
                        if (fieldName.equalsIgnoreCase("inputby") || fieldName.equalsIgnoreCase("inputdt") ||
                            fieldName.equalsIgnoreCase("updateby") || fieldName.equalsIgnoreCase("updatedt")) continue;
                        if (!field.isSaveOnInsert()) continue;
                        if (row.containsKey(fieldName)) {
                            columns.add(fieldName);
                            valuesParam.add("?");
                            args.add(sanitizeJdbcValue(detailTableName, fieldName, row.get(fieldName)));
                        }
                    }

                    String currentUser = getCurrentLoggedUser();
                    java.sql.Timestamp nowTs = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
                    columns.add("inputby");
                    valuesParam.add("?");
                    args.add(currentUser);
                    columns.add("inputdt");
                    valuesParam.add("?");
                    args.add(nowTs);
                    columns.add("version");
                    valuesParam.add("0");
                    row.put("inputby", currentUser);
                    row.put("inputdt", nowTs);
                    row.put("version", 0);

                    columns.add(detailFk);
                    valuesParam.add("?");
                    args.add(sanitizeJdbcValue(detailTableName, detailFk, masterId));

                    String sql = "INSERT INTO " + qDetailTable + " (" + columns.toString() + ") VALUES (" + valuesParam.toString() + ")";
                    org.springframework.jdbc.support.GeneratedKeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
                    jdbcTemplate.update(connection -> {
                        java.sql.PreparedStatement ps = connection.prepareStatement(sql, new String[]{detailPk});
                        for (int i = 0; i < args.size(); i++) {
                            ps.setObject(i + 1, args.get(i));
                        }
                        return ps;
                    }, keyHolder);
                    Object insertedDetailId = keyHolder.getKey();

                    for (FieldMeta field : formMeta.getFields()) {
                        if (field.isDetail() && field.isAuditLog()) {
                            String fieldName = field.getFieldName();
                            if (row.containsKey(fieldName)) {
                                recordFieldAuditLog(formMeta.getFormCode(), detailTableName, insertedDetailId, "INSERT", fieldName, null, row.get(fieldName), currentUser);
                            }
                        }
                    }
                }
            }
        }
    }

    public void ensureMasterDetailTablesExist(FormMeta formMeta) {
        if (formMeta == null) return;
        ensureTableExists(formMeta);

        if ("MASTER_DETAIL".equalsIgnoreCase(formMeta.getFormType())) {
            String detailTableName = formMeta.getDetailTableName();
            String detailPk = formMeta.getDetailPrimaryKey() != null ? formMeta.getDetailPrimaryKey() : "id";
            String detailFk = formMeta.getDetailForeignKey();
            String masterPk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";

            if (detailTableName == null || detailTableName.trim().isEmpty() || detailFk == null || detailFk.trim().isEmpty()) {
                return;
            }

            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS dynamic;");
            String qDetailTable = getQualifiedTableName(detailTableName);

            // 1. Create detail table if not exists
            boolean isDetailTextPk = !detailPk.equalsIgnoreCase("id");
            boolean isFkText = !masterPk.equalsIgnoreCase("id");
            if (formMeta.getFields() != null) {
                for (FieldMeta fm : formMeta.getFields()) {
                    if (fm.getFieldName().equalsIgnoreCase(detailPk) && (fm.getSequenceCode() != null || "TEXTBOX".equalsIgnoreCase(fm.getComponentType()) || "VARCHAR".equalsIgnoreCase(fm.getComponentType()) || "STRING".equalsIgnoreCase(fm.getComponentType()))) {
                        isDetailTextPk = true;
                    }
                    if (fm.getFieldName().equalsIgnoreCase(detailFk) || fm.getFieldName().equalsIgnoreCase(masterPk)) {
                        if (fm.getSequenceCode() != null || "TEXTBOX".equalsIgnoreCase(fm.getComponentType()) || "VARCHAR".equalsIgnoreCase(fm.getComponentType()) || "STRING".equalsIgnoreCase(fm.getComponentType()) || "TEXTFIELD".equalsIgnoreCase(fm.getComponentType())) {
                            isFkText = true;
                        }
                    }
                }
            }
            StringBuilder createSql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            createSql.append(qDetailTable).append(" (");
            createSql.append(detailPk).append(isDetailTextPk ? " VARCHAR(100) PRIMARY KEY, " : " SERIAL PRIMARY KEY, ");
            createSql.append(detailFk).append(isFkText ? " VARCHAR(100) NOT NULL" : " INTEGER NOT NULL");
            createSql.append(")");
            jdbcTemplate.execute(createSql.toString());

            // 2. Add dynamic columns for detail fields
            for (FieldMeta field : formMeta.getFields()) {
                if (!field.isDetail()) continue; // skip master fields
                if (!field.isSaveOnInsert() && !field.isSaveOnUpdate()) continue; // Skip transient columns for detail table
                String colName = field.getFieldName();
                if (colName.equalsIgnoreCase(detailPk) || colName.equalsIgnoreCase(detailFk)) continue;

                String typeDef;
                if ("DECIMALBOX".equalsIgnoreCase(field.getComponentType())) {
                    typeDef = "DECIMAL(19, 2)";
                } else if ("DATEBOX".equalsIgnoreCase(field.getComponentType())) {
                    typeDef = "DATE";
                } else if ("DATETIMEBOX".equalsIgnoreCase(field.getComponentType())) {
                    typeDef = "TIMESTAMP";
                } else if ("TIMEBOX".equalsIgnoreCase(field.getComponentType())) {
                    typeDef = "TIME";
                } else if ("FILE_UPLOAD".equalsIgnoreCase(field.getComponentType()) || "IMAGE_UPLOAD".equalsIgnoreCase(field.getComponentType()) || "TEXTAREA".equalsIgnoreCase(field.getComponentType())) {
                    typeDef = "TEXT";
                } else {
                    typeDef = "VARCHAR(255)";
                }

                String alterSql = "ALTER TABLE " + qDetailTable + " ADD COLUMN IF NOT EXISTS " + colName + " " + typeDef;
                try {
                    jdbcTemplate.execute(alterSql);
                } catch (Exception ignored) {}
            }
            ensureAuditColumnsExist(detailTableName);
        }
    }

    public List<com.vaadinerp.meta.FormActionMeta> getFormActions(String formCode, String targetScope) {
        if (formActionMetaRepository == null || formCode == null) return java.util.Collections.emptyList();
        List<com.vaadinerp.meta.FormActionMeta> result = new ArrayList<>();

        // 1. First check direct actions attached to formCode
        if (targetScope == null) {
            result.addAll(formActionMetaRepository.findByFormMeta_FormCode(formCode));
        } else {
            result.addAll(formActionMetaRepository.findByFormMeta_FormCodeAndTargetScope(formCode, targetScope));
        }

        // 2. Then check if formMeta has assigned actions via extraToolbars catalog selection
        com.vaadinerp.meta.FormMeta formMeta = formMetaRepository.findById(formCode).orElse(null);
        if (formMeta != null && formMeta.getExtraToolbars() != null && !formMeta.getExtraToolbars().trim().isEmpty()) {
            String[] codes = formMeta.getExtraToolbars().split(",");
            for (String c : codes) {
                String cleanCode = c.trim();
                if (!cleanCode.isEmpty()) {
                    com.vaadinerp.meta.FormActionMeta act = formActionMetaRepository.findByActionCode(cleanCode);
                    if (act != null) {
                        if (targetScope == null || targetScope.equalsIgnoreCase(act.getTargetScope())) {
                            if (result.stream().noneMatch(a -> a.getActionCode() != null && a.getActionCode().equalsIgnoreCase(cleanCode))) {
                                result.add(act);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public String evaluateFilterMappingDiagnostic(String filterMapping, Map<String, Object> headerRecord) {
        if (filterMapping == null || filterMapping.trim().isEmpty()) {
            return "Tidak ada filter mapping aktif.";
        }
        StringBuilder sb = new StringBuilder();
        String cleanMapping = filterMapping.trim();
        if (cleanMapping.startsWith("{") && cleanMapping.endsWith("}")) {
            cleanMapping = cleanMapping.substring(1, cleanMapping.length() - 1).trim();
        }
        String[] pairs = cleanMapping.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length < 2) kv = pair.split("=");
            if (kv.length == 2) {
                String col = kv[0].replaceAll("[\"']", "").trim();
                String valSpec = kv[1].trim();
                Object paramVal = null;
                boolean isHeader = false;
                String headerKey = "";
                if (valSpec.startsWith("header.") || valSpec.startsWith("\"header.")) {
                    isHeader = true;
                    headerKey = valSpec.replaceAll("[\"']", "").substring(valSpec.indexOf("header.") + "header.".length()).trim();
                    if (headerRecord != null) {
                        paramVal = getCaseInsensitiveValue(headerRecord, headerKey);
                    }
                } else if (valSpec.startsWith("'") && valSpec.endsWith("'")) {
                    paramVal = valSpec.substring(1, valSpec.length() - 1);
                } else if (valSpec.startsWith("\"") && valSpec.endsWith("\"")) {
                    paramVal = valSpec.substring(1, valSpec.length() - 1);
                } else {
                    try {
                        paramVal = Double.parseDouble(valSpec);
                    } catch (Exception e) {
                        paramVal = valSpec;
                    }
                }
                sb.append("• ").append(col).append(" ➔ ");
                if (isHeader) {
                    if (paramVal != null) {
                        sb.append("OK (ekspresi 'header.").append(headerKey).append("' bernilai: ").append(paramVal).append(")");
                    } else {
                        sb.append("⚠️ KOSONG/NULL (ekspresi 'header.").append(headerKey).append("' tidak ditemukan atau null di form)");
                    }
                } else {
                    sb.append("Literal '").append(paramVal).append("'");
                }
                sb.append("\n");
            }
        }
        if (headerRecord != null && !headerRecord.isEmpty()) {
            sb.append("💡 Field form aktif saat ini: ").append(headerRecord.keySet());
        } else {
            sb.append("💡 Data form saat ini kosong.");
        }
        return sb.toString().trim();
    }

    public List<Map<String, Object>> fetchLovDataWithActionFilters(String sourceLovCode, String filterMapping, Map<String, Object> headerRecord, String searchTerm) {
        if (sourceLovCode == null || sourceLovCode.trim().isEmpty()) return new ArrayList<>();
        System.out.println("=== DEBUG FETCH LOV ACTION FILTERS [" + sourceLovCode + "] ===");
        System.out.println(evaluateFilterMappingDiagnostic(filterMapping, headerRecord));
        LovMeta lovMeta = getLovMeta(sourceLovCode).orElse(null);
        String srcTable = lovMeta != null ? lovMeta.getTableName() : sourceLovCode;
        String searchCol = lovMeta != null ? lovMeta.getSearchColumn() : null;

        StringBuilder sql = new StringBuilder();
        String trimmed = srcTable.trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            sql.append("SELECT * FROM ( ").append(validateAndSanitizeSelectQuery(trimmed)).append(" ) AS subquery");
        } else {
            sql.append("SELECT * FROM ").append(getLovQualifiedTableName(trimmed));
        }

        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (filterMapping != null && !filterMapping.trim().isEmpty()) {
            String cleanMapping = filterMapping.trim();
            if (cleanMapping.startsWith("{") && cleanMapping.endsWith("}")) {
                cleanMapping = cleanMapping.substring(1, cleanMapping.length() - 1).trim();
            }
            String[] pairs = cleanMapping.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length < 2) kv = pair.split("=");
                if (kv.length == 2) {
                    String col = kv[0].replaceAll("[\"']", "").trim();
                    validateSqlIdentifier(col, "action filter column");
                    String valSpec = kv[1].trim();
                    Object paramVal = null;
                    if (valSpec.startsWith("header.") || valSpec.startsWith("\"header.")) {
                        String headerKey = valSpec.replaceAll("[\"']", "").substring(valSpec.indexOf("header.") + "header.".length()).trim();
                        if (headerRecord != null) {
                            paramVal = getCaseInsensitiveValue(headerRecord, headerKey);
                        }
                    } else if (valSpec.startsWith("'") && valSpec.endsWith("'")) {
                        paramVal = valSpec.substring(1, valSpec.length() - 1);
                    } else if (valSpec.startsWith("\"") && valSpec.endsWith("\"")) {
                        paramVal = valSpec.substring(1, valSpec.length() - 1);
                    } else {
                        try {
                            paramVal = Double.parseDouble(valSpec);
                        } catch (Exception e) {
                            paramVal = valSpec;
                        }
                    }
                    if (paramVal != null) {
                        if (paramVal instanceof java.util.Collection<?> colVal) {
                            if (colVal.isEmpty()) {
                                conditions.add("1 = 0");
                            } else {
                                String placeholders = colVal.stream().map(v -> "?").collect(java.util.stream.Collectors.joining(", "));
                                conditions.add(col + " IN (" + placeholders + ")");
                                params.addAll(colVal);
                            }
                        } else {
                            conditions.add(col + " = ?");
                            params.add(paramVal);
                        }
                    } else if (valSpec.startsWith("header.") || valSpec.startsWith("\"header.")) {
                        conditions.add("1 = 0");
                    }
                }
            }
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            if (searchCol == null || searchCol.trim().isEmpty()) {
                List<String> allCols = getColumnsForQueryOrTable(trimmed);
                if (!allCols.isEmpty()) searchCol = String.join(",", allCols);
            }
            if (searchCol != null && !searchCol.trim().isEmpty()) {
                String[] cols = searchCol.split(",");
                java.util.StringJoiner orJoiner = new java.util.StringJoiner(" OR ");
                for (String col : cols) {
                    String safeCol = col.trim();
                    validateSqlIdentifier(safeCol, "search column");
                    orJoiner.add("CAST(" + safeCol + " AS text) ILIKE ?");
                    params.add("%" + searchTerm + "%");
                }
                conditions.add("(" + orJoiner.toString() + ")");
            }
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        sql.append(getLovDefaultOrderByClause(sourceLovCode));
        sql.append(" LIMIT 5000");

        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
