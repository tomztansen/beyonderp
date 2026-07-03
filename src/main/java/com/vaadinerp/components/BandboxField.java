package com.vaadinerp.components;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.function.Consumer;

public class BandboxField<T, V> extends CustomField<V> {

    private final TextField displayField = new TextField();
    private final Button dropdownBtn = new Button(new Icon(VaadinIcon.CHEVRON_DOWN_SMALL));
    private final Button clearBtn = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
    private final Dialog popup = new Dialog();

    private final Grid<T> grid = new Grid<>();
    private final TextField searchField = new TextField();

    private V selectedValue;
    private T selectedItem;

    private final java.util.Map<String, FilterCondition> activeFilters = new java.util.HashMap<>();

    public void setFilterValue(FilterCondition condition) {
        if (condition.getValue() == null || condition.getValue().toString().trim().isEmpty()) {
            activeFilters.remove(condition.getFilterId());
        } else {
            activeFilters.put(condition.getFilterId(), condition);
        }
    }

    public java.util.Map<String, FilterCondition> getActiveFilters() {
        return activeFilters;
    }

    private ItemLabelGenerator<T> labelGenerator;
    private ItemValueGenerator<T, V> valueGenerator;
    private Consumer<T> onSelectListener;

    // For eagerly loaded in-memory data
    private DataFetchCallback<T> dataFetchCallback;

    // For lazy loaded paginated data from database
    private ConfigurableFilterDataProvider<T, Void, String> dataProvider;

    private ItemFinderCallback<T, V> itemFinder;

    public interface ItemLabelGenerator<T> {
        String apply(T item);
    }

    public interface ItemValueGenerator<T, V> {
        V apply(T item);
    }

    public interface ItemFinderCallback<T, V> {
        T find(V value);
    }

    public interface DataFetchCallback<T> {
        java.util.List<T> fetch(String keyword);
    }

    public void setItemFinder(ItemFinderCallback<T, V> itemFinder) {
        this.itemFinder = itemFinder;
    }

    public BandboxField(String label) {
        setLabel(label);

        setupDisplayField();
        setupPopup();
    }

    private void setupDisplayField() {
        displayField.setReadOnly(true);
        displayField.setWidth("300px");

        dropdownBtn.addThemeVariants(ButtonVariant.LUMO_ICON);
        dropdownBtn.addClickListener(e -> openPopup());

        clearBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        clearBtn.addClickListener(e -> this.clear());

        displayField.getElement().addEventListener("click", e -> openPopup());

        HorizontalLayout searchLayout = new HorizontalLayout(displayField, clearBtn, dropdownBtn);
        searchLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
        searchLayout.setSpacing(false);
        searchLayout.getThemeList().add("spacing-xs");

        add(searchLayout);
    }

    private void setupPopup() {
        popup.setWidth("700px");
        popup.setCloseOnOutsideClick(true);
        popup.setModal(false); // Modeless agar terasa seperti dropdown

        // Search field
        searchField.setPlaceholder("Cari data...");
        searchField.setWidthFull();
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setValueChangeTimeout(300);
        searchField.addValueChangeListener(e -> executeSearch(e.getValue()));
        searchField.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, e -> selectFirstItemOrSelected());

        // Grid
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setAllRowsVisible(true);
        grid.setEmptyStateText("Tidak ada data");

        grid.addItemDoubleClickListener(e -> doSelect(e.getItem()));
        
        com.vaadin.flow.component.Shortcuts.addShortcutListener(grid, this::selectFirstItemOrSelected, com.vaadin.flow.component.Key.ENTER);

        // Layout
        VerticalLayout layout = new VerticalLayout(searchField, grid);
        layout.setWidthFull();
        layout.getStyle().set("max-height", "400px").set("overflow-y", "auto");
        layout.setPadding(false);
        layout.setSpacing(true);

        popup.add(layout);

        // Footer Buttons
        Button btnPilih = new Button("Pilih", e -> {
            grid.asSingleSelect().getOptionalValue().ifPresent(this::doSelect);
        });
        btnPilih.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnBatal = new Button("Batal", e -> popup.close());
        popup.getFooter().add(btnBatal, btnPilih);

        // Load data initially
        popup.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                executeSearch(searchField.getValue());
                searchField.focus();

                // Posisikan Dialog secara dinamis (di bawah atau di atas jika overflow)
                popup.getElement().executeJs(
                        "setTimeout(() => {" +
                                "  const rect = $0.getBoundingClientRect();" +
                                "  const overlay = this.$.overlay;" +
                                "  if (overlay) {" +
                                "    overlay.style.alignItems = 'flex-start';" +
                                "    overlay.style.justifyContent = 'flex-start';" +
                                "    const overlayPart = overlay.shadowRoot.querySelector('[part=\"overlay\"]');" +
                                "    if (overlayPart) {" +
                                "      overlayPart.style.position = 'absolute';" +
                                "      overlayPart.style.margin = '0';" +
                                "      const popupHeight = overlayPart.offsetHeight || 480;" +
                                "      const spaceBelow = window.innerHeight - rect.bottom;" +
                                "      const spaceAbove = rect.top;" +
                                "      if (spaceBelow < popupHeight && spaceAbove > spaceBelow) {" +
                                "        overlayPart.style.top = Math.max(0, rect.top - popupHeight - 5) + 'px';" +
                                "      } else {" +
                                "        overlayPart.style.top = rect.bottom + 'px';" +
                                "      }" +
                                "      const popupWidth = overlayPart.offsetWidth || 700;" +
                                "      if (rect.left + popupWidth > window.innerWidth) {" +
                                "        overlayPart.style.left = Math.max(0, window.innerWidth - popupWidth - 15) + 'px';" +
                                "      } else {" +
                                "        overlayPart.style.left = rect.left + 'px';" +
                                "      }" +
                                "    }" +
                                "  }" +
                                "}, 50);",
                        displayField.getElement());

                // Register click-outside and focus-outside listeners
                this.getElement().executeJs(
                        "const self = this;" +
                        "if (self._bandboxClick) { document.removeEventListener('mousedown', self._bandboxClick, true); }" +
                        "if (self._bandboxFocus) { document.removeEventListener('focusin', self._bandboxFocus, true); }" +
                        "const cleanUp = () => {" +
                        "  if (self._bandboxClick) { document.removeEventListener('mousedown', self._bandboxClick, true); delete self._bandboxClick; }" +
                        "  if (self._bandboxFocus) { document.removeEventListener('focusin', self._bandboxFocus, true); delete self._bandboxFocus; }" +
                        "};" +
                        "self._bandboxClick = (event) => {" +
                        "  const path = event.composedPath();" +
                        "  if (!path.includes($0) && !path.includes($1)) {" +
                        "    self.$server.closeFromClient();" +
                        "    cleanUp();" +
                        "  }" +
                        "};" +
                        "self._bandboxFocus = (event) => {" +
                        "  const path = event.composedPath();" +
                        "  if (!path.includes($0) && !path.includes($1)) {" +
                        "    self.$server.closeFromClient();" +
                        "    cleanUp();" +
                        "  }" +
                        "};" +
                        "document.addEventListener('mousedown', self._bandboxClick, true);" +
                        "document.addEventListener('focusin', self._bandboxFocus, true);",
                        displayField.getElement(), layout.getElement()
                );
            } else {
                // Remove listeners when closed
                this.getElement().executeJs(
                        "if (this._bandboxClick) { document.removeEventListener('mousedown', this._bandboxClick, true); delete this._bandboxClick; }" +
                        "if (this._bandboxFocus) { document.removeEventListener('focusin', this._bandboxFocus, true); delete this._bandboxFocus; }"
                );
            }
        });
    }

    @com.vaadin.flow.component.ClientCallable
    public void closeFromClient() {
        popup.close();
    }

    private void openPopup() {
        if (!popup.isOpened()) {
            popup.open();
        }
    }

    private void selectFirstItemOrSelected() {
        grid.asSingleSelect().getOptionalValue().ifPresentOrElse(
            this::doSelect,
            () -> {
                try {
                    T firstItem = grid.getGenericDataView().getItem(0);
                    if (firstItem != null) {
                        doSelect(firstItem);
                    }
                } catch (IndexOutOfBoundsException | NullPointerException ignored) {
                }
            }
        );
    }

    @SuppressWarnings("unchecked")
    private void doSelect(T item) {
        this.selectedItem = item;

        if (valueGenerator != null) {
            this.selectedValue = valueGenerator.apply(item);
        } else {
            this.selectedValue = (V) item; // Fallback jika T dan V sama
        }

        if (labelGenerator != null) {
            String lbl = labelGenerator.apply(item);
            setSelectedLabel((lbl != null && !lbl.isEmpty()) ? lbl : (this.selectedValue != null ? this.selectedValue.toString() : ""));
        } else if (this.selectedValue != null) {
            setSelectedLabel(this.selectedValue.toString());
        }

        if (onSelectListener != null) {
            onSelectListener.accept(item);
        }

        setValue(this.selectedValue); // Memicu form binder
        popup.close();
    }

    private void executeSearch(String keyword) {
        if (dataProvider != null) {
            dataProvider.setFilter(keyword);
        } else if (dataFetchCallback != null) {
            grid.setItems(dataFetchCallback.fetch(keyword));
        }
    }

    public Grid<T> getGrid() {
        return grid;
    }

    /**
     * Menggunakan fetch secara manual (Data in-memory / list sederhana)
     */
    public void setDataFetchCallback(DataFetchCallback<T> callback) {
        this.dataFetchCallback = callback;
    }

    /**
     * Menambahkan kapabilitas Lazy Loading (Pagination dari database)
     */
    public void setDataProvider(CallbackDataProvider.FetchCallback<T, String> fetchCallback,
            CallbackDataProvider.CountCallback<T, String> countCallback) {
        DataProvider<T, String> baseProvider = DataProvider.fromFilteringCallbacks(fetchCallback, countCallback);
        this.dataProvider = baseProvider.withConfigurableFilter();
        this.grid.setItems(this.dataProvider);
    }

    public void setItemLabelGenerator(ItemLabelGenerator<T> labelGenerator) {
        this.labelGenerator = labelGenerator;
    }

    public void setItemValueGenerator(ItemValueGenerator<T, V> valueGenerator) {
        this.valueGenerator = valueGenerator;
    }

    public void setOnSelectListener(Consumer<T> listener) {
        this.onSelectListener = listener;
    }

    public void setSelectedLabel(String label) {
        displayField.setValue(label != null ? label : "");
    }

    public void setPlaceholder(String placeholder) {
        displayField.setPlaceholder(placeholder);
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    public String getDisplayLabel() {
        return displayField.getValue();
    }

    @Override
    protected V generateModelValue() {
        return selectedValue;
    }

    @Override
    public void clear() {
        super.clear(); // This triggers ValueChangeEvent with null
        this.selectedItem = null;
        displayField.clear();
    }

    @Override
    protected void setPresentationValue(V newPresentationValue) {
        this.selectedValue = newPresentationValue;
        if (newPresentationValue == null) {
            displayField.clear();
            this.selectedItem = null;
        } else {
            if (itemFinder != null) {
                this.selectedItem = itemFinder.find(newPresentationValue);
                if (this.selectedItem != null && labelGenerator != null) {
                    String lbl = labelGenerator.apply(this.selectedItem);
                    displayField.setValue((lbl != null && !lbl.isEmpty()) ? lbl : newPresentationValue.toString());
                } else {
                    displayField.setValue(newPresentationValue.toString());
                }
            } else {
                displayField.setValue(newPresentationValue.toString());
            }
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        getElement().setProperty("readonly", readOnly);
        dropdownBtn.setEnabled(!readOnly);
        clearBtn.setEnabled(!readOnly);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        dropdownBtn.setEnabled(enabled);
        clearBtn.setEnabled(enabled);
    }

    @Override
    public void focus() {
        displayField.focus();
    }
}
