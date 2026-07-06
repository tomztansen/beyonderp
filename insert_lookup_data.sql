-- SQL Script to insert Master-Detail Lookup Data into dynamic.mhlookup and dynamic.mdlookup

BEGIN;

-- 1. Insert Master Header Lookup (mhlookup)
INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0001', 'Master : Account Type', 'Master : Account Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0001');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0002', 'Master : Item Type', 'Master : Item Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0002');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0003', 'Master : UOM', 'Master : UOM', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0003');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0004', 'Master : Business Partner Type', 'Master : Business Partner Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0004');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0006', 'Master : Down Payment Type', 'Master : Down Payment Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0006');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0007', 'Master : Delivery Mode', 'Master : Delivery Mode', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0007');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0008', 'Master : Delivery Term', 'Master : Delivery Term', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0008');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0009', 'Master : Purchase Type', 'Master : Purchase Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0009');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0010', 'Master : Purchase Payment Type', 'Master : Purchase Payment Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0010');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0011', 'Master : Sales Type', 'Master : Sales Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0011');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0012', 'Master : Sales Payment Type', 'Master : Sales Payment Type', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0012');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0013', 'Master : Element', 'Master : Element', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0013');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0014', 'Master : Item Parent Group', 'Master : Item Parent Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0014');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0015', 'Master : Item Group', 'Master : Item Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0015');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0016', 'Master : Resource Group', 'Master : Resource Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0016');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0017', 'Master : Site', 'Master : Site', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0017');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0018', 'Master : Resource', 'Master : Resource', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0018');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0020', 'Master : Procurement Category', 'Master : Procurement Category', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0020');

-- 2. Insert Master Detail Lookup (mdlookup)
INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '0', 'Debit', 'Debit', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0001'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '0' AND d.name = 'Debit');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'Credit', 'Credit', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0001'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'Credit');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'Stock', 'Stock', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0002'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'Stock');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2', 'Service', 'Service', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0002'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2' AND d.name = 'Service');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/2 cu. in', '1/2 cubic inch', '1/2 cubic inch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/2 cu. in' AND d.name = '1/2 cubic inch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/2 in', '1/2 inch', '1/2 inch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/2 in' AND d.name = '1/2 inch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/2 lb', '1/2 pound', '1/2 pound', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/2 lb' AND d.name = '1/2 pound');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/2 pt', 'half-pints', 'half-pints', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/2 pt' AND d.name = 'half-pints');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/2 sq. in', '1/2 square inch', '1/2 square inch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/2 sq. in' AND d.name = '1/2 square inch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/4 cu. in', '1/4 cubic inch', '1/4 cubic inch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/4 cu. in' AND d.name = '1/4 cubic inch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/4 in', '1/4 inch', '1/4 inch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/4 in' AND d.name = '1/4 inch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/4 lb', '1/4 pound', '1/4 pound', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/4 lb' AND d.name = '1/4 pound');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/4 sq. in', '1/4 square inch', '1/4 square inch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/4 sq. in' AND d.name = '1/4 square inch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/8 cu. in', '1/8 cubic inch', '1/8 cubic inch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/8 cu. in' AND d.name = '1/8 cubic inch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/8 in', '1/8 inch', '1/8 inch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/8 in' AND d.name = '1/8 inch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1/8 sq. in', '1/8 square inch', '1/8 square inch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1/8 sq. in' AND d.name = '1/8 square inch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Colly', 'Colly', 'Colly', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Colly' AND d.name = 'Colly');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Cyc', 'Cycle', 'Cycle', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Cyc' AND d.name = 'Cycle');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'PRT', 'Power UOM for Power Plant', 'Power UOM for Power Plant', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'PRT' AND d.name = 'Power UOM for Power Plant');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Quantity', 'Quantity', 'Quantity', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Quantity' AND d.name = 'Quantity');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Ton', 'Ton', 'Ton', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Ton' AND d.name = 'Ton');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Ton(US)', 'Short Ton (US)', 'Short Ton (US)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Ton(US)' AND d.name = 'Short Ton (US)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Trip', 'trip', 'trip', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Trip' AND d.name = 'trip');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'bar', 'bar', 'bar', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'bar' AND d.name = 'bar');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'bks', 'bungkus', 'bungkus', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'bks' AND d.name = 'bungkus');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'blk', 'blok', 'blok', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'blk' AND d.name = 'blok');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'box', 'box', 'box', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'box' AND d.name = 'box');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'btg', 'batang', 'batang', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'btg' AND d.name = 'batang');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'btl', 'botol', 'botol', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'btl' AND d.name = 'botol');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'can', 'can/kaleng', 'can/kaleng', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'can' AND d.name = 'can/kaleng');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cl', 'Centiliter', 'Centiliter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cl' AND d.name = 'Centiliter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cm', 'Centimeter', 'Centimeter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cm' AND d.name = 'Centimeter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cm2', 'Square centimeter', 'Square centimeter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cm2' AND d.name = 'Square centimeter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cm3', 'Cubic centimeter', 'Cubic centimeter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cm3' AND d.name = 'Cubic centimeter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cont', 'container', 'container', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cont' AND d.name = 'container');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'crt', 'crate', 'crate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'crt' AND d.name = 'crate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'ctg', 'centong', 'centong', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'ctg' AND d.name = 'centong');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cu. ft', 'cubic feet', 'cubic feet', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cu. ft' AND d.name = 'cubic feet');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cu. in.', 'cubic inches', 'cubic inches', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cu. in.' AND d.name = 'cubic inches');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cu. yd', 'cubic yards', 'cubic yards', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cu. yd' AND d.name = 'cubic yards');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cup', 'cup', 'cup', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cup' AND d.name = 'cup');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'cwt', 'Quintal', 'Quintal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'cwt' AND d.name = 'Quintal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'day', 'day(s)', 'day(s)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'day' AND d.name = 'day(s)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'dl', 'Deciliter', 'Deciliter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'dl' AND d.name = 'Deciliter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'drg', 'dirigen', 'dirigen', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'drg' AND d.name = 'dirigen');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'drm', 'drum', 'drum', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'drm' AND d.name = 'drum');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'dzn', 'dozen', 'dozen', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'dzn' AND d.name = 'dozen');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'fl. oz', 'fluid ounces', 'fluid ounces', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'fl. oz' AND d.name = 'fluid ounces');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'ft', 'Feet', 'Feet', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'ft' AND d.name = 'Feet');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'g', 'gram', 'gram', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'g' AND d.name = 'gram');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'gal', 'Gallons', 'Gallons', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'gal' AND d.name = 'Gallons');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'glg', 'gulung', 'gulung', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'glg' AND d.name = 'gulung');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'hour', 'hour(s)', 'hour(s)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'hour' AND d.name = 'hour(s)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'ikt', 'ikat', 'ikat', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'ikt' AND d.name = 'ikat');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'in', 'inches', 'inches', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'in' AND d.name = 'inches');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'kg', 'kilogram', 'kilogram', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'kg' AND d.name = 'kilogram');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'kit', 'kit', 'kit', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'kit' AND d.name = 'kit');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'km', 'Kilometer', 'Kilometer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'km' AND d.name = 'Kilometer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'kp', 'keping', 'keping', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'kp' AND d.name = 'keping');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'kpg', '[NULL]', '[NULL]', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'kpg' AND d.name = '[NULL]');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'krg', 'karung', 'karung', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'krg' AND d.name = 'karung');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'ktn', 'karton', 'karton', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'ktn' AND d.name = 'karton');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'kwin', '[NULL]', '[NULL]', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'kwin' AND d.name = '[NULL]');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'l', 'Liter', 'Liter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'l' AND d.name = 'Liter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'lb', 'pounds', 'pounds', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'lb' AND d.name = 'pounds');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'lbr', 'lembar', 'lembar', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'lbr' AND d.name = 'lembar');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'lot', 'unit measurement for service cost', 'unit measurement for service cost', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'lot' AND d.name = 'unit measurement for service cost');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'm', 'Meters', 'Meters', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'm' AND d.name = 'Meters');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'm', 'meter', 'meter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'm' AND d.name = 'meter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'm2', 'Square meter', 'Square meter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'm2' AND d.name = 'Square meter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'm3', 'Cubic meter', 'Cubic meter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'm3' AND d.name = 'Cubic meter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'mbl', 'mobil', 'mobil', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'mbl' AND d.name = 'mobil');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'mg', 'Milligram', 'Milligram', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'mg' AND d.name = 'Milligram');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'mi', 'miles', 'miles', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'mi' AND d.name = 'miles');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'min', 'minute(s)', 'minute(s)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'min' AND d.name = 'minute(s)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'ml', 'Milliliter', 'Milliliter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'ml' AND d.name = 'Milliliter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'mm', 'Millimeter', 'Millimeter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'mm' AND d.name = 'Millimeter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'mm2', 'Square millimeter', 'Square millimeter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'mm2' AND d.name = 'Square millimeter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'mm3', 'Cubic millimeter', 'Cubic millimeter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'mm3' AND d.name = 'Cubic millimeter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'month', 'month', 'month', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'month' AND d.name = 'month');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'mt', 'metric tons', 'metric tons', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'mt' AND d.name = 'metric tons');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'ons', 'ons', 'ons', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'ons' AND d.name = 'ons');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'oz', 'Ounces', 'Ounces', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'oz' AND d.name = 'Ounces');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'pal', 'pail', 'pail', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'pal' AND d.name = 'pail');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'pcs', 'Pcs', 'Pcs', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'pcs' AND d.name = 'Pcs');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'plt', 'pallet', 'pallet', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'plt' AND d.name = 'pallet');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'psg', 'pasang', 'pasang', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'psg' AND d.name = 'pasang');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'pt', 'pints', 'pints', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'pt' AND d.name = 'pints');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'ptg', 'potong', 'potong', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'ptg' AND d.name = 'potong');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'qt', 'quarts', 'quarts', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'qt' AND d.name = 'quarts');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'rim', 'rim', 'rim', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'rim' AND d.name = 'rim');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'rol', 'rol', 'rol', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'rol' AND d.name = 'rol');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'sak', 'sak', 'sak', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'sak' AND d.name = 'sak');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'sec', 'second(s)', 'second(s)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'sec' AND d.name = 'second(s)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'set', 'set', 'set', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'set' AND d.name = 'set');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'sq. ft', 'square feet', 'square feet', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'sq. ft' AND d.name = 'square feet');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'sq. in.', 'square inches', 'square inches', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'sq. in.' AND d.name = 'square inches');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'sq. mi', 'square miles', 'square miles', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'sq. mi' AND d.name = 'square miles');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'sq. yd', 'square yards', 'square yards', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'sq. yd' AND d.name = 'square yards');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'stl', 'stel', 'stel', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'stl' AND d.name = 'stel');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 't', 'Ton', 'Ton', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 't' AND d.name = 'Ton');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'tbg', 'tabung', 'tabung', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'tbg' AND d.name = 'tabung');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'tkl', 'tungkul', 'tungkul', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'tkl' AND d.name = 'tungkul');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'tub', 'tube', 'tube', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'tub' AND d.name = 'tube');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'unt', 'unit', 'unit', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'unt' AND d.name = 'unit');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'x', 'storage unit cost', 'storage unit cost', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'x' AND d.name = 'storage unit cost');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'yd', 'Yards', 'Yards', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'yd' AND d.name = 'Yards');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'kWh', 'kWh', 'kWh', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0003'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'kWh' AND d.name = 'kWh');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '0', 'Corporate', 'Corporate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0004'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '0' AND d.name = 'Corporate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'Individual', 'Individual', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0004'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'Individual');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '0', 'Payment', 'Payment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0006'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '0' AND d.name = 'Payment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'Received', 'Received', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0006'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'Received');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'Truck', 'Truck', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0007'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'Truck');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2', 'Sea', 'Sea', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0007'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2' AND d.name = 'Sea');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'FOB', 'FOB', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0008'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'FOB');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2', 'CIF', 'CIF', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0008'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2' AND d.name = 'CIF');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3', 'DAP', 'DAP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0008'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3' AND d.name = 'DAP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4', 'DDP', 'DDP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0008'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4' AND d.name = 'DDP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'Purchase Order', 'Purchase Order', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0009'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'Purchase Order');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2', 'Purchase Return', 'Purchase Return', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0009'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2' AND d.name = 'Purchase Return');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'Down Payment', 'Down Payment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0010'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'Down Payment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2', 'Purchase Invoice', 'Purchase Invoice', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0010'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2' AND d.name = 'Purchase Invoice');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3', 'Expense', 'Expense', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0010'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3' AND d.name = 'Expense');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'Sales Order', 'Sales Order', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0011'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'Sales Order');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2', 'Sales Return', 'Sales Return', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0011'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2' AND d.name = 'Sales Return');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', 'Down Payment', 'Down Payment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0012'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = 'Down Payment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2', 'Sales Invoice', 'Sales Invoice', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0012'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2' AND d.name = 'Sales Invoice');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3', 'Receiving', 'Receiving', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0012'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3' AND d.name = 'Receiving');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Actinium', 'Ac', 'Actinium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Actinium' AND d.name = 'Ac');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Aluminium', 'Al', 'Aluminium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Aluminium' AND d.name = 'Al');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Americium', 'Am', 'Americium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Americium' AND d.name = 'Am');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Antimony', 'Sb', 'Antimony', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Antimony' AND d.name = 'Sb');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Argon', 'Ar', 'Argon', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Argon' AND d.name = 'Ar');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Arsenic', 'As', 'Arsenic', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Arsenic' AND d.name = 'As');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Astatine', 'At', 'Astatine', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Astatine' AND d.name = 'At');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Barium', 'Ba', 'Barium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Barium' AND d.name = 'Ba');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Berkelium', 'Bk', 'Berkelium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Berkelium' AND d.name = 'Bk');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Beryllium', 'Be', 'Beryllium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Beryllium' AND d.name = 'Be');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Bismuth', 'Bi', 'Bismuth', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Bismuth' AND d.name = 'Bi');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Bohrium', 'Bh', 'Bohrium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Bohrium' AND d.name = 'Bh');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Boron', 'B', 'Boron', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Boron' AND d.name = 'B');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Bromine', 'Br', 'Bromine', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Bromine' AND d.name = 'Br');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Cadmium', 'Cd', 'Cadmium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Cadmium' AND d.name = 'Cd');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Caesium', 'Cs', 'Caesium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Caesium' AND d.name = 'Cs');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Calcium', 'Ca', 'Calcium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Calcium' AND d.name = 'Ca');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Californium', 'Cf', 'Californium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Californium' AND d.name = 'Cf');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Carbon', 'C', 'Carbon', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Carbon' AND d.name = 'C');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Cerium', 'Ce', 'Cerium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Cerium' AND d.name = 'Ce');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Chlorine', 'Cl', 'Chlorine', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Chlorine' AND d.name = 'Cl');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Chromium', 'Cr', 'Chromium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Chromium' AND d.name = 'Cr');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Cobalt', 'Co', 'Cobalt', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Cobalt' AND d.name = 'Co');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Copernicium', 'Cn', 'Copernicium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Copernicium' AND d.name = 'Cn');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Copper', 'Cu', 'Copper', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Copper' AND d.name = 'Cu');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Curium', 'Cm', 'Curium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Curium' AND d.name = 'Cm');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Darmstadtium', 'Ds', 'Darmstadtium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Darmstadtium' AND d.name = 'Ds');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Dubnium', 'Db', 'Dubnium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Dubnium' AND d.name = 'Db');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Dysprosium', 'Dy', 'Dysprosium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Dysprosium' AND d.name = 'Dy');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Einsteinium', 'Es', 'Einsteinium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Einsteinium' AND d.name = 'Es');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Erbium', 'Er', 'Erbium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Erbium' AND d.name = 'Er');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Europium', 'Eu', 'Europium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Europium' AND d.name = 'Eu');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fermium', 'Fm', 'Fermium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fermium' AND d.name = 'Fm');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Flerovium', 'Fl', 'Flerovium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Flerovium' AND d.name = 'Fl');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fluorine', 'F', 'Fluorine', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fluorine' AND d.name = 'F');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Francium', 'Fr', 'Francium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Francium' AND d.name = 'Fr');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Gadolinium', 'Gd', 'Gadolinium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Gadolinium' AND d.name = 'Gd');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Gallium', 'Ga', 'Gallium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Gallium' AND d.name = 'Ga');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Germanium', 'Ge', 'Germanium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Germanium' AND d.name = 'Ge');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Gold', 'Au', 'Gold', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Gold' AND d.name = 'Au');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Hafnium', 'Hf', 'Hafnium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Hafnium' AND d.name = 'Hf');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Hassium', 'Hs', 'Hassium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Hassium' AND d.name = 'Hs');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Helium', 'He', 'Helium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Helium' AND d.name = 'He');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Holmium', 'Ho', 'Holmium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Holmium' AND d.name = 'Ho');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Hydrogen', 'H', 'Hydrogen', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Hydrogen' AND d.name = 'H');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Indium', 'In', 'Indium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Indium' AND d.name = 'In');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Iodine', 'I', 'Iodine', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Iodine' AND d.name = 'I');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Iridium', 'Ir', 'Iridium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Iridium' AND d.name = 'Ir');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Iron', 'Fe', 'Iron', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Iron' AND d.name = 'Fe');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Krypton', 'Kr', 'Krypton', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Krypton' AND d.name = 'Kr');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Lanthanum', 'La', 'Lanthanum', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Lanthanum' AND d.name = 'La');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Lawrencium', 'Lr', 'Lawrencium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Lawrencium' AND d.name = 'Lr');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Lead', 'Pb', 'Lead', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Lead' AND d.name = 'Pb');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Lithium', 'Li', 'Lithium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Lithium' AND d.name = 'Li');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Livermorium', 'Lv', 'Livermorium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Livermorium' AND d.name = 'Lv');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Lutetium', 'Lu', 'Lutetium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Lutetium' AND d.name = 'Lu');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Magnesium', 'Mg', 'Magnesium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Magnesium' AND d.name = 'Mg');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Manganese', 'Mn', 'Manganese', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Manganese' AND d.name = 'Mn');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Meitnerium', 'Mt', 'Meitnerium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Meitnerium' AND d.name = 'Mt');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Mendelevium', 'Md', 'Mendelevium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Mendelevium' AND d.name = 'Md');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Mercury', 'Hg', 'Mercury', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Mercury' AND d.name = 'Hg');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Molybdenum', 'Mo', 'Molybdenum', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Molybdenum' AND d.name = 'Mo');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Neodymium', 'Nd', 'Neodymium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Neodymium' AND d.name = 'Nd');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Neon', 'Ne', 'Neon', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Neon' AND d.name = 'Ne');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Neptunium', 'Np', 'Neptunium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Neptunium' AND d.name = 'Np');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Nickel', 'Ni', 'Nickel', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Nickel' AND d.name = 'Ni');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Niobium', 'Nb', 'Niobium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Niobium' AND d.name = 'Nb');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Nitrogen', 'N', 'Nitrogen', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Nitrogen' AND d.name = 'N');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Nobelium', 'No', 'Nobelium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Nobelium' AND d.name = 'No');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Osmium', 'Os', 'Osmium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Osmium' AND d.name = 'Os');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Oxygen', 'O', 'Oxygen', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Oxygen' AND d.name = 'O');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Palladium', 'Pd', 'Palladium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Palladium' AND d.name = 'Pd');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Phosphorus', 'P', 'Phosphorus', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Phosphorus' AND d.name = 'P');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Platinum', 'Pt', 'Platinum', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Platinum' AND d.name = 'Pt');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Plutonium', 'Pu', 'Plutonium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Plutonium' AND d.name = 'Pu');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Polonium', 'Po', 'Polonium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Polonium' AND d.name = 'Po');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Potassium', 'K', 'Potassium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Potassium' AND d.name = 'K');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Praseodymium', 'Pr', 'Praseodymium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Praseodymium' AND d.name = 'Pr');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Promethium', 'Pm', 'Promethium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Promethium' AND d.name = 'Pm');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Protactinium', 'Pa', 'Protactinium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Protactinium' AND d.name = 'Pa');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Radium', 'Ra', 'Radium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Radium' AND d.name = 'Ra');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Radon', 'Rn', 'Radon', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Radon' AND d.name = 'Rn');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rhenium', 'Re', 'Rhenium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rhenium' AND d.name = 'Re');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rhodium', 'Rh', 'Rhodium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rhodium' AND d.name = 'Rh');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Roentgenium', 'Rg', 'Roentgenium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Roentgenium' AND d.name = 'Rg');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubidium', 'Rb', 'Rubidium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubidium' AND d.name = 'Rb');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Ruthenium', 'Ru', 'Ruthenium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Ruthenium' AND d.name = 'Ru');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rutherfordium', 'Rf', 'Rutherfordium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rutherfordium' AND d.name = 'Rf');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Samarium', 'Sm', 'Samarium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Samarium' AND d.name = 'Sm');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Scandium', 'Sc', 'Scandium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Scandium' AND d.name = 'Sc');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Seaborgium', 'Sg', 'Seaborgium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Seaborgium' AND d.name = 'Sg');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Selenium', 'Se', 'Selenium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Selenium' AND d.name = 'Se');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Silicon', 'Si', 'Silicon', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Silicon' AND d.name = 'Si');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Silver', 'Ag', 'Silver', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Silver' AND d.name = 'Ag');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Sodium', 'Na', 'Sodium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Sodium' AND d.name = 'Na');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Strontium', 'Sr', 'Strontium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Strontium' AND d.name = 'Sr');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Sulfur', 'S', 'Sulfur', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Sulfur' AND d.name = 'S');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Tantalum', 'Ta', 'Tantalum', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Tantalum' AND d.name = 'Ta');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Technetium', 'Tc', 'Technetium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Technetium' AND d.name = 'Tc');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Tellurium', 'Te', 'Tellurium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Tellurium' AND d.name = 'Te');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Terbium', 'Tb', 'Terbium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Terbium' AND d.name = 'Tb');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Thallium', 'Tl', 'Thallium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Thallium' AND d.name = 'Tl');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Thorium', 'Th', 'Thorium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Thorium' AND d.name = 'Th');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Thulium', 'Tm', 'Thulium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Thulium' AND d.name = 'Tm');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Tin', 'Sn', 'Tin', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Tin' AND d.name = 'Sn');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Titanium', 'Ti', 'Titanium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Titanium' AND d.name = 'Ti');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Tungsten', 'W', 'Tungsten', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Tungsten' AND d.name = 'W');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Ununoctium', 'Uuo', 'Ununoctium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Ununoctium' AND d.name = 'Uuo');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Ununpentium', 'Uup', 'Ununpentium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Ununpentium' AND d.name = 'Uup');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Ununseptium', 'Uus', 'Ununseptium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Ununseptium' AND d.name = 'Uus');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Ununtrium', 'Uut', 'Ununtrium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Ununtrium' AND d.name = 'Uut');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Uranium', 'U', 'Uranium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Uranium' AND d.name = 'U');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Vanadium', 'V', 'Vanadium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Vanadium' AND d.name = 'V');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Xenon', 'Xe', 'Xenon', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Xenon' AND d.name = 'Xe');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Ytterbium', 'Yb', 'Ytterbium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Ytterbium' AND d.name = 'Yb');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Yttrium', 'Y', 'Yttrium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Yttrium' AND d.name = 'Y');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Zinc', 'Zn', 'Zinc', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Zinc' AND d.name = 'Zn');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Zirconium', 'Zr', 'Zirconium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Zirconium' AND d.name = 'Zr');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fe4', 'Fe4', 'Fe4', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fe4' AND d.name = 'Fe4');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fe4N', 'Fe4N', 'Fe4N', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0013'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fe4N' AND d.name = 'Fe4N');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1', '01. Raw Materials', '01. Raw Materials', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1' AND d.name = '01. Raw Materials');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2', '02. Additive', '02. Additive', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2' AND d.name = '02. Additive');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3', '03. Supporting Material', '03. Supporting Material', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3' AND d.name = '03. Supporting Material');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4', '04. Fixed Asset', '04. Fixed Asset', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4' AND d.name = '04. Fixed Asset');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5', '05. WIP', '05. WIP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5' AND d.name = '05. WIP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '6', '06. Semi Finished Goods', '06. Semi Finished Goods', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '6' AND d.name = '06. Semi Finished Goods');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7', '07. Finished Goods', '07. Finished Goods', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7' AND d.name = '07. Finished Goods');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8', '08. Service', '08. Service', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8' AND d.name = '08. Service');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '9', '09. Material', '09. Material', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0014'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '9' AND d.name = '09. Material');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.01', '2.01.01 Additive - Alloy', '2.01.01 Additive - Alloy', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.01' AND d.name = '2.01.01 Additive - Alloy');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.02', '2.01.02 Additive - Refractory For Metal', '2.01.02 Additive - Refractory For Metal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.02' AND d.name = '2.01.02 Additive - Refractory For Metal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.03', '2.01.03 Additive - Carbon Riser', '2.01.03 Additive - Carbon Riser', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.03' AND d.name = '2.01.03 Additive - Carbon Riser');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.05', '2.01.05 Additive - Exothermic Topping', '2.01.05 Additive - Exothermic Topping', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.05' AND d.name = '2.01.05 Additive - Exothermic Topping');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.06', '2.01.06 Additive - Deoxidiser and Desulphuriser Agent', '2.01.06 Additive - Deoxidiser and Desulphuriser Agent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.06' AND d.name = '2.01.06 Additive - Deoxidiser and Desulphuriser Agent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.07', '2.01.07 Additive - Slag Removal', '2.01.07 Additive - Slag Removal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.07' AND d.name = '2.01.07 Additive - Slag Removal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.11', '2.01.11 Additive - Binder', '2.01.11 Additive - Binder', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.11' AND d.name = '2.01.11 Additive - Binder');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.12', '2.01.12 Additive - Coating', '2.01.12 Additive - Coating', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.12' AND d.name = '2.01.12 Additive - Coating');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.13', '2.01.13 Additive - Sleeve', '2.01.13 Additive - Sleeve', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.13' AND d.name = '2.01.13 Additive - Sleeve');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.14', '2.01.14 Additive - Ceramic', '2.01.14 Additive - Ceramic', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.14' AND d.name = '2.01.14 Additive - Ceramic');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD01.15', '2.01.15 Additive - Joining Compound', '2.01.15 Additive - Joining Compound', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD01.15' AND d.name = '2.01.15 Additive - Joining Compound');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD03.01', '2.03.01 Rubber Additive - Antitack', '2.03.01 Rubber Additive - Antitack', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD03.01' AND d.name = '2.03.01 Rubber Additive - Antitack');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD03.02', '2.03.02 Rubber Additive - Bonding Agent', '2.03.02 Rubber Additive - Bonding Agent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD03.02' AND d.name = '2.03.02 Rubber Additive - Bonding Agent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD03.03', '2.03.03 Rubber Additive - Solvent', '2.03.03 Rubber Additive - Solvent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD03.03' AND d.name = '2.03.03 Rubber Additive - Solvent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'AD03.04', '2.03.04 Rubber Additive - Mold Release Agent', '2.03.04 Rubber Additive - Mold Release Agent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'AD03.04' AND d.name = '2.03.04 Rubber Additive - Mold Release Agent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Chemical', '3.11. Chemical', '3.11. Chemical', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Chemical' AND d.name = '3.11. Chemical');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'ConsGen', '3.13. Consumable for General', '3.13. Consumable for General', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'ConsGen' AND d.name = '3.13. Consumable for General');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'ConsProd', '3.13. Consumable For Production', '3.13. Consumable For Production', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'ConsProd' AND d.name = '3.13. Consumable For Production');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Construct', '3.12. Construction Material', '3.12. Construction Material', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Construct' AND d.name = '3.12. Construction Material');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Electric', '3.14. Electrical Material', '3.14. Electrical Material', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Electric' AND d.name = '3.14. Electrical Material');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FA', '4.01. Fixed Asset Items', '4.01. Fixed Asset Items', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FA' AND d.name = '4.01. Fixed Asset Items');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG01.01', '7.01.01 FG Foundry - GAI', '7.01.01 FG Foundry - GAI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG01.01' AND d.name = '7.01.01 FG Foundry - GAI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG01.02', '7.01.02 FG Foundry - GAS', '7.01.02 FG Foundry - GAS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG01.02' AND d.name = '7.01.02 FG Foundry - GAS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG01.03', '7.01.03 FG Foundry - Others', '7.01.03 FG Foundry - Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG01.03' AND d.name = '7.01.03 FG Foundry - Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG01.09', '7.01.09 FG Foundry - Set', '7.01.09 FG Foundry - Set', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG01.09' AND d.name = '7.01.09 FG Foundry - Set');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG02.01', '7.02.01 FG Fastener - Bolt', '7.02.01 FG Fastener - Bolt', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG02.01' AND d.name = '7.02.01 FG Fastener - Bolt');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG02.02', '7.02.02 FG Fastener - Nut', '7.02.02 FG Fastener - Nut', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG02.02' AND d.name = '7.02.02 FG Fastener - Nut');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG02.03', '7.02.03 FG Fastener - Washer', '7.02.03 FG Fastener - Washer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG02.03' AND d.name = '7.02.03 FG Fastener - Washer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG02.04', '7.02.04 FG Fastener - Set', '7.02.04 FG Fastener - Set', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG02.04' AND d.name = '7.02.04 FG Fastener - Set');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG02.05', '7.02.05 FG Fastener - Others', '7.02.05 FG Fastener - Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG02.05' AND d.name = '7.02.05 FG Fastener - Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG03.01', '7.03.01 FG Rubber - Liner', '7.03.01 FG Rubber - Liner', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG03.01' AND d.name = '7.03.01 FG Rubber - Liner');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG03.04', '7.03.04 FG Rubber - Joint Strip', '7.03.04 FG Rubber - Joint Strip', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG03.04' AND d.name = '7.03.04 FG Rubber - Joint Strip');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG03.05', '7.03.05 FG Rubber - Seal', '7.03.05 FG Rubber - Seal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG03.05' AND d.name = '7.03.05 FG Rubber - Seal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG03.06', '7.03.06 FG Rubber - Set', '7.03.06 FG Rubber - Set', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG03.06' AND d.name = '7.03.06 FG Rubber - Set');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG03.07', '7.03.07 FG Rubber - Others', '7.03.07 FG Rubber - Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG03.07' AND d.name = '7.03.07 FG Rubber - Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG04.01', '7.04.01 FG Grinding Media - Ball', '7.04.01 FG Grinding Media - Ball', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG04.01' AND d.name = '7.04.01 FG Grinding Media - Ball');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FG04.02', '7.04.02 FG Grinding Media - Rod', '7.04.02 FG Grinding Media - Rod', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FG04.02' AND d.name = '7.04.02 FG Grinding Media - Rod');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'FuelGas', '3.16. Fuel & Gas', '3.16. Fuel & Gas', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'FuelGas' AND d.name = '3.16. Fuel & Gas');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Furniture', '3.15. Furniture', '3.15. Furniture', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Furniture' AND d.name = '3.15. Furniture');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Grocery', '3.17. Grocery', '3.17. Grocery', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Grocery' AND d.name = '3.17. Grocery');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'HomeApp', '3.21. Home Appliance', '3.21. Home Appliance', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'HomeApp' AND d.name = '3.21. Home Appliance');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'IT', '3.18. IT Supplies', '3.18. IT Supplies', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'IT' AND d.name = '3.18. IT Supplies');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Instrument', '3.33. Instruments', '3.33. Instruments', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Instrument' AND d.name = '3.33. Instruments');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Marketing', '3.34. Merchandise Marketing', '3.34. Merchandise Marketing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Marketing' AND d.name = '3.34. Merchandise Marketing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Mat01', '9.01. Material Foundry', '9.01. Material Foundry', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Mat01' AND d.name = '9.01. Material Foundry');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Mat02', '9.02. Material Fastener', '9.02. Material Fastener', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Mat02' AND d.name = '9.02. Material Fastener');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Mat03', '9.03. Material Rubber', '9.03. Material Rubber', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Mat03' AND d.name = '9.03. Material Rubber');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Mat04', '9.04. Material Grinding Ball', '9.04. Material Grinding Ball', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Mat04' AND d.name = '9.04. Material Grinding Ball');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Medical', '3.20. Medical Supplies', '3.20. Medical Supplies', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Medical' AND d.name = '3.20. Medical Supplies');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'OfficeSup', '3.22. Office Supplies', '3.22. Office Supplies', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'OfficeSup' AND d.name = '3.22. Office Supplies');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Oil&Lub', '3.23. Oil & Lubricant', '3.23. Oil & Lubricant', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Oil&Lub' AND d.name = '3.23. Oil & Lubricant');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'PPE', '3.24. Personnal Protection Equipment', '3.24. Personnal Protection Equipment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'PPE' AND d.name = '3.24. Personnal Protection Equipment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Packaging', '3.31. Packaging Material', '3.31. Packaging Material', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Packaging' AND d.name = '3.31. Packaging Material');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM01.01', '1.01.01 RM Foundry - Scrap', '1.01.01 RM Foundry - Scrap', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM01.01' AND d.name = '1.01.01 RM Foundry - Scrap');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM01.11', '1.01.11 RM Foundry - Sand', '1.01.11 RM Foundry - Sand', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM01.11' AND d.name = '1.01.11 RM Foundry - Sand');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM02.01', '1.02.01 RM Fastener - As Steel Bar 42CRMO', '1.02.01 RM Fastener - As Steel Bar 42CRMO', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM02.01' AND d.name = '1.02.01 RM Fastener - As Steel Bar 42CRMO');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM02.02', '1.02.02 RM Fastener - As Steel Bar 1040', '1.02.02 RM Fastener - As Steel Bar 1040', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM02.02' AND d.name = '1.02.02 RM Fastener - As Steel Bar 1040');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM02.03', '1.02.03 RM Fastener - As AISI 4140', '1.02.03 RM Fastener - As AISI 4140', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM02.03' AND d.name = '1.02.03 RM Fastener - As AISI 4140');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM02.04', '1.02.04 RM Fastener - As SUS', '1.02.04 RM Fastener - As SUS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM02.04' AND d.name = '1.02.04 RM Fastener - As SUS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM03.01', '1.03.01 RM Rubber - Polymer', '1.03.01 RM Rubber - Polymer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM03.01' AND d.name = '1.03.01 RM Rubber - Polymer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM03.02', '1.03.02 RM Rubber - Fillers', '1.03.02 RM Rubber - Fillers', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM03.02' AND d.name = '1.03.02 RM Rubber - Fillers');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM03.03', '1.03.03 RM Rubber - Chemical', '1.03.03 RM Rubber - Chemical', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM03.03' AND d.name = '1.03.03 RM Rubber - Chemical');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM03.04', '1.03.04 RM Rubber - Curative Agent', '1.03.04 RM Rubber - Curative Agent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM03.04' AND d.name = '1.03.04 RM Rubber - Curative Agent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM03.05', '1.03.05 RM Rubber - Oil', '1.03.05 RM Rubber - Oil', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM03.05' AND d.name = '1.03.05 RM Rubber - Oil');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM03.06', '1.03.06 RM Rubber - Coloring Agent', '1.03.06 RM Rubber - Coloring Agent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM03.06' AND d.name = '1.03.06 RM Rubber - Coloring Agent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM03.09', '1.03.09 RM Rubber - Metal', '1.03.09 RM Rubber - Metal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM03.09' AND d.name = '1.03.09 RM Rubber - Metal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM04.01', '1.04.01 RM GM - Steel Bar', '1.04.01 RM GM - Steel Bar', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM04.01' AND d.name = '1.04.01 RM GM - Steel Bar');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM05.01', '1.05.01 RM PLTU - Wood', '1.05.01 RM PLTU - Wood', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM05.01' AND d.name = '1.05.01 RM PLTU - Wood');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM05.02', '1.05.02 RM PLTU - Palm', '1.05.02 RM PLTU - Palm', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM05.02' AND d.name = '1.05.02 RM PLTU - Palm');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM05.03', '1.05.03 RM PLTU - Biomass', '1.05.03 RM PLTU - Biomass', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM05.03' AND d.name = '1.05.03 RM PLTU - Biomass');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'SERVICE', '8.99. Uncategorized Service', '8.99. Uncategorized Service', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'SERVICE' AND d.name = '8.99. Uncategorized Service');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'SF01.01', '5.01. Semi Finished Goods - Composite Casting', '5.01. Semi Finished Goods - Composite Casting', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'SF01.01' AND d.name = '5.01. Semi Finished Goods - Composite Casting');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'SF03.01', '5.02. Ceramic Rubber Tube', '5.02. Ceramic Rubber Tube', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'SF03.01' AND d.name = '5.02. Ceramic Rubber Tube');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'SF03.02', '5.03. Semi Finished Goods - Wearplate', '5.03. Semi Finished Goods - Wearplate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'SF03.02' AND d.name = '5.03. Semi Finished Goods - Wearplate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv001', '8.01. IT Service', '8.01. IT Service', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv001' AND d.name = '8.01. IT Service');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv002', '8.02. Import Cost', '8.02. Import Cost', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv002' AND d.name = '8.02. Import Cost');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv003', '8.03. Export Cost', '8.03. Export Cost', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv003' AND d.name = '8.03. Export Cost');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv004', '8.04. Maintenance', '8.04. Maintenance', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv004' AND d.name = '8.04. Maintenance');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv005', '8.05. Rent', '8.05. Rent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv005' AND d.name = '8.05. Rent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv006', '8.06. Expedition', '8.06. Expedition', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv006' AND d.name = '8.06. Expedition');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv007', '8.07. Jasa Outsourcing', '8.07. Jasa Outsourcing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv007' AND d.name = '8.07. Jasa Outsourcing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv008', '8.08. Waste Treatment', '8.08. Waste Treatment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv008' AND d.name = '8.08. Waste Treatment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv009', '8.09. Professional and Engineer', '8.09. Professional and Engineer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv009' AND d.name = '8.09. Professional and Engineer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv010', '8.10. Marketing', '8.10. Marketing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv010' AND d.name = '8.10. Marketing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv011', '8.11. Seminar and Training', '8.11. Seminar and Training', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv011' AND d.name = '8.11. Seminar and Training');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv012', '8.12. Project', '8.12. Project', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv012' AND d.name = '8.12. Project');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv013', '8.13. Prepaid', '8.13. Prepaid', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv013' AND d.name = '8.13. Prepaid');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Serv016', '8.16. Sembako', '8.16. Sembako', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Serv016' AND d.name = '8.16. Sembako');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Sparepart', '3.25. Spare Part', '3.25. Spare Part', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Sparepart' AND d.name = '3.25. Spare Part');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Steel', '3.26. Steel', '3.26. Steel', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Steel' AND d.name = '3.26. Steel');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Toiletry', '3.27. Toiletry Supplies', '3.27. Toiletry Supplies', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Toiletry' AND d.name = '3.27. Toiletry Supplies');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Tools', '3.28. Tools', '3.28. Tools', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Tools' AND d.name = '3.28. Tools');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Uniform', '3.35. Uniform', '3.35. Uniform', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Uniform' AND d.name = '3.35. Uniform');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Wood', '3.30. Wood', '3.30. Wood', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Wood' AND d.name = '3.30. Wood');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'RM03.07', '1.03.07 RM Rubber - Casting', '1.03.07 RM Rubber - Casting', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0015'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'RM03.07' AND d.name = '1.03.07 RM Rubber - Casting');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Foundry', 'Foundry Resource Group', 'Foundry Resource Group', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0016'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Foundry' AND d.name = 'Foundry Resource Group');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener', 'Fastener Resource Group', 'Fastener Resource Group', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0016'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener' AND d.name = 'Fastener Resource Group');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber', 'Rubber Resource Group', 'Rubber Resource Group', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0016'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber' AND d.name = 'Rubber Resource Group');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Grinding Media', 'Grinding Media Resource Group', 'Grinding Media Resource Group', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0016'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Grinding Media' AND d.name = 'Grinding Media Resource Group');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU08', 'Grinding Media Pasuruan', 'Grinding Media Pasuruan', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU08' AND d.name = 'Grinding Media Pasuruan');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU07', 'HO Medan', 'HO Medan', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU07' AND d.name = 'HO Medan');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU01', 'Foundry', 'Foundry', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU01' AND d.name = 'Foundry');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU02', 'Fastener', 'Fastener', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU02' AND d.name = 'Fastener');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU03', 'Rubber Plant', 'Rubber Plant', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU03' AND d.name = 'Rubber Plant');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU04', 'Grinding Media Medan', 'Grinding Media Medan', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU04' AND d.name = 'Grinding Media Medan');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU05', 'PLTU I', 'PLTU I', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU05' AND d.name = 'PLTU I');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU06', 'PLTU II', 'PLTU II', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0017'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU06' AND d.name = 'PLTU II');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Foundry - Moulding', 'Foundry - Moulding', 'Foundry - Moulding', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Foundry - Moulding' AND d.name = 'Foundry - Moulding');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Foundry - Knock Out', 'Foundry - Knock Out', 'Foundry - Knock Out', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Foundry - Knock Out' AND d.name = 'Foundry - Knock Out');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Foundry - Heat Treatment', 'Foundry - Heat Treatment', 'Foundry - Heat Treatment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Foundry - Heat Treatment' AND d.name = 'Foundry - Heat Treatment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Foundry - Melting', 'Foundry - Melting', 'Foundry - Melting', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Foundry - Melting' AND d.name = 'Foundry - Melting');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Foundry - Fettling', 'Foundry - Fettling', 'Foundry - Fettling', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Foundry - Fettling' AND d.name = 'Foundry - Fettling');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Foundry - Machining', 'Foundry - Machining', 'Foundry - Machining', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Foundry - Machining' AND d.name = 'Foundry - Machining');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Foundry - Final Finishing', 'Foundry - Final Finishing', 'Foundry - Final Finishing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Foundry - Final Finishing' AND d.name = 'Foundry - Final Finishing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Foundry - QC', 'Foundry - QC', 'Foundry - QC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Foundry - QC' AND d.name = 'Foundry - QC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Mixing', 'Rubber - Mixing', 'Rubber - Mixing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Mixing' AND d.name = 'Rubber - Mixing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener - Bolt Cutting', 'Fastener - Bolt Cutting', 'Fastener - Bolt Cutting', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener - Bolt Cutting' AND d.name = 'Fastener - Bolt Cutting');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener -  Bolt MPI', 'Fastener - Bolt MPI', 'Fastener - Bolt MPI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener -  Bolt MPI' AND d.name = 'Fastener - Bolt MPI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener - Washer Cutting', 'Fastener - Washer Cutting', 'Fastener - Washer Cutting', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener - Washer Cutting' AND d.name = 'Fastener - Washer Cutting');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener - Bolt Forging', 'Fastener - Bolt Forging', 'Fastener - Bolt Forging', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener - Bolt Forging' AND d.name = 'Fastener - Bolt Forging');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener -  Bolt Heat Treatment', 'Fastener - Bolt Heat Treatment', 'Fastener - Bolt Heat Treatment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener -  Bolt Heat Treatment' AND d.name = 'Fastener - Bolt Heat Treatment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Laminating', 'Rubber - Laminating', 'Rubber - Laminating', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Laminating' AND d.name = 'Rubber - Laminating');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Extrusion', 'Rubber - Extrusion', 'Rubber - Extrusion', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Extrusion' AND d.name = 'Rubber - Extrusion');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Compression Moulding', 'Rubber - Compression Moulding', 'Rubber - Compression Moulding', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Compression Moulding' AND d.name = 'Rubber - Compression Moulding');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Autoclave', 'Rubber - Autoclave', 'Rubber - Autoclave', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Autoclave' AND d.name = 'Rubber - Autoclave');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Hand Lining', 'Rubber - Hand Lining', 'Rubber - Hand Lining', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Hand Lining' AND d.name = 'Rubber - Hand Lining');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Final Finishing', 'Rubber - Final Finishing', 'Rubber - Final Finishing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Final Finishing' AND d.name = 'Rubber - Final Finishing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - QC', 'Rubber - QC', 'Rubber - QC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - QC' AND d.name = 'Rubber - QC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Preforming', 'Rubber - Preforming', 'Rubber - Preforming', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Preforming' AND d.name = 'Rubber - Preforming');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Shotblasting', 'Rubber - Shotblasting', 'Rubber - Shotblasting', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Shotblasting' AND d.name = 'Rubber - Shotblasting');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Rubber - Spray Boothing', 'Rubber - Spray Boothing', 'Rubber - Spray Boothing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Rubber - Spray Boothing' AND d.name = 'Rubber - Spray Boothing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener - Washer Machining', 'Fastener - Washer Machining', 'Fastener - Washer Machining', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener - Washer Machining' AND d.name = 'Fastener - Washer Machining');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener - Bolt Machining', 'Fastener - Bolt Machining', 'Fastener - Bolt Machining', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener - Bolt Machining' AND d.name = 'Fastener - Bolt Machining');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener -  Bolt Roll Tread', 'Fastener - Bolt Roll Tread', 'Fastener - Bolt Roll Tread', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener -  Bolt Roll Tread' AND d.name = 'Fastener - Bolt Roll Tread');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Fastener - QC', 'Fastener - QC', 'Fastener - QC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0018'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Fastener - QC' AND d.name = 'Fastener - QC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '01. Raw Material', '01. Raw Material', '01. Raw Material', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '01. Raw Material' AND d.name = '01. Raw Material');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '07. Finished  Goods', '07. Finished  Goods', '07. Finished  Goods', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '07. Finished  Goods' AND d.name = '07. Finished  Goods');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '08. Service', '08. Service', '08. Service', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '08. Service' AND d.name = '08. Service');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01-001 RM Foundry - Scrap', '1.01-001 RM Foundry - Scrap', '1.01-001 RM Foundry - Scrap', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01-001 RM Foundry - Scrap' AND d.name = '1.01-001 RM Foundry - Scrap');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01-011.01 RM Foundry - Sand - Chromite', '1.01-011.01 RM Foundry - Sand - Chromite', '1.01-011.01 RM Foundry - Sand - Chromite', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01-011.01 RM Foundry - Sand - Chromite' AND d.name = '1.01-011.01 RM Foundry - Sand - Chromite');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01-011.02 RM Foundry - Sand - Silica', '1.01-011.02 RM Foundry - Sand - Silica', '1.01-011.02 RM Foundry - Sand - Silica', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01-011.02 RM Foundry - Sand - Silica' AND d.name = '1.01-011.02 RM Foundry - Sand - Silica');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01-011.99 RM Foundry - Other Sand', '1.01-011.99 RM Foundry - Other Sand', '1.01-011.99 RM Foundry - Other Sand', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01-011.99 RM Foundry - Other Sand' AND d.name = '1.01-011.99 RM Foundry - Other Sand');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02-001.01 RM Fastener - AS Steel Bar 42CRMO - Bo', '1.02-001.01 RM Fastener - AS Steel Bar 42CRMO - Bolt', '1.02-001.01 RM Fastener - AS Steel Bar 42CRMO - Bolt', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02-001.01 RM Fastener - AS Steel Bar 42CRMO - Bo' AND d.name = '1.02-001.01 RM Fastener - AS Steel Bar 42CRMO - Bolt');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02-001.02 RM Fastener - AS Steel Bar 42CRMO - Wa', '1.02-001.02 RM Fastener - AS Steel Bar 42CRMO - Washer', '1.02-001.02 RM Fastener - AS Steel Bar 42CRMO - Washer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02-001.02 RM Fastener - AS Steel Bar 42CRMO - Wa' AND d.name = '1.02-001.02 RM Fastener - AS Steel Bar 42CRMO - Washer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02-002 RM Fastener - As Steel Bar 1040', '1.02-002 RM Fastener - As Steel Bar 1040', '1.02-002 RM Fastener - As Steel Bar 1040', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02-002 RM Fastener - As Steel Bar 1040' AND d.name = '1.02-002 RM Fastener - As Steel Bar 1040');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02-003 RM Fastener - As AISI 4140', '1.02-003 RM Fastener - As AISI 4140', '1.02-003 RM Fastener - As AISI 4140', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02-003 RM Fastener - As AISI 4140' AND d.name = '1.02-003 RM Fastener - As AISI 4140');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02-004 RM Fastener - As SUS', '1.02-004 RM Fastener - As SUS', '1.02-004 RM Fastener - As SUS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02-004 RM Fastener - As SUS' AND d.name = '1.02-004 RM Fastener - As SUS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-001 RM Rubber - Polymer', '1.03-001 RM Rubber - Polymer', '1.03-001 RM Rubber - Polymer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-001 RM Rubber - Polymer' AND d.name = '1.03-001 RM Rubber - Polymer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-002 RM Rubber - Fillers', '1.03-002 RM Rubber - Fillers', '1.03-002 RM Rubber - Fillers', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-002 RM Rubber - Fillers' AND d.name = '1.03-002 RM Rubber - Fillers');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-003 RM Rubber - Chemical', '1.03-003 RM Rubber - Chemical', '1.03-003 RM Rubber - Chemical', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-003 RM Rubber - Chemical' AND d.name = '1.03-003 RM Rubber - Chemical');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-004 RM Rubber - Curative Agent', '1.03-004 RM Rubber - Curative Agent', '1.03-004 RM Rubber - Curative Agent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-004 RM Rubber - Curative Agent' AND d.name = '1.03-004 RM Rubber - Curative Agent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-005 RM Rubber - Oil', '1.03-005 RM Rubber - Oil', '1.03-005 RM Rubber - Oil', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-005 RM Rubber - Oil' AND d.name = '1.03-005 RM Rubber - Oil');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-006 RM Rubber - Coloring Agent', '1.03-006 RM Rubber - Coloring Agent', '1.03-006 RM Rubber - Coloring Agent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-006 RM Rubber - Coloring Agent' AND d.name = '1.03-006 RM Rubber - Coloring Agent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-007 RM Rubber - Casting', '1.03-007 RM Rubber - Casting', '1.03-007 RM Rubber - Casting', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-007 RM Rubber - Casting' AND d.name = '1.03-007 RM Rubber - Casting');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-901 Wearplate', '1.03-901 Wearplate', '1.03-901 Wearplate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-901 Wearplate' AND d.name = '1.03-901 Wearplate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-902 Aluminium C-Channel', '1.03-902 Aluminium C-Channel', '1.03-902 Aluminium C-Channel', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-902 Aluminium C-Channel' AND d.name = '1.03-902 Aluminium C-Channel');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-903 Aluminium T-Track', '1.03-903 Aluminium T-Track', '1.03-903 Aluminium T-Track', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-903 Aluminium T-Track' AND d.name = '1.03-903 Aluminium T-Track');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-904 Mild Steel C-Channel', '1.03-904 Mild Steel C-Channel', '1.03-904 Mild Steel C-Channel', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-904 Mild Steel C-Channel' AND d.name = '1.03-904 Mild Steel C-Channel');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04-001 RM GM - Steel Bar', '1.04-001 RM GM - Steel Bar', '1.04-001 RM GM - Steel Bar', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04-001 RM GM - Steel Bar' AND d.name = '1.04-001 RM GM - Steel Bar');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05-001 RM PLTU - Wood', '1.05-001 RM PLTU - Wood', '1.05-001 RM PLTU - Wood', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05-001 RM PLTU - Wood' AND d.name = '1.05-001 RM PLTU - Wood');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05-002 RM PLTU - Palm', '1.05-002 RM PLTU - Palm', '1.05-002 RM PLTU - Palm', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05-002 RM PLTU - Palm' AND d.name = '1.05-002 RM PLTU - Palm');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05-003 RM PLTU - Biomass', '1.05-003 RM PLTU - Biomass', '1.05-003 RM PLTU - Biomass', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05-003 RM PLTU - Biomass' AND d.name = '1.05-003 RM PLTU - Biomass');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-001.01 Additive - Alloy - Ferro Chromium', '2.01-001.01 Additive - Alloy - Ferro Chromium', '2.01-001.01 Additive - Alloy - Ferro Chromium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-001.01 Additive - Alloy - Ferro Chromium' AND d.name = '2.01-001.01 Additive - Alloy - Ferro Chromium');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-001.02 Additive - Alloy - Ferro Molybdenum', '2.01-001.02 Additive - Alloy - Ferro Molybdenum', '2.01-001.02 Additive - Alloy - Ferro Molybdenum', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-001.02 Additive - Alloy - Ferro Molybdenum' AND d.name = '2.01-001.02 Additive - Alloy - Ferro Molybdenum');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-001.03 Additive - Alloy - Ferro Silicon', '2.01-001.03 Additive - Alloy - Ferro Silicon', '2.01-001.03 Additive - Alloy - Ferro Silicon', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-001.03 Additive - Alloy - Ferro Silicon' AND d.name = '2.01-001.03 Additive - Alloy - Ferro Silicon');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-001.04 Additive - Alloy - Ferro Titanium', '2.01-001.04 Additive - Alloy - Ferro Titanium', '2.01-001.04 Additive - Alloy - Ferro Titanium', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-001.04 Additive - Alloy - Ferro Titanium' AND d.name = '2.01-001.04 Additive - Alloy - Ferro Titanium');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-001.99 Additive - Alloy - Others', '2.01-001.99 Additive - Alloy - Others', '2.01-001.99 Additive - Alloy - Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-001.99 Additive - Alloy - Others' AND d.name = '2.01-001.99 Additive - Alloy - Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-002 Additive - Refractory For Metal', '2.01-002 Additive - Refractory For Metal', '2.01-002 Additive - Refractory For Metal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-002 Additive - Refractory For Metal' AND d.name = '2.01-002 Additive - Refractory For Metal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-003 Additive - Carbon Riser', '2.01-003 Additive - Carbon Riser', '2.01-003 Additive - Carbon Riser', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-003 Additive - Carbon Riser' AND d.name = '2.01-003 Additive - Carbon Riser');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-005 Additive - Exothermic Topping', '2.01-005 Additive - Exothermic Topping', '2.01-005 Additive - Exothermic Topping', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-005 Additive - Exothermic Topping' AND d.name = '2.01-005 Additive - Exothermic Topping');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-006 Additive - Deoxidiser and Desulphuriser A', '2.01-006 Additive - Deoxidiser and Desulphuriser Agent', '2.01-006 Additive - Deoxidiser and Desulphuriser Agent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-006 Additive - Deoxidiser and Desulphuriser A' AND d.name = '2.01-006 Additive - Deoxidiser and Desulphuriser Agent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-007 Additive - Slag Removal', '2.01-007 Additive - Slag Removal', '2.01-007 Additive - Slag Removal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-007 Additive - Slag Removal' AND d.name = '2.01-007 Additive - Slag Removal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-011 Additive - Binder - Catalyst', '2.01-011 Additive - Binder - Catalyst', '2.01-011 Additive - Binder - Catalyst', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-011 Additive - Binder - Catalyst' AND d.name = '2.01-011 Additive - Binder - Catalyst');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-011 Additive - Binder - Resin', '2.01-011 Additive - Binder - Resin', '2.01-011 Additive - Binder - Resin', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-011 Additive - Binder - Resin' AND d.name = '2.01-011 Additive - Binder - Resin');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-012 Additive - Coating', '2.01-012 Additive - Coating', '2.01-012 Additive - Coating', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-012 Additive - Coating' AND d.name = '2.01-012 Additive - Coating');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-012 Additive - Solvent/Fuel', '2.01-012 Additive - Solvent/Fuel', '2.01-012 Additive - Solvent/Fuel', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-012 Additive - Solvent/Fuel' AND d.name = '2.01-012 Additive - Solvent/Fuel');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-013 Additive - Sleeve', '2.01-013 Additive - Sleeve', '2.01-013 Additive - Sleeve', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-013 Additive - Sleeve' AND d.name = '2.01-013 Additive - Sleeve');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-014 Additive - Ceramic', '2.01-014 Additive - Ceramic', '2.01-014 Additive - Ceramic', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-014 Additive - Ceramic' AND d.name = '2.01-014 Additive - Ceramic');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-015 Additive - Joining Compound', '2.01-015 Additive - Joining Compound', '2.01-015 Additive - Joining Compound', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-015 Additive - Joining Compound' AND d.name = '2.01-015 Additive - Joining Compound');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.03-001 Rubber Additive - Antitack', '2.03-001 Rubber Additive - Antitack', '2.03-001 Rubber Additive - Antitack', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.03-001 Rubber Additive - Antitack' AND d.name = '2.03-001 Rubber Additive - Antitack');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.03-002 Rubber Additive - Bonding Agent', '2.03-002 Rubber Additive - Bonding Agent', '2.03-002 Rubber Additive - Bonding Agent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.03-002 Rubber Additive - Bonding Agent' AND d.name = '2.03-002 Rubber Additive - Bonding Agent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.03-003 Rubber Additive - Solvent', '2.03-003 Rubber Additive - Solvent', '2.03-003 Rubber Additive - Solvent', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.03-003 Rubber Additive - Solvent' AND d.name = '2.03-003 Rubber Additive - Solvent');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.11-001 Chemical Solid', '3.11-001 Chemical Solid', '3.11-001 Chemical Solid', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.11-001 Chemical Solid' AND d.name = '3.11-001 Chemical Solid');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.11-002 Chemical Liquid', '3.11-002 Chemical Liquid', '3.11-002 Chemical Liquid', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.11-002 Chemical Liquid' AND d.name = '3.11-002 Chemical Liquid');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-001 PLAT', '3.12-001 PLAT', '3.12-001 PLAT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-001 PLAT' AND d.name = '3.12-001 PLAT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-002 PASIR', '3.12-002 PASIR', '3.12-002 PASIR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-002 PASIR' AND d.name = '3.12-002 PASIR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-003 KRAN / PADDLE', '3.12-003 KRAN / PADDLE', '3.12-003 KRAN / PADDLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-003 KRAN / PADDLE' AND d.name = '3.12-003 KRAN / PADDLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-004 KLOSET / URINAL', '3.12-004 KLOSET / URINAL', '3.12-004 KLOSET / URINAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-004 KLOSET / URINAL' AND d.name = '3.12-004 KLOSET / URINAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-005 WIREMESH', '3.12-005 WIREMESH', '3.12-005 WIREMESH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-005 WIREMESH' AND d.name = '3.12-005 WIREMESH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-006 SENG', '3.12-006 SENG', '3.12-006 SENG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-006 SENG' AND d.name = '3.12-006 SENG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-007 ENGSEL', '3.12-007 ENGSEL', '3.12-007 ENGSEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-007 ENGSEL' AND d.name = '3.12-007 ENGSEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-008 SEMEN', '3.12-008 SEMEN', '3.12-008 SEMEN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-008 SEMEN' AND d.name = '3.12-008 SEMEN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-009 WASTAFEL', '3.12-009 WASTAFEL', '3.12-009 WASTAFEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-009 WASTAFEL' AND d.name = '3.12-009 WASTAFEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-010 PARTISI', '3.12-010 PARTISI', '3.12-010 PARTISI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-010 PARTISI' AND d.name = '3.12-010 PARTISI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-011 BAJA RINGAN / FURING', '3.12-011 BAJA RINGAN / FURING', '3.12-011 BAJA RINGAN / FURING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-011 BAJA RINGAN / FURING' AND d.name = '3.12-011 BAJA RINGAN / FURING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-012 BATU / DITCH / BOREPILE', '3.12-012 BATU / DITCH / BOREPILE', '3.12-012 BATU / DITCH / BOREPILE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-012 BATU / DITCH / BOREPILE' AND d.name = '3.12-012 BATU / DITCH / BOREPILE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-013 KAWAT', '3.12-013 KAWAT', '3.12-013 KAWAT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-013 KAWAT' AND d.name = '3.12-013 KAWAT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-014 KERAMIK', '3.12-014 KERAMIK', '3.12-014 KERAMIK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-014 KERAMIK' AND d.name = '3.12-014 KERAMIK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-015 KALSIBOARD / GYPSUM', '3.12-015 KALSIBOARD / GYPSUM', '3.12-015 KALSIBOARD / GYPSUM', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-015 KALSIBOARD / GYPSUM' AND d.name = '3.12-015 KALSIBOARD / GYPSUM');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-016 DEMPUL', '3.12-016 DEMPUL', '3.12-016 DEMPUL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-016 DEMPUL' AND d.name = '3.12-016 DEMPUL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-017 READY MIX', '3.12-017 READY MIX', '3.12-017 READY MIX', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-017 READY MIX' AND d.name = '3.12-017 READY MIX');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-018 PINTU / JENDELA / PAGAR', '3.12-018 PINTU / JENDELA / PAGAR', '3.12-018 PINTU / JENDELA / PAGAR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-018 PINTU / JENDELA / PAGAR' AND d.name = '3.12-018 PINTU / JENDELA / PAGAR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-019 KACA', '3.12-019 KACA', '3.12-019 KACA', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-019 KACA' AND d.name = '3.12-019 KACA');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-020 SHOWER', '3.12-020 SHOWER', '3.12-020 SHOWER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-020 SHOWER' AND d.name = '3.12-020 SHOWER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-021 POLYCARBONATE', '3.12-021 POLYCARBONATE', '3.12-021 POLYCARBONATE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-021 POLYCARBONATE' AND d.name = '3.12-021 POLYCARBONATE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-022 GRENDEL / HANDLE / KUNCI', '3.12-022 GRENDEL / HANDLE / KUNCI', '3.12-022 GRENDEL / HANDLE / KUNCI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-022 GRENDEL / HANDLE / KUNCI' AND d.name = '3.12-022 GRENDEL / HANDLE / KUNCI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-023 PITA ASBES', '3.12-023 PITA ASBES', '3.12-023 PITA ASBES', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-023 PITA ASBES' AND d.name = '3.12-023 PITA ASBES');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12-024 BAHAN BANGUNAN LAINNYA', '3.12-024 BAHAN BANGUNAN LAINNYA', '3.12-024 BAHAN BANGUNAN LAINNYA', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12-024 BAHAN BANGUNAN LAINNYA' AND d.name = '3.12-024 BAHAN BANGUNAN LAINNYA');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.12. Construction Material', '3.12. Construction Material', '3.12. Construction Material', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.12. Construction Material' AND d.name = '3.12. Construction Material');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-001 ADHESIVE (Perekat/ Lem)', '3.13-001 ADHESIVE (Perekat/ Lem)', '3.13-001 ADHESIVE (Perekat/ Lem)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-001 ADHESIVE (Perekat/ Lem)' AND d.name = '3.13-001 ADHESIVE (Perekat/ Lem)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-002 DOUBLE BEAMS', '3.13-002 DOUBLE BEAMS', '3.13-002 DOUBLE BEAMS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-002 DOUBLE BEAMS' AND d.name = '3.13-002 DOUBLE BEAMS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-003 GAS DIFFUSER', '3.13-003 GAS DIFFUSER', '3.13-003 GAS DIFFUSER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-003 GAS DIFFUSER' AND d.name = '3.13-003 GAS DIFFUSER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-004 MOTOR SIRENE', '3.13-004 MOTOR SIRENE', '3.13-004 MOTOR SIRENE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-004 MOTOR SIRENE' AND d.name = '3.13-004 MOTOR SIRENE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-005 WELDING WIRE', '3.13-005 WELDING WIRE', '3.13-005 WELDING WIRE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-005 WELDING WIRE' AND d.name = '3.13-005 WELDING WIRE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-006 PIPE', '3.13-006 PIPE', '3.13-006 PIPE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-006 PIPE' AND d.name = '3.13-006 PIPE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-007 RUBBER', '3.13-007 RUBBER', '3.13-007 RUBBER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-007 RUBBER' AND d.name = '3.13-007 RUBBER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-008 ASPAL', '3.13-008 ASPAL', '3.13-008 ASPAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-008 ASPAL' AND d.name = '3.13-008 ASPAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-009 KERTAS PASIR', '3.13-009 KERTAS PASIR', '3.13-009 KERTAS PASIR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-009 KERTAS PASIR' AND d.name = '3.13-009 KERTAS PASIR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-010 BATU / MATA GRINDER', '3.13-010 BATU / MATA GRINDER', '3.13-010 BATU / MATA GRINDER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-010 BATU / MATA GRINDER' AND d.name = '3.13-010 BATU / MATA GRINDER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-011 SOCKET', '3.13-011 SOCKET', '3.13-011 SOCKET', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-011 SOCKET' AND d.name = '3.13-011 SOCKET');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-012 KLINGERIT', '3.13-012 KLINGERIT', '3.13-012 KLINGERIT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-012 KLINGERIT' AND d.name = '3.13-012 KLINGERIT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-013 REDUCER', '3.13-013 REDUCER', '3.13-013 REDUCER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-013 REDUCER' AND d.name = '3.13-013 REDUCER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-014 NOZZLE', '3.13-014 NOZZLE', '3.13-014 NOZZLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-014 NOZZLE' AND d.name = '3.13-014 NOZZLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-015 SLING', '3.13-015 SLING', '3.13-015 SLING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-015 SLING' AND d.name = '3.13-015 SLING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-016 TOOL HOLDER', '3.13-016 TOOL HOLDER', '3.13-016 TOOL HOLDER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-016 TOOL HOLDER' AND d.name = '3.13-016 TOOL HOLDER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-017 TEE', '3.13-017 TEE', '3.13-017 TEE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-017 TEE' AND d.name = '3.13-017 TEE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-018 KLEM', '3.13-018 KLEM', '3.13-018 KLEM', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-018 KLEM' AND d.name = '3.13-018 KLEM');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-019 DRILL', '3.13-019 DRILL', '3.13-019 DRILL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-019 DRILL' AND d.name = '3.13-019 DRILL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-020 TOOL BOARD', '3.13-020 TOOL BOARD', '3.13-020 TOOL BOARD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-020 TOOL BOARD' AND d.name = '3.13-020 TOOL BOARD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-021 CAT / SEALER', '3.13-021 CAT / SEALER', '3.13-021 CAT / SEALER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-021 CAT / SEALER' AND d.name = '3.13-021 CAT / SEALER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-022 STOPPER RODS', '3.13-022 STOPPER RODS', '3.13-022 STOPPER RODS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-022 STOPPER RODS' AND d.name = '3.13-022 STOPPER RODS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-023 MASTER LINK', '3.13-023 MASTER LINK', '3.13-023 MASTER LINK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-023 MASTER LINK' AND d.name = '3.13-023 MASTER LINK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-024 COATING', '3.13-024 COATING', '3.13-024 COATING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-024 COATING' AND d.name = '3.13-024 COATING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-025 PAKU', '3.13-025 PAKU', '3.13-025 PAKU', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-025 PAKU' AND d.name = '3.13-025 PAKU');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-026 SHIM PLATE', '3.13-026 SHIM PLATE', '3.13-026 SHIM PLATE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-026 SHIM PLATE' AND d.name = '3.13-026 SHIM PLATE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-027 CLEANER', '3.13-027 CLEANER', '3.13-027 CLEANER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-027 CLEANER' AND d.name = '3.13-027 CLEANER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-028 KAWAT EMAIL DRAT', '3.13-028 KAWAT EMAIL DRAT', '3.13-028 KAWAT EMAIL DRAT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-028 KAWAT EMAIL DRAT' AND d.name = '3.13-028 KAWAT EMAIL DRAT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-029 HEAT SHRINK / SELONGSONG KABEL', '3.13-029 HEAT SHRINK / SELONGSONG KABEL', '3.13-029 HEAT SHRINK / SELONGSONG KABEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-029 HEAT SHRINK / SELONGSONG KABEL' AND d.name = '3.13-029 HEAT SHRINK / SELONGSONG KABEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-030 DOP', '3.13-030 DOP', '3.13-030 DOP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-030 DOP' AND d.name = '3.13-030 DOP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-031 GEMBOK', '3.13-031 GEMBOK', '3.13-031 GEMBOK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-031 GEMBOK' AND d.name = '3.13-031 GEMBOK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-032 WIRE ROPE', '3.13-032 WIRE ROPE', '3.13-032 WIRE ROPE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-032 WIRE ROPE' AND d.name = '3.13-032 WIRE ROPE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-033 FISHER', '3.13-033 FISHER', '3.13-033 FISHER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-033 FISHER' AND d.name = '3.13-033 FISHER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-034 TALI', '3.13-034 TALI', '3.13-034 TALI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-034 TALI' AND d.name = '3.13-034 TALI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-035 PLATE', '3.13-035 PLATE', '3.13-035 PLATE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-035 PLATE' AND d.name = '3.13-035 PLATE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-036 MATA PAHAT', '3.13-036 MATA PAHAT', '3.13-036 MATA PAHAT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-036 MATA PAHAT' AND d.name = '3.13-036 MATA PAHAT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-037 TOMBOL / KNOB', '3.13-037 TOMBOL / KNOB', '3.13-037 TOMBOL / KNOB', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-037 TOMBOL / KNOB' AND d.name = '3.13-037 TOMBOL / KNOB');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-038 SARUNG EMAIL', '3.13-038 SARUNG EMAIL', '3.13-038 SARUNG EMAIL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-038 SARUNG EMAIL' AND d.name = '3.13-038 SARUNG EMAIL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-039 KAWAT AYAKAN', '3.13-039 KAWAT AYAKAN', '3.13-039 KAWAT AYAKAN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-039 KAWAT AYAKAN' AND d.name = '3.13-039 KAWAT AYAKAN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-040 PROBE', '3.13-040 PROBE', '3.13-040 PROBE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-040 PROBE' AND d.name = '3.13-040 PROBE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-041 GRIT', '3.13-041 GRIT', '3.13-041 GRIT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-041 GRIT' AND d.name = '3.13-041 GRIT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-042 DUCT', '3.13-042 DUCT', '3.13-042 DUCT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-042 DUCT' AND d.name = '3.13-042 DUCT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-043 FILLING / FILLER', '3.13-043 FILLING / FILLER', '3.13-043 FILLING / FILLER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-043 FILLING / FILLER' AND d.name = '3.13-043 FILLING / FILLER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-044 ....', '3.13-044 ....', '3.13-044 ....', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-044 ....' AND d.name = '3.13-044 ....');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13-999 GENERAL', '3.13-999 GENERAL', '3.13-999 GENERAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13-999 GENERAL' AND d.name = '3.13-999 GENERAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.13. Consumable', '3.13. Consumable', '3.13. Consumable', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.13. Consumable' AND d.name = '3.13. Consumable');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-001 BREAKER', '3.14-001 BREAKER', '3.14-001 BREAKER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-001 BREAKER' AND d.name = '3.14-001 BREAKER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-002 FUSE / SEKRING', '3.14-002 FUSE / SEKRING', '3.14-002 FUSE / SEKRING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-002 FUSE / SEKRING' AND d.name = '3.14-002 FUSE / SEKRING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-003 CAPASITOR', '3.14-003 CAPASITOR', '3.14-003 CAPASITOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-003 CAPASITOR' AND d.name = '3.14-003 CAPASITOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-004 SWITCH / SAKLAR', '3.14-004 SWITCH / SAKLAR', '3.14-004 SWITCH / SAKLAR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-004 SWITCH / SAKLAR' AND d.name = '3.14-004 SWITCH / SAKLAR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-006 COVER', '3.14-006 COVER', '3.14-006 COVER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-006 COVER' AND d.name = '3.14-006 COVER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-007 CABLE', '3.14-007 CABLE', '3.14-007 CABLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-007 CABLE' AND d.name = '3.14-007 CABLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-008 INSULATOR / ISOLATOR', '3.14-008 INSULATOR / ISOLATOR', '3.14-008 INSULATOR / ISOLATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-008 INSULATOR / ISOLATOR' AND d.name = '3.14-008 INSULATOR / ISOLATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-009 CONDUIT', '3.14-009 CONDUIT', '3.14-009 CONDUIT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-009 CONDUIT' AND d.name = '3.14-009 CONDUIT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-010 SOCKET/STOP KONTAK', '3.14-010 SOCKET/STOP KONTAK', '3.14-010 SOCKET/STOP KONTAK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-010 SOCKET/STOP KONTAK' AND d.name = '3.14-010 SOCKET/STOP KONTAK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-011 BOX PANEL', '3.14-011 BOX PANEL', '3.14-011 BOX PANEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-011 BOX PANEL' AND d.name = '3.14-011 BOX PANEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-012 BUSBAR', '3.14-012 BUSBAR', '3.14-012 BUSBAR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-012 BUSBAR' AND d.name = '3.14-012 BUSBAR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-013 RESISTOR', '3.14-013 RESISTOR', '3.14-013 RESISTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-013 RESISTOR' AND d.name = '3.14-013 RESISTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-014 PLUG', '3.14-014 PLUG', '3.14-014 PLUG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-014 PLUG' AND d.name = '3.14-014 PLUG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-015 MAGNETIC CONTACTOR', '3.14-015 MAGNETIC CONTACTOR', '3.14-015 MAGNETIC CONTACTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-015 MAGNETIC CONTACTOR' AND d.name = '3.14-015 MAGNETIC CONTACTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-016 INVERTER', '3.14-016 INVERTER', '3.14-016 INVERTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-016 INVERTER' AND d.name = '3.14-016 INVERTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-017 SKUN', '3.14-017 SKUN', '3.14-017 SKUN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-017 SKUN' AND d.name = '3.14-017 SKUN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-018 CONNECTOR', '3.14-018 CONNECTOR', '3.14-018 CONNECTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-018 CONNECTOR' AND d.name = '3.14-018 CONNECTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-019 MODULE', '3.14-019 MODULE', '3.14-019 MODULE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-019 MODULE' AND d.name = '3.14-019 MODULE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-020 SOLENOID', '3.14-020 SOLENOID', '3.14-020 SOLENOID', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-020 SOLENOID' AND d.name = '3.14-020 SOLENOID');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-021 STABILIZER', '3.14-021 STABILIZER', '3.14-021 STABILIZER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-021 STABILIZER' AND d.name = '3.14-021 STABILIZER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-022 POWER SUPPLY', '3.14-022 POWER SUPPLY', '3.14-022 POWER SUPPLY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-022 POWER SUPPLY' AND d.name = '3.14-022 POWER SUPPLY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-033 RELAY', '3.14-033 RELAY', '3.14-033 RELAY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-033 RELAY' AND d.name = '3.14-033 RELAY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-034 HMI', '3.14-034 HMI', '3.14-034 HMI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-034 HMI' AND d.name = '3.14-034 HMI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-035 PLC', '3.14-035 PLC', '3.14-035 PLC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-035 PLC' AND d.name = '3.14-035 PLC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-036 TEMPERATURE CONTROLLER', '3.14-036 TEMPERATURE CONTROLLER', '3.14-036 TEMPERATURE CONTROLLER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-036 TEMPERATURE CONTROLLER' AND d.name = '3.14-036 TEMPERATURE CONTROLLER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-037 LAMP', '3.14-037 LAMP', '3.14-037 LAMP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-037 LAMP' AND d.name = '3.14-037 LAMP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-038 POTENSIOMETER', '3.14-038 POTENSIOMETER', '3.14-038 POTENSIOMETER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-038 POTENSIOMETER' AND d.name = '3.14-038 POTENSIOMETER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-039 MCB', '3.14-039 MCB', '3.14-039 MCB', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-039 MCB' AND d.name = '3.14-039 MCB');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-040 SCR', '3.14-040 SCR', '3.14-040 SCR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-040 SCR' AND d.name = '3.14-040 SCR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-041 TRANSDUCER', '3.14-041 TRANSDUCER', '3.14-041 TRANSDUCER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-041 TRANSDUCER' AND d.name = '3.14-041 TRANSDUCER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-042 MCCB', '3.14-042 MCCB', '3.14-042 MCCB', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-042 MCCB' AND d.name = '3.14-042 MCCB');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-043 UPS', '3.14-043 UPS', '3.14-043 UPS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-043 UPS' AND d.name = '3.14-043 UPS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-044 HZ / FREQUENCY METER', '3.14-044 HZ / FREQUENCY METER', '3.14-044 HZ / FREQUENCY METER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-044 HZ / FREQUENCY METER' AND d.name = '3.14-044 HZ / FREQUENCY METER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-045 PCB', '3.14-045 PCB', '3.14-045 PCB', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-045 PCB' AND d.name = '3.14-045 PCB');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-046 TRAFO', '3.14-046 TRAFO', '3.14-046 TRAFO', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-046 TRAFO' AND d.name = '3.14-046 TRAFO');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-047 SHUNT', '3.14-047 SHUNT', '3.14-047 SHUNT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-047 SHUNT' AND d.name = '3.14-047 SHUNT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-048 BOARD', '3.14-048 BOARD', '3.14-048 BOARD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-048 BOARD' AND d.name = '3.14-048 BOARD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-049 VARISTOR', '3.14-049 VARISTOR', '3.14-049 VARISTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-049 VARISTOR' AND d.name = '3.14-049 VARISTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-050 REGULATOR', '3.14-050 REGULATOR', '3.14-050 REGULATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-050 REGULATOR' AND d.name = '3.14-050 REGULATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-051 COIL', '3.14-051 COIL', '3.14-051 COIL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-051 COIL' AND d.name = '3.14-051 COIL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-052 VOLT METER', '3.14-052 VOLT METER', '3.14-052 VOLT METER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-052 VOLT METER' AND d.name = '3.14-052 VOLT METER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-053 KW METER / POWER METER', '3.14-053 KW METER / POWER METER', '3.14-053 KW METER / POWER METER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-053 KW METER / POWER METER' AND d.name = '3.14-053 KW METER / POWER METER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-054 AMPERE METER', '3.14-054 AMPERE METER', '3.14-054 AMPERE METER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-054 AMPERE METER' AND d.name = '3.14-054 AMPERE METER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-055 BUZZER', '3.14-055 BUZZER', '3.14-055 BUZZER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-055 BUZZER' AND d.name = '3.14-055 BUZZER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-056 CONTACTOR', '3.14-056 CONTACTOR', '3.14-056 CONTACTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-056 CONTACTOR' AND d.name = '3.14-056 CONTACTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-057 ACB', '3.14-057 ACB', '3.14-057 ACB', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-057 ACB' AND d.name = '3.14-057 ACB');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-059 PANEL METER', '3.14-059 PANEL METER', '3.14-059 PANEL METER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-059 PANEL METER' AND d.name = '3.14-059 PANEL METER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-060 THYRISTOR / TRANSISTOR', '3.14-060 THYRISTOR / TRANSISTOR', '3.14-060 THYRISTOR / TRANSISTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-060 THYRISTOR / TRANSISTOR' AND d.name = '3.14-060 THYRISTOR / TRANSISTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-061 TIMER', '3.14-061 TIMER', '3.14-061 TIMER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-061 TIMER' AND d.name = '3.14-061 TIMER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-062 TERMINAL', '3.14-062 TERMINAL', '3.14-062 TERMINAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-062 TERMINAL' AND d.name = '3.14-062 TERMINAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-063 CONVERTER', '3.14-063 CONVERTER', '3.14-063 CONVERTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-063 CONVERTER' AND d.name = '3.14-063 CONVERTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.14-064 GENERAL', '3.14-064 GENERAL', '3.14-064 GENERAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.14-064 GENERAL' AND d.name = '3.14-064 GENERAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.16-001 Petrol', '3.16-001 Petrol', '3.16-001 Petrol', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.16-001 Petrol' AND d.name = '3.16-001 Petrol');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.16-002 Gas O2', '3.16-002 Gas O2', '3.16-002 Gas O2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.16-002 Gas O2' AND d.name = '3.16-002 Gas O2');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.16-003 Gas CO2', '3.16-003 Gas CO2', '3.16-003 Gas CO2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.16-003 Gas CO2' AND d.name = '3.16-003 Gas CO2');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.16-004 Natural Gas', '3.16-004 Natural Gas', '3.16-004 Natural Gas', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.16-004 Natural Gas' AND d.name = '3.16-004 Natural Gas');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.16-999 Other Fuel', '3.16-999 Other Fuel', '3.16-999 Other Fuel', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.16-999 Other Fuel' AND d.name = '3.16-999 Other Fuel');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.17-001 SEMBAKO', '3.17-001 SEMBAKO', '3.17-001 SEMBAKO', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.17-001 SEMBAKO' AND d.name = '3.17-001 SEMBAKO');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.17-002 GENERAL', '3.17-002 GENERAL', '3.17-002 GENERAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.17-002 GENERAL' AND d.name = '3.17-002 GENERAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-001 CPU', '3.18-001 CPU', '3.18-001 CPU', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-001 CPU' AND d.name = '3.18-001 CPU');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-002 LAPTOP', '3.18-002 LAPTOP', '3.18-002 LAPTOP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-002 LAPTOP' AND d.name = '3.18-002 LAPTOP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-003 MONITOR', '3.18-003 MONITOR', '3.18-003 MONITOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-003 MONITOR' AND d.name = '3.18-003 MONITOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-004 PRINTER', '3.18-004 PRINTER', '3.18-004 PRINTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-004 PRINTER' AND d.name = '3.18-004 PRINTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-005 SCANNER', '3.18-005 SCANNER', '3.18-005 SCANNER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-005 SCANNER' AND d.name = '3.18-005 SCANNER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-006 TOOLS', '3.18-006 TOOLS', '3.18-006 TOOLS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-006 TOOLS' AND d.name = '3.18-006 TOOLS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-007 MOTHERBOARD', '3.18-007 MOTHERBOARD', '3.18-007 MOTHERBOARD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-007 MOTHERBOARD' AND d.name = '3.18-007 MOTHERBOARD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-008 NETWORKING', '3.18-008 NETWORKING', '3.18-008 NETWORKING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-008 NETWORKING' AND d.name = '3.18-008 NETWORKING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-009 MEMORY', '3.18-009 MEMORY', '3.18-009 MEMORY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-009 MEMORY' AND d.name = '3.18-009 MEMORY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-010 HARDDISK', '3.18-010 HARDDISK', '3.18-010 HARDDISK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-010 HARDDISK' AND d.name = '3.18-010 HARDDISK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-011 POWER SUPPLY', '3.18-011 POWER SUPPLY', '3.18-011 POWER SUPPLY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-011 POWER SUPPLY' AND d.name = '3.18-011 POWER SUPPLY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-012 SERVER', '3.18-012 SERVER', '3.18-012 SERVER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-012 SERVER' AND d.name = '3.18-012 SERVER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-013 CCTV', '3.18-013 CCTV', '3.18-013 CCTV', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-013 CCTV' AND d.name = '3.18-013 CCTV');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-014 UPS', '3.18-014 UPS', '3.18-014 UPS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-014 UPS' AND d.name = '3.18-014 UPS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-015 KABEL IT', '3.18-015 KABEL IT', '3.18-015 KABEL IT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-015 KABEL IT' AND d.name = '3.18-015 KABEL IT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-016 ACCESSORIES', '3.18-016 ACCESSORIES', '3.18-016 ACCESSORIES', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-016 ACCESSORIES' AND d.name = '3.18-016 ACCESSORIES');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-017 CONVERTER', '3.18-017 CONVERTER', '3.18-017 CONVERTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-017 CONVERTER' AND d.name = '3.18-017 CONVERTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-018 SPLITTER', '3.18-018 SPLITTER', '3.18-018 SPLITTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-018 SPLITTER' AND d.name = '3.18-018 SPLITTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-019 DASH CAMERA', '3.18-019 DASH CAMERA', '3.18-019 DASH CAMERA', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-019 DASH CAMERA' AND d.name = '3.18-019 DASH CAMERA');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.18-020 PROYEKTOR', '3.18-020 PROYEKTOR', '3.18-020 PROYEKTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.18-020 PROYEKTOR' AND d.name = '3.18-020 PROYEKTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.20-002 Obat Luar', '3.20-002 Obat Luar', '3.20-002 Obat Luar', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.20-002 Obat Luar' AND d.name = '3.20-002 Obat Luar');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.20-003 Health Equipment', '3.20-003 Health Equipment', '3.20-003 Health Equipment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.20-003 Health Equipment' AND d.name = '3.20-003 Health Equipment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.20-004 General', '3.20-004 General', '3.20-004 General', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.20-004 General' AND d.name = '3.20-004 General');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.21-001 Desk & Chair', '3.21-001 Desk & Chair', '3.21-001 Desk & Chair', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.21-001 Desk & Chair' AND d.name = '3.21-001 Desk & Chair');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.21-002 Furniture', '3.21-002 Furniture', '3.21-002 Furniture', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.21-002 Furniture' AND d.name = '3.21-002 Furniture');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.21-003 Accessories', '3.21-003 Accessories', '3.21-003 Accessories', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.21-003 Accessories' AND d.name = '3.21-003 Accessories');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.21-004 Cabinet', '3.21-004 Cabinet', '3.21-004 Cabinet', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.21-004 Cabinet' AND d.name = '3.21-004 Cabinet');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.21-005 Electronic', '3.21-005 Electronic', '3.21-005 Electronic', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.21-005 Electronic' AND d.name = '3.21-005 Electronic');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-001 BOOK / BINDING / MAP', '3.22-001 BOOK / BINDING / MAP', '3.22-001 BOOK / BINDING / MAP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-001 BOOK / BINDING / MAP' AND d.name = '3.22-001 BOOK / BINDING / MAP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-002 FORM', '3.22-002 FORM', '3.22-002 FORM', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-002 FORM' AND d.name = '3.22-002 FORM');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-003 PAPER', '3.22-003 PAPER', '3.22-003 PAPER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-003 PAPER' AND d.name = '3.22-003 PAPER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-004 ATK', '3.22-004 ATK', '3.22-004 ATK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-004 ATK' AND d.name = '3.22-004 ATK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-005 STAPLES', '3.22-005 STAPLES', '3.22-005 STAPLES', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-005 STAPLES' AND d.name = '3.22-005 STAPLES');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-006 GENERAL', '3.22-006 GENERAL', '3.22-006 GENERAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-006 GENERAL' AND d.name = '3.22-006 GENERAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-007 Amplop', '3.22-007 Amplop', '3.22-007 Amplop', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-007 Amplop' AND d.name = '3.22-007 Amplop');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-008 TINTA/TONER', '3.22-008 TINTA/TONER', '3.22-008 TINTA/TONER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-008 TINTA/TONER' AND d.name = '3.22-008 TINTA/TONER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-009 Battery', '3.22-009 Battery', '3.22-009 Battery', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-009 Battery' AND d.name = '3.22-009 Battery');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-010 Sticker', '3.22-010 Sticker', '3.22-010 Sticker', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-010 Sticker' AND d.name = '3.22-010 Sticker');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-011 Isolasiban / Tape', '3.22-011 Isolasiban / Tape', '3.22-011 Isolasiban / Tape', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-011 Isolasiban / Tape' AND d.name = '3.22-011 Isolasiban / Tape');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-012 Plastic', '3.22-012 Plastic', '3.22-012 Plastic', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-012 Plastic' AND d.name = '3.22-012 Plastic');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.22-013 PUNCH / PERFORATOR', '3.22-013 PUNCH / PERFORATOR', '3.22-013 PUNCH / PERFORATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.22-013 PUNCH / PERFORATOR' AND d.name = '3.22-013 PUNCH / PERFORATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.23-001 Grease', '3.23-001 Grease', '3.23-001 Grease', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.23-001 Grease' AND d.name = '3.23-001 Grease');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.23-002 Oli', '3.23-002 Oli', '3.23-002 Oli', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.23-002 Oli' AND d.name = '3.23-002 Oli');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.23-999 Other Oil', '3.23-999 Other Oil', '3.23-999 Other Oil', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.23-999 Other Oil' AND d.name = '3.23-999 Other Oil');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-001 HELMET', '3.24-001 HELMET', '3.24-001 HELMET', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-001 HELMET' AND d.name = '3.24-001 HELMET');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-002 KACAMATA', '3.24-002 KACAMATA', '3.24-002 KACAMATA', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-002 KACAMATA' AND d.name = '3.24-002 KACAMATA');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-003 TOPENG', '3.24-003 TOPENG', '3.24-003 TOPENG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-003 TOPENG' AND d.name = '3.24-003 TOPENG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-004 KACA LAS', '3.24-004 KACA LAS', '3.24-004 KACA LAS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-004 KACA LAS' AND d.name = '3.24-004 KACA LAS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-005 SERAGAM', '3.24-005 SERAGAM', '3.24-005 SERAGAM', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-005 SERAGAM' AND d.name = '3.24-005 SERAGAM');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-006 MASKER', '3.24-006 MASKER', '3.24-006 MASKER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-006 MASKER' AND d.name = '3.24-006 MASKER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-007 SARUNG TANGAN', '3.24-007 SARUNG TANGAN', '3.24-007 SARUNG TANGAN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-007 SARUNG TANGAN' AND d.name = '3.24-007 SARUNG TANGAN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-008 SHOES & BOOT', '3.24-008 SHOES & BOOT', '3.24-008 SHOES & BOOT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-008 SHOES & BOOT' AND d.name = '3.24-008 SHOES & BOOT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-009 EAR PLUG', '3.24-009 EAR PLUG', '3.24-009 EAR PLUG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-009 EAR PLUG' AND d.name = '3.24-009 EAR PLUG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-010 SAFETY BELT', '3.24-010 SAFETY BELT', '3.24-010 SAFETY BELT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-010 SAFETY BELT' AND d.name = '3.24-010 SAFETY BELT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-011 APAR', '3.24-011 APAR', '3.24-011 APAR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-011 APAR' AND d.name = '3.24-011 APAR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-012 CARTRIDGE DUST', '3.24-012 CARTRIDGE DUST', '3.24-012 CARTRIDGE DUST', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-012 CARTRIDGE DUST' AND d.name = '3.24-012 CARTRIDGE DUST');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.24-999 OTHER SAFETY EQUIPMENT', '3.24-999 OTHER SAFETY EQUIPMENT', '3.24-999 OTHER SAFETY EQUIPMENT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.24-999 OTHER SAFETY EQUIPMENT' AND d.name = '3.24-999 OTHER SAFETY EQUIPMENT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-001 ....', '3.25-001 ....', '3.25-001 ....', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-001 ....' AND d.name = '3.25-001 ....');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-002 ADAPTER', '3.25-002 ADAPTER', '3.25-002 ADAPTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-002 ADAPTER' AND d.name = '3.25-002 ADAPTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-003 ADJUSTING INSTRUMENT', '3.25-003 ADJUSTING INSTRUMENT', '3.25-003 ADJUSTING INSTRUMENT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-003 ADJUSTING INSTRUMENT' AND d.name = '3.25-003 ADJUSTING INSTRUMENT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-004 AIR CHUCK', '3.25-004 AIR CHUCK', '3.25-004 AIR CHUCK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-004 AIR CHUCK' AND d.name = '3.25-004 AIR CHUCK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-005 AIR CONDITIONER', '3.25-005 AIR CONDITIONER', '3.25-005 AIR CONDITIONER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-005 AIR CONDITIONER' AND d.name = '3.25-005 AIR CONDITIONER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-006 AUXILIARY', '3.25-006 AUXILIARY', '3.25-006 AUXILIARY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-006 AUXILIARY' AND d.name = '3.25-006 AUXILIARY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-008 ARMATURE', '3.25-008 ARMATURE', '3.25-008 ARMATURE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-008 ARMATURE' AND d.name = '3.25-008 ARMATURE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-009 AXLE', '3.25-009 AXLE', '3.25-009 AXLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-009 AXLE' AND d.name = '3.25-009 AXLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-012 BEARING', '3.25-012 BEARING', '3.25-012 BEARING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-012 BEARING' AND d.name = '3.25-012 BEARING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-013 BELTING', '3.25-013 BELTING', '3.25-013 BELTING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-013 BELTING' AND d.name = '3.25-013 BELTING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-015 BOHEL', '3.25-015 BOHEL', '3.25-015 BOHEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-015 BOHEL' AND d.name = '3.25-015 BOHEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-017 BOX FUSE', '3.25-017 BOX FUSE', '3.25-017 BOX FUSE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-017 BOX FUSE' AND d.name = '3.25-017 BOX FUSE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-018 BRACKET', '3.25-018 BRACKET', '3.25-018 BRACKET', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-018 BRACKET' AND d.name = '3.25-018 BRACKET');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-019 BRAKE', '3.25-019 BRAKE', '3.25-019 BRAKE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-019 BRAKE' AND d.name = '3.25-019 BRAKE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-020 BRUSH', '3.25-020 BRUSH', '3.25-020 BRUSH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-020 BRUSH' AND d.name = '3.25-020 BRUSH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-021 BUCKET TOOTH', '3.25-021 BUCKET TOOTH', '3.25-021 BUCKET TOOTH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-021 BUCKET TOOTH' AND d.name = '3.25-021 BUCKET TOOTH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-022 BUSHING', '3.25-022 BUSHING', '3.25-022 BUSHING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-022 BUSHING' AND d.name = '3.25-022 BUSHING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-024 CAP / TUTUP', '3.25-024 CAP / TUTUP', '3.25-024 CAP / TUTUP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-024 CAP / TUTUP' AND d.name = '3.25-024 CAP / TUTUP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-025 CASING', '3.25-025 CASING', '3.25-025 CASING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-025 CASING' AND d.name = '3.25-025 CASING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-026 CHARGER', '3.25-026 CHARGER', '3.25-026 CHARGER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-026 CHARGER' AND d.name = '3.25-026 CHARGER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-027 CHUCK', '3.25-027 CHUCK', '3.25-027 CHUCK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-027 CHUCK' AND d.name = '3.25-027 CHUCK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-028 COMMUNICATION', '3.25-028 COMMUNICATION', '3.25-028 COMMUNICATION', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-028 COMMUNICATION' AND d.name = '3.25-028 COMMUNICATION');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-029 CLAMP', '3.25-029 CLAMP', '3.25-029 CLAMP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-029 CLAMP' AND d.name = '3.25-029 CLAMP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-030 CLIP', '3.25-030 CLIP', '3.25-030 CLIP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-030 CLIP' AND d.name = '3.25-030 CLIP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-032 COIL', '3.25-032 COIL', '3.25-032 COIL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-032 COIL' AND d.name = '3.25-032 COIL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-033 CONNECTOR', '3.25-033 CONNECTOR', '3.25-033 CONNECTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-033 CONNECTOR' AND d.name = '3.25-033 CONNECTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-034 CONTROLLER', '3.25-034 CONTROLLER', '3.25-034 CONTROLLER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-034 CONTROLLER' AND d.name = '3.25-034 CONTROLLER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-035 COOLER', '3.25-035 COOLER', '3.25-035 COOLER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-035 COOLER' AND d.name = '3.25-035 COOLER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-036 COUNTER', '3.25-036 COUNTER', '3.25-036 COUNTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-036 COUNTER' AND d.name = '3.25-036 COUNTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-037 COUPLING', '3.25-037 COUPLING', '3.25-037 COUPLING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-037 COUPLING' AND d.name = '3.25-037 COUPLING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-038 COVER', '3.25-038 COVER', '3.25-038 COVER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-038 COVER' AND d.name = '3.25-038 COVER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-039 CYLINDER', '3.25-039 CYLINDER', '3.25-039 CYLINDER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-039 CYLINDER' AND d.name = '3.25-039 CYLINDER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-040 DAUN KIPAS', '3.25-040 DAUN KIPAS', '3.25-040 DAUN KIPAS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-040 DAUN KIPAS' AND d.name = '3.25-040 DAUN KIPAS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-041 DIAPRAGM', '3.25-041 DIAPRAGM', '3.25-041 DIAPRAGM', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-041 DIAPRAGM' AND d.name = '3.25-041 DIAPRAGM');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-043 DRAIN', '3.25-043 DRAIN', '3.25-043 DRAIN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-043 DRAIN' AND d.name = '3.25-043 DRAIN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-046 ELECTRO MOTOR', '3.25-046 ELECTRO MOTOR', '3.25-046 ELECTRO MOTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-046 ELECTRO MOTOR' AND d.name = '3.25-046 ELECTRO MOTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-047 ENGINE', '3.25-047 ENGINE', '3.25-047 ENGINE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-047 ENGINE' AND d.name = '3.25-047 ENGINE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-048 FAN', '3.25-048 FAN', '3.25-048 FAN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-048 FAN' AND d.name = '3.25-048 FAN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-049 FASTENER', '3.25-049 FASTENER', '3.25-049 FASTENER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-049 FASTENER' AND d.name = '3.25-049 FASTENER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-050 FILTER', '3.25-050 FILTER', '3.25-050 FILTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-050 FILTER' AND d.name = '3.25-050 FILTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-051 FLIER', '3.25-051 FLIER', '3.25-051 FLIER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-051 FLIER' AND d.name = '3.25-051 FLIER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-052 FORK', '3.25-052 FORK', '3.25-052 FORK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-052 FORK' AND d.name = '3.25-052 FORK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-054 GASKET', '3.25-054 GASKET', '3.25-054 GASKET', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-054 GASKET' AND d.name = '3.25-054 GASKET');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-055 GAUGE', '3.25-055 GAUGE', '3.25-055 GAUGE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-055 GAUGE' AND d.name = '3.25-055 GAUGE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-056 GEAR', '3.25-056 GEAR', '3.25-056 GEAR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-056 GEAR' AND d.name = '3.25-056 GEAR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-057 GEARBOX', '3.25-057 GEARBOX', '3.25-057 GEARBOX', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-057 GEARBOX' AND d.name = '3.25-057 GEARBOX');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-059 GOVERNOR ASSY', '3.25-059 GOVERNOR ASSY', '3.25-059 GOVERNOR ASSY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-059 GOVERNOR ASSY' AND d.name = '3.25-059 GOVERNOR ASSY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-061 GUARD', '3.25-061 GUARD', '3.25-061 GUARD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-061 GUARD' AND d.name = '3.25-061 GUARD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-062 HANDLE', '3.25-062 HANDLE', '3.25-062 HANDLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-062 HANDLE' AND d.name = '3.25-062 HANDLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-063 HANGING PLATE', '3.25-063 HANGING PLATE', '3.25-063 HANGING PLATE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-063 HANGING PLATE' AND d.name = '3.25-063 HANGING PLATE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-064 GRID', '3.25-064 GRID', '3.25-064 GRID', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-064 GRID' AND d.name = '3.25-064 GRID');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-065 HEATER', '3.25-065 HEATER', '3.25-065 HEATER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-065 HEATER' AND d.name = '3.25-065 HEATER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-066 HOOK', '3.25-066 HOOK', '3.25-066 HOOK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-066 HOOK' AND d.name = '3.25-066 HOOK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-067 HORN', '3.25-067 HORN', '3.25-067 HORN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-067 HORN' AND d.name = '3.25-067 HORN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-068 HOSE / SELANG', '3.25-068 HOSE / SELANG', '3.25-068 HOSE / SELANG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-068 HOSE / SELANG' AND d.name = '3.25-068 HOSE / SELANG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-069 HYDROLIC MOTOR', '3.25-069 HYDROLIC MOTOR', '3.25-069 HYDROLIC MOTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-069 HYDROLIC MOTOR' AND d.name = '3.25-069 HYDROLIC MOTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-070 HINGE / ENGSEL', '3.25-070 HINGE / ENGSEL', '3.25-070 HINGE / ENGSEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-070 HINGE / ENGSEL' AND d.name = '3.25-070 HINGE / ENGSEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-071 IMPELLER / EXPELLER', '3.25-071 IMPELLER / EXPELLER', '3.25-071 IMPELLER / EXPELLER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-071 IMPELLER / EXPELLER' AND d.name = '3.25-071 IMPELLER / EXPELLER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-072 INDICATOR', '3.25-072 INDICATOR', '3.25-072 INDICATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-072 INDICATOR' AND d.name = '3.25-072 INDICATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-073 JOINT / JOINER', '3.25-073 JOINT / JOINER', '3.25-073 JOINT / JOINER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-073 JOINT / JOINER' AND d.name = '3.25-073 JOINT / JOINER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-074 LEVER', '3.25-074 LEVER', '3.25-074 LEVER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-074 LEVER' AND d.name = '3.25-074 LEVER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-075 KEY / KIT', '3.25-075 KEY / KIT', '3.25-075 KEY / KIT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-075 KEY / KIT' AND d.name = '3.25-075 KEY / KIT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-076 LAMP VEHICLE', '3.25-076 LAMP VEHICLE', '3.25-076 LAMP VEHICLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-076 LAMP VEHICLE' AND d.name = '3.25-076 LAMP VEHICLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-077 LATCH', '3.25-077 LATCH', '3.25-077 LATCH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-077 LATCH' AND d.name = '3.25-077 LATCH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-078 LINER', '3.25-078 LINER', '3.25-078 LINER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-078 LINER' AND d.name = '3.25-078 LINER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-079 LINK', '3.25-079 LINK', '3.25-079 LINK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-079 LINK' AND d.name = '3.25-079 LINK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-080 LOCK', '3.25-080 LOCK', '3.25-080 LOCK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-080 LOCK' AND d.name = '3.25-080 LOCK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-081 LUBRICATOR', '3.25-081 LUBRICATOR', '3.25-081 LUBRICATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-081 LUBRICATOR' AND d.name = '3.25-081 LUBRICATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-083 METALAN', '3.25-083 METALAN', '3.25-083 METALAN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-083 METALAN' AND d.name = '3.25-083 METALAN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-084 MIRROR / GLASS', '3.25-084 MIRROR / GLASS', '3.25-084 MIRROR / GLASS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-084 MIRROR / GLASS' AND d.name = '3.25-084 MIRROR / GLASS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-085 MUFFLER', '3.25-085 MUFFLER', '3.25-085 MUFFLER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-085 MUFFLER' AND d.name = '3.25-085 MUFFLER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-086 NEPPLE/NIPPLE', '3.25-086 NEPPLE/NIPPLE', '3.25-086 NEPPLE/NIPPLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-086 NEPPLE/NIPPLE' AND d.name = '3.25-086 NEPPLE/NIPPLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-087 O-RING', '3.25-087 O-RING', '3.25-087 O-RING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-087 O-RING' AND d.name = '3.25-087 O-RING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-088 PACKING', '3.25-088 PACKING', '3.25-088 PACKING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-088 PACKING' AND d.name = '3.25-088 PACKING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-089 PEDAL', '3.25-089 PEDAL', '3.25-089 PEDAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-089 PEDAL' AND d.name = '3.25-089 PEDAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-090 PIN', '3.25-090 PIN', '3.25-090 PIN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-090 PIN' AND d.name = '3.25-090 PIN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-091 PISTON', '3.25-091 PISTON', '3.25-091 PISTON', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-091 PISTON' AND d.name = '3.25-091 PISTON');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-092 PLATE', '3.25-092 PLATE', '3.25-092 PLATE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-092 PLATE' AND d.name = '3.25-092 PLATE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-093 PLUG', '3.25-093 PLUG', '3.25-093 PLUG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-093 PLUG' AND d.name = '3.25-093 PLUG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-094 PNEUMATIC', '3.25-094 PNEUMATIC', '3.25-094 PNEUMATIC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-094 PNEUMATIC' AND d.name = '3.25-094 PNEUMATIC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-095 PULLEY', '3.25-095 PULLEY', '3.25-095 PULLEY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-095 PULLEY' AND d.name = '3.25-095 PULLEY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-096 PUMP', '3.25-096 PUMP', '3.25-096 PUMP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-096 PUMP' AND d.name = '3.25-096 PUMP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-097 PUSHER', '3.25-097 PUSHER', '3.25-097 PUSHER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-097 PUSHER' AND d.name = '3.25-097 PUSHER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-098 RADIATOR', '3.25-098 RADIATOR', '3.25-098 RADIATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-098 RADIATOR' AND d.name = '3.25-098 RADIATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-099 INDUCTION MOTOR', '3.25-099 INDUCTION MOTOR', '3.25-099 INDUCTION MOTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-099 INDUCTION MOTOR' AND d.name = '3.25-099 INDUCTION MOTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-100 REDUCER', '3.25-100 REDUCER', '3.25-100 REDUCER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-100 REDUCER' AND d.name = '3.25-100 REDUCER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-101 REGULATOR', '3.25-101 REGULATOR', '3.25-101 REGULATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-101 REGULATOR' AND d.name = '3.25-101 REGULATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-102 RELAY', '3.25-102 RELAY', '3.25-102 RELAY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-102 RELAY' AND d.name = '3.25-102 RELAY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-103 REPAIR KIT', '3.25-103 REPAIR KIT', '3.25-103 REPAIR KIT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-103 REPAIR KIT' AND d.name = '3.25-103 REPAIR KIT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-105 RING', '3.25-105 RING', '3.25-105 RING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-105 RING' AND d.name = '3.25-105 RING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-106 TIE ROD', '3.25-106 TIE ROD', '3.25-106 TIE ROD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-106 TIE ROD' AND d.name = '3.25-106 TIE ROD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-107 ROLLER', '3.25-107 ROLLER', '3.25-107 ROLLER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-107 ROLLER' AND d.name = '3.25-107 ROLLER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-108 PILLOW BLOCK', '3.25-108 PILLOW BLOCK', '3.25-108 PILLOW BLOCK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-108 PILLOW BLOCK' AND d.name = '3.25-108 PILLOW BLOCK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-109 SEAL', '3.25-109 SEAL', '3.25-109 SEAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-109 SEAL' AND d.name = '3.25-109 SEAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-110 SENSOR', '3.25-110 SENSOR', '3.25-110 SENSOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-110 SENSOR' AND d.name = '3.25-110 SENSOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-111 SEPARATOR', '3.25-111 SEPARATOR', '3.25-111 SEPARATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-111 SEPARATOR' AND d.name = '3.25-111 SEPARATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-112 SERVO', '3.25-112 SERVO', '3.25-112 SERVO', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-112 SERVO' AND d.name = '3.25-112 SERVO');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-113 SHAFT', '3.25-113 SHAFT', '3.25-113 SHAFT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-113 SHAFT' AND d.name = '3.25-113 SHAFT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-114 SHEAVE', '3.25-114 SHEAVE', '3.25-114 SHEAVE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-114 SHEAVE' AND d.name = '3.25-114 SHEAVE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-115 SHIM', '3.25-115 SHIM', '3.25-115 SHIM', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-115 SHIM' AND d.name = '3.25-115 SHIM');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-116 SHOE', '3.25-116 SHOE', '3.25-116 SHOE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-116 SHOE' AND d.name = '3.25-116 SHOE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-117 SITTING', '3.25-117 SITTING', '3.25-117 SITTING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-117 SITTING' AND d.name = '3.25-117 SITTING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-118 SNAP RING', '3.25-118 SNAP RING', '3.25-118 SNAP RING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-118 SNAP RING' AND d.name = '3.25-118 SNAP RING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-120 VELG', '3.25-120 VELG', '3.25-120 VELG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-120 VELG' AND d.name = '3.25-120 VELG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-121 SPACER', '3.25-121 SPACER', '3.25-121 SPACER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-121 SPACER' AND d.name = '3.25-121 SPACER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-122 SPINDLE', '3.25-122 SPINDLE', '3.25-122 SPINDLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-122 SPINDLE' AND d.name = '3.25-122 SPINDLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-123 SPRING (PER)', '3.25-123 SPRING (PER)', '3.25-123 SPRING (PER)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-123 SPRING (PER)' AND d.name = '3.25-123 SPRING (PER)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-124 SPROCKET', '3.25-124 SPROCKET', '3.25-124 SPROCKET', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-124 SPROCKET' AND d.name = '3.25-124 SPROCKET');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-125 STRAINER', '3.25-125 STRAINER', '3.25-125 STRAINER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-125 STRAINER' AND d.name = '3.25-125 STRAINER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-126 STARTER', '3.25-126 STARTER', '3.25-126 STARTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-126 STARTER' AND d.name = '3.25-126 STARTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-127 PINION', '3.25-127 PINION', '3.25-127 PINION', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-127 PINION' AND d.name = '3.25-127 PINION');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-129 SWITCH', '3.25-129 SWITCH', '3.25-129 SWITCH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-129 SWITCH' AND d.name = '3.25-129 SWITCH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-130 TIRE / BAN', '3.25-130 TIRE / BAN', '3.25-130 TIRE / BAN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-130 TIRE / BAN' AND d.name = '3.25-130 TIRE / BAN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-131 TRANSMITION', '3.25-131 TRANSMITION', '3.25-131 TRANSMITION', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-131 TRANSMITION' AND d.name = '3.25-131 TRANSMITION');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-132 TUBE', '3.25-132 TUBE', '3.25-132 TUBE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-132 TUBE' AND d.name = '3.25-132 TUBE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-133 DINAMO MOTOR', '3.25-133 DINAMO MOTOR', '3.25-133 DINAMO MOTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-133 DINAMO MOTOR' AND d.name = '3.25-133 DINAMO MOTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-134 VALVE / KLEP', '3.25-134 VALVE / KLEP', '3.25-134 VALVE / KLEP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-134 VALVE / KLEP' AND d.name = '3.25-134 VALVE / KLEP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-135 WASHER', '3.25-135 WASHER', '3.25-135 WASHER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-135 WASHER' AND d.name = '3.25-135 WASHER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-136 WHEEL', '3.25-136 WHEEL', '3.25-136 WHEEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-136 WHEEL' AND d.name = '3.25-136 WHEEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-137 WIPER', '3.25-137 WIPER', '3.25-137 WIPER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-137 WIPER' AND d.name = '3.25-137 WIPER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-138 WRENCH', '3.25-138 WRENCH', '3.25-138 WRENCH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-138 WRENCH' AND d.name = '3.25-138 WRENCH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-139 NOZZLE', '3.25-139 NOZZLE', '3.25-139 NOZZLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-139 NOZZLE' AND d.name = '3.25-139 NOZZLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-140 SHIELD', '3.25-140 SHIELD', '3.25-140 SHIELD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-140 SHIELD' AND d.name = '3.25-140 SHIELD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-141 FITTING', '3.25-141 FITTING', '3.25-141 FITTING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-141 FITTING' AND d.name = '3.25-141 FITTING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-142 HOUSING', '3.25-142 HOUSING', '3.25-142 HOUSING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-142 HOUSING' AND d.name = '3.25-142 HOUSING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-143 FLANGE', '3.25-143 FLANGE', '3.25-143 FLANGE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-143 FLANGE' AND d.name = '3.25-143 FLANGE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-144 PTO', '3.25-144 PTO', '3.25-144 PTO', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-144 PTO' AND d.name = '3.25-144 PTO');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-145 SUPPORTING', '3.25-145 SUPPORTING', '3.25-145 SUPPORTING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-145 SUPPORTING' AND d.name = '3.25-145 SUPPORTING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-146 DISCHARGER', '3.25-146 DISCHARGER', '3.25-146 DISCHARGER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-146 DISCHARGER' AND d.name = '3.25-146 DISCHARGER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-147 BAUT / BOLT', '3.25-147 BAUT / BOLT', '3.25-147 BAUT / BOLT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-147 BAUT / BOLT' AND d.name = '3.25-147 BAUT / BOLT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-148 OIL SEAL', '3.25-148 OIL SEAL', '3.25-148 OIL SEAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-148 OIL SEAL' AND d.name = '3.25-148 OIL SEAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-149 CHAIN', '3.25-149 CHAIN', '3.25-149 CHAIN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-149 CHAIN' AND d.name = '3.25-149 CHAIN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-150 HUB', '3.25-150 HUB', '3.25-150 HUB', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-150 HUB' AND d.name = '3.25-150 HUB');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-151 SAFETY VALVE', '3.25-151 SAFETY VALVE', '3.25-151 SAFETY VALVE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-151 SAFETY VALVE' AND d.name = '3.25-151 SAFETY VALVE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-152 BREATHER', '3.25-152 BREATHER', '3.25-152 BREATHER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-152 BREATHER' AND d.name = '3.25-152 BREATHER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-153 CONTROL VALVE', '3.25-153 CONTROL VALVE', '3.25-153 CONTROL VALVE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-153 CONTROL VALVE' AND d.name = '3.25-153 CONTROL VALVE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-154 NUT / MUR', '3.25-154 NUT / MUR', '3.25-154 NUT / MUR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-154 NUT / MUR' AND d.name = '3.25-154 NUT / MUR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-155 SHOCK', '3.25-155 SHOCK', '3.25-155 SHOCK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-155 SHOCK' AND d.name = '3.25-155 SHOCK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-156 CARRIAGE', '3.25-156 CARRIAGE', '3.25-156 CARRIAGE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-156 CARRIAGE' AND d.name = '3.25-156 CARRIAGE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-157 UPPER', '3.25-157 UPPER', '3.25-157 UPPER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-157 UPPER' AND d.name = '3.25-157 UPPER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-158 REMOTE CONTROLLER', '3.25-158 REMOTE CONTROLLER', '3.25-158 REMOTE CONTROLLER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-158 REMOTE CONTROLLER' AND d.name = '3.25-158 REMOTE CONTROLLER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-159 SCREEN MESH', '3.25-159 SCREEN MESH', '3.25-159 SCREEN MESH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-159 SCREEN MESH' AND d.name = '3.25-159 SCREEN MESH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-160 ROTOR', '3.25-160 ROTOR', '3.25-160 ROTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-160 ROTOR' AND d.name = '3.25-160 ROTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-161 EXTENSIONER', '3.25-161 EXTENSIONER', '3.25-161 EXTENSIONER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-161 EXTENSIONER' AND d.name = '3.25-161 EXTENSIONER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-162 CUSHION', '3.25-162 CUSHION', '3.25-162 CUSHION', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-162 CUSHION' AND d.name = '3.25-162 CUSHION');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-163 STEERING', '3.25-163 STEERING', '3.25-163 STEERING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-163 STEERING' AND d.name = '3.25-163 STEERING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-164 BUCKGUY', '3.25-164 BUCKGUY', '3.25-164 BUCKGUY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-164 BUCKGUY' AND d.name = '3.25-164 BUCKGUY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-165 ACTUATOR', '3.25-165 ACTUATOR', '3.25-165 ACTUATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-165 ACTUATOR' AND d.name = '3.25-165 ACTUATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-166 TANK', '3.25-166 TANK', '3.25-166 TANK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-166 TANK' AND d.name = '3.25-166 TANK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-167 OIL LEVEL', '3.25-167 OIL LEVEL', '3.25-167 OIL LEVEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-167 OIL LEVEL' AND d.name = '3.25-167 OIL LEVEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-168 GLAND', '3.25-168 GLAND', '3.25-168 GLAND', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-168 GLAND' AND d.name = '3.25-168 GLAND');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-169 PIPA (FACTORY VEHICLE)', '3.25-169 PIPA (FACTORY VEHICLE)', '3.25-169 PIPA (FACTORY VEHICLE)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-169 PIPA (FACTORY VEHICLE)' AND d.name = '3.25-169 PIPA (FACTORY VEHICLE)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-170 BATTERY', '3.25-170 BATTERY', '3.25-170 BATTERY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-170 BATTERY' AND d.name = '3.25-170 BATTERY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-171 AIR VENT', '3.25-171 AIR VENT', '3.25-171 AIR VENT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-171 AIR VENT' AND d.name = '3.25-171 AIR VENT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-172 ROUND ROD', '3.25-172 ROUND ROD', '3.25-172 ROUND ROD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-172 ROUND ROD' AND d.name = '3.25-172 ROUND ROD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-173 KLOS', '3.25-173 KLOS', '3.25-173 KLOS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-173 KLOS' AND d.name = '3.25-173 KLOS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-174 GUIDE VANE', '3.25-174 GUIDE VANE', '3.25-174 GUIDE VANE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-174 GUIDE VANE' AND d.name = '3.25-174 GUIDE VANE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-175 AIR DRYER', '3.25-175 AIR DRYER', '3.25-175 AIR DRYER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-175 AIR DRYER' AND d.name = '3.25-175 AIR DRYER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-176 SWIVEL', '3.25-176 SWIVEL', '3.25-176 SWIVEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-176 SWIVEL' AND d.name = '3.25-176 SWIVEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-185 VANES', '3.25-185 VANES', '3.25-185 VANES', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-185 VANES' AND d.name = '3.25-185 VANES');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-186 GEAR MOTOR', '3.25-186 GEAR MOTOR', '3.25-186 GEAR MOTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-186 GEAR MOTOR' AND d.name = '3.25-186 GEAR MOTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-187 KEY / SPIE', '3.25-187 KEY / SPIE', '3.25-187 KEY / SPIE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-187 KEY / SPIE' AND d.name = '3.25-187 KEY / SPIE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-188 COLLAR', '3.25-188 COLLAR', '3.25-188 COLLAR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-188 COLLAR' AND d.name = '3.25-188 COLLAR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-189 SCREW', '3.25-189 SCREW', '3.25-189 SCREW', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-189 SCREW' AND d.name = '3.25-189 SCREW');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-190 AIR MOTOR', '3.25-190 AIR MOTOR', '3.25-190 AIR MOTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-190 AIR MOTOR' AND d.name = '3.25-190 AIR MOTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-191 DIFFUSER PUMP', '3.25-191 DIFFUSER PUMP', '3.25-191 DIFFUSER PUMP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-191 DIFFUSER PUMP' AND d.name = '3.25-191 DIFFUSER PUMP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-192 SLEEVE', '3.25-192 SLEEVE', '3.25-192 SLEEVE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-192 SLEEVE' AND d.name = '3.25-192 SLEEVE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-193 ROD', '3.25-193 ROD', '3.25-193 ROD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-193 ROD' AND d.name = '3.25-193 ROD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-194 NECK', '3.25-194 NECK', '3.25-194 NECK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-194 NECK' AND d.name = '3.25-194 NECK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-195 MOTOR & SWITCH', '3.25-195 MOTOR & SWITCH', '3.25-195 MOTOR & SWITCH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-195 MOTOR & SWITCH' AND d.name = '3.25-195 MOTOR & SWITCH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-196 THERMOSTATIC', '3.25-196 THERMOSTATIC', '3.25-196 THERMOSTATIC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-196 THERMOSTATIC' AND d.name = '3.25-196 THERMOSTATIC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-197 FLOW CONTROL', '3.25-197 FLOW CONTROL', '3.25-197 FLOW CONTROL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-197 FLOW CONTROL' AND d.name = '3.25-197 FLOW CONTROL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-198 BLOWDOWN', '3.25-198 BLOWDOWN', '3.25-198 BLOWDOWN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-198 BLOWDOWN' AND d.name = '3.25-198 BLOWDOWN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-199 ELECTRIFIED BLOCK', '3.25-199 ELECTRIFIED BLOCK', '3.25-199 ELECTRIFIED BLOCK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-199 ELECTRIFIED BLOCK' AND d.name = '3.25-199 ELECTRIFIED BLOCK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-200 STEAM TRAP', '3.25-200 STEAM TRAP', '3.25-200 STEAM TRAP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-200 STEAM TRAP' AND d.name = '3.25-200 STEAM TRAP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-201 BLOCK', '3.25-201 BLOCK', '3.25-201 BLOCK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-201 BLOCK' AND d.name = '3.25-201 BLOCK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-202 HOLDER', '3.25-202 HOLDER', '3.25-202 HOLDER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-202 HOLDER' AND d.name = '3.25-202 HOLDER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-203 BLASTING', '3.25-203 BLASTING', '3.25-203 BLASTING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-203 BLASTING' AND d.name = '3.25-203 BLASTING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-204 DISC', '3.25-204 DISC', '3.25-204 DISC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-204 DISC' AND d.name = '3.25-204 DISC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-205 TERMINAL', '3.25-205 TERMINAL', '3.25-205 TERMINAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-205 TERMINAL' AND d.name = '3.25-205 TERMINAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-206 THERMOCOUPLE', '3.25-206 THERMOCOUPLE', '3.25-206 THERMOCOUPLE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-206 THERMOCOUPLE' AND d.name = '3.25-206 THERMOCOUPLE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-207 FIRE ALARM', '3.25-207 FIRE ALARM', '3.25-207 FIRE ALARM', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-207 FIRE ALARM' AND d.name = '3.25-207 FIRE ALARM');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-208 MOUNTING', '3.25-208 MOUNTING', '3.25-208 MOUNTING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-208 MOUNTING' AND d.name = '3.25-208 MOUNTING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-209 ELECTRODE CNC', '3.25-209 ELECTRODE CNC', '3.25-209 ELECTRODE CNC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-209 ELECTRODE CNC' AND d.name = '3.25-209 ELECTRODE CNC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-210 SHACKLE / SEGEL', '3.25-210 SHACKLE / SEGEL', '3.25-210 SHACKLE / SEGEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-210 SHACKLE / SEGEL' AND d.name = '3.25-210 SHACKLE / SEGEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-211 INDENTER', '3.25-211 INDENTER', '3.25-211 INDENTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-211 INDENTER' AND d.name = '3.25-211 INDENTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-212 RAIL', '3.25-212 RAIL', '3.25-212 RAIL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-212 RAIL' AND d.name = '3.25-212 RAIL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-213 INJECTOR', '3.25-213 INJECTOR', '3.25-213 INJECTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-213 INJECTOR' AND d.name = '3.25-213 INJECTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-214 DASHER', '3.25-214 DASHER', '3.25-214 DASHER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-214 DASHER' AND d.name = '3.25-214 DASHER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-215 FLOAT / PELAMPUNG', '3.25-215 FLOAT / PELAMPUNG', '3.25-215 FLOAT / PELAMPUNG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-215 FLOAT / PELAMPUNG' AND d.name = '3.25-215 FLOAT / PELAMPUNG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-216 TRIGGER', '3.25-216 TRIGGER', '3.25-216 TRIGGER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-216 TRIGGER' AND d.name = '3.25-216 TRIGGER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-217 ARRESTER / ANVIL', '3.25-217 ARRESTER / ANVIL', '3.25-217 ARRESTER / ANVIL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-217 ARRESTER / ANVIL' AND d.name = '3.25-217 ARRESTER / ANVIL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-218 END TROLLEY/MIDDLE TROLEY/CABLE TROLLEY', '3.25-218 END TROLLEY/MIDDLE TROLEY/CABLE TROLLEY', '3.25-218 END TROLLEY/MIDDLE TROLEY/CABLE TROLLEY', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-218 END TROLLEY/MIDDLE TROLEY/CABLE TROLLEY' AND d.name = '3.25-218 END TROLLEY/MIDDLE TROLEY/CABLE TROLLEY');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-219 THERMOSTAT', '3.25-219 THERMOSTAT', '3.25-219 THERMOSTAT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-219 THERMOSTAT' AND d.name = '3.25-219 THERMOSTAT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-220 GUIDE', '3.25-220 GUIDE', '3.25-220 GUIDE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-220 GUIDE' AND d.name = '3.25-220 GUIDE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-221 BUMPER', '3.25-221 BUMPER', '3.25-221 BUMPER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-221 BUMPER' AND d.name = '3.25-221 BUMPER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-222 BARE', '3.25-222 BARE', '3.25-222 BARE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-222 BARE' AND d.name = '3.25-222 BARE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-223 PLUNGER', '3.25-223 PLUNGER', '3.25-223 PLUNGER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-223 PLUNGER' AND d.name = '3.25-223 PLUNGER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-224 MOUTH', '3.25-224 MOUTH', '3.25-224 MOUTH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-224 MOUTH' AND d.name = '3.25-224 MOUTH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-225 DAMPER', '3.25-225 DAMPER', '3.25-225 DAMPER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-225 DAMPER' AND d.name = '3.25-225 DAMPER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-226 CARBON BRUSH', '3.25-226 CARBON BRUSH', '3.25-226 CARBON BRUSH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-226 CARBON BRUSH' AND d.name = '3.25-226 CARBON BRUSH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-227 COMPRESSOR', '3.25-227 COMPRESSOR', '3.25-227 COMPRESSOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-227 COMPRESSOR' AND d.name = '3.25-227 COMPRESSOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-228 STATOR', '3.25-228 STATOR', '3.25-228 STATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-228 STATOR' AND d.name = '3.25-228 STATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-229 CLUTCH', '3.25-229 CLUTCH', '3.25-229 CLUTCH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-229 CLUTCH' AND d.name = '3.25-229 CLUTCH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25-999 Other Part', '3.25-999 Other Part', '3.25-999 Other Part', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25-999 Other Part' AND d.name = '3.25-999 Other Part');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.25. Sparepart', '3.25. Sparepart', '3.25. Sparepart', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.25. Sparepart' AND d.name = '3.25. Sparepart');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-001 BESI ULIR / BESI BETON', '3.26-001 BESI ULIR / BESI BETON', '3.26-001 BESI ULIR / BESI BETON', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-001 BESI ULIR / BESI BETON' AND d.name = '3.26-001 BESI ULIR / BESI BETON');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-003 PIPA HIDROLIK', '3.26-003 PIPA HIDROLIK', '3.26-003 PIPA HIDROLIK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-003 PIPA HIDROLIK' AND d.name = '3.26-003 PIPA HIDROLIK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-004 PIPA STAINLESS STEEL', '3.26-004 PIPA STAINLESS STEEL', '3.26-004 PIPA STAINLESS STEEL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-004 PIPA STAINLESS STEEL' AND d.name = '3.26-004 PIPA STAINLESS STEEL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-005 AS BAR', '3.26-005 AS BAR', '3.26-005 AS BAR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-005 AS BAR' AND d.name = '3.26-005 AS BAR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-006 BESI CNP', '3.26-006 BESI CNP', '3.26-006 BESI CNP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-006 BESI CNP' AND d.name = '3.26-006 BESI CNP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-007 BESI SIKU', '3.26-007 BESI SIKU', '3.26-007 BESI SIKU', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-007 BESI SIKU' AND d.name = '3.26-007 BESI SIKU');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-008 BESI UNP', '3.26-008 BESI UNP', '3.26-008 BESI UNP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-008 BESI UNP' AND d.name = '3.26-008 BESI UNP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-009 PLAT', '3.26-009 PLAT', '3.26-009 PLAT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-009 PLAT' AND d.name = '3.26-009 PLAT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-010 PIPA HOLLOW / HITAM', '3.26-010 PIPA HOLLOW / HITAM', '3.26-010 PIPA HOLLOW / HITAM', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-010 PIPA HOLLOW / HITAM' AND d.name = '3.26-010 PIPA HOLLOW / HITAM');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-011 TIE ROD STANG / BORING', '3.26-011 TIE ROD STANG / BORING', '3.26-011 TIE ROD STANG / BORING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-011 TIE ROD STANG / BORING' AND d.name = '3.26-011 TIE ROD STANG / BORING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-012 PIPA STEAM SEAMLESS', '3.26-012 PIPA STEAM SEAMLESS', '3.26-012 PIPA STEAM SEAMLESS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-012 PIPA STEAM SEAMLESS' AND d.name = '3.26-012 PIPA STEAM SEAMLESS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-013 BESI SPIE/SPI', '3.26-013 BESI SPIE/SPI', '3.26-013 BESI SPIE/SPI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-013 BESI SPIE/SPI' AND d.name = '3.26-013 BESI SPIE/SPI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-014 PIPA CARBON', '3.26-014 PIPA CARBON', '3.26-014 PIPA CARBON', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-014 PIPA CARBON' AND d.name = '3.26-014 PIPA CARBON');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-015 PIPA GALVANIS', '3.26-015 PIPA GALVANIS', '3.26-015 PIPA GALVANIS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-015 PIPA GALVANIS' AND d.name = '3.26-015 PIPA GALVANIS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-016 BESI IWF', '3.26-016 BESI IWF', '3.26-016 BESI IWF', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-016 BESI IWF' AND d.name = '3.26-016 BESI IWF');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-017 PIPA ALUMINIUM', '3.26-017 PIPA ALUMINIUM', '3.26-017 PIPA ALUMINIUM', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-017 PIPA ALUMINIUM' AND d.name = '3.26-017 PIPA ALUMINIUM');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-018 PIPA FASTENER (NOT USE)', '3.26-018 PIPA FASTENER (NOT USE)', '3.26-018 PIPA FASTENER (NOT USE)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-018 PIPA FASTENER (NOT USE)' AND d.name = '3.26-018 PIPA FASTENER (NOT USE)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-019 BESI HNP', '3.26-019 BESI HNP', '3.26-019 BESI HNP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-019 BESI HNP' AND d.name = '3.26-019 BESI HNP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-020 BESI INP', '3.26-020 BESI INP', '3.26-020 BESI INP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-020 BESI INP' AND d.name = '3.26-020 BESI INP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-022 RAIL', '3.26-022 RAIL', '3.26-022 RAIL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-022 RAIL' AND d.name = '3.26-022 RAIL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-023 BESI / PIPA RHS', '3.26-023 BESI / PIPA RHS', '3.26-023 BESI / PIPA RHS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-023 BESI / PIPA RHS' AND d.name = '3.26-023 BESI / PIPA RHS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-024 PLAT EXPANDED METAL', '3.26-024 PLAT EXPANDED METAL', '3.26-024 PLAT EXPANDED METAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-024 PLAT EXPANDED METAL' AND d.name = '3.26-024 PLAT EXPANDED METAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-025 BESI WF', '3.26-025 BESI WF', '3.26-025 BESI WF', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-025 BESI WF' AND d.name = '3.26-025 BESI WF');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.26-027 WIRE ROD', '3.26-027 WIRE ROD', '3.26-027 WIRE ROD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.26-027 WIRE ROD' AND d.name = '3.26-027 WIRE ROD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.27-001 CLEANING TOOL', '3.27-001 CLEANING TOOL', '3.27-001 CLEANING TOOL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.27-001 CLEANING TOOL' AND d.name = '3.27-001 CLEANING TOOL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.27-002 CLEANER LEQUID', '3.27-002 CLEANER LEQUID', '3.27-002 CLEANER LEQUID', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.27-002 CLEANER LEQUID' AND d.name = '3.27-002 CLEANER LEQUID');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.27-003 GENERAL', '3.27-003 GENERAL', '3.27-003 GENERAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.27-003 GENERAL' AND d.name = '3.27-003 GENERAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.27-004 TISSUE', '3.27-004 TISSUE', '3.27-004 TISSUE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.27-004 TISSUE' AND d.name = '3.27-004 TISSUE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.27-005 KITCHEN UTENSILS', '3.27-005 KITCHEN UTENSILS', '3.27-005 KITCHEN UTENSILS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.27-005 KITCHEN UTENSILS' AND d.name = '3.27-005 KITCHEN UTENSILS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.27-006 GARDEN EQUIPMENT', '3.27-006 GARDEN EQUIPMENT', '3.27-006 GARDEN EQUIPMENT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.27-006 GARDEN EQUIPMENT' AND d.name = '3.27-006 GARDEN EQUIPMENT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.27-007 KAIN / KARPET / TIRAI / GORDEN', '3.27-007 KAIN / KARPET / TIRAI / GORDEN', '3.27-007 KAIN / KARPET / TIRAI / GORDEN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.27-007 KAIN / KARPET / TIRAI / GORDEN' AND d.name = '3.27-007 KAIN / KARPET / TIRAI / GORDEN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.27-008 GANTUNGAN', '3.27-008 GANTUNGAN', '3.27-008 GANTUNGAN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.27-008 GANTUNGAN' AND d.name = '3.27-008 GANTUNGAN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.27-009 KERANJANG / TONG', '3.27-009 KERANJANG / TONG', '3.27-009 KERANJANG / TONG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.27-009 KERANJANG / TONG' AND d.name = '3.27-009 KERANJANG / TONG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-001 ELECTRICAL', '3.28-001 ELECTRICAL', '3.28-001 ELECTRICAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-001 ELECTRICAL' AND d.name = '3.28-001 ELECTRICAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-002 CUTTER', '3.28-002 CUTTER', '3.28-002 CUTTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-002 CUTTER' AND d.name = '3.28-002 CUTTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-003 CARBIDE', '3.28-003 CARBIDE', '3.28-003 CARBIDE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-003 CARBIDE' AND d.name = '3.28-003 CARBIDE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-004 GRINDER (GERINDA)', '3.28-004 GRINDER (GERINDA)', '3.28-004 GRINDER (GERINDA)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-004 GRINDER (GERINDA)' AND d.name = '3.28-004 GRINDER (GERINDA)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-005 PUMP', '3.28-005 PUMP', '3.28-005 PUMP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-005 PUMP' AND d.name = '3.28-005 PUMP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-006 SCRAP', '3.28-006 SCRAP', '3.28-006 SCRAP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-006 SCRAP' AND d.name = '3.28-006 SCRAP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-007 PUNCH / MARTIL', '3.28-007 PUNCH / MARTIL', '3.28-007 PUNCH / MARTIL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-007 PUNCH / MARTIL' AND d.name = '3.28-007 PUNCH / MARTIL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-008 BLANK ROLL', '3.28-008 BLANK ROLL', '3.28-008 BLANK ROLL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-008 BLANK ROLL' AND d.name = '3.28-008 BLANK ROLL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-009 TANG', '3.28-009 TANG', '3.28-009 TANG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-009 TANG' AND d.name = '3.28-009 TANG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-010 MESIN DRILL', '3.28-010 MESIN DRILL', '3.28-010 MESIN DRILL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-010 MESIN DRILL' AND d.name = '3.28-010 MESIN DRILL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-011 MACHINE POLISHER', '3.28-011 MACHINE POLISHER', '3.28-011 MACHINE POLISHER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-011 MACHINE POLISHER' AND d.name = '3.28-011 MACHINE POLISHER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-012 HOLDER / BAIS', '3.28-012 HOLDER / BAIS', '3.28-012 HOLDER / BAIS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-012 HOLDER / BAIS' AND d.name = '3.28-012 HOLDER / BAIS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-013 GUN', '3.28-013 GUN', '3.28-013 GUN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-013 GUN' AND d.name = '3.28-013 GUN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-014 COLLET', '3.28-014 COLLET', '3.28-014 COLLET', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-014 COLLET' AND d.name = '3.28-014 COLLET');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-015 KUNCI / WRENCH', '3.28-015 KUNCI / WRENCH', '3.28-015 KUNCI / WRENCH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-015 KUNCI / WRENCH' AND d.name = '3.28-015 KUNCI / WRENCH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-016 PIN & BUSH', '3.28-016 PIN & BUSH', '3.28-016 PIN & BUSH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-016 PIN & BUSH' AND d.name = '3.28-016 PIN & BUSH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-017 CALIPER', '3.28-017 CALIPER', '3.28-017 CALIPER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-017 CALIPER' AND d.name = '3.28-017 CALIPER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-018 PROTECTOR', '3.28-018 PROTECTOR', '3.28-018 PROTECTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-018 PROTECTOR' AND d.name = '3.28-018 PROTECTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-019 ALLEN KEY / KUNCI L', '3.28-019 ALLEN KEY / KUNCI L', '3.28-019 ALLEN KEY / KUNCI L', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-019 ALLEN KEY / KUNCI L' AND d.name = '3.28-019 ALLEN KEY / KUNCI L');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-020 DETECTOR', '3.28-020 DETECTOR', '3.28-020 DETECTOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-020 DETECTOR' AND d.name = '3.28-020 DETECTOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-021 THREAD', '3.28-021 THREAD', '3.28-021 THREAD', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-021 THREAD' AND d.name = '3.28-021 THREAD');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-022 FLASHBACK', '3.28-022 FLASHBACK', '3.28-022 FLASHBACK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-022 FLASHBACK' AND d.name = '3.28-022 FLASHBACK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-023 CALIBRATOR', '3.28-023 CALIBRATOR', '3.28-023 CALIBRATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-023 CALIBRATOR' AND d.name = '3.28-023 CALIBRATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-024 LIFT / CRANE', '3.28-024 LIFT / CRANE', '3.28-024 LIFT / CRANE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-024 LIFT / CRANE' AND d.name = '3.28-024 LIFT / CRANE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-025 FASTENER', '3.28-025 FASTENER', '3.28-025 FASTENER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-025 FASTENER' AND d.name = '3.28-025 FASTENER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-027 OBENG', '3.28-027 OBENG', '3.28-027 OBENG', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-027 OBENG' AND d.name = '3.28-027 OBENG');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-030 ALAT UKUR / MEASURING INSTRUMENT', '3.28-030 ALAT UKUR / MEASURING INSTRUMENT', '3.28-030 ALAT UKUR / MEASURING INSTRUMENT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-030 ALAT UKUR / MEASURING INSTRUMENT' AND d.name = '3.28-030 ALAT UKUR / MEASURING INSTRUMENT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-031 TORCH', '3.28-031 TORCH', '3.28-031 TORCH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-031 TORCH' AND d.name = '3.28-031 TORCH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-032 GANCU', '3.28-032 GANCU', '3.28-032 GANCU', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-032 GANCU' AND d.name = '3.28-032 GANCU');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-034 INSERT & MOULD, PATTERN', '3.28-034 INSERT & MOULD, PATTERN', '3.28-034 INSERT & MOULD, PATTERN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-034 INSERT & MOULD, PATTERN' AND d.name = '3.28-034 INSERT & MOULD, PATTERN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-035 ROD DRYER', '3.28-035 ROD DRYER', '3.28-035 ROD DRYER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-035 ROD DRYER' AND d.name = '3.28-035 ROD DRYER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-036 SNAI / DIES', '3.28-036 SNAI / DIES', '3.28-036 SNAI / DIES', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-036 SNAI / DIES' AND d.name = '3.28-036 SNAI / DIES');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-037 PAINT BRUSH / KUAS', '3.28-037 PAINT BRUSH / KUAS', '3.28-037 PAINT BRUSH / KUAS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-037 PAINT BRUSH / KUAS' AND d.name = '3.28-037 PAINT BRUSH / KUAS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-038 END MILL / BALL NOSE', '3.28-038 END MILL / BALL NOSE', '3.28-038 END MILL / BALL NOSE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-038 END MILL / BALL NOSE' AND d.name = '3.28-038 END MILL / BALL NOSE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-040 BLOWER', '3.28-040 BLOWER', '3.28-040 BLOWER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-040 BLOWER' AND d.name = '3.28-040 BLOWER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-041 GEAR HOB CUTTER', '3.28-041 GEAR HOB CUTTER', '3.28-041 GEAR HOB CUTTER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-041 GEAR HOB CUTTER' AND d.name = '3.28-041 GEAR HOB CUTTER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-042 MESIN / TRAFO LAS', '3.28-042 MESIN / TRAFO LAS', '3.28-042 MESIN / TRAFO LAS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-042 MESIN / TRAFO LAS' AND d.name = '3.28-042 MESIN / TRAFO LAS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-043 VENTILATOR', '3.28-043 VENTILATOR', '3.28-043 VENTILATOR', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-043 VENTILATOR' AND d.name = '3.28-043 VENTILATOR');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-044 SENDOK SEMEN / SEKOP', '3.28-044 SENDOK SEMEN / SEKOP', '3.28-044 SENDOK SEMEN / SEKOP', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-044 SENDOK SEMEN / SEKOP' AND d.name = '3.28-044 SENDOK SEMEN / SEKOP');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-045 LABORATORY EQUIPMENT', '3.28-045 LABORATORY EQUIPMENT', '3.28-045 LABORATORY EQUIPMENT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-045 LABORATORY EQUIPMENT' AND d.name = '3.28-045 LABORATORY EQUIPMENT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-046 SOLDER', '3.28-046 SOLDER', '3.28-046 SOLDER', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-046 SOLDER' AND d.name = '3.28-046 SOLDER');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-047 TROLLY / TOOL CART', '3.28-047 TROLLY / TOOL CART', '3.28-047 TROLLY / TOOL CART', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-047 TROLLY / TOOL CART' AND d.name = '3.28-047 TROLLY / TOOL CART');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-048 FLARING', '3.28-048 FLARING', '3.28-048 FLARING', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-048 FLARING' AND d.name = '3.28-048 FLARING');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-049 TROUGH', '3.28-049 TROUGH', '3.28-049 TROUGH', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-049 TROUGH' AND d.name = '3.28-049 TROUGH');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28-999 GENERAL', '3.28-999 GENERAL', '3.28-999 GENERAL', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28-999 GENERAL' AND d.name = '3.28-999 GENERAL');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.28. Tools', '3.28. Tools', '3.28. Tools', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.28. Tools' AND d.name = '3.28. Tools');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.30-001 BROTI', '3.30-001 BROTI', '3.30-001 BROTI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.30-001 BROTI' AND d.name = '3.30-001 BROTI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.30-002 PAPAN', '3.30-002 PAPAN', '3.30-002 PAPAN', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.30-002 PAPAN' AND d.name = '3.30-002 PAPAN');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.30-003 TRIPLEK', '3.30-003 TRIPLEK', '3.30-003 TRIPLEK', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.30-003 TRIPLEK' AND d.name = '3.30-003 TRIPLEK');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.30-004 KAYU LAT', '3.30-004 KAYU LAT', '3.30-004 KAYU LAT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.30-004 KAYU LAT' AND d.name = '3.30-004 KAYU LAT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.31-001 Bags', '3.31-001 Bags', '3.31-001 Bags', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.31-001 Bags' AND d.name = '3.31-001 Bags');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.31-002 Pallet', '3.31-002 Pallet', '3.31-002 Pallet', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.31-002 Pallet' AND d.name = '3.31-002 Pallet');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.31-009 Other Packaging Material', '3.31-009 Other Packaging Material', '3.31-009 Other Packaging Material', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.31-009 Other Packaging Material' AND d.name = '3.31-009 Other Packaging Material');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.33-001 Tester', '3.33-001 Tester', '3.33-001 Tester', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.33-001 Tester' AND d.name = '3.33-001 Tester');

COMMIT;
-- End of script
