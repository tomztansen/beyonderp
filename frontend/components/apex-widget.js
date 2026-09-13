import { LitElement, html, css } from 'lit';

// Palet seragam dashboard (tanpa merah, dicadangkan untuk error/late)
const PALETTE = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16', '#f97316', '#6366f1', '#14b8a6'];

class ApexWidget extends LitElement {
  static get styles() {
    return css`:host { display: block; width: 100%; } #c { width: 100%; min-height: 240px; }`;
  }

  createRenderRoot() { return this; }
  render() { return html`<div id="c"></div>`; }

  constructor() {
    super();
    this.chart = null;
    this.cfg = null;
    this.selected = -1;
  }

  async firstUpdated() {
    const m = await import('apexcharts');
    this._Apex = m.default || m;
    if (this.cfg) this._apply();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.destroyChart();
  }

  setConfig(json) {
    this.cfg = typeof json === 'string' ? JSON.parse(json) : json;
    if (this._Apex) this._apply();
  }

  highlight(index) {
    this.selected = index == null ? -1 : Number(index);
    if (this.chart && this.cfg) this.chart.updateOptions(this._options(), false, true);
  }

  destroyChart() {
    if (this.chart) { this.chart.destroy(); this.chart = null; }
  }

  _apply() {
    const el = this.querySelector('#c');
    if (!el) return;
    const opts = this._options();
    if (this.chart) {
      this.chart.updateOptions(opts, false, true);
    } else {
      this.chart = new this._Apex(el, opts);
      this.chart.render();
    }
  }

  _colors() {
    const base = (this.cfg.colors && this.cfg.colors.length) ? this.cfg.colors : PALETTE;
    return base;
  }

  // Redupkan yang tidak terpilih: bar/pie via fungsi/array warna, line via anotasi kategori
  _options() {
    const c = this.cfg;
    const type = c.type;
    const colors = this._colors();
    const sel = this.selected;
    const dim = (hex) => hex + '55';
    const base = {
      chart: {
        type: type === 'radialBar' ? 'radialBar' : type,
        height: 260,
        toolbar: { show: false },
        animations: { enabled: false },
        stacked: !!c.stacked,
        events: {
          dataPointSelection: (e, ctx, cfg) => {
            this.dispatchEvent(new CustomEvent('widget-select', {
              detail: { index: cfg.dataPointIndex, seriesIndex: cfg.seriesIndex }, bubbles: true, composed: true }));
          }
        }
      },
      states: { active: { filter: { type: 'none' } } },
      dataLabels: { enabled: type === 'pie' || type === 'radialBar' },
      legend: { position: 'bottom', show: c.series.length > 1 || type === 'pie' },
      tooltip: { shared: type !== 'pie' }
    };
    if (type === 'pie') {
      return Object.assign(base, {
        series: c.series[0].data,
        labels: c.categories,
        colors: c.categories.map((_, i) => sel >= 0 && i !== sel ? dim(colors[i % colors.length]) : colors[i % colors.length])
      });
    }
    if (type === 'radialBar') {
      return Object.assign(base, {
        series: c.series[0].data,
        labels: [c.series[0].name],
        colors: [colors[0]],
        plotOptions: { radialBar: { hollow: { size: '60%' }, dataLabels: { value: { formatter: (v) => Math.round(v) + '%' } } } }
      });
    }
    const series = c.series.map((s) => ({ name: s.name, type: s.type || undefined, data: s.data }));
    const opts = Object.assign(base, {
      series,
      xaxis: { categories: c.categories },
      plotOptions: { bar: { horizontal: !!c.horizontal, columnWidth: '60%' } },
      stroke: { width: type === 'line' ? 2 : 0, curve: 'smooth' },
      colors: colors
    });
    if (sel >= 0) {
      if (type === 'bar' && series.length === 1) {
        opts.colors = [({ dataPointIndex }) => dataPointIndex === sel ? colors[0] : dim(colors[0])];
      } else {
        opts.annotations = { xaxis: [{ x: c.categories[sel], borderColor: '#111827', label: { text: '▼', borderWidth: 0 } }] };
      }
    } else {
      opts.annotations = { xaxis: [] };
    }
    return opts;
  }
}

customElements.define('apex-widget', ApexWidget);
