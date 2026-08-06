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
      groupOrder: 'content',
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
      zoomMin: 1000 * 60 * 60 * 24 * 3,    // min zoom: 3 days
      zoomMax: 1000 * 60 * 60 * 24 * 90,    // max zoom: 90 days
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
        let className = '';
        if (status.overcapacity) {
          className = 'overcapacity';
        } else if (status.warningCapacity) {
          className = 'warning-capacity';
        }
        this.items.update({ id: status.itemId, className: className });
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
