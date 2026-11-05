def detailRows = getElementValue("subform_grid1", false)

if (detailRows == null || detailRows.isEmpty()) {
    showError("Validation", "Data details cannot be empty")
    return false
}

def toLong = { v ->
    (v == null || v.toString().trim().isEmpty()) ? null : (v.toString().trim() as BigDecimal).longValue()
}

// ============================================================
// 1. Validasi: hanya yang memang harus diisi pengguna
//    (next dihitung otomatis di langkah 2)
// ============================================================
def errors = []
def usedSequences = [] as Set

for (int i = 0; i < detailRows.size(); i++) {
    def sequence = toLong(detailRows[i]['sequence'])

    if (sequence == null) {
        errors.add("Row ${i + 1}: The sequence field is mandatory.")
        continue
    }
    if (sequence == 999) {
        errors.add("Row ${i + 1}: The sequence must not be 999.")
    }
    if (!usedSequences.add(sequence)) {
        errors.add("Row ${i + 1}: Duplicate sequence ${sequence}.")
    }
}

if (!errors.isEmpty()) {
    showError("Validation", "Found ${errors.size()} validation error(s).")
    errors.each { error ->
        showError("Validation", error)
    }
    return false
}

// ============================================================
// 2. Urutkan berdasarkan sequence (di tempat: daftar inilah yang disimpan),
//    lalu hitung ulang next: next = sequence baris berikutnya, baris terakhir = 999.
//    Menyisip atau menghapus baris di tengah otomatis menyambung rantainya.
//    JANGAN menomori ulang sequence yang sudah ada: nilainya dipakai pelacakan produksi (tdbook.next).
// ============================================================
detailRows.sort { toLong(it['sequence']) }

for (int i = 0; i < detailRows.size(); i++) {
    detailRows[i]['next'] = (i < detailRows.size() - 1) ? toLong(detailRows[i + 1]['sequence']) : 999L
}

return true
