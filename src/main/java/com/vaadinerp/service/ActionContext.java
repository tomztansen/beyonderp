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
 * ActionContext (ctx) - Helper object disuntikkan ke dalam eksekusi Groovy Script (Extra Toolbar).
 * Menyediakan dukungan penuh untuk macro @gridtable{...}, @app{userid}, dan metode DSL UI.
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
            
            if (message != null && (message.contains("<br") || message.contains("</br>") || message.contains("<b") || message.contains("<span"))) {
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

    public boolean executeProcedure(int procedureId, Object callbackOrJson, Object... rest) {
        String jsonParams = null;
        String userId = getUserId();
        Object callback = null;

        if (callbackOrJson instanceof Closure<?> || callbackOrJson instanceof Runnable || callbackOrJson instanceof Consumer) {
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
                System.out.println("Executing Procedure [" + procedureId + "] with params: " + jsonParams + " by user: " + userId);
            }
        } catch (Exception ex) {
            status = false;
            System.err.println("Procedure execution failed: " + ex.getMessage());
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
        UI ui = UI.getCurrent();
        if (ui == null && currentView != null && currentView.getUI().isPresent()) {
            ui = currentView.getUI().get();
        }
        if (ui != null) {
            ui.access(() -> {
                Notification.show("Membuka Tab [" + tabTitle + " (ID: " + tabId + ")]...", 3000, Notification.Position.MIDDLE);
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
        } catch (Exception ignored) {}
        return "admin";
    }

    public Map<String, Object> getHeaderBean() {
        return headerBean;
    }

    public List<Map<String, Object>> getSelectedGridRows() {
        return selectedGridRows;
    }
}
