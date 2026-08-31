package com.vaadinerp.report;

import com.vaadinerp.components.ComponentFactory;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.service.DynamicDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
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
    private final int previewRows;

    public ReportDataService(DataSource dataSource,
                             @Value("${app.report.query-timeout-seconds:30}") int queryTimeoutSeconds,
                             @Value("${app.report.preview-rows:500}") int previewRows,
                             FormMetaRepository formMetaRepository,
                             DynamicDataService dynamicDataService) {
        this.previewRows = previewRows;
        // JdbcTemplate khusus report dengan query timeout — TIDAK memengaruhi JdbcTemplate app global.
        // Query runaway dibunuh di ~timeout → koneksi HikariCP cepat kembali → lindungi user lain.
        JdbcTemplate reportJt = new JdbcTemplate(dataSource);
        reportJt.setQueryTimeout(queryTimeoutSeconds);
        this.npjt = new NamedParameterJdbcTemplate(reportJt);
        this.formMetaRepository = formMetaRepository;
        this.dynamicDataService = dynamicDataService;
    }

    /**
     * Cari FormMeta dari source key report. Key itu bisa table_name, view_table, atau
     * form_code (dipakai untuk form yang tak punya tabel dasar) — satu-satunya definisi
     * pencocokan ada di repository, supaya jalur run dan Stimulsoft tidak berbeda.
     */
    private FormMeta findFormBySourceKey(String key) {
        if (key == null || key.trim().isEmpty()) return null;
        return formMetaRepository.findByReportSourceKey(key.trim()).stream().findFirst().orElse(null);
    }

    /**
     * Bangun WHERE dari parameter Model B (filterColumn + operator + nilai). Param tanpa
     * filterColumn/operator (=Model A) atau tanpa nilai dilewati. Nilai di-bind ke outBind
     * (LIKE/ILIKE dibungkus %..%). Operator & kolom divalidasi.
     *
     * <p>Parameter {@code FORM_FIELD} selalu berisi List (lihat
     * {@link ReportParamResolver#resolveFromRows}), sehingga operator tersimpan diabaikan dan
     * klausanya selalu {@code IN (:param)} — satu-satunya bentuk yang di-expand
     * NamedParameterJdbcTemplate untuk Collection. {@code = ANY(:param)} akan gagal dengan
     * "Can't infer the SQL type ... java.util.ArrayList".
     */
    public static String buildModelBWhere(List<com.vaadinerp.meta.ReportParamMeta> params,
                                          Map<String, Object> values, Map<String, Object> outBind) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (com.vaadinerp.meta.ReportParamMeta p : params) {
            String col = p.getFilterColumn();
            String opRaw = p.getOperator();
            boolean formField = "FORM_FIELD".equalsIgnoreCase(
                    p.getSource() == null ? "" : p.getSource().trim());
            if (col == null || col.isBlank()) continue;
            if (!formField && (opRaw == null || opRaw.isBlank())) continue;

            String op = formField ? "IN" : opRaw.trim().toUpperCase();
            boolean noValue = "IS NULL".equals(op) || "IS NOT NULL".equals(op);

            Object val = values != null ? values.get(p.getParamName()) : null;
            if (!noValue && (val == null
                    || (val instanceof String s && s.isBlank())
                    || (val instanceof java.util.Collection<?> c && c.isEmpty()))) continue;

            DynamicDataService.validateSqlIdentifier(col, "filter column");
            String name = p.getParamName();
            sb.append(sb.length() == 0 ? " WHERE " : " AND ");

            if (noValue) {
                // IS NULL / IS NOT NULL — tidak butuh bind value
                sb.append(col).append(" ").append(op);
            } else if ("IN".equals(op)) {
                // FORM_FIELD selalu Collection; USER_INPUT bisa comma-separated string
                Object inVal = (val instanceof String sv)
                        ? java.util.Arrays.stream(sv.split(",")).map(String::trim)
                                .filter(s -> !s.isEmpty()).toList()
                        : val;
                sb.append(col).append(" IN (:").append(name).append(")");
                outBind.put(name, inVal);
            } else if ("NOT IN".equals(op)) {
                Object inVal = (val instanceof String sv)
                        ? java.util.Arrays.stream(sv.split(",")).map(String::trim)
                                .filter(s -> !s.isEmpty()).toList()
                        : val;
                sb.append(col).append(" NOT IN (:").append(name).append(")");
                outBind.put(name, inVal);
            } else {
                String validOp = DynamicDataService.validateComparisonOperator(op);
                sb.append(col).append(" ").append(validOp).append(" :").append(name);
                if (validOp.contains("LIKE")) {
                    String s = val.toString();
                    outBind.put(name, s.contains("%") ? s : "%" + s + "%");
                } else {
                    outBind.put(name, val);
                }
            }
        }
        return sb.toString();
    }

    /** Urutan datasource: dataQuery → form.viewTable → SELECT * FROM {qualified tableName}. Pure. */
    public static String resolveBaseQuery(ReportMeta report, FormMeta form, DynamicDataService dyn) {
        if (report.getDataQuery() != null && !report.getDataQuery().trim().isEmpty()) {
            return report.getDataQuery().trim();
        }
        if (form != null && form.getViewTable() != null && !form.getViewTable().trim().isEmpty()) {
            String vt = form.getViewTable().trim();
            String lower = vt.toLowerCase();
            // view_table may be a whole SELECT/WITH, or just a view name — a bare name
            // is not valid SQL on its own and must be wrapped.
            return (lower.startsWith("select") || lower.startsWith("with"))
                    ? vt
                    : "SELECT * FROM " + dyn.getQualifiedTableName(vt);
        }
        if (report.getTableName() != null && !report.getTableName().trim().isEmpty()) {
            return "SELECT * FROM " + dyn.getQualifiedTableName(report.getTableName().trim());
        }
        return null;
    }

    public List<Map<String, Object>> fetchData(ReportMeta report, Map<String, Object> params, boolean sample) {
        FormMeta form = findFormBySourceKey(report.getTableName());

        String sql = resolveBaseQuery(report, form, dynamicDataService);
        if (sql == null) return null; // Return null so JasperRenderer knows to use JDBC connection

        // resolve keyword ($CURRENT_USER dll) & validasi read-only
        sql = DynamicDataService.validateAndSanitizeSelectQuery(
                dynamicDataService.resolveSqlKeywords(sql));

        // Tolak query kalau param wajib (Model B) kosong — cegah return semua data.
        if (report.getParams() != null) {
            for (com.vaadinerp.meta.ReportParamMeta p : report.getParams()) {
                if (!p.isRequired() || p.getFilterColumn() == null || p.getFilterColumn().isBlank()) continue;
                Object v = params != null ? params.get(p.getParamName()) : null;
                boolean empty = v == null
                        || (v instanceof String s && s.isBlank())
                        || (v instanceof java.util.Collection<?> c && c.isEmpty());
                if (empty) throw new IllegalArgumentException(
                        "Parameter '" + (p.getParamLabel() != null ? p.getParamLabel() : p.getParamName()) + "' is required.");
            }
        }

        // Bind Model A (:param di query) + Model B (WHERE dari filterColumn/operator).
        Map<String, Object> bind = new HashMap<>();
        if (params != null) params.forEach(bind::put);
        String whereB = buildModelBWhere(report.getParams(), params, bind);
        if (!whereB.isEmpty()) {
            sql = "SELECT * FROM ( " + sql + " ) AS _rpt" + whereB;
        }
        // Preview: batasi baris agar tidak menarik dataset penuh ke heap (previewRows dari config, bukan input user).
        if (sample && previewRows > 0) {
            sql = "SELECT * FROM ( " + sql + " ) AS _rpt_sample LIMIT " + previewRows;
        }
        // NamedParameterJdbcTemplate menangani cast PostgreSQL '::type' dengan benar (bukan parameter).
        MapSqlParameterSource src = new MapSqlParameterSource();
        bind.forEach(src::addValue);

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
