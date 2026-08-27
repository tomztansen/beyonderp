package com.vaadinerp.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;
import com.vaadinerp.components.StandardGridUtils;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Report Designer: daftar report (grid berfilter + toolbar, pola sama seperti grid standar) +
 * tab editor. Editor metadata/parameter, engine-adaptive surface, dan preview dilengkapi di
 * task berikutnya.
 */
@Route("report-designer")
public class ReportDesignerView extends VerticalLayout {

    private final ReportMetaRepository reportMetaRepository;
    private final Grid<ReportMeta> grid = new Grid<>(ReportMeta.class, false);
    private final TabSheet tabs = new TabSheet();
    private final VerticalLayout editorTab = new VerticalLayout();
    private Runnable reapplyFilters = () -> {};

    public ReportDesignerView(ReportMetaRepository reportMetaRepository) {
        this.reportMetaRepository = reportMetaRepository;
        setSizeFull();

        Grid.Column<ReportMeta> colCode = grid.addColumn(ReportMeta::getReportCode).setHeader("Code");
        Grid.Column<ReportMeta> colTitle = grid.addColumn(ReportMeta::getReportTitle).setHeader("Title");
        Grid.Column<ReportMeta> colEngine = grid.addColumn(this::engineOf).setHeader("Engine");
        Grid.Column<ReportMeta> colSource = grid.addColumn(ReportMeta::getTableName).setHeader("Source");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();

        // Filter header per-kolom + sort + resize + clipboard (helper standar aplikasi)
        Map<Grid.Column<ReportMeta>, Function<ReportMeta, String>> colGetters = new LinkedHashMap<>();
        colGetters.put(colCode, r -> nz(r.getReportCode()));
        colGetters.put(colTitle, r -> nz(r.getReportTitle()));
        colGetters.put(colEngine, this::engineOf);
        colGetters.put(colSource, r -> nz(r.getTableName()));
        this.reapplyFilters = StandardGridUtils.attachGridFilters(grid, colGetters, reportMetaRepository::findAll);
        StandardGridUtils.enableRowClickSelection(grid);
        refreshGrid();

        HorizontalLayout toolbar = new HorizontalLayout(
            new com.vaadinerp.components.SafeButton("New", e -> openEditor(null)),
            new com.vaadinerp.components.SafeButton("Edit", e -> withSelected(this::openEditor)),
            new com.vaadinerp.components.SafeButton("Design", e -> withSelected(this::openDesigner)),
            new com.vaadinerp.components.SafeButton("Delete", e -> withSelected(this::deleteReport)),
            new com.vaadinerp.components.SafeButton("Preview", e -> withSelected(this::preview)),
            new com.vaadinerp.components.SafeButton("Refresh", e -> refreshGrid()),
            StandardGridUtils.createExportExcelButton(grid, "report_list")
        );

        VerticalLayout listTab = new VerticalLayout(toolbar, grid);
        listTab.setSizeFull();
        tabs.add("Report List", listTab);
        tabs.add("Editor", editorTab);
        tabs.setSizeFull();
        add(tabs);
    }

    private String engineOf(ReportMeta r) {
        return r.getEngineType() != null ? r.getEngineType() : "STANDARD";
    }

    private String nz(String s) {
        return s != null ? s : "";
    }

    private void refreshGrid() {
        reapplyFilters.run();
    }

    private void withSelected(java.util.function.Consumer<ReportMeta> action) {
        ReportMeta sel = grid.asSingleSelect().getValue();
        if (sel == null) {
            com.vaadin.flow.component.notification.Notification.show("Please select a report first.");
            return;
        }
        action.accept(sel);
    }

    // Filled in later tasks:
    private void openEditor(ReportMeta report) { tabs.setSelectedIndex(1); }
    private void openDesigner(ReportMeta report) { /* Task 5 */ }
    private void deleteReport(ReportMeta report) { /* Task 5 */ }
    private void preview(ReportMeta report) { /* Task 6 */ }
}
