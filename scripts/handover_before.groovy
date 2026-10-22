// RPT_HANDOVER_SUM / RPT_HANDOVER_DTL - script BEFORE. Mengisi kepala dokumen untuk template (p_from_name, p_to_name,
// p_doc_no, p_doc_rev, p_doc_eff). Pasangan (resource asal, resource tujuan) yang tidak terdaftar di DOCS TETAP dicetak,
// hanya no. dokumen / rev / eff. date-nya kosong. Tujuan 0 = Finished Good (dicetak sebagai "Dispatch").
// Menambah jenis serah terima baru: tambah satu baris di DOCS (id mdlookup resource asal, id tujuan; 0 = Finished Good).
final long FINISHED_GOOD = 0L

// [asal, tujuan] -> [no. dokumen, rev, eff. date] (teks persis seperti di form)
final Map DOCS = [
    ([1985L, 2438L]): ["GA/FM/QC-034", "'26-01", "01 Maret 2026"],   // Final Finishing -> QC Pre Assembly
    ([2438L, 2437L]): ["GA/FM/QC-034", "'26-01", "01 Maret 2026"],   // QC Pre Assembly -> Fabrication
    ([1986L, FINISHED_GOOD]): ["GA/FM/QC-011", "'22-04", "03 Jan 2022"]   // QC -> Dispatch (Finished Good)
]

def toId = { String name ->
    def v = params[name]
    if (v == null || v.toString().trim().isEmpty()) {
        throw new RuntimeException(name == "p_from_resource" ? "From Resource is required." : "To Resource is required.")
    }
    try {
        return v.toString().trim() as Long
    } catch (NumberFormatException ex) {
        throw new RuntimeException("Invalid value for " + name + ": " + v)
    }
}

def fromId = toId("p_from_resource")
def toResId = toId("p_to_resource")

if (params.p_from_date == null || params.p_to_date == null) {
    throw new RuntimeException("From Booking Date and To Booking Date are required.")
}
if (params.p_from_date.isAfter(params.p_to_date)) {
    throw new RuntimeException("From Booking Date must not be after To Booking Date.")
}
if (!(params.p_item_scope in ["ONLY", "EXCLUDE"])) {
    throw new RuntimeException("Item Scope is required.")
}

// "Foundry - Final Finishing" -> "Final Finishing"
def shortName = { Long id ->
    def rows = dataService.getJdbcTemplate().queryForList("SELECT name FROM dynamic.mdlookup WHERE id = ?", id)
    if (rows.isEmpty()) {
        throw new RuntimeException("Resource " + id + " not found.")
    }
    return rows[0].name.toString().replaceFirst(/^Foundry - /, "")
}

params.p_from_name = shortName(fromId)
params.p_to_name = (toResId == FINISHED_GOOD) ? "Dispatch" : shortName(toResId)

def doc = DOCS.get([fromId, toResId])
params.p_doc_no = doc ? doc[0] : null
params.p_doc_rev = doc ? doc[1] : null
params.p_doc_eff = doc ? doc[2] : null
