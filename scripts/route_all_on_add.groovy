// On Add Row - subform_grid1 (ROUTE_ALL)
// Baris pertama = material consumption.
if (rowIndex == 1) {
    row.materialconsumption = true
} else {
    row.materialconsumption = false
}

// Nomor urut otomatis = sequence terbesar + 10 (selisih 10 menyisakan ruang untuk menyisip, mis. 15 di antara 10 dan 20).
// Baris yang sebelumnya terakhir langsung menunjuk ke baris baru, dan baris baru menjadi yang terakhir (next = 999).
// BEFORE_SAVE menghitung ulang next semua baris; ini hanya supaya rantainya sudah benar sejak baris ditambahkan.
def lastRow = null
def lastSeq = null
items.each { r ->
    def s = (r.sequence == null || r.sequence.toString().trim().isEmpty()) ? null : (r.sequence.toString().trim() as BigDecimal).longValue()
    if (s != null && (lastSeq == null || s > lastSeq)) {
        lastSeq = s
        lastRow = r
    }
}
def newSeq = (lastSeq == null) ? 10L : lastSeq + 10L
row.sequence = newSeq
row.next = 999L
if (lastRow != null) {
    lastRow.next = newSeq
}

// Lookup data dari tabel lain di database
def item = db.find('msmethod', 'itemid',header.itemid)
if (item != null) {
    row.qty = item.pcsperbox
}
