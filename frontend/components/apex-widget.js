import { LitElement, html } from 'lit';

// Palet seragam dashboard (tanpa merah, dicadangkan untuk error/late)
const PALETTE = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16', '#f97316', '#6366f1', '#14b8a6'];

class ApexWidget extends LitElement {
  createRenderRoot() { return this; }
  render() { return html`<div class="apex-c" style="width:100%;min-height:240px"></div>`; }

  constructor() {
    super();
    this.chart = null;
    this.cfg = null;
    this.selected = -1;
    this._needsApply = false;
    this._lastW = 0;
    this._ro = null;
    this._applyQueued = false; // F2: debounce microtask
  }

  connectedCallback() {
    super.connectedCallback();
    this.style.display = 'block';
    this.style.width = '100%';
    if (!this._ro) {
      this._ro = new ResizeObserver(entries => {
        const w = entries[0]?.contentRect.width || this.clientWidth;
        if (!w || w === this._lastW) return;
        this._lastW = w;
        if (this._needsApply || !this.chart) {
          this._needsApply = false;
          if (this.cfg && this._Apex) this._apply();
        }
        // else: ApexCharts handles window resize itself; tidak perlu re-create
      });
      this._ro.observe(this);
    }
  }

  async firstUpdated() {
    const m = await import('apexcharts');
    this._Apex = m.default || m;
    if (this.cfg) this._apply();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    if (this._ro) { this._ro.disconnect(); this._ro = null; }
    this._lastW = 0; // F1: reset agar ResizeObserver re-apply saat re-attach
    this.destroyChart();
  }

  setConfig(json) {
    this.cfg = typeof json === 'string' ? JSON.parse(json) : json;
    if (this._Apex) this._apply();
  }

  highlight(index) {
    this.selected = index == null ? -1 : Number(index);
    if (this.cfg && this._Apex) this._apply();
  }

  destroyChart() {
    if (this.chart) { this.chart.destroy(); this.chart = null; }
  }

  // F2: debounce – setConfig + highlight yang tiba berurutan hanya build sekali
  _apply() {
    if (this._applyQueued) return;
    this._applyQueued = true;
    Promise.resolve().then(() => { this._applyQueued = false; this._applyNow(); });
  }

  // F3: destroyChart dulu sebelum build options; guard series kosong
  _applyNow() {
    const el = this.querySelector('.apex-c');
    if (!el) return;
    if (!el.clientWidth) { this._needsApply = true; return; }
    this.destroyChart(); // F3: destroy dulu agar tidak ada chart lama jika _options() throw
    try {
      const opts = this._options();
      this.chart = new this._Apex(el, opts);
      this.chart.render()
        .catch(e => { console.error('[apex-widget] render', e); });
    } catch (e) { console.error('[apex-widget] _applyNow error', e); }
  }

  // Redupkan yang tidak terpilih: bar/pie via fungsi/array warna, line via anotasi kategori
  _options() {
    const c = this.cfg;
    const type = c.type;
    // F4: inlined _colors()
    const colors = (c.colors && c.colors.length) ? c.colors : PALETTE;
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
      tooltip: { shared: type !== 'pie', intersect: false },
      noData: { text: 'No data' } // F3: fallback label saat series kosong
    };
    if (type === 'pie') {
      // F3: guard c.series[0]
      const s = c.series.length ? c.series[0].data : [];
      return Object.assign(base, {
        series: s,
        labels: c.categories,
        colors: c.categories.map((_, i) => sel >= 0 && i !== sel ? dim(colors[i % colors.length]) : colors[i % colors.length])
      });
    }
    if (type === 'radialBar') {
      // F3: guard c.series[0]
      const s = c.series.length ? c.series[0].data : [];
      const name = c.series.length ? c.series[0].name : '';
      return Object.assign(base, {
        series: s,
        labels: [name],
        colors: [colors[0]],
        plotOptions: { radialBar: { hollow: { size: '60%' }, dataLabels: { value: { formatter: (v) => Math.round(v) + '%' } } } }
      });
    }
    const series = c.series.map((s) => ({ name: s.name, type: s.type || undefined, data: s.data }));
    const opts = Object.assign(base, {
      series,
      xaxis: { categories: c.categories },
      plotOptions: { bar: { horizontal: !!c.horizontal, columnWidth: '60%' } },
      stroke: { width: series.map(s => (s.type === 'line' || type === 'line') ? 2 : 0), curve: 'smooth' },
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
