package com.vaadinerp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;

@SpringBootTest
public class TestJpaUpdate {
    @Autowired
    private FormMetaRepository repo;

    @Test
    public void testAuditUpdate() {
        FormMeta fm = repo.findById("MASTER_CUSTOMER").orElse(null);
        if (fm != null) {
            System.out.println("====== FOUND MASTER_CUSTOMER ======");
            fm.setFormTitle("Test Update Title " + System.currentTimeMillis());
            FormMeta saved = repo.save(fm);
            System.out.println("====== UPDATEBY: " + saved.getUpdateby() + " ======");
            System.out.println("====== UPDATEDT: " + saved.getUpdatedt() + " ======");
            System.out.println("====== VERSION: " + saved.getVersion() + " ======");
        }
    }
}
