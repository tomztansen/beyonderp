-- SQL Script to insert Excel Lookup Data into dynamic.mhlookup and dynamic.mdlookup

BEGIN;

-- 1. Insert Master Header Lookup (mhlookup)
INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0020', 'Master : Procurement Category', 'Master : Procurement Category', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0020');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0021', 'Master : Sales Category', 'Master : Sales Category', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0021');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0022', 'Master : Warehouse', 'Master : Warehouse', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0022');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0023', 'Master : Location', 'Master : Location', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0023');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0024', 'Master : Tooling Group', 'Master : Tooling Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0024');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0027', 'Master : Asset Group', 'Master : Asset Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0027');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0028', 'Master : Depreciation Group', 'Master : Depreciation Group', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0028');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0029', 'Master : Business Unit', 'Master : Business Unit', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0029');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0030', 'Master : Department', 'Master : Department', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0030');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0031', 'Master : Asset Category', 'Master : Asset Category', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0031');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0032', 'Master : Asset Status', 'Master : Asset Status', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0032');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0033', 'Master : Asset Location', 'Master : Asset Location', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0033');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0038', 'Master : Energy Source', 'Master : Energy Source', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0038');

INSERT INTO dynamic.mhlookup (category_code, category_name, description, inputby, inputdt, status)
SELECT 'MAS0039', 'Master Shift', 'Master Shift', 'admin', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM dynamic.mhlookup WHERE category_code = 'MAS0039');

-- 2. Insert Master Detail Lookup (mdlookup)
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

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.33-002 Recorder', '3.33-002 Recorder', '3.33-002 Recorder', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.33-002 Recorder' AND d.name = '3.33-002 Recorder');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.33-003 Measurer', '3.33-003 Measurer', '3.33-003 Measurer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.33-003 Measurer' AND d.name = '3.33-003 Measurer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.34-001 Marketing', '3.34-001 Marketing', '3.34-001 Marketing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.34-001 Marketing' AND d.name = '3.34-001 Marketing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.35-001 Labour Uniform', '3.35-001 Labour Uniform', '3.35-001 Labour Uniform', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.35-001 Labour Uniform' AND d.name = '3.35-001 Labour Uniform');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.35-002 Staff Uniform', '3.35-002 Staff Uniform', '3.35-002 Staff Uniform', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.35-002 Staff Uniform' AND d.name = '3.35-002 Staff Uniform');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.35-003 Laboratory Uniform', '3.35-003 Laboratory Uniform', '3.35-003 Laboratory Uniform', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.35-003 Laboratory Uniform' AND d.name = '3.35-003 Laboratory Uniform');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.35-004 Security Uniform', '3.35-004 Security Uniform', '3.35-004 Security Uniform', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.35-004 Security Uniform' AND d.name = '3.35-004 Security Uniform');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.01-001 Machine', '4.01-001 Machine', '4.01-001 Machine', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.01-001 Machine' AND d.name = '4.01-001 Machine');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.01. Machine', '4.01. Machine', '4.01. Machine', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.01. Machine' AND d.name = '4.01. Machine');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.02-001 CRANE', '4.02-001 CRANE', '4.02-001 CRANE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.02-001 CRANE' AND d.name = '4.02-001 CRANE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.02-002 INSTRUMENT', '4.02-002 INSTRUMENT', '4.02-002 INSTRUMENT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.02-002 INSTRUMENT' AND d.name = '4.02-002 INSTRUMENT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.02-003 PRODUCTION TOOLS', '4.02-003 PRODUCTION TOOLS', '4.02-003 PRODUCTION TOOLS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.02-003 PRODUCTION TOOLS' AND d.name = '4.02-003 PRODUCTION TOOLS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.02. Tools', '4.02. Tools', '4.02. Tools', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.02. Tools' AND d.name = '4.02. Tools');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.03-001 Froklift', '4.03-001 Froklift', '4.03-001 Froklift', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.03-001 Froklift' AND d.name = '4.03-001 Froklift');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.03-002 Truck', '4.03-002 Truck', '4.03-002 Truck', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.03-002 Truck' AND d.name = '4.03-002 Truck');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.03-003 MPV', '4.03-003 MPV', '4.03-003 MPV', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.03-003 MPV' AND d.name = '4.03-003 MPV');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.03-004 Loader', '4.03-004 Loader', '4.03-004 Loader', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.03-004 Loader' AND d.name = '4.03-004 Loader');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.03-005 Becak', '4.03-005 Becak', '4.03-005 Becak', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.03-005 Becak' AND d.name = '4.03-005 Becak');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5.01-001 Composite Casting GAI', '5.01-001 Composite Casting GAI', '5.01-001 Composite Casting GAI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5.01-001 Composite Casting GAI' AND d.name = '5.01-001 Composite Casting GAI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5.01-002 Composite Casting GAS', '5.01-002 Composite Casting GAS', '5.01-002 Composite Casting GAS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5.01-002 Composite Casting GAS' AND d.name = '5.01-002 Composite Casting GAS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5.01-003 Composite Casting Subcon', '5.01-003 Composite Casting Subcon', '5.01-003 Composite Casting Subcon', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5.01-003 Composite Casting Subcon' AND d.name = '5.01-003 Composite Casting Subcon');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5.01. Composite Casting', '5.01. Composite Casting', '5.01. Composite Casting', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5.01. Composite Casting' AND d.name = '5.01. Composite Casting');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5.02-001 Ceramic Rubber Tile', '5.02-001 Ceramic Rubber Tile', '5.02-001 Ceramic Rubber Tile', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5.02-001 Ceramic Rubber Tile' AND d.name = '5.02-001 Ceramic Rubber Tile');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5.03. Wearplate', '5.03. Wearplate', '5.03. Wearplate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5.03. Wearplate' AND d.name = '5.03. Wearplate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.01-001 FG Foundry GAI', '7.01-001 FG Foundry GAI', '7.01-001 FG Foundry GAI', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.01-001 FG Foundry GAI' AND d.name = '7.01-001 FG Foundry GAI');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.01-002 FG Foundry GAS', '7.01-002 FG Foundry GAS', '7.01-002 FG Foundry GAS', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.01-002 FG Foundry GAS' AND d.name = '7.01-002 FG Foundry GAS');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.01-003 FG Foundry Others', '7.01-003 FG Foundry Others', '7.01-003 FG Foundry Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.01-003 FG Foundry Others' AND d.name = '7.01-003 FG Foundry Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.01-009 FG Foundry Set', '7.01-009 FG Foundry Set', '7.01-009 FG Foundry Set', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.01-009 FG Foundry Set' AND d.name = '7.01-009 FG Foundry Set');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.01. FG Foundry', '7.01. FG Foundry', '7.01. FG Foundry', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.01. FG Foundry' AND d.name = '7.01. FG Foundry');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-002 FG Fastener Nut', '7.02-002 FG Fastener Nut', '7.02-002 FG Fastener Nut', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-002 FG Fastener Nut' AND d.name = '7.02-002 FG Fastener Nut');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-003 FG Fastener Washer', '7.02-003 FG Fastener Washer', '7.02-003 FG Fastener Washer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-003 FG Fastener Washer' AND d.name = '7.02-003 FG Fastener Washer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-004 FG Fastener Set', '7.02-004 FG Fastener Set', '7.02-004 FG Fastener Set', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-004 FG Fastener Set' AND d.name = '7.02-004 FG Fastener Set');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-005 FG Fastener Others', '7.02-005 FG Fastener Others', '7.02-005 FG Fastener Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-005 FG Fastener Others' AND d.name = '7.02-005 FG Fastener Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-101 FG Fastener Bolt - Oval Head Tapper', '7.02-101 FG Fastener Bolt - Oval Head Tapper', '7.02-101 FG Fastener Bolt - Oval Head Tapper', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-101 FG Fastener Bolt - Oval Head Tapper' AND d.name = '7.02-101 FG Fastener Bolt - Oval Head Tapper');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-102 FG Fastener Bolt - Gash', '7.02-102 FG Fastener Bolt - Gash', '7.02-102 FG Fastener Bolt - Gash', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-102 FG Fastener Bolt - Gash' AND d.name = '7.02-102 FG Fastener Bolt - Gash');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-103 FG Fastener Bolt - Hexagon', '7.02-103 FG Fastener Bolt - Hexagon', '7.02-103 FG Fastener Bolt - Hexagon', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-103 FG Fastener Bolt - Hexagon' AND d.name = '7.02-103 FG Fastener Bolt - Hexagon');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-104 FG Fastener Bolt - Spherical', '7.02-104 FG Fastener Bolt - Spherical', '7.02-104 FG Fastener Bolt - Spherical', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-104 FG Fastener Bolt - Spherical' AND d.name = '7.02-104 FG Fastener Bolt - Spherical');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-105 FG Fastener Bolt - T-Bolt', '7.02-105 FG Fastener Bolt - T-Bolt', '7.02-105 FG Fastener Bolt - T-Bolt', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-105 FG Fastener Bolt - T-Bolt' AND d.name = '7.02-105 FG Fastener Bolt - T-Bolt');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-106 FG Fastener Bolt - Stud', '7.02-106 FG Fastener Bolt - Stud', '7.02-106 FG Fastener Bolt - Stud', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-106 FG Fastener Bolt - Stud' AND d.name = '7.02-106 FG Fastener Bolt - Stud');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-107 FG Fastener Bolt - Eye', '7.02-107 FG Fastener Bolt - Eye', '7.02-107 FG Fastener Bolt - Eye', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-107 FG Fastener Bolt - Eye' AND d.name = '7.02-107 FG Fastener Bolt - Eye');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-108 FG Fastener Bolt - Socket Head', '7.02-108 FG Fastener Bolt - Socket Head', '7.02-108 FG Fastener Bolt - Socket Head', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-108 FG Fastener Bolt - Socket Head' AND d.name = '7.02-108 FG Fastener Bolt - Socket Head');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-109 FG Fastener Bolt - Square Head', '7.02-109 FG Fastener Bolt - Square Head', '7.02-109 FG Fastener Bolt - Square Head', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-109 FG Fastener Bolt - Square Head' AND d.name = '7.02-109 FG Fastener Bolt - Square Head');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-110 FG Fastener Bolt - Elevator', '7.02-110 FG Fastener Bolt - Elevator', '7.02-110 FG Fastener Bolt - Elevator', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-110 FG Fastener Bolt - Elevator' AND d.name = '7.02-110 FG Fastener Bolt - Elevator');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-111 FG Fastener Bolt - Round Head Square Neck', '7.02-111 FG Fastener Bolt - Round Head Square Neck', '7.02-111 FG Fastener Bolt - Round Head Square Neck', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-111 FG Fastener Bolt - Round Head Square Neck' AND d.name = '7.02-111 FG Fastener Bolt - Round Head Square Neck');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02-112 FG Fastener Bolt - Countersunk', '7.02-112 FG Fastener Bolt - Countersunk', '7.02-112 FG Fastener Bolt - Countersunk', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02-112 FG Fastener Bolt - Countersunk' AND d.name = '7.02-112 FG Fastener Bolt - Countersunk');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02. FG Fastener', '7.02. FG Fastener', '7.02. FG Fastener', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02. FG Fastener' AND d.name = '7.02. FG Fastener');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.03-001 FG Rubber - Liner', '7.03-001 FG Rubber - Liner', '7.03-001 FG Rubber - Liner', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.03-001 FG Rubber - Liner' AND d.name = '7.03-001 FG Rubber - Liner');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.03-004 FG Rubber - Joint Strip', '7.03-004 FG Rubber - Joint Strip', '7.03-004 FG Rubber - Joint Strip', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.03-004 FG Rubber - Joint Strip' AND d.name = '7.03-004 FG Rubber - Joint Strip');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.03-005 FG Rubber - Seal', '7.03-005 FG Rubber - Seal', '7.03-005 FG Rubber - Seal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.03-005 FG Rubber - Seal' AND d.name = '7.03-005 FG Rubber - Seal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.03-006 FG Rubber - Set', '7.03-006 FG Rubber - Set', '7.03-006 FG Rubber - Set', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.03-006 FG Rubber - Set' AND d.name = '7.03-006 FG Rubber - Set');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.03-007 FG Rubber - Plug', '7.03-007 FG Rubber - Plug', '7.03-007 FG Rubber - Plug', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.03-007 FG Rubber - Plug' AND d.name = '7.03-007 FG Rubber - Plug');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.03-008 FG Rubber - Metal', '7.03-008 FG Rubber - Metal', '7.03-008 FG Rubber - Metal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.03-008 FG Rubber - Metal' AND d.name = '7.03-008 FG Rubber - Metal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.03. FG Rubber', '7.03. FG Rubber', '7.03. FG Rubber', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.03. FG Rubber' AND d.name = '7.03. FG Rubber');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.04-001 FG GM - Ball', '7.04-001 FG GM - Ball', '7.04-001 FG GM - Ball', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.04-001 FG GM - Ball' AND d.name = '7.04-001 FG GM - Ball');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.04-002 FG GM - Rod', '7.04-002 FG GM - Rod', '7.04-002 FG GM - Rod', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.04-002 FG GM - Rod' AND d.name = '7.04-002 FG GM - Rod');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.04. FG Grinding Media', '7.04. FG Grinding Media', '7.04. FG Grinding Media', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.04. FG Grinding Media' AND d.name = '7.04. FG Grinding Media');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.01. IT Service', '8.01. IT Service', '8.01. IT Service', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.01. IT Service' AND d.name = '8.01. IT Service');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.02. Import Cost', '8.02. Import Cost', '8.02. Import Cost', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.02. Import Cost' AND d.name = '8.02. Import Cost');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.03. Export Cost', '8.03. Export Cost', '8.03. Export Cost', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.03. Export Cost' AND d.name = '8.03. Export Cost');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.04. Maintenance', '8.04. Maintenance', '8.04. Maintenance', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.04. Maintenance' AND d.name = '8.04. Maintenance');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.05. Rental', '8.05. Rental', '8.05. Rental', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.05. Rental' AND d.name = '8.05. Rental');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.06. Expedition', '8.06. Expedition', '8.06. Expedition', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.06. Expedition' AND d.name = '8.06. Expedition');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.07. Outsourcing', '8.07. Outsourcing', '8.07. Outsourcing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.07. Outsourcing' AND d.name = '8.07. Outsourcing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.08. Waste Treatment', '8.08. Waste Treatment', '8.08. Waste Treatment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.08. Waste Treatment' AND d.name = '8.08. Waste Treatment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.09. Professional and Engineer', '8.09. Professional and Engineer', '8.09. Professional and Engineer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.09. Professional and Engineer' AND d.name = '8.09. Professional and Engineer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.10. Marketing', '8.10. Marketing', '8.10. Marketing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.10. Marketing' AND d.name = '8.10. Marketing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.11. Seminar and Training', '8.11. Seminar and Training', '8.11. Seminar and Training', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.11. Seminar and Training' AND d.name = '8.11. Seminar and Training');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.12. Project', '8.12. Project', '8.12. Project', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.12. Project' AND d.name = '8.12. Project');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.13. Prepayment', '8.13. Prepayment', '8.13. Prepayment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.13. Prepayment' AND d.name = '8.13. Prepayment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.15. Techical Engineering', '8.15. Techical Engineering', '8.15. Techical Engineering', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.15. Techical Engineering' AND d.name = '8.15. Techical Engineering');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.16. Sembako', '8.16. Sembako', '8.16. Sembako', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.16. Sembako' AND d.name = '8.16. Sembako');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '8.99. Other Service', '8.99. Other Service', '8.99. Other Service', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '8.99. Other Service' AND d.name = '8.99. Other Service');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-001.01 RM Rubber - Polymer - Natural Rubber', '1.03-001.01 RM Rubber - Polymer - Natural Rubber', '1.03-001.01 RM Rubber - Polymer - Natural Rubber', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-001.01 RM Rubber - Polymer - Natural Rubber' AND d.name = '1.03-001.01 RM Rubber - Polymer - Natural Rubber');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01-001.05 Additive - Alloy - Ferro Manganese', '2.01-001.05 Additive - Alloy - Ferro Manganese', '2.01-001.05 Additive - Alloy - Ferro Manganese', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01-001.05 Additive - Alloy - Ferro Manganese' AND d.name = '2.01-001.05 Additive - Alloy - Ferro Manganese');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03-001.02 RM Rubber - Polymer - Synthetic Rubber', '1.03-001.02 RM Rubber - Polymer - Synthetic Rubber', '1.03-001.02 RM Rubber - Polymer - Synthetic Rubber', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0020'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03-001.02 RM Rubber - Polymer - Synthetic Rubber' AND d.name = '1.03-001.02 RM Rubber - Polymer - Synthetic Rubber');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.21.FE Throat Liner (SAG/AG Mill)', '1.01.21.FE Throat Liner (SAG/AG Mill)', '1.01.21.FE Throat Liner (SAG/AG Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.21.FE Throat Liner (SAG/AG Mill)' AND d.name = '1.01.21.FE Throat Liner (SAG/AG Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.22.FE Inner Liner (SAG/AG Mill)', '1.01.22.FE Inner Liner (SAG/AG Mill)', '1.01.22.FE Inner Liner (SAG/AG Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.22.FE Inner Liner (SAG/AG Mill)' AND d.name = '1.01.22.FE Inner Liner (SAG/AG Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.23.FE Middle Liner (SAG/AG Mill)', '1.01.23.FE Middle Liner (SAG/AG Mill)', '1.01.23.FE Middle Liner (SAG/AG Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.23.FE Middle Liner (SAG/AG Mill)' AND d.name = '1.01.23.FE Middle Liner (SAG/AG Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.24.FE Outer Liner (SAG/AG Mill)', '1.01.24.FE Outer Liner (SAG/AG Mill)', '1.01.24.FE Outer Liner (SAG/AG Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.24.FE Outer Liner (SAG/AG Mill)' AND d.name = '1.01.24.FE Outer Liner (SAG/AG Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.25.Top Hat Shell Liner (Single Chord)', '1.01.25.Top Hat Shell Liner (Single Chord)', '1.01.25.Top Hat Shell Liner (Single Chord)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.25.Top Hat Shell Liner (Single Chord)' AND d.name = '1.01.25.Top Hat Shell Liner (Single Chord)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.26.Top Hat Shell Liner (Double Chord)', '1.01.26.Top Hat Shell Liner (Double Chord)', '1.01.26.Top Hat Shell Liner (Double Chord)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.26.Top Hat Shell Liner (Double Chord)' AND d.name = '1.01.26.Top Hat Shell Liner (Double Chord)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.27.Lifter Bar', '1.01.27.Lifter Bar', '1.01.27.Lifter Bar', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.27.Lifter Bar' AND d.name = '1.01.27.Lifter Bar');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.28.Shell Plate', '1.01.28.Shell Plate', '1.01.28.Shell Plate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.28.Shell Plate' AND d.name = '1.01.28.Shell Plate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.29.Outer Pulp Lifter', '1.01.29.Outer Pulp Lifter', '1.01.29.Outer Pulp Lifter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.29.Outer Pulp Lifter' AND d.name = '1.01.29.Outer Pulp Lifter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.30.Middle Pulp Lifter', '1.01.30.Middle Pulp Lifter', '1.01.30.Middle Pulp Lifter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.30.Middle Pulp Lifter' AND d.name = '1.01.30.Middle Pulp Lifter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.31.Inner Pulp Lifter', '1.01.31.Inner Pulp Lifter', '1.01.31.Inner Pulp Lifter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.31.Inner Pulp Lifter' AND d.name = '1.01.31.Inner Pulp Lifter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.32.Chamber Pulp Lifter', '1.01.32.Chamber Pulp Lifter', '1.01.32.Chamber Pulp Lifter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.32.Chamber Pulp Lifter' AND d.name = '1.01.32.Chamber Pulp Lifter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.33.Grate', '1.01.33.Grate', '1.01.33.Grate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.33.Grate' AND d.name = '1.01.33.Grate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.34.DE Middle Liner', '1.01.34.DE Middle Liner', '1.01.34.DE Middle Liner', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.34.DE Middle Liner' AND d.name = '1.01.34.DE Middle Liner');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.35.Pulp Discharger', '1.01.35.Pulp Discharger', '1.01.35.Pulp Discharger', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.35.Pulp Discharger' AND d.name = '1.01.35.Pulp Discharger');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.36.Filler Ring', '1.01.36.Filler Ring', '1.01.36.Filler Ring', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.36.Filler Ring' AND d.name = '1.01.36.Filler Ring');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.37.Clamping Ring', '1.01.37.Clamping Ring', '1.01.37.Clamping Ring', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.37.Clamping Ring' AND d.name = '1.01.37.Clamping Ring');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.38.Insert (White Iron)', '1.01.38.Insert (White Iron)', '1.01.38.Insert (White Iron)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.38.Insert (White Iron)' AND d.name = '1.01.38.Insert (White Iron)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.39.FE/DE Inner Liner (Ball/Rod Mill)', '1.01.39.FE/DE Inner Liner (Ball/Rod Mill)', '1.01.39.FE/DE Inner Liner (Ball/Rod Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.39.FE/DE Inner Liner (Ball/Rod Mill)' AND d.name = '1.01.39.FE/DE Inner Liner (Ball/Rod Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.40.FE/DE Middle Liner (Ball/Rod Mill)', '1.01.40.FE/DE Middle Liner (Ball/Rod Mill)', '1.01.40.FE/DE Middle Liner (Ball/Rod Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.40.FE/DE Middle Liner (Ball/Rod Mill)' AND d.name = '1.01.40.FE/DE Middle Liner (Ball/Rod Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.41.FE/DE Outer Liner (Ball/Rod Mill)', '1.01.41.FE/DE Outer Liner (Ball/Rod Mill)', '1.01.41.FE/DE Outer Liner (Ball/Rod Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.41.FE/DE Outer Liner (Ball/Rod Mill)' AND d.name = '1.01.41.FE/DE Outer Liner (Ball/Rod Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.42.Shell Liner Single Chord (Ball/Rod Mill)', '1.01.42.Shell Liner Single Chord (Ball/Rod Mill)', '1.01.42.Shell Liner Single Chord (Ball/Rod Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.42.Shell Liner Single Chord (Ball/Rod Mill)' AND d.name = '1.01.42.Shell Liner Single Chord (Ball/Rod Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.43.Shell Liner Double Chord (Ball/Rod Mill)', '1.01.43.Shell Liner Double Chord (Ball/Rod Mill)', '1.01.43.Shell Liner Double Chord (Ball/Rod Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.43.Shell Liner Double Chord (Ball/Rod Mill)' AND d.name = '1.01.43.Shell Liner Double Chord (Ball/Rod Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.44.Shell Liner Triple Chord (Ball/Rod Mill)', '1.01.44.Shell Liner Triple Chord (Ball/Rod Mill)', '1.01.44.Shell Liner Triple Chord (Ball/Rod Mill)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.44.Shell Liner Triple Chord (Ball/Rod Mill)' AND d.name = '1.01.44.Shell Liner Triple Chord (Ball/Rod Mill)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.99.Others', '1.01.99.Others', '1.01.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.99.Others' AND d.name = '1.01.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.01.Mining Steel Liner', '1.01.Mining Steel Liner', '1.01.Mining Steel Liner', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.01.Mining Steel Liner' AND d.name = '1.01.Mining Steel Liner');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02.01.Bowl/ Mantle/ Concave', '1.02.01.Bowl/ Mantle/ Concave', '1.02.01.Bowl/ Mantle/ Concave', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02.01.Bowl/ Mantle/ Concave' AND d.name = '1.02.01.Bowl/ Mantle/ Concave');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02.02.Jaw', '1.02.02.Jaw', '1.02.02.Jaw', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02.02.Jaw' AND d.name = '1.02.02.Jaw');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02.03.Cheek plate', '1.02.03.Cheek plate', '1.02.03.Cheek plate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02.03.Cheek plate' AND d.name = '1.02.03.Cheek plate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02.04.Segmented concave', '1.02.04.Segmented concave', '1.02.04.Segmented concave', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02.04.Segmented concave' AND d.name = '1.02.04.Segmented concave');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02.99.Others', '1.02.99.Others', '1.02.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02.99.Others' AND d.name = '1.02.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.02.Mining Crusher', '1.02.Mining Crusher', '1.02.Mining Crusher', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.02.Mining Crusher' AND d.name = '1.02.Mining Crusher');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03.01.Bolt', '1.03.01.Bolt', '1.03.01.Bolt', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03.01.Bolt' AND d.name = '1.03.01.Bolt');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03.02.Washer', '1.03.02.Washer', '1.03.02.Washer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03.02.Washer' AND d.name = '1.03.02.Washer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03.03.Nut', '1.03.03.Nut', '1.03.03.Nut', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03.03.Nut' AND d.name = '1.03.03.Nut');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03.99.Others', '1.03.99.Others', '1.03.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03.99.Others' AND d.name = '1.03.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.03.Mining Fastener', '1.03.Mining Fastener', '1.03.Mining Fastener', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.03.Mining Fastener' AND d.name = '1.03.Mining Fastener');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.01.Joint strip/ rubber wedge', '1.04.01.Joint strip/ rubber wedge', '1.04.01.Joint strip/ rubber wedge', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.01.Joint strip/ rubber wedge' AND d.name = '1.04.01.Joint strip/ rubber wedge');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.02.Seal', '1.04.02.Seal', '1.04.02.Seal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.02.Seal' AND d.name = '1.04.02.Seal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.03.Rubber Plug', '1.04.03.Rubber Plug', '1.04.03.Rubber Plug', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.03.Rubber Plug' AND d.name = '1.04.03.Rubber Plug');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.04.Rubber Bushing', '1.04.04.Rubber Bushing', '1.04.04.Rubber Bushing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.04.Rubber Bushing' AND d.name = '1.04.04.Rubber Bushing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.05.Fastener Plug', '1.04.05.Fastener Plug', '1.04.05.Fastener Plug', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.05.Fastener Plug' AND d.name = '1.04.05.Fastener Plug');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.06.Screening Panel', '1.04.06.Screening Panel', '1.04.06.Screening Panel', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.06.Screening Panel' AND d.name = '1.04.06.Screening Panel');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.07.Wear Pad', '1.04.07.Wear Pad', '1.04.07.Wear Pad', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.07.Wear Pad' AND d.name = '1.04.07.Wear Pad');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.09.ISA Mill Grinding Disc', '1.04.09.ISA Mill Grinding Disc', '1.04.09.ISA Mill Grinding Disc', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.09.ISA Mill Grinding Disc' AND d.name = '1.04.09.ISA Mill Grinding Disc');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.11.ISA Mill Others', '1.04.11.ISA Mill Others', '1.04.11.ISA Mill Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.11.ISA Mill Others' AND d.name = '1.04.11.ISA Mill Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.12.Rubber Backing Sheet', '1.04.12.Rubber Backing Sheet', '1.04.12.Rubber Backing Sheet', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.12.Rubber Backing Sheet' AND d.name = '1.04.12.Rubber Backing Sheet');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.99.Others', '1.04.99.Others', '1.04.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.99.Others' AND d.name = '1.04.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.04.Mining Rubber Components', '1.04.Mining Rubber Components', '1.04.Mining Rubber Components', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.04.Mining Rubber Components' AND d.name = '1.04.Mining Rubber Components');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05.01.Chute liner plate', '1.05.01.Chute liner plate', '1.05.01.Chute liner plate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05.01.Chute liner plate' AND d.name = '1.05.01.Chute liner plate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05.02.Trunnion Liner/ Bearing Housing', '1.05.02.Trunnion Liner/ Bearing Housing', '1.05.02.Trunnion Liner/ Bearing Housing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05.02.Trunnion Liner/ Bearing Housing' AND d.name = '1.05.02.Trunnion Liner/ Bearing Housing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05.03.Feed Spout/ Elbow/ Pipe Bend', '1.05.03.Feed Spout/ Elbow/ Pipe Bend', '1.05.03.Feed Spout/ Elbow/ Pipe Bend', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05.03.Feed Spout/ Elbow/ Pipe Bend' AND d.name = '1.05.03.Feed Spout/ Elbow/ Pipe Bend');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05.04.Verti Liner', '1.05.04.Verti Liner', '1.05.04.Verti Liner', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05.04.Verti Liner' AND d.name = '1.05.04.Verti Liner');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05.05.Apron Feeder', '1.05.05.Apron Feeder', '1.05.05.Apron Feeder', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05.05.Apron Feeder' AND d.name = '1.05.05.Apron Feeder');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05.99.Others', '1.05.99.Others', '1.05.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05.99.Others' AND d.name = '1.05.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.05.Mining Other Components', '1.05.Mining Other Components', '1.05.Mining Other Components', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.05.Mining Other Components' AND d.name = '1.05.Mining Other Components');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.01.Head Plate (Full Rubber)', '1.06.01.Head Plate (Full Rubber)', '1.06.01.Head Plate (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.01.Head Plate (Full Rubber)' AND d.name = '1.06.01.Head Plate (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.02.Head Lifter Bar (Full Rubber)', '1.06.02.Head Lifter Bar (Full Rubber)', '1.06.02.Head Lifter Bar (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.02.Head Lifter Bar (Full Rubber)' AND d.name = '1.06.02.Head Lifter Bar (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.03.Head Optiliner (Full Rubber)', '1.06.03.Head Optiliner (Full Rubber)', '1.06.03.Head Optiliner (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.03.Head Optiliner (Full Rubber)' AND d.name = '1.06.03.Head Optiliner (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.04.Shell Plate (Full Rubber)', '1.06.04.Shell Plate (Full Rubber)', '1.06.04.Shell Plate (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.04.Shell Plate (Full Rubber)' AND d.name = '1.06.04.Shell Plate (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.05.Shell Lifter Bar (Full Rubber)', '1.06.05.Shell Lifter Bar (Full Rubber)', '1.06.05.Shell Lifter Bar (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.05.Shell Lifter Bar (Full Rubber)' AND d.name = '1.06.05.Shell Lifter Bar (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.06.Shell Optiliner (Full Rubber)', '1.06.06.Shell Optiliner (Full Rubber)', '1.06.06.Shell Optiliner (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.06.Shell Optiliner (Full Rubber)' AND d.name = '1.06.06.Shell Optiliner (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.07.Filler Block (Full Rubber)', '1.06.07.Filler Block (Full Rubber)', '1.06.07.Filler Block (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.07.Filler Block (Full Rubber)' AND d.name = '1.06.07.Filler Block (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.08.Filler Ring (Full Rubber)', '1.06.08.Filler Ring (Full Rubber)', '1.06.08.Filler Ring (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.08.Filler Ring (Full Rubber)' AND d.name = '1.06.08.Filler Ring (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.09.Filler Insert (Full Rubber)', '1.06.09.Filler Insert (Full Rubber)', '1.06.09.Filler Insert (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.09.Filler Insert (Full Rubber)' AND d.name = '1.06.09.Filler Insert (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.10.Grate (Full Rubber)', '1.06.10.Grate (Full Rubber)', '1.06.10.Grate (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.10.Grate (Full Rubber)' AND d.name = '1.06.10.Grate (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.11.Trunnion Liner (Full Rubber)', '1.06.11.Trunnion Liner (Full Rubber)', '1.06.11.Trunnion Liner (Full Rubber)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.11.Trunnion Liner (Full Rubber)' AND d.name = '1.06.11.Trunnion Liner (Full Rubber)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.21.Head Plate (Composite)', '1.06.21.Head Plate (Composite)', '1.06.21.Head Plate (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.21.Head Plate (Composite)' AND d.name = '1.06.21.Head Plate (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.22.Head Lifter Bar (Composite)', '1.06.22.Head Lifter Bar (Composite)', '1.06.22.Head Lifter Bar (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.22.Head Lifter Bar (Composite)' AND d.name = '1.06.22.Head Lifter Bar (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.23.Head Optiliner (Composite)', '1.06.23.Head Optiliner (Composite)', '1.06.23.Head Optiliner (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.23.Head Optiliner (Composite)' AND d.name = '1.06.23.Head Optiliner (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.24.Shell Plate (Composite)', '1.06.24.Shell Plate (Composite)', '1.06.24.Shell Plate (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.24.Shell Plate (Composite)' AND d.name = '1.06.24.Shell Plate (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.25.Shell Lifter Bar (Composite)', '1.06.25.Shell Lifter Bar (Composite)', '1.06.25.Shell Lifter Bar (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.25.Shell Lifter Bar (Composite)' AND d.name = '1.06.25.Shell Lifter Bar (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.26.Shell Optiliner (Composite)', '1.06.26.Shell Optiliner (Composite)', '1.06.26.Shell Optiliner (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.26.Shell Optiliner (Composite)' AND d.name = '1.06.26.Shell Optiliner (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.27.Filler Block (Composite)', '1.06.27.Filler Block (Composite)', '1.06.27.Filler Block (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.27.Filler Block (Composite)' AND d.name = '1.06.27.Filler Block (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.28.Filler Ring (Composite)', '1.06.28.Filler Ring (Composite)', '1.06.28.Filler Ring (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.28.Filler Ring (Composite)' AND d.name = '1.06.28.Filler Ring (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.29.Grate (Composite)', '1.06.29.Grate (Composite)', '1.06.29.Grate (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.29.Grate (Composite)' AND d.name = '1.06.29.Grate (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.30.Trunnion Liner (Composite)', '1.06.30.Trunnion Liner (Composite)', '1.06.30.Trunnion Liner (Composite)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.30.Trunnion Liner (Composite)' AND d.name = '1.06.30.Trunnion Liner (Composite)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.41.Moulded Pulp System', '1.06.41.Moulded Pulp System', '1.06.41.Moulded Pulp System', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.41.Moulded Pulp System' AND d.name = '1.06.41.Moulded Pulp System');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.42.Hand Lined Pulp System', '1.06.42.Hand Lined Pulp System', '1.06.42.Hand Lined Pulp System', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.42.Hand Lined Pulp System' AND d.name = '1.06.42.Hand Lined Pulp System');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.51.Modular Discharger', '1.06.51.Modular Discharger', '1.06.51.Modular Discharger', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.51.Modular Discharger' AND d.name = '1.06.51.Modular Discharger');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.99.Others', '1.06.99.Others', '1.06.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.99.Others' AND d.name = '1.06.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.07.01.Grinding ball forged', '1.07.01.Grinding ball forged', '1.07.01.Grinding ball forged', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.07.01.Grinding ball forged' AND d.name = '1.07.01.Grinding ball forged');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.07.02.Grinding ball cast', '1.07.02.Grinding ball cast', '1.07.02.Grinding ball cast', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.07.02.Grinding ball cast' AND d.name = '1.07.02.Grinding ball cast');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.07.03.Grinding rod', '1.07.03.Grinding rod', '1.07.03.Grinding rod', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.07.03.Grinding rod' AND d.name = '1.07.03.Grinding rod');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.Mining', '1.Mining', '1.Mining', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.Mining' AND d.name = '1.Mining');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.01.Bowl/ Mantle/ Concave', '2.01.Bowl/ Mantle/ Concave', '2.01.Bowl/ Mantle/ Concave', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.01.Bowl/ Mantle/ Concave' AND d.name = '2.01.Bowl/ Mantle/ Concave');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.02.Jaw', '2.02.Jaw', '2.02.Jaw', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.02.Jaw' AND d.name = '2.02.Jaw');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.03.Cheek plate', '2.03.Cheek plate', '2.03.Cheek plate', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.03.Cheek plate' AND d.name = '2.03.Cheek plate');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.04.Segmented concave', '2.04.Segmented concave', '2.04.Segmented concave', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.04.Segmented concave' AND d.name = '2.04.Segmented concave');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '2.99.Others (e.g. jaw clamping)', '2.99.Others (e.g. jaw clamping)', '2.99.Others (e.g. jaw clamping)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '2.99.Others (e.g. jaw clamping)' AND d.name = '2.99.Others (e.g. jaw clamping)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.01.Casting for Foundry BU', '3.01.Casting for Foundry BU', '3.01.Casting for Foundry BU', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.01.Casting for Foundry BU' AND d.name = '3.01.Casting for Foundry BU');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.02.Casting for Rubber BU', '3.02.Casting for Rubber BU', '3.02.Casting for Rubber BU', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.02.Casting for Rubber BU' AND d.name = '3.02.Casting for Rubber BU');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.03.Casting for Grinding BU', '3.03.Casting for Grinding BU', '3.03.Casting for Grinding BU', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.03.Casting for Grinding BU' AND d.name = '3.03.Casting for Grinding BU');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.98.Service', '3.98.Service', '3.98.Service', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.98.Service' AND d.name = '3.98.Service');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.99.Others', '3.99.Others', '3.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.99.Others' AND d.name = '3.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '3.Foundry', '3.Foundry', '3.Foundry', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '3.Foundry' AND d.name = '3.Foundry');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.01.Roll', '4.01.Roll', '4.01.Roll', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.01.Roll' AND d.name = '4.01.Roll');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.02.Component', '4.02.Component', '4.02.Component', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.02.Component' AND d.name = '4.02.Component');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '4.Crumb Rubber', '4.Crumb Rubber', '4.Crumb Rubber', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '4.Crumb Rubber' AND d.name = '4.Crumb Rubber');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5.01.Roll', '5.01.Roll', '5.01.Roll', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5.01.Roll' AND d.name = '5.01.Roll');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5.02.Component', '5.02.Component', '5.02.Component', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5.02.Component' AND d.name = '5.02.Component');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '5.98.Service', '5.98.Service', '5.98.Service', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '5.98.Service' AND d.name = '5.98.Service');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '6.01.Grinding plate/ table liner', '6.01.Grinding plate/ table liner', '6.01.Grinding plate/ table liner', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '6.01.Grinding plate/ table liner' AND d.name = '6.01.Grinding plate/ table liner');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '6.02.Grinding roll/ roll tyre', '6.02.Grinding roll/ roll tyre', '6.02.Grinding roll/ roll tyre', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '6.02.Grinding roll/ roll tyre' AND d.name = '6.02.Grinding roll/ roll tyre');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '6.99.Others', '6.99.Others', '6.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '6.99.Others' AND d.name = '6.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.01.Liner', '7.01.Liner', '7.01.Liner', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.01.Liner' AND d.name = '7.01.Liner');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.02.Table liner', '7.02.Table liner', '7.02.Table liner', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.02.Table liner' AND d.name = '7.02.Table liner');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.03.Roll tyre', '7.03.Roll tyre', '7.03.Roll tyre', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.03.Roll tyre' AND d.name = '7.03.Roll tyre');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '7.99.Others', '7.99.Others', '7.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '7.99.Others' AND d.name = '7.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '9.01 Down Payment', '9.01 Down Payment', '9.01 Down Payment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '9.01 Down Payment' AND d.name = '9.01 Down Payment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '9.02 Freight Charge', '9.02 Freight Charge', '9.02 Freight Charge', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '9.02 Freight Charge' AND d.name = '9.02 Freight Charge');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.07.99.Others', '1.07.99.Others', '1.07.99.Others', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.07.99.Others' AND d.name = '1.07.99.Others');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.07.Grinding Ball', '1.07.Grinding Ball', '1.07.Grinding Ball', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.07.Grinding Ball' AND d.name = '1.07.Grinding Ball');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '1.06.Mining Rubber Liner', '1.06.Mining Rubber Liner', '1.06.Mining Rubber Liner', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0021'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '1.06.Mining Rubber Liner' AND d.name = '1.06.Mining Rubber Liner');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '07-SALES', '07-SALES', '07-SALES', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0022'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '07-SALES' AND d.name = '07-SALES');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '00_Default', '00_Default', '00_Default', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0023'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '00_Default' AND d.name = '00_Default');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Pattern', 'Pattern', 'Pattern', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Pattern' AND d.name = 'Pattern');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'QC Jig', 'QC Jig', 'QC Jig', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'QC Jig' AND d.name = 'QC Jig');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Insert', 'Insert', 'Insert', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Insert' AND d.name = 'Insert');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Dedicated Mould', 'Dedicated Mould', 'Dedicated Mould', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Dedicated Mould' AND d.name = 'Dedicated Mould');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Box Mould', 'Box Mould', 'Box Mould', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Box Mould' AND d.name = 'Box Mould');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Insert Mould', 'Insert Mould', 'Insert Mould', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Insert Mould' AND d.name = 'Insert Mould');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Extruder Head', 'Extruder Head', 'Extruder Head', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0024'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Extruder Head' AND d.name = 'Extruder Head');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Land', 'Land', 'Land', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Land' AND d.name = 'Land');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Factory Building', 'Factory Buildings', 'Factory Buildings', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Factory Building' AND d.name = 'Factory Buildings');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Office Building', 'Office Buildings', 'Office Buildings', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Office Building' AND d.name = 'Office Buildings');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine', 'Machines', 'Machines', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine' AND d.name = 'Machines');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Tools', 'Tools', 'Tools', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Tools' AND d.name = 'Tools');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Equipment', 'Equipments', 'Equipments', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Equipment' AND d.name = 'Equipments');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Office Vehicle', 'Office Vehicles', 'Office Vehicles', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Office Vehicle' AND d.name = 'Office Vehicles');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Factory Vehicle', 'Factory Vehicles', 'Factory Vehicles', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0027'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Factory Vehicle' AND d.name = 'Factory Vehicles');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '04 Years', '48', '48', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '04 Years' AND d.name = '48');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '08 Years', '96', '96', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '08 Years' AND d.name = '96');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '10 Years', '120', '120', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '10 Years' AND d.name = '120');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '16 Years', '192', '192', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '16 Years' AND d.name = '192');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, '20 Years', '240', '240', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0028'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = '20 Years' AND d.name = '240');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU01', 'Foundry', 'Foundry', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU01' AND d.name = 'Foundry');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU02', 'Fastener', 'Fastener', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU02' AND d.name = 'Fastener');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU03', 'Rubber Plant', 'Rubber Plant', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU03' AND d.name = 'Rubber Plant');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU04', 'Grinding Media Medan', 'Grinding Media Medan', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU04' AND d.name = 'Grinding Media Medan');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU05', 'PLTU 1', 'PLTU 1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU05' AND d.name = 'PLTU 1');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU06', 'PLTU 2', 'PLTU 2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU06' AND d.name = 'PLTU 2');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU07', 'Head Office Medan', 'Head Office Medan', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU07' AND d.name = 'Head Office Medan');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA.BU08', 'Grinding Media Pasuruan', 'Grinding Media Pasuruan', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0029'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA.BU08' AND d.name = 'Grinding Media Pasuruan');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAN.P01', 'Fastener - Production', 'Fastener - Production', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAN.P01' AND d.name = 'Fastener - Production');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAN.P02', 'Fastener - QC', 'Fastener - QC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAN.P02' AND d.name = 'Fastener - QC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAN.P03', 'Fastener - Machining', 'Fastener - Machining', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAN.P03' AND d.name = 'Fastener - Machining');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAN.S01', 'Fastener - Dispatch', 'Fastener - Dispatch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAN.S01' AND d.name = 'Fastener - Dispatch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAN.S02', 'Fastener - Maintenance', 'Fastener - Maintenance', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAN.S02' AND d.name = 'Fastener - Maintenance');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.P01', 'Foundry - Production', 'Foundry - Production', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.P01' AND d.name = 'Foundry - Production');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.P02', 'Foundry - Pattern', 'Foundry - Pattern', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.P02' AND d.name = 'Foundry - Pattern');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.P03', 'Foundry - Molding', 'Foundry - Molding', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.P03' AND d.name = 'Foundry - Molding');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.P04', 'Foundry - Melting (Furnace)', 'Foundry - Melting (Furnace)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.P04' AND d.name = 'Foundry - Melting (Furnace)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.P05', 'Foundry - Fettling', 'Foundry - Fettling', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.P05' AND d.name = 'Foundry - Fettling');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.P06', 'Foundry - Heat Treatment', 'Foundry - Heat Treatment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.P06' AND d.name = 'Foundry - Heat Treatment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.P07', 'Foundry - QC', 'Foundry - QC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.P07' AND d.name = 'Foundry - QC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.P09', 'Foundry - Machining Metal', 'Foundry - Machining Metal', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.P09' AND d.name = 'Foundry - Machining Metal');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.P10', 'Foundry - Machining Wood', 'Foundry - Machining Wood', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.P10' AND d.name = 'Foundry - Machining Wood');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S01', 'Foundry - Dispatch', 'Foundry - Dispatch', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S01' AND d.name = 'Foundry - Dispatch');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S02', 'Foundry - Design & Drafting', 'Foundry - Design & Drafting', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S02' AND d.name = 'Foundry - Design & Drafting');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S03', 'Foundry - Method', 'Foundry - Method', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S03' AND d.name = 'Foundry - Method');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S04', 'Foundry - HSE', 'Foundry - HSE', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S04' AND d.name = 'Foundry - HSE');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S05', 'Foundry - Fabrication', 'Foundry - Fabrication', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S05' AND d.name = 'Foundry - Fabrication');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S06', 'Foundry - Maintenance', 'Foundry - Maintenance', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S06' AND d.name = 'Foundry - Maintenance');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S07', 'Foundry - Warehouse', 'Foundry - Warehouse', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S07' AND d.name = 'Foundry - Warehouse');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S08', 'Foundry - PPC', 'Foundry - PPC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S08' AND d.name = 'Foundry - PPC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S09', 'Foundry - Technical', 'Foundry - Technical', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S09' AND d.name = 'Foundry - Technical');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S10', 'Foundry - Expedition', 'Foundry - Expedition', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S10' AND d.name = 'Foundry - Expedition');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAF.S11', 'Foundry - Lean & Sustainability', 'Foundry - Lean & Sustainability', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAF.S11' AND d.name = 'Foundry - Lean & Sustainability');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAH.D01', 'HO - Marketing', 'HO - Marketing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAH.D01' AND d.name = 'HO - Marketing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAH.D02', 'HO - Purchasing', 'HO - Purchasing', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAH.D02' AND d.name = 'HO - Purchasing');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAH.D03', 'HO - Export', 'HO - Export', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAH.D03' AND d.name = 'HO - Export');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAH.D04', 'HO - HRGA', 'HO - HRGA', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAH.D04' AND d.name = 'HO - HRGA');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAH.D05', 'HO - IT', 'HO - IT', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAH.D05' AND d.name = 'HO - IT');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAH.D06', 'HO - Finance', 'HO - Finance', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAH.D06' AND d.name = 'HO - Finance');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAH.D07', 'HO - Accounting & Tax', 'HO - Accounting & Tax', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAH.D07' AND d.name = 'HO - Accounting & Tax');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAH.D08', 'HO - General', 'HO - General', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAH.D08' AND d.name = 'HO - General');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAK.P01', 'PLTU II - Production', 'PLTU II - Production', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAK.P01' AND d.name = 'PLTU II - Production');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAP.P01', 'PLTU I - Production', 'PLTU I - Production', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAP.P01' AND d.name = 'PLTU I - Production');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAR.P01', 'Rubber Plant - Production', 'Rubber Plant - Production', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAR.P01' AND d.name = 'Rubber Plant - Production');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GAR.P02', 'Rubber Plant - Compounding', 'Rubber Plant - Compounding', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0030'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GAR.P02' AND d.name = 'Rubber Plant - Compounding');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Factory Building - Warehouse', 'Factory Building - Warehouse', 'Factory Building - Warehouse', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Factory Building - Warehouse' AND d.name = 'Factory Building - Warehouse');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Rubber Press', 'Machine Production - Rubber Press', 'Machine Production - Rubber Press', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Rubber Press' AND d.name = 'Machine Production - Rubber Press');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Heat Treatment', 'Machine Production - Heat Treatment', 'Machine Production - Heat Treatment', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Heat Treatment' AND d.name = 'Machine Production - Heat Treatment');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Procuction - Rubber Mixer', 'Machine Procuction - Rubber Mixer', 'Machine Procuction - Rubber Mixer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Procuction - Rubber Mixer' AND d.name = 'Machine Procuction - Rubber Mixer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Shotblast', 'Machine Production - Shotblast', 'Machine Production - Shotblast', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Shotblast' AND d.name = 'Machine Production - Shotblast');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Calender', 'Machine Production - Calender', 'Machine Production - Calender', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Calender' AND d.name = 'Machine Production - Calender');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - EDM (Extruder)', 'Machine Production - EDM (Extruder)', 'Machine Production - EDM (Extruder)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - EDM (Extruder)' AND d.name = 'Machine Production - EDM (Extruder)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Autoclave', 'Machine Production - Autoclave', 'Machine Production - Autoclave', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Autoclave' AND d.name = 'Machine Production - Autoclave');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Sand Mixer', 'Machine Production - Sand Mixer', 'Machine Production - Sand Mixer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Sand Mixer' AND d.name = 'Machine Production - Sand Mixer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Induction Furnace', 'Machine Production - Induction Furnace', 'Machine Production - Induction Furnace', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Induction Furnace' AND d.name = 'Machine Production - Induction Furnace');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Power Hammer', 'Machine Production - Power Hammer', 'Machine Production - Power Hammer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Power Hammer' AND d.name = 'Machine Production - Power Hammer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Rubber Preformer', 'Machine Production - Rubber Preformer', 'Machine Production - Rubber Preformer', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Rubber Preformer' AND d.name = 'Machine Production - Rubber Preformer');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Hydrolic Cutter', 'Hydrolic Cutter', 'Hydrolic Cutter', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Hydrolic Cutter' AND d.name = 'Hydrolic Cutter');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Spray Booth', 'Spray Booth', 'Spray Booth', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Spray Booth' AND d.name = 'Spray Booth');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Machine Production - Cutting Machine', 'Machine Production - Cutting Machine', 'Machine Production - Cutting Machine', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Machine Production - Cutting Machine' AND d.name = 'Machine Production - Cutting Machine');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Magnetic Particle Inspector (MPI)', 'Magnetic Particle Inspector (MPI)', 'Magnetic Particle Inspector (MPI)', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Magnetic Particle Inspector (MPI)' AND d.name = 'Magnetic Particle Inspector (MPI)');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Roll Tread', 'Roll Tread', 'Roll Tread', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Roll Tread' AND d.name = 'Roll Tread');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Mesin Forging', 'Mesin Forging', 'Mesin Forging', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Mesin Forging' AND d.name = 'Mesin Forging');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'CNC', 'CNC', 'CNC', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0031'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'CNC' AND d.name = 'CNC');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Open', 'Open', 'Open', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0032'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Open' AND d.name = 'Open');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Closed', 'Closed', 'Closed', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0032'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Closed' AND d.name = 'Closed');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Scrap/Sold', 'Scrap/Sold', 'Scrap/Sold', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0032'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Scrap/Sold' AND d.name = 'Scrap/Sold');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 01', 'KIM-03, Bay 01', 'KIM-03, Bay 01', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 01' AND d.name = 'KIM-03, Bay 01');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 02', 'KIM-03, Bay 02', 'KIM-03, Bay 02', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 02' AND d.name = 'KIM-03, Bay 02');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 03', 'KIM-03, Bay 03', 'KIM-03, Bay 03', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 03' AND d.name = 'KIM-03, Bay 03');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 04', 'KIM-03, Bay 04', 'KIM-03, Bay 04', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 04' AND d.name = 'KIM-03, Bay 04');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 05', 'KIM-03, Bay 05', 'KIM-03, Bay 05', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 05' AND d.name = 'KIM-03, Bay 05');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 06', 'KIM-03, Bay 06', 'KIM-03, Bay 06', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 06' AND d.name = 'KIM-03, Bay 06');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 07', 'KIM-03, Bay 07', 'KIM-03, Bay 07', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 07' AND d.name = 'KIM-03, Bay 07');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 08', 'KIM-03, Bay 08', 'KIM-03, Bay 08', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 08' AND d.name = 'KIM-03, Bay 08');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-01, GDG 01', 'GA-01, GDG 01', 'GA-01, GDG 01', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-01, GDG 01' AND d.name = 'GA-01, GDG 01');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-01, GDG 02', 'GA-01, GDG 02', 'GA-01, GDG 02', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-01, GDG 02' AND d.name = 'GA-01, GDG 02');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-01, GDG 03', 'GA-01, GDG 03', 'GA-01, GDG 03', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-01, GDG 03' AND d.name = 'GA-01, GDG 03');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-01, GDG 04', 'GA-01, GDG 04', 'GA-01, GDG 04', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-01, GDG 04' AND d.name = 'GA-01, GDG 04');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-01, GDG 05', 'GA-01, GDG 05', 'GA-01, GDG 05', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-01, GDG 05' AND d.name = 'GA-01, GDG 05');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-02, GDG 06', 'GA-02, GDG 06', 'GA-02, GDG 06', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-02, GDG 06' AND d.name = 'GA-02, GDG 06');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-02, GDG 07', 'GA-02, GDG 07', 'GA-02, GDG 07', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-02, GDG 07' AND d.name = 'GA-02, GDG 07');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-02, GDG 08', 'GA-02, GDG 08', 'GA-02, GDG 08', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-02, GDG 08' AND d.name = 'GA-02, GDG 08');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-02, GDG 09', 'GA-02, GDG 09', 'GA-02, GDG 09', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-02, GDG 09' AND d.name = 'GA-02, GDG 09');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-02, GDG 10', 'GA-02, GDG 10', 'GA-02, GDG 10', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-02, GDG 10' AND d.name = 'GA-02, GDG 10');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-08, GDG 00', 'GA-08, GDG 00', 'GA-08, GDG 00', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-08, GDG 00' AND d.name = 'GA-08, GDG 00');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-08, GDG 01', 'GA-08, GDG 01', 'GA-08, GDG 01', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-08, GDG 01' AND d.name = 'GA-08, GDG 01');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-08, GDG 02', 'GA-08, GDG 02', 'GA-08, GDG 02', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-08, GDG 02' AND d.name = 'GA-08, GDG 02');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-08, GDG 03', 'GA-08, GDG 03', 'GA-08, GDG 03', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-08, GDG 03' AND d.name = 'GA-08, GDG 03');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 11', 'KIM-03, Bay 11', 'KIM-03, Bay 11', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 11' AND d.name = 'KIM-03, Bay 11');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 09', 'KIM-03, Bay 09', 'KIM-03, Bay 09', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 09' AND d.name = 'KIM-03, Bay 09');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'KIM-03, Bay 10', 'KIM-03, Bay 10', 'KIM-03, Bay 10', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'KIM-03, Bay 10' AND d.name = 'KIM-03, Bay 10');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-03, GDG 01', 'GA-03, GDG 01', 'GA-03, GDG 01', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-03, GDG 01' AND d.name = 'GA-03, GDG 01');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-03, GDG 02', 'GA-03, GDG 02', 'GA-03, GDG 02', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-03, GDG 02' AND d.name = 'GA-03, GDG 02');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'GA-03, GDG 03', 'GA-03, GDG 03', 'GA-03, GDG 03', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0033'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'GA-03, GDG 03' AND d.name = 'GA-03, GDG 03');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Natural Gas', 'Natural Gas', 'Natural Gas', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0038'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Natural Gas' AND d.name = 'Natural Gas');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Electricity', 'Electricity', 'Electricity', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0038'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Electricity' AND d.name = 'Electricity');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Shift 1', 'Shift 1', 'Shift 1', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0039'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Shift 1' AND d.name = 'Shift 1');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Shift 2', 'Shift 2', 'Shift 2', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0039'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Shift 2' AND d.name = 'Shift 2');

INSERT INTO dynamic.mdlookup (global_category_id, code, name, description, inputby, inputdt, status)
SELECT h.id, 'Shift 3', 'Shift 3', 'Shift 3', 'admin', NOW(), true
FROM dynamic.mhlookup h
WHERE h.category_code = 'MAS0039'
  AND NOT EXISTS (SELECT 1 FROM dynamic.mdlookup d WHERE d.global_category_id = h.id AND d.code = 'Shift 3' AND d.name = 'Shift 3');

COMMIT;
-- End of script
