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

    @SuppressWarnings("unused")
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
            binding.setVariable("header", headerBean != null ? prepareHeaderForScript(headerBean) : new HashMap<>());
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
            binding.setVariable("msgBox", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object... args) {
                    if (args != null && args.length == 1) {
                        ctx.msgBox("Message Box", args[0]);
                    } else if (args != null && args.length >= 2) {
                        ctx.msgBox(args[0] != null ? args[0].toString() : "Message Box", args[1]);
                    } else {
                        ctx.msgBox("Message Box", "");
                    }
                }
            });
            binding.setVariable("showMainTab", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object tabId, String tabTitle) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, null, null);
                }
                public void doCall(Object tabId, String tabTitle, Object urlOrExtra) {
                    if (urlOrExtra instanceof Map) {
                        ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, null, urlOrExtra);
                    } else {
                        ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, urlOrExtra != null ? urlOrExtra.toString() : null, null);
                    }
                }
                public void doCall(Object tabId, String tabTitle, Object url, Object extra) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, url != null ? url.toString() : null, extra);
                }
                public void doCall(Map namedArgs, Object tabId, String tabTitle) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, null, namedArgs);
                }
                public void doCall(Map namedArgs, Object tabId, String tabTitle, Object url) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, url != null ? url.toString() : null, namedArgs);
                }
                public void doCall(Map namedArgs, Object tabId, String tabTitle, Object url, Object extra) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, url != null ? url.toString() : null, namedArgs);
                }
                public void doCall(Object... args) {
                    if (args == null || args.length == 0) return;
                    Object extra = null;
                    Object tabId = null;
                    String tabTitle = "";
                    String url = null;
                    for (Object arg : args) {
                        if (arg instanceof Map) {
                            extra = arg;
                        } else if (tabId == null) {
                            tabId = arg;
                        } else if (tabTitle.isEmpty() && arg instanceof String s) {
                            tabTitle = s;
                        } else if (url == null) {
                            url = arg != null ? arg.toString() : null;
                        } else if (extra == null) {
                            extra = arg;
                        }
                    }
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, url, extra);
                }
            });
            binding.setVariable("getElementValue", new groovy.lang.Closure<List<Map<String, Object>>>(null) {
                public List<Map<String, Object>> doCall(String ref, boolean selected) {
                    return ctx.getElementValue(ref, selected);
                }
            });
            binding.setVariable("setElementValue", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object ref, Object val) {
                    ctx.setElementValue(ref, val);
                }
            });
            binding.setVariable("refreshForm", new groovy.lang.Closure<Void>(null) {
                public void doCall() {
                    ctx.refreshForm();
                }
            });
            binding.setVariable("clearForm", new groovy.lang.Closure<Void>(null) {
                public void doCall() {
                    ctx.clearForm();
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

    public static Map<String, Object> prepareHeaderForScript(Map<String, Object> sourceBean) {
        if (sourceBean == null) return new HashMap<>();
        Map<String, SmartHeaderNode> smartNodes = new HashMap<>();
        Map<String, Object> targetBean = new HashMap<>(sourceBean);

        List<Map.Entry<String, Object>> entries = new ArrayList<>(targetBean.entrySet());
        for (Map.Entry<String, Object> entry : entries) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (k == null) continue;

            if (k.contains(".")) {
                String[] parts = k.split("\\.", 2);
                String parentKey = parts[0];
                String subKey = parts[1];

                SmartHeaderNode node = smartNodes.computeIfAbsent(parentKey, pk -> {
                    Object baseVal = targetBean.get(pk);
                    if (baseVal instanceof SmartHeaderNode shn) return shn;
                    return new SmartHeaderNode(baseVal);
                });
                node.putProperty(subKey, v);
                targetBean.put(parentKey, node);

                String underscoreKey = parentKey + "_" + subKey;
                if (!targetBean.containsKey(underscoreKey) || targetBean.get(underscoreKey) == null) {
                    targetBean.put(underscoreKey, v);
                }
            } else if (k.contains("_")) {
                int idx = k.lastIndexOf("_");
                if (idx > 0 && idx < k.length() - 1) {
                    String parentKey = k.substring(0, idx);
                    String subKey = k.substring(idx + 1);
                    SmartHeaderNode node = smartNodes.computeIfAbsent(parentKey, pk -> {
                        Object baseVal = targetBean.get(pk);
                        if (baseVal instanceof SmartHeaderNode shn) return shn;
                        return new SmartHeaderNode(baseVal);
                    });
                    node.putProperty(subKey, v);
                    targetBean.put(parentKey, node);

                    String dotKey = parentKey + "." + subKey;
                    if (!targetBean.containsKey(dotKey) || targetBean.get(dotKey) == null) {
                        targetBean.put(dotKey, v);
                    }
                }
            }
        }

        for (Map.Entry<String, Object> entry : new ArrayList<>(targetBean.entrySet())) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (k != null && !(v instanceof SmartHeaderNode) && !k.contains(".") && !k.contains("_")) {
                targetBean.put(k, new SmartHeaderNode(v));
            }
        }
        return targetBean;
    }

    public static class SmartHeaderNode extends Number implements Map<String, Object>, Comparable<Object>, groovy.lang.GroovyObject {
        private final Object primaryValue;
        private final Map<String, Object> properties = new LinkedHashMap<>();
        private transient groovy.lang.MetaClass metaClass;

        public SmartHeaderNode(Object primaryValue) {
            this.primaryValue = normalizeValue(primaryValue);
            this.metaClass = groovy.lang.GroovySystem.getMetaClassRegistry().getMetaClass(SmartHeaderNode.class);
            if (this.primaryValue != null && !(this.primaryValue instanceof Map) && !(this.primaryValue instanceof SmartHeaderNode)) {
                properties.put("id", this.primaryValue);
                properties.put("value", this.primaryValue);
            }
        }

        private static Object normalizeValue(Object v) {
            if (v instanceof String s) {
                String trim = s.trim();
                if ("true".equalsIgnoreCase(trim)) return Boolean.TRUE;
                if ("false".equalsIgnoreCase(trim)) return Boolean.FALSE;
            }
            return v;
        }

        public void putProperty(String key, Object value) {
            if (key != null) properties.put(key, normalizeValue(value));
        }

        public Object getPrimaryValue() {
            return primaryValue;
        }

        @Override
        public groovy.lang.MetaClass getMetaClass() {
            if (metaClass == null) {
                metaClass = groovy.lang.GroovySystem.getMetaClassRegistry().getMetaClass(SmartHeaderNode.class);
            }
            return metaClass;
        }

        @Override
        public void setMetaClass(groovy.lang.MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        @Override
        public Object getProperty(String property) {
            if (properties.containsKey(property)) {
                return normalizeValue(properties.get(property));
            }
            if ("id".equals(property) || "value".equals(property)) {
                return normalizeValue(primaryValue);
            }
            if (primaryValue != null) {
                try {
                    return groovy.lang.GroovySystem.getMetaClassRegistry().getMetaClass(primaryValue.getClass()).getProperty(primaryValue, property);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public void setProperty(String property, Object newValue) {
            properties.put(property, normalizeValue(newValue));
        }

        public boolean asBoolean() {
            Object val = properties.containsKey("id") ? properties.get("id") : (properties.containsKey("value") ? properties.get("value") : primaryValue);
            val = normalizeValue(val);
            if (val instanceof Boolean b) return b.booleanValue();
            if (val instanceof Number n) return n.doubleValue() != 0;
            if (val instanceof String s) {
                String trim = s.trim();
                return !trim.isEmpty() && !"false".equalsIgnoreCase(trim) && !"0".equals(trim) && !"null".equalsIgnoreCase(trim);
            }
            return val != null && !Boolean.FALSE.equals(val);
        }

        @Override
        public String toString() {
            if (primaryValue != null && !(primaryValue instanceof Map) && !(primaryValue instanceof SmartHeaderNode)) {
                return primaryValue.toString();
            }
            return properties.toString();
        }

        @Override
        public Object invokeMethod(String name, Object args) {
            Object[] argArray = args instanceof Object[] ? (Object[]) args : new Object[]{args};
            if (!metaClass.respondsTo(this, name, argArray).isEmpty()) {
                return metaClass.invokeMethod(this, name, args);
            }
            if (primaryValue != null) {
                return groovy.lang.GroovySystem.getMetaClassRegistry().getMetaClass(primaryValue.getClass()).invokeMethod(primaryValue, name, args);
            }
            throw new groovy.lang.MissingMethodException(name, SmartHeaderNode.class, argArray);
        }

        @Override public int intValue() { return primaryValue instanceof Number n ? n.intValue() : (primaryValue != null && primaryValue.toString().matches("-?\\d+") ? Integer.parseInt(primaryValue.toString()) : 0); }
        @Override public long longValue() { return primaryValue instanceof Number n ? n.longValue() : (primaryValue != null && primaryValue.toString().matches("-?\\d+") ? Long.parseLong(primaryValue.toString()) : 0L); }
        @Override public float floatValue() { return primaryValue instanceof Number n ? n.floatValue() : (primaryValue != null ? Float.parseFloat(primaryValue.toString()) : 0f); }
        @Override public double doubleValue() { return primaryValue instanceof Number n ? n.doubleValue() : (primaryValue != null ? Double.parseDouble(primaryValue.toString()) : 0d); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (primaryValue != null && o != null) {
                if (o instanceof SmartHeaderNode shn) {
                    return Objects.equals(primaryValue, shn.primaryValue);
                }
                if (primaryValue instanceof Number n1 && o instanceof Number n2) {
                    return Double.compare(n1.doubleValue(), n2.doubleValue()) == 0;
                }
                return Objects.equals(primaryValue, o) || Objects.equals(primaryValue.toString(), o.toString());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return primaryValue != null ? primaryValue.hashCode() : super.hashCode();
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public int compareTo(Object o) {
            if (primaryValue instanceof Comparable c1 && o != null) {
                if (o instanceof SmartHeaderNode shn && shn.primaryValue != null) {
                    return c1.compareTo(shn.primaryValue);
                }
                if (c1.getClass().isInstance(o)) {
                    return c1.compareTo(o);
                }
            }
            return 0;
        }

        @Override public int size() { return properties.size(); }
        @Override public boolean isEmpty() { return properties.isEmpty(); }
        @Override public boolean containsKey(Object key) { return properties.containsKey(key); }
        @Override public boolean containsValue(Object value) { return properties.containsValue(value); }
        @Override public Object get(Object key) { return properties.get(key); }
        @Override public Object put(String key, Object value) { return properties.put(key, value); }
        @Override public Object remove(Object key) { return properties.remove(key); }
        @Override public void putAll(Map<? extends String, ?> m) { properties.putAll(m); }
        @Override public void clear() { properties.clear(); }
        @Override public Set<String> keySet() { return properties.keySet(); }
        @Override public Collection<Object> values() { return properties.values(); }
        @Override public Set<Entry<String, Object>> entrySet() { return properties.entrySet(); }
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
                if (!clean.startsWith("SELECT ") && !clean.startsWith("WITH ")) {
                    throw new IllegalArgumentException("Hanya query SELECT atau WITH (CTE) yang diperbolehkan dalam script!");
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
