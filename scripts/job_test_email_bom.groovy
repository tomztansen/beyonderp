// TEST kirim email: render report RPT_BOM_DOC_JSP (BOM terbaru) jadi PDF lalu kirim ke Tommy.
// Nama file tetap, jadi tiap uji menimpa file lama (folder report_out tidak membengkak).
def to = 'tommy@growthsteel.com'

def bomId = db.getValue("SELECT max(id) FROM dynamic.mhbom")
if (!bomId) {
    throw new IllegalStateException('Tidak ada data BOM untuk dilaporkan')
}
def idno = db.getValue("SELECT idno FROM dynamic.mhbom WHERE id = ?", bomId)

def file = renderReport('RPT_BOM_DOC_JSP', [bom_id: bomId.toString()], 'PDF', 'BOM_TEST_EMAIL')

sendEmail(to, null,
        "[TEST] BOM ${idno} - job ${jobCode}",
        "<p>Uji kirim email dari Scheduled Job <b>${jobCode}</b>.</p>" +
        "<p>Lampiran: report <b>RPT_BOM_DOC_JSP</b> untuk BOM ${idno}.</p>",
        file)
log.info("Email uji masuk antrean: {} lampiran {}", to, file)
