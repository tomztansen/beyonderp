-- =========================================================================================
-- STORED PROCEDURE: generate_production_tag
-- SCHEMA: dynamic
-- DESKRIPSI:
--   Procedure untuk membuat (generate) Production Tag secara otomatis berdasarkan
--   Production Order yang dipilih, menghitung jumlah box berdasarkan kuantitas yang dipesan
--   dan kapasitas per box (pcsperbox), serta memperbarui status Production Order menjadi WIP.
-- =========================================================================================

CREATE OR REPLACE PROCEDURE dynamic.generate_production_tag(
    IN puserid character varying,
    IN ptsproductionorderid bigint,
    IN pisunique integer
)
LANGUAGE plpgsql
AS $procedure$
DECLARE
    vmsresourcegroupid bigint;
    vqtyordered numeric;
    vquantityperbox integer;
    v_total_boxes integer;
    v_tag_status_id bigint;
    v_order_status_id bigint;
    i integer;
BEGIN
    -- 1. Ambil data resourcegroupid, qtyordered, dan pcsperbox dari Production Order, Sales Line, & Method
    SELECT COALESCE(a.resourcegroupid, b.resourcegroup),
           COALESCE(b.qtyordered, a.qty, 0),
           COALESCE(c.pcsperbox, 1)
      INTO vmsresourcegroupid, vqtyordered, vquantityperbox
      FROM dynamic.tsproductionorder a
      LEFT JOIN dynamic.tssalesline b ON a.tssaleslineid = b.id
      LEFT JOIN dynamic.msmethod c ON COALESCE(b.itemid, a.itemid) = c.itemid AND COALESCE(c.status, true) = true
     WHERE a.id = ptsproductionorderid
     ORDER BY COALESCE(c.version, 0) DESC
     LIMIT 1;

    -- Safety check pencegahan division by zero atau nilai null/negatif
    IF vquantityperbox IS NULL OR vquantityperbox <= 0 THEN
        vquantityperbox := 1;
    END IF;
    IF vqtyordered IS NULL OR vqtyordered < 0 THEN
        vqtyordered := 0;
    END IF;

    -- Hitung total box yang akan di-generate
    v_total_boxes := CEIL(vqtyordered / vquantityperbox::numeric)::integer;
    IF v_total_boxes <= 0 THEN
        v_total_boxes := 1;
    END IF;

    -- 2. Ambil ID lookup status untuk Production Order (MAS0026 code '2' = WIP)
    SELECT b.id INTO v_order_status_id
      FROM dynamic.mhlookup a
      JOIN dynamic.mdlookup b ON a.id = b.global_category_id
     WHERE a.category_code = 'MAS0026' AND b.code = '2'
     LIMIT 1;

    -- Update status tsproductionorder menjadi WIP beserta audit trail & version
    UPDATE dynamic.tsproductionorder
       SET status = COALESCE(v_order_status_id, status),
           statusdate = NOW(),
           updateby = puserid,
           updatedt = NOW()::timestamp,
           version = COALESCE(version, 0) + 1
     WHERE id = ptsproductionorderid;

    -- 3. Ambil ID lookup status awal untuk Production Tag (MAS0040 code '1' = Assigned)
    SELECT b.id INTO v_tag_status_id
      FROM dynamic.mhlookup a
      JOIN dynamic.mdlookup b ON a.id = b.global_category_id
     WHERE a.category_code = 'MAS0040' AND b.code = '1'
     LIMIT 1;

    -- 4. Generate & insert production tags ke tabel dynamic.thproductiontag
    FOR i IN 1..v_total_boxes LOOP
        INSERT INTO dynamic.thproductiontag (
            id,
            idno,
            msresourcegroupid,
            tsproductionorderid,
            quantityperbox,
            quantity,
            isunique,
            status,
            statusby,
            statusdate,
            inputby,
            inputdt,
            version
        ) VALUES (
            nextval('dynamic.thproductiontag_id_seq'::regclass),
            dynamic.add_serial_no('TAG_NO'),
            vmsresourcegroupid,
            ptsproductionorderid,
            vquantityperbox,
            LEAST(vqtyordered - ((i - 1) * vquantityperbox), vquantityperbox::numeric),
            pisunique,
            v_tag_status_id,
            puserid,
            NOW(),
            puserid,
            NOW()::timestamp,
            0
        );
    END LOOP;

END;
$procedure$;


-- =========================================================================================
-- WRAPPER PROCEDURE UNTUK PEMANGGILAN VIA ACTION STUDIO / DSL (executeProcedure)
-- =========================================================================================
CREATE OR REPLACE PROCEDURE dynamic.generate_production_tag(
    IN p_params json,
    IN puserid character varying
)
LANGUAGE plpgsql
AS $procedure$
DECLARE
    v_order_id bigint;
    v_is_unique integer := 1;
    v_is_unique_str text;
BEGIN
    IF p_params IS NOT NULL THEN
        IF json_typeof(p_params) = 'object' THEN
            v_order_id := COALESCE(
                (p_params->>'tsproductionorderid')::bigint,
                (p_params->>'productionorderid')::bigint,
                (p_params->>'orderid')::bigint,
                (p_params->>'id')::bigint
            );
            v_is_unique_str := p_params->>'isunique';
        ELSIF json_typeof(p_params) = 'array' THEN
            v_order_id := (p_params->>0)::bigint;
            v_is_unique_str := p_params->>1;
        ELSIF json_typeof(p_params) = 'number' OR json_typeof(p_params) = 'string' THEN
            v_order_id := (p_params#>>'{}')::bigint;
            v_is_unique_str := '1';
        END IF;

        IF v_is_unique_str IS NOT NULL THEN
            IF lower(v_is_unique_str) IN ('true', 't', 'yes', 'y') THEN
                v_is_unique := 1;
            ELSIF lower(v_is_unique_str) IN ('false', 'f', 'no', 'n') THEN
                v_is_unique := 0;
            ELSE
                BEGIN
                    v_is_unique := v_is_unique_str::integer;
                EXCEPTION WHEN OTHERS THEN
                    v_is_unique := 1;
                END;
            END IF;
        ELSE
            v_is_unique := 1;
        END IF;
    END IF;

    IF v_order_id IS NULL THEN
        RAISE EXCEPTION 'ID Production Order tidak ditemukan dari parameter JSON procedure generate_production_tag: %', p_params;
    END IF;

    CALL dynamic.generate_production_tag(puserid, v_order_id, v_is_unique);
END;
$procedure$;


CREATE OR REPLACE PROCEDURE dynamic.proc_5(
    IN p_params json,
    IN puserid character varying
)
LANGUAGE plpgsql
AS $procedure$
BEGIN
    CALL dynamic.generate_production_tag(p_params, puserid);
END;
$procedure$;


CREATE OR REPLACE PROCEDURE dynamic.generate_production_tag(
    IN puserid character varying,
    IN ptsproductionorderid bigint,
    IN pisunique boolean
)
LANGUAGE plpgsql
AS $procedure$
BEGIN
    CALL dynamic.generate_production_tag(puserid, ptsproductionorderid, CASE WHEN pisunique THEN 1 ELSE 0 END);
END;
$procedure$;


CREATE OR REPLACE PROCEDURE dynamic.proc_5(
    IN puserid character varying,
    IN ptsproductionorderid bigint,
    IN pisunique integer
)
LANGUAGE plpgsql
AS $procedure$
BEGIN
    CALL dynamic.generate_production_tag(puserid, ptsproductionorderid, pisunique);
END;
$procedure$;


CREATE OR REPLACE PROCEDURE dynamic.proc_5(
    IN puserid character varying,
    IN ptsproductionorderid bigint,
    IN pisunique boolean
)
LANGUAGE plpgsql
AS $procedure$
BEGIN
    CALL dynamic.generate_production_tag(puserid, ptsproductionorderid, CASE WHEN pisunique THEN 1 ELSE 0 END);
END;
$procedure$;

