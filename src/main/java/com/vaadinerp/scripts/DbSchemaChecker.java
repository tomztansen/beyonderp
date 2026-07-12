package com.vaadinerp.scripts;

import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import java.util.List;

@Component
public class DbSchemaChecker implements CommandLineRunner {
    private final JdbcTemplate jdbc;
    
    public DbSchemaChecker(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== DB SCHEMA CHECK ======");
        List<Map<String, Object>> res = jdbc.queryForList("SELECT column_name, character_maximum_length FROM information_schema.columns WHERE table_name = 'meta_form_action' AND column_name = 'target_mapping'");
        if (!res.isEmpty()) {
            System.out.println("target_mapping max length: " + res.get(0).get("character_maximum_length"));
        } else {
            System.out.println("target_mapping column not found!");
        }
        
        List<Map<String, Object>> actions = jdbc.queryForList("SELECT action_code, LENGTH(target_mapping) as len, target_mapping FROM meta_form_action WHERE target_mapping IS NOT NULL");
        for (Map<String, Object> a : actions) {
            System.out.println("Action: " + a.get("action_code") + " | Length: " + a.get("len") + " | Value: " + a.get("target_mapping"));
        }
        System.out.println("=============================");
    }
}
