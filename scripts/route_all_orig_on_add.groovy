// ✨ Di-generate oleh AI Assistant dari perintah: "jika baris pertama kolom materialcompsumtion = true"
if (rowIndex == 1) {
    row.materialconsumption = true
} else {
    row.materialconsumption = false
}
// Lookup data dari tabel lain di database
def item = db.find('msmethod', 'itemid',header.itemid)
if (item != null) {
    row.qty = item.pcsperbox
}
