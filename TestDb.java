import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.List;

public class TestDb {
    public static void main(String[] args) {
        try {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUrl("jdbc:postgresql://localhost:5432/grp?currentSchema=public,dynamic");
            ds.setUsername("postgres");
            ds.setPassword("postgres");

            JdbcTemplate jdbc = new JdbcTemplate(ds);
            Object[] arguments = new Object[0];
            Object result = jdbc.queryForObject("select 1", Object.class, arguments);
            System.out.println("Result: " + result + " of type " + result.getClass());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
