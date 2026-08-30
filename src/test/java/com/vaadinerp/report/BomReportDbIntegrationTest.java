package com.vaadinerp.report;

import com.vaadinerp.report.render.JasperRenderer;
import com.vaadinerp.report.render.ReportContext;
import com.vaadinerp.report.render.ReportOutput;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class BomReportDbIntegrationTest {

    private Connection connect() {
        String url = System.getProperty("test.db.url", "jdbc:postgresql://localhost:5432/grp");
        try {
            return DriverManager.getConnection(url, "postgres", "postgres");
        } catch (Exception e) {
            return null;
        }
    }

    private String getSeedQuery(Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT data_query FROM public.meta_report WHERE report_code = 'RPT_BOM_DOC_STD'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private boolean checkBomExists(Connection conn, int bomId1, int bomId2) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT count(id) FROM dynamic.mhbom WHERE id IN (?, ?)")) {
            ps.setInt(1, bomId1);
            ps.setInt(2, bomId2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 2;
                }
            }
        }
        return false;
    }

    @Test
    void seedQueryReturnsTwoBoms() throws Exception {
        try (Connection conn = connect()) {
            assumeTrue(conn != null, "DB PostgreSQL tidak tersedia, test di-skip");

            String query = getSeedQuery(conn);
            assumeTrue(query != null, "data_query untuk RPT_BOM_DOC_STD tidak ditemukan, test di-skip");

            boolean hasBoms = checkBomExists(conn, 38, 49);
            assumeTrue(hasBoms, "BOM 38 atau 49 tidak ada di tabel dynamic.mhbom, test di-skip");

            String sql = query.replace(":bom_id", "?, ?");

            Set<Integer> bomIds = new HashSet<>();
            int rowCount = 0;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, 38);
                ps.setInt(2, 49);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        bomIds.add(rs.getInt("bom_id"));
                        rowCount++;
                    }
                }
            }

            assertThat(bomIds).hasSize(2).containsExactlyInAnyOrder(38, 49);
            assertThat(rowCount).isGreaterThan(2);
        }
    }

    @Test
    void realDataGeneratesTwoPagePdf() throws Exception {
        File templateFile = new File("uploads/jasper/RPT_BOM_DOC_JSP.jrxml");
        assumeTrue(templateFile.exists(), "Template " + templateFile.getPath() + " tidak ada, test di-skip");

        try (Connection conn = connect()) {
            assumeTrue(conn != null, "DB PostgreSQL tidak tersedia, test di-skip");

            String query = getSeedQuery(conn);
            assumeTrue(query != null, "data_query untuk RPT_BOM_DOC_STD tidak ditemukan, test di-skip");

            boolean hasBoms = checkBomExists(conn, 38, 49);
            assumeTrue(hasBoms, "BOM 38 atau 49 tidak ada di tabel dynamic.mhbom, test di-skip");

            String sql = query.replace(":bom_id", "?, ?");
            
            List<Map<String, Object>> data = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, 38);
                ps.setInt(2, 49);
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int columns = md.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columns; ++i) {
                            row.put(md.getColumnLabel(i).toLowerCase(), rs.getObject(i));
                        }
                        data.add(row);
                    }
                }
            }
            
            assumeTrue(!data.isEmpty(), "Hasil query kosong, test di-skip");

            ReportContext ctx = new ReportContext(
                    "RPT_BOM_DOC_STD",
                    "JASPER",
                    templateFile,
                    data,
                    new HashMap<>(),
                    null, null, null, null, null
            );

            JasperTemplateService templateService = new JasperTemplateService();
            
            // Hitung halaman menggunakan JasperPrint terlebih dahulu
            JasperReport jr = templateService.loadCompiled(templateFile);
            @SuppressWarnings({"unchecked", "rawtypes"})
            JRMapCollectionDataSource ds = new JRMapCollectionDataSource((java.util.Collection) data);
            JasperPrint print = JasperFillManager.fillReport(jr, new HashMap<>(), ds);
            
            assertThat(print.getPages()).hasSize(2);

            // Export ke PDF
            // DataSource null: jalur ini memakai ctx.data() non-null, koneksi JDBC tidak dipakai.
            JasperRenderer renderer = new JasperRenderer(templateService, null);
            ReportOutput output = renderer.render(ctx);

            byte[] pdfBytes = output.bytes();
            assertThat(pdfBytes).isNotNull().isNotEmpty();
            
            // Verifikasi byte hasil diawali %PDF-
            String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
            assertThat(header).isEqualTo("%PDF-");
        }
    }
}
