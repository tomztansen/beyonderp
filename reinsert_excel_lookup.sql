-- SQL Script to Re-import Excel Lookup Data with custom mapping

BEGIN;

-- 0. Ensure columns have sufficient length and refcode column exists
ALTER TABLE dynamic.mdlookup ALTER COLUMN code TYPE TEXT;
ALTER TABLE dynamic.mdlookup ALTER COLUMN name TYPE TEXT;
ALTER TABLE dynamic.mdlookup ALTER COLUMN description TYPE TEXT;
ALTER TABLE dynamic.mdlookup ADD COLUMN IF NOT EXISTS refcode TEXT;
ALTER TABLE dynamic.mdlookup ALTER COLUMN refcode TYPE TEXT;

-- 1. Insert/Update Master Header Lookup (mhlookup)
INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0001', 'Master : Account Type', 'Master : Account Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0001');
UPDATE dynamic.mhlookup SET category_name = 'Master : Account Type', description = 'Master : Account Type' WHERE category_code = 'MAS0001';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0002', 'Master : Item Type', 'Master : Item Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0002');
UPDATE dynamic.mhlookup SET category_name = 'Master : Item Type', description = 'Master : Item Type' WHERE category_code = 'MAS0002';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0003', 'Master : UOM', 'Master : UOM', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0003');
UPDATE dynamic.mhlookup SET category_name = 'Master : UOM', description = 'Master : UOM' WHERE category_code = 'MAS0003';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0004', 'Master : Business Partner Type', 'Master : Business Partner Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0004');
UPDATE dynamic.mhlookup SET category_name = 'Master : Business Partner Type', description = 'Master : Business Partner Type' WHERE category_code = 'MAS0004';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0006', 'Master : Down Payment Type', 'Master : Down Payment Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0006');
UPDATE dynamic.mhlookup SET category_name = 'Master : Down Payment Type', description = 'Master : Down Payment Type' WHERE category_code = 'MAS0006';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0007', 'Master : Delivery Mode', 'Master : Delivery Mode', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0007');
UPDATE dynamic.mhlookup SET category_name = 'Master : Delivery Mode', description = 'Master : Delivery Mode' WHERE category_code = 'MAS0007';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0008', 'Master : Delivery Term', 'Master : Delivery Term', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0008');
UPDATE dynamic.mhlookup SET category_name = 'Master : Delivery Term', description = 'Master : Delivery Term' WHERE category_code = 'MAS0008';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0009', 'Master : Purchase Type', 'Master : Purchase Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0009');
UPDATE dynamic.mhlookup SET category_name = 'Master : Purchase Type', description = 'Master : Purchase Type' WHERE category_code = 'MAS0009';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0010', 'Master : Purchase Payment Type', 'Master : Purchase Payment Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0010');
UPDATE dynamic.mhlookup SET category_name = 'Master : Purchase Payment Type', description = 'Master : Purchase Payment Type' WHERE category_code = 'MAS0010';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0011', 'Master : Sales Type', 'Master : Sales Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0011');
UPDATE dynamic.mhlookup SET category_name = 'Master : Sales Type', description = 'Master : Sales Type' WHERE category_code = 'MAS0011';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0012', 'Master : Sales Payment Type', 'Master : Sales Payment Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0012');
UPDATE dynamic.mhlookup SET category_name = 'Master : Sales Payment Type', description = 'Master : Sales Payment Type' WHERE category_code = 'MAS0012';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0013', 'Master : Element', 'Master : Element', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0013');
UPDATE dynamic.mhlookup SET category_name = 'Master : Element', description = 'Master : Element' WHERE category_code = 'MAS0013';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0014', 'Master : Item Parent Group', 'Master : Item Parent Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0014');
UPDATE dynamic.mhlookup SET category_name = 'Master : Item Parent Group', description = 'Master : Item Parent Group' WHERE category_code = 'MAS0014';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0015', 'Master : Item Group', 'Master : Item Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0015');
UPDATE dynamic.mhlookup SET category_name = 'Master : Item Group', description = 'Master : Item Group' WHERE category_code = 'MAS0015';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0016', 'Master : Resource Group', 'Master : Resource Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0016');
UPDATE dynamic.mhlookup SET category_name = 'Master : Resource Group', description = 'Master : Resource Group' WHERE category_code = 'MAS0016';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0017', 'Master : Site', 'Master : Site', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0017');
UPDATE dynamic.mhlookup SET category_name = 'Master : Site', description = 'Master : Site' WHERE category_code = 'MAS0017';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0018', 'Master : Resource', 'Master : Resource', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0018');
UPDATE dynamic.mhlookup SET category_name = 'Master : Resource', description = 'Master : Resource' WHERE category_code = 'MAS0018';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0020', 'Master : Procurement Category', 'Master : Procurement Category', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0020');
UPDATE dynamic.mhlookup SET category_name = 'Master : Procurement Category', description = 'Master : Procurement Category' WHERE category_code = 'MAS0020';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0021', 'Master : Sales Category', 'Master : Sales Category', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0021');
UPDATE dynamic.mhlookup SET category_name = 'Master : Sales Category', description = 'Master : Sales Category' WHERE category_code = 'MAS0021';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0022', 'Master : Warehouse', 'Master : Warehouse', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0022');
UPDATE dynamic.mhlookup SET category_name = 'Master : Warehouse', description = 'Master : Warehouse' WHERE category_code = 'MAS0022';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0023', 'Master : Location', 'Master : Location', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0023');
UPDATE dynamic.mhlookup SET category_name = 'Master : Location', description = 'Master : Location' WHERE category_code = 'MAS0023';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0024', 'Master : Tooling Group', 'Master : Tooling Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0024');
UPDATE dynamic.mhlookup SET category_name = 'Master : Tooling Group', description = 'Master : Tooling Group' WHERE category_code = 'MAS0024';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0027', 'Master : Asset Group', 'Master : Asset Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0027');
UPDATE dynamic.mhlookup SET category_name = 'Master : Asset Group', description = 'Master : Asset Group' WHERE category_code = 'MAS0027';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0028', 'Master : Depreciation Group', 'Master : Depreciation Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0028');
UPDATE dynamic.mhlookup SET category_name = 'Master : Depreciation Group', description = 'Master : Depreciation Group' WHERE category_code = 'MAS0028';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0029', 'Master : Business Unit', 'Master : Business Unit', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0029');
UPDATE dynamic.mhlookup SET category_name = 'Master : Business Unit', description = 'Master : Business Unit' WHERE category_code = 'MAS0029';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0030', 'Master : Department', 'Master : Department', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0030');
UPDATE dynamic.mhlookup SET category_name = 'Master : Department', description = 'Master : Department' WHERE category_code = 'MAS0030';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0031', 'Master : Asset Category', 'Master : Asset Category', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0031');
UPDATE dynamic.mhlookup SET category_name = 'Master : Asset Category', description = 'Master : Asset Category' WHERE category_code = 'MAS0031';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0032', 'Master : Asset Status', 'Master : Asset Status', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0032');
UPDATE dynamic.mhlookup SET category_name = 'Master : Asset Status', description = 'Master : Asset Status' WHERE category_code = 'MAS0032';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0033', 'Master : Asset Location', 'Master : Asset Location', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0033');
UPDATE dynamic.mhlookup SET category_name = 'Master : Asset Location', description = 'Master : Asset Location' WHERE category_code = 'MAS0033';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0038', 'Master : Energy Source', 'Master : Energy Source', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0038');
UPDATE dynamic.mhlookup SET category_name = 'Master : Energy Source', description = 'Master : Energy Source' WHERE category_code = 'MAS0038';

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0039', 'Master Shift', 'Master Shift', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0039');
UPDATE dynamic.mhlookup SET category_name = 'Master Shift', description = 'Master Shift' WHERE category_code = 'MAS0039';

-- 2. Clean up old detail items for these MAS categories before inserting fresh mapped data
DELETE FROM dynamic.mdlookup WHERE global_category_id IN (SELECT id FROM dynamic.mhlookup WHERE category_code LIKE 'MAS%');

-- 3. Insert Master Detail Lookup (mdlookup) with new mapping
INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 99, h.id, 'Debit', 'Debit', 'Debit', '0', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0001';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 100, h.id, 'Credit', 'Credit', 'Credit', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0001';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 101, h.id, 'Stock', 'Stock', 'Stock', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0002';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 102, h.id, 'Service', 'Service', 'Service', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0002';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2152, h.id, '1/2 cubic inch', '1/2 cu. in', '1/2 cu. in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2153, h.id, '1/2 inch', '1/2 in', '1/2 in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2154, h.id, '1/2 pound', '1/2 lb', '1/2 lb', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2155, h.id, 'half-pints', '1/2 pt', '1/2 pt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2156, h.id, '1/2 square inch', '1/2 sq. in', '1/2 sq. in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2157, h.id, '1/4 cubic inch', '1/4 cu. in', '1/4 cu. in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2158, h.id, '1/4 inch', '1/4 in', '1/4 in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2159, h.id, '1/4 pound', '1/4 lb', '1/4 lb', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2160, h.id, '1/4 square inch', '1/4 sq. in', '1/4 sq. in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2161, h.id, '1/8 cubic inch', '1/8 cu. in', '1/8 cu. in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2162, h.id, '1/8 inch', '1/8 in', '1/8 in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2163, h.id, '1/8 square inch', '1/8 sq. in', '1/8 sq. in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2164, h.id, 'Colly', 'Colly', 'Colly', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2165, h.id, 'Cycle', 'Cyc', 'Cyc', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2166, h.id, 'Power UOM for Power Plant', 'PRT', 'PRT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2167, h.id, 'Quantity', 'Quantity', 'Quantity', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2168, h.id, 'Ton', 'Ton', 'Ton', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2169, h.id, 'Short Ton (US)', 'Ton(US)', 'Ton(US)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2170, h.id, 'trip', 'Trip', 'Trip', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2171, h.id, 'bar', 'bar', 'bar', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2172, h.id, 'bungkus', 'bks', 'bks', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2173, h.id, 'blok', 'blk', 'blk', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2174, h.id, 'box', 'box', 'box', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2175, h.id, 'batang', 'btg', 'btg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2176, h.id, 'botol', 'btl', 'btl', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2177, h.id, 'can/kaleng', 'can', 'can', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2178, h.id, 'Centiliter', 'cl', 'cl', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2179, h.id, 'Centimeter', 'cm', 'cm', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2180, h.id, 'Square centimeter', 'cm2', 'cm2', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2181, h.id, 'Cubic centimeter', 'cm3', 'cm3', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2182, h.id, 'container', 'cont', 'cont', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2183, h.id, 'crate', 'crt', 'crt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2184, h.id, 'centong', 'ctg', 'ctg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2185, h.id, 'cubic feet', 'cu. ft', 'cu. ft', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2186, h.id, 'cubic inches', 'cu. in.', 'cu. in.', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2187, h.id, 'cubic yards', 'cu. yd', 'cu. yd', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2188, h.id, 'cup', 'cup', 'cup', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2189, h.id, 'Quintal', 'cwt', 'cwt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2190, h.id, 'day(s)', 'day', 'day', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2191, h.id, 'Deciliter', 'dl', 'dl', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2192, h.id, 'dirigen', 'drg', 'drg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2193, h.id, 'drum', 'drm', 'drm', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2194, h.id, 'dozen', 'dzn', 'dzn', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2195, h.id, 'fluid ounces', 'fl. oz', 'fl. oz', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2196, h.id, 'Feet', 'ft', 'ft', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2197, h.id, 'gram', 'g', 'g', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2198, h.id, 'Gallons', 'gal', 'gal', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2199, h.id, 'gulung', 'glg', 'glg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2200, h.id, 'hour(s)', 'hour', 'hour', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2202, h.id, 'ikat', 'ikt', 'ikt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2203, h.id, 'inches', 'in', 'in', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2204, h.id, 'kilogram', 'kg', 'kg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2205, h.id, 'kit', 'kit', 'kit', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2206, h.id, 'Kilometer', 'km', 'km', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2207, h.id, 'keping', 'kp', 'kp', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2208, h.id, 'kpg', 'kpg', 'kpg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2209, h.id, 'karung', 'krg', 'krg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2210, h.id, 'karton', 'ktn', 'ktn', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2211, h.id, 'kwin', 'kwin', 'kwin', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2212, h.id, 'Liter', 'l', 'l', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2213, h.id, 'pounds', 'lb', 'lb', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2214, h.id, 'lembar', 'lbr', 'lbr', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2215, h.id, 'unit measurement for service cost', 'lot', 'lot', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2216, h.id, 'Meters', 'm', 'm', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2217, h.id, 'meter', 'm', 'm', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2218, h.id, 'Square meter', 'm2', 'm2', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2219, h.id, 'Cubic meter', 'm3', 'm3', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2221, h.id, 'mobil', 'mbl', 'mbl', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2222, h.id, 'Milligram', 'mg', 'mg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2223, h.id, 'miles', 'mi', 'mi', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2224, h.id, 'minute(s)', 'min', 'min', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2225, h.id, 'Milliliter', 'ml', 'ml', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2226, h.id, 'Millimeter', 'mm', 'mm', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2227, h.id, 'Square millimeter', 'mm2', 'mm2', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2228, h.id, 'Cubic millimeter', 'mm3', 'mm3', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2229, h.id, 'month', 'month', 'month', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2230, h.id, 'metric tons', 'mt', 'mt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2231, h.id, 'ons', 'ons', 'ons', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2232, h.id, 'Ounces', 'oz', 'oz', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2233, h.id, 'pail', 'pal', 'pal', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2234, h.id, 'Pcs', 'pcs', 'pcs', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2235, h.id, 'pallet', 'plt', 'plt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2236, h.id, 'pasang', 'psg', 'psg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2237, h.id, 'pints', 'pt', 'pt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2238, h.id, 'potong', 'ptg', 'ptg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2239, h.id, 'quarts', 'qt', 'qt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2240, h.id, 'rim', 'rim', 'rim', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2241, h.id, 'rol', 'rol', 'rol', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2242, h.id, 'sak', 'sak', 'sak', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2243, h.id, 'second(s)', 'sec', 'sec', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2244, h.id, 'set', 'set', 'set', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2245, h.id, 'square feet', 'sq. ft', 'sq. ft', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2246, h.id, 'square inches', 'sq. in.', 'sq. in.', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2247, h.id, 'square miles', 'sq. mi', 'sq. mi', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2248, h.id, 'square yards', 'sq. yd', 'sq. yd', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2249, h.id, 'stel', 'stl', 'stl', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2250, h.id, 'Ton', 't', 't', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2251, h.id, 'tabung', 'tbg', 'tbg', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2252, h.id, 'tungkul', 'tkl', 'tkl', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2253, h.id, 'tube', 'tub', 'tub', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2254, h.id, 'unit', 'unt', 'unt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2255, h.id, 'storage unit cost', 'x', 'x', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2256, h.id, 'Yards', 'yd', 'yd', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2257, h.id, 'kWh', 'kWh', 'kWh', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 201, h.id, 'Corporate', 'Corporate', 'Corporate', '0', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0004';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 202, h.id, 'Individual', 'Individual', 'Individual', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0004';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1011, h.id, 'Payment', 'Payment', 'Payment', '0', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0006';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1012, h.id, 'Received', 'Received', 'Received', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0006';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1013, h.id, 'Truck', 'Truck', 'Truck', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0007';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1014, h.id, 'Sea', 'Sea', 'Sea', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0007';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1015, h.id, 'FOB', 'FOB', 'FOB', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0008';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1016, h.id, 'CIF', 'CIF', 'CIF', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0008';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1017, h.id, 'DAP', 'DAP', 'DAP', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0008';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1018, h.id, 'DDP', 'DDP', 'DDP', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0008';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1019, h.id, 'Purchase Order', 'Purchase Order', 'Purchase Order', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0009';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1020, h.id, 'Purchase Return', 'Purchase Return', 'Purchase Return', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0009';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1021, h.id, 'Down Payment', 'Down Payment', 'Down Payment', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0010';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1022, h.id, 'Purchase Invoice', 'Purchase Invoice', 'Purchase Invoice', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0010';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1023, h.id, 'Expense', 'Expense', 'Expense', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0010';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1024, h.id, 'Sales Order', 'Sales Order', 'Sales Order', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0011';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1025, h.id, 'Sales Return', 'Sales Return', 'Sales Return', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0011';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1026, h.id, 'Down Payment', 'Down Payment', 'Down Payment', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0012';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1027, h.id, 'Sales Invoice', 'Sales Invoice', 'Sales Invoice', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0012';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1028, h.id, 'Receiving', 'Receiving', 'Receiving', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0012';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1030, h.id, 'Ac', 'Actinium', 'Actinium', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1031, h.id, 'Al', 'Aluminium', 'Aluminium', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1032, h.id, 'Am', 'Americium', 'Americium', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1033, h.id, 'Sb', 'Antimony', 'Antimony', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1034, h.id, 'Ar', 'Argon', 'Argon', '5', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1035, h.id, 'As', 'Arsenic', 'Arsenic', '6', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1036, h.id, 'At', 'Astatine', 'Astatine', '7', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1037, h.id, 'Ba', 'Barium', 'Barium', '8', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1038, h.id, 'Bk', 'Berkelium', 'Berkelium', '9', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1039, h.id, 'Be', 'Beryllium', 'Beryllium', '10', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1040, h.id, 'Bi', 'Bismuth', 'Bismuth', '11', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1041, h.id, 'Bh', 'Bohrium', 'Bohrium', '12', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1042, h.id, 'B', 'Boron', 'Boron', '13', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1043, h.id, 'Br', 'Bromine', 'Bromine', '14', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1044, h.id, 'Cd', 'Cadmium', 'Cadmium', '15', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1045, h.id, 'Cs', 'Caesium', 'Caesium', '16', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1046, h.id, 'Ca', 'Calcium', 'Calcium', '17', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1047, h.id, 'Cf', 'Californium', 'Californium', '18', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1048, h.id, 'C', 'Carbon', 'Carbon', '19', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1049, h.id, 'Ce', 'Cerium', 'Cerium', '20', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1050, h.id, 'Cl', 'Chlorine', 'Chlorine', '21', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1051, h.id, 'Cr', 'Chromium', 'Chromium', '22', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1052, h.id, 'Co', 'Cobalt', 'Cobalt', '23', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1053, h.id, 'Cn', 'Copernicium', 'Copernicium', '24', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1054, h.id, 'Cu', 'Copper', 'Copper', '25', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1055, h.id, 'Cm', 'Curium', 'Curium', '26', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1056, h.id, 'Ds', 'Darmstadtium', 'Darmstadtium', '27', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1057, h.id, 'Db', 'Dubnium', 'Dubnium', '28', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1058, h.id, 'Dy', 'Dysprosium', 'Dysprosium', '29', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1059, h.id, 'Es', 'Einsteinium', 'Einsteinium', '30', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1060, h.id, 'Er', 'Erbium', 'Erbium', '31', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1061, h.id, 'Eu', 'Europium', 'Europium', '32', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1062, h.id, 'Fm', 'Fermium', 'Fermium', '33', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1063, h.id, 'Fl', 'Flerovium', 'Flerovium', '34', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1064, h.id, 'F', 'Fluorine', 'Fluorine', '35', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1065, h.id, 'Fr', 'Francium', 'Francium', '36', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1066, h.id, 'Gd', 'Gadolinium', 'Gadolinium', '37', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1067, h.id, 'Ga', 'Gallium', 'Gallium', '38', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1068, h.id, 'Ge', 'Germanium', 'Germanium', '39', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1069, h.id, 'Au', 'Gold', 'Gold', '40', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1070, h.id, 'Hf', 'Hafnium', 'Hafnium', '41', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1071, h.id, 'Hs', 'Hassium', 'Hassium', '42', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1072, h.id, 'He', 'Helium', 'Helium', '43', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1073, h.id, 'Ho', 'Holmium', 'Holmium', '44', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1074, h.id, 'H', 'Hydrogen', 'Hydrogen', '45', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1075, h.id, 'In', 'Indium', 'Indium', '46', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1076, h.id, 'I', 'Iodine', 'Iodine', '47', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1077, h.id, 'Ir', 'Iridium', 'Iridium', '48', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1078, h.id, 'Fe', 'Iron', 'Iron', '49', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1079, h.id, 'Kr', 'Krypton', 'Krypton', '50', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1080, h.id, 'La', 'Lanthanum', 'Lanthanum', '51', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1081, h.id, 'Lr', 'Lawrencium', 'Lawrencium', '52', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1082, h.id, 'Pb', 'Lead', 'Lead', '53', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1083, h.id, 'Li', 'Lithium', 'Lithium', '54', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1084, h.id, 'Lv', 'Livermorium', 'Livermorium', '55', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1085, h.id, 'Lu', 'Lutetium', 'Lutetium', '56', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1086, h.id, 'Mg', 'Magnesium', 'Magnesium', '57', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1087, h.id, 'Mn', 'Manganese', 'Manganese', '58', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1088, h.id, 'Mt', 'Meitnerium', 'Meitnerium', '59', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1089, h.id, 'Md', 'Mendelevium', 'Mendelevium', '60', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1090, h.id, 'Hg', 'Mercury', 'Mercury', '61', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1091, h.id, 'Mo', 'Molybdenum', 'Molybdenum', '62', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1092, h.id, 'Nd', 'Neodymium', 'Neodymium', '63', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1093, h.id, 'Ne', 'Neon', 'Neon', '64', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1094, h.id, 'Np', 'Neptunium', 'Neptunium', '65', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1095, h.id, 'Ni', 'Nickel', 'Nickel', '66', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1096, h.id, 'Nb', 'Niobium', 'Niobium', '67', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1097, h.id, 'N', 'Nitrogen', 'Nitrogen', '68', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1098, h.id, 'No', 'Nobelium', 'Nobelium', '69', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1099, h.id, 'Os', 'Osmium', 'Osmium', '70', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1100, h.id, 'O', 'Oxygen', 'Oxygen', '71', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1101, h.id, 'Pd', 'Palladium', 'Palladium', '72', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1102, h.id, 'P', 'Phosphorus', 'Phosphorus', '73', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1103, h.id, 'Pt', 'Platinum', 'Platinum', '74', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1104, h.id, 'Pu', 'Plutonium', 'Plutonium', '75', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1105, h.id, 'Po', 'Polonium', 'Polonium', '76', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1106, h.id, 'K', 'Potassium', 'Potassium', '77', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1107, h.id, 'Pr', 'Praseodymium', 'Praseodymium', '78', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1108, h.id, 'Pm', 'Promethium', 'Promethium', '79', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1109, h.id, 'Pa', 'Protactinium', 'Protactinium', '80', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1110, h.id, 'Ra', 'Radium', 'Radium', '81', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1111, h.id, 'Rn', 'Radon', 'Radon', '82', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1112, h.id, 'Re', 'Rhenium', 'Rhenium', '83', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1113, h.id, 'Rh', 'Rhodium', 'Rhodium', '84', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1114, h.id, 'Rg', 'Roentgenium', 'Roentgenium', '85', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1115, h.id, 'Rb', 'Rubidium', 'Rubidium', '86', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1116, h.id, 'Ru', 'Ruthenium', 'Ruthenium', '87', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1117, h.id, 'Rf', 'Rutherfordium', 'Rutherfordium', '88', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1118, h.id, 'Sm', 'Samarium', 'Samarium', '89', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1119, h.id, 'Sc', 'Scandium', 'Scandium', '90', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1120, h.id, 'Sg', 'Seaborgium', 'Seaborgium', '91', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1121, h.id, 'Se', 'Selenium', 'Selenium', '92', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1122, h.id, 'Si', 'Silicon', 'Silicon', '93', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1123, h.id, 'Ag', 'Silver', 'Silver', '94', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1124, h.id, 'Na', 'Sodium', 'Sodium', '95', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1125, h.id, 'Sr', 'Strontium', 'Strontium', '96', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1126, h.id, 'S', 'Sulfur', 'Sulfur', '97', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1127, h.id, 'Ta', 'Tantalum', 'Tantalum', '98', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1128, h.id, 'Tc', 'Technetium', 'Technetium', '99', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1129, h.id, 'Te', 'Tellurium', 'Tellurium', '100', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1130, h.id, 'Tb', 'Terbium', 'Terbium', '101', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1131, h.id, 'Tl', 'Thallium', 'Thallium', '102', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1132, h.id, 'Th', 'Thorium', 'Thorium', '103', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1133, h.id, 'Tm', 'Thulium', 'Thulium', '104', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1134, h.id, 'Sn', 'Tin', 'Tin', '105', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1135, h.id, 'Ti', 'Titanium', 'Titanium', '106', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1136, h.id, 'W', 'Tungsten', 'Tungsten', '107', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1137, h.id, 'Uuo', 'Ununoctium', 'Ununoctium', '108', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1138, h.id, 'Uup', 'Ununpentium', 'Ununpentium', '109', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1139, h.id, 'Uus', 'Ununseptium', 'Ununseptium', '110', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1140, h.id, 'Uut', 'Ununtrium', 'Ununtrium', '111', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1141, h.id, 'U', 'Uranium', 'Uranium', '112', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1142, h.id, 'V', 'Vanadium', 'Vanadium', '113', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1143, h.id, 'Xe', 'Xenon', 'Xenon', '114', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1144, h.id, 'Yb', 'Ytterbium', 'Ytterbium', '115', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1145, h.id, 'Y', 'Yttrium', 'Yttrium', '116', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1146, h.id, 'Zn', 'Zinc', 'Zinc', '117', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1147, h.id, 'Zr', 'Zirconium', 'Zirconium', '118', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2150, h.id, 'Fe4', 'Fe4', 'Fe4', '999', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2151, h.id, 'Fe4N', 'Fe4N', 'Fe4N', '888', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1149, h.id, '01. Raw Materials', '01. Raw Materials', '01. Raw Materials', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1150, h.id, '02. Additive', '02. Additive', '02. Additive', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1151, h.id, '03. Supporting Material', '03. Supporting Material', '03. Supporting Material', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1152, h.id, '04. Fixed Asset', '04. Fixed Asset', '04. Fixed Asset', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1153, h.id, '05. WIP', '05. WIP', '05. WIP', '5', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1154, h.id, '06. Semi Finished Goods', '06. Semi Finished Goods', '06. Semi Finished Goods', '6', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1155, h.id, '07. Finished Goods', '07. Finished Goods', '07. Finished Goods', '7', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1156, h.id, '08. Service', '08. Service', '08. Service', '8', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1157, h.id, '09. Material', '09. Material', '09. Material', '9', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1159, h.id, '2.01.01 Additive - Alloy', 'AD01.01', 'AD01.01', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1160, h.id, '2.01.02 Additive - Refractory For Metal', 'AD01.02', 'AD01.02', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1161, h.id, '2.01.03 Additive - Carbon Riser', 'AD01.03', 'AD01.03', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1162, h.id, '2.01.05 Additive - Exothermic Topping', 'AD01.05', 'AD01.05', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1163, h.id, '2.01.06 Additive - Deoxidiser and Desulphuriser Agent', 'AD01.06', 'AD01.06', '5', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1164, h.id, '2.01.07 Additive - Slag Removal', 'AD01.07', 'AD01.07', '6', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1165, h.id, '2.01.11 Additive - Binder', 'AD01.11', 'AD01.11', '7', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1166, h.id, '2.01.12 Additive - Coating', 'AD01.12', 'AD01.12', '8', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1167, h.id, '2.01.13 Additive - Sleeve', 'AD01.13', 'AD01.13', '9', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1168, h.id, '2.01.14 Additive - Ceramic', 'AD01.14', 'AD01.14', '10', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1169, h.id, '2.01.15 Additive - Joining Compound', 'AD01.15', 'AD01.15', '11', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1170, h.id, '2.03.01 Rubber Additive - Antitack', 'AD03.01', 'AD03.01', '12', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1171, h.id, '2.03.02 Rubber Additive - Bonding Agent', 'AD03.02', 'AD03.02', '13', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1172, h.id, '2.03.03 Rubber Additive - Solvent', 'AD03.03', 'AD03.03', '14', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1173, h.id, '2.03.04 Rubber Additive - Mold Release Agent', 'AD03.04', 'AD03.04', '15', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1174, h.id, '3.11. Chemical', 'Chemical', 'Chemical', '16', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1175, h.id, '3.13. Consumable for General', 'ConsGen', 'ConsGen', '17', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1176, h.id, '3.13. Consumable For Production', 'ConsProd', 'ConsProd', '18', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1177, h.id, '3.12. Construction Material', 'Construct', 'Construct', '19', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1178, h.id, '3.14. Electrical Material', 'Electric', 'Electric', '20', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1179, h.id, '4.01. Fixed Asset Items', 'FA', 'FA', '21', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1180, h.id, '7.01.01 FG Foundry - GAI', 'FG01.01', 'FG01.01', '22', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1181, h.id, '7.01.02 FG Foundry - GAS', 'FG01.02', 'FG01.02', '23', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1182, h.id, '7.01.03 FG Foundry - Others', 'FG01.03', 'FG01.03', '24', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1183, h.id, '7.01.09 FG Foundry - Set', 'FG01.09', 'FG01.09', '25', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1184, h.id, '7.02.01 FG Fastener - Bolt', 'FG02.01', 'FG02.01', '26', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1185, h.id, '7.02.02 FG Fastener - Nut', 'FG02.02', 'FG02.02', '27', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1186, h.id, '7.02.03 FG Fastener - Washer', 'FG02.03', 'FG02.03', '28', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1187, h.id, '7.02.04 FG Fastener - Set', 'FG02.04', 'FG02.04', '29', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1188, h.id, '7.02.05 FG Fastener - Others', 'FG02.05', 'FG02.05', '30', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1189, h.id, '7.03.01 FG Rubber - Liner', 'FG03.01', 'FG03.01', '31', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1190, h.id, '7.03.04 FG Rubber - Joint Strip', 'FG03.04', 'FG03.04', '32', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1191, h.id, '7.03.05 FG Rubber - Seal', 'FG03.05', 'FG03.05', '33', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1192, h.id, '7.03.06 FG Rubber - Set', 'FG03.06', 'FG03.06', '34', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1193, h.id, '7.03.07 FG Rubber - Others', 'FG03.07', 'FG03.07', '35', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1194, h.id, '7.04.01 FG Grinding Media - Ball', 'FG04.01', 'FG04.01', '36', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1195, h.id, '7.04.02 FG Grinding Media - Rod', 'FG04.02', 'FG04.02', '37', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1196, h.id, '3.16. Fuel & Gas', 'FuelGas', 'FuelGas', '38', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1197, h.id, '3.15. Furniture', 'Furniture', 'Furniture', '39', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1198, h.id, '3.17. Grocery', 'Grocery', 'Grocery', '40', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1199, h.id, '3.21. Home Appliance', 'HomeApp', 'HomeApp', '41', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1200, h.id, '3.18. IT Supplies', 'IT', 'IT', '42', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1201, h.id, '3.33. Instruments', 'Instrument', 'Instrument', '43', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1202, h.id, '3.34. Merchandise Marketing', 'Marketing', 'Marketing', '44', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1203, h.id, '9.01. Material Foundry', 'Mat01', 'Mat01', '45', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1204, h.id, '9.02. Material Fastener', 'Mat02', 'Mat02', '46', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1205, h.id, '9.03. Material Rubber', 'Mat03', 'Mat03', '47', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1206, h.id, '9.04. Material Grinding Ball', 'Mat04', 'Mat04', '48', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1207, h.id, '3.20. Medical Supplies', 'Medical', 'Medical', '49', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1208, h.id, '3.22. Office Supplies', 'OfficeSup', 'OfficeSup', '50', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1209, h.id, '3.23. Oil & Lubricant', 'Oil&Lub', 'Oil&Lub', '51', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1210, h.id, '3.24. Personnal Protection Equipment', 'PPE', 'PPE', '52', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1211, h.id, '3.31. Packaging Material', 'Packaging', 'Packaging', '53', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1212, h.id, '1.01.01 RM Foundry - Scrap', 'RM01.01', 'RM01.01', '54', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1213, h.id, '1.01.11 RM Foundry - Sand', 'RM01.11', 'RM01.11', '55', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1214, h.id, '1.02.01 RM Fastener - As Steel Bar 42CRMO', 'RM02.01', 'RM02.01', '56', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1215, h.id, '1.02.02 RM Fastener - As Steel Bar 1040', 'RM02.02', 'RM02.02', '57', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1216, h.id, '1.02.03 RM Fastener - As AISI 4140', 'RM02.03', 'RM02.03', '58', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1217, h.id, '1.02.04 RM Fastener - As SUS', 'RM02.04', 'RM02.04', '59', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1218, h.id, '1.03.01 RM Rubber - Polymer', 'RM03.01', 'RM03.01', '60', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1219, h.id, '1.03.02 RM Rubber - Fillers', 'RM03.02', 'RM03.02', '61', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1220, h.id, '1.03.03 RM Rubber - Chemical', 'RM03.03', 'RM03.03', '62', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1221, h.id, '1.03.04 RM Rubber - Curative Agent', 'RM03.04', 'RM03.04', '63', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1222, h.id, '1.03.05 RM Rubber - Oil', 'RM03.05', 'RM03.05', '64', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1223, h.id, '1.03.06 RM Rubber - Coloring Agent', 'RM03.06', 'RM03.06', '65', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1224, h.id, '1.03.09 RM Rubber - Metal', 'RM03.09', 'RM03.09', '66', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1225, h.id, '1.04.01 RM GM - Steel Bar', 'RM04.01', 'RM04.01', '67', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1226, h.id, '1.05.01 RM PLTU - Wood', 'RM05.01', 'RM05.01', '68', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1227, h.id, '1.05.02 RM PLTU - Palm', 'RM05.02', 'RM05.02', '69', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1228, h.id, '1.05.03 RM PLTU - Biomass', 'RM05.03', 'RM05.03', '70', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1229, h.id, '8.99. Uncategorized Service', 'SERVICE', 'SERVICE', '71', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1230, h.id, '5.01. Semi Finished Goods - Composite Casting', 'SF01.01', 'SF01.01', '72', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1231, h.id, '5.02. Ceramic Rubber Tube', 'SF03.01', 'SF03.01', '73', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1232, h.id, '5.03. Semi Finished Goods - Wearplate', 'SF03.02', 'SF03.02', '74', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1233, h.id, '8.01. IT Service', 'Serv001', 'Serv001', '75', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1234, h.id, '8.02. Import Cost', 'Serv002', 'Serv002', '76', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1235, h.id, '8.03. Export Cost', 'Serv003', 'Serv003', '77', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1236, h.id, '8.04. Maintenance', 'Serv004', 'Serv004', '78', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1237, h.id, '8.05. Rent', 'Serv005', 'Serv005', '79', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1238, h.id, '8.06. Expedition', 'Serv006', 'Serv006', '80', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1239, h.id, '8.07. Jasa Outsourcing', 'Serv007', 'Serv007', '81', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1240, h.id, '8.08. Waste Treatment', 'Serv008', 'Serv008', '82', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1241, h.id, '8.09. Professional and Engineer', 'Serv009', 'Serv009', '83', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1242, h.id, '8.10. Marketing', 'Serv010', 'Serv010', '84', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1243, h.id, '8.11. Seminar and Training', 'Serv011', 'Serv011', '85', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1244, h.id, '8.12. Project', 'Serv012', 'Serv012', '86', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1245, h.id, '8.13. Prepaid', 'Serv013', 'Serv013', '87', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1246, h.id, '8.16. Sembako', 'Serv016', 'Serv016', '88', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1247, h.id, '3.25. Spare Part', 'Sparepart', 'Sparepart', '89', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1248, h.id, '3.26. Steel', 'Steel', 'Steel', '90', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1249, h.id, '3.27. Toiletry Supplies', 'Toiletry', 'Toiletry', '91', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1250, h.id, '3.28. Tools', 'Tools', 'Tools', '92', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1251, h.id, '3.35. Uniform', 'Uniform', 'Uniform', '93', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1252, h.id, '3.30. Wood', 'Wood', 'Wood', '94', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2303, h.id, '1.03.07 RM Rubber - Casting', 'RM03.07', 'RM03.07', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1253, h.id, 'Foundry Resource Group', 'Foundry', 'Foundry', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0016';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1254, h.id, 'Fastener Resource Group', 'Fastener', 'Fastener', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0016';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1255, h.id, 'Rubber Resource Group', 'Rubber', 'Rubber', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0016';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1256, h.id, 'Grinding Media Resource Group', 'Grinding Media', 'Grinding Media', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0016';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1261, h.id, 'Grinding Media Pasuruan', 'GA.BU08', 'GA.BU08', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2001, h.id, 'HO Medan', 'GA.BU07', 'GA.BU07', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2127, h.id, 'Foundry', 'GA.BU01', 'GA.BU01', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2128, h.id, 'Fastener', 'GA.BU02', 'GA.BU02', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2129, h.id, 'Rubber Plant', 'GA.BU03', 'GA.BU03', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2130, h.id, 'Grinding Media Medan', 'GA.BU04', 'GA.BU04', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2131, h.id, 'PLTU I', 'GA.BU05', 'GA.BU05', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2132, h.id, 'PLTU II', 'GA.BU06', 'GA.BU06', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1262, h.id, 'Foundry - Moulding', 'Foundry - Moulding', 'Foundry - Moulding', '101', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1263, h.id, 'Foundry - Knock Out', 'Foundry - Knock Out', 'Foundry - Knock Out', '105', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1264, h.id, 'Foundry - Heat Treatment', 'Foundry - Heat Treatment', 'Foundry - Heat Treatment', '107', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1980, h.id, 'Foundry - Melting', 'Foundry - Melting', 'Foundry - Melting', '102', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1981, h.id, 'Foundry - Fettling', 'Foundry - Fettling', 'Foundry - Fettling', '103', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1982, h.id, 'Foundry - Machining', 'Foundry - Machining', 'Foundry - Machining', '108', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1985, h.id, 'Foundry - Final Finishing', 'Foundry - Final Finishing', 'Foundry - Final Finishing', '109', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1986, h.id, 'Foundry - QC', 'Foundry - QC', 'Foundry - QC', '110', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1987, h.id, 'Rubber - Mixing', 'Rubber - Mixing', 'Rubber - Mixing', '301', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1988, h.id, 'Fastener - Bolt Cutting', 'Fastener - Bolt Cutting', 'Fastener - Bolt Cutting', '201', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1989, h.id, 'Fastener - Bolt MPI', 'Fastener -  Bolt MPI', 'Fastener -  Bolt MPI', '209', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1990, h.id, 'Fastener - Washer Cutting', 'Fastener - Washer Cutting', 'Fastener - Washer Cutting', '202', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1991, h.id, 'Fastener - Bolt Forging', 'Fastener - Bolt Forging', 'Fastener - Bolt Forging', '203', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1993, h.id, 'Fastener - Bolt Heat Treatment', 'Fastener -  Bolt Heat Treatment', 'Fastener -  Bolt Heat Treatment', '204', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1995, h.id, 'Rubber - Laminating', 'Rubber - Laminating', 'Rubber - Laminating', '303', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1996, h.id, 'Rubber - Extrusion', 'Rubber - Extrusion', 'Rubber - Extrusion', '304', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1997, h.id, 'Rubber - Compression Moulding', 'Rubber - Compression Moulding', 'Rubber - Compression Moulding', '305', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1998, h.id, 'Rubber - Autoclave', 'Rubber - Autoclave', 'Rubber - Autoclave', '306', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2265, h.id, 'Rubber - Hand Lining', 'Rubber - Hand Lining', 'Rubber - Hand Lining', '307', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2266, h.id, 'Rubber - Final Finishing', 'Rubber - Final Finishing', 'Rubber - Final Finishing', '308', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2267, h.id, 'Rubber - QC', 'Rubber - QC', 'Rubber - QC', '309', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2271, h.id, 'Rubber - Preforming', 'Rubber - Preforming', 'Rubber - Preforming', '310', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2277, h.id, 'Rubber - Shotblasting', 'Rubber - Shotblasting', 'Rubber - Shotblasting', '311', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2278, h.id, 'Rubber - Spray Boothing', 'Rubber - Spray Boothing', 'Rubber - Spray Boothing', '312', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2283, h.id, 'Fastener - Washer Machining', 'Fastener - Washer Machining', 'Fastener - Washer Machining', '206', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2284, h.id, 'Fastener - Bolt Machining', 'Fastener - Bolt Machining', 'Fastener - Bolt Machining', '207', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2286, h.id, 'Fastener - Bolt Roll Tread', 'Fastener -  Bolt Roll Tread', 'Fastener -  Bolt Roll Tread', '208', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2287, h.id, 'Fastener - QC', 'Fastener - QC', 'Fastener - QC', '210', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1268, h.id, '01. Raw Material', '01. Raw Material', '01. Raw Material', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1269, h.id, '07. Finished  Goods', '07. Finished  Goods', '07. Finished  Goods', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1270, h.id, '08. Service', '08. Service', '08. Service', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1271, h.id, '1.01-001 RM Foundry - Scrap', '1.01-001 RM Foundry - Scrap', '1.01-001 RM Foundry - Scrap', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1272, h.id, '1.01-011.01 RM Foundry - Sand - Chromite', '1.01-011.01 RM Foundry - Sand - Chromite', '1.01-011.01 RM Foundry - Sand - Chromite', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1273, h.id, '1.01-011.02 RM Foundry - Sand - Silica', '1.01-011.02 RM Foundry - Sand - Silica', '1.01-011.02 RM Foundry - Sand - Silica', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1274, h.id, '1.01-011.99 RM Foundry - Other Sand', '1.01-011.99 RM Foundry - Other Sand', '1.01-011.99 RM Foundry - Other Sand', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1275, h.id, '1.02-001.01 RM Fastener - AS Steel Bar 42CRMO - Bolt', '1.02-001.01 RM Fastener - AS Steel Bar 42CRMO - Bolt', '1.02-001.01 RM Fastener - AS Steel Bar 42CRMO - Bolt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1276, h.id, '1.02-001.02 RM Fastener - AS Steel Bar 42CRMO - Washer', '1.02-001.02 RM Fastener - AS Steel Bar 42CRMO - Washer', '1.02-001.02 RM Fastener - AS Steel Bar 42CRMO - Washer', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1277, h.id, '1.02-002 RM Fastener - As Steel Bar 1040', '1.02-002 RM Fastener - As Steel Bar 1040', '1.02-002 RM Fastener - As Steel Bar 1040', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1278, h.id, '1.02-003 RM Fastener - As AISI 4140', '1.02-003 RM Fastener - As AISI 4140', '1.02-003 RM Fastener - As AISI 4140', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1279, h.id, '1.02-004 RM Fastener - As SUS', '1.02-004 RM Fastener - As SUS', '1.02-004 RM Fastener - As SUS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1280, h.id, '1.03-001 RM Rubber - Polymer', '1.03-001 RM Rubber - Polymer', '1.03-001 RM Rubber - Polymer', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1281, h.id, '1.03-002 RM Rubber - Fillers', '1.03-002 RM Rubber - Fillers', '1.03-002 RM Rubber - Fillers', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1282, h.id, '1.03-003 RM Rubber - Chemical', '1.03-003 RM Rubber - Chemical', '1.03-003 RM Rubber - Chemical', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1283, h.id, '1.03-004 RM Rubber - Curative Agent', '1.03-004 RM Rubber - Curative Agent', '1.03-004 RM Rubber - Curative Agent', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1284, h.id, '1.03-005 RM Rubber - Oil', '1.03-005 RM Rubber - Oil', '1.03-005 RM Rubber - Oil', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1285, h.id, '1.03-006 RM Rubber - Coloring Agent', '1.03-006 RM Rubber - Coloring Agent', '1.03-006 RM Rubber - Coloring Agent', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1286, h.id, '1.03-007 RM Rubber - Casting', '1.03-007 RM Rubber - Casting', '1.03-007 RM Rubber - Casting', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1287, h.id, '1.03-901 Wearplate', '1.03-901 Wearplate', '1.03-901 Wearplate', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1288, h.id, '1.03-902 Aluminium C-Channel', '1.03-902 Aluminium C-Channel', '1.03-902 Aluminium C-Channel', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1289, h.id, '1.03-903 Aluminium T-Track', '1.03-903 Aluminium T-Track', '1.03-903 Aluminium T-Track', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1290, h.id, '1.03-904 Mild Steel C-Channel', '1.03-904 Mild Steel C-Channel', '1.03-904 Mild Steel C-Channel', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1291, h.id, '1.04-001 RM GM - Steel Bar', '1.04-001 RM GM - Steel Bar', '1.04-001 RM GM - Steel Bar', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1292, h.id, '1.05-001 RM PLTU - Wood', '1.05-001 RM PLTU - Wood', '1.05-001 RM PLTU - Wood', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1293, h.id, '1.05-002 RM PLTU - Palm', '1.05-002 RM PLTU - Palm', '1.05-002 RM PLTU - Palm', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1294, h.id, '1.05-003 RM PLTU - Biomass', '1.05-003 RM PLTU - Biomass', '1.05-003 RM PLTU - Biomass', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1295, h.id, '2.01-001.01 Additive - Alloy - Ferro Chromium', '2.01-001.01 Additive - Alloy - Ferro Chromium', '2.01-001.01 Additive - Alloy - Ferro Chromium', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1296, h.id, '2.01-001.02 Additive - Alloy - Ferro Molybdenum', '2.01-001.02 Additive - Alloy - Ferro Molybdenum', '2.01-001.02 Additive - Alloy - Ferro Molybdenum', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1297, h.id, '2.01-001.03 Additive - Alloy - Ferro Silicon', '2.01-001.03 Additive - Alloy - Ferro Silicon', '2.01-001.03 Additive - Alloy - Ferro Silicon', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1298, h.id, '2.01-001.04 Additive - Alloy - Ferro Titanium', '2.01-001.04 Additive - Alloy - Ferro Titanium', '2.01-001.04 Additive - Alloy - Ferro Titanium', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1299, h.id, '2.01-001.99 Additive - Alloy - Others', '2.01-001.99 Additive - Alloy - Others', '2.01-001.99 Additive - Alloy - Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1300, h.id, '2.01-002 Additive - Refractory For Metal', '2.01-002 Additive - Refractory For Metal', '2.01-002 Additive - Refractory For Metal', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1301, h.id, '2.01-003 Additive - Carbon Riser', '2.01-003 Additive - Carbon Riser', '2.01-003 Additive - Carbon Riser', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1302, h.id, '2.01-005 Additive - Exothermic Topping', '2.01-005 Additive - Exothermic Topping', '2.01-005 Additive - Exothermic Topping', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1303, h.id, '2.01-006 Additive - Deoxidiser and Desulphuriser Agent', '2.01-006 Additive - Deoxidiser and Desulphuriser Agent', '2.01-006 Additive - Deoxidiser and Desulphuriser Agent', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1304, h.id, '2.01-007 Additive - Slag Removal', '2.01-007 Additive - Slag Removal', '2.01-007 Additive - Slag Removal', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1305, h.id, '2.01-011 Additive - Binder - Catalyst', '2.01-011 Additive - Binder - Catalyst', '2.01-011 Additive - Binder - Catalyst', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1306, h.id, '2.01-011 Additive - Binder - Resin', '2.01-011 Additive - Binder - Resin', '2.01-011 Additive - Binder - Resin', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1307, h.id, '2.01-012 Additive - Coating', '2.01-012 Additive - Coating', '2.01-012 Additive - Coating', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1308, h.id, '2.01-012 Additive - Solvent/Fuel', '2.01-012 Additive - Solvent/Fuel', '2.01-012 Additive - Solvent/Fuel', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1309, h.id, '2.01-013 Additive - Sleeve', '2.01-013 Additive - Sleeve', '2.01-013 Additive - Sleeve', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1310, h.id, '2.01-014 Additive - Ceramic', '2.01-014 Additive - Ceramic', '2.01-014 Additive - Ceramic', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1311, h.id, '2.01-015 Additive - Joining Compound', '2.01-015 Additive - Joining Compound', '2.01-015 Additive - Joining Compound', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1312, h.id, '2.03-001 Rubber Additive - Antitack', '2.03-001 Rubber Additive - Antitack', '2.03-001 Rubber Additive - Antitack', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1313, h.id, '2.03-002 Rubber Additive - Bonding Agent', '2.03-002 Rubber Additive - Bonding Agent', '2.03-002 Rubber Additive - Bonding Agent', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1314, h.id, '2.03-003 Rubber Additive - Solvent', '2.03-003 Rubber Additive - Solvent', '2.03-003 Rubber Additive - Solvent', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1315, h.id, '3.11-001 Chemical Solid', '3.11-001 Chemical Solid', '3.11-001 Chemical Solid', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1316, h.id, '3.11-002 Chemical Liquid', '3.11-002 Chemical Liquid', '3.11-002 Chemical Liquid', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1317, h.id, '3.12-001 PLAT', '3.12-001 PLAT', '3.12-001 PLAT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1318, h.id, '3.12-002 PASIR', '3.12-002 PASIR', '3.12-002 PASIR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1319, h.id, '3.12-003 KRAN / PADDLE', '3.12-003 KRAN / PADDLE', '3.12-003 KRAN / PADDLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1320, h.id, '3.12-004 KLOSET / URINAL', '3.12-004 KLOSET / URINAL', '3.12-004 KLOSET / URINAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1321, h.id, '3.12-005 WIREMESH', '3.12-005 WIREMESH', '3.12-005 WIREMESH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1322, h.id, '3.12-006 SENG', '3.12-006 SENG', '3.12-006 SENG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1323, h.id, '3.12-007 ENGSEL', '3.12-007 ENGSEL', '3.12-007 ENGSEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1324, h.id, '3.12-008 SEMEN', '3.12-008 SEMEN', '3.12-008 SEMEN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1325, h.id, '3.12-009 WASTAFEL', '3.12-009 WASTAFEL', '3.12-009 WASTAFEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1326, h.id, '3.12-010 PARTISI', '3.12-010 PARTISI', '3.12-010 PARTISI', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1327, h.id, '3.12-011 BAJA RINGAN / FURING', '3.12-011 BAJA RINGAN / FURING', '3.12-011 BAJA RINGAN / FURING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1328, h.id, '3.12-012 BATU / DITCH / BOREPILE', '3.12-012 BATU / DITCH / BOREPILE', '3.12-012 BATU / DITCH / BOREPILE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1329, h.id, '3.12-013 KAWAT', '3.12-013 KAWAT', '3.12-013 KAWAT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1330, h.id, '3.12-014 KERAMIK', '3.12-014 KERAMIK', '3.12-014 KERAMIK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1331, h.id, '3.12-015 KALSIBOARD / GYPSUM', '3.12-015 KALSIBOARD / GYPSUM', '3.12-015 KALSIBOARD / GYPSUM', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1332, h.id, '3.12-016 DEMPUL', '3.12-016 DEMPUL', '3.12-016 DEMPUL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1333, h.id, '3.12-017 READY MIX', '3.12-017 READY MIX', '3.12-017 READY MIX', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1334, h.id, '3.12-018 PINTU / JENDELA / PAGAR', '3.12-018 PINTU / JENDELA / PAGAR', '3.12-018 PINTU / JENDELA / PAGAR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1335, h.id, '3.12-019 KACA', '3.12-019 KACA', '3.12-019 KACA', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1336, h.id, '3.12-020 SHOWER', '3.12-020 SHOWER', '3.12-020 SHOWER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1337, h.id, '3.12-021 POLYCARBONATE', '3.12-021 POLYCARBONATE', '3.12-021 POLYCARBONATE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1338, h.id, '3.12-022 GRENDEL / HANDLE / KUNCI', '3.12-022 GRENDEL / HANDLE / KUNCI', '3.12-022 GRENDEL / HANDLE / KUNCI', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1339, h.id, '3.12-023 PITA ASBES', '3.12-023 PITA ASBES', '3.12-023 PITA ASBES', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1340, h.id, '3.12-024 BAHAN BANGUNAN LAINNYA', '3.12-024 BAHAN BANGUNAN LAINNYA', '3.12-024 BAHAN BANGUNAN LAINNYA', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1341, h.id, '3.12. Construction Material', '3.12. Construction Material', '3.12. Construction Material', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1342, h.id, '3.13-001 ADHESIVE (Perekat/ Lem)', '3.13-001 ADHESIVE (Perekat/ Lem)', '3.13-001 ADHESIVE (Perekat/ Lem)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1343, h.id, '3.13-002 DOUBLE BEAMS', '3.13-002 DOUBLE BEAMS', '3.13-002 DOUBLE BEAMS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1344, h.id, '3.13-003 GAS DIFFUSER', '3.13-003 GAS DIFFUSER', '3.13-003 GAS DIFFUSER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1345, h.id, '3.13-004 MOTOR SIRENE', '3.13-004 MOTOR SIRENE', '3.13-004 MOTOR SIRENE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1346, h.id, '3.13-005 WELDING WIRE', '3.13-005 WELDING WIRE', '3.13-005 WELDING WIRE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1347, h.id, '3.13-006 PIPE', '3.13-006 PIPE', '3.13-006 PIPE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1348, h.id, '3.13-007 RUBBER', '3.13-007 RUBBER', '3.13-007 RUBBER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1349, h.id, '3.13-008 ASPAL', '3.13-008 ASPAL', '3.13-008 ASPAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1350, h.id, '3.13-009 KERTAS PASIR', '3.13-009 KERTAS PASIR', '3.13-009 KERTAS PASIR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1351, h.id, '3.13-010 BATU / MATA GRINDER', '3.13-010 BATU / MATA GRINDER', '3.13-010 BATU / MATA GRINDER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1352, h.id, '3.13-011 SOCKET', '3.13-011 SOCKET', '3.13-011 SOCKET', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1353, h.id, '3.13-012 KLINGERIT', '3.13-012 KLINGERIT', '3.13-012 KLINGERIT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1354, h.id, '3.13-013 REDUCER', '3.13-013 REDUCER', '3.13-013 REDUCER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1355, h.id, '3.13-014 NOZZLE', '3.13-014 NOZZLE', '3.13-014 NOZZLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1356, h.id, '3.13-015 SLING', '3.13-015 SLING', '3.13-015 SLING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1357, h.id, '3.13-016 TOOL HOLDER', '3.13-016 TOOL HOLDER', '3.13-016 TOOL HOLDER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1358, h.id, '3.13-017 TEE', '3.13-017 TEE', '3.13-017 TEE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1359, h.id, '3.13-018 KLEM', '3.13-018 KLEM', '3.13-018 KLEM', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1360, h.id, '3.13-019 DRILL', '3.13-019 DRILL', '3.13-019 DRILL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1361, h.id, '3.13-020 TOOL BOARD', '3.13-020 TOOL BOARD', '3.13-020 TOOL BOARD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1362, h.id, '3.13-021 CAT / SEALER', '3.13-021 CAT / SEALER', '3.13-021 CAT / SEALER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1363, h.id, '3.13-022 STOPPER RODS', '3.13-022 STOPPER RODS', '3.13-022 STOPPER RODS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1364, h.id, '3.13-023 MASTER LINK', '3.13-023 MASTER LINK', '3.13-023 MASTER LINK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1365, h.id, '3.13-024 COATING', '3.13-024 COATING', '3.13-024 COATING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1366, h.id, '3.13-025 PAKU', '3.13-025 PAKU', '3.13-025 PAKU', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1367, h.id, '3.13-026 SHIM PLATE', '3.13-026 SHIM PLATE', '3.13-026 SHIM PLATE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1368, h.id, '3.13-027 CLEANER', '3.13-027 CLEANER', '3.13-027 CLEANER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1369, h.id, '3.13-028 KAWAT EMAIL DRAT', '3.13-028 KAWAT EMAIL DRAT', '3.13-028 KAWAT EMAIL DRAT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1370, h.id, '3.13-029 HEAT SHRINK / SELONGSONG KABEL', '3.13-029 HEAT SHRINK / SELONGSONG KABEL', '3.13-029 HEAT SHRINK / SELONGSONG KABEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1371, h.id, '3.13-030 DOP', '3.13-030 DOP', '3.13-030 DOP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1372, h.id, '3.13-031 GEMBOK', '3.13-031 GEMBOK', '3.13-031 GEMBOK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1373, h.id, '3.13-032 WIRE ROPE', '3.13-032 WIRE ROPE', '3.13-032 WIRE ROPE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1374, h.id, '3.13-033 FISHER', '3.13-033 FISHER', '3.13-033 FISHER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1375, h.id, '3.13-034 TALI', '3.13-034 TALI', '3.13-034 TALI', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1376, h.id, '3.13-035 PLATE', '3.13-035 PLATE', '3.13-035 PLATE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1377, h.id, '3.13-036 MATA PAHAT', '3.13-036 MATA PAHAT', '3.13-036 MATA PAHAT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1378, h.id, '3.13-037 TOMBOL / KNOB', '3.13-037 TOMBOL / KNOB', '3.13-037 TOMBOL / KNOB', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1379, h.id, '3.13-038 SARUNG EMAIL', '3.13-038 SARUNG EMAIL', '3.13-038 SARUNG EMAIL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1380, h.id, '3.13-039 KAWAT AYAKAN', '3.13-039 KAWAT AYAKAN', '3.13-039 KAWAT AYAKAN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1381, h.id, '3.13-040 PROBE', '3.13-040 PROBE', '3.13-040 PROBE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1382, h.id, '3.13-041 GRIT', '3.13-041 GRIT', '3.13-041 GRIT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1383, h.id, '3.13-042 DUCT', '3.13-042 DUCT', '3.13-042 DUCT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1384, h.id, '3.13-043 FILLING / FILLER', '3.13-043 FILLING / FILLER', '3.13-043 FILLING / FILLER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1385, h.id, '3.13-044 ....', '3.13-044 ....', '3.13-044 ....', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1386, h.id, '3.13-999 GENERAL', '3.13-999 GENERAL', '3.13-999 GENERAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1387, h.id, '3.13. Consumable', '3.13. Consumable', '3.13. Consumable', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1388, h.id, '3.14-001 BREAKER', '3.14-001 BREAKER', '3.14-001 BREAKER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1389, h.id, '3.14-002 FUSE / SEKRING', '3.14-002 FUSE / SEKRING', '3.14-002 FUSE / SEKRING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1390, h.id, '3.14-003 CAPASITOR', '3.14-003 CAPASITOR', '3.14-003 CAPASITOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1391, h.id, '3.14-004 SWITCH / SAKLAR', '3.14-004 SWITCH / SAKLAR', '3.14-004 SWITCH / SAKLAR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1392, h.id, '3.14-006 COVER', '3.14-006 COVER', '3.14-006 COVER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1393, h.id, '3.14-007 CABLE', '3.14-007 CABLE', '3.14-007 CABLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1394, h.id, '3.14-008 INSULATOR / ISOLATOR', '3.14-008 INSULATOR / ISOLATOR', '3.14-008 INSULATOR / ISOLATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1395, h.id, '3.14-009 CONDUIT', '3.14-009 CONDUIT', '3.14-009 CONDUIT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1396, h.id, '3.14-010 SOCKET/STOP KONTAK', '3.14-010 SOCKET/STOP KONTAK', '3.14-010 SOCKET/STOP KONTAK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1397, h.id, '3.14-011 BOX PANEL', '3.14-011 BOX PANEL', '3.14-011 BOX PANEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1398, h.id, '3.14-012 BUSBAR', '3.14-012 BUSBAR', '3.14-012 BUSBAR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1399, h.id, '3.14-013 RESISTOR', '3.14-013 RESISTOR', '3.14-013 RESISTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1400, h.id, '3.14-014 PLUG', '3.14-014 PLUG', '3.14-014 PLUG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1401, h.id, '3.14-015 MAGNETIC CONTACTOR', '3.14-015 MAGNETIC CONTACTOR', '3.14-015 MAGNETIC CONTACTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1402, h.id, '3.14-016 INVERTER', '3.14-016 INVERTER', '3.14-016 INVERTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1403, h.id, '3.14-017 SKUN', '3.14-017 SKUN', '3.14-017 SKUN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1404, h.id, '3.14-018 CONNECTOR', '3.14-018 CONNECTOR', '3.14-018 CONNECTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1405, h.id, '3.14-019 MODULE', '3.14-019 MODULE', '3.14-019 MODULE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1406, h.id, '3.14-020 SOLENOID', '3.14-020 SOLENOID', '3.14-020 SOLENOID', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1407, h.id, '3.14-021 STABILIZER', '3.14-021 STABILIZER', '3.14-021 STABILIZER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1408, h.id, '3.14-022 POWER SUPPLY', '3.14-022 POWER SUPPLY', '3.14-022 POWER SUPPLY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1409, h.id, '3.14-033 RELAY', '3.14-033 RELAY', '3.14-033 RELAY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1410, h.id, '3.14-034 HMI', '3.14-034 HMI', '3.14-034 HMI', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1411, h.id, '3.14-035 PLC', '3.14-035 PLC', '3.14-035 PLC', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1412, h.id, '3.14-036 TEMPERATURE CONTROLLER', '3.14-036 TEMPERATURE CONTROLLER', '3.14-036 TEMPERATURE CONTROLLER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1413, h.id, '3.14-037 LAMP', '3.14-037 LAMP', '3.14-037 LAMP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1414, h.id, '3.14-038 POTENSIOMETER', '3.14-038 POTENSIOMETER', '3.14-038 POTENSIOMETER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1415, h.id, '3.14-039 MCB', '3.14-039 MCB', '3.14-039 MCB', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1416, h.id, '3.14-040 SCR', '3.14-040 SCR', '3.14-040 SCR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1417, h.id, '3.14-041 TRANSDUCER', '3.14-041 TRANSDUCER', '3.14-041 TRANSDUCER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1418, h.id, '3.14-042 MCCB', '3.14-042 MCCB', '3.14-042 MCCB', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1419, h.id, '3.14-043 UPS', '3.14-043 UPS', '3.14-043 UPS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1420, h.id, '3.14-044 HZ / FREQUENCY METER', '3.14-044 HZ / FREQUENCY METER', '3.14-044 HZ / FREQUENCY METER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1421, h.id, '3.14-045 PCB', '3.14-045 PCB', '3.14-045 PCB', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1422, h.id, '3.14-046 TRAFO', '3.14-046 TRAFO', '3.14-046 TRAFO', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1423, h.id, '3.14-047 SHUNT', '3.14-047 SHUNT', '3.14-047 SHUNT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1424, h.id, '3.14-048 BOARD', '3.14-048 BOARD', '3.14-048 BOARD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1425, h.id, '3.14-049 VARISTOR', '3.14-049 VARISTOR', '3.14-049 VARISTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1426, h.id, '3.14-050 REGULATOR', '3.14-050 REGULATOR', '3.14-050 REGULATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1427, h.id, '3.14-051 COIL', '3.14-051 COIL', '3.14-051 COIL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1428, h.id, '3.14-052 VOLT METER', '3.14-052 VOLT METER', '3.14-052 VOLT METER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1429, h.id, '3.14-053 KW METER / POWER METER', '3.14-053 KW METER / POWER METER', '3.14-053 KW METER / POWER METER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1430, h.id, '3.14-054 AMPERE METER', '3.14-054 AMPERE METER', '3.14-054 AMPERE METER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1431, h.id, '3.14-055 BUZZER', '3.14-055 BUZZER', '3.14-055 BUZZER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1432, h.id, '3.14-056 CONTACTOR', '3.14-056 CONTACTOR', '3.14-056 CONTACTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1433, h.id, '3.14-057 ACB', '3.14-057 ACB', '3.14-057 ACB', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1434, h.id, '3.14-059 PANEL METER', '3.14-059 PANEL METER', '3.14-059 PANEL METER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1435, h.id, '3.14-060 THYRISTOR / TRANSISTOR', '3.14-060 THYRISTOR / TRANSISTOR', '3.14-060 THYRISTOR / TRANSISTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1436, h.id, '3.14-061 TIMER', '3.14-061 TIMER', '3.14-061 TIMER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1437, h.id, '3.14-062 TERMINAL', '3.14-062 TERMINAL', '3.14-062 TERMINAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1438, h.id, '3.14-063 CONVERTER', '3.14-063 CONVERTER', '3.14-063 CONVERTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1439, h.id, '3.14-064 GENERAL', '3.14-064 GENERAL', '3.14-064 GENERAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1440, h.id, '3.16-001 Petrol', '3.16-001 Petrol', '3.16-001 Petrol', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1441, h.id, '3.16-002 Gas O2', '3.16-002 Gas O2', '3.16-002 Gas O2', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1442, h.id, '3.16-003 Gas CO2', '3.16-003 Gas CO2', '3.16-003 Gas CO2', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1443, h.id, '3.16-004 Natural Gas', '3.16-004 Natural Gas', '3.16-004 Natural Gas', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1444, h.id, '3.16-999 Other Fuel', '3.16-999 Other Fuel', '3.16-999 Other Fuel', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1445, h.id, '3.17-001 SEMBAKO', '3.17-001 SEMBAKO', '3.17-001 SEMBAKO', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1446, h.id, '3.17-002 GENERAL', '3.17-002 GENERAL', '3.17-002 GENERAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1447, h.id, '3.18-001 CPU', '3.18-001 CPU', '3.18-001 CPU', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1448, h.id, '3.18-002 LAPTOP', '3.18-002 LAPTOP', '3.18-002 LAPTOP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1449, h.id, '3.18-003 MONITOR', '3.18-003 MONITOR', '3.18-003 MONITOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1450, h.id, '3.18-004 PRINTER', '3.18-004 PRINTER', '3.18-004 PRINTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1451, h.id, '3.18-005 SCANNER', '3.18-005 SCANNER', '3.18-005 SCANNER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1452, h.id, '3.18-006 TOOLS', '3.18-006 TOOLS', '3.18-006 TOOLS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1453, h.id, '3.18-007 MOTHERBOARD', '3.18-007 MOTHERBOARD', '3.18-007 MOTHERBOARD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1454, h.id, '3.18-008 NETWORKING', '3.18-008 NETWORKING', '3.18-008 NETWORKING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1455, h.id, '3.18-009 MEMORY', '3.18-009 MEMORY', '3.18-009 MEMORY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1456, h.id, '3.18-010 HARDDISK', '3.18-010 HARDDISK', '3.18-010 HARDDISK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1457, h.id, '3.18-011 POWER SUPPLY', '3.18-011 POWER SUPPLY', '3.18-011 POWER SUPPLY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1458, h.id, '3.18-012 SERVER', '3.18-012 SERVER', '3.18-012 SERVER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1459, h.id, '3.18-013 CCTV', '3.18-013 CCTV', '3.18-013 CCTV', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1460, h.id, '3.18-014 UPS', '3.18-014 UPS', '3.18-014 UPS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1461, h.id, '3.18-015 KABEL IT', '3.18-015 KABEL IT', '3.18-015 KABEL IT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1462, h.id, '3.18-016 ACCESSORIES', '3.18-016 ACCESSORIES', '3.18-016 ACCESSORIES', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1463, h.id, '3.18-017 CONVERTER', '3.18-017 CONVERTER', '3.18-017 CONVERTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1464, h.id, '3.18-018 SPLITTER', '3.18-018 SPLITTER', '3.18-018 SPLITTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1465, h.id, '3.18-019 DASH CAMERA', '3.18-019 DASH CAMERA', '3.18-019 DASH CAMERA', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1466, h.id, '3.18-020 PROYEKTOR', '3.18-020 PROYEKTOR', '3.18-020 PROYEKTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1467, h.id, '3.20-002 Obat Luar', '3.20-002 Obat Luar', '3.20-002 Obat Luar', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1468, h.id, '3.20-003 Health Equipment', '3.20-003 Health Equipment', '3.20-003 Health Equipment', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1469, h.id, '3.20-004 General', '3.20-004 General', '3.20-004 General', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1470, h.id, '3.21-001 Desk & Chair', '3.21-001 Desk & Chair', '3.21-001 Desk & Chair', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1471, h.id, '3.21-002 Furniture', '3.21-002 Furniture', '3.21-002 Furniture', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1472, h.id, '3.21-003 Accessories', '3.21-003 Accessories', '3.21-003 Accessories', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1473, h.id, '3.21-004 Cabinet', '3.21-004 Cabinet', '3.21-004 Cabinet', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1474, h.id, '3.21-005 Electronic', '3.21-005 Electronic', '3.21-005 Electronic', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1475, h.id, '3.22-001 BOOK / BINDING / MAP', '3.22-001 BOOK / BINDING / MAP', '3.22-001 BOOK / BINDING / MAP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1476, h.id, '3.22-002 FORM', '3.22-002 FORM', '3.22-002 FORM', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1477, h.id, '3.22-003 PAPER', '3.22-003 PAPER', '3.22-003 PAPER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1478, h.id, '3.22-004 ATK', '3.22-004 ATK', '3.22-004 ATK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1479, h.id, '3.22-005 STAPLES', '3.22-005 STAPLES', '3.22-005 STAPLES', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1480, h.id, '3.22-006 GENERAL', '3.22-006 GENERAL', '3.22-006 GENERAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1481, h.id, '3.22-007 Amplop', '3.22-007 Amplop', '3.22-007 Amplop', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1482, h.id, '3.22-008 TINTA/TONER', '3.22-008 TINTA/TONER', '3.22-008 TINTA/TONER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1483, h.id, '3.22-009 Battery', '3.22-009 Battery', '3.22-009 Battery', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1484, h.id, '3.22-010 Sticker', '3.22-010 Sticker', '3.22-010 Sticker', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1485, h.id, '3.22-011 Isolasiban / Tape', '3.22-011 Isolasiban / Tape', '3.22-011 Isolasiban / Tape', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1486, h.id, '3.22-012 Plastic', '3.22-012 Plastic', '3.22-012 Plastic', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1487, h.id, '3.22-013 PUNCH / PERFORATOR', '3.22-013 PUNCH / PERFORATOR', '3.22-013 PUNCH / PERFORATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1488, h.id, '3.23-001 Grease', '3.23-001 Grease', '3.23-001 Grease', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1489, h.id, '3.23-002 Oli', '3.23-002 Oli', '3.23-002 Oli', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1490, h.id, '3.23-999 Other Oil', '3.23-999 Other Oil', '3.23-999 Other Oil', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1491, h.id, '3.24-001 HELMET', '3.24-001 HELMET', '3.24-001 HELMET', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1492, h.id, '3.24-002 KACAMATA', '3.24-002 KACAMATA', '3.24-002 KACAMATA', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1493, h.id, '3.24-003 TOPENG', '3.24-003 TOPENG', '3.24-003 TOPENG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1494, h.id, '3.24-004 KACA LAS', '3.24-004 KACA LAS', '3.24-004 KACA LAS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1495, h.id, '3.24-005 SERAGAM', '3.24-005 SERAGAM', '3.24-005 SERAGAM', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1496, h.id, '3.24-006 MASKER', '3.24-006 MASKER', '3.24-006 MASKER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1497, h.id, '3.24-007 SARUNG TANGAN', '3.24-007 SARUNG TANGAN', '3.24-007 SARUNG TANGAN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1498, h.id, '3.24-008 SHOES & BOOT', '3.24-008 SHOES & BOOT', '3.24-008 SHOES & BOOT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1499, h.id, '3.24-009 EAR PLUG', '3.24-009 EAR PLUG', '3.24-009 EAR PLUG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1500, h.id, '3.24-010 SAFETY BELT', '3.24-010 SAFETY BELT', '3.24-010 SAFETY BELT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1501, h.id, '3.24-011 APAR', '3.24-011 APAR', '3.24-011 APAR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1502, h.id, '3.24-012 CARTRIDGE DUST', '3.24-012 CARTRIDGE DUST', '3.24-012 CARTRIDGE DUST', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1503, h.id, '3.24-999 OTHER SAFETY EQUIPMENT', '3.24-999 OTHER SAFETY EQUIPMENT', '3.24-999 OTHER SAFETY EQUIPMENT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1504, h.id, '3.25-001 ....', '3.25-001 ....', '3.25-001 ....', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1505, h.id, '3.25-002 ADAPTER', '3.25-002 ADAPTER', '3.25-002 ADAPTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1506, h.id, '3.25-003 ADJUSTING INSTRUMENT', '3.25-003 ADJUSTING INSTRUMENT', '3.25-003 ADJUSTING INSTRUMENT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1507, h.id, '3.25-004 AIR CHUCK', '3.25-004 AIR CHUCK', '3.25-004 AIR CHUCK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1508, h.id, '3.25-005 AIR CONDITIONER', '3.25-005 AIR CONDITIONER', '3.25-005 AIR CONDITIONER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1509, h.id, '3.25-006 AUXILIARY', '3.25-006 AUXILIARY', '3.25-006 AUXILIARY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1510, h.id, '3.25-008 ARMATURE', '3.25-008 ARMATURE', '3.25-008 ARMATURE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1511, h.id, '3.25-009 AXLE', '3.25-009 AXLE', '3.25-009 AXLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1512, h.id, '3.25-012 BEARING', '3.25-012 BEARING', '3.25-012 BEARING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1513, h.id, '3.25-013 BELTING', '3.25-013 BELTING', '3.25-013 BELTING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1514, h.id, '3.25-015 BOHEL', '3.25-015 BOHEL', '3.25-015 BOHEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1515, h.id, '3.25-017 BOX FUSE', '3.25-017 BOX FUSE', '3.25-017 BOX FUSE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1516, h.id, '3.25-018 BRACKET', '3.25-018 BRACKET', '3.25-018 BRACKET', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1517, h.id, '3.25-019 BRAKE', '3.25-019 BRAKE', '3.25-019 BRAKE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1518, h.id, '3.25-020 BRUSH', '3.25-020 BRUSH', '3.25-020 BRUSH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1519, h.id, '3.25-021 BUCKET TOOTH', '3.25-021 BUCKET TOOTH', '3.25-021 BUCKET TOOTH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1520, h.id, '3.25-022 BUSHING', '3.25-022 BUSHING', '3.25-022 BUSHING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1521, h.id, '3.25-024 CAP / TUTUP', '3.25-024 CAP / TUTUP', '3.25-024 CAP / TUTUP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1522, h.id, '3.25-025 CASING', '3.25-025 CASING', '3.25-025 CASING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1523, h.id, '3.25-026 CHARGER', '3.25-026 CHARGER', '3.25-026 CHARGER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1524, h.id, '3.25-027 CHUCK', '3.25-027 CHUCK', '3.25-027 CHUCK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1525, h.id, '3.25-028 COMMUNICATION', '3.25-028 COMMUNICATION', '3.25-028 COMMUNICATION', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1526, h.id, '3.25-029 CLAMP', '3.25-029 CLAMP', '3.25-029 CLAMP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1527, h.id, '3.25-030 CLIP', '3.25-030 CLIP', '3.25-030 CLIP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1528, h.id, '3.25-032 COIL', '3.25-032 COIL', '3.25-032 COIL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1529, h.id, '3.25-033 CONNECTOR', '3.25-033 CONNECTOR', '3.25-033 CONNECTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1530, h.id, '3.25-034 CONTROLLER', '3.25-034 CONTROLLER', '3.25-034 CONTROLLER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1531, h.id, '3.25-035 COOLER', '3.25-035 COOLER', '3.25-035 COOLER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1532, h.id, '3.25-036 COUNTER', '3.25-036 COUNTER', '3.25-036 COUNTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1533, h.id, '3.25-037 COUPLING', '3.25-037 COUPLING', '3.25-037 COUPLING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1534, h.id, '3.25-038 COVER', '3.25-038 COVER', '3.25-038 COVER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1535, h.id, '3.25-039 CYLINDER', '3.25-039 CYLINDER', '3.25-039 CYLINDER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1536, h.id, '3.25-040 DAUN KIPAS', '3.25-040 DAUN KIPAS', '3.25-040 DAUN KIPAS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1537, h.id, '3.25-041 DIAPRAGM', '3.25-041 DIAPRAGM', '3.25-041 DIAPRAGM', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1538, h.id, '3.25-043 DRAIN', '3.25-043 DRAIN', '3.25-043 DRAIN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1539, h.id, '3.25-046 ELECTRO MOTOR', '3.25-046 ELECTRO MOTOR', '3.25-046 ELECTRO MOTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1540, h.id, '3.25-047 ENGINE', '3.25-047 ENGINE', '3.25-047 ENGINE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1541, h.id, '3.25-048 FAN', '3.25-048 FAN', '3.25-048 FAN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1542, h.id, '3.25-049 FASTENER', '3.25-049 FASTENER', '3.25-049 FASTENER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1543, h.id, '3.25-050 FILTER', '3.25-050 FILTER', '3.25-050 FILTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1544, h.id, '3.25-051 FLIER', '3.25-051 FLIER', '3.25-051 FLIER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1545, h.id, '3.25-052 FORK', '3.25-052 FORK', '3.25-052 FORK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1546, h.id, '3.25-054 GASKET', '3.25-054 GASKET', '3.25-054 GASKET', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1547, h.id, '3.25-055 GAUGE', '3.25-055 GAUGE', '3.25-055 GAUGE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1548, h.id, '3.25-056 GEAR', '3.25-056 GEAR', '3.25-056 GEAR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1549, h.id, '3.25-057 GEARBOX', '3.25-057 GEARBOX', '3.25-057 GEARBOX', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1550, h.id, '3.25-059 GOVERNOR ASSY', '3.25-059 GOVERNOR ASSY', '3.25-059 GOVERNOR ASSY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1551, h.id, '3.25-061 GUARD', '3.25-061 GUARD', '3.25-061 GUARD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1552, h.id, '3.25-062 HANDLE', '3.25-062 HANDLE', '3.25-062 HANDLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1553, h.id, '3.25-063 HANGING PLATE', '3.25-063 HANGING PLATE', '3.25-063 HANGING PLATE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1554, h.id, '3.25-064 GRID', '3.25-064 GRID', '3.25-064 GRID', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1555, h.id, '3.25-065 HEATER', '3.25-065 HEATER', '3.25-065 HEATER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1556, h.id, '3.25-066 HOOK', '3.25-066 HOOK', '3.25-066 HOOK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1557, h.id, '3.25-067 HORN', '3.25-067 HORN', '3.25-067 HORN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1558, h.id, '3.25-068 HOSE / SELANG', '3.25-068 HOSE / SELANG', '3.25-068 HOSE / SELANG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1559, h.id, '3.25-069 HYDROLIC MOTOR', '3.25-069 HYDROLIC MOTOR', '3.25-069 HYDROLIC MOTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1560, h.id, '3.25-070 HINGE / ENGSEL', '3.25-070 HINGE / ENGSEL', '3.25-070 HINGE / ENGSEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1561, h.id, '3.25-071 IMPELLER / EXPELLER', '3.25-071 IMPELLER / EXPELLER', '3.25-071 IMPELLER / EXPELLER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1562, h.id, '3.25-072 INDICATOR', '3.25-072 INDICATOR', '3.25-072 INDICATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1563, h.id, '3.25-073 JOINT / JOINER', '3.25-073 JOINT / JOINER', '3.25-073 JOINT / JOINER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1564, h.id, '3.25-074 LEVER', '3.25-074 LEVER', '3.25-074 LEVER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1565, h.id, '3.25-075 KEY / KIT', '3.25-075 KEY / KIT', '3.25-075 KEY / KIT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1566, h.id, '3.25-076 LAMP VEHICLE', '3.25-076 LAMP VEHICLE', '3.25-076 LAMP VEHICLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1567, h.id, '3.25-077 LATCH', '3.25-077 LATCH', '3.25-077 LATCH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1568, h.id, '3.25-078 LINER', '3.25-078 LINER', '3.25-078 LINER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1569, h.id, '3.25-079 LINK', '3.25-079 LINK', '3.25-079 LINK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1570, h.id, '3.25-080 LOCK', '3.25-080 LOCK', '3.25-080 LOCK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1571, h.id, '3.25-081 LUBRICATOR', '3.25-081 LUBRICATOR', '3.25-081 LUBRICATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1572, h.id, '3.25-083 METALAN', '3.25-083 METALAN', '3.25-083 METALAN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1573, h.id, '3.25-084 MIRROR / GLASS', '3.25-084 MIRROR / GLASS', '3.25-084 MIRROR / GLASS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1574, h.id, '3.25-085 MUFFLER', '3.25-085 MUFFLER', '3.25-085 MUFFLER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1575, h.id, '3.25-086 NEPPLE/NIPPLE', '3.25-086 NEPPLE/NIPPLE', '3.25-086 NEPPLE/NIPPLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1576, h.id, '3.25-087 O-RING', '3.25-087 O-RING', '3.25-087 O-RING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1577, h.id, '3.25-088 PACKING', '3.25-088 PACKING', '3.25-088 PACKING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1578, h.id, '3.25-089 PEDAL', '3.25-089 PEDAL', '3.25-089 PEDAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1579, h.id, '3.25-090 PIN', '3.25-090 PIN', '3.25-090 PIN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1580, h.id, '3.25-091 PISTON', '3.25-091 PISTON', '3.25-091 PISTON', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1581, h.id, '3.25-092 PLATE', '3.25-092 PLATE', '3.25-092 PLATE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1582, h.id, '3.25-093 PLUG', '3.25-093 PLUG', '3.25-093 PLUG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1583, h.id, '3.25-094 PNEUMATIC', '3.25-094 PNEUMATIC', '3.25-094 PNEUMATIC', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1584, h.id, '3.25-095 PULLEY', '3.25-095 PULLEY', '3.25-095 PULLEY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1585, h.id, '3.25-096 PUMP', '3.25-096 PUMP', '3.25-096 PUMP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1586, h.id, '3.25-097 PUSHER', '3.25-097 PUSHER', '3.25-097 PUSHER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1587, h.id, '3.25-098 RADIATOR', '3.25-098 RADIATOR', '3.25-098 RADIATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1588, h.id, '3.25-099 INDUCTION MOTOR', '3.25-099 INDUCTION MOTOR', '3.25-099 INDUCTION MOTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1589, h.id, '3.25-100 REDUCER', '3.25-100 REDUCER', '3.25-100 REDUCER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1590, h.id, '3.25-101 REGULATOR', '3.25-101 REGULATOR', '3.25-101 REGULATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1591, h.id, '3.25-102 RELAY', '3.25-102 RELAY', '3.25-102 RELAY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1592, h.id, '3.25-103 REPAIR KIT', '3.25-103 REPAIR KIT', '3.25-103 REPAIR KIT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1593, h.id, '3.25-105 RING', '3.25-105 RING', '3.25-105 RING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1594, h.id, '3.25-106 TIE ROD', '3.25-106 TIE ROD', '3.25-106 TIE ROD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1595, h.id, '3.25-107 ROLLER', '3.25-107 ROLLER', '3.25-107 ROLLER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1596, h.id, '3.25-108 PILLOW BLOCK', '3.25-108 PILLOW BLOCK', '3.25-108 PILLOW BLOCK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1597, h.id, '3.25-109 SEAL', '3.25-109 SEAL', '3.25-109 SEAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1598, h.id, '3.25-110 SENSOR', '3.25-110 SENSOR', '3.25-110 SENSOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1599, h.id, '3.25-111 SEPARATOR', '3.25-111 SEPARATOR', '3.25-111 SEPARATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1600, h.id, '3.25-112 SERVO', '3.25-112 SERVO', '3.25-112 SERVO', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1601, h.id, '3.25-113 SHAFT', '3.25-113 SHAFT', '3.25-113 SHAFT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1602, h.id, '3.25-114 SHEAVE', '3.25-114 SHEAVE', '3.25-114 SHEAVE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1603, h.id, '3.25-115 SHIM', '3.25-115 SHIM', '3.25-115 SHIM', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1604, h.id, '3.25-116 SHOE', '3.25-116 SHOE', '3.25-116 SHOE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1605, h.id, '3.25-117 SITTING', '3.25-117 SITTING', '3.25-117 SITTING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1606, h.id, '3.25-118 SNAP RING', '3.25-118 SNAP RING', '3.25-118 SNAP RING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1607, h.id, '3.25-120 VELG', '3.25-120 VELG', '3.25-120 VELG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1608, h.id, '3.25-121 SPACER', '3.25-121 SPACER', '3.25-121 SPACER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1609, h.id, '3.25-122 SPINDLE', '3.25-122 SPINDLE', '3.25-122 SPINDLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1610, h.id, '3.25-123 SPRING (PER)', '3.25-123 SPRING (PER)', '3.25-123 SPRING (PER)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1611, h.id, '3.25-124 SPROCKET', '3.25-124 SPROCKET', '3.25-124 SPROCKET', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1612, h.id, '3.25-125 STRAINER', '3.25-125 STRAINER', '3.25-125 STRAINER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1613, h.id, '3.25-126 STARTER', '3.25-126 STARTER', '3.25-126 STARTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1614, h.id, '3.25-127 PINION', '3.25-127 PINION', '3.25-127 PINION', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1615, h.id, '3.25-129 SWITCH', '3.25-129 SWITCH', '3.25-129 SWITCH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1616, h.id, '3.25-130 TIRE / BAN', '3.25-130 TIRE / BAN', '3.25-130 TIRE / BAN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1617, h.id, '3.25-131 TRANSMITION', '3.25-131 TRANSMITION', '3.25-131 TRANSMITION', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1618, h.id, '3.25-132 TUBE', '3.25-132 TUBE', '3.25-132 TUBE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1619, h.id, '3.25-133 DINAMO MOTOR', '3.25-133 DINAMO MOTOR', '3.25-133 DINAMO MOTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1620, h.id, '3.25-134 VALVE / KLEP', '3.25-134 VALVE / KLEP', '3.25-134 VALVE / KLEP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1621, h.id, '3.25-135 WASHER', '3.25-135 WASHER', '3.25-135 WASHER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1622, h.id, '3.25-136 WHEEL', '3.25-136 WHEEL', '3.25-136 WHEEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1623, h.id, '3.25-137 WIPER', '3.25-137 WIPER', '3.25-137 WIPER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1624, h.id, '3.25-138 WRENCH', '3.25-138 WRENCH', '3.25-138 WRENCH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1625, h.id, '3.25-139 NOZZLE', '3.25-139 NOZZLE', '3.25-139 NOZZLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1626, h.id, '3.25-140 SHIELD', '3.25-140 SHIELD', '3.25-140 SHIELD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1627, h.id, '3.25-141 FITTING', '3.25-141 FITTING', '3.25-141 FITTING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1628, h.id, '3.25-142 HOUSING', '3.25-142 HOUSING', '3.25-142 HOUSING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1629, h.id, '3.25-143 FLANGE', '3.25-143 FLANGE', '3.25-143 FLANGE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1630, h.id, '3.25-144 PTO', '3.25-144 PTO', '3.25-144 PTO', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1631, h.id, '3.25-145 SUPPORTING', '3.25-145 SUPPORTING', '3.25-145 SUPPORTING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1632, h.id, '3.25-146 DISCHARGER', '3.25-146 DISCHARGER', '3.25-146 DISCHARGER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1633, h.id, '3.25-147 BAUT / BOLT', '3.25-147 BAUT / BOLT', '3.25-147 BAUT / BOLT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1634, h.id, '3.25-148 OIL SEAL', '3.25-148 OIL SEAL', '3.25-148 OIL SEAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1635, h.id, '3.25-149 CHAIN', '3.25-149 CHAIN', '3.25-149 CHAIN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1636, h.id, '3.25-150 HUB', '3.25-150 HUB', '3.25-150 HUB', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1637, h.id, '3.25-151 SAFETY VALVE', '3.25-151 SAFETY VALVE', '3.25-151 SAFETY VALVE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1638, h.id, '3.25-152 BREATHER', '3.25-152 BREATHER', '3.25-152 BREATHER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1639, h.id, '3.25-153 CONTROL VALVE', '3.25-153 CONTROL VALVE', '3.25-153 CONTROL VALVE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1640, h.id, '3.25-154 NUT / MUR', '3.25-154 NUT / MUR', '3.25-154 NUT / MUR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1641, h.id, '3.25-155 SHOCK', '3.25-155 SHOCK', '3.25-155 SHOCK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1642, h.id, '3.25-156 CARRIAGE', '3.25-156 CARRIAGE', '3.25-156 CARRIAGE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1643, h.id, '3.25-157 UPPER', '3.25-157 UPPER', '3.25-157 UPPER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1644, h.id, '3.25-158 REMOTE CONTROLLER', '3.25-158 REMOTE CONTROLLER', '3.25-158 REMOTE CONTROLLER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1645, h.id, '3.25-159 SCREEN MESH', '3.25-159 SCREEN MESH', '3.25-159 SCREEN MESH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1646, h.id, '3.25-160 ROTOR', '3.25-160 ROTOR', '3.25-160 ROTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1647, h.id, '3.25-161 EXTENSIONER', '3.25-161 EXTENSIONER', '3.25-161 EXTENSIONER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1648, h.id, '3.25-162 CUSHION', '3.25-162 CUSHION', '3.25-162 CUSHION', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1649, h.id, '3.25-163 STEERING', '3.25-163 STEERING', '3.25-163 STEERING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1650, h.id, '3.25-164 BUCKGUY', '3.25-164 BUCKGUY', '3.25-164 BUCKGUY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1651, h.id, '3.25-165 ACTUATOR', '3.25-165 ACTUATOR', '3.25-165 ACTUATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1652, h.id, '3.25-166 TANK', '3.25-166 TANK', '3.25-166 TANK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1653, h.id, '3.25-167 OIL LEVEL', '3.25-167 OIL LEVEL', '3.25-167 OIL LEVEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1654, h.id, '3.25-168 GLAND', '3.25-168 GLAND', '3.25-168 GLAND', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1655, h.id, '3.25-169 PIPA (FACTORY VEHICLE)', '3.25-169 PIPA (FACTORY VEHICLE)', '3.25-169 PIPA (FACTORY VEHICLE)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1656, h.id, '3.25-170 BATTERY', '3.25-170 BATTERY', '3.25-170 BATTERY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1657, h.id, '3.25-171 AIR VENT', '3.25-171 AIR VENT', '3.25-171 AIR VENT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1658, h.id, '3.25-172 ROUND ROD', '3.25-172 ROUND ROD', '3.25-172 ROUND ROD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1659, h.id, '3.25-173 KLOS', '3.25-173 KLOS', '3.25-173 KLOS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1660, h.id, '3.25-174 GUIDE VANE', '3.25-174 GUIDE VANE', '3.25-174 GUIDE VANE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1661, h.id, '3.25-175 AIR DRYER', '3.25-175 AIR DRYER', '3.25-175 AIR DRYER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1662, h.id, '3.25-176 SWIVEL', '3.25-176 SWIVEL', '3.25-176 SWIVEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1663, h.id, '3.25-185 VANES', '3.25-185 VANES', '3.25-185 VANES', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1664, h.id, '3.25-186 GEAR MOTOR', '3.25-186 GEAR MOTOR', '3.25-186 GEAR MOTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1665, h.id, '3.25-187 KEY / SPIE', '3.25-187 KEY / SPIE', '3.25-187 KEY / SPIE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1666, h.id, '3.25-188 COLLAR', '3.25-188 COLLAR', '3.25-188 COLLAR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1667, h.id, '3.25-189 SCREW', '3.25-189 SCREW', '3.25-189 SCREW', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1668, h.id, '3.25-190 AIR MOTOR', '3.25-190 AIR MOTOR', '3.25-190 AIR MOTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1669, h.id, '3.25-191 DIFFUSER PUMP', '3.25-191 DIFFUSER PUMP', '3.25-191 DIFFUSER PUMP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1670, h.id, '3.25-192 SLEEVE', '3.25-192 SLEEVE', '3.25-192 SLEEVE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1671, h.id, '3.25-193 ROD', '3.25-193 ROD', '3.25-193 ROD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1672, h.id, '3.25-194 NECK', '3.25-194 NECK', '3.25-194 NECK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1673, h.id, '3.25-195 MOTOR & SWITCH', '3.25-195 MOTOR & SWITCH', '3.25-195 MOTOR & SWITCH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1674, h.id, '3.25-196 THERMOSTATIC', '3.25-196 THERMOSTATIC', '3.25-196 THERMOSTATIC', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1675, h.id, '3.25-197 FLOW CONTROL', '3.25-197 FLOW CONTROL', '3.25-197 FLOW CONTROL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1676, h.id, '3.25-198 BLOWDOWN', '3.25-198 BLOWDOWN', '3.25-198 BLOWDOWN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1677, h.id, '3.25-199 ELECTRIFIED BLOCK', '3.25-199 ELECTRIFIED BLOCK', '3.25-199 ELECTRIFIED BLOCK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1678, h.id, '3.25-200 STEAM TRAP', '3.25-200 STEAM TRAP', '3.25-200 STEAM TRAP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1679, h.id, '3.25-201 BLOCK', '3.25-201 BLOCK', '3.25-201 BLOCK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1680, h.id, '3.25-202 HOLDER', '3.25-202 HOLDER', '3.25-202 HOLDER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1681, h.id, '3.25-203 BLASTING', '3.25-203 BLASTING', '3.25-203 BLASTING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1682, h.id, '3.25-204 DISC', '3.25-204 DISC', '3.25-204 DISC', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1683, h.id, '3.25-205 TERMINAL', '3.25-205 TERMINAL', '3.25-205 TERMINAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1684, h.id, '3.25-206 THERMOCOUPLE', '3.25-206 THERMOCOUPLE', '3.25-206 THERMOCOUPLE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1685, h.id, '3.25-207 FIRE ALARM', '3.25-207 FIRE ALARM', '3.25-207 FIRE ALARM', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1686, h.id, '3.25-208 MOUNTING', '3.25-208 MOUNTING', '3.25-208 MOUNTING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1687, h.id, '3.25-209 ELECTRODE CNC', '3.25-209 ELECTRODE CNC', '3.25-209 ELECTRODE CNC', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1688, h.id, '3.25-210 SHACKLE / SEGEL', '3.25-210 SHACKLE / SEGEL', '3.25-210 SHACKLE / SEGEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1689, h.id, '3.25-211 INDENTER', '3.25-211 INDENTER', '3.25-211 INDENTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1690, h.id, '3.25-212 RAIL', '3.25-212 RAIL', '3.25-212 RAIL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1691, h.id, '3.25-213 INJECTOR', '3.25-213 INJECTOR', '3.25-213 INJECTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1692, h.id, '3.25-214 DASHER', '3.25-214 DASHER', '3.25-214 DASHER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1693, h.id, '3.25-215 FLOAT / PELAMPUNG', '3.25-215 FLOAT / PELAMPUNG', '3.25-215 FLOAT / PELAMPUNG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1694, h.id, '3.25-216 TRIGGER', '3.25-216 TRIGGER', '3.25-216 TRIGGER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1695, h.id, '3.25-217 ARRESTER / ANVIL', '3.25-217 ARRESTER / ANVIL', '3.25-217 ARRESTER / ANVIL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1696, h.id, '3.25-218 END TROLLEY/MIDDLE TROLEY/CABLE TROLLEY', '3.25-218 END TROLLEY/MIDDLE TROLEY/CABLE TROLLEY', '3.25-218 END TROLLEY/MIDDLE TROLEY/CABLE TROLLEY', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1697, h.id, '3.25-219 THERMOSTAT', '3.25-219 THERMOSTAT', '3.25-219 THERMOSTAT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1698, h.id, '3.25-220 GUIDE', '3.25-220 GUIDE', '3.25-220 GUIDE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1699, h.id, '3.25-221 BUMPER', '3.25-221 BUMPER', '3.25-221 BUMPER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1700, h.id, '3.25-222 BARE', '3.25-222 BARE', '3.25-222 BARE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1701, h.id, '3.25-223 PLUNGER', '3.25-223 PLUNGER', '3.25-223 PLUNGER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1702, h.id, '3.25-224 MOUTH', '3.25-224 MOUTH', '3.25-224 MOUTH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1703, h.id, '3.25-225 DAMPER', '3.25-225 DAMPER', '3.25-225 DAMPER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1704, h.id, '3.25-226 CARBON BRUSH', '3.25-226 CARBON BRUSH', '3.25-226 CARBON BRUSH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1705, h.id, '3.25-227 COMPRESSOR', '3.25-227 COMPRESSOR', '3.25-227 COMPRESSOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1706, h.id, '3.25-228 STATOR', '3.25-228 STATOR', '3.25-228 STATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1707, h.id, '3.25-229 CLUTCH', '3.25-229 CLUTCH', '3.25-229 CLUTCH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1708, h.id, '3.25-999 Other Part', '3.25-999 Other Part', '3.25-999 Other Part', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1709, h.id, '3.25. Sparepart', '3.25. Sparepart', '3.25. Sparepart', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1710, h.id, '3.26-001 BESI ULIR / BESI BETON', '3.26-001 BESI ULIR / BESI BETON', '3.26-001 BESI ULIR / BESI BETON', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1711, h.id, '3.26-003 PIPA HIDROLIK', '3.26-003 PIPA HIDROLIK', '3.26-003 PIPA HIDROLIK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1712, h.id, '3.26-004 PIPA STAINLESS STEEL', '3.26-004 PIPA STAINLESS STEEL', '3.26-004 PIPA STAINLESS STEEL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1713, h.id, '3.26-005 AS BAR', '3.26-005 AS BAR', '3.26-005 AS BAR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1714, h.id, '3.26-006 BESI CNP', '3.26-006 BESI CNP', '3.26-006 BESI CNP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1715, h.id, '3.26-007 BESI SIKU', '3.26-007 BESI SIKU', '3.26-007 BESI SIKU', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1716, h.id, '3.26-008 BESI UNP', '3.26-008 BESI UNP', '3.26-008 BESI UNP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1717, h.id, '3.26-009 PLAT', '3.26-009 PLAT', '3.26-009 PLAT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1718, h.id, '3.26-010 PIPA HOLLOW / HITAM', '3.26-010 PIPA HOLLOW / HITAM', '3.26-010 PIPA HOLLOW / HITAM', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1719, h.id, '3.26-011 TIE ROD STANG / BORING', '3.26-011 TIE ROD STANG / BORING', '3.26-011 TIE ROD STANG / BORING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1720, h.id, '3.26-012 PIPA STEAM SEAMLESS', '3.26-012 PIPA STEAM SEAMLESS', '3.26-012 PIPA STEAM SEAMLESS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1721, h.id, '3.26-013 BESI SPIE/SPI', '3.26-013 BESI SPIE/SPI', '3.26-013 BESI SPIE/SPI', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1722, h.id, '3.26-014 PIPA CARBON', '3.26-014 PIPA CARBON', '3.26-014 PIPA CARBON', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1723, h.id, '3.26-015 PIPA GALVANIS', '3.26-015 PIPA GALVANIS', '3.26-015 PIPA GALVANIS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1724, h.id, '3.26-016 BESI IWF', '3.26-016 BESI IWF', '3.26-016 BESI IWF', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1725, h.id, '3.26-017 PIPA ALUMINIUM', '3.26-017 PIPA ALUMINIUM', '3.26-017 PIPA ALUMINIUM', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1726, h.id, '3.26-018 PIPA FASTENER (NOT USE)', '3.26-018 PIPA FASTENER (NOT USE)', '3.26-018 PIPA FASTENER (NOT USE)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1727, h.id, '3.26-019 BESI HNP', '3.26-019 BESI HNP', '3.26-019 BESI HNP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1728, h.id, '3.26-020 BESI INP', '3.26-020 BESI INP', '3.26-020 BESI INP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1729, h.id, '3.26-022 RAIL', '3.26-022 RAIL', '3.26-022 RAIL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1730, h.id, '3.26-023 BESI / PIPA RHS', '3.26-023 BESI / PIPA RHS', '3.26-023 BESI / PIPA RHS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1731, h.id, '3.26-024 PLAT EXPANDED METAL', '3.26-024 PLAT EXPANDED METAL', '3.26-024 PLAT EXPANDED METAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1732, h.id, '3.26-025 BESI WF', '3.26-025 BESI WF', '3.26-025 BESI WF', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1733, h.id, '3.26-027 WIRE ROD', '3.26-027 WIRE ROD', '3.26-027 WIRE ROD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1734, h.id, '3.27-001 CLEANING TOOL', '3.27-001 CLEANING TOOL', '3.27-001 CLEANING TOOL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1735, h.id, '3.27-002 CLEANER LEQUID', '3.27-002 CLEANER LEQUID', '3.27-002 CLEANER LEQUID', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1736, h.id, '3.27-003 GENERAL', '3.27-003 GENERAL', '3.27-003 GENERAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1737, h.id, '3.27-004 TISSUE', '3.27-004 TISSUE', '3.27-004 TISSUE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1738, h.id, '3.27-005 KITCHEN UTENSILS', '3.27-005 KITCHEN UTENSILS', '3.27-005 KITCHEN UTENSILS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1739, h.id, '3.27-006 GARDEN EQUIPMENT', '3.27-006 GARDEN EQUIPMENT', '3.27-006 GARDEN EQUIPMENT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1740, h.id, '3.27-007 KAIN / KARPET / TIRAI / GORDEN', '3.27-007 KAIN / KARPET / TIRAI / GORDEN', '3.27-007 KAIN / KARPET / TIRAI / GORDEN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1741, h.id, '3.27-008 GANTUNGAN', '3.27-008 GANTUNGAN', '3.27-008 GANTUNGAN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1742, h.id, '3.27-009 KERANJANG / TONG', '3.27-009 KERANJANG / TONG', '3.27-009 KERANJANG / TONG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1743, h.id, '3.28-001 ELECTRICAL', '3.28-001 ELECTRICAL', '3.28-001 ELECTRICAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1744, h.id, '3.28-002 CUTTER', '3.28-002 CUTTER', '3.28-002 CUTTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1745, h.id, '3.28-003 CARBIDE', '3.28-003 CARBIDE', '3.28-003 CARBIDE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1746, h.id, '3.28-004 GRINDER (GERINDA)', '3.28-004 GRINDER (GERINDA)', '3.28-004 GRINDER (GERINDA)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1747, h.id, '3.28-005 PUMP', '3.28-005 PUMP', '3.28-005 PUMP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1748, h.id, '3.28-006 SCRAP', '3.28-006 SCRAP', '3.28-006 SCRAP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1749, h.id, '3.28-007 PUNCH / MARTIL', '3.28-007 PUNCH / MARTIL', '3.28-007 PUNCH / MARTIL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1750, h.id, '3.28-008 BLANK ROLL', '3.28-008 BLANK ROLL', '3.28-008 BLANK ROLL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1751, h.id, '3.28-009 TANG', '3.28-009 TANG', '3.28-009 TANG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1752, h.id, '3.28-010 MESIN DRILL', '3.28-010 MESIN DRILL', '3.28-010 MESIN DRILL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1753, h.id, '3.28-011 MACHINE POLISHER', '3.28-011 MACHINE POLISHER', '3.28-011 MACHINE POLISHER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1754, h.id, '3.28-012 HOLDER / BAIS', '3.28-012 HOLDER / BAIS', '3.28-012 HOLDER / BAIS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1755, h.id, '3.28-013 GUN', '3.28-013 GUN', '3.28-013 GUN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1756, h.id, '3.28-014 COLLET', '3.28-014 COLLET', '3.28-014 COLLET', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1757, h.id, '3.28-015 KUNCI / WRENCH', '3.28-015 KUNCI / WRENCH', '3.28-015 KUNCI / WRENCH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1758, h.id, '3.28-016 PIN & BUSH', '3.28-016 PIN & BUSH', '3.28-016 PIN & BUSH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1759, h.id, '3.28-017 CALIPER', '3.28-017 CALIPER', '3.28-017 CALIPER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1760, h.id, '3.28-018 PROTECTOR', '3.28-018 PROTECTOR', '3.28-018 PROTECTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1761, h.id, '3.28-019 ALLEN KEY / KUNCI L', '3.28-019 ALLEN KEY / KUNCI L', '3.28-019 ALLEN KEY / KUNCI L', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1762, h.id, '3.28-020 DETECTOR', '3.28-020 DETECTOR', '3.28-020 DETECTOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1763, h.id, '3.28-021 THREAD', '3.28-021 THREAD', '3.28-021 THREAD', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1764, h.id, '3.28-022 FLASHBACK', '3.28-022 FLASHBACK', '3.28-022 FLASHBACK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1765, h.id, '3.28-023 CALIBRATOR', '3.28-023 CALIBRATOR', '3.28-023 CALIBRATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1766, h.id, '3.28-024 LIFT / CRANE', '3.28-024 LIFT / CRANE', '3.28-024 LIFT / CRANE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1767, h.id, '3.28-025 FASTENER', '3.28-025 FASTENER', '3.28-025 FASTENER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1768, h.id, '3.28-027 OBENG', '3.28-027 OBENG', '3.28-027 OBENG', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1769, h.id, '3.28-030 ALAT UKUR / MEASURING INSTRUMENT', '3.28-030 ALAT UKUR / MEASURING INSTRUMENT', '3.28-030 ALAT UKUR / MEASURING INSTRUMENT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1770, h.id, '3.28-031 TORCH', '3.28-031 TORCH', '3.28-031 TORCH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1771, h.id, '3.28-032 GANCU', '3.28-032 GANCU', '3.28-032 GANCU', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1772, h.id, '3.28-034 INSERT & MOULD, PATTERN', '3.28-034 INSERT & MOULD, PATTERN', '3.28-034 INSERT & MOULD, PATTERN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1773, h.id, '3.28-035 ROD DRYER', '3.28-035 ROD DRYER', '3.28-035 ROD DRYER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1774, h.id, '3.28-036 SNAI / DIES', '3.28-036 SNAI / DIES', '3.28-036 SNAI / DIES', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1775, h.id, '3.28-037 PAINT BRUSH / KUAS', '3.28-037 PAINT BRUSH / KUAS', '3.28-037 PAINT BRUSH / KUAS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1776, h.id, '3.28-038 END MILL / BALL NOSE', '3.28-038 END MILL / BALL NOSE', '3.28-038 END MILL / BALL NOSE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1777, h.id, '3.28-040 BLOWER', '3.28-040 BLOWER', '3.28-040 BLOWER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1778, h.id, '3.28-041 GEAR HOB CUTTER', '3.28-041 GEAR HOB CUTTER', '3.28-041 GEAR HOB CUTTER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1779, h.id, '3.28-042 MESIN / TRAFO LAS', '3.28-042 MESIN / TRAFO LAS', '3.28-042 MESIN / TRAFO LAS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1780, h.id, '3.28-043 VENTILATOR', '3.28-043 VENTILATOR', '3.28-043 VENTILATOR', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1781, h.id, '3.28-044 SENDOK SEMEN / SEKOP', '3.28-044 SENDOK SEMEN / SEKOP', '3.28-044 SENDOK SEMEN / SEKOP', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1782, h.id, '3.28-045 LABORATORY EQUIPMENT', '3.28-045 LABORATORY EQUIPMENT', '3.28-045 LABORATORY EQUIPMENT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1783, h.id, '3.28-046 SOLDER', '3.28-046 SOLDER', '3.28-046 SOLDER', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1784, h.id, '3.28-047 TROLLY / TOOL CART', '3.28-047 TROLLY / TOOL CART', '3.28-047 TROLLY / TOOL CART', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1785, h.id, '3.28-048 FLARING', '3.28-048 FLARING', '3.28-048 FLARING', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1786, h.id, '3.28-049 TROUGH', '3.28-049 TROUGH', '3.28-049 TROUGH', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1787, h.id, '3.28-999 GENERAL', '3.28-999 GENERAL', '3.28-999 GENERAL', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1788, h.id, '3.28. Tools', '3.28. Tools', '3.28. Tools', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1789, h.id, '3.30-001 BROTI', '3.30-001 BROTI', '3.30-001 BROTI', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1790, h.id, '3.30-002 PAPAN', '3.30-002 PAPAN', '3.30-002 PAPAN', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1791, h.id, '3.30-003 TRIPLEK', '3.30-003 TRIPLEK', '3.30-003 TRIPLEK', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1792, h.id, '3.30-004 KAYU LAT', '3.30-004 KAYU LAT', '3.30-004 KAYU LAT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1793, h.id, '3.31-001 Bags', '3.31-001 Bags', '3.31-001 Bags', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1794, h.id, '3.31-002 Pallet', '3.31-002 Pallet', '3.31-002 Pallet', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1795, h.id, '3.31-009 Other Packaging Material', '3.31-009 Other Packaging Material', '3.31-009 Other Packaging Material', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1796, h.id, '3.33-001 Tester', '3.33-001 Tester', '3.33-001 Tester', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1797, h.id, '3.33-002 Recorder', '3.33-002 Recorder', '3.33-002 Recorder', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1798, h.id, '3.33-003 Measurer', '3.33-003 Measurer', '3.33-003 Measurer', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1799, h.id, '3.34-001 Marketing', '3.34-001 Marketing', '3.34-001 Marketing', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1800, h.id, '3.35-001 Labour Uniform', '3.35-001 Labour Uniform', '3.35-001 Labour Uniform', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1801, h.id, '3.35-002 Staff Uniform', '3.35-002 Staff Uniform', '3.35-002 Staff Uniform', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1802, h.id, '3.35-003 Laboratory Uniform', '3.35-003 Laboratory Uniform', '3.35-003 Laboratory Uniform', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1803, h.id, '3.35-004 Security Uniform', '3.35-004 Security Uniform', '3.35-004 Security Uniform', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1804, h.id, '4.01-001 Machine', '4.01-001 Machine', '4.01-001 Machine', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1805, h.id, '4.01. Machine', '4.01. Machine', '4.01. Machine', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1806, h.id, '4.02-001 CRANE', '4.02-001 CRANE', '4.02-001 CRANE', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1807, h.id, '4.02-002 INSTRUMENT', '4.02-002 INSTRUMENT', '4.02-002 INSTRUMENT', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1808, h.id, '4.02-003 PRODUCTION TOOLS', '4.02-003 PRODUCTION TOOLS', '4.02-003 PRODUCTION TOOLS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1809, h.id, '4.02. Tools', '4.02. Tools', '4.02. Tools', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1810, h.id, '4.03-001 Froklift', '4.03-001 Froklift', '4.03-001 Froklift', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1811, h.id, '4.03-002 Truck', '4.03-002 Truck', '4.03-002 Truck', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1812, h.id, '4.03-003 MPV', '4.03-003 MPV', '4.03-003 MPV', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1813, h.id, '4.03-004 Loader', '4.03-004 Loader', '4.03-004 Loader', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1814, h.id, '4.03-005 Becak', '4.03-005 Becak', '4.03-005 Becak', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1815, h.id, '5.01-001 Composite Casting GAI', '5.01-001 Composite Casting GAI', '5.01-001 Composite Casting GAI', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1816, h.id, '5.01-002 Composite Casting GAS', '5.01-002 Composite Casting GAS', '5.01-002 Composite Casting GAS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1817, h.id, '5.01-003 Composite Casting Subcon', '5.01-003 Composite Casting Subcon', '5.01-003 Composite Casting Subcon', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1818, h.id, '5.01. Composite Casting', '5.01. Composite Casting', '5.01. Composite Casting', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1819, h.id, '5.02-001 Ceramic Rubber Tile', '5.02-001 Ceramic Rubber Tile', '5.02-001 Ceramic Rubber Tile', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1820, h.id, '5.03. Wearplate', '5.03. Wearplate', '5.03. Wearplate', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1821, h.id, '7.01-001 FG Foundry GAI', '7.01-001 FG Foundry GAI', '7.01-001 FG Foundry GAI', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1822, h.id, '7.01-002 FG Foundry GAS', '7.01-002 FG Foundry GAS', '7.01-002 FG Foundry GAS', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1823, h.id, '7.01-003 FG Foundry Others', '7.01-003 FG Foundry Others', '7.01-003 FG Foundry Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1824, h.id, '7.01-009 FG Foundry Set', '7.01-009 FG Foundry Set', '7.01-009 FG Foundry Set', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1825, h.id, '7.01. FG Foundry', '7.01. FG Foundry', '7.01. FG Foundry', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1826, h.id, '7.02-002 FG Fastener Nut', '7.02-002 FG Fastener Nut', '7.02-002 FG Fastener Nut', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1827, h.id, '7.02-003 FG Fastener Washer', '7.02-003 FG Fastener Washer', '7.02-003 FG Fastener Washer', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1828, h.id, '7.02-004 FG Fastener Set', '7.02-004 FG Fastener Set', '7.02-004 FG Fastener Set', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1829, h.id, '7.02-005 FG Fastener Others', '7.02-005 FG Fastener Others', '7.02-005 FG Fastener Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1830, h.id, '7.02-101 FG Fastener Bolt - Oval Head Tapper', '7.02-101 FG Fastener Bolt - Oval Head Tapper', '7.02-101 FG Fastener Bolt - Oval Head Tapper', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1831, h.id, '7.02-102 FG Fastener Bolt - Gash', '7.02-102 FG Fastener Bolt - Gash', '7.02-102 FG Fastener Bolt - Gash', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1832, h.id, '7.02-103 FG Fastener Bolt - Hexagon', '7.02-103 FG Fastener Bolt - Hexagon', '7.02-103 FG Fastener Bolt - Hexagon', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1833, h.id, '7.02-104 FG Fastener Bolt - Spherical', '7.02-104 FG Fastener Bolt - Spherical', '7.02-104 FG Fastener Bolt - Spherical', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1834, h.id, '7.02-105 FG Fastener Bolt - T-Bolt', '7.02-105 FG Fastener Bolt - T-Bolt', '7.02-105 FG Fastener Bolt - T-Bolt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1835, h.id, '7.02-106 FG Fastener Bolt - Stud', '7.02-106 FG Fastener Bolt - Stud', '7.02-106 FG Fastener Bolt - Stud', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1836, h.id, '7.02-107 FG Fastener Bolt - Eye', '7.02-107 FG Fastener Bolt - Eye', '7.02-107 FG Fastener Bolt - Eye', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1837, h.id, '7.02-108 FG Fastener Bolt - Socket Head', '7.02-108 FG Fastener Bolt - Socket Head', '7.02-108 FG Fastener Bolt - Socket Head', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1838, h.id, '7.02-109 FG Fastener Bolt - Square Head', '7.02-109 FG Fastener Bolt - Square Head', '7.02-109 FG Fastener Bolt - Square Head', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1839, h.id, '7.02-110 FG Fastener Bolt - Elevator', '7.02-110 FG Fastener Bolt - Elevator', '7.02-110 FG Fastener Bolt - Elevator', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1840, h.id, '7.02-111 FG Fastener Bolt - Round Head Square Neck', '7.02-111 FG Fastener Bolt - Round Head Square Neck', '7.02-111 FG Fastener Bolt - Round Head Square Neck', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1841, h.id, '7.02-112 FG Fastener Bolt - Countersunk', '7.02-112 FG Fastener Bolt - Countersunk', '7.02-112 FG Fastener Bolt - Countersunk', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1842, h.id, '7.02. FG Fastener', '7.02. FG Fastener', '7.02. FG Fastener', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1843, h.id, '7.03-001 FG Rubber - Liner', '7.03-001 FG Rubber - Liner', '7.03-001 FG Rubber - Liner', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1844, h.id, '7.03-004 FG Rubber - Joint Strip', '7.03-004 FG Rubber - Joint Strip', '7.03-004 FG Rubber - Joint Strip', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1845, h.id, '7.03-005 FG Rubber - Seal', '7.03-005 FG Rubber - Seal', '7.03-005 FG Rubber - Seal', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1846, h.id, '7.03-006 FG Rubber - Set', '7.03-006 FG Rubber - Set', '7.03-006 FG Rubber - Set', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1847, h.id, '7.03-007 FG Rubber - Plug', '7.03-007 FG Rubber - Plug', '7.03-007 FG Rubber - Plug', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1848, h.id, '7.03-008 FG Rubber - Metal', '7.03-008 FG Rubber - Metal', '7.03-008 FG Rubber - Metal', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1849, h.id, '7.03. FG Rubber', '7.03. FG Rubber', '7.03. FG Rubber', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1850, h.id, '7.04-001 FG GM - Ball', '7.04-001 FG GM - Ball', '7.04-001 FG GM - Ball', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1851, h.id, '7.04-002 FG GM - Rod', '7.04-002 FG GM - Rod', '7.04-002 FG GM - Rod', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1852, h.id, '7.04. FG Grinding Media', '7.04. FG Grinding Media', '7.04. FG Grinding Media', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1853, h.id, '8.01. IT Service', '8.01. IT Service', '8.01. IT Service', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1854, h.id, '8.02. Import Cost', '8.02. Import Cost', '8.02. Import Cost', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1855, h.id, '8.03. Export Cost', '8.03. Export Cost', '8.03. Export Cost', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1856, h.id, '8.04. Maintenance', '8.04. Maintenance', '8.04. Maintenance', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1857, h.id, '8.05. Rental', '8.05. Rental', '8.05. Rental', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1858, h.id, '8.06. Expedition', '8.06. Expedition', '8.06. Expedition', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1859, h.id, '8.07. Outsourcing', '8.07. Outsourcing', '8.07. Outsourcing', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1860, h.id, '8.08. Waste Treatment', '8.08. Waste Treatment', '8.08. Waste Treatment', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1861, h.id, '8.09. Professional and Engineer', '8.09. Professional and Engineer', '8.09. Professional and Engineer', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1862, h.id, '8.10. Marketing', '8.10. Marketing', '8.10. Marketing', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1863, h.id, '8.11. Seminar and Training', '8.11. Seminar and Training', '8.11. Seminar and Training', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1864, h.id, '8.12. Project', '8.12. Project', '8.12. Project', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1865, h.id, '8.13. Prepayment', '8.13. Prepayment', '8.13. Prepayment', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1866, h.id, '8.15. Techical Engineering', '8.15. Techical Engineering', '8.15. Techical Engineering', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1867, h.id, '8.16. Sembako', '8.16. Sembako', '8.16. Sembako', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1868, h.id, '8.99. Other Service', '8.99. Other Service', '8.99. Other Service', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2300, h.id, '1.03-001.01 RM Rubber - Polymer - Natural Rubber', '1.03-001.01 RM Rubber - Polymer - Natural Rubber', '1.03-001.01 RM Rubber - Polymer - Natural Rubber', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2301, h.id, '2.01-001.05 Additive - Alloy - Ferro Manganese', '2.01-001.05 Additive - Alloy - Ferro Manganese', '2.01-001.05 Additive - Alloy - Ferro Manganese', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2302, h.id, '1.03-001.02 RM Rubber - Polymer - Synthetic Rubber', '1.03-001.02 RM Rubber - Polymer - Synthetic Rubber', '1.03-001.02 RM Rubber - Polymer - Synthetic Rubber', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2306, h.id, '7.02-201 FG Fastener Nut - Nylon Lock', '7.02-201 FG Fastener Nut - Nylon Lock', '7.02-201 FG Fastener Nut - Nylon Lock', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2307, h.id, '7.02-202 FG Fastener Nut - Hexagon', '7.02-202 FG Fastener Nut - Hexagon', '7.02-202 FG Fastener Nut - Hexagon', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2308, h.id, '7.02-203 FG Fastener Nut - Core Lock', '7.02-203 FG Fastener Nut - Core Lock', '7.02-203 FG Fastener Nut - Core Lock', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1869, h.id, '1.01.21.FE Throat Liner (SAG/AG Mill)', '1.01.21.FE Throat Liner (SAG/AG Mill)', '1.01.21.FE Throat Liner (SAG/AG Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1870, h.id, '1.01.22.FE Inner Liner (SAG/AG Mill)', '1.01.22.FE Inner Liner (SAG/AG Mill)', '1.01.22.FE Inner Liner (SAG/AG Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1871, h.id, '1.01.23.FE Middle Liner (SAG/AG Mill)', '1.01.23.FE Middle Liner (SAG/AG Mill)', '1.01.23.FE Middle Liner (SAG/AG Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1872, h.id, '1.01.24.FE Outer Liner (SAG/AG Mill)', '1.01.24.FE Outer Liner (SAG/AG Mill)', '1.01.24.FE Outer Liner (SAG/AG Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1873, h.id, '1.01.25.Top Hat Shell Liner (Single Chord)', '1.01.25.Top Hat Shell Liner (Single Chord)', '1.01.25.Top Hat Shell Liner (Single Chord)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1874, h.id, '1.01.26.Top Hat Shell Liner (Double Chord)', '1.01.26.Top Hat Shell Liner (Double Chord)', '1.01.26.Top Hat Shell Liner (Double Chord)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1875, h.id, '1.01.27.Lifter Bar', '1.01.27.Lifter Bar', '1.01.27.Lifter Bar', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1876, h.id, '1.01.28.Shell Plate', '1.01.28.Shell Plate', '1.01.28.Shell Plate', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1877, h.id, '1.01.29.Outer Pulp Lifter', '1.01.29.Outer Pulp Lifter', '1.01.29.Outer Pulp Lifter', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1878, h.id, '1.01.30.Middle Pulp Lifter', '1.01.30.Middle Pulp Lifter', '1.01.30.Middle Pulp Lifter', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1879, h.id, '1.01.31.Inner Pulp Lifter', '1.01.31.Inner Pulp Lifter', '1.01.31.Inner Pulp Lifter', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1880, h.id, '1.01.32.Chamber Pulp Lifter', '1.01.32.Chamber Pulp Lifter', '1.01.32.Chamber Pulp Lifter', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1881, h.id, '1.01.33.Grate', '1.01.33.Grate', '1.01.33.Grate', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1882, h.id, '1.01.34.DE Middle Liner', '1.01.34.DE Middle Liner', '1.01.34.DE Middle Liner', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1883, h.id, '1.01.35.Pulp Discharger', '1.01.35.Pulp Discharger', '1.01.35.Pulp Discharger', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1884, h.id, '1.01.36.Filler Ring', '1.01.36.Filler Ring', '1.01.36.Filler Ring', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1885, h.id, '1.01.37.Clamping Ring', '1.01.37.Clamping Ring', '1.01.37.Clamping Ring', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1886, h.id, '1.01.38.Insert (White Iron)', '1.01.38.Insert (White Iron)', '1.01.38.Insert (White Iron)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1887, h.id, '1.01.39.FE/DE Inner Liner (Ball/Rod Mill)', '1.01.39.FE/DE Inner Liner (Ball/Rod Mill)', '1.01.39.FE/DE Inner Liner (Ball/Rod Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1888, h.id, '1.01.40.FE/DE Middle Liner (Ball/Rod Mill)', '1.01.40.FE/DE Middle Liner (Ball/Rod Mill)', '1.01.40.FE/DE Middle Liner (Ball/Rod Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1889, h.id, '1.01.41.FE/DE Outer Liner (Ball/Rod Mill)', '1.01.41.FE/DE Outer Liner (Ball/Rod Mill)', '1.01.41.FE/DE Outer Liner (Ball/Rod Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1890, h.id, '1.01.42.Shell Liner Single Chord (Ball/Rod Mill)', '1.01.42.Shell Liner Single Chord (Ball/Rod Mill)', '1.01.42.Shell Liner Single Chord (Ball/Rod Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1891, h.id, '1.01.43.Shell Liner Double Chord (Ball/Rod Mill)', '1.01.43.Shell Liner Double Chord (Ball/Rod Mill)', '1.01.43.Shell Liner Double Chord (Ball/Rod Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1892, h.id, '1.01.44.Shell Liner Triple Chord (Ball/Rod Mill)', '1.01.44.Shell Liner Triple Chord (Ball/Rod Mill)', '1.01.44.Shell Liner Triple Chord (Ball/Rod Mill)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1893, h.id, '1.01.99.Others', '1.01.99.Others', '1.01.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1894, h.id, '1.01.Mining Steel Liner', '1.01.Mining Steel Liner', '1.01.Mining Steel Liner', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1895, h.id, '1.02.01.Bowl/ Mantle/ Concave', '1.02.01.Bowl/ Mantle/ Concave', '1.02.01.Bowl/ Mantle/ Concave', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1896, h.id, '1.02.02.Jaw', '1.02.02.Jaw', '1.02.02.Jaw', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1897, h.id, '1.02.03.Cheek plate', '1.02.03.Cheek plate', '1.02.03.Cheek plate', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1898, h.id, '1.02.04.Segmented concave', '1.02.04.Segmented concave', '1.02.04.Segmented concave', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1899, h.id, '1.02.99.Others', '1.02.99.Others', '1.02.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1900, h.id, '1.02.Mining Crusher', '1.02.Mining Crusher', '1.02.Mining Crusher', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1901, h.id, '1.03.01.Bolt', '1.03.01.Bolt', '1.03.01.Bolt', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1902, h.id, '1.03.02.Washer', '1.03.02.Washer', '1.03.02.Washer', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1903, h.id, '1.03.03.Nut', '1.03.03.Nut', '1.03.03.Nut', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1904, h.id, '1.03.99.Others', '1.03.99.Others', '1.03.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1905, h.id, '1.03.Mining Fastener', '1.03.Mining Fastener', '1.03.Mining Fastener', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1906, h.id, '1.04.01.Joint strip/ rubber wedge', '1.04.01.Joint strip/ rubber wedge', '1.04.01.Joint strip/ rubber wedge', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1907, h.id, '1.04.02.Seal', '1.04.02.Seal', '1.04.02.Seal', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1908, h.id, '1.04.03.Rubber Plug', '1.04.03.Rubber Plug', '1.04.03.Rubber Plug', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1909, h.id, '1.04.04.Rubber Bushing', '1.04.04.Rubber Bushing', '1.04.04.Rubber Bushing', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1910, h.id, '1.04.05.Fastener Plug', '1.04.05.Fastener Plug', '1.04.05.Fastener Plug', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1911, h.id, '1.04.06.Screening Panel', '1.04.06.Screening Panel', '1.04.06.Screening Panel', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1912, h.id, '1.04.07.Wear Pad', '1.04.07.Wear Pad', '1.04.07.Wear Pad', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1913, h.id, '1.04.09.ISA Mill Grinding Disc', '1.04.09.ISA Mill Grinding Disc', '1.04.09.ISA Mill Grinding Disc', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1914, h.id, '1.04.11.ISA Mill Others', '1.04.11.ISA Mill Others', '1.04.11.ISA Mill Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1915, h.id, '1.04.12.Rubber Backing Sheet', '1.04.12.Rubber Backing Sheet', '1.04.12.Rubber Backing Sheet', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1916, h.id, '1.04.99.Others', '1.04.99.Others', '1.04.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1917, h.id, '1.04.Mining Rubber Components', '1.04.Mining Rubber Components', '1.04.Mining Rubber Components', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1918, h.id, '1.05.01.Chute liner plate', '1.05.01.Chute liner plate', '1.05.01.Chute liner plate', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1919, h.id, '1.05.02.Trunnion Liner/ Bearing Housing', '1.05.02.Trunnion Liner/ Bearing Housing', '1.05.02.Trunnion Liner/ Bearing Housing', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1920, h.id, '1.05.03.Feed Spout/ Elbow/ Pipe Bend', '1.05.03.Feed Spout/ Elbow/ Pipe Bend', '1.05.03.Feed Spout/ Elbow/ Pipe Bend', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1921, h.id, '1.05.04.Verti Liner', '1.05.04.Verti Liner', '1.05.04.Verti Liner', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1922, h.id, '1.05.05.Apron Feeder', '1.05.05.Apron Feeder', '1.05.05.Apron Feeder', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1923, h.id, '1.05.99.Others', '1.05.99.Others', '1.05.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1924, h.id, '1.05.Mining Other Components', '1.05.Mining Other Components', '1.05.Mining Other Components', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1925, h.id, '1.06.01.Head Plate (Full Rubber)', '1.06.01.Head Plate (Full Rubber)', '1.06.01.Head Plate (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1926, h.id, '1.06.02.Head Lifter Bar (Full Rubber)', '1.06.02.Head Lifter Bar (Full Rubber)', '1.06.02.Head Lifter Bar (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1927, h.id, '1.06.03.Head Optiliner (Full Rubber)', '1.06.03.Head Optiliner (Full Rubber)', '1.06.03.Head Optiliner (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1928, h.id, '1.06.04.Shell Plate (Full Rubber)', '1.06.04.Shell Plate (Full Rubber)', '1.06.04.Shell Plate (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1929, h.id, '1.06.05.Shell Lifter Bar (Full Rubber)', '1.06.05.Shell Lifter Bar (Full Rubber)', '1.06.05.Shell Lifter Bar (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1930, h.id, '1.06.06.Shell Optiliner (Full Rubber)', '1.06.06.Shell Optiliner (Full Rubber)', '1.06.06.Shell Optiliner (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1931, h.id, '1.06.07.Filler Block (Full Rubber)', '1.06.07.Filler Block (Full Rubber)', '1.06.07.Filler Block (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1932, h.id, '1.06.08.Filler Ring (Full Rubber)', '1.06.08.Filler Ring (Full Rubber)', '1.06.08.Filler Ring (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1933, h.id, '1.06.09.Filler Insert (Full Rubber)', '1.06.09.Filler Insert (Full Rubber)', '1.06.09.Filler Insert (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1934, h.id, '1.06.10.Grate (Full Rubber)', '1.06.10.Grate (Full Rubber)', '1.06.10.Grate (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1935, h.id, '1.06.11.Trunnion Liner (Full Rubber)', '1.06.11.Trunnion Liner (Full Rubber)', '1.06.11.Trunnion Liner (Full Rubber)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1936, h.id, '1.06.21.Head Plate (Composite)', '1.06.21.Head Plate (Composite)', '1.06.21.Head Plate (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1937, h.id, '1.06.22.Head Lifter Bar (Composite)', '1.06.22.Head Lifter Bar (Composite)', '1.06.22.Head Lifter Bar (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1938, h.id, '1.06.23.Head Optiliner (Composite)', '1.06.23.Head Optiliner (Composite)', '1.06.23.Head Optiliner (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1939, h.id, '1.06.24.Shell Plate (Composite)', '1.06.24.Shell Plate (Composite)', '1.06.24.Shell Plate (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1940, h.id, '1.06.25.Shell Lifter Bar (Composite)', '1.06.25.Shell Lifter Bar (Composite)', '1.06.25.Shell Lifter Bar (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1941, h.id, '1.06.26.Shell Optiliner (Composite)', '1.06.26.Shell Optiliner (Composite)', '1.06.26.Shell Optiliner (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1942, h.id, '1.06.27.Filler Block (Composite)', '1.06.27.Filler Block (Composite)', '1.06.27.Filler Block (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1943, h.id, '1.06.28.Filler Ring (Composite)', '1.06.28.Filler Ring (Composite)', '1.06.28.Filler Ring (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1944, h.id, '1.06.29.Grate (Composite)', '1.06.29.Grate (Composite)', '1.06.29.Grate (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1945, h.id, '1.06.30.Trunnion Liner (Composite)', '1.06.30.Trunnion Liner (Composite)', '1.06.30.Trunnion Liner (Composite)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1946, h.id, '1.06.41.Moulded Pulp System', '1.06.41.Moulded Pulp System', '1.06.41.Moulded Pulp System', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1947, h.id, '1.06.42.Hand Lined Pulp System', '1.06.42.Hand Lined Pulp System', '1.06.42.Hand Lined Pulp System', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1948, h.id, '1.06.51.Modular Discharger', '1.06.51.Modular Discharger', '1.06.51.Modular Discharger', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1949, h.id, '1.06.99.Others', '1.06.99.Others', '1.06.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1950, h.id, '1.07.01.Grinding ball forged', '1.07.01.Grinding ball forged', '1.07.01.Grinding ball forged', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1951, h.id, '1.07.02.Grinding ball cast', '1.07.02.Grinding ball cast', '1.07.02.Grinding ball cast', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1952, h.id, '1.07.03.Grinding rod', '1.07.03.Grinding rod', '1.07.03.Grinding rod', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1953, h.id, '1.Mining', '1.Mining', '1.Mining', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1954, h.id, '2.01.Bowl/ Mantle/ Concave', '2.01.Bowl/ Mantle/ Concave', '2.01.Bowl/ Mantle/ Concave', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1955, h.id, '2.02.Jaw', '2.02.Jaw', '2.02.Jaw', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1956, h.id, '2.03.Cheek plate', '2.03.Cheek plate', '2.03.Cheek plate', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1957, h.id, '2.04.Segmented concave', '2.04.Segmented concave', '2.04.Segmented concave', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1958, h.id, '2.99.Others (e.g. jaw clamping)', '2.99.Others (e.g. jaw clamping)', '2.99.Others (e.g. jaw clamping)', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1959, h.id, '3.01.Casting for Foundry BU', '3.01.Casting for Foundry BU', '3.01.Casting for Foundry BU', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1960, h.id, '3.02.Casting for Rubber BU', '3.02.Casting for Rubber BU', '3.02.Casting for Rubber BU', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1961, h.id, '3.03.Casting for Grinding BU', '3.03.Casting for Grinding BU', '3.03.Casting for Grinding BU', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1962, h.id, '3.98.Service', '3.98.Service', '3.98.Service', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1963, h.id, '3.99.Others', '3.99.Others', '3.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1964, h.id, '3.Foundry', '3.Foundry', '3.Foundry', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1965, h.id, '4.01.Roll', '4.01.Roll', '4.01.Roll', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1966, h.id, '4.02.Component', '4.02.Component', '4.02.Component', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1967, h.id, '4.Crumb Rubber', '4.Crumb Rubber', '4.Crumb Rubber', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1968, h.id, '5.01.Roll', '5.01.Roll', '5.01.Roll', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1969, h.id, '5.02.Component', '5.02.Component', '5.02.Component', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1970, h.id, '5.98.Service', '5.98.Service', '5.98.Service', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1971, h.id, '6.01.Grinding plate/ table liner', '6.01.Grinding plate/ table liner', '6.01.Grinding plate/ table liner', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1972, h.id, '6.02.Grinding roll/ roll tyre', '6.02.Grinding roll/ roll tyre', '6.02.Grinding roll/ roll tyre', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1973, h.id, '6.99.Others', '6.99.Others', '6.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1974, h.id, '7.01.Liner', '7.01.Liner', '7.01.Liner', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1975, h.id, '7.02.Table liner', '7.02.Table liner', '7.02.Table liner', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1976, h.id, '7.03.Roll tyre', '7.03.Roll tyre', '7.03.Roll tyre', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1977, h.id, '7.99.Others', '7.99.Others', '7.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1978, h.id, '9.01 Down Payment', '9.01 Down Payment', '9.01 Down Payment', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1979, h.id, '9.02 Freight Charge', '9.02 Freight Charge', '9.02 Freight Charge', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 1999, h.id, '1.07.99.Others', '1.07.99.Others', '1.07.99.Others', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2000, h.id, '1.07.Grinding Ball', '1.07.Grinding Ball', '1.07.Grinding Ball', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2296, h.id, '1.06.Mining Rubber Liner', '1.06.Mining Rubber Liner', '1.06.Mining Rubber Liner', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2002, h.id, '07-SALES', '07-SALES', '07-SALES', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0022';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2004, h.id, '00_Default', '00_Default', '00_Default', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0023';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2013, h.id, 'Pattern', 'Pattern', 'Pattern', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2015, h.id, 'QC Jig', 'QC Jig', 'QC Jig', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2016, h.id, 'Insert', 'Insert', 'Insert', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2017, h.id, 'Dedicated Mould', 'Dedicated Mould', 'Dedicated Mould', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2029, h.id, 'Box Mould', 'Box Mould', 'Box Mould', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2030, h.id, 'Insert Mould', 'Insert Mould', 'Insert Mould', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2031, h.id, 'Extruder Head', 'Extruder Head', 'Extruder Head', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2032, h.id, 'Land', 'Land', 'Land', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2033, h.id, 'Factory Buildings', 'Factory Building', 'Factory Building', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2037, h.id, 'Office Buildings', 'Office Building', 'Office Building', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2039, h.id, 'Machines', 'Machine', 'Machine', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2040, h.id, 'Tools', 'Tools', 'Tools', '5', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2051, h.id, 'Equipments', 'Equipment', 'Equipment', '6', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2053, h.id, 'Office Vehicles', 'Office Vehicle', 'Office Vehicle', '7', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2055, h.id, 'Factory Vehicles', 'Factory Vehicle', 'Factory Vehicle', '8', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2038, h.id, '48', '04 Years', '04 Years', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2041, h.id, '96', '08 Years', '08 Years', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2042, h.id, '120', '10 Years', '10 Years', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2043, h.id, '192', '16 Years', '16 Years', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2044, h.id, '240', '20 Years', '20 Years', '5', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2045, h.id, 'Foundry', 'GA.BU01', 'GA.BU01', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2046, h.id, 'Fastener', 'GA.BU02', 'GA.BU02', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2047, h.id, 'Rubber Plant', 'GA.BU03', 'GA.BU03', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2048, h.id, 'Grinding Media Medan', 'GA.BU04', 'GA.BU04', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2049, h.id, 'PLTU 1', 'GA.BU05', 'GA.BU05', '5', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2050, h.id, 'PLTU 2', 'GA.BU06', 'GA.BU06', '6', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2052, h.id, 'Head Office Medan', 'GA.BU07', 'GA.BU07', '7', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2054, h.id, 'Grinding Media Pasuruan', 'GA.BU08', 'GA.BU08', '8', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2056, h.id, 'Fastener - Production', 'GAN.P01', 'GAN.P01', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2057, h.id, 'Fastener - QC', 'GAN.P02', 'GAN.P02', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2058, h.id, 'Fastener - Machining', 'GAN.P03', 'GAN.P03', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2059, h.id, 'Fastener - Dispatch', 'GAN.S01', 'GAN.S01', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2061, h.id, 'Fastener - Maintenance', 'GAN.S02', 'GAN.S02', '5', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2062, h.id, 'Foundry - Production', 'GAF.P01', 'GAF.P01', '6', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2063, h.id, 'Foundry - Pattern', 'GAF.P02', 'GAF.P02', '7', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2064, h.id, 'Foundry - Molding', 'GAF.P03', 'GAF.P03', '8', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2065, h.id, 'Foundry - Melting (Furnace)', 'GAF.P04', 'GAF.P04', '9', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2066, h.id, 'Foundry - Fettling', 'GAF.P05', 'GAF.P05', '10', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2067, h.id, 'Foundry - Heat Treatment', 'GAF.P06', 'GAF.P06', '11', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2068, h.id, 'Foundry - QC', 'GAF.P07', 'GAF.P07', '12', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2069, h.id, 'Foundry - Machining Metal', 'GAF.P09', 'GAF.P09', '13', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2070, h.id, 'Foundry - Machining Wood', 'GAF.P10', 'GAF.P10', '14', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2071, h.id, 'Foundry - Dispatch', 'GAF.S01', 'GAF.S01', '15', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2072, h.id, 'Foundry - Design & Drafting', 'GAF.S02', 'GAF.S02', '16', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2073, h.id, 'Foundry - Method', 'GAF.S03', 'GAF.S03', '17', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2074, h.id, 'Foundry - HSE', 'GAF.S04', 'GAF.S04', '18', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2075, h.id, 'Foundry - Fabrication', 'GAF.S05', 'GAF.S05', '19', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2076, h.id, 'Foundry - Maintenance', 'GAF.S06', 'GAF.S06', '20', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2077, h.id, 'Foundry - Warehouse', 'GAF.S07', 'GAF.S07', '21', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2078, h.id, 'Foundry - PPC', 'GAF.S08', 'GAF.S08', '22', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2079, h.id, 'Foundry - Technical', 'GAF.S09', 'GAF.S09', '23', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2080, h.id, 'Foundry - Expedition', 'GAF.S10', 'GAF.S10', '24', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2081, h.id, 'Foundry - Lean & Sustainability', 'GAF.S11', 'GAF.S11', '25', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2084, h.id, 'HO - Marketing', 'GAH.D01', 'GAH.D01', '26', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2085, h.id, 'HO - Purchasing', 'GAH.D02', 'GAH.D02', '27', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2086, h.id, 'HO - Export', 'GAH.D03', 'GAH.D03', '28', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2087, h.id, 'HO - HRGA', 'GAH.D04', 'GAH.D04', '29', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2088, h.id, 'HO - IT', 'GAH.D05', 'GAH.D05', '30', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2089, h.id, 'HO - Finance', 'GAH.D06', 'GAH.D06', '31', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2090, h.id, 'HO - Accounting & Tax', 'GAH.D07', 'GAH.D07', '32', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2091, h.id, 'HO - General', 'GAH.D08', 'GAH.D08', '33', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2092, h.id, 'PLTU II - Production', 'GAK.P01', 'GAK.P01', '34', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2093, h.id, 'PLTU I - Production', 'GAP.P01', 'GAP.P01', '35', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2094, h.id, 'Rubber Plant - Production', 'GAR.P01', 'GAR.P01', '36', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2095, h.id, 'Rubber Plant - Compounding', 'GAR.P02', 'GAR.P02', '37', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2096, h.id, 'Factory Building - Warehouse', 'Factory Building - Warehouse', 'Factory Building - Warehouse', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2097, h.id, 'Machine Production - Rubber Press', 'Machine Production - Rubber Press', 'Machine Production - Rubber Press', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2098, h.id, 'Machine Production - Heat Treatment', 'Machine Production - Heat Treatment', 'Machine Production - Heat Treatment', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2099, h.id, 'Machine Procuction - Rubber Mixer', 'Machine Procuction - Rubber Mixer', 'Machine Procuction - Rubber Mixer', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2100, h.id, 'Machine Production - Shotblast', 'Machine Production - Shotblast', 'Machine Production - Shotblast', '5', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2101, h.id, 'Machine Production - Calender', 'Machine Production - Calender', 'Machine Production - Calender', '6', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2102, h.id, 'Machine Production - EDM (Extruder)', 'Machine Production - EDM (Extruder)', 'Machine Production - EDM (Extruder)', '7', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2103, h.id, 'Machine Production - Autoclave', 'Machine Production - Autoclave', 'Machine Production - Autoclave', '8', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2104, h.id, 'Machine Production - Sand Mixer', 'Machine Production - Sand Mixer', 'Machine Production - Sand Mixer', '9', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2105, h.id, 'Machine Production - Induction Furnace', 'Machine Production - Induction Furnace', 'Machine Production - Induction Furnace', '10', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2106, h.id, 'Machine Production - Power Hammer', 'Machine Production - Power Hammer', 'Machine Production - Power Hammer', '11', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2272, h.id, 'Machine Production - Rubber Preformer', 'Machine Production - Rubber Preformer', 'Machine Production - Rubber Preformer', '12', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2274, h.id, 'Hydrolic Cutter', 'Hydrolic Cutter', 'Hydrolic Cutter', '13', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2275, h.id, 'Spray Booth', 'Spray Booth', 'Spray Booth', '14', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2291, h.id, 'Machine Production - Cutting Machine', 'Machine Production - Cutting Machine', 'Machine Production - Cutting Machine', '15', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2292, h.id, 'Magnetic Particle Inspector (MPI)', 'Magnetic Particle Inspector (MPI)', 'Magnetic Particle Inspector (MPI)', '16', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2293, h.id, 'Roll Tread', 'Roll Tread', 'Roll Tread', '17', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2294, h.id, 'Mesin Forging', 'Mesin Forging', 'Mesin Forging', '18', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2295, h.id, 'CNC', 'CNC', 'CNC', '19', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2108, h.id, 'Open', 'Open', 'Open', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0032';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2109, h.id, 'Closed', 'Closed', 'Closed', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0032';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2110, h.id, 'Scrap/Sold', 'Scrap/Sold', 'Scrap/Sold', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0032';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2116, h.id, 'KIM-03, Bay 01', 'KIM-03, Bay 01', 'KIM-03, Bay 01', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2117, h.id, 'KIM-03, Bay 02', 'KIM-03, Bay 02', 'KIM-03, Bay 02', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2118, h.id, 'KIM-03, Bay 03', 'KIM-03, Bay 03', 'KIM-03, Bay 03', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2119, h.id, 'KIM-03, Bay 04', 'KIM-03, Bay 04', 'KIM-03, Bay 04', '4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2120, h.id, 'KIM-03, Bay 05', 'KIM-03, Bay 05', 'KIM-03, Bay 05', '5', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2121, h.id, 'KIM-03, Bay 06', 'KIM-03, Bay 06', 'KIM-03, Bay 06', '6', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2122, h.id, 'KIM-03, Bay 07', 'KIM-03, Bay 07', 'KIM-03, Bay 07', '7', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2123, h.id, 'KIM-03, Bay 08', 'KIM-03, Bay 08', 'KIM-03, Bay 08', '8', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2133, h.id, 'GA-01, GDG 01', 'GA-01, GDG 01', 'GA-01, GDG 01', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2134, h.id, 'GA-01, GDG 02', 'GA-01, GDG 02', 'GA-01, GDG 02', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2135, h.id, 'GA-01, GDG 03', 'GA-01, GDG 03', 'GA-01, GDG 03', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2136, h.id, 'GA-01, GDG 04', 'GA-01, GDG 04', 'GA-01, GDG 04', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2137, h.id, 'GA-01, GDG 05', 'GA-01, GDG 05', 'GA-01, GDG 05', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2138, h.id, 'GA-02, GDG 06', 'GA-02, GDG 06', 'GA-02, GDG 06', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2139, h.id, 'GA-02, GDG 07', 'GA-02, GDG 07', 'GA-02, GDG 07', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2140, h.id, 'GA-02, GDG 08', 'GA-02, GDG 08', 'GA-02, GDG 08', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2141, h.id, 'GA-02, GDG 09', 'GA-02, GDG 09', 'GA-02, GDG 09', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2142, h.id, 'GA-02, GDG 10', 'GA-02, GDG 10', 'GA-02, GDG 10', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2258, h.id, 'GA-08, GDG 00', 'GA-08, GDG 00', 'GA-08, GDG 00', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2259, h.id, 'GA-08, GDG 01', 'GA-08, GDG 01', 'GA-08, GDG 01', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2260, h.id, 'GA-08, GDG 02', 'GA-08, GDG 02', 'GA-08, GDG 02', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2261, h.id, 'GA-08, GDG 03', 'GA-08, GDG 03', 'GA-08, GDG 03', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2262, h.id, 'KIM-03, Bay 11', 'KIM-03, Bay 11', 'KIM-03, Bay 11', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2263, h.id, 'KIM-03, Bay 09', 'KIM-03, Bay 09', 'KIM-03, Bay 09', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2264, h.id, 'KIM-03, Bay 10', 'KIM-03, Bay 10', 'KIM-03, Bay 10', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2288, h.id, 'GA-03, GDG 01', 'GA-03, GDG 01', 'GA-03, GDG 01', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2289, h.id, 'GA-03, GDG 02', 'GA-03, GDG 02', 'GA-03, GDG 02', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2290, h.id, 'GA-03, GDG 03', 'GA-03, GDG 03', 'GA-03, GDG 03', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2148, h.id, 'Natural Gas', 'Natural Gas', 'Natural Gas', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0038';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2149, h.id, 'Electricity', 'Electricity', 'Electricity', NULL, 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0038';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2297, h.id, 'Shift 1', 'Shift 1', 'Shift 1', '1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0039';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2298, h.id, 'Shift 2', 'Shift 2', 'Shift 2', '2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0039';

INSERT INTO dynamic.mdlookup (id, global_category_id, code, name, description, refcode, inputby, inputdt, status)
SELECT 2299, h.id, 'Shift 3', 'Shift 3', 'Shift 3', '3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0039';

-- 4. Update sequence for mdlookup.id so future inserts don't collide
SELECT setval(pg_get_serial_sequence('dynamic.mdlookup', 'id'), COALESCE((SELECT MAX(id) FROM dynamic.mdlookup), 1) + 1, false);

COMMIT;
-- End of script
