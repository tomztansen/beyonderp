package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadinerp.components.StandardGridUtils;
import com.vaadinerp.service.DynamicDataService;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PgCronDesignerView extends VerticalLayout {

    private final DynamicDataService dynamicDataService;
    private final Grid<Map<String, Object>> jobsGrid = new Grid<>();
    private final Span recordCountSpan = new Span();
    private List<Map<String, Object>> allJobsList = new ArrayList<>();

    private final Button btnCommitChanges = new Button("Commit to DB", VaadinIcon.DATABASE.create());
    private final Button btnDiscardChanges = new Button("Discard Draft", VaadinIcon.CLOSE_CIRCLE.create());
    private final Span pendingStatusInfo = new Span("");

    public enum CronActionType {
        ADD_JOB, DELETE_JOB
    }

    public static class PendingPgCronAction {
        public CronActionType actionType;
        public Long jobId;
        public String jobName;
        public String schedule;
        public String command;

        public PendingPgCronAction(CronActionType actionType, Long jobId, String jobName, String schedule,
                String command) {
            this.actionType = actionType;
            this.jobId = jobId;
            this.jobName = jobName;
            this.schedule = schedule;
            this.command = command;
        }
    }

    private final List<PendingPgCronAction> pendingChanges = new ArrayList<>();

    public PgCronDesignerView(DynamicDataService dynamicDataService) {
        this.dynamicDataService = dynamicDataService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button btnRefresh = new Button("Refresh List", VaadinIcon.REFRESH.create());
        btnRefresh.addClickListener(e -> loadJobs());

        Button btnCreateNew = new Button("⚡ Create New Job", VaadinIcon.PLUS.create());
        btnCreateNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnCreateNew.addClickListener(e -> openJobDialog(null));

        btnCommitChanges.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnCommitChanges.setVisible(false);
        btnCommitChanges.addClickListener(e -> commitPendingChanges());

        btnDiscardChanges.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        btnDiscardChanges.setVisible(false);
        btnDiscardChanges.addClickListener(e -> discardPendingChanges());

        pendingStatusInfo.getStyle().set("font-weight", "bold").set("color", "var(--lumo-error-text-color)");

        HorizontalLayout pendingToolbar = new HorizontalLayout(btnCommitChanges, btnDiscardChanges, pendingStatusInfo);
        pendingToolbar.setAlignItems(Alignment.CENTER);
        pendingToolbar.setSpacing(true);

        HorizontalLayout toolbar = new HorizontalLayout(btnRefresh, btnCreateNew, pendingToolbar);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setWidthFull();

        jobsGrid.setSizeFull();
        jobsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        StandardGridUtils.enableCellClipboardCopy(jobsGrid);

        jobsGrid.addColumn(row -> row.get("jobid")).setHeader("ID").setWidth("80px").setFlexGrow(0).setSortable(true);
        jobsGrid.addColumn(row -> row.get("jobname")).setHeader("Job Name").setWidth("200px").setFlexGrow(1)
                .setSortable(true);
        jobsGrid.addColumn(row -> {
            String sch = (String) row.get("schedule");
            return row.containsKey("_pending_state") ? "📝 " + sch : sch;
        }).setHeader("Schedule").setWidth("150px").setFlexGrow(0);
        jobsGrid.addColumn(row -> row.get("command")).setHeader("Command (SQL)").setWidth("350px").setFlexGrow(2);
        jobsGrid.addColumn(row -> row.get("active")).setHeader("Active").setWidth("100px").setFlexGrow(0);

        jobsGrid.addComponentColumn(row -> {
            boolean isPendingDel = "_pending_delete".equals(row.get("_pending_state"));

            Button btnEdit = new Button("Edit", VaadinIcon.EDIT.create());
            btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            btnEdit.addClickListener(e -> openJobDialog(row));
            btnEdit.setEnabled(!isPendingDel);

            Button btnDrop = new Button("Delete", VaadinIcon.TRASH.create());
            btnDrop.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            btnDrop.addClickListener(e -> markJobForDeletion(row));
            btnDrop.setEnabled(!isPendingDel);

            Button btnSyncNow = new Button("Sync Now", VaadinIcon.PLAY.create());
            btnSyncNow.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            btnSyncNow.addClickListener(e -> executeSyncNow(row));
            btnSyncNow.setEnabled(!isPendingDel);

            Button btnLog = new Button("Logs", VaadinIcon.FILE_TEXT_O.create());
            btnLog.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnLog.addClickListener(e -> openLogDialog(row));
            btnLog.setEnabled(row.get("jobid") instanceof Number && ((Number) row.get("jobid")).longValue() > 0);

            HorizontalLayout actions = new HorizontalLayout(btnEdit, btnDrop, btnSyncNow, btnLog);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Action").setWidth("400px").setFlexGrow(0);

        HorizontalLayout footer = new HorizontalLayout(recordCountSpan);
        footer.setAlignItems(Alignment.CENTER);

        add(new H4("Manajemen Job Scheduler (pg_cron)"), toolbar, jobsGrid, footer);

        loadJobs();
    }

    private void loadJobs() {
        allJobsList = dynamicDataService.fetchPgCronJobs();
        if (allJobsList.isEmpty() && pendingChanges.isEmpty()) {
            Notification.show("Tidak ada data pg_cron atau ekstensi pg_cron belum diinstal di server.", 4000,
                    Notification.Position.BOTTOM_END);
        }
        refreshGrid();
    }

    private void refreshGrid() {
        List<Map<String, Object>> mergedList = new ArrayList<>();

        // Copy original list
        for (Map<String, Object> originalRow : allJobsList) {
            Map<String, Object> rowCopy = new HashMap<>(originalRow);
            Long jId = rowCopy.get("jobid") instanceof Number ? ((Number) rowCopy.get("jobid")).longValue() : null;


            for (PendingPgCronAction action : pendingChanges) {
                if (action.jobId != null && action.jobId.equals(jId)) {
                    if (action.actionType == CronActionType.DELETE_JOB) {
                        rowCopy.put("_pending_state", "_pending_delete");
                        rowCopy.put("jobname", "[DELETED] " + rowCopy.get("jobname"));
                    } else if (action.actionType == CronActionType.ADD_JOB) {
                        rowCopy.put("_pending_state", "_pending_edit");
                        rowCopy.put("schedule", action.schedule);
                        rowCopy.put("command", action.command);
                        rowCopy.put("jobname", action.jobName);
                    }

                }
            }
            mergedList.add(rowCopy);
        }

        // Add new pending jobs
        long tempId = -1;
        for (PendingPgCronAction action : pendingChanges) {
            if (action.jobId == null && action.actionType == CronActionType.ADD_JOB) {
                Map<String, Object> newRow = new HashMap<>();
                newRow.put("jobid", tempId--);
                newRow.put("jobname", action.jobName);
                newRow.put("schedule", action.schedule);
                newRow.put("command", action.command);
                newRow.put("active", true);
                newRow.put("_pending_state", "_pending_new");
                mergedList.add(newRow);
            }
        }

        jobsGrid.setItems(mergedList);
        recordCountSpan.setText("Total Jobs: " + mergedList.size());
        updatePendingUI();
    }

    private void openJobDialog(Map<String, Object> existingRow) {
        boolean isNew = (existingRow == null || "_pending_new".equals(existingRow.get("_pending_state")));
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Create New Scheduler Job" : "Edit Scheduler Job");
        dialog.setWidth("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        TextField nameField = new TextField("Job Name");
        nameField.setWidthFull();
        nameField.setPlaceholder("contoh: sync_karyawan_mysql");

        RadioButtonGroup<String> scheduleType = new RadioButtonGroup<>("Tipe Jadwal");
        scheduleType.setItems("Interval Menit", "Interval Jam", "Harian", "Custom");
        scheduleType.setValue("Interval Menit");

        IntegerField minuteField = new IntegerField("Setiap X Menit");
        minuteField.setValue(30);
        minuteField.setMin(1);
        minuteField.setMax(59);

        IntegerField hourField = new IntegerField("Setiap X Jam");
        hourField.setValue(1);
        hourField.setMin(1);
        hourField.setMax(23);
        hourField.setVisible(false);

        TimePicker timePicker = new TimePicker("Pukul (HH:mm)");
        timePicker.setValue(LocalTime.of(2, 0));
        timePicker.setVisible(false);

        TextField customCronField = new TextField("Format Cron (* * * * *)");
        customCronField.setPlaceholder("*/30 * * * *");
        customCronField.setVisible(false);

        scheduleType.addValueChangeListener(e -> {
            minuteField.setVisible("Interval Menit".equals(e.getValue()));
            hourField.setVisible("Interval Jam".equals(e.getValue()));
            timePicker.setVisible("Harian".equals(e.getValue()));
            customCronField.setVisible("Custom".equals(e.getValue()));
        });

        TextArea commandArea = new TextArea("Command SQL (Yang akan dieksekusi)");
        commandArea.setWidthFull();
        commandArea.setHeight("150px");
        commandArea.setPlaceholder("CALL sp_sinkronisasi();");
        commandArea.getStyle().set("font-family", "monospace");

        if (existingRow != null) {
            String oldName = existingRow.get("jobname") != null ? existingRow.get("jobname").toString() : "";
            String oldSchedule = existingRow.get("schedule") != null ? existingRow.get("schedule").toString() : "";
            String oldCommand = existingRow.get("command") != null ? existingRow.get("command").toString() : "";

            nameField.setValue(oldName);
            commandArea.setValue(oldCommand);
            scheduleType.setValue("Custom");
            customCronField.setValue(oldSchedule);
        }

        layout.add(nameField, scheduleType, minuteField, hourField, timePicker, customCronField, commandArea);
        dialog.add(layout);

        Button btnSave = new Button("Save as Draft", VaadinIcon.PENCIL.create(), e -> {
            String jName = nameField.getValue().trim();
            String cmd = commandArea.getValue().trim();

            if (jName.isEmpty() || cmd.isEmpty()) {
                Notification.show("Nama Job dan Command tidak boleh kosong!", 3000, Notification.Position.MIDDLE);
                return;
            }

            String cronExp = "";
            String sType = scheduleType.getValue();
            if ("Interval Menit".equals(sType)) {
                cronExp = "*/" + (minuteField.getValue() != null ? minuteField.getValue() : 30) + " * * * *";
            } else if ("Interval Jam".equals(sType)) {
                cronExp = "0 */" + (hourField.getValue() != null ? hourField.getValue() : 1) + " * * *";
            } else if ("Harian".equals(sType)) {
                LocalTime t = timePicker.getValue() != null ? timePicker.getValue() : LocalTime.of(0, 0);
                cronExp = t.getMinute() + " " + t.getHour() + " * * *";
            } else {
                cronExp = customCronField.getValue().trim();
            }

            Long jId = null;
            if (existingRow != null && existingRow.get("jobid") != null
                    && !(existingRow.get("jobid").toString().startsWith("-"))) {
                jId = ((Number) existingRow.get("jobid")).longValue();
            }

            pendingChanges.add(new PendingPgCronAction(CronActionType.ADD_JOB, jId, jName, cronExp, cmd));
            Notification.show("Perubahan Job ditambahkan ke draf.", 2000, Notification.Position.BOTTOM_END);
            refreshGrid();
            dialog.close();
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnCancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(btnCancel, btnSave);
        dialog.open();
    }

    private void markJobForDeletion(Map<String, Object> row) {
        Long jId = row.get("jobid") instanceof Number ? ((Number) row.get("jobid")).longValue() : null;
        if (jId != null && jId > 0) {
            pendingChanges.add(new PendingPgCronAction(CronActionType.DELETE_JOB, jId, null, null, null));
            Notification.show("Penghapusan Job ditambahkan ke draf.", 2000, Notification.Position.BOTTOM_END);
            refreshGrid();
        } else {
            // It's a new unsaved job, just remove it from pending list
            pendingChanges.removeIf(p -> p.jobId == null && row.get("jobname").equals(p.jobName));
            refreshGrid();
        }
    }

    private void executeSyncNow(Map<String, Object> row) {
        String cmd = row.get("command") != null ? row.get("command").toString() : "";
        if (cmd.isEmpty())
            return;

        Dialog confirm = new Dialog();
        confirm.setHeaderTitle("Konfirmasi Sync Now");
        confirm.add(new Span("Anda akan mengeksekusi perintah berikut saat ini juga:"));
        confirm.add(new com.vaadin.flow.component.html.Pre(cmd));

        Button btnYes = new Button("Jalankan Sekarang", VaadinIcon.PLAY.create(), e -> {
            try {
                dynamicDataService.executeProcedureScript(cmd);
                Notification.show("✅ Command berhasil dieksekusi secara instan!", 3000,
                        Notification.Position.BOTTOM_END);
                confirm.close();
            } catch (Exception ex) {
                Notification.show("❌ Gagal mengeksekusi command: " + ex.getMessage(), 5000,
                        Notification.Position.MIDDLE);
            }
        });
        btnYes.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button btnNo = new Button("Batal", e -> confirm.close());
        confirm.getFooter().add(btnNo, btnYes);
        confirm.open();
    }

    private void updatePendingUI() {
        if (pendingChanges.isEmpty()) {
            btnCommitChanges.setVisible(false);
            btnDiscardChanges.setVisible(false);
            pendingStatusInfo.setText("");
        } else {
            btnCommitChanges.setVisible(true);
            btnDiscardChanges.setVisible(true);
            pendingStatusInfo.setText(pendingChanges.size() + " perubahan DRAFT belum disimpan!");
        }
    }

    private void commitPendingChanges() {
        if (pendingChanges.isEmpty())
            return;
        try {
            for (PendingPgCronAction action : pendingChanges) {
                if (action.actionType == CronActionType.DELETE_JOB) {
                    dynamicDataService.unschedulePgCronJob(action.jobId);
                } else if (action.actionType == CronActionType.ADD_JOB) {
                    // Cukup gunakan schedule(). pg_cron akan otomatis melakukan UPDATE 
                    // dan mempertahankan jobid serta riwayat log asalkan jobName sama.
                    dynamicDataService.schedulePgCronJob(action.jobName, action.schedule, action.command);
                }
            }
            Notification.show("✅ Semua perubahan penjadwalan berhasil disimpan ke database!", 3000,
                    Notification.Position.BOTTOM_END);
            pendingChanges.clear();
            loadJobs();
        } catch (Exception e) {
            Notification.show("❌ Terjadi kesalahan saat commit: " + e.getMessage(), 6000, Notification.Position.MIDDLE);
        }
    }

    private void discardPendingChanges() {
        pendingChanges.clear();
        refreshGrid();
        Notification.show("Draf perubahan dibatalkan.", 2000, Notification.Position.BOTTOM_END);
    }

    private void openLogDialog(Map<String, Object> row) {
        Long jId = row.get("jobid") instanceof Number ? ((Number) row.get("jobid")).longValue() : null;
        if (jId == null) return;
        
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Riwayat Log Eksekusi Job: " + row.get("jobname"));
        dialog.setWidth("800px");
        dialog.setHeight("500px");

        Grid<Map<String, Object>> logGrid = new Grid<>();
        logGrid.setSizeFull();
        logGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        
        logGrid.addColumn(r -> r.get("start_time")).setHeader("Mulai").setAutoWidth(true);
        logGrid.addColumn(r -> r.get("end_time")).setHeader("Selesai").setAutoWidth(true);
        logGrid.addColumn(r -> r.get("status")).setHeader("Status").setAutoWidth(true);
        logGrid.addColumn(r -> r.get("return_message")).setHeader("Pesan").setWidth("250px").setFlexGrow(1);
        
        List<Map<String, Object>> logs = dynamicDataService.fetchPgCronLogs(jId);
        logGrid.setItems(logs);
        
        Button btnClose = new Button("Tutup", e -> dialog.close());
        
        VerticalLayout layout = new VerticalLayout(new Span("Menampilkan hingga 100 riwayat eksekusi terakhir:"), logGrid);
        layout.setSizeFull();
        layout.setPadding(false);
        
        dialog.add(layout);
        dialog.getFooter().add(btnClose);
        dialog.open();
    }
}
