package com.vaadinerp.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import java.util.function.Consumer;

public class PaginationBar extends HorizontalLayout {

    private int currentPage = 0; // 0-indexed
    private int pageSize = 50;
    private long totalRecords = 0;

    private final ComboBox<Integer> pageSizeCombo = new ComboBox<>("Baris / hal");
    private final Button btnFirst = new Button("<< Pertama");
    private final Button btnPrev = new Button("< Prev");
    private final Button btnNext = new Button("Next >");
    private final Button btnLast = new Button("Terakhir >>");
    private final Span pageInfo = new Span("Halaman 0 dari 0");
    private final IntegerField jumpField = new IntegerField("Ke Hal");
    private final Button btnGo = new Button("Go");

    private Consumer<Void> onPageChange;

    public PaginationBar(Consumer<Void> onPageChange) {
        this.onPageChange = onPageChange;

        setWidthFull();
        setAlignItems(Alignment.BASELINE);
        setSpacing(true);
        getStyle().set("padding", "5px 10px")
                  .set("background-color", "var(--lumo-contrast-5pct)")
                  .set("border-radius", "4px")
                  .set("margin-top", "5px");

        pageSizeCombo.setItems(25, 50, 100, 250, 500, 1000);
        pageSizeCombo.setValue(50);
        pageSizeCombo.setWidth("110px");
        pageSizeCombo.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() != pageSize) {
                pageSize = e.getValue();
                currentPage = 0;
                triggerChange();
            }
        });

        btnFirst.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnPrev.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnNext.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnLast.addThemeVariants(ButtonVariant.LUMO_SMALL);
        btnGo.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

        btnFirst.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage = 0;
                triggerChange();
            }
        });

        btnPrev.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                triggerChange();
            }
        });

        btnNext.addClickListener(e -> {
            int maxPages = getMaxPages();
            if ((currentPage + 1) < maxPages) {
                currentPage++;
                triggerChange();
            }
        });

        btnLast.addClickListener(e -> {
            int maxPages = getMaxPages();
            if ((currentPage + 1) < maxPages && maxPages > 0) {
                currentPage = maxPages - 1;
                triggerChange();
            }
        });

        jumpField.setWidth("80px");
        jumpField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        jumpField.setMin(1);

        btnGo.addClickListener(e -> {
            if (jumpField.getValue() != null) {
                int target = jumpField.getValue() - 1;
                int maxPages = getMaxPages();
                if (target < 0) target = 0;
                if (target >= maxPages && maxPages > 0) target = maxPages - 1;
                if (target != currentPage) {
                    currentPage = target;
                    triggerChange();
                }
            }
        });

        pageInfo.getStyle().set("font-weight", "600").set("margin", "0 10px");

        HorizontalLayout navButtons = new HorizontalLayout(btnFirst, btnPrev, pageInfo, btnNext, btnLast);
        navButtons.setAlignItems(Alignment.CENTER);
        
        HorizontalLayout jumpLayout = new HorizontalLayout(jumpField, btnGo);
        jumpLayout.setAlignItems(Alignment.BASELINE);

        add(pageSizeCombo, navButtons, jumpLayout);
        expand(navButtons);
        
        updateUI();
    }

    private void triggerChange() {
        if (onPageChange != null) {
            onPageChange.accept(null);
        }
    }

    public int getOffset() {
        return currentPage * pageSize;
    }

    public int getLimit() {
        return pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void resetPage() {
        this.currentPage = 0;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
        int maxPages = getMaxPages();
        if (currentPage >= maxPages && maxPages > 0) {
            currentPage = maxPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        updateUI();
    }

    public int getMaxPages() {
        if (totalRecords == 0 || pageSize == 0) return 1;
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    private void updateUI() {
        int maxPages = getMaxPages();
        int displayPage = totalRecords == 0 ? 0 : (currentPage + 1);
        pageInfo.setText("Halaman " + displayPage + " dari " + maxPages + " (Total: " + String.format("%,d", totalRecords) + " data)");
        
        btnFirst.setEnabled(currentPage > 0);
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled((currentPage + 1) < maxPages);
        btnLast.setEnabled((currentPage + 1) < maxPages);
        
        jumpField.setMax(maxPages);
        jumpField.setValue(displayPage);
    }
}
