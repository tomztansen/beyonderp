package com.vaadinerp.components;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadinerp.meta.ReportParamFilterMeta;
import com.vaadinerp.meta.ReportParamMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Editor filter LOV inline untuk satu parameter report (dipakai sebagai row-detail di Report Designer).
 * Menyunting langsung {@code param.getFilters()} (buffered=false) sehingga Save di toolbar utama
 * cukup baca daftar itu. STATIC = nilai tetap, FIELD = nama parameter lain (cascading).
 */
public class ParamFilterEditor extends VerticalLayout {

    private final Grid<ReportParamFilterMeta> grid = new Grid<>(ReportParamFilterMeta.class, false);

    public ParamFilterEditor(ReportParamMeta param, Supplier<List<String>> otherParamNames) {
        setPadding(false);
        setSpacing(false);
        getStyle().set("padding", "6px 12px").set("background", "#f8fafc");
        if (param.getFilters() == null) param.setFilters(new ArrayList<>());

        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        Binder<ReportParamFilterMeta> binder = new Binder<>(ReportParamFilterMeta.class);
        grid.getEditor().setBinder(binder);
        grid.getEditor().setBuffered(false);

        Grid.Column<ReportParamFilterMeta> cLogic = grid.addColumn(f -> nz(f.getLogicalOperator()))
                .setHeader("Logic").setWidth("90px").setFlexGrow(0);
        ComboBox<String> eLogic = new ComboBox<>();
        eLogic.setItems("AND", "OR");
        binder.bind(eLogic, ReportParamFilterMeta::getLogicalOperator, ReportParamFilterMeta::setLogicalOperator);
        cLogic.setEditorComponent(eLogic);

        Grid.Column<ReportParamFilterMeta> cCol = grid.addColumn(f -> nz(f.getFilterColumn()))
                .setHeader("Target Column (LOV)").setFlexGrow(1);
        TextField eCol = new TextField();
        eCol.setWidthFull();
        binder.bind(eCol, ReportParamFilterMeta::getFilterColumn, ReportParamFilterMeta::setFilterColumn);
        cCol.setEditorComponent(eCol);

        Grid.Column<ReportParamFilterMeta> cOp = grid.addColumn(f -> nz(f.getComparisonOperator()))
                .setHeader("Op").setWidth("90px").setFlexGrow(0);
        ComboBox<String> eOp = new ComboBox<>();
        eOp.setItems("=", ">", "<", ">=", "<=", "LIKE", "ILIKE", "!=", "= ANY");
        binder.bind(eOp, ReportParamFilterMeta::getComparisonOperator, ReportParamFilterMeta::setComparisonOperator);
        cOp.setEditorComponent(eOp);

        Grid.Column<ReportParamFilterMeta> cSrcType = grid.addColumn(f -> nz(f.getSourceType()))
                .setHeader("Source Type").setWidth("120px").setFlexGrow(0);
        ComboBox<String> eSrcType = new ComboBox<>();
        eSrcType.setItems("STATIC", "FIELD");
        binder.bind(eSrcType, ReportParamFilterMeta::getSourceType, ReportParamFilterMeta::setSourceType);
        cSrcType.setEditorComponent(eSrcType);

        Grid.Column<ReportParamFilterMeta> cSrc = grid.addColumn(f -> nz(f.getSourceName()))
                .setHeader("Source (value / param)").setFlexGrow(1);
        ComboBox<String> eSrc = new ComboBox<>();
        eSrc.setAllowCustomValue(true);
        eSrc.addCustomValueSetListener(e -> eSrc.setValue(e.getDetail()));
        if (otherParamNames != null) {
            List<String> names = otherParamNames.get();
            if (names != null) {
                eSrc.setItems(names);
            } else {
                eSrc.setItems(new java.util.ArrayList<>());
            }
        } else {
            eSrc.setItems(new java.util.ArrayList<>());
        }
        binder.bind(eSrc, ReportParamFilterMeta::getSourceName, ReportParamFilterMeta::setSourceName);
        cSrc.setEditorComponent(eSrc);

        grid.addItemDoubleClickListener(e -> grid.getEditor().editItem(e.getItem()));

        SafeButton add = new SafeButton("Add filter", VaadinIcon.PLUS.create(), e -> {
            ReportParamFilterMeta nf = new ReportParamFilterMeta();
            nf.setSourceType("STATIC");
            nf.setComparisonOperator("=");
            nf.setLogicalOperator("AND");
            nf.setFilterColumn("");
            nf.setSourceName("");
            nf.setParamMeta(param);
            param.getFilters().add(nf);
            grid.getDataProvider().refreshAll();
            grid.getEditor().editItem(nf);
        });
        SafeButton del = new SafeButton("Delete", VaadinIcon.TRASH.create(), e -> {
            var sel = new ArrayList<>(grid.getSelectedItems());
            if (!sel.isEmpty()) {
                if (grid.getEditor().isOpen()) grid.getEditor().cancel();
                param.getFilters().removeAll(sel);
                grid.getDataProvider().refreshAll();
            }
        });
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        HorizontalLayout bar = new HorizontalLayout(add, del);
        bar.setSpacing(true);
        grid.setItems(param.getFilters());
        add(bar, grid);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
