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
import java.util.concurrent.*;

@Service
public class ScriptExecutorService {

    private final org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider;
    private final ConcurrentHashMap<String, Class<? extends Script>> scriptCache = new ConcurrentHashMap<>();
    private CompilerConfiguration compilerConfiguration;
    private final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "groovy-script-exec");
        t.setDaemon(true);
        return t;
    });

    public ScriptExecutorService(org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider) {
        this.dataServiceProvider = dataServiceProvider;
        initCompilerConfig();
    }

    private void initCompilerConfig() {
        SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setIndirectImportCheckEnabled(true);
        // Block dangerous imports & receivers
        secure.setDisallowedImports(Arrays.asList(
                "java.lang.System", "java.lang.Runtime", "java.io.File",
                "java.net.*", "java.lang.Thread", "java.lang.ThreadGroup", "java.lang.Process", "java.lang.ProcessBuilder",
                "java.lang.reflect.*", "java.lang.invoke.*", "org.springframework.*", "javax.sql.*", "java.sql.*",
                "groovy.lang.GroovyShell", "groovy.lang.GroovyClassLoader", "groovy.util.Eval", "groovy.lang.MetaClass"
        ));
        secure.setDisallowedReceivers(Arrays.asList(
                "java.lang.System", "java.lang.Runtime", "java.lang.Thread", "java.lang.ThreadGroup", "java.lang.Process", "java.lang.ProcessBuilder",
                "java.lang.Class", "java.lang.ClassLoader", "groovy.lang.GroovyShell", "groovy.lang.GroovyClassLoader",
                "groovy.util.Eval", "groovy.lang.MetaClass", "org.codehaus.groovy.runtime.InvokerHelper", "org.codehaus.groovy.runtime.ProcessGroovyMethods"
        ));
        // Block dangerous reflection & code execution method calls to prevent dynamic typing/reflection bypasses
        secure.addExpressionCheckers(expression -> {
            if (expression instanceof org.codehaus.groovy.ast.expr.MethodCallExpression mce) {
                String methodName = mce.getMethodAsString();
                if (methodName != null && Arrays.asList("getClass", "forName", "invoke", "newInstance", "eval", "execute", "getSystemClassLoader", "getClassLoader",
                        "getConstructor", "getDeclaredConstructor", "getMethod", "getDeclaredMethod", "getField", "getDeclaredField",
                        "exit", "halt", "loadClass", "defineClass", "getMetaClass", "setMetaClass", "invokeMethod",
                        "setProperty", "start", "dump", "inspect").contains(methodName)) {
                    return false;
                }
            }
            return true;
        });


        // Allow all constant literal types (int, boolean, String, BigDecimal, List, Map, etc.)
        // Security is enforced via disallowed imports, receivers, methods, and execution timeout.

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

            Future<?> future = executorService.submit((Runnable) () -> scriptInstance.run());
            try {
                future.get(2, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                future.cancel(true);
                throw new RuntimeException("Script Execution Timeout: exceeded 2 seconds maximum limit.");
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause() != null ? ee.getCause() : ee;
                throw new RuntimeException("Script Error: " + cause.getMessage(), cause);
            }
        } catch (Exception e) {
            System.err.println("Error executing script [" + scriptId + "]: " + e.getMessage());
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Script Error: " + e.getMessage(), e);
        }
    }

    public void executeActionScript(com.vaadinerp.meta.FormActionMeta act,
                                    Map<String, Object> headerBean,
                                    List<Map<String, Object>> selectedGridRows,
                                    com.vaadin.flow.component.Component currentView) {
        if (act == null || act.getScriptContent() == null || act.getScriptContent().isBlank()) {
            return;
        }
        String scriptText = act.getScriptContent().trim();

        ActionContext ctx = new ActionContext(dataServiceProvider.getIfAvailable(), headerBean, selectedGridRows, currentView);

        // Ganti macro @gridtable{...} dengan ctx.getElementValue('...', true)
        scriptText = scriptText.replaceAll("@gridtable\\{([^}]+)\\}", "ctx.getElementValue('$1', true)");
        // Ganti macro @app{userid} dengan ctx.getUserId()
        scriptText = scriptText.replaceAll("@app\\{userid\\}", "ctx.getUserId()");

        // Konversi syntax JS let -> def agar mudah bagi user jika copas JS
        scriptText = scriptText.replaceAll("\\blet\\s+", "def ");

        // Auto-import untuk JSON utilities jika belum di-import oleh user
        if (!scriptText.contains("import groovy.json.")) {
            scriptText = "import groovy.json.JsonOutput;\nimport groovy.json.JsonSlurper;\n" + scriptText;
        }

        // Auto fallback jika user mengetik nama procedure tanpa tanda kutip (misal: executeProcedure(salesordertoproduction, ...))
        if (!scriptText.contains("propertyMissing")) {
            scriptText = scriptText + "\n\ndef propertyMissing(String name) { return name; }\n";
        }

        final String finalScriptText = scriptText;
        String scriptId = "action_" + (act.getId() != null ? act.getId() : act.getActionCode()) + "_" + finalScriptText.hashCode();

        try {
            Class<? extends Script> scriptClass = scriptCache.computeIfAbsent(scriptId, id -> {
                GroovyShell shell = new GroovyShell(compilerConfiguration);
                return shell.parse(finalScriptText).getClass();
            });

            Script scriptInstance = scriptClass.getDeclaredConstructor().newInstance();

            Binding binding = new Binding();
            binding.setVariable("ctx", ctx);
            binding.setVariable("header", headerBean != null ? headerBean : new HashMap<>());
            binding.setVariable("selectedRows", selectedGridRows != null ? selectedGridRows : new ArrayList<>());
            binding.setVariable("db", new DatabaseHelper(dataServiceProvider));
            binding.setVariable("JsonOutput", groovy.json.JsonOutput.class);
            binding.setVariable("JsonSlurper", groovy.json.JsonSlurper.class);

            binding.setVariable("showYesNoDialog", new groovy.lang.Closure<Void>(null) {
                public void doCall(String title, String message, Object callback) {
                    ctx.showYesNoDialog(title, message, callback);
                }
            });
            binding.setVariable("executeProcedure", new groovy.lang.Closure<Boolean>(null) {
                public boolean doCall(Object procRef, Object callbackOrJson, Object... rest) {
                    return ctx.executeProcedure(procRef, callbackOrJson, rest);
                }
            });
            binding.setVariable("showSuccess", new groovy.lang.Closure<Void>(null) {
                public void doCall(String title, String message) {
                    ctx.showSuccess(title, message);
                }
            });
            binding.setVariable("showError", new groovy.lang.Closure<Void>(null) {
                public void doCall(String title, String message) {
                    ctx.showError(title, message);
                }
            });
            binding.setVariable("showMainTab", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object tabId, String tabTitle) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, null, null);
                }
                public void doCall(Object tabId, String tabTitle, String url) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, url, null);
                }
                public void doCall(Object tabId, String tabTitle, String url, String extra) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, url, extra);
                }
            });
            binding.setVariable("getElementValue", new groovy.lang.Closure<List<Map<String, Object>>>(null) {
                public List<Map<String, Object>> doCall(String ref, boolean selected) {
                    return ctx.getElementValue(ref, selected);
                }
            });

            scriptInstance.setBinding(binding);
            scriptInstance.run();
        } catch (Exception e) {
            String cleanMsg = ctx.extractCleanErrorMessage(e);
            System.err.println("Error executing action script [" + act.getActionCode() + "]: " + cleanMsg);
            ctx.showError("Gagal Eksekusi Script (" + act.getActionCode() + ")", "<b>Pesan Error:</b><br/>" + cleanMsg);
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

        // Blokir akses refleksi atau properti internal dari script Groovy
        public Object getDataServiceProvider() {
            throw new SecurityException("Akses ke dataServiceProvider internal ditolak!");
        }

        public Object getJdbcTemplate() {
            throw new SecurityException("Akses langsung ke JdbcTemplate ditolak!");
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

        // Ambil 1 nilai langsung dari SQL query ringan (hanya SELECT murni tanpa chained queries)
        public Object getValue(String sql, Object... args) {
            try {
                if (sql == null || sql.trim().isEmpty()) {
                    return null;
                }
                String clean = sql.trim().toUpperCase();
                if (!clean.startsWith("SELECT ")) {
                    throw new IllegalArgumentException("Hanya query SELECT yang diperbolehkan dalam script!");
                }
                if (clean.contains("INSERT ") || clean.contains("UPDATE ") || clean.contains("DELETE ") ||
                        clean.contains("DROP ") || clean.contains("ALTER ") || clean.contains("TRUNCATE ") ||
                        clean.contains("GRANT ") || clean.contains("REVOKE ") || clean.contains("EXEC ") ||
                        clean.contains("EXECUTE ") || clean.contains("PG_SLEEP") || clean.contains("PG_TERMINATE") ||
                        clean.contains("PG_CANCEL") || clean.contains("DBLINK") || sql.contains(";") ||
                        sql.contains("--") || sql.contains("/*")) {
                    throw new IllegalArgumentException("Query mengandung perintah perusak database atau karakter terlarang!");
                }
                DynamicDataService dataService = dataServiceProvider.getIfAvailable();
                if (dataService == null) return null;
                return dataService.getJdbcTemplate().queryForObject(sql, Object.class, args);
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    throw e;
                }
                return null;
            }
        }
    }
}
