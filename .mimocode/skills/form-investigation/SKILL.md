---
name: form-investigation
description: Investigate how a form works in the Vaadinerp metadata-driven system — trace from form code to view to components
---

# Form Investigation

Investigate how a specific form works in Vaadinerp by tracing from metadata definition through to runtime rendering.

## When to Use

- User asks "how does form X work?" or "check form X"
- User asks about a specific form's fields, grid, or behavior
- Debugging form-related issues (missing fields, wrong behavior, rendering problems)

## Investigation Procedure

### Step 1: Find the Form Metadata

Search for the form name/code in `DataInitializer.java`:

```
grep "<form_name>" src/main/java/com/vaadinerp/DataInitializer.java
```

This tells you:
- The `formCode` (e.g., `SCROLL_MD`, `INVOICE_MD`)
- The `formType` (`SINGLE` or `MASTER_DETAIL`)
- The master table name and primary key
- The master fields and their component types

### Step 2: Determine Which View Handles It

Based on `formType`:
- **`SINGLE`** → `GenericFormView.java` (route: `/generic/{formCode}`)
  - If it has `SUBFORM_GRID` component → detail grid is handled by `SubformGridField.java`
- **`MASTER_DETAIL`** → `GenericMasterDetailFormView.java` (route: `/masterdetail/{formCode}`)

### Step 3: Read the View File

Read the relevant view to understand:
- How the grid is built (columns, filters, sorting)
- How data loading works (`refreshAll()`, `fetchTableData()`)
- How editing works (inline editor, add/delete rows)
- How double-click navigates to detail

Key code locations in `GenericFormView.java`:
- `buildGrid()` — grid column setup
- `applyFilters()` — filter logic
- `setupAddButton()` — add row logic

Key code locations in `GenericMasterDetailFormView.java`:
- `buildMasterGrid()` — master grid setup
- `buildDetailGrid()` — detail grid setup
- `masterGrid.addItemDoubleClickListener()` — navigation to detail

### Step 4: Check Components

If the form uses special components:
- **`SUBFORM_GRID`** → read `SubformGridField.java`
- **`BANDBOX`** → read `BandboxField.java`
- **`COMBOBOX`** with LOV → check `LovComboBox.java`
- **`CHOSENBOX`** → check `LovSelect.java`

### Step 5: Check for Cascading Filters

Look for `FieldFilterMeta` entries in `DataInitializer.java` for the form's fields.
- `STATIC` — hardcoded value
- `FIELD` — value from another form field
- `QUERY` — URL query parameter

### Step 6: Report Findings

Present a structured summary:
1. Form metadata (code, type, table, fields)
2. Which view handles it and why
3. Grid configuration (columns, filters, sorting)
4. Edit behavior (inline editor, add row, delete)
5. Any special components or cascading filters
6. Data flow (how data loads, saves, refreshes)

## Key Files Reference

| File | Purpose |
|------|---------|
| `DataInitializer.java` | All form/field metadata definitions + seed data |
| `GenericFormView.java` | SINGLE type forms with grid |
| `GenericMasterDetailFormView.java` | MASTER_DETAIL type forms |
| `SubformGridField.java` | SUBFORM_GRID embedded child grids |
| `ComponentFactory.java` | Creates Vaadin components from FieldMeta |
| `DynamicDataService.java` | JDBC CRUD, LOV resolution, schema management |
| `FormMeta.java` | Form definition entity |
| `FieldMeta.java` | Field definition entity |

## Tips

- Always start from `DataInitializer.java` — it's the single source of truth for all form definitions
- The `formType` field determines which view renders the form
- `SUBFORM_GRID` in a SINGLE form is equivalent to MASTER_DETAIL but modular
- Check `scroll_md_setup.sql` if the form is SCROLL_MD (auto-scroll variant)
