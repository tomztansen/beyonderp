package com.vaadinerp.meta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportMetaRepository extends JpaRepository<ReportMeta, String> {

    @Modifying
    @jakarta.transaction.Transactional
    @Query(value = "DELETE FROM meta_report_column WHERE report_code = :reportCode", nativeQuery = true)
    void deleteLegacyColumns(@Param("reportCode") String reportCode);
}
