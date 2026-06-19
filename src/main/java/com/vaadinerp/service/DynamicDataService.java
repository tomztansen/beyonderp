package com.vaadinerp.service;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
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

    public DynamicDataService(JdbcTemplate jdbcTemplate, LovMetaRepository lovMetaRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.lovMetaRepository = lovMetaRepository;
    }

    public String getQualifiedTableName(String tableName) {
        if (tableName == null) return null;
        if (tableName.contains(".")) return tableName;
        return "dynamic." + tableName;
    }

    public String getLovQualifiedTableName(String tableName) {
        if (tableName == null) return null;
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
                "SELECT column_name FROM information_schema.columns WHERE table_schema = 'dynamic' AND table_name = ? ORDER BY ordinal_position",
                String.class,
                rawName.toLowerCase()
            );
        } catch (Exception e) {
            return new ArrayList<>();
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
                "WHERE table_schema = 'dynamic' AND table_name = ? " +
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
                "SELECT trigger_name, action_timing, event_manipulation " +
                "FROM information_schema.triggers " +
                "WHERE event_object_schema = 'dynamic' AND event_object_table = ?",
                rawName.toLowerCase()
            );
        } catch (Exception e) {
            return new ArrayList<>();
        }
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

    public Optional<LovMeta> getLovMeta(String lovCode) {
        if (lovCode == null) return Optional.empty();
        return lovMetaRepository.findById(lovCode);
    }

    @Transactional
    public void saveData(FormMeta formMeta, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data kosong, tidak ada yang disimpan!");
        }

        ensureTableExists(formMeta);

        String pk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";
        boolean isUpdate = data.containsKey(pk) && data.get(pk) != null && !data.get(pk).toString().trim().isEmpty();

        if (isUpdate) {
            // UPDATE: Jika kolom primary key ada nilainya
            StringJoiner setClause = new StringJoiner(", ");
            List<Object> args = new ArrayList<>();

            for (FieldMeta field : formMeta.getFields()) {
                String fieldName = field.getFieldName();
                if (fieldName.equalsIgnoreCase(pk)) continue;
                if (!field.isSaveOnUpdate()) continue;
                if (data.containsKey(fieldName)) {
                    setClause.add(fieldName + " = ?");
                    args.add(data.get(fieldName));
                }
            }

            if (args.isEmpty()) {
                return; // Tidak ada perubahan
            }

            String sql = "UPDATE " + getQualifiedTableName(formMeta.getTableName()) + " SET " + setClause.toString() + " WHERE " + pk + " = ?";
            args.add(data.get(pk)); // Parameter untuk WHERE PK
            jdbcTemplate.update(sql, args.toArray());
        } else {
            // INSERT: Jika primary key tidak ada nilainya
            StringJoiner columns = new StringJoiner(", ");
            StringJoiner valuesParam = new StringJoiner(", ");
            List<Object> args = new ArrayList<>();

            for (FieldMeta field : formMeta.getFields()) {
                String fieldName = field.getFieldName();
                if (fieldName.equalsIgnoreCase(pk)) continue;
                if (!field.isSaveOnInsert()) continue;
                if (data.containsKey(fieldName)) {
                    columns.add(fieldName);
                    valuesParam.add("?");
                    args.add(data.get(fieldName));
                }
            }

            if (args.isEmpty()) {
                throw new IllegalArgumentException("Tidak ada field data valid untuk disimpan.");
            }

            String sql = "INSERT INTO " + getQualifiedTableName(formMeta.getTableName()) + " (" + columns.toString() + ") VALUES (" + valuesParam.toString() + ")";
            jdbcTemplate.update(sql, args.toArray());
        }
    }

    @Transactional
    public void deleteData(FormMeta formMeta, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        String pk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";
        if (data.containsKey(pk) && data.get(pk) != null && !data.get(pk).toString().trim().isEmpty()) {
            if ("MASTER_DETAIL".equalsIgnoreCase(formMeta.getFormType())) {
                String detailTable = formMeta.getDetailTableName();
                String detailFk = formMeta.getDetailForeignKey();
                if (detailTable != null && !detailTable.trim().isEmpty() && detailFk != null && !detailFk.trim().isEmpty()) {
                    try {
                        String sqlDtl = "DELETE FROM " + getQualifiedTableName(detailTable) + " WHERE " + detailFk + " = ?";
                        jdbcTemplate.update(sqlDtl, data.get(pk));
                    } catch (Exception ignored) {}
                }
            }
            String sql = "DELETE FROM " + getQualifiedTableName(formMeta.getTableName()) + " WHERE " + pk + " = ?";
            jdbcTemplate.update(sql, data.get(pk));
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
            if (field.isDetail()) continue; // Skip detail columns for master table
            String colName = field.getFieldName();
            if (colName.equalsIgnoreCase(pk)) continue;

            String typeDef;
            if ("DECIMALBOX".equalsIgnoreCase(field.getComponentType())) {
                typeDef = "DECIMAL(19, 2)";
            } else if ("DATEBOX".equalsIgnoreCase(field.getComponentType())) {
                typeDef = "DATE";
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
    }

    public List<Map<String, Object>> fetchLovData(String lovCode, String searchBy, String searchTerm) {
        if (lovCode == null || lovCode.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Asumsi: lovCode adalah nama view atau nama tabel
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(getLovQualifiedTableName(lovCode));
        List<Object> params = new ArrayList<>();
        
        if (searchBy != null && !searchBy.trim().isEmpty() && searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" WHERE ");
            String[] cols = searchBy.split(",");
            java.util.StringJoiner orJoiner = new java.util.StringJoiner(" OR ");
            for (String col : cols) {
                orJoiner.add(col.trim() + " ILIKE ?");
                params.add("%" + searchTerm + "%");
            }
            sql.append(orJoiner.toString());
        }
        
        sql.append(" LIMIT 50"); // Batasi hasil agar tidak berat
        
        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            // Jika tabel tidak ditemukan, return kosong
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> fetchLovDataWithFilters(String tableName, String searchBy, String searchTerm, Map<String, Object> filters) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(getLovQualifiedTableName(tableName));
        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;
        
        if (filters != null && !filters.isEmpty()) {
            StringJoiner filterJoiner = new StringJoiner(" AND ");
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().toString().trim().isEmpty()) {
                    filterJoiner.add(entry.getKey() + " = ?");
                    params.add(entry.getValue());
                }
            }
            if (filterJoiner.length() > 0) {
                sql.append(" WHERE ").append(filterJoiner.toString());
                hasWhere = true;
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
            StringJoiner orJoiner = new StringJoiner(" OR ");
            for (String col : cols) {
                orJoiner.add(col.trim() + " ILIKE ?");
                params.add("%" + searchTerm + "%");
            }
            sql.append(orJoiner.toString()).append(")");
        }
        
        sql.append(" LIMIT 50");
        
        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Map<String, Object> fetchLovRecord(String tableName, String valueColumn, Object value) {
        if (tableName == null || valueColumn == null || value == null || value.toString().trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT * FROM " + getLovQualifiedTableName(tableName) + " WHERE " + valueColumn + " = ? LIMIT 1";
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, value);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
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
        String sql = "SELECT * FROM " + getQualifiedTableName(detailTableName) + " WHERE " + fkColumn + " = ?";
        try {
            return jdbcTemplate.queryForList(sql, fkValue);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional
    public void saveMasterDetailData(FormMeta formMeta, Map<String, Object> masterData,
                                     List<Map<String, Object>> detailsData,
                                     List<Map<String, Object>> deletedDetailsData) {
        if (masterData == null || masterData.isEmpty()) {
            throw new IllegalArgumentException("Data master kosong!");
        }

        ensureMasterDetailTablesExist(formMeta);

        String masterPk = formMeta.getPrimaryKey() != null ? formMeta.getPrimaryKey() : "id";
        String detailTableName = formMeta.getDetailTableName();
        String detailPk = formMeta.getDetailPrimaryKey() != null ? formMeta.getDetailPrimaryKey() : "id";
        String detailFk = formMeta.getDetailForeignKey();

        if (detailTableName == null || detailTableName.trim().isEmpty() || detailFk == null || detailFk.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama tabel detail dan foreign key harus didefinisikan!");
        }

        boolean isUpdate = masterData.containsKey(masterPk) && masterData.get(masterPk) != null 
                           && !masterData.get(masterPk).toString().trim().isEmpty();

        Object masterId;

        // 1. Save or Update Master Record
        if (isUpdate) {
            masterId = masterData.get(masterPk);
            StringJoiner setClause = new StringJoiner(", ");
            List<Object> args = new ArrayList<>();

            for (FieldMeta field : formMeta.getFields()) {
                if (field.isDetail()) continue; // skip detail columns
                String fieldName = field.getFieldName();
                if (fieldName.equalsIgnoreCase(masterPk)) continue;
                if (!field.isSaveOnUpdate()) continue;
                if (masterData.containsKey(fieldName)) {
                    setClause.add(fieldName + " = ?");
                    args.add(masterData.get(fieldName));
                }
            }

            if (!args.isEmpty()) {
                String sql = "UPDATE " + getQualifiedTableName(formMeta.getTableName()) + " SET " + setClause.toString() + " WHERE " + masterPk + " = ?";
                args.add(masterId);
                jdbcTemplate.update(sql, args.toArray());
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
                if (!field.isSaveOnInsert()) continue;
                if (masterData.containsKey(fieldName)) {
                    columns.add(fieldName);
                    valuesParam.add("?");
                    args.add(masterData.get(fieldName));
                }
            }

            String qMasterTable = getQualifiedTableName(formMeta.getTableName());
            String sql = "INSERT INTO " + qMasterTable + " (" + columns.toString() + ") VALUES (" + valuesParam.toString() + ")";
            org.springframework.jdbc.support.GeneratedKeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
            
            jdbcTemplate.update(connection -> {
                java.sql.PreparedStatement ps = connection.prepareStatement(sql, new String[]{masterPk});
                for (int i = 0; i < args.size(); i++) {
                    ps.setObject(i + 1, args.get(i));
                }
                return ps;
            }, keyHolder);
            
            masterId = keyHolder.getKey();
            masterData.put(masterPk, masterId); // update local bean state
        }

        // 2. Delete removed detail rows
        if (deletedDetailsData != null && !deletedDetailsData.isEmpty()) {
            String qDetailTable = getQualifiedTableName(detailTableName);
            for (Map<String, Object> row : deletedDetailsData) {
                if (row.containsKey(detailPk) && row.get(detailPk) != null && !row.get(detailPk).toString().trim().isEmpty()) {
                    String sql = "DELETE FROM " + qDetailTable + " WHERE " + detailPk + " = ?";
                    jdbcTemplate.update(sql, row.get(detailPk));
                }
            }
        }

        // 3. Save or Update Detail rows
        if (detailsData != null) {
            String qDetailTable = getQualifiedTableName(detailTableName);
            for (Map<String, Object> row : detailsData) {
                row.put(detailFk, masterId);

                boolean isDetailUpdate = row.containsKey(detailPk) && row.get(detailPk) != null 
                                         && !row.get(detailPk).toString().trim().isEmpty();

                if (isDetailUpdate) {
                    // UPDATE detail row
                    StringJoiner setClause = new StringJoiner(", ");
                    List<Object> args = new ArrayList<>();

                    for (FieldMeta field : formMeta.getFields()) {
                        if (!field.isDetail()) continue; // skip master fields
                        String fieldName = field.getFieldName();
                        if (fieldName.equalsIgnoreCase(detailPk)) continue;
                        if (!field.isSaveOnUpdate()) continue;
                        if (row.containsKey(fieldName)) {
                            setClause.add(fieldName + " = ?");
                            args.add(row.get(fieldName));
                        }
                    }
                    setClause.add(detailFk + " = ?");
                    args.add(masterId);

                    if (!args.isEmpty()) {
                        String sql = "UPDATE " + qDetailTable + " SET " + setClause.toString() + " WHERE " + detailPk + " = ?";
                        args.add(row.get(detailPk));
                        jdbcTemplate.update(sql, args.toArray());
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
                        if (!field.isSaveOnInsert()) continue;
                        if (row.containsKey(fieldName)) {
                            columns.add(fieldName);
                            valuesParam.add("?");
                            args.add(row.get(fieldName));
                        }
                    }
                    columns.add(detailFk);
                    valuesParam.add("?");
                    args.add(masterId);

                    String sql = "INSERT INTO " + qDetailTable + " (" + columns.toString() + ") VALUES (" + valuesParam.toString() + ")";
                    jdbcTemplate.update(sql, args.toArray());
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
                String colName = field.getFieldName();
                if (colName.equalsIgnoreCase(detailPk) || colName.equalsIgnoreCase(detailFk)) continue;

                String typeDef;
                if ("DECIMALBOX".equalsIgnoreCase(field.getComponentType())) {
                    typeDef = "DECIMAL(19, 2)";
                } else if ("DATEBOX".equalsIgnoreCase(field.getComponentType())) {
                    typeDef = "DATE";
                } else {
                    typeDef = "VARCHAR(255)";
                }

                String alterSql = "ALTER TABLE " + qDetailTable + " ADD COLUMN IF NOT EXISTS " + colName + " " + typeDef;
                try {
                    jdbcTemplate.execute(alterSql);
                } catch (Exception ignored) {}
            }
        }
    }
}
