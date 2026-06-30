---
name: vaadin-grid-focus-fix
description: Fix Vaadin Grid row-edit focus timing issues where editor components don't appear or focus after adding a row
---

# Vaadin Grid Focus Timing Fix

Fix the common Vaadin Grid issue where clicking "Add Row" results in an empty row without editor components, requiring a second click to see the detail fields.

## Problem

When calling `editor.editItem(newRow)` immediately after `refreshAll()`, the DOM row hasn't been rendered yet. The editor opens on a non-existent row, so:
- The row appears empty (no editor components)
- The user must click again to see the detail fields
- Focus doesn't land on the first input

This is worse with many columns (e.g., 14 columns in SCROLL_MD) because DOM rendering takes longer.

## Solution Pattern

Replace direct `editItem()` calls with a JS-based approach that:
1. Polls the Vaadin Grid shadow DOM until the new row exists
2. Scrolls the row into view
3. Dispatches a `dblclick` event on the first cell — Vaadin Grid's built-in double-click-to-edit behavior opens the editor automatically

## Implementation

### For SubformGridField.java (SUBFORM_GRID component)

In the `btnAdd` click listener, replace the old approach:

```java
// OLD (broken):
refreshAll();
editor.editItem(newRow);
// focus doesn't work because row not in DOM yet
```

With the JS polling approach:

```java
// NEW (fixed):
btnAdd.addClickListener(e -> {
    // ... existing add-row logic ...
    refreshAll();
    
    String js = """
        const grid = this.shadowRoot.querySelector('vaadin-grid');
        if (!grid) return;
        
        let attempts = 0;
        const maxAttempts = 80;
        const interval = setInterval(() => {
            attempts++;
            const items = grid.items || [];
            const rows = grid.shadowRoot.querySelectorAll('#items .generated');
            
            if (rows.length > 0 || attempts >= maxAttempts) {
                clearInterval(interval);
                const lastRow = rows[rows.length - 1];
                if (lastRow) {
                    lastRow.scrollIntoView({behavior: 'smooth', block: 'center'});
                    const firstCell = lastRow.querySelector('td');
                    if (firstCell) {
                        firstCell.dispatchEvent(new MouseEvent('dblclick', {bubbles: true}));
                    }
                }
            }
        }, 25);
    """;
    UI.getCurrent().getPage().executeJs(js);
});
```

### For GenericMasterDetailFormView.java (MASTER_DETAIL type)

Same pattern in `setupAddRowButton()`:

```java
String js = """
    const grid = this.shadowRoot.querySelector('vaadin-grid');
    if (!grid) return;
    
    let attempts = 0;
    const interval = setInterval(() => {
        attempts++;
        const rows = grid.shadowRoot.querySelectorAll('#items .generated');
        
        if (rows.length > 0 || attempts >= 80) {
            clearInterval(interval);
            const lastRow = rows[rows.length - 1];
            if (lastRow) {
                lastRow.scrollIntoView({behavior: 'smooth', block: 'center'});
                const firstCell = lastRow.querySelector('td');
                if (firstCell) {
                    firstCell.dispatchEvent(new MouseEvent('dblclick', {bubbles: true}));
                }
            }
        }
    }, 25);
""";
UI.getCurrent().getPage().executeJs(js);
```

## Key Parameters

| Parameter | Value | Why |
|-----------|-------|-----|
| `maxAttempts` | 80 | Max polling iterations (80 × 25ms = 2 seconds) |
| `interval` | 25ms | Polling frequency — fast enough to catch DOM render |
| `scrollIntoView` | `{behavior: 'smooth', block: 'center'}` | Smooth scroll to center the row in viewport |
| Event | `dblclick` | Vaadin Grid's built-in editor trigger |

## Why This Works

1. **Polling** — waits for Vaadin Grid's shadow DOM to render the new row (server-side `refreshAll()` is async)
2. **`dblclick` dispatch** — triggers Vaadin Grid's native editor-open behavior, which handles all the editor component rendering
3. **No manual `focus()`** — Vaadin Grid's editor system handles focus automatically when opened via `dblclick`

## When to Apply

Apply this pattern when:
- User reports "add row shows empty row" or "editor doesn't open"
- User reports "must click twice to see detail fields"
- The issue is worse with forms that have many columns (>4)
- The issue appears after `refreshAll()` + `editItem()` calls

## Files Where This Pattern Is Used

- `SubformGridField.java` — SUBFORM_GRID component (handles SINGLE forms with child grids)
- `GenericMasterDetailFormView.java` — MASTER_DETAIL type forms

## Verification

After applying the fix:
1. Run `/compile-check` to verify build passes
2. Test by clicking "Tambah Baris" (Add Row) on the affected form
3. The editor should open immediately with the first input focused
4. Test with both few columns (4) and many columns (14) to confirm timing works
