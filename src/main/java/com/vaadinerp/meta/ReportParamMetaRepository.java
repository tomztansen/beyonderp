package com.vaadinerp.meta;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportParamMetaRepository extends JpaRepository<ReportParamMeta, Long> {
    List<ReportParamMeta> findByReportCodeOrderByColOrderAsc(String reportCode);
}
