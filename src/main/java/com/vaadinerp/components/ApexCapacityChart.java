package com.vaadinerp.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonArray;

@Tag("apex-capacity-wrapper")
@NpmPackage(value = "apexcharts", version = "^4.5.0")
@JsModule("./components/apexcharts-capacity-wrapper.js")
public class ApexCapacityChart extends Component implements HasSize {

    @DomEvent("chart-item-click")
    public static class ChartItemClickEvent extends ComponentEvent<ApexCapacityChart> {
        private final String taskName;
        private final String date;

        public ChartItemClickEvent(ApexCapacityChart source, boolean fromClient,
                @EventData("event.detail.taskName") String taskName,
                @EventData("event.detail.date") String date) {
            super(source, fromClient);
            this.taskName = taskName;
            this.date = date;
        }

        public String getTaskName() {
            return taskName;
        }

        public String getDate() {
            return date;
        }
    }

    public Registration addChartItemClickListener(ComponentEventListener<ChartItemClickEvent> listener) {
        return addListener(ChartItemClickEvent.class, listener);
    }

    /**
     * Set chart data for capacity histogram.
     *
     * @param data        JSON array of {date, taskName, value} objects
     * @param maxCapacity maximum capacity line value (e.g. 80)
     * @param label       capacity label (e.g. "Qty Box" or "Weight (kg)")
     */
    public void setChartData(JsonArray data, int maxCapacity, String label, String startDate, String endDate) {
        getElement().callJsFunction("setChartData", data, maxCapacity, label, startDate, endDate);
    }

    /**
     * Set the capacity mode (QTYBOX or WEIGHT).
     */
    public void setCapacityMode(String mode) {
        getElement().callJsFunction("setCapacityMode", mode);
    }

    /**
     * Set whether the chart should group data by week.
     */
    public void setWeeklyView(boolean weeklyView) {
        getElement().callJsFunction("setWeeklyView", weeklyView);
    }

    /**
     * Destroy the chart instance to free resources.
     */
    public void destroyChart() {
        getElement().callJsFunction("destroyChart");
    }

    public void cleanup() {
        destroyChart();
    }
}
