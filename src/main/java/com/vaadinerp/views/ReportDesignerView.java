package com.vaadinerp.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;

/**
 * Report Designer: daftar report (grid + toolbar) + tab editor. Meniru pola
 * GenericFormView. Editor metadata/parameter, engine-adaptive surface, dan preview
 * dilengkapi di task berikutnya.
 */
@Route("report-designer")
public class ReportDesignerView extends VerticalLayout {

    private final ReportMetaRepository reportMetaRepository;
    private final Grid<ReportMeta> grid = new Grid<>(ReportMeta.class, false);
    private final TabSheet tabs = new TabSheet();
    private final VerticalLayout editorTab = new VerticalLayout();

    public ReportDesignerView(ReportMetaRepository reportMetaRepository) {
        this.reportMetaRepository = reportMetaRepository;
        setSizeFull();

        grid.addColumn(ReportMeta::getReportCode).setHeader("Code");
        grid.addColumn(ReportMeta::getReportTitle).setHeader("Title");
        grid.addColumn(r -> r.getEngineType() != null ? r.getEngineType() : "STANDARD").setHeader("Engine");
        grid.addColumn(ReportMeta::getTableName).setHeader("Source");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        refreshGrid();

        HorizontalLayout toolbar = new HorizontalLayout(
            new com.vaadinerp.components.SafeButton("New", e -> openEditor(null)),
            new com.vaadinerp.components.SafeButton("Edit", e -> withSelected(this::openEditor)),
            new com.vaadinerp.components.SafeButton("Design", e -> withSelected(this::openDesigner)),
            new com.vaadinerp.components.SafeButton("Delete", e -> withSelected(this::deleteReport)),
            new com.vaadinerp.components.SafeButton("Preview", e -> withSelected(this::preview)),
            new com.vaadinerp.components.SafeButton("Refresh", e -> refreshGrid())
        );

        VerticalLayout listTab = new VerticalLayout(toolbar, grid);
        listTab.setSizeFull();
        tabs.add("Report List", listTab);
        tabs.add("Editor", editorTab);
        tabs.setSizeFull();
        add(tabs);
    }

    private void refreshGrid() {
        grid.setItems(reportMetaRepository.findAll());
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
