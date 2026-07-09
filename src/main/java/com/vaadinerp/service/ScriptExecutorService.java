package com.vaadinerp.service;

import com.vaadinerp.meta.FieldMeta;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.transform.TimedInterrupt;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScriptExecutorService {

    private final org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider;
    private final ConcurrentHashMap<String, Class<? extends Script>> scriptCache = new ConcurrentHashMap<>();
    private CompilerConfiguration compilerConfiguration;

    public ScriptExecutorService(org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider) {
        this.dataServiceProvider = dataServiceProvider;
        initCompilerConfig();
    }

    private void initCompilerConfig() {
        SecureASTCustomizer secure = new SecureASTCustomizer();
        // Block dangerous imports & receivers
        secure.setDisallowedImports(Arrays.asList(
                "java.lang.System", "java.lang.Runtime", "java.io.File",
                "java.net.*", "java.lang.Thread", "java.lang.reflect.*", "org.springframework.*", "javax.sql.*", "java.sql.*"
        ));
        secure.setDisallowedReceivers(Arrays.asList(
                "java.lang.System", "java.lang.Runtime", "java.lang.Thread", "java.lang.Class"
        ));

        // Allow all constant literal types (int, boolean, String, BigDecimal, List, Map, etc.)
        // Security is enforced via disallowed imports, receivers, and execution timeout.

        compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(secure);

        // Add timeout protection (max 2 seconds)
        try {
            ASTTransformationCustomizer timerCustomizer = new ASTTransformationCustomizer(
                    Collections.singletonMap("value", 2L), TimedInterrupt.class
            );
            compilerConfiguration.addCompilationCustomizers(timerCustomizer);
        } catch (Exception ignored) {}
    }

    public void executeOnAddScript(FieldMeta fieldMeta, Map<String, Object> newRow, int rowIndex,
                                   Map<String, Object> headerData, List<Map<String, Object>> items) {
        if (fieldMeta == null) {
            return;
        }
        String scriptText = fieldMeta.getOnAddScript();
        if (scriptText == null || scriptText.trim().isEmpty()) {
            return;
        }

        executeScript(fieldMeta.getId() != null ? "field_" + fieldMeta.getId() : "temp_" + scriptText.hashCode(),
                scriptText, newRow, rowIndex, headerData, items);
    }

    public void executeScript(String scriptId, String scriptText, Map<String, Object> newRow, int rowIndex,
                              Map<String, Object> headerData, List<Map<String, Object>> items) {
        try {
            Class<? extends Script> scriptClass = scriptCache.computeIfAbsent(scriptId, id -> {
                GroovyShell shell = new GroovyShell(compilerConfiguration);
                return shell.parse(scriptText).getClass();
            });

            Script scriptInstance = scriptClass.getDeclaredConstructor().newInstance();

            Binding binding = new Binding();
            binding.setVariable("row", newRow);
            binding.setVariable("rowIndex", rowIndex);
            binding.setVariable("header", headerData != null ? headerData : new HashMap<>());
            binding.setVariable("form", headerData != null ? headerData : new HashMap<>());
            binding.setVariable("items", items != null ? items : new ArrayList<>());
            binding.setVariable("db", new DatabaseHelper(dataServiceProvider));

            scriptInstance.setBinding(binding);
            scriptInstance.run();
        } catch (Exception e) {
            System.err.println("Error executing script [" + scriptId + "]: " + e.getMessage());
            throw new RuntimeException("Script Error: " + e.getMessage(), e);
        }
    }

    public void clearCache(String scriptId) {
        scriptCache.remove(scriptId);
    }

    public void clearAllCache() {
        scriptCache.clear();
    }

    // Helper class for safe database queries in script
    public static class DatabaseHelper {
        private final org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider;

        public DatabaseHelper(org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider) {
            this.dataServiceProvider = dataServiceProvider;
        }

        // Ambil 1 baris data dari tabel berdasarkan kolom kunci & nilai
        public Map<String, Object> find(String tableName, String keyColumn, Object keyValue) {
            try {
                DynamicDataService dataService = dataServiceProvider.getIfAvailable();
                if (dataService == null) return null;
                return dataService.fetchLovRecord(tableName, keyColumn, keyValue);
            } catch (Exception e) {
                return null;
            }
        }

        // Ambil 1 nilai langsung dari SQL query ringan (hanya SELECT)
        public Object getValue(String sql, Object... args) {
            try {
                if (sql == null || !sql.trim().toUpperCase().startsWith("SELECT ")) {
                    throw new IllegalArgumentException("Hanya query SELECT yang diperbolehkan dalam script!");
                }
                DynamicDataService dataService = dataServiceProvider.getIfAvailable();
                if (dataService == null) return null;
                return dataService.getJdbcTemplate().queryForObject(sql, Object.class, args);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
