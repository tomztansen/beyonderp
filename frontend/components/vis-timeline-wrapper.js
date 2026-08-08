import { LitElement, html, css } from 'lit';
import { Timeline } from 'vis-timeline/standalone/umd/vis-timeline-graph2d.min.js';
import { DataSet } from 'vis-data/peer/esm/vis-data.js';
import 'vis-timeline/styles/vis-timeline-graph2d.min.css';

class VisTimelineWrapper extends LitElement {
  static get properties() {
    return {
      stackMode: { type: Boolean }
    };
  }

  static get styles() {
    return css`
      :host {
        display: block;
        width: 100%;
        height: 100%;
      }
      #visualization {
        width: 100%;
        height: 100%;
      }
      .vis-timeline {
        border: none;
        font-family: var(--lumo-font-family);
      }
      .vis-item {
        border-color: #2563eb;
        background-color: #3b82f6;
        color: white;
        border-radius: 4px;
        font-size: 13px;
        overflow: hidden;
      }
      .vis-item.vis-selected {
        border-color: #1e40af;
        background-color: #1d4ed8;
      }
      .vis-item.overcapacity {
        border-color: #dc2626;
        background-color: #ef4444;
      }
      .vis-item.overcapacity.vis-selected {
        border-color: #991b1b;
        background-color: #dc2626;
      }
      .vis-item.warning-capacity {
        border-color: #d97706;
        background-color: #f59e0b;
      }
      .vis-item.is-late {
        background-color: #ef4444 !important;
        border-color: #b91c1c !important;
        color: white !important;
        font-weight: bold !important;
      }
      .vis-item.is-late.vis-selected {
        background-color: #b91c1c !important;
        border-color: #7f1d1d !important;
        border-width: 2px !important;
      }
    `;
  }

  constructor() {
    super();
    this.stackMode = true;
    this.timeline = null;
    this.items = new DataSet([]);
    this.groups = new DataSet([]);
    this._capacityMap = {}; // { "mesin|date": { total, max, overcapacity } }
  }

  createRenderRoot() {
    return this; // Disable shadow DOM so global vis-timeline CSS applies
  }

  render() {
    return html`<div id="visualization"></div>`;
  }

  firstUpdated() {
    const container = this.querySelector('#visualization');

    const options = {
      groupOrder: function (a, b) {
        if (a.id === 'shipping_milestones') return -1;
        if (b.id === 'shipping_milestones') return 1;
        if (a.id === 'unassigned') return 1;
        if (b.id === 'unassigned') return -1;
        return (a.content || '').localeCompare(b.content || '');
      },
      editable: {
        add: false,
        updateTime: true,
        updateGroup: false, // Drag hanya horizontal (tanggal), tidak pindah mesin
        remove: false
      },
      margin: {
        item: 10,
        axis: 5
      },
      orientation: 'top',
      stack: this.stackMode,
      timeAxis: { scale: 'day', step: 1 },
      snap: (date) => {
        // Snap ke awal hari
        const d = new Date(date);
        d.setHours(0, 0, 0, 0);
        return d;
      },
      onMoving: (item, callback) => {
        // Allow the move visually while dragging
        callback(item);
      },
      onMove: (item, callback) => {
        // Notify server when drop is complete
        const formatLocal = (date) => {
            if (!date) return null;
            const d = new Date(date);
            const pad = (n) => n.toString().padStart(2, '0');
            return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
        };
        const newStart = formatLocal(item.start);
        const newEnd = formatLocal(item.end);
        const newGroup = item.group;

        // Notify server — server will decide whether to accept or reject
        this.$server.onItemMoved(item.id.toString(), newStart, newEnd, newGroup != null ? newGroup.toString() : null);
        callback(item); // proceed with move locally (server will refresh if needed)
      },
      zoomMin: 1000 * 60 * 60 * 24 * 7,    // min zoom: 7 days
      zoomMax: 1000 * 60 * 60 * 24 * 90,    // max zoom: 90 days
      align: 'left',
      tooltip: {
        followMouse: true,
        overflowMethod: 'cap'
      }
    };

    this.timeline = new Timeline(container, this.items, this.groups, options);

    this.timeline.on('select', (properties) => {
      if (properties.items && properties.items.length > 0) {
        this.$server.onItemClicked(properties.items[0].toString());
      }
    });

    this.timeline.on('contextmenu', (props) => {
      props.event.preventDefault();
      if (props.item) {
        // Send array of currently selected items to backend for merge if needed,
        // or just the right-clicked item for split
        this.$server.onItemContextMenu(props.item.toString(), this.timeline.getSelection().map(String));
      }
    });

    // Add today marker
    this.timeline.addCustomTime(new Date(), 'today');
    this.timeline.setCustomTimeMarker('Hari Ini', 'today');
  }

  setGroups(groupsArray) {
    this.groups.clear();
    if (groupsArray && groupsArray.length > 0) {
      this.groups.add(groupsArray);
    }
  }

  setItems(itemsArray) {
    this.items.clear();
    if (itemsArray && itemsArray.length > 0) {
      this.items.add(itemsArray);
    }

    if (this.timeline && itemsArray && itemsArray.length > 0) {
      this.timeline.fit({ animation: { duration: 300, easingFunction: 'easeInOutQuad' } });
    }
  }

  setCustomTimes(timesArray) {
    if (!this.timeline) return;
    
    if (!this._addedCustomTimes) {
      this._addedCustomTimes = [];
    }
    
    // Remove existing
    this._addedCustomTimes.forEach(id => {
      try { this.timeline.removeCustomTime(id); } catch (e) {}
    });
    this._addedCustomTimes = [];
    
    // Add new
    if (timesArray && timesArray.length > 0) {
      timesArray.forEach(t => {
        try {
          this.timeline.addCustomTime(t.date, t.id);
          // Use HTML in title to add emoji
          let titleHtml = t.isLate ? 
            "<span style='color:red; font-weight:bold;'>📦 " + t.title + "</span>" : 
            "<span style='color:green; font-weight:bold;'>📦 " + t.title + "</span>";
          this.timeline.setCustomTimeMarker(titleHtml, t.id);
          this._addedCustomTimes.push(t.id);
        } catch (e) {}
      });
    }
  }

  setSelection(itemIds) {
    if (this.timeline) {
      this.timeline.setSelection(itemIds, { focus: false });
    }
  }

  /**
   * Set capacity status for items — changes bar color based on overcapacity
   * @param {Array} capacityStatus - [{itemId, overcapacity: boolean, warningCapacity: boolean}]
   */
  setItemCapacityStatus(statusArray) {
    if (!statusArray) return;
    for (let i = 0; i < statusArray.length; i++) {
      const status = statusArray[i];
      const item = this.items.get(status.itemId);
      if (item) {
        let baseClass = item.className || '';
        baseClass = baseClass.replace(/\bovercapacity\b/g, '').replace(/\bwarning-capacity\b/g, '').trim();
        
        let capClass = '';
        if (status.overcapacity) {
          capClass = 'overcapacity';
        } else if (status.warningCapacity) {
          capClass = 'warning-capacity';
        }
        
        let newClassName = (baseClass + ' ' + capClass).trim();
        if (newClassName !== (item.className || '')) {
          this.items.update({ id: status.itemId, className: newClassName });
        }
      }
    }
  }

  setStackMode(stack) {
    this.stackMode = stack;
    if (this.timeline) {
      this.timeline.setOptions({ stack: stack });
    }
  }

  zoomIn() {
    if (this.timeline) {
      this.timeline.zoomIn(0.3);
    }
  }

  zoomOut() {
    if (this.timeline) {
      this.timeline.zoomOut(0.3);
    }
  }

  fitAll() {
    if (this.timeline) {
      this.timeline.fit({ animation: { duration: 300 } });
    }
  }
}

customElements.define('vis-timeline-wrapper', VisTimelineWrapper);
