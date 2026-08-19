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
    `;
  }

  constructor() {
    super();
    this.stackMode = true;
    this.timeline = null;
    this.items = new DataSet([]);
    this.groups = new DataSet([]);
    this._capacityMap = {}; // { "mesin|date": { total, max, overcapacity } }
    this.moveDebounceTimers = {}; // { itemId: timerId }
    this._timelineScale = 'daily'; // 'daily' or 'weekly'
  }

  createRenderRoot() {
    return this; // Disable shadow DOM so global vis-timeline CSS applies
  }

  render() {
    return html`
      <style>
        .vis-timeline {
          border: none;
          font-family: var(--lumo-font-family);
        }
        /* Mengatur lebar kolom pertama (Grup/Mesin) agar teksnya menurun (wrap) */
        .vis-labelset .vis-label {
          width: 200px !important;
          white-space: normal !important;
          word-wrap: break-word !important;
          padding: 8px !important;
          color: #1e293b !important;
        }
        .vis-labelset .vis-label .vis-inner {
          white-space: normal !important;
          word-wrap: break-word !important;
          color: #1e293b !important;
        }
        /* Nested group parent label */
        .vis-labelset .vis-label.vis-nesting-group {
          background-color: #e2e8f0 !important;
          color: #334155 !important;
          font-weight: 600 !important;
        }
        /* Fix the arrow and text alignment for parent groups */
        .vis-labelset .vis-label.vis-nesting-group .vis-inner {
          display: inline-block !important;
          vertical-align: top !important;
        }
        /* Nested group child label */
        .vis-labelset .vis-label.vis-nested-group {
          background-color: #f8fafc !important;
          color: #1e293b !important;
          padding-left: 20px !important;
        }
        .vis-labelset .vis-label.vis-nested-group .vis-inner {
          color: #1e293b !important;
          display: inline-block !important;
        }
        
        /* Mengisi header kosong di sudut kiri atas */
        .vis-panel.vis-top.vis-left {
          display: flex !important;
          align-items: center;
          justify-content: center;
          background-color: #f9fafb;
          border-bottom: 1px solid #e5e7eb;
        }
        .vis-panel.vis-top.vis-left::before {
          content: "Mesin / Stasiun Kerja";
          font-weight: 600;
          font-size: 13px;
          color: #4b5563;
          text-align: center;
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
          background: linear-gradient(to bottom, #ff0000 50%, #ffff00 50%) !important;
          border-color: #cc0000 !important;
          color: black !important;
          font-weight: bold !important;
        }
        .vis-item.is-late.vis-selected {
          background: linear-gradient(to bottom, #cc0000 50%, #d4d400 50%) !important;
          border-color: #990000 !important;
          border-width: 2px !important;
        }
        #visualization:focus {
          outline: none;
        }
        /* Weekly mode: items fill ~1 week width */
        .weekly-scale .vis-item {
          min-width: 12% !important;
        }
        .vis-tooltip {
          font-size: 11px !important;
          padding: 6px 10px !important;
          background-color: rgba(255, 255, 255, 0.96) !important;
          border: 1px solid #cbd5e1 !important;
          border-radius: 6px !important;
          box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1) !important;
          line-height: 1.4 !important;
          pointer-events: none !important; /* Membuat tooltip tidak menghalangi klik mouse */
          z-index: 9999 !important;
        }
        .weekly-scale .vis-item .vis-item-content {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          width: 100%;
        }
      </style>
      <div id="visualization" style="width: 100%; height: 100%;" tabindex="0"></div>
    `;
  }

  firstUpdated() {
    const container = this.querySelector('#visualization');

    const options = {
      groupOrder: function (a, b) {
        // Use explicit order index from server to guarantee parent-child grouping stability
        const orderA = a.orderIndex !== undefined ? a.orderIndex : 99999;
        const orderB = b.orderIndex !== undefined ? b.orderIndex : 99999;
        return orderA - orderB;
      },
      editable: {
        add: false,
        updateTime: true,
        updateGroup: true, // Drag vertikal dan horizontal
        remove: false
      },
      margin: {
        item: {
          horizontal: 0,
          vertical: 10
        },
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
      },
      multiselect: true,
      multiselectPerGroup: true
    };

    if (this._pendingWindowStart && this._pendingWindowEnd) {
      options.start = this._pendingWindowStart;
      options.end = this._pendingWindowEnd;
    }

    this.timeline = new Timeline(container, this.items, this.groups, options);

    // Inject custom header text into the top-left empty corner
    this.timeline.on('changed', () => {
      const topLeftPanel = container.querySelector('.vis-panel.vis-top.vis-left');
      if (topLeftPanel && !topLeftPanel.querySelector('.custom-header-text')) {
        topLeftPanel.innerHTML = '<div class="custom-header-text" style="display:flex; align-items:center; justify-content:center; width:100%; height:100%; font-weight:600; font-size:13px; color:#4b5563; background-color:#f9fafb; border-bottom:1px solid #e5e7eb; box-sizing:border-box; text-align:center; padding:5px;">Mesin / Stasiun Kerja</div>';
      }
    });

    this.timeline.on('select', (properties) => {
      container.focus(); // Ensure container receives keyboard events
      if (properties.items) {
        this.$server.onItemsSelected(properties.items.map(String));
      }
    });

    // Keyboard support for moving items left/right and up/down
    this._keydownListener = (e) => {
      const selection = this.timeline.getSelection();
      if (!selection || selection.length === 0) return;

      if (e.key === 'ArrowLeft' || e.key === 'ArrowRight' || e.key === 'ArrowUp' || e.key === 'ArrowDown') {
        e.preventDefault();

        let allGroups = null;
        if (e.key === 'ArrowUp' || e.key === 'ArrowDown') {
           allGroups = this.groups.get();
           allGroups.sort((a, b) => {
              let orderA = a.orderIndex !== undefined ? a.orderIndex : 99999;
              let orderB = b.orderIndex !== undefined ? b.orderIndex : 99999;
              return orderA - orderB;
           });
        }

        const daysToMove = (e.key === 'ArrowRight' ? 1 : (e.key === 'ArrowLeft' ? -1 : 0)) * (this._timelineScale === 'weekly' ? 7 : 1);

        selection.forEach(itemId => {
          const item = this.items.get(itemId);
          if (item) {
            let changed = false;

            if (daysToMove !== 0) {
                // Visual update date
                const newStart = new Date(item.start);
                newStart.setDate(newStart.getDate() + daysToMove);
                item.start = newStart;

                if (item.end) {
                  const newEnd = new Date(item.end);
                  newEnd.setDate(newEnd.getDate() + daysToMove);
                  item.end = newEnd;
                }
                changed = true;
            }

            if ((e.key === 'ArrowUp' || e.key === 'ArrowDown') && item.group) {
                const currentGroupIndex = allGroups.findIndex(g => g.id === item.group);
                if (currentGroupIndex !== -1) {
                   // Cari grup berikutnya ke atas/bawah
                   let step = e.key === 'ArrowDown' ? 1 : -1;
                   let newGroupIndex = currentGroupIndex + step;
                   if (newGroupIndex >= 0 && newGroupIndex < allGroups.length) {
                       item.group = allGroups[newGroupIndex].id;
                       changed = true;
                   }
                }
            }

            if (changed) {
                this.items.update(item);

                // Debounce server update
                if (this.moveDebounceTimers[itemId]) {
                  clearTimeout(this.moveDebounceTimers[itemId]);
                }
                this.moveDebounceTimers[itemId] = setTimeout(() => {
                  const formatLocal = (date) => {
                    if (!date) return null;
                    const d = new Date(date);
                    const pad = (n) => n.toString().padStart(2, '0');
                    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
                  };
                  this.$server.onItemMoved(
                    item.id.toString(), 
                    formatLocal(item.start), 
                    formatLocal(item.end), 
                    item.group != null ? item.group.toString() : null
                  );
                  delete this.moveDebounceTimers[itemId];
                }, 600); // 600ms debounce
            }
          }
        });
      }
    };
    container.addEventListener('keydown', this._keydownListener);

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
      console.log('[VIS-TIMELINE] setGroups received:', JSON.stringify(groupsArray, null, 2));
      // Use update instead of add to gracefully handle any duplicate IDs from the backend
      this.groups.update(groupsArray);
      
      if (this.timeline) {
        // Force redraw to ensure label heights and visibility are calculated correctly
        setTimeout(() => {
          this.timeline.redraw();
        }, 50);
      }
    }
  }

  setItems(itemsArray) {
    this.items.clear();
    if (itemsArray && itemsArray.length > 0) {
      this.items.add(itemsArray);
    }
  }

  setCustomTimes(timesArray) {
    if (!this.timeline) return;

    if (!this._addedCustomTimes) {
      this._addedCustomTimes = [];
    }

    // Remove existing
    this._addedCustomTimes.forEach(id => {
      try { this.timeline.removeCustomTime(id); } catch (e) { }
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
        } catch (e) { }
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

  setWindow(start, end) {
    this._pendingWindowStart = start;
    this._pendingWindowEnd = end;
    if (this.timeline) {
      this.timeline.setWindow(start, end, { animation: false });
    }
  }

  /**
   * Switch timeline scale between 'daily' and 'weekly'.
   * - daily: timeAxis day/1, snap to day, keyboard ±1 day
   * - weekly: timeAxis day/7, snap to Monday, keyboard ±7 days
   */
  setTimelineScale(mode) {
    this._timelineScale = mode; // 'daily' or 'weekly'
    if (!this.timeline) return;

    const container = this.querySelector('#visualization');
    const now = new Date();
    now.setHours(0, 0, 0, 0);

    if (mode === 'weekly') {
      container.classList.add('weekly-scale');
      this.timeline.setOptions({
        timeAxis: { scale: 'week', step: 1 },
        snap: (date) => {
          // Snap to Monday
          const d = new Date(date);
          const day = d.getDay();
          const diff = day === 0 ? -6 : 1 - day; // Monday = 1
          d.setDate(d.getDate() + diff);
          d.setHours(0, 0, 0, 0);
          return d;
        },
        zoomMin: 1000 * 60 * 60 * 24 * 14,   // min zoom: 2 weeks
        zoomMax: 1000 * 60 * 60 * 24 * 365,   // max zoom: 1 year
      });
      // Zoom out to show ~8 weeks
      const start = new Date(now);
      start.setDate(start.getDate() - 7);
      const end = new Date(now);
      end.setDate(end.getDate() + 7 * 8);
      this.timeline.setWindow(start, end, { animation: { duration: 300, easingFunction: 'easeInOutQuad' } });
    } else {
      container.classList.remove('weekly-scale');
      this.timeline.setOptions({
        timeAxis: { scale: 'day', step: 1 },
        snap: (date) => {
          const d = new Date(date);
          d.setHours(0, 0, 0, 0);
          return d;
        },
        zoomMin: 1000 * 60 * 60 * 24 * 7,    // min zoom: 7 days
        zoomMax: 1000 * 60 * 60 * 24 * 90,   // max zoom: 90 days
      });
      // Restore default view: fit all items
      this.timeline.fit({ animation: { duration: 300, easingFunction: 'easeInOutQuad' } });
    }

    this.timeline.redraw();
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

  destroyTimeline() {
    if (this.timeline) {
      this.timeline.destroy();
      this.timeline = null;
    }
    if (this._keydownListener) {
      const container = this.querySelector('#visualization');
      if (container) {
        container.removeEventListener('keydown', this._keydownListener);
      }
      this._keydownListener = null;
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.destroyTimeline();
  }
}

customElements.define('vis-timeline-wrapper', VisTimelineWrapper);
