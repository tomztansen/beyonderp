-- =========================================================================
-- SQL Script for Scrollable Master-Detail Example (SCROLL_MD)
-- Database: PostgreSQL
--
-- This script sets up the physical tables, registers form metadata,
-- and inserts initial mock records to test horizontal scrollable grids.
-- =========================================================================

-- 1. Create Schema and Physical Tables
CREATE SCHEMA IF NOT EXISTS dynamic;

-- Clean up existing physical tables if they exist
DROP TABLE IF EXISTS dynamic.scroll_dtl CASCADE;
DROP TABLE IF EXISTS dynamic.scroll_hdr CASCADE;

-- Create Master/Header Table (with 12 columns)
CREATE TABLE dynamic.scroll_hdr (
    id SERIAL PRIMARY KEY,
    invoice_no VARCHAR(255) NOT NULL,
    invoice_date DATE NOT NULL,
    customer VARCHAR(255),
    npwp VARCHAR(255),
    payment_term VARCHAR(255),
    salesperson VARCHAR(255),
    shipping_method VARCHAR(255),
    tracking_no VARCHAR(255),
    warehouse_code VARCHAR(255),
    currency VARCHAR(255),
    exchange_rate DECIMAL(19, 2),
    notes TEXT
);

-- Create Detail Table (with 14 columns + Foreign Key)
CREATE TABLE dynamic.scroll_dtl (
    id SERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES dynamic.scroll_hdr(id) ON DELETE CASCADE,
    item_code VARCHAR(255) NOT NULL,
    item_name VARCHAR(255),
    qty INTEGER NOT NULL,
    uom VARCHAR(255),
    price DECIMAL(19, 2) NOT NULL,
    discount_pct DECIMAL(19, 2),
    discount_amt DECIMAL(19, 2),
    tax_pct DECIMAL(19, 2),
    tax_amt DECIMAL(19, 2),
    weight_grid DECIMAL(19, 2),
    batch_no VARCHAR(255),
    expiry_date DATE,
    total_price DECIMAL(19, 2),
    notes_detail TEXT
);

-- 2. Clean Up Existing Metadata to Avoid Conflicts
DELETE FROM meta_field WHERE form_code IN ('SCROLL_MD', 'SCROLL_MD_DTL');
DELETE FROM meta_form WHERE form_code IN ('SCROLL_MD', 'SCROLL_MD_DTL');

-- 3. Insert Form Metadata
INSERT INTO meta_form (form_code, form_title, table_name, form_type, label_width, primary_key) VALUES
('SCROLL_MD_DTL', 'Detail Barang Banyak Kolom', 'scroll_dtl', 'SINGLE', '150px', 'id'),
('SCROLL_MD', 'Faktur Pengiriman Lengkap (Auto Scroll)', 'scroll_hdr', 'SINGLE', '160px', 'id');

-- 4. Insert Field Metadata for Detail (SCROLL_MD_DTL)
INSERT INTO meta_field (form_code, field_name, field_label, component_type, lov_code, row_group, col_order, is_required, is_readonly, show_in_grid, hide_in_form, is_detail, save_on_insert, save_on_update) VALUES
('SCROLL_MD_DTL', 'item_code', 'Kode Barang', 'BANDBOX', 'lov_child', 1, 10, true, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'item_name', 'Nama Barang', 'TEXTBOX', NULL, 1, 20, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'qty', 'Kuantitas', 'INTBOX', NULL, 1, 30, true, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'uom', 'Satuan (UOM)', 'COMBOBOX', NULL, 1, 40, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'price', 'Harga Satuan', 'DECIMALBOX', NULL, 1, 50, true, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'discount_pct', 'Diskon %', 'DECIMALBOX', NULL, 1, 60, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'discount_amt', 'Nilai Diskon', 'DECIMALBOX', NULL, 1, 70, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'tax_pct', 'Pajak %', 'DECIMALBOX', NULL, 1, 80, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'tax_amt', 'Nilai Pajak', 'DECIMALBOX', NULL, 1, 90, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'weight_grid', 'Berat (kg)', 'DECIMALBOX', NULL, 1, 100, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'batch_no', 'Nomor Batch', 'TEXTBOX', NULL, 1, 110, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'expiry_date', 'Tgl Kadaluarsa', 'DATEBOX', NULL, 1, 120, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'total_price', 'Subtotal Bersih', 'DECIMALBOX', NULL, 1, 130, false, false, true, false, true, true, true),
('SCROLL_MD_DTL', 'notes_detail', 'Catatan Rincian', 'TEXTAREA', NULL, 1, 140, false, false, true, false, true, true, true);

-- 5. Insert Field Metadata for Master (SCROLL_MD)
INSERT INTO meta_field (form_code, field_name, field_label, component_type, lov_code, row_group, col_order, is_required, is_readonly, show_in_grid, hide_in_form, is_detail, save_on_insert, save_on_update) VALUES
('SCROLL_MD', 'invoice_no', 'Nomor Faktur', 'TEXTBOX', NULL, 1, 10, true, false, true, false, false, true, true),
('SCROLL_MD', 'invoice_date', 'Tanggal Faktur', 'DATEBOX', NULL, 1, 20, true, false, true, false, false, true, true),
('SCROLL_MD', 'customer', 'Pelanggan', 'COMBOBOX', 'lov_parent', 1, 30, false, false, true, false, false, true, true),
('SCROLL_MD', 'npwp', 'NPWP Pajak', 'TEXTBOX', NULL, 2, 40, false, false, true, false, false, true, true),
('SCROLL_MD', 'payment_term', 'Termin Pembayaran', 'COMBOBOX', NULL, 2, 50, false, false, true, false, false, true, true),
('SCROLL_MD', 'salesperson', 'Nama Sales', 'TEXTBOX', NULL, 2, 60, false, false, true, false, false, true, true),
('SCROLL_MD', 'shipping_method', 'Metode Pengiriman', 'TEXTBOX', NULL, 3, 70, false, false, true, false, false, true, true),
('SCROLL_MD', 'tracking_no', 'Nomor Resi', 'TEXTBOX', NULL, 3, 80, false, false, true, false, false, true, true),
('SCROLL_MD', 'warehouse_code', 'Kode Gudang', 'TEXTBOX', NULL, 3, 90, false, false, true, false, false, true, true),
('SCROLL_MD', 'currency', 'Mata Uang', 'TEXTBOX', NULL, 4, 100, false, false, true, false, false, true, true),
('SCROLL_MD', 'exchange_rate', 'Kurs', 'DECIMALBOX', NULL, 4, 110, false, false, true, false, false, true, true),
('SCROLL_MD', 'notes', 'Keterangan', 'TEXTAREA', NULL, 5, 120, false, false, true, false, false, true, true),
('SCROLL_MD', 'details', 'Daftar Item Barang (Rincian)', 'SUBFORM_GRID', 'SCROLL_MD_DTL', 6, 130, false, false, false, false, false, true, true);

-- 6. Insert Mock/Seed Data
INSERT INTO dynamic.scroll_hdr (id, invoice_no, invoice_date, customer, npwp, payment_term, salesperson, shipping_method, tracking_no, warehouse_code, currency, exchange_rate, notes) VALUES
(101, 'INV/2026/0001', '2026-06-22', 'IT', '01.234.567.8-901.000', 'COD', 'ALEX GMN', 'JNE YES', 'TRK1002345', 'WH-MAIN', 'IDR', 1.0, 'Pengiriman tahap pertama'),
(102, 'INV/2026/0002', '2026-06-23', 'HR', '02.456.789.0-123.000', '30 DAYS', 'SARAH JANE', 'DHL EXPRESS', 'TRK9988771', 'WH-EAST', 'USD', 16500.0, 'Prioritas pengiriman kilat');

-- Adjust serial sequence for scroll_hdr
SELECT setval(pg_get_serial_sequence('dynamic.scroll_hdr', 'id'), 102);

INSERT INTO dynamic.scroll_dtl (id, invoice_id, item_code, item_name, qty, uom, price, discount_pct, discount_amt, tax_pct, tax_amt, weight_grid, batch_no, expiry_date, total_price, notes_detail) VALUES
(201, 101, 'IT-DEV', 'Software Development Service', 5, 'Man-Months', 15000000.00, 10.0, 7500000.00, 11.0, 7425000.00, 0.0, 'BATCH-01', '2027-12-31', 74925000.00, 'Development Phase 1'),
(202, 101, 'IT-OPS', 'IT Infrastructure Ops', 10, 'Hours', 850000.00, 0.0, 0.0, 11.0, 935000.00, 2.5, 'BATCH-02', '2026-12-31', 9435000.00, 'Support Setup'),
(203, 102, 'HR-REC', 'Senior Recruiter Hour', 20, 'Hours', 1200000.00, 5.0, 1200000.00, 11.0, 2508000.00, 1.2, 'BATCH-R3', '2026-08-31', 25308000.00, 'Recruitment Drive');

-- Adjust serial sequence for scroll_dtl
SELECT setval(pg_get_serial_sequence('dynamic.scroll_dtl', 'id'), 203);
