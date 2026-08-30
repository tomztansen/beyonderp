-- =============================================================================
-- Migration: vaadinerp meta schema (idempotent)
-- Run on: public schema (and public for all meta_* tables)
-- =============================================================================

-- -------------------------
-- Audit columns helper
-- -------------------------
-- Semua tabel meta mewarisi BaseAuditableEntity:
--   inputby VARCHAR(255), inputdt TIMESTAMP,
--   updateby VARCHAR(255), updatedt TIMESTAMP,
--   version INTEGER DEFAULT 0

-- =============================================================================
-- 1. meta_form
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_form (
    form_code               VARCHAR(50)  PRIMARY KEY,
    form_title              VARCHAR(100),
    table_name              VARCHAR(100),
    view_table              TEXT,
    primary_key             VARCHAR(50),
    label_width             VARCHAR(50),
    default_sort_field      VARCHAR(50),
    default_sort_direction  VARCHAR(10),
    form_type               VARCHAR(20)  DEFAULT 'SINGLE',
    detail_table_name       VARCHAR(100),
    detail_primary_key      VARCHAR(50),
    detail_foreign_key      VARCHAR(50),
    extra_toolbars          TEXT,
    inputby                 VARCHAR(255),
    inputdt                 TIMESTAMP,
    updateby                VARCHAR(255),
    updatedt                TIMESTAMP,
    version                 INTEGER      DEFAULT 0
);

-- =============================================================================
-- 2. meta_field
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_field (
    id                      BIGSERIAL    PRIMARY KEY,
    form_code               VARCHAR(50)  REFERENCES public.meta_form(form_code),
    field_name              VARCHAR(50),
    field_label             VARCHAR(100),
    component_type          VARCHAR(50),
    row_group               INTEGER,
    col_order               INTEGER,
    col_span                INTEGER,
    field_width             VARCHAR(20),
    is_required             BOOLEAN      DEFAULT FALSE,
    is_readonly             BOOLEAN      DEFAULT FALSE,
    readonly_mode           VARCHAR(20),
    lov_code                VARCHAR(50),
    show_in_grid            BOOLEAN      DEFAULT FALSE,
    hide_in_form            BOOLEAN      DEFAULT FALSE,
    is_detail               BOOLEAN,
    is_sortable             BOOLEAN      DEFAULT TRUE,
    formula                 VARCHAR(255),
    save_on_insert          BOOLEAN      DEFAULT TRUE,
    save_on_update          BOOLEAN      DEFAULT TRUE,
    validation_rule         VARCHAR(100),
    display_format          VARCHAR(50),
    sequence_code           VARCHAR(50),
    is_audit_log            BOOLEAN      DEFAULT FALSE,
    show_line_no            BOOLEAN      DEFAULT TRUE,
    save_line_no_to_db      BOOLEAN      DEFAULT FALSE,
    on_add_script           TEXT,
    scheduler_role          VARCHAR(30),
    hyperlink_target_form   VARCHAR(50),
    hyperlink_filter_mapping TEXT,
    inputby                 VARCHAR(255),
    inputdt                 TIMESTAMP,
    updateby                VARCHAR(255),
    updatedt                TIMESTAMP,
    version                 INTEGER      DEFAULT 0
);

-- =============================================================================
-- 3. meta_field_filter
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_field_filter (
    id                  BIGSERIAL    PRIMARY KEY,
    field_id            BIGINT       NOT NULL REFERENCES public.meta_field(id),
    filter_column       VARCHAR(50)  NOT NULL,
    source_type         VARCHAR(50)  NOT NULL,
    source_name         VARCHAR(50)  NOT NULL,
    logical_operator    VARCHAR(10)  DEFAULT 'AND',
    comparison_operator VARCHAR(10)  DEFAULT '=',
    inputby             VARCHAR(255),
    inputdt             TIMESTAMP,
    updateby            VARCHAR(255),
    updatedt            TIMESTAMP,
    version             INTEGER      DEFAULT 0
);

-- =============================================================================
-- 4. meta_field_lov_target
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_field_lov_target (
    id              BIGSERIAL    PRIMARY KEY,
    field_id        BIGINT       NOT NULL REFERENCES public.meta_field(id),
    source_column   VARCHAR(50)  NOT NULL,
    target_field    VARCHAR(50)  NOT NULL,
    action_type     VARCHAR(50)  DEFAULT 'COPY',
    lookup_column   VARCHAR(50),
    inputby         VARCHAR(255),
    inputdt         TIMESTAMP,
    updateby        VARCHAR(255),
    updatedt        TIMESTAMP,
    version         INTEGER      DEFAULT 0
);

-- =============================================================================
-- 5. meta_form_action
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_form_action (
    id                      BIGSERIAL    PRIMARY KEY,
    form_code               VARCHAR(50)  REFERENCES public.meta_form(form_code),
    action_code             VARCHAR(50)  NOT NULL,
    action_label            VARCHAR(100) NOT NULL,
    icon_name               VARCHAR(50),
    button_style            VARCHAR(50),
    target_scope            VARCHAR(50),
    action_type             VARCHAR(50)  DEFAULT 'POPUP_PICKER',
    source_lov_code         VARCHAR(50),
    filter_mapping          TEXT,
    target_mapping          TEXT,
    copy_source_lov_code    VARCHAR(50),
    copy_filter_mapping     TEXT,
    menu_group              VARCHAR(100),
    script_content          TEXT,
    inputby                 VARCHAR(255),
    inputdt                 TIMESTAMP,
    updateby                VARCHAR(255),
    updatedt                TIMESTAMP,
    version                 INTEGER      DEFAULT 0
);

-- =============================================================================
-- 6. meta_lov
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_lov (
    lov_code        VARCHAR(50)  PRIMARY KEY,
    lov_name        TEXT,
    table_name      TEXT,
    value_column    TEXT,
    label_column    TEXT,
    search_column   TEXT,
    grid_columns    TEXT,
    inputby         VARCHAR(255),
    inputdt         TIMESTAMP,
    updateby        VARCHAR(255),
    updatedt        TIMESTAMP,
    version         INTEGER      DEFAULT 0
);

-- =============================================================================
-- 7. meta_report
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_report (
    report_code     VARCHAR(50)  PRIMARY KEY,
    report_title    VARCHAR(100),
    table_name      VARCHAR(100),
    page_size       VARCHAR(20),
    orientation     VARCHAR(20),
    engine_type     VARCHAR(20),
    template_path   VARCHAR(255),
    data_query      TEXT,
    category        VARCHAR(50),
    description     VARCHAR(500),
    inputby         VARCHAR(255),
    inputdt         TIMESTAMP,
    updateby        VARCHAR(255),
    updatedt        TIMESTAMP,
    version         INTEGER      DEFAULT 0
);

-- =============================================================================
-- 8. meta_report_role  (ElementCollection)
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_report_role (
    report_code     VARCHAR(50)  NOT NULL REFERENCES public.meta_report(report_code),
    role_code       VARCHAR(100) NOT NULL,
    PRIMARY KEY (report_code, role_code)
);

-- =============================================================================
-- 9. meta_report_param
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_report_param (
    id                  BIGSERIAL    PRIMARY KEY,
    report_code         VARCHAR(50)  NOT NULL REFERENCES public.meta_report(report_code),
    param_name          VARCHAR(50)  NOT NULL,
    param_label         VARCHAR(100),
    param_type          VARCHAR(20)  DEFAULT 'STRING',
    default_value       VARCHAR(255),
    col_order           INTEGER      DEFAULT 0,
    lov_code            VARCHAR(100),
    source              VARCHAR(20)  DEFAULT 'USER_INPUT',
    source_key          VARCHAR(200),
    required            BOOLEAN      DEFAULT FALSE,
    filter_column       VARCHAR(100),
    operator            VARCHAR(20),
    lov_filter_column   VARCHAR(50),
    lov_filter_value    VARCHAR(255),
    lov_filter_operator VARCHAR(10),
    inputby             VARCHAR(255),
    inputdt             TIMESTAMP,
    updateby            VARCHAR(255),
    updatedt            TIMESTAMP,
    version             INTEGER      DEFAULT 0
);

-- =============================================================================
-- 10. meta_report_param_filter
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_report_param_filter (
    id                  BIGSERIAL    PRIMARY KEY,
    param_id            BIGINT       NOT NULL REFERENCES public.meta_report_param(id),
    filter_column       VARCHAR(50)  NOT NULL,
    source_type         VARCHAR(50)  NOT NULL DEFAULT 'STATIC',
    source_name         VARCHAR(255) NOT NULL,
    comparison_operator VARCHAR(10)  DEFAULT '=',
    logical_operator    VARCHAR(10)  DEFAULT 'AND',
    inputby             VARCHAR(255),
    inputdt             TIMESTAMP,
    updateby            VARCHAR(255),
    updatedt            TIMESTAMP,
    version             INTEGER      DEFAULT 0
);

-- =============================================================================
-- 11. meta_report_element
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_report_element (
    id              BIGSERIAL    PRIMARY KEY,
    report_code     VARCHAR(50)  REFERENCES public.meta_report(report_code),
    band_type       VARCHAR(30),
    element_type    VARCHAR(20),
    element_value   TEXT,
    column_width    VARCHAR(20),
    alignment       VARCHAR(20),
    font_weight     VARCHAR(20),
    col_order       INTEGER,
    format_pattern  VARCHAR(50),
    inputby         VARCHAR(255),
    inputdt         TIMESTAMP,
    updateby        VARCHAR(255),
    updatedt        TIMESTAMP,
    version         INTEGER      DEFAULT 0
);

-- =============================================================================
-- 12. app_standard_format
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.app_standard_format (
    component_type  VARCHAR(50)  PRIMARY KEY,
    format_pattern  VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    updated_at      TIMESTAMP,
    inputby         VARCHAR(255),
    inputdt         TIMESTAMP,
    updateby        VARCHAR(255),
    updatedt        TIMESTAMP,
    version         INTEGER      DEFAULT 0
);

-- =============================================================================
-- 13. meta_scheduler_config
-- =============================================================================
CREATE TABLE IF NOT EXISTS public.meta_scheduler_config (
    id                      SERIAL       PRIMARY KEY,
    form_code               VARCHAR(50)  NOT NULL UNIQUE,
    scheduler_query         TEXT,
    col_resource            VARCHAR(100),
    col_resource_group      VARCHAR(100),
    col_task_name           VARCHAR(100),
    col_start_date          VARCHAR(100),
    col_end_date            VARCHAR(100),
    col_qty                 VARCHAR(100),
    col_max_capacity        VARCHAR(100),
    col_group_id            VARCHAR(100),
    col_split_group         VARCHAR(100),
    col_dependency_id       VARCHAR(100),
    col_sequence            VARCHAR(100),
    col_lead_day            VARCHAR(100),
    col_weight              VARCHAR(100),
    col_qty_prod            VARCHAR(100),
    col_pcs_per_box         VARCHAR(100),
    col_max_capacity_weight VARCHAR(100),
    col_primary_key         VARCHAR(100),
    update_table            VARCHAR(100),
    update_date_column      VARCHAR(100),
    update_resource_column  VARCHAR(100),
    default_capacity_mode   VARCHAR(20)  DEFAULT 'QTYBOX',
    on_drag_script          TEXT,
    col_shipping_date       VARCHAR(100),
    holiday_table           VARCHAR(100),
    holiday_date_col        VARCHAR(100),
    inputby                 VARCHAR(255),
    inputdt                 TIMESTAMP,
    updateby                VARCHAR(255),
    updatedt                TIMESTAMP,
    version                 INTEGER      DEFAULT 0
);

-- =============================================================================
-- ADD MISSING COLUMNS (idempotent via DO block)
-- Jalankan ini bila tabel sudah ada tapi kolom baru belum ada
-- =============================================================================
DO $$
BEGIN
    -- meta_report: kolom baru (engine_type, template_path, data_query, category, description)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report' AND column_name='engine_type') THEN
        ALTER TABLE public.meta_report ADD COLUMN engine_type VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report' AND column_name='template_path') THEN
        ALTER TABLE public.meta_report ADD COLUMN template_path VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report' AND column_name='data_query') THEN
        ALTER TABLE public.meta_report ADD COLUMN data_query TEXT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report' AND column_name='category') THEN
        ALTER TABLE public.meta_report ADD COLUMN category VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report' AND column_name='description') THEN
        ALTER TABLE public.meta_report ADD COLUMN description VARCHAR(500);
    END IF;

    -- meta_report_param: kolom baru
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report_param' AND column_name='lov_filter_column') THEN
        ALTER TABLE public.meta_report_param ADD COLUMN lov_filter_column VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report_param' AND column_name='lov_filter_value') THEN
        ALTER TABLE public.meta_report_param ADD COLUMN lov_filter_value VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report_param' AND column_name='lov_filter_operator') THEN
        ALTER TABLE public.meta_report_param ADD COLUMN lov_filter_operator VARCHAR(10);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report_param' AND column_name='filter_column') THEN
        ALTER TABLE public.meta_report_param ADD COLUMN filter_column VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report_param' AND column_name='operator') THEN
        ALTER TABLE public.meta_report_param ADD COLUMN operator VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report_param' AND column_name='source') THEN
        ALTER TABLE public.meta_report_param ADD COLUMN source VARCHAR(20) DEFAULT 'USER_INPUT';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_report_param' AND column_name='source_key') THEN
        ALTER TABLE public.meta_report_param ADD COLUMN source_key VARCHAR(200);
    END IF;

    -- meta_report_role (mungkin belum ada)
    -- (sudah handle via CREATE TABLE IF NOT EXISTS di atas)

    -- meta_field: kolom baru
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_field' AND column_name='readonly_mode') THEN
        ALTER TABLE public.meta_field ADD COLUMN readonly_mode VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_field' AND column_name='hyperlink_target_form') THEN
        ALTER TABLE public.meta_field ADD COLUMN hyperlink_target_form VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_field' AND column_name='hyperlink_filter_mapping') THEN
        ALTER TABLE public.meta_field ADD COLUMN hyperlink_filter_mapping TEXT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_field' AND column_name='on_add_script') THEN
        ALTER TABLE public.meta_field ADD COLUMN on_add_script TEXT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_field' AND column_name='scheduler_role') THEN
        ALTER TABLE public.meta_field ADD COLUMN scheduler_role VARCHAR(30);
    END IF;

    -- meta_form_action: kolom baru
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_form_action' AND column_name='copy_source_lov_code') THEN
        ALTER TABLE public.meta_form_action ADD COLUMN copy_source_lov_code VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_form_action' AND column_name='copy_filter_mapping') THEN
        ALTER TABLE public.meta_form_action ADD COLUMN copy_filter_mapping TEXT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_form_action' AND column_name='menu_group') THEN
        ALTER TABLE public.meta_form_action ADD COLUMN menu_group VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='meta_form_action' AND column_name='script_content') THEN
        ALTER TABLE public.meta_form_action ADD COLUMN script_content TEXT;
    END IF;
END $$;

-- Print-dari-form: cakupan pemakaian report + kunci grouping engine STANDARD
ALTER TABLE public.meta_report ADD COLUMN IF NOT EXISTS usage_scope VARCHAR(20) DEFAULT 'RUNNER';
ALTER TABLE public.meta_report ADD COLUMN IF NOT EXISTS group_by VARCHAR(100);
UPDATE public.meta_report SET usage_scope = 'RUNNER' WHERE usage_scope IS NULL;

-- ── Report contoh: dokumen Bill of Material, satu per engine ────────────────────
-- Ketiganya memakai data_query yang sama; hanya engine + template yang berbeda.
-- table_name = 'mhbom' menghubungkan report ke form BOM_ALL (bukan sumber query,
-- karena data_query menang di resolveBaseQuery).

DELETE FROM public.meta_report_param
 WHERE report_code IN ('RPT_BOM_DOC_STD','RPT_BOM_DOC_JSP','RPT_BOM_DOC_STI');
DELETE FROM public.meta_report_element
 WHERE report_code IN ('RPT_BOM_DOC_STD','RPT_BOM_DOC_JSP','RPT_BOM_DOC_STI');
DELETE FROM public.meta_report
 WHERE report_code IN ('RPT_BOM_DOC_STD','RPT_BOM_DOC_JSP','RPT_BOM_DOC_STI');

-- template_path WAJIB diisi untuk JASPER: ReportResolver.masterExtension mengembalikan
-- "jasper" kecuali template_path berakhiran ".jrxml", sehingga resolver akan mencari
-- RPT_BOM_DOC_JSP.jasper (tidak ada) alih-alih .jrxml yang kita pasang. Report JASPER
-- `12345` yang sudah ada di DB memperlihatkan bug ini: template_path-nya kosong padahal
-- berkasnya 12345.jrxml, jadi report itu tidak akan pernah ketemu templatenya.
INSERT INTO public.meta_report
    (report_code, report_title, table_name, page_size, orientation, engine_type,
     data_query, category, description, usage_scope, group_by, template_path)
VALUES
    ('RPT_BOM_DOC_STD', 'Bill of Material Document (Standard)', 'mhbom', 'A4', 'PORTRAIT', 'STANDARD',
     'SELECT h.id AS bom_id, h.idno, h.itemname AS product, h.abmdrawingnumber AS drawing, h.netweight, d.itemname AS material, d.itemgroup, d.qty, d.perseries FROM dynamic.mhbom h LEFT JOIN dynamic.mdbom d ON d.mhbomid = h.id WHERE h.id IN (:bom_id) ORDER BY h.id, d.id',
     'Production', 'Prints the selected BOM rows as documents, one page per BOM.', 'FORM', 'bom_id', NULL),
    ('RPT_BOM_DOC_JSP', 'Bill of Material Document (Jasper)', 'mhbom', 'A4', 'PORTRAIT', 'JASPER',
     'SELECT h.id AS bom_id, h.idno, h.itemname AS product, h.abmdrawingnumber AS drawing, h.netweight, d.itemname AS material, d.itemgroup, d.qty, d.perseries FROM dynamic.mhbom h LEFT JOIN dynamic.mdbom d ON d.mhbomid = h.id WHERE h.id IN (:bom_id) ORDER BY h.id, d.id',
     'Production', 'Prints the selected BOM rows as documents, one page per BOM.', 'FORM', NULL, 'RPT_BOM_DOC_JSP.jrxml'),
    ('RPT_BOM_DOC_STI', 'Bill of Material Document (Stimulsoft)', 'mhbom', 'A4', 'PORTRAIT', 'STIMULSOFT',
     'SELECT h.id AS bom_id, h.idno, h.itemname AS product, h.abmdrawingnumber AS drawing, h.netweight, d.itemname AS material, d.itemgroup, d.qty, d.perseries FROM dynamic.mhbom h LEFT JOIN dynamic.mdbom d ON d.mhbomid = h.id WHERE h.id IN (:bom_id) ORDER BY h.id, d.id',
     'Production', 'Prints the selected BOM rows as documents, one page per BOM.', 'FORM', NULL, NULL);

INSERT INTO public.meta_report_param
    (report_code, param_name, param_label, param_type, source, source_key, required, col_order)
SELECT r.report_code, 'bom_id', 'BOM', 'TEXTBOX', 'FORM_FIELD', 'id', true, 1
  FROM public.meta_report r
 WHERE r.report_code IN ('RPT_BOM_DOC_STD','RPT_BOM_DOC_JSP','RPT_BOM_DOC_STI');

-- Band untuk engine STANDARD (dua engine lain menyimpan tata letak di templatenya).
INSERT INTO public.meta_report_element
    (report_code, band_type, element_type, element_value, column_width, alignment, font_weight, col_order, format_pattern)
VALUES
    ('RPT_BOM_DOC_STD', 'TITLE',        'LABEL',  'BILL OF MATERIAL', '100%', 'LEFT',  'BOLD',   1, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_HEADER', 'LABEL',  'BOM No:',          '80px', 'LEFT',  'BOLD',   1, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_HEADER', 'FIELD',  'idno',             '120px','LEFT',  'BOLD',   2, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_HEADER', 'LABEL',  'Product:',         '80px', 'LEFT',  'NORMAL', 3, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_HEADER', 'FIELD',  'product',          '275px','LEFT',  'NORMAL', 4, NULL),
    ('RPT_BOM_DOC_STD', 'COLUMN_HEADER','LABEL',  'Material',         '45%',  'LEFT',  'BOLD',   1, NULL),
    ('RPT_BOM_DOC_STD', 'COLUMN_HEADER','LABEL',  'Group',            '30%',  'LEFT',  'BOLD',   2, NULL),
    ('RPT_BOM_DOC_STD', 'COLUMN_HEADER','LABEL',  'Qty',              '15%',  'RIGHT', 'BOLD',   3, NULL),
    ('RPT_BOM_DOC_STD', 'COLUMN_HEADER','LABEL',  '/Series',          '10%',  'RIGHT', 'BOLD',   4, NULL),
    ('RPT_BOM_DOC_STD', 'DETAIL',       'FIELD',  'material',         '45%',  'LEFT',  'NORMAL', 1, NULL),
    ('RPT_BOM_DOC_STD', 'DETAIL',       'FIELD',  'itemgroup',        '30%',  'LEFT',  'NORMAL', 2, NULL),
    ('RPT_BOM_DOC_STD', 'DETAIL',       'FIELD',  'qty',              '15%',  'RIGHT', 'NORMAL', 3, '#,##0.00'),
    ('RPT_BOM_DOC_STD', 'DETAIL',       'FIELD',  'perseries',        '10%',  'RIGHT', 'NORMAL', 4, '#,##0.##'),
    ('RPT_BOM_DOC_STD', 'GROUP_FOOTER', 'LABEL',  'Total item:',      '85%',  'RIGHT', 'BOLD',   1, NULL),
    ('RPT_BOM_DOC_STD', 'GROUP_FOOTER', 'SYSTEM', 'COUNT()',          '15%',  'RIGHT', 'BOLD',   2, NULL);
