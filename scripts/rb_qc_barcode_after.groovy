// RB_QC_BARCODE_LIMIT / RB_QC_BARCODE_AUTH - script AFTER: hanya mencatat print count (tanpa alokasi NDT, tanpa CSV).
// Serial yang dinaikkan = serial yang tercetak = syarat yang sama dengan query template dan script BEFORE.
def fromSerial = params.p_from_serial?.toString()?.trim()
def toSerial = params.p_to_serial?.toString()?.trim()

if (fromSerial && toSerial) {
    try {
        int updatedRows = dataService.getJdbcTemplate().update("""
            UPDATE dynamic.tsproductionserial
               SET printcount = COALESCE(printcount, 0) + 1,
                   printby = ?,
                   printdt = CURRENT_TIMESTAMP,
                   updateby = ?,
                   updatedt = CURRENT_TIMESTAMP
             WHERE id IN (
                   SELECT v.id
                     FROM dynamic.v_production_serial v
                    WHERE lower(v.idno) BETWEEN lower(?) AND lower(?)
                      AND v.isreject = FALSE
                      AND v.isunique = TRUE
                      AND v.msresourcegroupid = 1255)
        """, username, username, fromSerial, toSerial)
        log.info("Print count updated for {} serial(s) in range [{} .. {}] by {}", updatedRows, fromSerial, toSerial, username)
    } catch (Exception ex) {
        log.error("Failed to update print count for range [{} .. {}]: {}", fromSerial, toSerial, ex.getMessage(), ex)
        throw new RuntimeException("Report rendered but failed to record print count: " + ex.getMessage(), ex)
    }
}
