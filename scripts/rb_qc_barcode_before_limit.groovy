// RB_QC_BARCODE_LIMIT - script BEFORE. Tiap serial hanya boleh dicetak SEKALI (printcount > 0 ditolak).
// Parameter: p_from_serial, p_to_serial (rentang idno, tanpa membedakan huruf besar/kecil), maksimal 100 label per cetak.
// Syarat cetak = syarat query template: isreject = false, isunique = true, msresourcegroupid = 1255.
// Serial di dalam rentang yang tidak memenuhi syarat MEMBATALKAN seluruh cetakan, dengan pesan yang menyebut serialnya.
final long RESOURCE_GROUP_ID = 1255L
final int MAX_LABELS = 100
final boolean BLOCK_ALREADY_PRINTED = true

def fromSerial = params.p_from_serial?.toString()?.trim()
def toSerial = params.p_to_serial?.toString()?.trim()

if (!fromSerial || !toSerial) {
    throw new RuntimeException("From Serial No and To Serial No are required.")
}

// Perbandingan dilakukan database supaya urutannya sama dengan BETWEEN di query.
def reversed = dataService.getJdbcTemplate().queryForObject("SELECT lower(?) > lower(?)", Boolean.class, fromSerial, toSerial)
if (reversed) {
    throw new RuntimeException("From Serial No must not be greater than To Serial No.")
}

def rows = dataService.getJdbcTemplate().queryForList("""
    SELECT v.idno, v.isreject, v.isunique, v.msresourcegroupid, COALESCE(ps.printcount, 0) AS printcount
      FROM dynamic.v_production_serial v
      LEFT JOIN dynamic.tsproductionserial ps ON ps.id = v.id
     WHERE lower(v.idno) BETWEEN lower(?) AND lower(?)
     ORDER BY v.idno
""", fromSerial, toSerial)

if (rows.isEmpty()) {
    throw new RuntimeException("No serial number found between '" + fromSerial + "' and '" + toSerial + "'.")
}

// Satu serial bisa muncul di beberapa baris view; satu baris bermasalah cukup untuk menandai serialnya.
def bySerial = rows.groupBy { it.idno }

def rejected = []
def notUnique = []
def wrongGroup = []
def unknownStatus = []
def printed = []
bySerial.each { idno, rs ->
    if (rs.any { it.isreject == true }) {
        rejected << idno
    } else if (rs.any { it.isunique != true }) {
        notUnique << idno
    } else if (rs.any { it.msresourcegroupid == null || (it.msresourcegroupid as Long) != RESOURCE_GROUP_ID }) {
        wrongGroup << idno
    } else if (rs.any { it.isreject == null }) {
        unknownStatus << idno
    } else if (BLOCK_ALREADY_PRINTED && rs.any { (it.printcount as Long) > 0 }) {
        printed << idno
    }
}

def show = { List l -> (l.size() <= 10 ? l : l.take(10) + ["(+" + (l.size() - 10) + " more)"]).join(", ") }

if (!rejected.isEmpty()) {
    throw new RuntimeException("The following items are marked as REJECT and cannot be printed: " + show(rejected))
}
if (!notUnique.isEmpty()) {
    throw new RuntimeException("The following items are not unique serial numbers and cannot be printed: " + show(notUnique))
}
if (!wrongGroup.isEmpty()) {
    throw new RuntimeException("The following items are not in resource group " + RESOURCE_GROUP_ID + ": " + show(wrongGroup))
}
if (!unknownStatus.isEmpty()) {
    throw new RuntimeException("The following items have no reject status and cannot be printed: " + show(unknownStatus))
}
if (!printed.isEmpty()) {
    throw new RuntimeException("The following items have already been printed and cannot be printed again: " + show(printed))
}
if (bySerial.size() > MAX_LABELS) {
    throw new RuntimeException("The range contains " + bySerial.size() + " serial numbers; the maximum is " + MAX_LABELS + " labels per print.")
}
