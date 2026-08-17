import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import com.vaadinerp.Application;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;

public class TestJpa {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        FormMetaRepository repo = ctx.getBean(FormMetaRepository.class);
        
        FormMeta fm = new FormMeta();
        fm.setFormCode("TEST_AUDIT_" + System.currentTimeMillis());
        fm.setFormTitle("Test Title");
        fm.setTableName("test_table");
        
        FormMeta saved = repo.save(fm);
        System.out.println("Saved ID: " + saved.getFormCode());
        System.out.println("inputby: " + saved.getInputby());
        System.out.println("inputdt: " + saved.getInputdt());
        System.out.println("version: " + saved.getVersion());
        
        System.exit(0);
    }
}
