package com.vaadinerp.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadinerp.service.DynamicDataService;
import com.vaadinerp.components.StandardGridUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VisualQueryBuilderView extends VerticalLayout {

    private final DynamicDataService dynamicDataService;
    
    private final ComboBox<String> tableSelect = new ComboBox<>("Select Base Table");
    private final MultiSelectComboBox<String> columnSelect = new MultiSelectComboBox<>("Select Column");
    private final TextArea sqlEditor = new TextArea("SQL Text (Generated / Manual Edit)");
    private final Button btnExecute = new Button("Execute Query", VaadinIcon.PLAY.create());
    private final Button btnCopy = new Button("Copy SQL", VaadinIcon.COPY.create());
    
    // Filters UI
    private final VerticalLayout filtersLayout = new VerticalLayout();
    private final Button btnAddFilter = new Button("Add Filter", VaadinIcon.PLUS.create());
    
    // Aggregates UI
    private final VerticalLayout aggregateLayout = new VerticalLayout();
    private final Button btnAddAggregate = new Button("Add Aggregate", VaadinIcon.PLUS.create());
    private final List<AggregateRow> activeAggregates = new ArrayList<>();

    // JOIN UI
    private final VerticalLayout joinLayout = new VerticalLayout();
    private final Button btnAddJoin = new Button("Add Join", VaadinIcon.PLUS.create());
    private final List<JoinRow> activeJoins = new ArrayList<>();

    // ===================== INNER CLASSES =====================

    /**
     * JoinRow: Mendukung multi-kondisi ON per JOIN.
     * Setiap JoinRow memiliki tipe join, tabel target, dan satu atau lebih pasangan ON.
     */
    private class JoinRow extends VerticalLayout {
        ComboBox<String> joinTypeSelect = new ComboBox<>();
        ComboBox<String> targetTableSelect = new ComboBox<>();
        List<JoinOnPair> onPairs = new ArrayList<>();
        VerticalLayout onPairsLayout = new VerticalLayout();
        Button btnAddOn = new Button("+ ON", VaadinIcon.PLUS.create());
        Button btnRemove = new Button(VaadinIcon.TRASH.create());

        JoinRow(List<String> allTables, List<String> sourceColumns) {
            setPadding(false);
            setSpacing(false);
            getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
            getStyle().set("border-radius", "6px");
            getStyle().set("padding", "6px");
            getStyle().set("margin-bottom", "4px");

            joinTypeSelect.setItems("INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN");
            joinTypeSelect.setValue("INNER JOIN");
            joinTypeSelect.setWidth("140px");

            targetTableSelect.setPlaceholder("Target Table...");
            targetTableSelect.setItems(allTables);
            targetTableSelect.setWidth("200px");

            targetTableSelect.addValueChangeListener(e -> {
                String tbl = e.getValue();
                if (tbl != null && !tbl.isEmpty()) {
                    // Refresh kolom target di semua ON pairs
                    List<String> tgtCols = dynamicDataService.fetchTableColumns(tbl);
                    for (JoinOnPair pair : onPairs) {
                        pair.targetColSelect.setItems(tgtCols);
                        pair.targetColSelect.setEnabled(true);
                    }
                } else {
                    for (JoinOnPair pair : onPairs) {
                        pair.targetColSelect.clear();
                        pair.targetColSelect.setEnabled(false);
                    }
                }
                refreshAllColumns();
                generateSql();
            });

            btnRemove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
            btnRemove.addClickListener(e -> {
                joinLayout.remove(this);
                activeJoins.remove(this);
                refreshAllColumns();
                generateSql();
            });

            btnAddOn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btnAddOn.addClickListener(e -> {
                List<String> srcCols = getAllAvailableColumns();
                List<String> tgtCols = new ArrayList<>();
                String tbl = targetTableSelect.getValue();
                if (tbl != null && !tbl.isEmpty()) {
                    tgtCols = dynamicDataService.fetchTableColumns(tbl);
                }
                JoinOnPair pair = new JoinOnPair(srcCols, tgtCols);
                onPairs.add(pair);
                onPairsLayout.add(pair);
            });

            joinTypeSelect.addValueChangeListener(e -> generateSql());

            // Tambahkan ON pair pertama secara default
            onPairsLayout.setPadding(false);
            onPairsLayout.setSpacing(false);
            JoinOnPair firstPair = new JoinOnPair(sourceColumns, new ArrayList<>());
            onPairs.add(firstPair);
            onPairsLayout.add(firstPair);

            HorizontalLayout headerRow = new HorizontalLayout(joinTypeSelect, targetTableSelect, btnAddOn, btnRemove);
            headerRow.setAlignItems(Alignment.BASELINE);

            add(headerRow, onPairsLayout);
        }

        /** Inner pair ON: sourceCol = targetCol */
        private class JoinOnPair extends HorizontalLayout {
            ComboBox<String> sourceColSelect = new ComboBox<>();
            ComboBox<String> targetColSelect = new ComboBox<>();
            Button btnRemovePair = new Button(VaadinIcon.CLOSE_SMALL.create());

            JoinOnPair(List<String> srcCols, List<String> tgtCols) {
                sourceColSelect.setPlaceholder("Source Column (ON)...");
                sourceColSelect.setItems(srcCols);
                sourceColSelect.setWidth("180px");

                targetColSelect.setPlaceholder("Target Column (ON)...");
                targetColSelect.setItems(tgtCols);
                targetColSelect.setWidth("180px");
                targetColSelect.setEnabled(!tgtCols.isEmpty());

                btnRemovePair.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
                btnRemovePair.addClickListener(e -> {
                    if (onPairs.size() > 1) {
                        onPairsLayout.remove(this);
                        onPairs.remove(this);
                        generateSql();
                    } else {
                        Notification.show("At least 1 ON condition is required per JOIN", 2000, Notification.Position.BOTTOM_END);
                    }
                });

                sourceColSelect.addValueChangeListener(e -> generateSql());
                targetColSelect.addValueChangeListener(e -> generateSql());

                Span onLabel = new Span("ON");
                onLabel.getStyle().set("font-weight", "600");
                onLabel.getStyle().set("min-width", "24px");
                add(onLabel, sourceColSelect, new Span("="), targetColSelect, btnRemovePair);
                setAlignItems(Alignment.BASELINE);
            }
        }
    }

    /**
     * FilterRow: Mendukung filter kolom vs nilai ATAU kolom vs kolom.
     */
    private class FilterRow extends HorizontalLayout {
        ComboBox<String> fieldSelect = new ComboBox<>();
        ComboBox<String> operatorSelect = new ComboBox<>();
        TextField valueField = new TextField();
        ComboBox<String> columnRefSelect = new ComboBox<>();
        Checkbox useColumnRef = new Checkbox("vs Kolom");
        Button btnRemove = new Button(VaadinIcon.TRASH.create());

        boolean isColumnMode = false;

        FilterRow(List<String> availableColumns) {
            fieldSelect.setPlaceholder("Select Column...");
            fieldSelect.setItems(availableColumns);
            fieldSelect.setWidth("200px");

            operatorSelect.setItems("=", "!=", ">", "<", ">=", "<=", "LIKE", "ILIKE");
            operatorSelect.setValue("=");
            operatorSelect.setWidth("100px");

            valueField.setPlaceholder("Nilai...");
            valueField.setWidth("200px");

            columnRefSelect.setPlaceholder("Select Column...");
            columnRefSelect.setItems(availableColumns);
            columnRefSelect.setWidth("200px");
            columnRefSelect.setVisible(false);

            useColumnRef.setTooltipText("Check to compare with another column, not a text value");
            useColumnRef.addValueChangeListener(e -> {
                isColumnMode = e.getValue();
                valueField.setVisible(!isColumnMode);
                columnRefSelect.setVisible(isColumnMode);
                generateSql();
            });

            btnRemove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
            btnRemove.addClickListener(e -> {
                filtersLayout.remove(this);
                activeFilters.remove(this);
                generateSql();
            });

            fieldSelect.addValueChangeListener(e -> generateSql());
            operatorSelect.addValueChangeListener(e -> generateSql());
            valueField.addValueChangeListener(e -> generateSql());
            valueField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
            columnRefSelect.addValueChangeListener(e -> generateSql());

            add(fieldSelect, operatorSelect, valueField, columnRefSelect, useColumnRef, btnRemove);
            setAlignItems(Alignment.BASELINE);
        }
    }

    private class AggregateRow extends HorizontalLayout {
        ComboBox<String> funcSelect = new ComboBox<>();
        ComboBox<String> colSelect = new ComboBox<>();
        Button btnRemove = new Button(VaadinIcon.TRASH.create());

        AggregateRow(List<String> availableColumns) {
            funcSelect.setItems("COUNT", "SUM", "AVG", "MIN", "MAX");
            funcSelect.setPlaceholder("Fungsi");
            funcSelect.setWidth("120px");

            colSelect.setItems(availableColumns);
            colSelect.setPlaceholder("Select Column...");
            colSelect.setWidth("200px");

            btnRemove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
            btnRemove.addClickListener(e -> {
                aggregateLayout.remove(this);
                activeAggregates.remove(this);
                generateSql();
            });

            funcSelect.addValueChangeListener(e -> generateSql());
            colSelect.addValueChangeListener(e -> generateSql());

            add(funcSelect, colSelect, btnRemove);
            setAlignItems(Alignment.BASELINE);
        }
    }
    
    private final Grid<Map<String, Object>> resultGrid = new Grid<>();
    private final Span recordCount = new Span("Waiting for query execution...");
    private final List<FilterRow> activeFilters = new ArrayList<>();

    // ===================== CONSTRUCTOR =====================

    public VisualQueryBuilderView(DynamicDataService dynamicDataService) {
        this.dynamicDataService = dynamicDataService;
        setSizeFull();
        setPadding(false);

        tableSelect.setItems(dynamicDataService.fetchDynamicTables());
        tableSelect.setWidth("300px");

        columnSelect.setWidth("400px");
        columnSelect.setEnabled(false);

        tableSelect.addValueChangeListener(e -> {
            String selectedTable = e.getValue();
            if (selectedTable != null) {
                List<String> cols = dynamicDataService.fetchTableColumns(selectedTable);
                columnSelect.setItems(cols);
                columnSelect.setEnabled(true);
                // Reset filters, aggregates, and joins
                filtersLayout.removeAll();
                activeFilters.clear();
                aggregateLayout.removeAll();
                activeAggregates.clear();
                joinLayout.removeAll();
                activeJoins.clear();
                generateSql();
            } else {
                columnSelect.setEnabled(false);
                columnSelect.clear();
                filtersLayout.removeAll();
                activeFilters.clear();
                aggregateLayout.removeAll();
                activeAggregates.clear();
                joinLayout.removeAll();
                activeJoins.clear();
                sqlEditor.clear();
            }
        });

        columnSelect.addValueChangeListener(e -> generateSql());

        HorizontalLayout topLayout = new HorizontalLayout(tableSelect, columnSelect);
        topLayout.setAlignItems(Alignment.BASELINE);

        // --- JOIN Section ---
        btnAddJoin.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnAddJoin.addClickListener(e -> {
            if (tableSelect.getValue() != null) {
                List<String> allTables = dynamicDataService.fetchDynamicTables();
                List<String> srcCols = getAllAvailableColumns();
                JoinRow row = new JoinRow(allTables, srcCols);
                joinLayout.add(row);
                activeJoins.add(row);
            } else {
                Notification.show("Please select a table first");
            }
        });

        HorizontalLayout joinHeader = new HorizontalLayout(new Span("JOIN Tabel:"), btnAddJoin);
        joinHeader.setAlignItems(Alignment.CENTER);

        joinLayout.setPadding(false);
        joinLayout.setSpacing(false);
        VerticalLayout joinSection = new VerticalLayout(joinHeader, joinLayout);
        joinSection.setPadding(false);

        // --- Filter Section ---
        btnAddFilter.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnAddFilter.addClickListener(e -> {
            if (tableSelect.getValue() != null) {
                List<String> cols = getAllAvailableColumns();
                FilterRow row = new FilterRow(cols);
                filtersLayout.add(row);
                activeFilters.add(row);
            } else {
                Notification.show("Please select a table first");
            }
        });
        
        HorizontalLayout filterHeader = new HorizontalLayout(new Span("Kondisi Filter:"), btnAddFilter);
        filterHeader.setAlignItems(Alignment.CENTER);
        
        VerticalLayout filterSection = new VerticalLayout(filterHeader, filtersLayout);
        filterSection.setPadding(false);

        // --- Aggregate Section ---
        btnAddAggregate.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnAddAggregate.addClickListener(e -> {
            if (tableSelect.getValue() != null) {
                List<String> cols = getAllAvailableColumns();
                AggregateRow row = new AggregateRow(cols);
                aggregateLayout.add(row);
                activeAggregates.add(row);
            } else {
                Notification.show("Please select a table first");
            }
        });
        
        HorizontalLayout aggHeader = new HorizontalLayout(new Span("Kolom Agregasi (Auto Group-By):"), btnAddAggregate);
        aggHeader.setAlignItems(Alignment.CENTER);
        
        VerticalLayout aggSection = new VerticalLayout(aggHeader, aggregateLayout);
        aggSection.setPadding(false);
        
        HorizontalLayout midLayout = new HorizontalLayout(filterSection, aggSection);
        midLayout.setWidthFull();

        // --- SQL Editor ---
        sqlEditor.setWidthFull();
        sqlEditor.setMinHeight("150px");
        sqlEditor.setHeight("300px");
        sqlEditor.setMaxHeight("600px");
        sqlEditor.getStyle().set("font-family", "monospace");
        // Menggunakan executeJs agar native textarea bisa di-resize secara vertikal
        sqlEditor.getElement().executeJs(
            "requestAnimationFrame(() => {" +
            "  const ta = this.inputElement || this.shadowRoot.querySelector('textarea');" +
            "  if(ta) { ta.style.resize = 'vertical'; ta.style.overflow = 'auto'; }" +
            "});"
        );

        btnExecute.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnExecute.addClickListener(e -> executeQuery());
        
        btnCopy.addClickListener(e -> {
            com.vaadin.flow.component.UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", sqlEditor.getValue());
            Notification.show("SQL copied to clipboard!", 2000, Notification.Position.BOTTOM_END);
        });

        HorizontalLayout actionLayout = new HorizontalLayout(btnExecute, btnCopy);
        
        resultGrid.setSizeFull();
        resultGrid.getStyle().set("border-radius", "8px");
        StandardGridUtils.enableCellClipboardCopy(resultGrid);

        HorizontalLayout gridHeader = new HorizontalLayout(recordCount, StandardGridUtils.createExportExcelButton(resultGrid, "query_export"));
        gridHeader.setAlignItems(Alignment.CENTER);

        add(topLayout, joinSection, midLayout, sqlEditor, actionLayout, gridHeader, resultGrid);
    }

    // ===================== SQL GENERATION =====================

    private void generateSql() {
        String table = tableSelect.getValue();
        if (table == null) {
            sqlEditor.clear();
            return;
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        List<String> selectParts = new ArrayList<>();
        
        if (columnSelect.getValue() != null && !columnSelect.getValue().isEmpty()) {
            selectParts.addAll(columnSelect.getValue());
        }
        
        for (AggregateRow row : activeAggregates) {
            String func = row.funcSelect.getValue();
            String col = row.colSelect.getValue();
            if (func != null && !func.isEmpty() && col != null && !col.isEmpty()) {
                selectParts.add(func + "(" + col + ") AS " + func.toLowerCase() + "_" + col.replace(".", "_"));
            }
        }

        if (selectParts.isEmpty()) {
            sql.append("* ");
        } else {
            sql.append(String.join(", ", selectParts)).append(" ");
        }

        boolean hasJoins = !activeJoins.isEmpty();
        String baseAlias = hasJoins ? "t0" : null;
        sql.append("\nFROM dynamic.").append(table);
        if (hasJoins) {
            sql.append(" ").append(baseAlias);
        }

        // Generate JOIN clauses (dengan multi ON condition)
        int joinIdx = 1;
        for (JoinRow jRow : activeJoins) {
            String joinType = jRow.joinTypeSelect.getValue();
            String targetTable = jRow.targetTableSelect.getValue();

            if (joinType != null && targetTable != null && !targetTable.isEmpty()) {
                String tAlias = "t" + joinIdx;
                
                // Kumpulkan semua ON pairs yang valid
                List<String> onConditions = new ArrayList<>();
                for (JoinRow.JoinOnPair pair : jRow.onPairs) {
                    String srcCol = pair.sourceColSelect.getValue();
                    String tgtCol = pair.targetColSelect.getValue();
                    if (srcCol != null && !srcCol.isEmpty() && tgtCol != null && !tgtCol.isEmpty()) {
                        onConditions.add(srcCol + " = " + tAlias + "." + tgtCol);
                    }
                }

                if (!onConditions.isEmpty()) {
                    sql.append("\n").append(joinType).append(" dynamic.").append(targetTable)
                            .append(" ").append(tAlias)
                            .append(" ON ").append(String.join(" AND ", onConditions));
                }
                joinIdx++;
            }
        }

        // Generate WHERE clauses (mendukung kolom vs nilai DAN kolom vs kolom)
        boolean first = true;
        for (FilterRow row : activeFilters) {
            String field = row.fieldSelect.getValue();
            String op = row.operatorSelect.getValue();

            if (field == null || field.isEmpty()) continue;

            String rightSide;
            if (row.isColumnMode) {
                // Mode perbandingan kolom vs kolom
                String refCol = row.columnRefSelect.getValue();
                if (refCol == null || refCol.isEmpty()) continue;
                rightSide = refCol; // tanpa tanda kutip
            } else {
                // Mode perbandingan kolom vs nilai
                String val = row.valueField.getValue();
                if (val == null || val.isEmpty()) continue;
                rightSide = "'" + val.replace("'", "''") + "'";
            }

            if (first) {
                sql.append("\nWHERE ");
                first = false;
            } else {
                sql.append("\n  AND ");
            }
            sql.append(field).append(" ").append(op).append(" ").append(rightSide);
        }

        // Generate GROUP BY
        if (!activeAggregates.isEmpty() && columnSelect.getValue() != null && !columnSelect.getValue().isEmpty()) {
            sql.append("\nGROUP BY ").append(String.join(", ", columnSelect.getValue()));
        }

        sqlEditor.setValue(sql.toString());
    }

    // ===================== EXECUTE QUERY =====================

    private void executeQuery() {
        String sql = sqlEditor.getValue();
        if (sql == null || sql.trim().isEmpty()) {
            Notification.show("SQL is empty!", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            // maxRows limit to 1000 for safety against memory leak
            List<Map<String, Object>> data = dynamicDataService.executeSafeAdhocQuery(sql, 1000);
            
            resultGrid.removeAllColumns();
            if (!data.isEmpty()) {
                Map<String, Object> firstRow = data.get(0);
                for (String colName : firstRow.keySet()) {
                    resultGrid.addColumn(row -> row.get(colName) != null ? row.get(colName).toString() : "")
                            .setHeader(colName)
                            .setSortable(true)
                            .setAutoWidth(true);
                }
            }
            
            resultGrid.setItems(data);
            recordCount.setText("Showing " + data.size() + " rows.");
            
        } catch (Exception ex) {
            Notification.show("Error eksekusi query: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            resultGrid.setItems(new ArrayList<>());
            recordCount.setText("Error during execution.");
        }
    }

    // ===================== HELPER METHODS =====================

    /**
     * Mengumpulkan semua kolom yang tersedia dari tabel dasar + semua tabel yang di-join.
     * Format: tabel_dasar -> kolom langsung, tabel join -> "t1.kolom", "t2.kolom", dst.
     */
    private List<String> getAllAvailableColumns() {
        List<String> allCols = new ArrayList<>();
        String baseTable = tableSelect.getValue();
        if (baseTable == null) return allCols;

        boolean hasJoins = !activeJoins.isEmpty();
        List<String> baseCols = dynamicDataService.fetchTableColumns(baseTable);

        if (hasJoins) {
            for (String c : baseCols) {
                allCols.add("t0." + c);
            }
            int idx = 1;
            for (JoinRow jRow : activeJoins) {
                String tbl = jRow.targetTableSelect.getValue();
                if (tbl != null && !tbl.isEmpty()) {
                    List<String> jCols = dynamicDataService.fetchTableColumns(tbl);
                    for (String c : jCols) {
                        allCols.add("t" + idx + "." + c);
                    }
                    idx++;
                }
            }
        } else {
            allCols.addAll(baseCols);
        }
        return allCols;
    }

    /**
     * Refresh daftar kolom di MultiSelectComboBox setelah JOIN berubah.
     */
    private void refreshAllColumns() {
        List<String> allCols = getAllAvailableColumns();
        columnSelect.setItems(allCols);
    }
}
