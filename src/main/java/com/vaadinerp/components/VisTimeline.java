package com.vaadinerp.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.ClientCallable;
import elemental.json.JsonArray;

@Tag("vis-timeline-wrapper")
@NpmPackage(value = "vis-timeline", version = "^7.7.3")
@NpmPackage(value = "vis-data", version = "^7.1.9")
@NpmPackage(value = "moment", version = "^2.29.4")
@JsModule("./components/vis-timeline-wrapper.js")
public class VisTimeline extends Component implements HasSize {

    private boolean stackMode = true;

    public interface ItemMoveListener {
        void onItemMoved(String itemId, String newStart, String newEnd, String newGroup);
    }

    public interface ItemClickListener {
        void onItemClicked(String itemId);
    }

    public interface ItemContextMenuListener {
        void onItemContextMenu(String clickedItemId, String[] allSelectedItems);
    }

    private ItemMoveListener itemMoveListener;
    private ItemClickListener itemClickListener;
    private ItemContextMenuListener itemContextMenuListener;

    public VisTimeline() {
        // Default options
        setStackMode(true);
    }

    public void setGroups(JsonArray groups) {
        getElement().callJsFunction("setGroups", groups);
    }

    public void setItems(JsonArray items) {
        getElement().callJsFunction("setItems", items);
    }

    public void setCustomTimes(JsonArray times) {
        getElement().callJsFunction("setCustomTimes", times);
    }

    /**
     * Set capacity status for items — changes bar color based on overcapacity.
     * Each element in the array should have: itemId, overcapacity (boolean),
     * warningCapacity (boolean)
     */
    public void setItemCapacityStatus(JsonArray statusArray) {
        getElement().callJsFunction("setItemCapacityStatus", statusArray);
    }

    public void setStackMode(boolean stack) {
        this.stackMode = stack;
        getElement().callJsFunction("setStackMode", stack);
    }

    public boolean isStackMode() {
        return this.stackMode;
    }

    public void setSelection(JsonArray itemIds) {
        getElement().callJsFunction("setSelection", itemIds);
    }

    public void zoomIn() {
        getElement().callJsFunction("zoomIn");
    }

    public void zoomOut() {
        getElement().callJsFunction("zoomOut");
    }

    public void fitAll() {
        getElement().callJsFunction("fitAll");
    }

    public void setItemMoveListener(ItemMoveListener listener) {
        this.itemMoveListener = listener;
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @ClientCallable
    public void onItemMoved(String itemId, String newStart, String newEnd, String newGroup) {
        if (itemMoveListener != null) {
            itemMoveListener.onItemMoved(itemId, newStart, newEnd, newGroup);
        }
    }

    @ClientCallable
    public void onItemClicked(String itemId) {
        if (itemClickListener != null) {
            itemClickListener.onItemClicked(itemId);
        }
    }

    public void setItemContextMenuListener(ItemContextMenuListener listener) {
        this.itemContextMenuListener = listener;
    }

    @ClientCallable
    public void onItemContextMenu(String clickedItemId, String[] allSelectedItems) {
        if (itemContextMenuListener != null) {
            itemContextMenuListener.onItemContextMenu(clickedItemId, allSelectedItems);
        }
    }
}
