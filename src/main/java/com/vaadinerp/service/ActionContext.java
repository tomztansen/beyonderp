package com.vaadinerp.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinSession;
import com.vaadinerp.security.entity.AppUser;
import groovy.lang.Closure;

import java.util.*;
import java.util.function.Consumer;

/**
 * ActionContext (ctx) - Helper object disuntikkan ke dalam eksekusi Groovy
 * Script (Extra Toolbar).
 * Menyediakan dukungan penuh untuk macro @gridtable{...}, @app{userid}, dan
 * metode DSL UI.
 */
public class ActionContext {

    private final DynamicDataService dataService;
    private final Map<String, Object> headerBean;
    private final List<Map<String, Object>> selectedGridRows;
    private final Component currentView;

    public ActionContext(DynamicDataService dataService,
            Map<String, Object> headerBean,
            List<Map<String, Object>> selectedGridRows,
            Component currentView) {
        this.dataService = dataService;
        this.headerBean = headerBean != null ? headerBean : new HashMap<>();
        this.selectedGridRows = selectedGridRows != null ? selectedGridRows : new ArrayList<>();
        this.currentView = currentView;
    }

    public List<Map<String, Object>> getElementValue(String gridReference, boolean selectedOnly) {
        if (selectedOnly || selectedGridRows != null && !selectedGridRows.isEmpty()) {
            return selectedGridRows;
        }
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getGridData(String gridReference, boolean selectedOnly) {
        return getElementValue(gridReference, selectedOnly);
    }

    public void showYesNoDialog(String title, String message, Object onYesCallback) {
        UI ui = UI.getCurrent();
        if (ui == null && currentView != null && currentView.getUI().isPresent()) {
            ui = currentView.getUI().get();
        }
        final UI activeUi = ui;

        Command openDialog = () -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader(title != null ? title : "Konfirmasi");

            if (message != null && (message.contains("<br") || message.contains("</br>") || message.contains("<b")
                    || message.contains("<span"))) {
                String cleanHtml = message.replace("</br>", "<br/>");
                dialog.setText(new Html("<div>" + cleanHtml + "</div>"));
            } else {
                dialog.setText(message != null ? message : "");
            }

            dialog.setCancelable(true);
            dialog.setCancelText("No / Batal");
            dialog.setConfirmText("Yes / Lanjutkan");
            dialog.setConfirmButtonTheme("primary");

            dialog.addConfirmListener(e -> {
                try {
                    if (onYesCallback instanceof Closure<?> closure) {
                        closure.call();
                    } else if (onYesCallback instanceof Runnable runnable) {
                        runnable.run();
                    } else if (onYesCallback instanceof Consumer consumer) {
                        @SuppressWarnings("unchecked")
                        Consumer<Boolean> c = (Consumer<Boolean>) consumer;
                        c.accept(true);
                    }
                } catch (Exception ex) {
                    showError("Script Execution Error", ex.getMessage());
                }
            });

            dialog.open();
        };

        if (activeUi != null) {
            activeUi.access(openDialog);
        } else {
            openDialog.execute();
        }
    }

    public void msgBox(Object content) {
        msgBox("Message Box", content);
    }

    public void msgBox(String title, Object content) {
        UI ui = UI.getCurrent();
        if (ui == null && currentView != null && currentView.getUI().isPresent()) {
            ui = currentView.getUI().get();
        }
        final UI activeUi = ui;

        Command openDialog = () -> {
            com.vaadin.flow.component.dialog.Dialog dlg = new com.vaadin.flow.component.dialog.Dialog();
            dlg.setHeaderTitle(title != null ? title : "Message Box");
            dlg.setWidth("650px");
            dlg.setHeight("450px");
            dlg.setResizable(true);
            dlg.setDraggable(true);

            String formattedText;
            if (content == null) {
                formattedText = "null";
            } else if (content instanceof Map || content instanceof List) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
                    formattedText = mapper.writeValueAsString(content);
                } catch (Exception ex) {
                    formattedText = content.toString();
                }
            } else {
                formattedText = content.toString().replace("<br>", "\n").replace("<br/>", "\n").replace("</br>", "\n");
            }

            com.vaadin.flow.component.orderedlayout.VerticalLayout layout = new com.vaadin.flow.component.orderedlayout.VerticalLayout();
            layout.setSizeFull();
            layout.setPadding(false);

            com.vaadin.flow.component.html.Pre pre = new com.vaadin.flow.component.html.Pre(formattedText);
            pre.getStyle()
                    .set("background-color", "#0f172a")
                    .set("color", "#38bdf8")
                    .set("padding", "14px")
                    .set("border-radius", "8px")
                    .set("overflow", "auto")
                    .set("width", "100%")
                    .set("height", "100%")
                    .set("font-family", "Consolas, monospace")
                    .set("font-size", "13px")
                    .set("margin", "0");

            layout.add(pre);
            dlg.add(layout);

            com.vaadin.flow.component.button.Button btnClose = new com.vaadin.flow.component.button.Button("Tutup", e -> dlg.close());
            dlg.getFooter().add(btnClose);

            dlg.open();
        };

        if (activeUi != null) {
            activeUi.access(openDialog);
        } else {
            openDialog.execute();
        }
    }

    public void refreshForm() {
        UI ui = UI.getCurrent();
        if (ui == null && currentView != null && currentView.getUI().isPresent()) {
            ui = currentView.getUI().get();
        }
        Command update = () -> {
            if (currentView instanceof com.vaadinerp.views.GenericFormView view) {
                view.refreshBinderBean(headerBean);
            } else if (currentView instanceof com.vaadinerp.views.GenericMasterDetailFormView view) {
                view.refreshBinderBean(headerBean);
            }
        };
        if (ui != null) ui.access(update);
        else update.execute();
    }

    public void clearForm() {
        if (headerBean != null) {
            headerBean.clear();
        }
        refreshForm();
    }

    public void setElementValue(Object ref, Object val) {
        if (ref == null) return;
        String fieldName = ref.toString();
        if (fieldName.startsWith("@formfield{") && fieldName.endsWith("}")) {
            fieldName = fieldName.substring(11, fieldName.length() - 1).trim();
        } else if (fieldName.startsWith("header.")) {
            fieldName = fieldName.substring(7).trim();
        }
        if (headerBean != null) {
            if (val == null || "null".equalsIgnoreCase(val.toString())) {
                headerBean.put(fieldName, null);
                if (!fieldName.contains("_") && !fieldName.contains(".")) {
                    headerBean.put(fieldName + "_idno", null);
                    headerBean.put(fieldName + ".idno", null);
                    headerBean.put(fieldName + "_code", null);
                    headerBean.put(fieldName + ".code", null);
                }
            } else {
                headerBean.put(fieldName, val);
            }
        }
        refreshForm();
    }

    public boolean executeProcedure(Object procRef, Object callbackOrJson, Object... rest) {
        String jsonParams = null;
        String userId = getUserId();
        Object callback = null;

        if (callbackOrJson instanceof Closure<?> || callbackOrJson instanceof Runnable
                || callbackOrJson instanceof Consumer) {
            callback = callbackOrJson;
            if (rest.length > 0 && rest[0] != null) {
                jsonParams = rest[0].toString();
            }
            if (rest.length > 1 && rest[1] != null) {
                userId = rest[1].toString();
            }
        } else if (callbackOrJson != null) {
            jsonParams = callbackOrJson.toString();
            if (rest.length > 0 && rest[0] != null) {
                userId = rest[0].toString();
            }
        }

        boolean status = true;
        try {
            if (dataService != null && dataService.getJdbcTemplate() != null) {
                String procName = null;
                if (procRef instanceof Number num) {
                    if (num.intValue() == 3) {
                        procName = "dynamic.salesordertoproduction";
                    } else if (num.intValue() == 4) {
                        procName = "dynamic.generate_production_serial_no";
                    } else if (num.intValue() == 5) {
                        procName = "dynamic.generate_production_tag";
                    } else {
                        procName = "dynamic.proc_" + num.intValue();
                    }
                } else if (procRef != null) {
                    procName = procRef.toString().trim();
                    if ("3".equals(procName) || "proc_3".equalsIgnoreCase(procName) || "dynamic.proc_3".equalsIgnoreCase(procName)) {
                        procName = "dynamic.salesordertoproduction";
                    } else if ("4".equals(procName) || "proc_4".equalsIgnoreCase(procName) || "dynamic.proc_4".equalsIgnoreCase(procName)) {
                        procName = "dynamic.generate_production_serial_no";
                    } else if ("5".equals(procName) || "proc_5".equalsIgnoreCase(procName) || "dynamic.proc_5".equalsIgnoreCase(procName)) {
                        procName = "dynamic.generate_production_tag";
                    } else if (!procName.contains(".")) {
                        procName = "dynamic." + procName;
                    }
                }

                if (procName != null && !procName.isEmpty()) {
                    Object[] cleanRest = new Object[rest.length];
                    for (int i = 0; i < rest.length; i++) {
                        Object arg = rest[i];
                        if (arg instanceof com.vaadinerp.service.ScriptExecutorService.SmartHeaderNode shn) {
                            arg = shn.getPrimaryValue();
                        }
                        if (arg instanceof String strVal) {
                            String trimmed = strVal.trim();
                            if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                                arg = Boolean.parseBoolean(trimmed);
                            } else if (trimmed.matches("-?\\d+")) {
                                try {
                                    long l = Long.parseLong(trimmed);
                                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                                        arg = (int) l;
                                    } else {
                                        arg = l;
                                    }
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                        cleanRest[i] = arg;
                    }

                    if (cleanRest.length >= 4) {
                        System.out.println("Executing Procedure [" + procName + "] with 4 arguments: " + cleanRest[0] + ", " + cleanRest[1] + ", " + cleanRest[2] + ", " + cleanRest[3]);
                        dataService.getJdbcTemplate().update("CALL " + procName + "(?, ?, ?, ?)", cleanRest[0], cleanRest[1], cleanRest[2], cleanRest[3]);
                    } else if (cleanRest.length == 3) {
                        System.out.println("Executing Procedure [" + procName + "] with 3 arguments: " + cleanRest[0] + ", " + cleanRest[1] + ", " + cleanRest[2]);
                        dataService.getJdbcTemplate().update("CALL " + procName + "(?, ?, ?)", cleanRest[0], cleanRest[1], cleanRest[2]);
                    } else if (cleanRest.length == 2 && jsonParams != null && !jsonParams.trim().startsWith("{") && !jsonParams.trim().startsWith("[")) {
                        System.out.println("Executing Procedure [" + procName + "] with 2 non-json arguments: " + cleanRest[0] + ", " + cleanRest[1]);
                        dataService.getJdbcTemplate().update("CALL " + procName + "(?, ?)", cleanRest[0], cleanRest[1]);
                    } else {
                        System.out.println("Executing Procedure [" + procName + "] with params: " + jsonParams
                                + " by user: " + userId);
                        dataService.getJdbcTemplate().update("CALL " + procName + "(?::json, ?)", jsonParams, userId);
                    }
                }
            }
        } catch (Exception ex) {
            status = false;
            String cleanMsg = extractCleanErrorMessage(ex);
            System.err.println("Procedure execution failed: " + cleanMsg);
            showError("Gagal Eksekusi Procedure", "<b>Error:</b><br/>" + cleanMsg);
        }

        if (callback instanceof Closure<?> closure) {
            closure.call(status);
        } else if (callback instanceof Consumer consumer) {
            @SuppressWarnings("unchecked")
            Consumer<Boolean> c = (Consumer<Boolean>) consumer;
            c.accept(status);
        } else if (callback instanceof Runnable runnable && status) {
            runnable.run();
        }

        return status;
    }

    public String extractCleanErrorMessage(Throwable ex) {
        Throwable current = ex;
        String message = ex.getMessage();
        while (current != null) {
            if (current instanceof java.sql.SQLException sqlEx) {
                if (sqlEx.getMessage() != null && !sqlEx.getMessage().isBlank()) {
                    message = sqlEx.getMessage();
                    break;
                }
            }
            current = current.getCause();
        }

        if (message == null)
            return "Terjadi kesalahan yang tidak diketahui.";

        int errIdx = message.indexOf("ERROR:");
        if (errIdx == -1)
            errIdx = message.indexOf("Error:");
        if (errIdx == -1)
            errIdx = message.indexOf("error:");

        if (errIdx != -1) {
            String clean = message.substring(errIdx + 6).trim();
            int nlIdx = clean.indexOf('\n');
            if (nlIdx != -1) {
                clean = clean.substring(0, nlIdx).trim();
            }
            return clean;
        }

        if (message.contains("; ")) {
            String[] parts = message.split("; ");
            return parts[parts.length - 1].trim();
        }

        return message;
    }

    public void showSuccess(String title, String message) {
        showNotification(message != null ? message : title, NotificationVariant.LUMO_SUCCESS);
    }

    public void showError(String title, String message) {
        showNotification(message != null ? message : title, NotificationVariant.LUMO_ERROR);
    }

    private void showNotification(String text, NotificationVariant variant) {
        UI ui = UI.getCurrent();
        if (ui == null && currentView != null && currentView.getUI().isPresent()) {
            ui = currentView.getUI().get();
        }
        final UI activeUi = ui;

        Command show = () -> {
            Notification notification = new Notification();
            if (text != null && (text.contains("<br") || text.contains("</br>") || text.contains("<b"))) {
                String cleanHtml = text.replace("</br>", "<br/>");
                notification.add(new Html("<div>" + cleanHtml + "</div>"));
            } else {
                notification.setText(text != null ? text : "");
            }
            notification.setDuration(4000);
            notification.setPosition(Notification.Position.TOP_CENTER);
            notification.addThemeVariants(variant);
            notification.open();
        };

        if (activeUi != null) {
            activeUi.access(show);
        } else {
            show.execute();
        }
    }

    public void showMainTab(int tabId, String tabTitle, String url, String extra) {
        showMainTab(String.valueOf(tabId), tabTitle, url, (Object) extra);
    }

    public void showMainTab(int tabId, String tabTitle, String url, Object extra) {
        showMainTab(String.valueOf(tabId), tabTitle, url, extra);
    }

    public void showMainTab(String tabIdOrCode, String tabTitle, String url, String extra) {
        showMainTab(tabIdOrCode, tabTitle, url, (Object) extra);
    }

    public void showMainTab(String tabIdOrCode, String tabTitle, String url, Object extra) {
        Object finalExtra = extra;
        if (url != null && ("HIDE_HISTORIS".equalsIgnoreCase(url.trim()) || "HIDE_HISTORY".equalsIgnoreCase(url.trim()))) {
            if (finalExtra == null) {
                finalExtra = "HIDE_HISTORIS";
            } else if (finalExtra instanceof Map<?, ?> mapExtra) {
                Map<Object, Object> combined = new java.util.HashMap<>(mapExtra);
                combined.put("HIDE_HISTORIS", true);
                finalExtra = combined;
            } else if (finalExtra instanceof String strExtra) {
                if (!strExtra.toUpperCase().contains("HIDE_HISTOR")) {
                    finalExtra = "HIDE_HISTORIS&" + strExtra;
                }
            }
        }
        final Object effectiveExtra = finalExtra;

        UI ui = UI.getCurrent();
        if (ui == null && currentView != null && currentView.getUI().isPresent()) {
            ui = currentView.getUI().get();
        }
        final UI finalUi = ui;
        if (finalUi != null) {
            finalUi.access(() -> {
                Component comp = currentView;
                com.vaadinerp.views.PortalView portal = null;
                while (comp != null) {
                    if (comp instanceof com.vaadinerp.views.PortalView pv) {
                        portal = pv;
                        break;
                    }
                    comp = comp.getParent().orElse(null);
                }
                if (portal == null) {
                    for (Component c : finalUi.getChildren().toList()) {
                        if (c instanceof com.vaadinerp.views.PortalView pv) {
                            portal = pv;
                            break;
                        }
                    }
                }
                if (portal != null) {
                    portal.openTabByCode(tabIdOrCode != null ? tabIdOrCode : tabTitle, tabTitle, effectiveExtra);
                } else {
                    Notification.show("Membuka Tab [" + tabTitle + " (ID: " + tabIdOrCode + ")]...", 3000,
                            Notification.Position.MIDDLE);
                }
            });
        }
    }

    public String getUserId() {
        try {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                Object obj = session.getAttribute("LOGGED_IN_APP_USER");
                if (obj instanceof AppUser u) {
                    if (u.getUsername() != null && !u.getUsername().isBlank()) {
                        return u.getUsername();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "admin";
    }

    public Map<String, Object> getHeaderBean() {
        return headerBean;
    }

    public List<Map<String, Object>> getSelectedGridRows() {
        return selectedGridRows;
    }
}
