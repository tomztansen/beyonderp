package com.vaadinerp.meta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportParamMetaRepository extends JpaRepository<ReportParamMeta, Long> {
}
