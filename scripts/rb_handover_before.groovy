// RB_HANDOVER_SUM / RB_HANDOVER_DTL - script BEFORE. Mengisi nama asal/tujuan untuk judul, nama Bagian, dan teks NB di template
// (p_from_name, p_to_name). Rubber tidak memakai no. dokumen / rev / eff. date (form lama menyembunyikannya).
// Tujuan 0 = Finished Good (dicetak sebagai "Dispatch").
final long FINISHED_GOOD = 0L

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

// "Rubber - Final Finishing" -> "Final Finishing"
def shortName = { Long id ->
    def rows = dataService.getJdbcTemplate().queryForList("SELECT name FROM dynamic.mdlookup WHERE id = ?", id)
    if (rows.isEmpty()) {
        throw new RuntimeException("Resource " + id + " not found.")
    }
    return rows[0].name.toString().replaceFirst(/^Rubber - /, "")
}

params.p_from_name = shortName(fromId)
params.p_to_name = (toResId == FINISHED_GOOD) ? "Dispatch" : shortName(toResId)
