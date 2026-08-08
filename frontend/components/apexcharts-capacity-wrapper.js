import { LitElement, html, css } from 'lit';

class ApexCapacityWrapper extends LitElement {

  static get properties() {
    return {
      capacityMode: { type: String }
    };
  }

  static get styles() {
    return css`
      :host {
        display: block;
        width: 100%;
        height: 100%;
      }
      #chart-container {
        width: 100%;
        height: 100%;
        min-height: 200px;
      }
    `;
  }

    constructor() {
    super();
    this.chart = null;
    this.capacityMode = 'QTYBOX';
    this.weeklyView = false;
    this._chartData = null;
    this._maxCapacity = 80;
    this._ApexCharts = null;
  }

  createRenderRoot() {
    return this;
  }

  render() {
    return html`<div id="chart-container"></div>`;
  }

  async firstUpdated() {
    // Dynamic import ApexCharts
    try {
      const module = await import('apexcharts');
      this._ApexCharts = module.default || module;
    } catch (e) {
      console.error('Failed to load ApexCharts:', e);
    }
  }

  /**
   * Called from Java: setChartData(jsonArray)
   * Expected format: [
   *   { date: "2026-08-29", categories: [...seriesNames], values: [...stackedValues], maxCapacity: 80 }
   * ]
   */
  setChartData(dataArray, maxCapacity, capacityLabel, startDateStr, endDateStr) {
    if (!this._ApexCharts) {
      // Retry after ApexCharts loads
      setTimeout(() => this.setChartData(dataArray, maxCapacity, capacityLabel, startDateStr, endDateStr), 200);
      return;
    }

    this._maxCapacity = maxCapacity || 80;
    
    // Parse data: group by date, stack by idno/task
    const dates = [];
    const seriesMap = {};

    for (let i = 0; i < dataArray.length; i++) {
      const item = dataArray[i];
      let date = item.date || '';
      const taskName = item.taskName || 'Unknown';
      const value = item.value || 0;

      if (!date) continue;
      
      if (!dates.includes(date)) {
        dates.push(date);
      }
      if (!seriesMap[taskName]) {
        seriesMap[taskName] = {};
      }
      seriesMap[taskName][date] = (seriesMap[taskName][date] || 0) + value;
    }

    let minDt, maxDt;
    if (startDateStr && endDateStr) {
      minDt = new Date(startDateStr);
      maxDt = new Date(endDateStr);
    } else if (dates.length > 0) {
      dates.sort();
      minDt = new Date(dates[0]);
      maxDt = new Date(dates[dates.length - 1]);
    }

    if (minDt && maxDt) {
      if (this.weeklyView) {
        // Adjust min to Monday
        let day = minDt.getDay();
        let diff = minDt.getDate() - day + (day === 0 ? -6 : 1);
        minDt.setDate(diff);
        
        // Adjust max to Sunday
        day = maxDt.getDay();
        diff = maxDt.getDate() + (day === 0 ? 0 : 7 - day);
        maxDt.setDate(diff);
      }
      
      // Generate all dates between min and max
      const fullDates = [];
      let curr = new Date(minDt);
      while (curr <= maxDt) {
        fullDates.push(curr.toISOString().split('T')[0]);
        curr.setDate(curr.getDate() + 1);
      }
      dates.length = 0;
      dates.push(...fullDates);
    }

    // Build ApexCharts series
    const series = Object.keys(seriesMap).map(name => ({
      name: name,
      data: dates.map(d => seriesMap[name][d] || 0)
    }));

    // Calculate totals per date for annotation
    const totals = dates.map(d => {
      let sum = 0;
      Object.values(seriesMap).forEach(dateMap => {
        sum += (dateMap[d] || 0);
      });
      return sum;
    });

    const maxCap = this._maxCapacity;
    const modeLabel = capacityLabel || 'Qty Box';

    const options = {
      chart: {
        type: 'bar',
        height: '100%',
        stacked: true,
        toolbar: { show: true, tools: { download: true, zoom: true, pan: true } },
        fontFamily: 'var(--lumo-font-family, Inter, sans-serif)',
        background: 'transparent',
        animations: { enabled: true, easing: 'easeinout', speed: 400 },
        events: {
          dataPointSelection: (event, chartContext, config) => {
            if (config.seriesIndex !== undefined && config.seriesIndex !== null && config.dataPointIndex !== undefined) {
              const seriesName = config.w.config.series[config.seriesIndex].name;
              const timestamp = config.w.config.xaxis.categories[config.dataPointIndex];
              const dateStr = timestamp ? new Date(timestamp).toISOString().split('T')[0] : '';
              this.dispatchEvent(new CustomEvent('chart-item-click', { detail: { taskName: seriesName, date: dateStr } }));
            }
          }
        }
      },
      plotOptions: {
        bar: {
          horizontal: false,
          columnWidth: '60%',
          borderRadius: 3,
          dataLabels: { total: { enabled: true, style: { fontSize: '11px', fontWeight: 600 } } }
        }
      },
      dataLabels: {
        enabled: false
      },
      xaxis: {
        type: 'datetime',
        tickAmount: this.weeklyView ? 7 : (dates.length > 0 ? dates.length : undefined),
        range: this.weeklyView ? 7 * 24 * 60 * 60 * 1000 : undefined, // 7 days in milliseconds
        categories: dates.map(d => new Date(d).getTime()),
        labels: { 
          hideOverlappingLabels: false,
          style: { fontSize: '11px' },
          formatter: function(value, timestamp) {
            if (!timestamp) return '';
            const dt = new Date(timestamp);
            return dt.toLocaleDateString('id-ID', { weekday: 'short', day: '2-digit', month: 'short' });
          }
        }
      },
      yaxis: {
        title: { text: modeLabel, style: { fontSize: '12px' } },
        max: Math.max(maxCap * 1.3, Math.max(...totals) * 1.1),
        labels: { style: { fontSize: '11px' } }
      },
      annotations: {
        yaxis: [{
          y: maxCap,
          borderColor: '#ef4444',
          strokeDashArray: 0,
          borderWidth: 2,
          label: {
            text: 'Max Capacity: ' + maxCap,
            position: 'left',
            offsetX: 10,
            style: {
              color: '#fff',
              background: '#ef4444',
              fontSize: '11px',
              fontWeight: 600,
              padding: { left: 6, right: 6, top: 2, bottom: 2 }
            }
          }
        }]
      },
      colors: this._generateColors(Object.keys(seriesMap)),
      legend: {
        position: 'top',
        fontSize: '11px',
        markers: { size: 8, shape: 'circle' }
      },
      fill: { opacity: 1 },
      tooltip: {
        y: { formatter: (val) => val + ' ' + modeLabel },
        shared: true,
        intersect: false
      },
      grid: {
        borderColor: '#e2e8f0',
        strokeDashArray: 3
      },
      series: series
    };

    const container = this.querySelector('#chart-container');
    if (!container) return;

    if (this.chart) {
      // Coba pertahankan posisi zoom/pan saat ini (jika ada)
      try {
        const currentMin = this.chart.w.globals.minX;
        const currentMax = this.chart.w.globals.maxX;
        if (currentMin !== undefined && currentMax !== undefined) {
          options.xaxis.min = currentMin;
          options.xaxis.max = currentMax;
          delete options.xaxis.range; // Matikan range default agar min/max yang dipakai
        }
      } catch (e) {
        // Abaikan jika tidak bisa membaca globals
      }
      this.chart.updateOptions(options);
    } else {
      this.chart = new this._ApexCharts(container, options);
      this.chart.render();
    }
  }

  setCapacityMode(mode) {
    this.capacityMode = mode;
  }

  setWeeklyView(weekly) {
    this.weeklyView = weekly;
  }

  _generateColors(seriesNames) {
    const palette = [
      '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899',
      '#06b6d4', '#84cc16', '#f97316', '#6366f1', '#14b8a6',
      '#0ea5e9', '#a855f7', '#22c55e', '#eab308'
    ]; // removed red from palette to reserve for LATE
    const colors = [];
    let paletteIdx = 0;
    for (let i = 0; i < seriesNames.length; i++) {
      if (seriesNames[i].includes('(LATE)')) {
        colors.push('#ef4444'); // Red for late jobs
      } else {
        colors.push(palette[paletteIdx % palette.length]);
        paletteIdx++;
      }
    }
    return colors;
  }

  destroyChart() {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }
}

customElements.define('apex-capacity-wrapper', ApexCapacityWrapper);
