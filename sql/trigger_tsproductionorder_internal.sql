CREATE OR REPLACE FUNCTION dynamic.trg_after_tsproductionorder_internal_fn()
RETURNS TRIGGER AS $$
BEGIN
    -- 1. Validasi BOM dan Route
    -- mhbom dan mhroute menggunakan kolom 'status' (boolean), bukan 'idstatus'
    -- mdbom dan mdroute tidak memiliki kolom status, dan menggunakan parent id: mhbomid & mhrouteid
    IF EXISTS (
        SELECT 1 
        FROM (SELECT NEW.itemid AS itemid) a
        LEFT JOIN dynamic.mhbom b ON b.status = true AND a.itemid = b.itemid 
        LEFT JOIN dynamic.mdbom b1 ON b.id = b1.mhbomid 
        LEFT JOIN dynamic.mhroute c ON c.status = true AND a.itemid = c.itemid
        LEFT JOIN dynamic.mdroute c1 ON c.id = c1.mhrouteid 
        WHERE (b.id IS NULL OR b1.id IS NULL OR c.id IS NULL OR c1.id IS NULL) 
        LIMIT 1 
    ) THEN 
        RAISE EXCEPTION 'Unable to release sales when BOM or route is empty.';
    END IF;

    -- Note: Blok logika "mhauth.logintimezone" dihapus 
    --       karena tabel mhauth dan logintimezone tidak ada di skema public maupun dynamic.

    -- 2. Insert ke tsproductionorderbomd jika belum ada
    IF NOT EXISTS(
        SELECT 1 
        FROM dynamic.tsproductionorderbomd a 
        WHERE a.tsproductionorderid = NEW.id
    ) THEN 
        INSERT INTO dynamic.tsproductionorderbomd(
            tsproductionorderid, materialid, materialgroupid, qty, perseries, uomid
        )
        SELECT 
            NEW.id, b.materialid, c.itemgroupid, b.qty, b.perseries, b.uom 
        FROM dynamic.mdbom b 
        LEFT JOIN dynamic.msitem c ON b.materialid = c.id 
        WHERE b.mhbomid = NEW.bomid;
    END IF;

    -- 3. Insert ke tsproductionorderrouted jika belum ada
    IF NOT EXISTS(
        SELECT 1 
        FROM dynamic.tsproductionorderrouted a 
        WHERE a.tsproductionorderid = NEW.id
    ) THEN 
        INSERT INTO dynamic.tsproductionorderrouted(
            tsproductionorderid, resourceid, qty, perseries, uomid, sequence, 
            next, leadday, ismaterialconsumption, proposedtime, proposedmdresourceid
        )
        SELECT 
            a.id, a.resourceid, a.qty, a.perseries, a.uom, a.sequence, 
            a.next, a.leadday, a.materialconsumption, a.proposedtime, a.proposedmdresourceid
        FROM (
            SELECT 
                NEW.id, c.resourceid, c.qty, c.perseries, c.uom, c.sequence, c.next, 
                c.leadday, 
                CASE WHEN c.materialconsumption = true THEN 1 ELSE 0 END AS materialconsumption, 
                b.shippingdateconfirmed::date - (COALESCE(SUM(c.leadday) OVER (ORDER BY sequence DESC, next ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW), 0) * interval '1 days') AS proposedtime, 
                e.id AS proposedmdresourceid 
            FROM dynamic.mdroute c 
            LEFT JOIN dynamic.tssalesline b ON b.id = NEW.tssaleslineid 
            LEFT JOIN dynamic.mhresource d ON c.resourceid = d.msresourceid AND c.msresourcegroupid = d.msresourcegroupid
            LEFT JOIN (
                SELECT MIN(id) id, mhresourceid 
                FROM dynamic.mdresource 
                WHERE isdefaultresource = true 
                GROUP BY mhresourceid 
            ) e ON d.id = e.mhresourceid 
            WHERE c.mhrouteid = NEW.routeid 
        ) a
        ORDER BY a.sequence;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_tsproductionorder_internal ON dynamic.tsproductionorder;

-- Memasang trigger pada tabel tsproductionorder dengan kondisi kolom internal = true
CREATE TRIGGER trg_tsproductionorder_internal
AFTER INSERT OR UPDATE ON dynamic.tsproductionorder
FOR EACH ROW
WHEN (NEW.internal = true)
EXECUTE FUNCTION dynamic.trg_after_tsproductionorder_internal_fn();
