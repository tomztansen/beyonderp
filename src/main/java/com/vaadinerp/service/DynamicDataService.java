package com.vaadinerp.service;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.meta.LovMetaRepository;
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

    private final JdbcTemplate jdbcTemplate;
    private final LovMetaRepository lovMetaRepository;
    private final FormMetaRepository formMetaRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.vaadinerp.security.repository.AppUserGridPreferenceRepository userGridPreferenceRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.vaadinerp.security.service.SessionSecurityService securityService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.vaadinerp.meta.FormActionMetaRepository formActionMetaRepository;

    public DynamicDataService(JdbcTemplate jdbcTemplate, LovMetaRepository lovMetaRepository, FormMetaRepository formMetaRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.lovMetaRepository = lovMetaRepository;
        this.formMetaRepository = formMetaRepository;
    }

    public FormMetaRepository getFormMetaRepository() {
        return formMetaRepository;
    }

    public String getQualifiedTableName(String tableName) {
        if (tableName == null) return null;
        if ("global_master".equalsIgnoreCase(tableName.trim())) return "dynamic.global_category";
        if (tableName.contains(".")) return tableName;
        return "dynamic." + tableName;
    }

    public String getLovQualifiedTableName(String tableName) {
        if (tableName == null) return null;
        if ("global_master".equalsIgnoreCase(tableName.trim())) return "dynamic.global_category";
        if (tableName.contains(".")) return tableName;
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'dynamic' AND table_name = ?",
                Integer.class,
                tableName.toLowerCase()
            );
            if (count != null && count > 0) {
                return "dynamic." + tableName;
            }
        } catch (Exception ignored) {}
        return tableName;
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
                String sql = "SELECT * FROM ( " + trimmed + " ) AS subquery LIMIT 1";
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
        try {
            String rawName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;
            return jdbcTemplate.queryForList(
                "SELECT column_name, data_type, is_nullable, column_default " +
                "FROM information_schema.columns " +
                "WHERE table_schema IN ('dynamic', 'public') AND table_name = ? " +
                "ORDER BY ordinal_position",
                rawName.toLowerCase()
            );
        } catch (Exception e) {
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
        } catch (Exception ignored) {}

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
        } catch (Exception ignored) {}

        return "-- Tulis kode PL/pgSQL trigger di sini\nBEGIN\n    RETURN NEW;\nEND;";
    }

    @Transactional
    public void addOrUpdateTableTrigger(String tableName, TriggerDefinition trigger) {
        if (trigger == null || trigger.getTriggerName() == null || trigger.getTriggerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama trigger tidak boleh kosong");
        }
        String qTableName = getQualifiedTableName(tableName);
        String rawTableName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;
        String triggerName = trigger.getTriggerName().trim();
        String functionName = "fn_" + rawTableName + "_" + triggerName;

        StringBuilder funcSql = new StringBuilder();
        funcSql.append("CREATE OR REPLACE FUNCTION dynamic.").append(functionName).append("()\n");
        funcSql.append("RETURNS TRIGGER AS $$\n");
        funcSql.append(trigger.getTriggerBody() != null ? trigger.getTriggerBody() : "").append("\n");
        funcSql.append("$$ LANGUAGE plpgsql;");

        jdbcTemplate.execute(funcSql.toString());

        jdbcTemplate.execute("DROP TRIGGER IF EXISTS " + triggerName + " ON " + qTableName);

        if (trigger.getEvents() != null && !trigger.getEvents().isEmpty()) {
            String eventStr = String.join(" OR ", trigger.getEvents());
            StringBuilder triggerSql = new StringBuilder();
            triggerSql.append("CREATE TRIGGER ").append(triggerName).append("\n");
            triggerSql.append(trigger.getTiming()).append(" ").append(eventStr).append("\n");
            triggerSql.append("ON ").append(qTableName).append("\n");
            triggerSql.append("FOR EACH ROW EXECUTE FUNCTION dynamic.").append(functionName).append("();");

            jdbcTemplate.execute(triggerSql.toString());
        }
    }

    @Transactional
    public void dropTableTrigger(String tableName, String triggerName) {
        String qTableName = getQualifiedTableName(tableName);
        String rawTableName = tableName.contains(".") ? tableName.substring(tableName.indexOf(".") + 1) : tableName;
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
        String qTableName = getQualifiedTableName(tableName);
        StringBuilder sql = new StringBuilder("ALTER TABLE ").append(qTableName).append(" ADD CONSTRAINT ").append(constraintName).append(" ");
        
        if ("FOREIGN KEY".equalsIgnoreCase(type)) {
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
        String qTableName = getQualifiedTableName(tableName);
        String sql = "ALTER TABLE " + qTableName + " DROP CONSTRAINT " + constraintName;
        jdbcTemplate.execute(sql);
    }

    @Transactional
    public void addTableColumn(String tableName, String columnName, String dataType, boolean nullable, String defaultVal) {
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

    private void executeAndLogSql(String sql, List<Object> args) {
        System.out.println("=================================================");
        System.out.println("EXECUTING SQL: " + sql);
        System.out.println("PARAMETERS: " + args);
        if (args != null) {
            List<String> types = args.stream().map(o -> o == null ? "NULL" : o.getClass().getSimpleName() + "(" + o + ")").toList();
            System.out.println("PARAM TYPES: " + types);
        }
        System.out.println("=================================================");
        try {
            if (args != null) {
                jdbcTemplate.update(sql, args.toArray());
            } else {
                jdbcTemplate.update(sql);
            }
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
            } catch (Exception ignored) {}
        }

        String pk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";
        pk = resolveExistingColumn(formMeta.getTableName(), pk);
        final String finalPk = pk;
        boolean isUpdate = data.containsKey(pk) && data.get(pk) != null && !data.get(pk).toString().trim().isEmpty();

        ensureAuditColumnsExist(formMeta.getTableName());

        Object masterId;

        if (isUpdate) {
            masterId = data.get(pk);
            Map<String, Object> oldRecord = null;
            try {
                String selectSql = "SELECT * FROM " + getQualifiedTableName(formMeta.getTableName()) + " WHERE CAST(" + pk + " AS text) = ?";
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, masterId != null ? masterId.toString().trim() : "");
                if (!rows.isEmpty()) {
                    oldRecord = rows.get(0);
                }
            } catch (Exception ignored) {}

            // UPDATE: Jika kolom primary key ada nilainya
            StringJoiner setClause = new StringJoiner(", ");
            List<Object> args = new ArrayList<>();

            for (FieldMeta field : formMeta.getFields()) {
                String fieldName = field.getFieldName();
                if (fieldName.equalsIgnoreCase(pk)) continue;
                if (fieldName.equalsIgnoreCase("inputby") || fieldName.equalsIgnoreCase("inputdt") ||
                    fieldName.equalsIgnoreCase("updateby") || fieldName.equalsIgnoreCase("updatedt")) continue;
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
            data.put("updateby", currentUser);
            data.put("updatedt", nowTs);

            if (!args.isEmpty()) {
                String sql = "UPDATE " + getQualifiedTableName(formMeta.getTableName()) + " SET " + setClause.toString() + " WHERE CAST(" + pk + " AS text) = ?";
                args.add(data.get(pk) != null ? data.get(pk).toString().trim() : ""); // Parameter untuk WHERE PK
                executeAndLogSql(sql, args);
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
            StringJoiner columns = new StringJoiner(", ");
            StringJoiner valuesParam = new StringJoiner(", ");
            List<Object> args = new ArrayList<>();

            for (FieldMeta field : formMeta.getFields()) {
                String fieldName = field.getFieldName();
                if (fieldName.equalsIgnoreCase(pk)) continue;
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
            data.put("inputby", currentUser);
            data.put("inputdt", nowTs);

            if (args.isEmpty()) {
                throw new IllegalArgumentException("Tidak ada field data valid untuk disimpan.");
            }

            String sql = "INSERT INTO " + getQualifiedTableName(formMeta.getTableName()) + " (" + columns.toString() + ") VALUES (" + valuesParam.toString() + ")";
            org.springframework.jdbc.support.GeneratedKeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
            
            System.out.println("=================================================");
            System.out.println("EXECUTING INSERT SQL: " + sql);
            System.out.println("PARAMETERS: " + args);
            System.out.println("=================================================");
            jdbcTemplate.update(connection -> {
                java.sql.PreparedStatement ps = connection.prepareStatement(sql, new String[]{finalPk});
                for (int i = 0; i < args.size(); i++) {
                    ps.setObject(i + 1, args.get(i));
                }
                return ps;
            }, keyHolder);
            
            masterId = keyHolder.getKey();
            data.put(pk, masterId); // Update key in data map

            for (FieldMeta field : formMeta.getFields()) {
                if (field.isAuditLog()) {
                    String fieldName = field.getFieldName();
                    if (data.containsKey(fieldName)) {
                        recordFieldAuditLog(formMeta.getFormCode(), formMeta.getTableName(), masterId, "INSERT", fieldName, null, data.get(fieldName), currentUser);
                    }
                }
            }
        }

        // Cascade save for subform grids
        for (FieldMeta field : formMeta.getFields()) {
            if ("SUBFORM_GRID".equalsIgnoreCase(field.getComponentType())) {
                String subformFieldName = field.getFieldName();
                
                // Get child FormMeta
                FormMeta childFormMeta = formMetaRepository.findById(field.getLovCode()).orElse(null);
                if (childFormMeta == null) continue;
                
                // Ensure child physical table exists
                generatePhysicalTable(childFormMeta);
                
                // 1. Delete removed rows
                String deletedKey = "__deleted_" + subformFieldName;
                if (data.containsKey(deletedKey) && data.get(deletedKey) instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> deletedRows = (List<Map<String, Object>>) data.get(deletedKey);
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
            } catch (Exception ignored) {}
            return map;
        });
        return colTypes.getOrDefault(columnName.toLowerCase(), "");
    }

    private Object sanitizeJdbcValue(Object val) {
        return sanitizeJdbcValue(null, null, val);
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
                    try { return Integer.valueOf(trimmedStr); } catch (Exception ignored) {}
                } else if ("bigint".equals(colType) || "bigserial".equals(colType)) {
                    try { return Long.valueOf(trimmedStr); } catch (Exception ignored) {}
                } else if ("numeric".equals(colType) || "decimal".equals(colType) || "real".equals(colType) || "double precision".equals(colType)) {
                    try { return new java.math.BigDecimal(trimmedStr); } catch (Exception ignored) {}
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
                    } catch (Exception ignored) {}
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
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }

            String sql = "DELETE FROM " + getQualifiedTableName(formMeta.getTableName()) + " WHERE CAST(" + pk + " AS text) = ?";
            jdbcTemplate.update(sql, data.get(pk) != null ? data.get(pk).toString().trim() : "");
        }
    }

    public void generatePhysicalTable(FormMeta formMeta) {
        if ("MASTER_DETAIL".equalsIgnoreCase(formMeta.getFormType())) {
            ensureMasterDetailTablesExist(formMeta);
        } else {
            ensureTableExists(formMeta);
        }
    }

    private void ensureTableExists(FormMeta formMeta) {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS dynamic;");
        String tableName = getQualifiedTableName(formMeta.getTableName());
        String pk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";
        
        // 1. Buat tabel minimal jika belum ada
        StringBuilder createSql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        createSql.append(tableName).append(" (");
        createSql.append(pk).append(" SERIAL PRIMARY KEY");
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
        } catch (Exception ignored) {}
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
        } catch (Exception ignored) {}
        return "SYSTEM";
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
        } catch (Exception ignored) {}
    }

    public void recordFieldAuditLog(String formCode, String tableName, Object recordId, String actionType, String fieldName, Object oldValue, Object newValue, String actionBy) {
        ensureFieldAuditTableExists();
        String oldStr = oldValue != null ? oldValue.toString() : null;
        String newStr = newValue != null ? newValue.toString() : null;
        if (oldStr != null && oldStr.equals(newStr)) return;
        try {
            String sql = "INSERT INTO sys_field_audit (form_code, table_name, record_id, action_type, field_name, old_value, new_value, action_by, action_dt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, formCode, tableName, recordId != null ? recordId.toString() : "", actionType, fieldName, oldStr, newStr, actionBy, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
        } catch (Exception ignored) {}
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
        return null;
    }

    public List<Map<String, Object>> fetchLovData(String lovCode, String searchBy, String searchTerm) {
        if (lovCode == null || lovCode.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder sql = new StringBuilder();
        String trimmed = lovCode.trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            sql.append("SELECT * FROM ( ").append(trimmed).append(" ) AS subquery");
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
                orJoiner.add("CAST(" + col.trim() + " AS text) ILIKE ?");
                params.add("%" + searchTerm + "%");
            }
            sql.append(orJoiner.toString());
        }
        
        sql.append(" LIMIT 50"); // Batasi hasil agar tidak berat
        
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
            sql.append("SELECT * FROM ( ").append(trimmed).append(" ) AS subquery");
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
                        String logOp = condition.getLogicalOperator() != null ? condition.getLogicalOperator().toUpperCase() : "AND";
                        filterBuilder.append(" ").append(logOp).append(" ");
                    }
                    isFirst = false;
                    
                    String compOp = condition.getComparisonOperator() != null ? condition.getComparisonOperator().toUpperCase() : "=";
                    
                    if ("LIKE".equals(compOp) || "ILIKE".equals(compOp)) {
                        filterBuilder.append("CAST(").append(condition.getFilterColumn()).append(" AS text) ").append(compOp).append(" ?");
                        params.add("%" + condition.getValue() + "%");
                    } else {
                        filterBuilder.append("CAST(").append(condition.getFilterColumn()).append(" AS text) ").append(compOp).append(" ?");
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
                orJoiner.add("CAST(" + col.trim() + " AS text) ILIKE ?");
                params.add("%" + searchTerm + "%");
            }
            sql.append(orJoiner.toString()).append(")");
        }
        
        sql.append(" LIMIT 50");
        
        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
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
            sql.append("SELECT * FROM ( ").append(trimmed).append(" ) AS subquery WHERE CAST(").append(valueColumn).append(" AS text) = ? LIMIT 1");
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
        String sql;
        if (trimmed.toLowerCase().startsWith("select")) {
            sql = "SELECT * FROM ( " + trimmed + " ) AS subquery LIMIT 3000";
        } else {
            sql = "SELECT * FROM " + getLovQualifiedTableName(trimmed) + " LIMIT 3000";
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
        String sql = "SELECT * FROM " + getQualifiedTableName(tableName);
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
                    return jdbcTemplate.queryForList(trimmed);
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

            // Build CREATE TRIGGER SQL
            String eventStr = String.join(" OR ", trigger.getEvents());
            
            StringBuilder triggerSql = new StringBuilder();
            triggerSql.append("CREATE TRIGGER ").append(triggerName).append("\n");
            triggerSql.append(trigger.getTiming()).append(" ").append(eventStr).append("\n");
            triggerSql.append("ON ").append(qTableName).append("\n");
            triggerSql.append("FOR EACH ROW EXECUTE FUNCTION dynamic.").append(functionName).append("();");

            jdbcTemplate.execute(triggerSql.toString());
        }
    }

    public List<Map<String, Object>> fetchDetailTableData(String detailTableName, String fkColumn, Object fkValue) {
        if (detailTableName == null || fkColumn == null || fkValue == null) {
            return new ArrayList<>();
        }
        fkColumn = resolveExistingColumn(detailTableName, fkColumn);
        String sql = "SELECT * FROM " + getQualifiedTableName(detailTableName) + " WHERE CAST(" + fkColumn + " AS text) = ?";
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

        Object masterId;

        // 1. Save or Update Master Record
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
                    fieldName.equalsIgnoreCase("updateby") || fieldName.equalsIgnoreCase("updatedt")) continue;
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
            masterData.put("updateby", currentUser);
            masterData.put("updatedt", nowTs);

            if (!args.isEmpty()) {
                String sql = "UPDATE " + getQualifiedTableName(formMeta.getTableName()) + " SET " + setClause.toString() + " WHERE CAST(" + masterPk + " AS text) = ?";
                args.add(masterId != null ? masterId.toString().trim() : "");
                executeAndLogSql(sql, args);
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
            masterData.put("inputby", currentUser);
            masterData.put("inputdt", nowTs);

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
                            fieldName.equalsIgnoreCase("updateby") || fieldName.equalsIgnoreCase("updatedt")) continue;
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
                    row.put("updateby", currentUser);
                    row.put("updatedt", nowTs);

                    setClause.add(detailFk + " = ?");
                    args.add(sanitizeJdbcValue(detailTableName, detailFk, masterId));

                    if (!args.isEmpty()) {
                        String sql = "UPDATE " + qDetailTable + " SET " + setClause.toString() + " WHERE CAST(" + detailPk + " AS text) = ?";
                        args.add(row.get(detailPk) != null ? row.get(detailPk).toString().trim() : "");
                        executeAndLogSql(sql, args);
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
                        if (fieldName.equalsIgnoreCase(detailPk)) continue;
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
                    row.put("inputby", currentUser);
                    row.put("inputdt", nowTs);

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
        ensureTableExists(formMeta);

        if ("MASTER_DETAIL".equalsIgnoreCase(formMeta.getFormType())) {
            String detailTableName = formMeta.getDetailTableName();
            String detailPk = formMeta.getDetailPrimaryKey() != null ? formMeta.getDetailPrimaryKey() : "id";
            String detailFk = formMeta.getDetailForeignKey();

            if (detailTableName == null || detailTableName.trim().isEmpty() || detailFk == null || detailFk.trim().isEmpty()) {
                return;
            }

            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS dynamic;");
            String qDetailTable = getQualifiedTableName(detailTableName);

            // 1. Create detail table if not exists
            StringBuilder createSql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            createSql.append(qDetailTable).append(" (");
            createSql.append(detailPk).append(" SERIAL PRIMARY KEY, ");
            createSql.append(detailFk).append(" INTEGER NOT NULL");
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
        if (targetScope == null) {
            return formActionMetaRepository.findByFormMeta_FormCode(formCode);
        }
        return formActionMetaRepository.findByFormMeta_FormCodeAndTargetScope(formCode, targetScope);
    }

    public List<Map<String, Object>> fetchLovDataWithActionFilters(String sourceLovCode, String filterMapping, Map<String, Object> headerRecord, String searchTerm) {
        if (sourceLovCode == null || sourceLovCode.trim().isEmpty()) return new ArrayList<>();
        LovMeta lovMeta = getLovMeta(sourceLovCode).orElse(null);
        String srcTable = lovMeta != null ? lovMeta.getTableName() : sourceLovCode;
        String searchCol = lovMeta != null ? lovMeta.getSearchColumn() : null;

        StringBuilder sql = new StringBuilder();
        String trimmed = srcTable.trim();
        if (trimmed.toLowerCase().startsWith("select")) {
            sql.append("SELECT * FROM ( ").append(trimmed).append(" ) AS subquery");
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
                        conditions.add(col + " = ?");
                        params.add(paramVal);
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
                    orJoiner.add("CAST(" + col.trim() + " AS text) ILIKE ?");
                    params.add("%" + searchTerm + "%");
                }
                conditions.add("(" + orJoiner.toString() + ")");
            }
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        sql.append(" LIMIT 100");

        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
