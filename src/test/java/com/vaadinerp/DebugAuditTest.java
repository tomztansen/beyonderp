package com.vaadinerp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;
import com.vaadinerp.meta.FormMeta;

@SpringBootTest
public class DebugAuditTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void dumpAuditAndDtl() {
        System.out.println("=== RECENT SYS_AUDIT_LOG ===");
        List<Map<String, Object>> logs = jdbcTemplate.queryForList("SELECT id, table_name, record_id, action_type, action_dt, old_data_json FROM sys_audit_log ORDER BY action_dt DESC LIMIT 15");
        for (Map<String, Object> log : logs) {
            System.out.println(log);
        }

        System.out.println("=== SCROLL_DTL ROWS ===");
        try {
            List<Map<String, Object>> dtls = jdbcTemplate.queryForList("SELECT * FROM scroll_dtl LIMIT 20");
            for (Map<String, Object> d : dtls) {
                System.out.println(d);
            }
            List<Map<String, Object>> auditDtls = jdbcTemplate.queryForList("SELECT id, table_name, record_id, action_type, old_data_json FROM sys_audit_log WHERE table_name = 'scroll_dtl'");
            System.out.println("=== SCROLL_DTL IN SYS_AUDIT_LOG ===");
            for (Map<String, Object> a : auditDtls) {
                System.out.println(a);
            }
        } catch (Exception e) {
            System.out.println("Error reading scroll_dtl: " + e.getMessage());
        }

        System.out.println("=== INV_HDR ROWS ===");
        try {
            List<Map<String, Object>> hdrs = jdbcTemplate.queryForList("SELECT * FROM inv_hdr LIMIT 20");
            for (Map<String, Object> h : hdrs) {
                System.out.println(h);
            }
        } catch (Exception e) {
            System.out.println("Error reading inv_hdr: " + e.getMessage());
        }
    }

    @Autowired
    private com.vaadinerp.service.DynamicDataService dynamicDataService;
    @Autowired
    private com.vaadinerp.meta.FormMetaRepository formMetaRepository;

    @Test
    public void testSubformSaveAndDeleteAudit() {
        com.vaadinerp.meta.FormMeta scrollForm = formMetaRepository.findById("SCROLL_MD").orElse(null);
        if (scrollForm == null) return;

        Map<String, Object> hdrData = new java.util.HashMap<>();
        hdrData.put("invoice_no", "TEST-CASCADE-001");
        hdrData.put("invoice_date", java.time.LocalDate.parse("2026-07-09"));
        hdrData.put("customer", "IT");

        Map<String, Object> dtl1 = new java.util.HashMap<>();
        dtl1.put("item_code", "IT-DEV");
        dtl1.put("qty", 2);
        dtl1.put("price", 1000.0);

        java.util.List<Map<String, Object>> detailsList = new java.util.ArrayList<>();
        detailsList.add(dtl1);
        hdrData.put("details", detailsList);

        dynamicDataService.saveData(scrollForm, hdrData);

        Object savedId = hdrData.get("id");
        System.out.println(">> SAVED SCROLL_HDR ID: " + savedId);
        java.util.List<Map<String, Object>> savedDtls = jdbcTemplate.queryForList("SELECT * FROM scroll_dtl WHERE CAST(invoice_id AS text) = ?", savedId.toString());
        System.out.println(">> SAVED SCROLL_DTL COUNT FOR ID " + savedId + ": " + savedDtls.size());

        // Now delete it
        dynamicDataService.deleteData(scrollForm, hdrData);

        java.util.List<Map<String, Object>> auditHdr = jdbcTemplate.queryForList("SELECT table_name, record_id, action_type FROM sys_audit_log WHERE table_name = 'scroll_hdr' AND record_id = ? AND action_type = 'DELETE'", savedId.toString());
        java.util.List<Map<String, Object>> auditDtl = jdbcTemplate.queryForList("SELECT table_name, record_id, action_type, old_data_json FROM sys_audit_log WHERE table_name = 'scroll_dtl' AND action_type = 'DELETE' ORDER BY id DESC LIMIT 5");

        System.out.println(">> AUDIT HDR AFTER DELETE: " + auditHdr);
        System.out.println(">> AUDIT DTL AFTER DELETE: " + auditDtl);

        // Verify Cascade Restore
        Long hdrLogId = jdbcTemplate.queryForObject("SELECT id FROM sys_audit_log WHERE table_name = 'scroll_hdr' AND record_id = ? AND action_type = 'DELETE' ORDER BY id DESC LIMIT 1", Long.class, savedId.toString());
        System.out.println(">> RESTORING FROM AUDIT LOG ID: " + hdrLogId);
        boolean restored = dynamicDataService.restoreFromAuditLog(hdrLogId);
        System.out.println(">> RESTORE SUCCESS: " + restored);

        java.util.List<Map<String, Object>> restoredHdr = jdbcTemplate.queryForList("SELECT * FROM scroll_hdr WHERE CAST(id AS text) = ?", savedId.toString());
        java.util.List<Map<String, Object>> restoredDtl = jdbcTemplate.queryForList("SELECT * FROM scroll_dtl WHERE CAST(invoice_id AS text) = ?", savedId.toString());
        System.out.println(">> RESTORED HDR COUNT: " + restoredHdr.size());
        System.out.println(">> RESTORED DTL COUNT: " + restoredDtl.size());

        // Test Meta Audit Logging (FormBuilder / System table delete)
        FormMeta testMeta = new FormMeta();
        testMeta.setFormCode("AUDIT_TEST_META");
        testMeta.setFormTitle("Test Audit Meta");
        testMeta.setTableName("test_table");
        formMetaRepository.save(testMeta);
        formMetaRepository.deleteById("AUDIT_TEST_META");
        java.util.List<Map<String, Object>> metaLogs = jdbcTemplate.queryForList("SELECT id, table_name, record_id, action_type FROM sys_audit_log WHERE table_name = 'meta_form' AND record_id = 'AUDIT_TEST_META' AND action_type = 'DELETE'");
        System.out.println(">> META AUDIT LOG AFTER DELETE: " + metaLogs);
        if (!metaLogs.isEmpty()) {
            Long metaLogId = ((Number) metaLogs.get(0).get("id")).longValue();
            boolean metaRestored = dynamicDataService.restoreFromAuditLog(metaLogId);
            System.out.println(">> META RESTORE SUCCESS: " + metaRestored);
            boolean metaExists = formMetaRepository.existsById("AUDIT_TEST_META");
            System.out.println(">> META EXISTS AFTER RESTORE: " + metaExists);
            // clean up test meta
            formMetaRepository.deleteById("AUDIT_TEST_META");
        }
    }
}
