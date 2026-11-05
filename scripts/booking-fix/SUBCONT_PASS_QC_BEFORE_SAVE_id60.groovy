// 1. Retrieve detail rows from subform grid 'tdbook'
def detailRows = getElementValue("tdbook", false)
if (!detailRows || detailRows.isEmpty()) {
    showError("Validation", "Booking details are empty. Please add at least one Serial No row.")
    return false
}

// 2. Accumulate quantity per serial in the current grid
// (Prevents overbooking if the same serial is entered across multiple rows)
def serialInputs = [:] 

// [BARU] Header ini dikeluarkan dari hitungan DB: isi grid saat ini yang menggantikannya
def thisHeaderId = header.id ?: 0

for (row in detailRows) {
    // Extract Serial Subcon ID (handles Map object or direct ID)
    def serialId = (row.tsproductionserialsubcon instanceof Map) 
        ? row.tsproductionserialsubcon.id 
        : row.tsproductionserialsubcon

    if (!serialId) {
        showError("Validation", "Serial No has not been selected for one or more detail rows.")
        return false
    }

    // Validate quantity input
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

    // Retrieve master serial information from dynamic.tsproductionserialsubcon if not yet cached
    if (!serialInputs.containsKey(serialId)) {
        def serialInfo = db.find("tsproductionserialsubcon", "id", serialId)
        def maxQtyVal = serialInfo?.quantity != null 
            ? new BigDecimal(serialInfo.quantity.toString()) 
            : new BigDecimal(db.getValue("SELECT COALESCE(quantity, 0) FROM dynamic.tsproductionserialsubcon WHERE id = ?", serialId)?.toString() ?: "0")
        def serialNoVal = serialInfo?.idno ?: db.getValue("SELECT idno FROM dynamic.tsproductionserialsubcon WHERE id = ?", serialId) ?: serialId.toString()
        def isUniqueVal = serialInfo?.isunique != null 
            ? (serialInfo.isunique == true || serialInfo.isunique.toString() == 't' || serialInfo.isunique.toString() == 'true')
            : false

        serialInputs[serialId] = [
            serialNo: serialNoVal,
            isUnique: isUniqueVal,
            maxQty: maxQtyVal,
            totalGridQty: 0.0
            // [LAMA] totalGridQty: 0.0,
            // [LAMA] existingRowQty: 0.0
        ]
    }

    // Accumulate total quantity entered in this grid for this serial
    serialInputs[serialId].totalGridQty += rowQty

    // [LAMA] // If EDITING an existing saved row in tdbook, track previous qty so it won't be double counted
    // [LAMA] if (row.id) {
    // [LAMA] def oldQty = db.getValue("SELECT COALESCE(qty, 0) FROM dynamic.tdbook WHERE id = ?", row.id) ?: 0
    // [LAMA] serialInputs[serialId].existingRowQty += new BigDecimal(oldQty.toString())
    // [LAMA] }
    // [BARU] tidak perlu: header ini dikeluarkan langsung di query bawah.
}

// 3. Validate booking quota for each Serial No
for (entry in serialInputs) {
    def sId = entry.key
    def info = entry.value

    // Calculate total accumulated quantity already saved in database for this serial
    def allSavedQty = db.getValue("""
        SELECT COALESCE(SUM(b.qty), 0)
        FROM dynamic.thbook a
        JOIN dynamic.tdbook b ON a.id = b.thbookid
        WHERE a.issubcon = TRUE 
          AND b.tsproductionserialsubcon = ?
          AND a.id IS DISTINCT FROM ?
    """, sId, thisHeaderId) ?: 0
    // [LAMA] query tanpa "AND a.id IS DISTINCT FROM ?", parameter hanya (sId)
    def totalSavedInDb = new BigDecimal(allSavedQty.toString())

    // [LAMA] // Quantity already booked by OTHER transactions (excluding the rows currently being edited)
    // [LAMA] def alreadyBookedOther = totalSavedInDb - info.existingRowQty
    // [LAMA] if (alreadyBookedOther < 0) alreadyBookedOther = 0.0
    // [BARU] Query di atas sudah mengecualikan header ini, jadi seluruhnya milik transaksi lain
    def alreadyBookedOther = totalSavedInDb

    // Case A: Unique Serial (isunique = true)
    if (info.isUnique) {
        if (info.totalGridQty > 1) {
            showError("Validation", "Serial No [${info.serialNo}] is a unique serial and cannot have a quantity greater than 1.")
            return false
        }
        if (alreadyBookedOther >= 1) {
            showError("Validation", "Serial No [${info.serialNo}] is a unique serial and has already been booked.")
            return false
        }
    }

    // Case B: Batch / Non-Unique Serial (isunique = false)
    def remainingAllowed = info.maxQty - alreadyBookedOther
    if (remainingAllowed < 0) remainingAllowed = 0.0

    // Check if total quantity entered exceeds remaining quota
    if (info.totalGridQty > remainingAllowed) {
        showError("Validation", 
            "Booking quantity for Serial No [${info.serialNo}] exceeds available capacity!<br/>" +
            "• Total Serial Qty: <b>${info.maxQty}</b><br/>" +
            "• Previously Booked: <b>${alreadyBookedOther}</b><br/>" +
            "• Max Remaining to Book: <b>${remainingAllowed}</b><br/>" +
            "• Total Quantity Entered: <b>${info.totalGridQty}</b>"
        )
        return false
    }
}

// All validation passed, proceed with save
return true
