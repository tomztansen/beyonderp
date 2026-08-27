package com.vaadinerp.report;

import com.vaadinerp.components.ComponentFactory;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.service.DynamicDataService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pipeline data report: resolusi datasource (dataQuery → form.viewTable → tableName),
 * pengikatan parameter aman via NamedParameterJdbcTemplate, dan pengayaan label LOV.
 */
@Service
public class ReportDataService {

    private final NamedParameterJdbcTemplate npjt;
    private final FormMetaRepository formMetaRepository;
    private final DynamicDataService dynamicDataService;

    public ReportDataService(NamedParameterJdbcTemplate npjt,
                             FormMetaRepository formMetaRepository,
                             DynamicDataService dynamicDataService) {
        this.npjt = npjt;
        this.formMetaRepository = formMetaRepository;
        this.dynamicDataService = dynamicDataService;
    }

    /** Urutan datasource: dataQuery → form.viewTable → SELECT * FROM {qualified tableName}. Pure. */
    public static String resolveBaseQuery(ReportMeta report, FormMeta form, DynamicDataService dyn) {
        if (report.getDataQuery() != null && !report.getDataQuery().trim().isEmpty()) {
            return report.getDataQuery().trim();
        }
        if (form != null && form.getViewTable() != null && !form.getViewTable().trim().isEmpty()) {
            return form.getViewTable().trim();
        }
        if (report.getTableName() != null && !report.getTableName().trim().isEmpty()) {
            return "SELECT * FROM " + dyn.getQualifiedTableName(report.getTableName().trim());
        }
        return null;
    }

    public List<Map<String, Object>> fetchData(ReportMeta report, Map<String, Object> params) {
        FormMeta form = (report.getTableName() != null)
                ? formMetaRepository.findByTableName(report.getTableName()).orElse(null)
                : null;

        String sql = resolveBaseQuery(report, form, dynamicDataService);
        if (sql == null) return new ArrayList<>();

        // resolve keyword ($CURRENT_USER dll) & validasi read-only
        sql = DynamicDataService.validateAndSanitizeSelectQuery(
                dynamicDataService.resolveSqlKeywords(sql));

        // NamedParameterJdbcTemplate menangani cast PostgreSQL '::type' dengan benar (bukan parameter).
        MapSqlParameterSource src = new MapSqlParameterSource();
        if (params != null) params.forEach(src::addValue);

        List<Map<String, Object>> rows = npjt.queryForList(sql, src);
        return enrichLov(form, rows);
    }

    /** Tambah kolom {field}_label untuk field ber-LOV pada form terkait. */
    private List<Map<String, Object>> enrichLov(FormMeta form, List<Map<String, Object>> rows) {
        if (form == null || form.getFields() == null || rows.isEmpty()) return rows;
        List<FieldMeta> lovFields = new ArrayList<>();
        for (FieldMeta f : form.getFields()) {
            if (f.getLovCode() != null && !f.getLovCode().trim().isEmpty()) lovFields.add(f);
        }
        if (lovFields.isEmpty()) return rows;

        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> nr = new HashMap<>(row);
            for (FieldMeta f : lovFields) {
                String col = f.getFieldName().toLowerCase();
                if (nr.containsKey(col) && nr.get(col) != null) {
                    nr.put(col + "_label",
                            ComponentFactory.formatFieldValueWithLov(f, nr.get(col), dynamicDataService));
                }
            }
            out.add(nr);
        }
        return out;
    }
}
