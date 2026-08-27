package com.vaadinerp.meta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Memverifikasi mapping JPA (ReportMeta.params → ReportParamMeta.reportMeta, +
 * kolom baru) lolos ddl-auto=validate terhadap DB nyata. Butuh PostgreSQL aktif.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReportMappingValidationTest {

    @Autowired
    private ReportParamMetaRepository reportParamMetaRepository;

    @Test
    void jpaContextLoadsAndSchemaValidates() {
        // Jika mapping/skema tidak valid, konteks JPA gagal load sebelum test ini jalan.
        assertThat(reportParamMetaRepository.findAll()).isNotNull();
    }
}
