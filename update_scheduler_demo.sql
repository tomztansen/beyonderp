-- ============================================================
-- PRODUCTION SCHEDULER DEMO — Full Setup Script
-- ============================================================
-- Menjalankan script ini akan:
-- 1. Membuat tabel meta_scheduler_config (jika belum ada)
-- 2. Membuat ulang tabel trx_production_schedule
-- 3. Insert data demo (4 baris produksi)
-- 4. Insert meta_form & meta_field untuk grid display
-- 5. Insert meta_scheduler_config untuk konfigurasi Gantt
-- ============================================================

-- ============================================================
-- 1. TABEL KONFIGURASI SCHEDULER (BARU)
-- ============================================================
CREATE TABLE IF NOT EXISTS public.meta_scheduler_config (
    id                      SERIAL PRIMARY KEY,
    form_code               character varying(50) NOT NULL,
    scheduler_query         TEXT,
    col_resource            character varying(100),
    col_task_name           character varying(100),
    col_start_date          character varying(100),
    col_end_date            character varying(100),
    col_qty                 character varying(100),
    col_max_capacity        character varying(100),
    col_group_id            character varying(100),
    col_sequence            character varying(100),
    col_lead_day            character varying(100),
    col_weight              character varying(100),
    col_max_capacity_weight character varying(100),
    col_primary_key         character varying(100),
    update_table            character varying(100),
    update_date_column      character varying(100),
    default_capacity_mode   character varying(20) DEFAULT 'QTYBOX',
    on_drag_script          TEXT,
    UNIQUE(form_code)
);

-- ============================================================
-- 2. TABEL TRANSAKSI PRODUKSI (RECREATE)
-- ============================================================
DROP TABLE IF EXISTS public.trx_production_schedule;
CREATE TABLE public.trx_production_schedule (
    id                      SERIAL PRIMARY KEY,
    capacitybox             integer,
    qtybox                  integer,
    mesin                   character varying(150),
    assetno                 character varying(50),
    capacity_code           character varying(20),
    idno                    character varying(50),
    qtyprod                 integer,
    tsproductionorderid     integer,
    resourceid              integer,
    qty                     integer,
    perseries               integer,
    uomid                   integer,
    sequence                integer,
    next_sequence           integer,
    leadday                 integer DEFAULT 1,
    ismaterialconsumption   boolean DEFAULT false,
    proposedtime            date,
    approvedtime            date,
    proposedmesinorderid    integer,
    weight                  numeric(12,2),
    capacityweight          numeric(12,2)
);

-- ============================================================
-- 3. DATA DEMO PRODUKSI (4 baris, 2 mesin, 2 idno)
-- ============================================================
INSERT INTO public.trx_production_schedule (
    capacitybox, qtybox, mesin, assetno, capacity_code, idno,
    qtyprod, tsproductionorderid, resourceid, qty, perseries, uomid,
    sequence, next_sequence, leadday, ismaterialconsumption,
    proposedtime, approvedtime, proposedmesinorderid
) VALUES
-- PRD20260805023: Mixer (seq 10) → Tanur (seq 20)
(80, 3, 'Continous Sand Mixer 10 Ton', 'FD.MC.SM004', '10 Ton', 'PRD20260805023',
 12, 435, 110, 1262, 2, 4,
 10, 20, 1, true,
 '2026-08-29', '2026-08-29', 126),

(80, 3, 'Tanur (Furnace) - B', 'FD.MC.FN002', '5 Ton', 'PRD20260805023',
 12, 436, 105, 1960, 2, 4,
 20, 30, 3, false,
 '2026-08-30', '2026-08-30', 128),

-- PRD20260805021: Mixer (seq 10) → Tanur (seq 20)
(80, 20, 'Continous Sand Mixer 10 Ton', 'FD.MC.SM004', '5 Ton', 'PRD20260805021',
 40, 421, 105, 1262, 2, 4,
 10, 20, 1, true,
 '2026-08-29', '2026-08-29', 125),

(80, 20, 'Tanur (Furnace) - B', 'FD.MC.FN002', '5 Ton', 'PRD20260805021',
 40, 422, 105, 1960, 2, 4,
 20, 30, 3, false,
 '2026-08-30', '2026-08-30', 128);

-- ============================================================
-- 4. META_FORM & META_FIELD (untuk grid display)
-- ============================================================
DELETE FROM public.meta_field WHERE form_code = 'SCHEDULER_DEMO';
DELETE FROM public.meta_form WHERE form_code = 'SCHEDULER_DEMO';

INSERT INTO public.meta_form (
    form_code, form_title, table_name, view_table, primary_key,
    default_sort_field, default_sort_direction, form_type
) VALUES (
    'SCHEDULER_DEMO', 'Production Scheduler Demo', 'trx_production_schedule',
    'trx_production_schedule', 'id', 'mesin', 'ASC', 'SCHEDULER_SPLIT'
);

-- Field: id (Hidden)
INSERT INTO public.meta_field (
    form_code, field_name, field_label, component_type,
    is_required, is_readonly, show_in_grid, hide_in_form,
    row_group, col_order, is_detail
) VALUES (
    'SCHEDULER_DEMO', 'id', 'ID', 'TEXT_FIELD',
    true, true, false, true, 1, 1, false
);

-- Field: mesin
INSERT INTO public.meta_field (
    form_code, field_name, field_label, component_type,
    is_required, is_readonly, show_in_grid, hide_in_form,
    row_group, col_order
) VALUES (
    'SCHEDULER_DEMO', 'mesin', 'Mesin / Fasilitas', 'TEXT_FIELD',
    true, false, true, false, 2, 1
);

-- Field: idno
INSERT INTO public.meta_field (
    form_code, field_name, field_label, component_type,
    is_required, is_readonly, show_in_grid, hide_in_form,
    row_group, col_order
) VALUES (
    'SCHEDULER_DEMO', 'idno', 'ID Produksi', 'TEXT_FIELD',
    true, false, true, false, 2, 2
);

-- Field: qtybox
INSERT INTO public.meta_field (
    form_code, field_name, field_label, component_type,
    is_required, is_readonly, show_in_grid, hide_in_form,
    row_group, col_order
) VALUES (
    'SCHEDULER_DEMO', 'qtybox', 'Qty Box', 'NUMBER_FIELD',
    false, false, true, false, 3, 1
);

-- Field: capacitybox
INSERT INTO public.meta_field (
    form_code, field_name, field_label, component_type,
    is_required, is_readonly, show_in_grid, hide_in_form,
    row_group, col_order
) VALUES (
    'SCHEDULER_DEMO', 'capacitybox', 'Max Capacity', 'NUMBER_FIELD',
    false, false, true, false, 3, 2
);

-- Field: proposedtime
INSERT INTO public.meta_field (
    form_code, field_name, field_label, component_type,
    is_required, is_readonly, show_in_grid, hide_in_form,
    row_group, col_order
) VALUES (
    'SCHEDULER_DEMO', 'proposedtime', 'Proposed Date', 'DATE_PICKER',
    true, false, true, false, 4, 1
);

-- Field: sequence
INSERT INTO public.meta_field (
    form_code, field_name, field_label, component_type,
    is_required, is_readonly, show_in_grid, hide_in_form,
    row_group, col_order
) VALUES (
    'SCHEDULER_DEMO', 'sequence', 'Sequence', 'NUMBER_FIELD',
    false, false, true, false, 4, 2
);

-- Field: leadday
INSERT INTO public.meta_field (
    form_code, field_name, field_label, component_type,
    is_required, is_readonly, show_in_grid, hide_in_form,
    row_group, col_order
) VALUES (
    'SCHEDULER_DEMO', 'leadday', 'Lead Day', 'NUMBER_FIELD',
    false, false, true, false, 5, 1
);

-- Field: assetno
INSERT INTO public.meta_field (
    form_code, field_name, field_label, component_type,
    is_required, is_readonly, show_in_grid, hide_in_form,
    row_group, col_order
) VALUES (
    'SCHEDULER_DEMO', 'assetno', 'Asset No', 'TEXT_FIELD',
    false, false, true, false, 5, 2
);

-- ============================================================
-- 5. SCHEDULER CONFIG (konfigurasi mapping kolom)
-- ============================================================
DELETE FROM public.meta_scheduler_config WHERE form_code = 'SCHEDULER_DEMO';

INSERT INTO public.meta_scheduler_config (
    form_code, scheduler_query,
    col_resource, col_task_name, col_start_date, col_primary_key,
    col_qty, col_max_capacity, col_group_id, col_sequence, col_lead_day,
    col_weight, col_max_capacity_weight,
    update_table, update_date_column, default_capacity_mode,
    col_split_group, col_dependency_id,
    on_drag_script
) VALUES (
    'SCHEDULER_DEMO',
    'SELECT * FROM trx_production_schedule ORDER BY mesin, proposedtime',
    'mesin', 'idno', 'proposedtime', 'id',
    'qtybox', 'capacitybox', 'idno', 'sequence', 'leadday',
    'weight', 'capacityweight',
    'trx_production_schedule', 'proposedtime', 'QTYBOX',
    'split_group', 'tsproductionorderid',
    -- Groovy script untuk validasi kapasitas saat drag
    'def totalQty = ctx.sqlValue("SELECT COALESCE(SUM(" + config.colQty + "),0) FROM " + config.updateTable + " WHERE " + config.colResource + "=? AND " + config.colStartDate + "=? AND " + config.colPrimaryKey + "!=?", [task.resource, newDate, task.id])
if (totalQty + task.qty > task.maxCapacity) {
    return ctx.confirm("Kapasitas akan melebihi batas!\\nMesin: " + task.resource + "\\nTanggal: " + newDate + "\\nTotal: " + (totalQty + task.qty) + "/" + task.maxCapacity + "\\n\\nLanjutkan?")
}
return true'
);
