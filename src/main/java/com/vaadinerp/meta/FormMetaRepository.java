package com.vaadinerp.meta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FormMetaRepository extends JpaRepository<FormMeta, String> {
    Optional<FormMeta> findByTableName(String tableName);
    Optional<FormMeta> findFirstByTableName(String tableName);

    /**
     * Forms matching the source key stored in {@code meta_report.table_name}.
     * That key is the base table for most forms, but the view name for forms that
     * only have a view — so both columns have to be checked. Returns a list because
     * nothing stops two forms from sharing a table.
     */
    @Query("select f from FormMeta f where lower(f.tableName) = lower(:key)"
            + " or lower(f.viewTable) = lower(:key) or lower(f.formCode) = lower(:key)"
            // Several forms may share one table (7 do here), and they can carry different
            // views — so the order decides which SQL runs. Fix it: an exact form_code is
            // unambiguous, then table, then view; form_code last as a stable tie-break.
            + " order by case when lower(f.formCode) = lower(:key) then 0"
            + " when lower(f.tableName) = lower(:key) then 1 else 2 end, f.formCode")
    List<FormMeta> findByReportSourceKey(@Param("key") String key);
}
