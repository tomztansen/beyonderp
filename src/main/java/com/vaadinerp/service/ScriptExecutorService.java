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

@Service
public class ScriptExecutorService {

    private final org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider;
    private final org.springframework.beans.factory.ObjectProvider<com.vaadinerp.security.service.LoginHistoryService> loginHistoryProvider;
    private final org.springframework.beans.factory.ObjectProvider<FileStorageService> fileStorageServiceProvider;
    private final org.springframework.beans.factory.ObjectProvider<EmailOutboxService> emailOutboxServiceProvider;
    private final org.springframework.beans.factory.ObjectProvider<WhatsAppOutboxService> whatsAppOutboxServiceProvider;
    private final com.github.benmanes.caffeine.cache.Cache<String, Class<? extends Script>> scriptCache = com.github.benmanes.caffeine.cache.Caffeine
            .newBuilder()
            .maximumSize(500)
            .build();

    private CompilerConfiguration compilerConfiguration;
    private CompilerConfiguration jobCompilerConfiguration;

    public ScriptExecutorService(
            org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider,
            org.springframework.beans.factory.ObjectProvider<com.vaadinerp.security.service.LoginHistoryService> loginHistoryProvider,
            org.springframework.beans.factory.ObjectProvider<FileStorageService> fileStorageServiceProvider,
            org.springframework.beans.factory.ObjectProvider<EmailOutboxService> emailOutboxServiceProvider,
            org.springframework.beans.factory.ObjectProvider<WhatsAppOutboxService> whatsAppOutboxServiceProvider) {
        this.dataServiceProvider = dataServiceProvider;
        this.loginHistoryProvider = loginHistoryProvider;
        this.fileStorageServiceProvider = fileStorageServiceProvider;
        this.emailOutboxServiceProvider = emailOutboxServiceProvider;
        this.whatsAppOutboxServiceProvider = whatsAppOutboxServiceProvider;
        initCompilerConfig();
    }

    /**
     * Binding 'renderReport' (action scope): render report Jasper/Standard ke file di
     * report_out/ dengan nama tetap (nama sama = ditimpa), TANPA before/after script report.
     * Hasilnya path relatif, siap jadi attachment sendEmail(). Bean diambil lewat
     * SpringContextHolder saat dipanggil -- ReportRunService sendiri bergantung ke service
     * ini (before/after script), jadi inject langsung akan jadi dependensi melingkar.
     * Overload beda jumlah argumen (2/3/4), tidak ambigu di Groovy.
     * Parameter diisi dengan urutan yang sama seperti Report Runner: SYSTEM ($CURRENT_USER,
     * CURRENT_DATE) dulu, lalu nilai dari script (menimpa), lalu default Designer untuk yang
     * masih belum ada.
     */
    private groovy.lang.Closure<String> buildRenderReportClosure(ActionContext ctx) {
        return new groovy.lang.Closure<String>(null) {
            @SuppressWarnings("unused")
            public String doCall(Object reportCode, Map<?, ?> params) {
                return doCall(reportCode, params, "PDF", null);
            }

            @SuppressWarnings("unused")
            public String doCall(Object reportCode, Map<?, ?> params, Object format) {
                return doCall(reportCode, params, format, null);
            }

            public String doCall(Object reportCode, Map<?, ?> params, Object format, Object fileName) {
                String code = reportCode != null ? reportCode.toString().trim() : "";
                com.vaadinerp.meta.ReportMeta report = com.vaadinerp.config.SpringContextHolder
                        .getBean(com.vaadinerp.meta.ReportMetaRepository.class).findById(code)
                        .orElseThrow(() -> new IllegalArgumentException("Report not found: " + code));

                String fmt = format != null ? format.toString().trim().toUpperCase() : "PDF";
                if ("EXCEL".equals(fmt)) {
                    fmt = "XLSX";
                }
                if (!"PDF".equals(fmt) && !"XLSX".equals(fmt)) {
                    throw new IllegalArgumentException("renderReport format must be PDF or XLSX, got: " + format);
                }

                Map<String, Object> cleanParams = new HashMap<>(com.vaadinerp.report.ReportParamResolver
                        .resolveAuto(report.getParams(), Map.of(), ctx.getUserId()));
                // header.x di script berupa SmartHeaderNode/LovValueNode -- JDBC/Jasper butuh nilai aslinya.
                if (params != null) {
                    params.forEach((k, v) -> {
                        Object val = v;
                        if (val instanceof SmartHeaderNode shn) {
                            val = shn.getPrimaryValue();
                        } else if (val instanceof LovValueNode lvn) {
                            val = lvn.getPrimaryValue();
                        }
                        cleanParams.put(String.valueOf(k), val);
                    });
                }
                if (report.getParams() != null) {
                    for (com.vaadinerp.meta.ReportParamMeta p : report.getParams()) {
                        cleanParams.putIfAbsent(p.getParamName(), p.getDefaultValue());
                    }
                }

                com.vaadinerp.report.render.ReportOutput out = com.vaadinerp.config.SpringContextHolder
                        .getBean(com.vaadinerp.report.ReportRunService.class)
                        .renderWithoutScripts(report, cleanParams, fmt);
                String baseName = fileName != null && !fileName.toString().isBlank() ? fileName.toString() : code;
                return com.vaadinerp.config.SpringContextHolder.getBean(FileStorageService.class)
                        .storeReportOutput(out.bytes(), baseName, "XLSX".equals(fmt) ? "xlsx" : "pdf");
            }
        };
    }

    /**
     * Binding 'sendEmail' untuk script (row & action scope). Cuma INSERT ke
     * antrean (lihat EmailOutboxService.queueEmail) -- pengiriman sungguhan
     * dikerjakan worker terjadwal terpisah, tidak pernah menahan script/UI
     * menunggu SMTP.
     *
     * Signature tetap 5 argumen: sendEmail(to, cc, subject, body, attachments).
     * Isi null/"" untuk cc atau attachments kalau tidak dipakai -- sengaja
     * tidak dibuat overload/varargs supaya tidak ambigu di Groovy.
     */
    private groovy.lang.Closure<Void> buildSendEmailClosure(ActionContext ctx) {
        return new groovy.lang.Closure<Void>(null) {
            @SuppressWarnings("unused")
            public void doCall(Object to, Object cc, Object subject, Object htmlBody, Object attachments) {
                EmailOutboxService svc = emailOutboxServiceProvider.getIfAvailable();
                if (svc == null || to == null) {
                    return;
                }
                svc.queueEmail(to.toString(),
                        cc != null ? cc.toString() : null,
                        subject != null ? subject.toString() : "",
                        htmlBody != null ? htmlBody.toString() : "",
                        attachments != null ? attachments.toString() : null,
                        ctx != null ? ctx.getUserId() : "script");
            }
        };
    }

    /**
     * Binding 'sendWhatsApp' -- antre notifikasi WhatsApp biasa, tanpa approval.
     * Signature tetap 3 argumen: sendWhatsApp(chatId, message, sessionId).
     * sessionId isi null untuk pakai default app.whatsapp.session-id, atau
     * nama sesi OpenWA (dashboard OpenWA > Sessions) untuk pilih nomor lain.
     */
    private groovy.lang.Closure<Void> buildSendWhatsAppClosure(ActionContext ctx) {
        return new groovy.lang.Closure<Void>(null) {
            @SuppressWarnings("unused")
            public void doCall(Object chatId, Object message, Object sessionId) {
                WhatsAppOutboxService svc = whatsAppOutboxServiceProvider.getIfAvailable();
                if (svc == null || chatId == null) {
                    return;
                }
                svc.queueMessage(chatId.toString(), message != null ? message.toString() : "",
                        sessionId != null ? sessionId.toString() : null,
                        ctx != null ? ctx.getUserId() : "script");
            }
        };
    }

    /**
     * Binding 'sendWhatsAppApproval' -- antre permintaan approval lewat WhatsApp.
     * Balasan "APPROVE"/"OK"/"YA" dari nomor itu memanggil procName(procParams,
     * userId) lewat WhatsAppInboxService saat webhook masuk; balasan
     * "REJECT"/"TOLAK" cuma menutup permintaannya tanpa memanggil apa pun.
     * Signature tetap 5 argumen: sendWhatsAppApproval(chatId, message, procName,
     * jsonParams, sessionId). sessionId isi null untuk pakai default.
     */
    private groovy.lang.Closure<Void> buildSendWhatsAppApprovalClosure(ActionContext ctx) {
        return new groovy.lang.Closure<Void>(null) {
            @SuppressWarnings("unused")
            public void doCall(Object chatId, Object message, Object procName, Object jsonParams, Object sessionId) {
                WhatsAppOutboxService svc = whatsAppOutboxServiceProvider.getIfAvailable();
                if (svc == null || chatId == null || procName == null) {
                    return;
                }
                svc.queueApprovalRequest(chatId.toString(), message != null ? message.toString() : "",
                        procName.toString(), jsonParams != null ? jsonParams.toString() : "{}",
                        sessionId != null ? sessionId.toString() : null,
                        ctx != null ? ctx.getUserId() : "script");
            }
        };
    }

    /**
     * Path folder upload SEMENTARA sebagai String, atau "" kalau service belum
     * tersedia -- dipakai untuk binding 'uploadDir' di script (bukan Object besar,
     * cuma teks, tidak ada risiko memory).
     *
     * Sengaja folder sementara (bukan folder permanen): ON_CHANGE pada field
     * FILE_UPLOAD selalu terpicu SEBELUM form disimpan, dan file yang baru
     * diupload masih ada di folder sementara sampai saveData() memindahkannya
     * (lihat FileStorageService.promoteToPermanent()).
     */
    private String resolveUploadDir() {
        FileStorageService fs = fileStorageServiceProvider.getIfAvailable();
        if (fs == null || fs.getTempUploadDir() == null) {
            return "";
        }
        return fs.getTempUploadDir().toString();
    }

    private void initCompilerConfig() {
        compilerConfiguration = buildCompilerConfig(60L);
        jobCompilerConfiguration = buildCompilerConfig(JOB_TIMEOUT_SECONDS);
    }

    private CompilerConfiguration buildCompilerConfig(long timeoutSeconds) {
        SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setIndirectImportCheckEnabled(true);
        // Block dangerous imports & receivers
        secure.setDisallowedImports(Arrays.asList(
                "java.lang.System", "java.lang.Runtime", "java.io.File",
                "java.net.*", "java.lang.Thread", "java.lang.ThreadGroup", "java.lang.Process",
                "java.lang.ProcessBuilder",
                "java.lang.reflect.*", "java.lang.invoke.*", "org.springframework.*", "javax.sql.*", "java.sql.*",
                "groovy.lang.GroovyShell", "groovy.lang.GroovyClassLoader", "groovy.util.Eval",
                "groovy.lang.MetaClass"));
        secure.setDisallowedReceivers(Arrays.asList(
                "java.lang.System", "java.lang.Runtime", "java.lang.Thread", "java.lang.ThreadGroup",
                "java.lang.Process", "java.lang.ProcessBuilder",
                "java.lang.Class", "java.lang.ClassLoader", "groovy.lang.GroovyShell", "groovy.lang.GroovyClassLoader",
                "groovy.util.Eval", "groovy.lang.MetaClass", "org.codehaus.groovy.runtime.InvokerHelper",
                "org.codehaus.groovy.runtime.ProcessGroovyMethods"));
        // Block dangerous reflection & code execution method calls to prevent dynamic
        // typing/reflection bypasses
        secure.addExpressionCheckers(expression -> {
            if (expression instanceof org.codehaus.groovy.ast.expr.MethodCallExpression mce) {
                String methodName = mce.getMethodAsString();
                if (methodName != null && Arrays.asList("getClass", "forName", "invoke", "newInstance", "eval",
                        "execute", "getSystemClassLoader", "getClassLoader",
                        "getConstructor", "getDeclaredConstructor", "getMethod", "getDeclaredMethod", "getField",
                        "getDeclaredField",
                        "exit", "halt", "loadClass", "defineClass", "getMetaClass", "setMetaClass", "invokeMethod",
                        "setProperty", "start", "dump", "inspect").contains(methodName)) {
                    return false;
                }
            }
            return true;
        });

        // Allow all constant literal types (int, boolean, String, BigDecimal, List,
        // Map, etc.)
        // Security is enforced via disallowed imports, receivers, methods, and
        // execution timeout.

        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(secure);

        // Add timeout protection
        try {
            ASTTransformationCustomizer timerCustomizer = new ASTTransformationCustomizer(
                    Collections.singletonMap("value", timeoutSeconds), TimedInterrupt.class);
            config.addCompilationCustomizers(timerCustomizer);
        } catch (Exception ignored) {
        }
        return config;
    }

    public void executeOnAddScript(FieldMeta fieldMeta, Map<String, Object> newRow, int rowIndex,
            Map<String, Object> headerData, List<Map<String, Object>> items,
            com.vaadin.flow.component.Component currentView) {
        if (fieldMeta == null) {
            return;
        }
        String scriptText = fieldMeta.getOnAddScript();
        if (scriptText == null || scriptText.trim().isEmpty()) {
            return;
        }

        executeScript(
                fieldMeta.getId() != null ? "field_" + fieldMeta.getId() + "_" + scriptText.hashCode()
                        : "temp_" + scriptText.hashCode(),
                scriptText, newRow, rowIndex, headerData, items, currentView);
    }

    /**
     * Ekspresi LOV Switch mode Script. Binding hanya header & row -- sengaja tanpa db/ctx:
     * ini dijalankan sekali per baris setiap grid dirender, jadi query di sini = N+1.
     * @return kode LOV hasil script, atau null (kosong) = pakai LOV default field.
     */
    public String evaluateLovSwitchScript(String scriptText, Map<String, Object> header, Map<String, Object> row)
            throws Exception {
        Script script = compileLovSwitchScript(scriptText).getDeclaredConstructor().newInstance();
        Binding binding = new Binding();
        binding.setVariable("header", normalizeForLovScript(header));
        binding.setVariable("row", normalizeForLovScript(row));
        script.setBinding(binding);
        Object result = script.run();
        return result != null && !result.toString().isBlank() ? result.toString().trim() : null;
    }

    /** Validasi sintaks untuk Form Builder. @return pesan error, atau null kalau bisa dikompilasi. */
    public String checkLovSwitchScript(String scriptText) {
        try {
            compileLovSwitchScript(scriptText);
            return null;
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private Class<? extends Script> compileLovSwitchScript(String scriptText) {
        // Teks lengkap sebagai kunci (bukan hashCode) supaya dua script berbeda tidak bisa bertabrakan.
        return scriptCache.get("lovswitch:" + scriptText,
                id -> new GroovyShell(compilerConfiguration).parse(scriptText).getClass());
    }

    /**
     * Salinan dengan kunci huruf kecil, dan tanggal dari JDBC disamakan dengan nilai dari
     * komponen (LocalDate/LocalDateTime) -- tanpa ini tipe header.tanggal beda antara record
     * yang baru dimuat dan yang baru diubah user.
     */
    private static Map<String, Object> normalizeForLovScript(Map<String, Object> source) {
        Map<String, Object> copy = new HashMap<>();
        if (source != null) {
            source.forEach((k, v) -> {
                if (k == null)
                    return;
                Object val = v;
                if (v instanceof java.sql.Timestamp ts) {
                    val = ts.toLocalDateTime();
                } else if (v instanceof java.sql.Date d) {
                    val = d.toLocalDate();
                }
                copy.put(k.toLowerCase(), val);
            });
        }
        return copy;
    }

    public void executeReportScript(String scriptText, Map<String, Object> params, String username, org.slf4j.Logger log) {
        if (scriptText == null || scriptText.trim().isEmpty()) {
            return;
        }
        try {
            String scriptId = "report_" + scriptText.hashCode();
            Class<? extends Script> scriptClass = scriptCache.get(scriptId, id -> {
                return new GroovyShell(compilerConfiguration).parse(scriptText).getClass();
            });

            Script scriptInstance = scriptClass.getDeclaredConstructor().newInstance();
            Binding binding = new Binding();
            binding.setVariable("params", params != null ? params : new HashMap<>());
            binding.setVariable("username", username);
            binding.setVariable("log", log);
            binding.setVariable("db", new DatabaseHelper(dataServiceProvider));
            binding.setVariable("dataService", dataServiceProvider.getIfAvailable());

            scriptInstance.setBinding(binding);
            scriptInstance.run();
        } catch (Exception ex) {
            throw new RuntimeException("Report Script Error: " + ex.getMessage(), ex);
        }
    }

    /** Batas waktu total script job terjadwal (detik); script form/action tetap 60 detik. */
    public static final long JOB_TIMEOUT_SECONDS = 120L;
    public static final String SCHEDULER_USER = "SCHEDULER";

    /** Nama yang tersedia di script job terjadwal — lihat {@link #executeScheduledJobScript}. */
    public static final java.util.Set<String> SCHEDULED_JOB_SCRIPT_NAMES = java.util.Set.of(
            "dataService", "db", "jobCode", "log", "renderReport", "sendEmail", "username");

    /**
     * Jalankan script job terjadwal. Tanpa UI/sesi: tidak ada fungsi layar (showError, msgBox, dst.).
     * Pemanggil membungkusnya dalam transaksi; exception dilempar apa adanya (dibungkus RuntimeException)
     * supaya transaksi di-rollback. Kunci cache diawali "job_" karena kelasnya dikompilasi dengan
     * config 120 detik, terpisah dari script form/action.
     */
    public void executeScheduledJobScript(String jobCode, String scriptText, org.slf4j.Logger log) {
        if (scriptText == null || scriptText.isBlank()) {
            return;
        }
        ActionContext ctx = new ActionContext(dataServiceProvider.getIfAvailable(), null, null, null);
        ctx.setUserIdOverride(SCHEDULER_USER);
        try {
            String scriptId = "job_" + scriptText.hashCode() + "_" + scriptText.length();
            Class<? extends Script> scriptClass = scriptCache.get(scriptId,
                    id -> new GroovyShell(jobCompilerConfiguration).parse(scriptText).getClass());

            Script scriptInstance = scriptClass.getDeclaredConstructor().newInstance();
            Binding binding = new Binding();
            binding.setVariable("jobCode", jobCode);
            binding.setVariable("username", SCHEDULER_USER);
            binding.setVariable("log", log);
            binding.setVariable("db", new DatabaseHelper(dataServiceProvider));
            binding.setVariable("dataService", dataServiceProvider.getIfAvailable());
            binding.setVariable("sendEmail", buildSendEmailClosure(ctx));
            binding.setVariable("renderReport", buildRenderReportClosure(ctx));
            scriptInstance.setBinding(binding);
            scriptInstance.run();
        } catch (Exception ex) {
            throw new RuntimeException("Scheduled Job Script Error: " + ex.getMessage(), ex);
        }
    }

    public void executeScript(String scriptId, String scriptText, Map<String, Object> newRow, int rowIndex,
            Map<String, Object> headerData, List<Map<String, Object>> items,
            com.vaadin.flow.component.Component currentView) {
        executeScript(scriptId, scriptText, newRow, rowIndex, headerData, items, currentView, null);
    }

    /**
     * @param selfField field pemicu; nilainya diikat ke variabel {@code self} supaya
     *                  script tidak perlu menyebut nama kolomnya sendiri. Kalau
     *                  fieldnya LOV, {@code self} juga bisa dibaca propertinya
     *                  ({@code self.itemname}) tanpa db.find. Null untuk script yang
     *                  tidak punya pemicu (On-Add-Row).
     */
    public void executeScript(String scriptId, String scriptText, Map<String, Object> newRow, int rowIndex,
            Map<String, Object> headerData, List<Map<String, Object>> items,
            com.vaadin.flow.component.Component currentView, FieldMeta selfField) {
        try {
            if (scriptText != null) {
                scriptText = scriptText.replaceAll(
                        "(?i)setElementEnabled\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                        "setElementEnabled('$1',");
                scriptText = scriptText.replaceAll(
                        "(?i)setElementReadonly\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                        "setElementReadonly('$1',");
                scriptText = scriptText.replaceAll(
                        "(?i)setElementValue\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                        "setElementValue('$1',")
                .replaceAll(
                        "(?i)setElementDisabled\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                        "setElementDisabled('$1',")
                .replaceAll(
                        "(?i)setElementReadonly\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                        "setElementReadonly('$1',");
                scriptText = scriptText.replaceAll(
                        "(?i)getElementValue\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                        "getElementValue('$1',");
            }
            final String finalScriptText = scriptText;

            Class<? extends Script> scriptClass = scriptCache.get(scriptId, id -> {
                return new GroovyShell(compilerConfiguration).parse(finalScriptText).getClass();
            });

            Script scriptInstance = scriptClass.getDeclaredConstructor().newInstance();

            Binding binding = new Binding();
            binding.setVariable("row", newRow);
            binding.setVariable("rowIndex", rowIndex);
            binding.setVariable("self", selfField != null
                    ? buildSelf(rawValue(newRow, selfField.getFieldName()), selfField.getLovCode())
                    : null);
            // lov('kolom') -> record LOV kolom itu di baris ini, tanpa perlu tahu nama
            // tabel maupun kolom kuncinya. Query hanya terjadi kalau benar dipanggil.
            binding.setVariable("lov", new groovy.lang.Closure<Map<String, Object>>(null) {
                @SuppressWarnings("unused")
                public Map<String, Object> doCall(String fieldName) {
                    FieldMeta target = currentView instanceof com.vaadinerp.components.SubformGridField sub
                            ? sub.findChildField(fieldName)
                            : null;
                    if (target == null) {
                        return null;
                    }
                    return lovRecord(dataServiceProvider, target.getLovCode(),
                            rawValue(newRow, target.getFieldName()));
                }
            });
            Map<String, Object> smartHeader = headerData != null ? prepareHeaderForScript(headerData) : new HashMap<>();
            binding.setVariable("header", smartHeader);
            binding.setVariable("form", smartHeader);
            binding.setVariable("items", items != null ? items : new ArrayList<>());
            binding.setVariable("db", new DatabaseHelper(dataServiceProvider));
            binding.setVariable("uploadDir", resolveUploadDir());

            if (currentView != null) {
                com.vaadin.flow.component.Component parentView = currentView;
                if (currentView instanceof com.vaadinerp.components.SubformGridField sub) {
                    parentView = sub.findParentView();
                }
                ActionContext ctx = new ActionContext(dataServiceProvider.getIfAvailable(), headerData, items,
                        parentView);
                binding.setVariable("setElementEnabled", new groovy.lang.Closure<Void>(null) {
                    @SuppressWarnings("unused")
                    public void doCall(Object ref, boolean enabled) {
                        ctx.setElementEnabled(ref, enabled);
                        if (currentView instanceof com.vaadinerp.components.SubformGridField sub) {
                            sub.setComponentEnabled(ref != null ? ref.toString() : null, enabled);
                        }
                    }
                });
                binding.setVariable("setElementReadonly", new groovy.lang.Closure<Void>(null) {
                    @SuppressWarnings("unused")
                    public void doCall(Object ref, boolean readOnly) {
                        ctx.setElementReadonly(ref, readOnly);
                        if (currentView instanceof com.vaadinerp.components.SubformGridField sub) {
                            sub.setComponentReadOnly(ref != null ? ref.toString() : null, readOnly);
                        }
                    }
                });
                binding.setVariable("setElementValue", new groovy.lang.Closure<Void>(null) {
                    @SuppressWarnings("unused")
                    public void doCall(Object ref, Object val) {
                        ctx.setElementValue(ref, val);
                    }
                });
                binding.setVariable("setElementVisible", new groovy.lang.Closure<Void>(null) {
                    @SuppressWarnings("unused")
                    public void doCall(Object ref, boolean visible) {
                        ctx.setElementVisible(ref, visible);
                        if (currentView instanceof com.vaadinerp.components.SubformGridField sub) {
                            sub.setColumnVisible(ref != null ? ref.toString() : null, visible);
                        }
                    }
                });
                binding.setVariable("sendEmail", buildSendEmailClosure(ctx));
                binding.setVariable("sendWhatsApp", buildSendWhatsAppClosure(ctx));
                binding.setVariable("sendWhatsAppApproval", buildSendWhatsAppApprovalClosure(ctx));
                binding.setVariable("getElementValue", new groovy.lang.Closure<List<Map<String, Object>>>(null) {
                    @SuppressWarnings("unused")
                    public List<Map<String, Object>> doCall(String ref, boolean selected) {
                        return ctx.getElementValue(ref, selected);
                    }
                });
                binding.setVariable("msgBox", new groovy.lang.Closure<Void>(null) {
                    @SuppressWarnings("unused")
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
            }

            scriptInstance.setBinding(binding);

            // Dijalankan di thread pemanggil, sama seperti executeActionScript. Kalau
            // dilempar ke thread lain, DSL yang menyentuh UI (msgBox, setElementValue,
            // setElementReadonly) gagal dengan "Cannot access state in VaadinSession or UI
            // without locking the session" karena lock session dipegang thread UI.
            // Batas 60 detik tetap berlaku lewat @TimedInterrupt di compilerConfiguration.
            scriptInstance.run();
        } catch (Exception e) {
            System.err.println("Error executing script [" + scriptId + "]: " + e.getMessage());
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Script Error: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public boolean executeActionScript(com.vaadinerp.meta.FormActionMeta act,
            Map<String, Object> headerBean,
            List<Map<String, Object>> selectedGridRows,
            com.vaadin.flow.component.Component currentView) {
        if (act == null || act.getScriptContent() == null || act.getScriptContent().isBlank()) {
            return true;
        }
        String scriptText = act.getScriptContent().trim();

        ActionContext ctx = new ActionContext(dataServiceProvider.getIfAvailable(), headerBean, selectedGridRows,
                currentView);

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

        // Auto fallback jika user mengetik nama procedure tanpa tanda kutip (misal:
        // executeProcedure(salesordertoproduction, ...))
        if (!scriptText.contains("propertyMissing")) {
            scriptText = scriptText + "\n\ndef propertyMissing(String name) { return name; }\n";
        }

        // Tambahan syntax sugar untuk referensi object (mengubah
        // setElementEnabled(row.tagid, ...) -> setElementEnabled('tagid', ...))
        scriptText = scriptText.replaceAll("(?i)setElementEnabled\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                "setElementEnabled('$1',");
        scriptText = scriptText.replaceAll(
                "(?i)setElementReadonly\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                "setElementReadonly('$1',");
        scriptText = scriptText.replaceAll("(?i)setElementValue\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                "setElementValue('$1',")
                .replaceAll("(?i)setElementDisabled\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                "setElementDisabled('$1',")
                .replaceAll("(?i)setElementReadonly\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                "setElementReadonly('$1',");
        scriptText = scriptText.replaceAll("(?i)getElementValue\\s*\\(\\s*(?:row|header|form)\\.([a-zA-Z0-9_]+)\\s*,",
                "getElementValue('$1',");

        final String finalScriptText = scriptText;
        String scriptId = "action_" + (act.getId() != null ? act.getId() : act.getActionCode()) + "_"
                + finalScriptText.hashCode();

        try {
            Class<? extends Script> scriptClass = scriptCache.get(scriptId, id -> {
                return new GroovyShell(compilerConfiguration).parse(finalScriptText).getClass();
            });

            Script scriptInstance = scriptClass.getDeclaredConstructor().newInstance();

            Binding binding = new Binding();
            binding.setVariable("ctx", ctx);
            binding.setVariable("header", headerBean != null ? prepareHeaderForScript(headerBean) : new HashMap<>());
            // Nilai mentah, bukan SmartHeaderNode, supaya `if (self)` berperilaku wajar.
            // Untuk field LOV dibungkus LovValueNode: tetap berperilaku seperti nilainya,
            // tapi propertinya bisa dibaca tanpa db.find.
            String trigger = act.getTriggerField() != null ? act.getTriggerField().trim() : "";
            binding.setVariable("self",
                    !trigger.isEmpty()
                            ? buildSelf(rawValue(headerBean, trigger), findLovCode(act.getFormMeta(), trigger))
                            : null);
            binding.setVariable("lov", new groovy.lang.Closure<Map<String, Object>>(null) {
                @SuppressWarnings("unused")
                public Map<String, Object> doCall(String fieldName) {
                    return lovRecord(dataServiceProvider, findLovCode(act.getFormMeta(), fieldName),
                            rawValue(headerBean, fieldName));
                }
            });
            binding.setVariable("selectedRows", selectedGridRows != null ? selectedGridRows : new ArrayList<>());
            // sessions.kick(header.id, ctx.getUserId()) dari action toolbar form Login History
            binding.setVariable("sessions", loginHistoryProvider.getIfAvailable());
            binding.setVariable("db", new DatabaseHelper(dataServiceProvider));
            binding.setVariable("uploadDir", resolveUploadDir());
            binding.setVariable("JsonOutput", groovy.json.JsonOutput.class);
            binding.setVariable("JsonSlurper", groovy.json.JsonSlurper.class);

            binding.setVariable("prompt", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object... args) {
                    if (args == null || args.length == 0) return;
                    // prompt("message", callback)
                    // prompt("title", "message", callback)
                    // prompt("message", "default", callback)
                    Object callback = null;
                    String title = "Input";
                    String message = null;
                    String defaultValue = null;
                    List<String> strings = new ArrayList<>();
                    for (Object arg : args) {
                        if (arg instanceof groovy.lang.Closure || arg instanceof java.util.function.Consumer) {
                            callback = arg;
                        } else if (arg != null) {
                            strings.add(arg.toString());
                        }
                    }
                    if (strings.size() == 1) {
                        message = strings.get(0);
                    } else if (strings.size() == 2) {
                        title = strings.get(0);
                        message = strings.get(1);
                    } else if (strings.size() >= 3) {
                        title = strings.get(0);
                        message = strings.get(1);
                        defaultValue = strings.get(2);
                    }
                    ctx.showInputDialog(title, message, defaultValue, callback);
                }
            });
            binding.setVariable("showYesNoDialog", new groovy.lang.Closure<Void>(null) {
                public void doCall(String title, String message, Object callback) {
                    ctx.showYesNoDialog(title, message, callback);
                }
            });
            binding.setVariable("showOptionsDialog", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object... args) {
                    if (args == null || args.length == 0)
                        return;

                    Object callback = null;
                    List<String> options = new ArrayList<>();
                    String title = null;
                    String message = null;
                    List<String> standaloneStrings = new ArrayList<>();

                    for (Object arg : args) {
                        if (arg instanceof groovy.lang.Closure || arg instanceof java.util.function.Consumer) {
                            callback = arg;
                        } else if (arg instanceof List) {
                            for (Object item : (List<?>) arg) {
                                options.add(item != null ? item.toString() : "");
                            }
                        } else if (arg != null) {
                            standaloneStrings.add(arg.toString());
                        }
                    }

                    if (options.isEmpty()) {
                        options.addAll(standaloneStrings);
                        title = "Pilihan";
                        message = "Please choose one of the following options:";
                    } else {
                        if (standaloneStrings.size() >= 1) title = standaloneStrings.get(0);
                        if (standaloneStrings.size() >= 2) message = standaloneStrings.get(1);
                        
                        if (title == null) title = "Pilihan";
                        if (message == null) message = "Please choose one of the following options:";
                    }

                    if (options.size() > 0) {
                        ctx.showOptionsDialog(title, message, options, callback);
                    }
                }
            });
            binding.setVariable("showDialog", binding.getVariable("showOptionsDialog"));
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
                        ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle,
                                urlOrExtra != null ? urlOrExtra.toString() : null, null);
                    }
                }

                public void doCall(Object tabId, String tabTitle, Object url, Object extra) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle,
                            url != null ? url.toString() : null, extra);
                }

                public void doCall(Map<?, ?> namedArgs, Object tabId, String tabTitle) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle, null, namedArgs);
                }

                public void doCall(Map<?, ?> namedArgs, Object tabId, String tabTitle, Object url) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle,
                            url != null ? url.toString() : null, namedArgs);
                }

                public void doCall(Map<?, ?> namedArgs, Object tabId, String tabTitle, Object url, Object extra) {
                    ctx.showMainTab(tabId != null ? tabId.toString() : "", tabTitle,
                            url != null ? url.toString() : null, namedArgs);
                }

                public void doCall(Object... args) {
                    if (args == null || args.length == 0)
                        return;
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
            binding.setVariable("sendEmail", buildSendEmailClosure(ctx));
            binding.setVariable("renderReport", buildRenderReportClosure(ctx));
            binding.setVariable("sendWhatsApp", buildSendWhatsAppClosure(ctx));
            binding.setVariable("sendWhatsAppApproval", buildSendWhatsAppApprovalClosure(ctx));
            binding.setVariable("setElementDisabled", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object ref, boolean disabled) {
                    ctx.setElementEnabled(ref, !disabled);
                }
            });
            binding.setVariable("setElementReadonly", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object ref, boolean readonly) {
                    ctx.setElementReadonly(ref, readonly);
                }
            });
            binding.setVariable("setElementEnabled", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object ref, boolean enabled) {
                    ctx.setElementEnabled(ref, enabled);
                }
            });
            binding.setVariable("setElementReadonly", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object ref, boolean readOnly) {
                    ctx.setElementReadonly(ref, readOnly);
                }
            });
            binding.setVariable("setElementVisible", new groovy.lang.Closure<Void>(null) {
                public void doCall(Object ref, boolean visible) {
                    ctx.setElementVisible(ref, visible);
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
            Object result = scriptInstance.run();
            return (result instanceof Boolean) ? (Boolean) result : true;
        } catch (Exception e) {
            String cleanMsg = ctx.extractCleanErrorMessage(e);
            System.err.println("Error executing action script [" + act.getActionCode() + "]: " + cleanMsg);
            ctx.showError("Script execution failed (" + act.getActionCode() + ")", "<b>Pesan Error:</b><br/>" + cleanMsg);
            return false;
        }
    }

    public void clearCache(String scriptId) {
        scriptCache.invalidate(scriptId);
    }

    public void clearAllCache() {
        scriptCache.invalidateAll();
    }

    public static Map<String, Object> prepareHeaderForScript(Map<String, Object> sourceBean) {
        if (sourceBean == null)
            return new HashMap<>();
        Map<String, SmartHeaderNode> smartNodes = new HashMap<>();
        Map<String, Object> targetBean = new HashMap<>(sourceBean);

        List<Map.Entry<String, Object>> entries = new ArrayList<>(targetBean.entrySet());
        for (Map.Entry<String, Object> entry : entries) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (k == null)
                continue;

            if (k.contains(".")) {
                String[] parts = k.split("\\.", 2);
                String parentKey = parts[0];
                String subKey = parts[1];

                SmartHeaderNode node = smartNodes.computeIfAbsent(parentKey, pk -> {
                    Object baseVal = targetBean.get(pk);
                    if (baseVal instanceof SmartHeaderNode shn)
                        return shn;
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
                        if (baseVal instanceof SmartHeaderNode shn)
                            return shn;
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

    public static class SmartHeaderNode extends Number
            implements Map<String, Object>, Comparable<Object>, groovy.lang.GroovyObject {
        private final Object primaryValue;
        private final Map<String, Object> properties = new LinkedHashMap<>();
        private transient groovy.lang.MetaClass metaClass;

        public SmartHeaderNode(Object primaryValue) {
            this.primaryValue = normalizeValue(primaryValue);
            this.metaClass = groovy.lang.GroovySystem.getMetaClassRegistry().getMetaClass(SmartHeaderNode.class);
            if (this.primaryValue != null && !(this.primaryValue instanceof Map)
                    && !(this.primaryValue instanceof SmartHeaderNode)) {
                properties.put("id", this.primaryValue);
                properties.put("value", this.primaryValue);
            }
        }

        private static Object normalizeValue(Object v) {
            if (v instanceof String s) {
                String trim = s.trim();
                if ("true".equalsIgnoreCase(trim))
                    return Boolean.TRUE;
                if ("false".equalsIgnoreCase(trim))
                    return Boolean.FALSE;
            }
            return v;
        }

        public void putProperty(String key, Object value) {
            if (key != null)
                properties.put(key, normalizeValue(value));
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
                    return groovy.lang.GroovySystem.getMetaClassRegistry().getMetaClass(primaryValue.getClass())
                            .getProperty(primaryValue, property);
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
            Object val = properties.containsKey("id") ? properties.get("id")
                    : (properties.containsKey("value") ? properties.get("value") : primaryValue);
            val = normalizeValue(val);
            if (val instanceof Boolean b)
                return b.booleanValue();
            if (val instanceof Number n)
                return n.doubleValue() != 0;
            if (val instanceof String s) {
                String trim = s.trim();
                return !trim.isEmpty() && !"false".equalsIgnoreCase(trim) && !"0".equals(trim)
                        && !"null".equalsIgnoreCase(trim);
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
            Object[] argArray = args instanceof Object[] ? (Object[]) args : new Object[] { args };
            if (!metaClass.respondsTo(this, name, argArray).isEmpty()) {
                return metaClass.invokeMethod(this, name, args);
            }
            if (primaryValue != null) {
                return groovy.lang.GroovySystem.getMetaClassRegistry().getMetaClass(primaryValue.getClass())
                        .invokeMethod(primaryValue, name, args);
            }
            throw new groovy.lang.MissingMethodException(name, SmartHeaderNode.class, argArray);
        }

        @Override
        public int intValue() {
            return primaryValue instanceof Number n ? n.intValue()
                    : (primaryValue != null && primaryValue.toString().matches("-?\\d+")
                            ? Integer.parseInt(primaryValue.toString())
                            : 0);
        }

        @Override
        public long longValue() {
            return primaryValue instanceof Number n ? n.longValue()
                    : (primaryValue != null && primaryValue.toString().matches("-?\\d+")
                            ? Long.parseLong(primaryValue.toString())
                            : 0L);
        }

        @Override
        public float floatValue() {
            return primaryValue instanceof Number n ? n.floatValue()
                    : (primaryValue != null ? Float.parseFloat(primaryValue.toString()) : 0f);
        }

        @Override
        public double doubleValue() {
            return primaryValue instanceof Number n ? n.doubleValue()
                    : (primaryValue != null ? Double.parseDouble(primaryValue.toString()) : 0d);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
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
        @SuppressWarnings("unchecked")
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

        @Override
        public int size() {
            return properties.size();
        }

        @Override
        public boolean isEmpty() {
            return properties.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return properties.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return properties.containsValue(value);
        }

        @Override
        public Object get(Object key) {
            return properties.get(key);
        }

        @Override
        public Object put(String key, Object value) {
            return properties.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return properties.remove(key);
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            properties.putAll(m);
        }

        @Override
        public void clear() {
            properties.clear();
        }

        @Override
        public Set<String> keySet() {
            return properties.keySet();
        }

        @Override
        public Collection<Object> values() {
            return properties.values();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return properties.entrySet();
        }
    }

    // Helper class for safe database queries in script
    /**
     * Nama yang tersedia di script baris/subform — lihat binding di
     * {@link #executeScript}. Dipakai On-Add-Row Script.
     *
     * Daftar ini ditaruh tepat di sebelah binding-nya supaya tidak melenceng saat
     * binding berubah; pemeriksa nama di editor script memakainya.
     */
    public static final java.util.Set<String> ROW_SCRIPT_NAMES = java.util.Set.of(
            "db", "form", "getElementValue", "header", "items", "lov", "msgBox", "row", "rowIndex", "self",
            "sendEmail", "sendWhatsApp", "sendWhatsAppApproval", "setElementEnabled", "setElementReadonly",
            "setElementValue", "setElementVisible", "uploadDir");

    /**
     * Nilai field pemicu untuk variabel {@code self}. Berperilaku seperti nilai
     * aslinya untuk truthiness, perbandingan, dan toString — jadi {@code if (self)}
     * dan {@code self == 'Y'} tetap menilai isinya, bukan objeknya. Properti lain
     * (mis. {@code self.itemname}) diambil dari record LOV, di-resolve sekali saat
     * pertama diakses. Script yang hanya memeriksa nilainya tidak menyentuh
     * database sama sekali.
     */
    public static class LovValueNode implements groovy.lang.GroovyObject {
        private final Object primaryValue;
        private final String lovCode;
        private final org.springframework.beans.factory.ObjectProvider<DynamicDataService> provider;
        private Map<String, Object> record;
        private boolean resolved;
        private transient groovy.lang.MetaClass metaClass;

        LovValueNode(Object primaryValue, String lovCode,
                org.springframework.beans.factory.ObjectProvider<DynamicDataService> provider) {
            this.primaryValue = primaryValue;
            this.lovCode = lovCode;
            this.provider = provider;
        }

        public Object getPrimaryValue() {
            return primaryValue;
        }

        /** Groovy truth mengikuti nilai aslinya, bukan keberadaan objek ini. */
        public boolean asBoolean() {
            return org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToBoolean(primaryValue);
        }

        private Map<String, Object> record() {
            if (resolved) {
                return record;
            }
            resolved = true;
            record = lovRecord(provider, lovCode, primaryValue);
            return record;
        }

        @Override
        public Object getProperty(String property) {
            if ("value".equals(property) || "id".equals(property)) {
                return primaryValue;
            }
            // Jalan untuk melihat kolom apa saja yang tersedia: msgBox(self._lov).
            // Diawali underscore supaya tidak bentrok dengan nama kolom nyata; kalau
            // toh ada kolom bernama sama, kolom aslinya yang menang.
            if ("_lov".equals(property) || "_record".equals(property)) {
                Map<String, Object> full = record();
                if (full == null || !full.containsKey(property)) {
                    return full;
                }
            }
            Map<String, Object> rec = record();
            if (rec == null) {
                return null;
            }
            if (rec.containsKey(property)) {
                return rec.get(property);
            }
            for (Map.Entry<String, Object> e : rec.entrySet()) {
                if (e.getKey() != null && e.getKey().equalsIgnoreCase(property)) {
                    return e.getValue();
                }
            }
            return null;
        }

        @Override
        public void setProperty(String property, Object newValue) {
            // self hanya untuk dibaca; menulis balik ke record LOV tidak masuk akal.
        }

        @Override
        public Object invokeMethod(String name, Object args) {
            return getMetaClass().invokeMethod(this, name, args);
        }

        @Override
        public groovy.lang.MetaClass getMetaClass() {
            if (metaClass == null) {
                metaClass = groovy.lang.GroovySystem.getMetaClassRegistry().getMetaClass(LovValueNode.class);
            }
            return metaClass;
        }

        @Override
        public void setMetaClass(groovy.lang.MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        @Override
        public boolean equals(Object other) {
            Object right = other instanceof LovValueNode node ? node.primaryValue : other;
            try {
                return org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.compareEqual(primaryValue, right);
            } catch (Exception ex) {
                return primaryValue != null && primaryValue.equals(right);
            }
        }

        @Override
        public int hashCode() {
            return primaryValue != null ? primaryValue.hashCode() : 0;
        }

        @Override
        public String toString() {
            return primaryValue != null ? primaryValue.toString() : "";
        }
    }

    /** Ambil satu record LOV berdasarkan lovCode dan nilai kuncinya. */
    private static Map<String, Object> lovRecord(
            org.springframework.beans.factory.ObjectProvider<DynamicDataService> provider, String lovCode,
            Object value) {
        DynamicDataService dataService = provider != null ? provider.getIfAvailable() : null;
        if (dataService == null || value == null || lovCode == null || lovCode.trim().isEmpty()) {
            return null;
        }
        // Nilai multi-pilih (dipisah koma) tidak menunjuk satu record.
        if (value.toString().contains(",")) {
            return null;
        }
        try {
            com.vaadinerp.meta.LovMeta lov = dataService.getLovMeta(lovCode.trim()).orElse(null);
            if (lov != null) {
                return dataService.fetchLovRecord(lov.getTableName(), lov.getValueColumn(), value);
            }
        } catch (Exception ignored) {
            // Script tidak boleh mati hanya karena LOV-nya tidak bisa dibaca.
        }
        return null;
    }

    /** Cari lovCode field pemicu di definisi form; null kalau bukan field LOV. */
    private String findLovCode(com.vaadinerp.meta.FormMeta formMeta, String fieldName) {
        if (formMeta == null || formMeta.getFields() == null || fieldName == null) {
            return null;
        }
        String clean = fieldName.trim();
        if (clean.startsWith("header.") || clean.startsWith("form.") || clean.startsWith("row.")) {
            clean = clean.substring(clean.indexOf('.') + 1);
        }
        for (FieldMeta field : formMeta.getFields()) {
            if (field.getFieldName() != null && field.getFieldName().trim().equalsIgnoreCase(clean)) {
                return field.getLovCode();
            }
        }
        return null;
    }

    /** Bungkus nilai pemicu hanya kalau fieldnya memang LOV. */
    private Object buildSelf(Object value, String lovCode) {
        if (lovCode == null || lovCode.trim().isEmpty()) {
            return value;
        }
        return new LovValueNode(value, lovCode.trim(), dataServiceProvider);
    }

    /** Ambil nilai dari map, exact dulu lalu case-insensitive. */
    private static Object rawValue(Map<String, Object> map, String key) {
        if (map == null || key == null)
            return null;
        if (map.containsKey(key))
            return map.get(key);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key))
                return e.getValue();
        }
        return null;
    }

    /**
     * Nama yang tersedia di script level form dan toolbar — lihat binding di
     * {@link #executeActionScript}. Dipakai ON_LOAD_*, BEFORE_SAVE, AFTER_SAVE,
     * ON_CHANGE, dan Extra Toolbar.
     */
    public static final java.util.Set<String> ACTION_SCRIPT_NAMES = java.util.Set.of(
            "JsonOutput", "JsonSlurper", "clearForm", "ctx", "db", "executeProcedure",
            "getElementValue", "header", "lov", "msgBox", "prompt", "refreshForm", "renderReport", "selectedRows", "self",
            "sendEmail", "sendWhatsApp", "sendWhatsAppApproval", "setElementDisabled", "setElementEnabled",
            "setElementReadonly", "setElementValue", "setElementVisible",
            "showDialog", "showError", "showMainTab", "showOptionsDialog", "showSuccess",
            "showYesNoDialog", "uploadDir");

    public static class DatabaseHelper {
        private final org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider;

        public DatabaseHelper(
                org.springframework.beans.factory.ObjectProvider<DynamicDataService> dataServiceProvider) {
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
                if (dataService == null)
                    return null;
                return dataService.fetchLovRecord(tableName, keyColumn, keyValue);
            } catch (Exception e) {
                return null;
            }
        }

        // Validasi bersama: hanya SELECT/WITH murni, blokir kata kunci & karakter berbahaya.
        // Dipakai oleh getValue/queryForMap/queryForList supaya aturan keamanannya satu tempat saja.
        private static void validateSelectOnly(String sql) {
            if (sql == null || sql.trim().isEmpty()) {
                throw new IllegalArgumentException("Query tidak boleh kosong!");
            }
            String clean = sql.trim().toUpperCase();
            if (!clean.startsWith("SELECT ") && !clean.startsWith("WITH ")) {
                throw new IllegalArgumentException(
                        "Hanya query SELECT atau WITH (CTE) yang diperbolehkan dalam script!");
            }
            if (clean.contains("INSERT ") || clean.contains("UPDATE ") || clean.contains("DELETE ") ||
                    clean.contains("DROP ") || clean.contains("ALTER ") || clean.contains("TRUNCATE ") ||
                    clean.contains("GRANT ") || clean.contains("REVOKE ") || clean.contains("EXEC ") ||
                    clean.contains("EXECUTE ") || clean.contains("PG_SLEEP") || clean.contains("PG_TERMINATE") ||
                    clean.contains("PG_CANCEL") || clean.contains("DBLINK") || sql.contains(";") ||
                    sql.contains("--") || sql.contains("/*")) {
                throw new IllegalArgumentException(
                        "Query mengandung perintah perusak database atau karakter terlarang!");
            }
        }

        // Unwrap SmartHeaderNode agar objek header.field aman dipassing langsung ke PreparedStatement JDBC
        private static Object[] cleanArgs(Object[] args) {
            if (args == null || args.length == 0) {
                return args;
            }
            Object[] cleaned = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object a = args[i];
                if (a instanceof SmartHeaderNode shn) {
                    a = shn.getPrimaryValue();
                }
                cleaned[i] = a;
            }
            return cleaned;
        }

        // Ambil 1 nilai langsung dari SQL query ringan (hanya SELECT murni tanpa
        // chained queries)
        public Object getValue(String sql, Object... args) {
            try {
                validateSelectOnly(sql);
                DynamicDataService dataService = dataServiceProvider.getIfAvailable();
                if (dataService == null)
                    return null;
                Object[] cleanArgs = cleanArgs(args);
                if (cleanArgs != null && cleanArgs.length > 0) {
                    return dataService.getJdbcTemplate().queryForObject(sql, Object.class, cleanArgs);
                } else {
                    return dataService.getJdbcTemplate().queryForObject(sql, Object.class);
                }
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                return null;
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    throw e;
                }
                System.err.println("SQL Error di db.getValue: " + sql + " | Error: " + e.getMessage());
                return null;
            }
        }

        // Ambil 1 baris, beberapa kolom sekaligus (mis. SELECT nama, alamat FROM ... WHERE id = ?)
        public Map<String, Object> queryForMap(String sql, Object... args) {
            try {
                validateSelectOnly(sql);
                DynamicDataService dataService = dataServiceProvider.getIfAvailable();
                if (dataService == null)
                    return null;
                Object[] cleanArgs = cleanArgs(args);
                if (cleanArgs != null && cleanArgs.length > 0) {
                    return dataService.getJdbcTemplate().queryForMap(sql, cleanArgs);
                } else {
                    return dataService.getJdbcTemplate().queryForMap(sql);
                }
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                return null;
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    throw e;
                }
                System.err.println("SQL Error di db.queryForMap: " + sql + " | Error: " + e.getMessage());
                return null;
            }
        }

        // Ambil beberapa baris sekaligus, tiap baris berisi beberapa kolom
        public List<Map<String, Object>> queryForList(String sql, Object... args) {
            try {
                validateSelectOnly(sql);
                DynamicDataService dataService = dataServiceProvider.getIfAvailable();
                if (dataService == null)
                    return java.util.Collections.emptyList();
                Object[] cleanArgs = cleanArgs(args);
                if (cleanArgs != null && cleanArgs.length > 0) {
                    return dataService.getJdbcTemplate().queryForList(sql, cleanArgs);
                } else {
                    return dataService.getJdbcTemplate().queryForList(sql);
                }
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    throw e;
                }
                System.err.println("SQL Error di db.queryForList: " + sql + " | Error: " + e.getMessage());
                return java.util.Collections.emptyList();
            }
        }
    }
}
