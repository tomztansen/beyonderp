package com.vaadinerp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;

@SpringBootTest
public class TestJpa {
    @Autowired
    private FormMetaRepository repo;

    @Test
    public void testAudit() {
        FormMeta fm = new FormMeta();
        fm.setFormCode("TEST_AUDIT_" + System.currentTimeMillis());
        fm.setFormTitle("Test Title");
        fm.setTableName("test_table");
        
        FormMeta saved = repo.save(fm);
        System.out.println("====== SAVED ID: " + saved.getFormCode() + " ======");
        System.out.println("====== INPUTBY: " + saved.getInputby() + " ======");
        System.out.println("====== INPUTDT: " + saved.getInputdt() + " ======");
        System.out.println("====== VERSION: " + saved.getVersion() + " ======");
    }
}
