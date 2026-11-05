// // =============================================================================
// // BAGIAN 1: Validasi Hak Akses User terhadap Resource (Existing Logic)
// // =============================================================================
// def countAdmin = db.getValue("""
//     select count('x') from public.app_user_roles a
//     where a.username = ?
//     and a.role_code in ('SUPER_ADMIN','ADMIN')
// """, ctx.getUserId())

// def isUserAdmin = (countAdmin != null && countAdmin > 0)

// if (!isUserAdmin) {
//     def resource = db.getValue("""
//         SELECT distinct STRING_AGG(a.msresourceid::text, ', ' ORDER BY a.msresourceid) AS msresourceids
//         FROM (
//             SELECT DISTINCT msresourceid
//             FROM dynamic.tsresourceaccess
//             WHERE userid IN (
//                 SELECT role_code 
//                 FROM public.app_user_roles 
//                 WHERE username = ?
//             )
//         ) a
//     """, ctx.getUserId())

//     if (resource == null || resource.trim().isEmpty()) {
//         showError("Validation", "No resource access defined for user")
//         return false
//     }

//     def allowedResources = resource.split(',').collect { it.trim() }
//     def targetResourceId = (header.resourceid instanceof Map ? header.resourceid.id : header.resourceid)?.toString()?.trim()

//     if (!targetResourceId || !allowedResources.contains(targetResourceId)) {
//         showError("Validation", "You do not have the authority to save to the resource of ${header.resourceid?.description ?: targetResourceId}")
//         return false
//     }
// }

// // 2. Ambil semua data baris detail
// def detailRows = getElementValue("tdbook", false) 
// def totalQty = 0.0
// if (detailRows.isEmpty()) {
//  showError("Validation", "Details Not Yet Filled In")
//  return false
// }

// // 3. Jumlahkan semua qty detail
// if (detailRows != null) {
//     for (row in detailRows) {
//         def qtyDetail = row['quantity']
//         if (qtyDetail != null && qtyDetail.toString().trim() != "") {
//             totalQty += new BigDecimal(qtyDetail.toString())
//         }

//         if(row.tagid){

//              //def allsavedqty = db.getValue(" select coalesce(sum(b.qty), 0) qty from thbook a left join tdbook b on a.id=b.thbookid where a.resourceid=? and b.tagid=? ", header.resourceid.id , row.tagid );
//              def existingrowqty = db.getValue(" select coalesce(max(qty), 0) qty from tdbook where id=? ", row.id ?: 0 );
//              def outsqty= db.getValue(" select coalesce(a.quantity, 0) qty from v_production_tag a where a.id=? ", row.tagid );
//             /*
//              if(allsavedqty-existingrowqty+row.qty > tagqty) { 
//                  showError("Validation", " Qty > Tag Qty ");
//                  return false;
//               }
//               */
//              if(row.qty > outsqty) { 
//                  showError("Validation", " Qty > Tag Qty ");
//                  return false;
//               }

//         } else {
        
//              //def allsavedqty = db.getValue(" select coalesce(sum(b.qty), 0) qty from thbook a left join tdbook b on a.id=b.thbookid where a.resourceid=? and b.serialid=? ", header.resourceid.id , row.serialid );
//              def existingrowqty = db.getValue(" select coalesce(max(qty), 0) qty from tdbook where id=? ", row.id ?: 0 );
//              def outsqty= db.getValue(" select coalesce(a.quantity, 0) qty from v_production_serial a where a.id=? ", row.serialid );

//              if(row.qty > outsqty) { 
//                  showError("Validation", " Qty > Serial Qty ");
//                  return false;
//               }
            
//         }
//     }

// }

// return true
// =============================================================================
// BAGIAN 1: Validasi Hak Akses User terhadap Resource (Existing Logic)
// =============================================================================
def countAdmin = db.getValue("""
    select count('x') from public.app_user_roles a
    where a.username = ?
    and a.role_code in ('SUPER_ADMIN','ADMIN')
""", ctx.getUserId())

def isUserAdmin = (countAdmin != null && countAdmin > 0)

if (!isUserAdmin) {
    def resource = db.getValue("""
        SELECT distinct STRING_AGG(a.msresourceid::text, ', ' ORDER BY a.msresourceid) AS msresourceids
        FROM (
            SELECT DISTINCT msresourceid
            FROM dynamic.tsresourceaccess
            WHERE userid IN (
                SELECT role_code 
                FROM public.app_user_roles 
                WHERE username = ?
            )
        ) a
    """, ctx.getUserId())

    if (resource == null || resource.trim().isEmpty()) {
        showError("Validation", "No resource access defined for user")
        return false
    }

    def allowedResources = resource.split(',').collect { it.trim() }
    def targetResourceId = (header.resourceid instanceof Map ? header.resourceid.id : header.resourceid)?.toString()?.trim()

    if (!targetResourceId || !allowedResources.contains(targetResourceId)) {
        showError("Validation", "You do not have the authority to save to the resource of ${header.resourceid?.description ?: targetResourceId}")
        return false
    }
}

// =============================================================================
// BAGIAN 2: Validasi Qty Detail Grid tdbook (Anti-Overbooking)
// =============================================================================
def detailRows = getElementValue("tdbook", false)
if (!detailRows || detailRows.isEmpty()) {
    showError("Validation", "Booking details are empty. Please add at least one row.")
    return false
}

// Ambil ID resource saat ini
def currentMhResourceId = (header.resourceid instanceof Map) ? header.resourceid.id : header.resourceid
def currentLookupResourceId = db.getValue("SELECT msresourceid FROM dynamic.mhresource WHERE id = ?", currentMhResourceId)

// [BARU] Header ini dikeluarkan dari hitungan DB: isi grid saat ini yang menggantikannya
// (baris yang dihapus/diedit di layar tidak boleh ikut terhitung).
def thisHeaderId = header.id ?: 0

// Map akumulasi grid saat ini
def tagInputs = [:]    // key: tagid
def serialInputs = [:] // key: serialid

for (row in detailRows) {
    def rawQty = row.qty ?: row.quantity
    if (rawQty == null || rawQty.toString().trim() == "") {
        showError("Validation", "Quantity cannot be empty in detail rows.")
        return false
    }
    def rowQty = new BigDecimal(rawQty.toString())
    if (rowQty <= 0) {
        showError("Validation", "Entered quantity must be greater than 0.")
        return false
    }

    def tagId = (row.tagid instanceof Map) ? row.tagid.id : row.tagid
    def serialId = (row.serialid instanceof Map) ? row.serialid.id : row.serialid

    if (!tagId && !serialId) {
        showError("Validation", "Either Tag ID or Serial No must be selected in each detail row.")
        return false
    }

        // [LAMA] def existingQty = 0.0
    // [LAMA] if (row.id) {
    // [LAMA] def old = db.getValue("SELECT COALESCE(qty, 0) FROM dynamic.tdbook WHERE id = ?", row.id) ?: 0
    // [LAMA] existingQty = new BigDecimal(old.toString())
    // [LAMA] }
    // [BARU] qty lama per baris tidak perlu dicari: header ini dikeluarkan langsung di query bawah.

    if (tagId) {
        if (!tagInputs.containsKey(tagId)) {
            // [LAMA] tagInputs[tagId] = [totalGridQty: 0.0, existingRowQty: 0.0]
            tagInputs[tagId] = [totalGridQty: 0.0]
        }
        tagInputs[tagId].totalGridQty += rowQty
        // [LAMA] tagInputs[tagId].existingRowQty += existingQty
    }

    if (serialId) {
        if (!serialInputs.containsKey(serialId)) {
            // [LAMA] serialInputs[serialId] = [totalGridQty: 0.0, existingRowQty: 0.0]
            serialInputs[serialId] = [totalGridQty: 0.0]
        }
        serialInputs[serialId].totalGridQty += rowQty
        // [LAMA] serialInputs[serialId].existingRowQty += existingQty
    }
}

// -----------------------------------------------------------------------------
// Validasi Baris Tag
// -----------------------------------------------------------------------------
for (entry in tagInputs) {
    def tId = entry.key
    def info = entry.value

    def tagNo = db.getValue("SELECT idno FROM dynamic.thproductiontag WHERE id = ?", tId) ?: tId.toString()
    def maxTagQtyVal = db.getValue("SELECT COALESCE(quantity, 0) FROM dynamic.thproductiontag WHERE id = ?", tId) ?: 0
    def maxTagQty = new BigDecimal(maxTagQtyVal.toString())

    def allSavedQty = db.getValue("""
        SELECT COALESCE(SUM(b.qty), 0)
        FROM dynamic.thbook a
        JOIN dynamic.tdbook b ON a.id = b.thbookid
        WHERE a.resourceid = ? AND b.tagid = ?
          AND a.id IS DISTINCT FROM ?
    """, currentMhResourceId, tId, thisHeaderId) ?: 0
    // [LAMA] query tanpa "AND a.id IS DISTINCT FROM ?", parameter hanya (currentMhResourceId, tId)
    // [LAMA] def alreadyBookedOther = new BigDecimal(allSavedQty.toString()) - info.existingRowQty
    def alreadyBookedOther = new BigDecimal(allSavedQty.toString())
    if (alreadyBookedOther < 0) alreadyBookedOther = 0.0

    def remainingAllowed = maxTagQty - alreadyBookedOther
    if (remainingAllowed < 0) remainingAllowed = 0.0

    if (info.totalGridQty > remainingAllowed) {
        showError("Validation", 
            "Booking quantity for Tag [${tagNo}] exceeds available capacity!<br/>" +
            "• Tag Total Qty: <b>${maxTagQty}</b><br/>" +
            "• Already Booked at this Resource: <b>${alreadyBookedOther}</b><br/>" +
            "• Max Remaining to Book: <b>${remainingAllowed}</b><br/>" +
            "• Total Quantity Entered: <b>${info.totalGridQty}</b>"
        )
        return false
    }
}

// -----------------------------------------------------------------------------
// Validasi Baris Serial (Tidak boleh melebihi qty tahap sebelumnya, dikurangi reject)
// -----------------------------------------------------------------------------
for (entry in serialInputs) {
    def sId = entry.key
    def info = entry.value

    def serialNo = db.getValue("SELECT idno FROM dynamic.tsproductionserial WHERE id = ?", sId) ?: sId.toString()

    // 1. Cek Qty yang masuk dari tahap sebelumnya (Next Resource = Resource saat ini)
    def prevStageQtyVal = db.getValue("""
        SELECT COALESCE(SUM(qty), 0)
        FROM dynamic.tdbook
        WHERE serialid = ? AND nextresourceid = ?
    """, sId, currentLookupResourceId) ?: 0
    def inflowQty = new BigDecimal(prevStageQtyVal.toString())

    // 2. Jika belum pernah dibooking sebelumnya (tahap serial pertama), ambil dari kuota serial di Tag
    if (inflowQty <= 0) {
        def initialSerialQty = db.getValue("""
            SELECT COALESCE(quantity, 0)
            FROM dynamic.tdproductiontag
            WHERE tsproductionserialid = ?
        """, sId) ?: 0
        inflowQty = new BigDecimal(initialSerialQty.toString())
    }

    // 3. Cek berapa yang sudah pernah dibooking keluar dari resource saat ini
    def allSavedFromCurrent = db.getValue("""
        SELECT COALESCE(SUM(b.qty), 0)
        FROM dynamic.thbook a
        JOIN dynamic.tdbook b ON a.id = b.thbookid
        WHERE a.resourceid = ? AND b.serialid = ?
          AND a.id IS DISTINCT FROM ?
    """, currentMhResourceId, sId, thisHeaderId) ?: 0
    // [LAMA] query tanpa "AND a.id IS DISTINCT FROM ?", parameter hanya (currentMhResourceId, sId)
    // [LAMA] def alreadyBookedOther = new BigDecimal(allSavedFromCurrent.toString()) - info.existingRowQty
    def alreadyBookedOther = new BigDecimal(allSavedFromCurrent.toString())
    if (alreadyBookedOther < 0) alreadyBookedOther = 0.0

    // 3b. Qty yang direject DI resource saat ini (scrap, tidak bisa dibooking maju)
    def rejectHereVal = db.getValue("""
        SELECT COALESCE(SUM(COALESCE(accumulatedrejectqty, 0)), 0)
        FROM dynamic.tdbook
        WHERE serialid = ? AND nextresourceid = ?
    """, sId, currentLookupResourceId) ?: 0
    def rejectedHere = new BigDecimal(rejectHereVal.toString())

    // 4. Sisa yang boleh dibooking = inflow − reject − sudah dibooking
    def remainingAllowed = inflowQty - rejectedHere - alreadyBookedOther
    if (remainingAllowed < 0) remainingAllowed = 0.0

    // 5. Validasi
    if (info.totalGridQty > remainingAllowed) {
        showError("Validation", 
            "Booking quantity for Serial No [${serialNo}] exceeds available quantity!<br/>" +
            "• Qty from Previous Stage: <b>${inflowQty}</b><br/>" +
            "• Rejected at this Resource: <b>${rejectedHere}</b><br/>" +
            "• Already Booked at this Resource: <b>${alreadyBookedOther}</b><br/>" +
            "• Max Remaining to Book: <b>${remainingAllowed}</b><br/>" +
            "• Total Quantity Entered: <b>${info.totalGridQty}</b>"
        )
        return false
    }
}

return true
