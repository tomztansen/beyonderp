package com.vaadinerp;

import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import com.vaadinerp.meta.FormMetaRepository;
import com.vaadinerp.meta.LovMeta;
import com.vaadinerp.meta.LovMetaRepository;
import com.vaadinerp.meta.FieldLovTargetMeta;
import com.vaadinerp.meta.FieldFilterMeta;
import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportElementMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FormMetaRepository formMetaRepository;
    private final LovMetaRepository lovMetaRepository;
    private final ReportMetaRepository reportMetaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final com.vaadinerp.service.DynamicDataService dynamicDataService;

    public DataInitializer(FormMetaRepository formMetaRepository, LovMetaRepository lovMetaRepository,
            ReportMetaRepository reportMetaRepository, JdbcTemplate jdbcTemplate,
            com.vaadinerp.service.DynamicDataService dynamicDataService) {
        this.formMetaRepository = formMetaRepository;
        this.lovMetaRepository = lovMetaRepository;
        this.reportMetaRepository = reportMetaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dynamicDataService = dynamicDataService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create table lov_parent if not exists
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS lov_parent (" +
                "code VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "location VARCHAR(255), " +
                "manager VARCHAR(100)" +
                ")");

        // Insert mock data into lov_parent
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lov_parent", Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.execute("INSERT INTO lov_parent (code, name, location, manager) VALUES " +
                    "('HR', 'Human Resources', 'Gedung A Lantai 2', 'JENNIE LEOWARDY'), " +
                    "('IT', 'Information Technology', 'Gedung B Lantai 4', 'JOHN DOE'), " +
                    "('FIN', 'Finance', 'Gedung C Lantai 1', 'JANE SMITH'), " +
                    "('MKT', 'Marketing', 'Gedung D Lantai 3', 'ALICE WONDER'), " +
                    "('OPS', 'Operations', 'Gedung E Lantai 5', 'BOB MARLEY')");
        }

        // Create table lov_child if not exists
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS lov_child (" +
                "code VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "parent_code VARCHAR(50)" +
                ")");

        // Insert mock data into lov_child
        Integer sectionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lov_child", Integer.class);
        if (sectionCount == null || sectionCount == 0) {
            jdbcTemplate.execute("INSERT INTO lov_child (code, name, parent_code) VALUES " +
                    "('IT-DEV', 'Software Development', 'IT'), " +
                    "('IT-OPS', 'IT Operations & Support', 'IT'), " +
                    "('HR-REC', 'Recruitment & Talent', 'HR'), " +
                    "('HR-BEN', 'Compensation & Benefits', 'HR'), " +
                    "('FIN-ACC', 'Accounting', 'FIN'), " +
                    "('FIN-TAX', 'Taxation', 'FIN'), " +
                    "('MKT-DIG', 'Digital Marketing', 'MKT')");
        }

        // Seed LovMeta metadata (Clean up existing DEPT_LOV/SECTION_LOV if they exist
        // to keep db clean)
        if (lovMetaRepository.existsById("DEPT_LOV")) {
            lovMetaRepository.deleteById("DEPT_LOV");
        }
        if (lovMetaRepository.existsById("SECTION_LOV")) {
            lovMetaRepository.deleteById("SECTION_LOV");
        }

        if (!lovMetaRepository.existsById("lov_parent")) {
            LovMeta lovMeta = new LovMeta();
            lovMeta.setLovCode("lov_parent");
            lovMeta.setLovName("Daftar Induk");
            lovMeta.setTableName("lov_parent");
            lovMeta.setValueColumn("code");
            lovMeta.setLabelColumn("name");
            lovMeta.setSearchColumn("name");
            lovMeta.setGridColumns("code:Kode:130px,name:Nama:180px,location:Lokasi:220px,manager:Manajer:180px");
            lovMetaRepository.save(lovMeta);
        }

        if (!lovMetaRepository.existsById("lov_child")) {
            LovMeta lovMeta = new LovMeta();
            lovMeta.setLovCode("lov_child");
            lovMeta.setLovName("Daftar Cabang/Seksi");
            lovMeta.setTableName("lov_child");
            lovMeta.setValueColumn("code");
            lovMeta.setLabelColumn("name");
            lovMeta.setSearchColumn("name");
            lovMeta.setGridColumns("code:Kode:130px,name:Nama:180px,parent_code:Kode Induk:130px");
            lovMetaRepository.save(lovMeta);
        }

        // Force recreate of TEST_FORM_2 to include new cascading fields and filters
        if (formMetaRepository.existsById("TEST_FORM_2")) {
            formMetaRepository.deleteById("TEST_FORM_2");
            System.out.println("Existing TEST_FORM_2 metadata deleted.");
        }

        System.out.println("Inserting fresh TEST_FORM_2 metadata...");

        FormMeta form = new FormMeta();
        form.setFormCode("TEST_FORM_2");
        form.setFormTitle("Formulir Pegawai (TEST)");
        form.setTableName("employees");
        form.setPrimaryKey("id");
        form.setLabelWidth("150px");
        form.setFields(new ArrayList<>());

        // 1. empName
        FieldMeta f1 = new FieldMeta();
        f1.setFormMeta(form);
        f1.setFieldName("empName");
        f1.setFieldLabel("Nama Pegawai");
        f1.setComponentType("TEXTBOX");
        f1.setRowGroup(1);
        f1.setColOrder(1);
        f1.setRequired(true);
        f1.setShowInGrid(true);

        List<FieldLovTargetMeta> tEmp = new ArrayList<>();
        FieldLovTargetMeta t3 = new FieldLovTargetMeta();
        t3.setFieldMeta(f1);
        t3.setSourceColumn("value");
        t3.setTargetField("address");
        tEmp.add(t3);

        FieldLovTargetMeta t6 = new FieldLovTargetMeta();
        t6.setFieldMeta(f1);
        t6.setSourceColumn("value");
        t6.setTargetField("department");
        t6.setActionType("QUERY_LOV");
        t6.setLookupColumn("manager");
        tEmp.add(t6);

        f1.setLovTargets(tEmp);
        form.getFields().add(f1);

        // 2. birthDate
        FieldMeta f2 = new FieldMeta();
        f2.setFormMeta(form);
        f2.setFieldName("birthDate");
        f2.setFieldLabel("Tanggal Lahir");
        f2.setComponentType("DATEBOX");
        f2.setRowGroup(1);
        f2.setColOrder(2);
        f2.setShowInGrid(true);
        form.getFields().add(f2);

        // 3. salary
        FieldMeta f3 = new FieldMeta();
        f3.setFormMeta(form);
        f3.setFieldName("salary");
        f3.setFieldLabel("Gaji");
        f3.setComponentType("DECIMALBOX");
        f3.setRowGroup(2);
        f3.setColOrder(1);
        f3.setRequired(true);
        f3.setShowInGrid(true);
        form.getFields().add(f3);

        // 4. department
        FieldMeta f4 = new FieldMeta();
        f4.setFormMeta(form);
        f4.setFieldName("department");
        f4.setFieldLabel("Departemen");
        f4.setComponentType("BANDBOX");
        f4.setLovCode("lov_parent");
        f4.setRowGroup(2);
        f4.setColOrder(2);
        f4.setShowInGrid(true);

        List<FieldLovTargetMeta> targets = new ArrayList<>();
        FieldLovTargetMeta t1 = new FieldLovTargetMeta();
        t1.setFieldMeta(f4);
        t1.setSourceColumn("location");
        t1.setTargetField("address");
        targets.add(t1);

        FieldLovTargetMeta t2 = new FieldLovTargetMeta();
        t2.setFieldMeta(f4);
        t2.setSourceColumn("manager");
        t2.setTargetField("empName");
        targets.add(t2);

        FieldLovTargetMeta t5 = new FieldLovTargetMeta();
        t5.setFieldMeta(f4);
        t5.setSourceColumn("code");
        t5.setTargetField("status");
        targets.add(t5);

        f4.setLovTargets(targets);
        form.getFields().add(f4);

        // 5. address
        FieldMeta f5 = new FieldMeta();
        f5.setFormMeta(form);
        f5.setFieldName("address");
        f5.setFieldLabel("Alamat Lengkap");
        f5.setComponentType("TEXTAREA");
        f5.setRowGroup(3);
        f5.setColOrder(1);
        f5.setShowInGrid(false);
        form.getFields().add(f5);

        // 6. status (combobox)
        FieldMeta f6 = new FieldMeta();
        f6.setFormMeta(form);
        f6.setFieldName("status");
        f6.setFieldLabel("Status Pegawai");
        f6.setComponentType("COMBOBOX");
        f6.setRowGroup(4);
        f6.setColOrder(1);
        f6.setShowInGrid(true);

        List<FieldLovTargetMeta> tStatus = new ArrayList<>();
        FieldLovTargetMeta t4 = new FieldLovTargetMeta();
        t4.setFieldMeta(f6);
        t4.setSourceColumn("value");
        t4.setTargetField("department");
        tStatus.add(t4);
        f6.setLovTargets(tStatus);
        form.getFields().add(f6);

        // 7. shift
        FieldMeta f7 = new FieldMeta();
        f7.setFormMeta(form);
        f7.setFieldName("shift");
        f7.setFieldLabel("Shift Kerja");
        f7.setComponentType("LISTBOX");
        f7.setRowGroup(4);
        f7.setColOrder(2);
        f7.setShowInGrid(true);
        form.getFields().add(f7);

        // 8. skills
        FieldMeta f8 = new FieldMeta();
        f8.setFormMeta(form);
        f8.setFieldName("skills");
        f8.setFieldLabel("Keahlian");
        f8.setComponentType("CHOSENBOX");
        f8.setRowGroup(5);
        f8.setColOrder(1);
        f8.setShowInGrid(false);
        form.getFields().add(f8);

        // 9. section (NEW CASCADING FIELD)
        FieldMeta f9 = new FieldMeta();
        f9.setFormMeta(form);
        f9.setFieldName("section");
        f9.setFieldLabel("Seksi Departemen (Cascading ComboBox)");
        f9.setComponentType("COMBOBOX");
        f9.setLovCode("lov_child");
        f9.setRowGroup(4);
        f9.setColOrder(3);
        f9.setShowInGrid(true);

        // Filters for section field
        List<FieldFilterMeta> f9Filters = new ArrayList<>();

        // Filter 1: parent_code column in lov_child matches department field in form
        FieldFilterMeta filt1 = new FieldFilterMeta();
        filt1.setFieldMeta(f9);
        filt1.setFilterColumn("parent_code");
        filt1.setSourceType("FIELD");
        filt1.setSourceName("department");
        f9Filters.add(filt1);

        // Filter 2: parent_code column in lov_child can also be filtered by query param
        // "parent"
        FieldFilterMeta filt2 = new FieldFilterMeta();
        filt2.setFieldMeta(f9);
        filt2.setFilterColumn("parent_code");
        filt2.setSourceType("QUERY");
        filt2.setSourceName("parent");
        f9Filters.add(filt2);

        f9.setFilters(f9Filters);
        form.getFields().add(f9);

        formMetaRepository.save(form);
        dynamicDataService.generatePhysicalTable(form);

        // Create table invoice_items if not exists under schema dynamic
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS dynamic;");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dynamic.invoice_items (" +
                "id SERIAL PRIMARY KEY, " +
                "item_name VARCHAR(255), " +
                "qty INTEGER, " +
                "price DECIMAL(19, 2), " +
                "total_price DECIMAL(19, 2)" +
                ")");

        // Insert mock data into invoice_items
        Integer itemCheck = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dynamic.invoice_items", Integer.class);
        if (itemCheck == null || itemCheck == 0) {
            jdbcTemplate.execute("INSERT INTO dynamic.invoice_items (item_name, qty, price, total_price) VALUES " +
                    "('MacBook Pro M3 Max 16-inch', 1, 48000000.00, 48000000.00), " +
                    "('Logitech MX Master 3S Mouse', 2, 1650000.00, 3300000.00), " +
                    "('Dell UltraSharp 32 4K Monitor U3223QE', 1, 14200000.00, 14200000.00), " +
                    "('Keychron Q1 Pro Mechanical Keyboard', 1, 2950000.00, 2950000.00)");
        }

        if (!formMetaRepository.existsById("TEST_INVOICE_FORM")) {
            FormMeta invForm = new FormMeta();
            invForm.setFormCode("TEST_INVOICE_FORM");
            invForm.setFormTitle("Invoice Penjualan (TEST)");
            invForm.setTableName("invoice_items");
            invForm.setPrimaryKey("id");
            invForm.setLabelWidth("150px");
            invForm.setFields(new ArrayList<>());

            FieldMeta fi1 = new FieldMeta();
            fi1.setFormMeta(invForm);
            fi1.setFieldName("item_name");
            fi1.setFieldLabel("Nama Barang");
            fi1.setComponentType("TEXTBOX");
            fi1.setRowGroup(1);
            fi1.setColOrder(1);
            fi1.setRequired(true);
            fi1.setShowInGrid(true);
            invForm.getFields().add(fi1);

            FieldMeta fi2 = new FieldMeta();
            fi2.setFormMeta(invForm);
            fi2.setFieldName("price");
            fi2.setFieldLabel("Harga Satuan");
            fi2.setComponentType("DECIMALBOX");
            fi2.setRowGroup(2);
            fi2.setColOrder(1);
            fi2.setRequired(true);
            fi2.setShowInGrid(true);
            invForm.getFields().add(fi2);

            FieldMeta fi3 = new FieldMeta();
            fi3.setFormMeta(invForm);
            fi3.setFieldName("qty");
            fi3.setFieldLabel("Kuantitas");
            fi3.setComponentType("INTBOX");
            fi3.setRowGroup(2);
            fi3.setColOrder(2);
            fi3.setRequired(true);
            fi3.setShowInGrid(true);
            invForm.getFields().add(fi3);

            FieldMeta fi4 = new FieldMeta();
            fi4.setFormMeta(invForm);
            fi4.setFieldName("total_price");
            fi4.setFieldLabel("Subtotal");
            fi4.setComponentType("DECIMALBOX");
            fi4.setRowGroup(2);
            fi4.setColOrder(3);
            fi4.setRequired(true);
            fi4.setShowInGrid(true);
            fi4.setFormula("qty * price");
            fi4.setSaveOnInsert(false);
            fi4.setSaveOnUpdate(false);
            invForm.getFields().add(fi4);

            formMetaRepository.save(invForm);
        }

        // Create pre-defined Invoice Master-Detail Form
        if (formMetaRepository.existsById("INVOICE_MD")) {
            formMetaRepository.deleteById("INVOICE_MD");
        }

        FormMeta mdForm = new FormMeta();
        mdForm.setFormCode("INVOICE_MD");
        mdForm.setFormTitle("Faktur Penjualan (Master-Detail)");
        mdForm.setFormType("MASTER_DETAIL");
        mdForm.setTableName("inv_hdr");
        mdForm.setPrimaryKey("id");
        mdForm.setLabelWidth("150px");
        mdForm.setDetailTableName("inv_dtl");
        mdForm.setDetailPrimaryKey("id");
        mdForm.setDetailForeignKey("invoice_id");
        mdForm.setFields(new ArrayList<>());

        // Master field 1
        FieldMeta mf1 = new FieldMeta();
        mf1.setFormMeta(mdForm);
        mf1.setFieldName("invoice_no");
        mf1.setFieldLabel("Nomor Faktur");
        mf1.setComponentType("TEXTBOX");
        mf1.setRowGroup(1);
        mf1.setColOrder(10);
        mf1.setRequired(true);
        mf1.setShowInGrid(true);
        mf1.setDetail(false);
        mdForm.getFields().add(mf1);

        // Master field 2
        FieldMeta mf2 = new FieldMeta();
        mf2.setFormMeta(mdForm);
        mf2.setFieldName("invoice_date");
        mf2.setFieldLabel("Tanggal Faktur");
        mf2.setComponentType("DATEBOX");
        mf2.setRowGroup(1);
        mf2.setColOrder(20);
        mf2.setRequired(true);
        mf2.setShowInGrid(true);
        mf2.setDetail(false);
        mdForm.getFields().add(mf2);

        // Master field 3
        FieldMeta mf3 = new FieldMeta();
        mf3.setFormMeta(mdForm);
        mf3.setFieldName("customer");
        mf3.setFieldLabel("Nama Pelanggan");
        mf3.setComponentType("COMBOBOX");
        mf3.setLovCode("lov_parent");
        mf3.setRowGroup(2);
        mf3.setColOrder(30);
        mf3.setRequired(false);
        mf3.setShowInGrid(true);
        mf3.setDetail(false);
        mdForm.getFields().add(mf3);

        // Detail field 1
        FieldMeta df1 = new FieldMeta();
        df1.setFormMeta(mdForm);
        df1.setFieldName("item_code");
        df1.setFieldLabel("Kode Barang");
        df1.setComponentType("BANDBOX");
        df1.setLovCode("lov_child");
        df1.setColOrder(10);
        df1.setRequired(true);
        df1.setDetail(true);

        List<FieldFilterMeta> df1Filters = new ArrayList<>();
        FieldFilterMeta filt = new FieldFilterMeta();
        filt.setFieldMeta(df1);
        filt.setFilterColumn("parent_code");
        filt.setSourceType("FIELD");
        filt.setSourceName("customer");
        df1Filters.add(filt);
        df1.setFilters(df1Filters);

        mdForm.getFields().add(df1);

        // Detail field 2
        FieldMeta df2 = new FieldMeta();
        df2.setFormMeta(mdForm);
        df2.setFieldName("qty");
        df2.setFieldLabel("Kuantitas");
        df2.setComponentType("INTBOX");
        df2.setColOrder(20);
        df2.setRequired(true);
        df2.setDetail(true);
        mdForm.getFields().add(df2);

        // Detail field 3
        FieldMeta df3 = new FieldMeta();
        df3.setFormMeta(mdForm);
        df3.setFieldName("price");
        df3.setFieldLabel("Harga Satuan");
        df3.setComponentType("DECIMALBOX");
        df3.setColOrder(30);
        df3.setRequired(true);
        df3.setDetail(true);
        mdForm.getFields().add(df3);

        // Detail field 4
        FieldMeta df4 = new FieldMeta();
        df4.setFormMeta(mdForm);
        df4.setFieldName("total_price");
        df4.setFieldLabel("Subtotal");
        df4.setComponentType("DECIMALBOX");
        df4.setColOrder(40);
        df4.setReadonly(true);
        df4.setDetail(true);
        df4.setFormula("qty * price");
        df4.setSaveOnInsert(true);
        df4.setSaveOnUpdate(true);
        mdForm.getFields().add(df4);

        formMetaRepository.save(mdForm);

        // Create pre-defined Invoice Report
        if (reportMetaRepository.existsById("INVOICE_REPORT")) {
            reportMetaRepository.deleteById("INVOICE_REPORT");
        }

        ReportMeta invoiceReport = new ReportMeta();
        invoiceReport.setReportCode("INVOICE_REPORT");
        invoiceReport.setReportTitle("INVOICE PENJUALAN");
        invoiceReport.setTableName("invoice_items");
        invoiceReport.setPageSize("A4");
        invoiceReport.setOrientation("PORTRAIT");
        invoiceReport.setElements(new ArrayList<>());

        int o = 1;
        // Title Band
        invoiceReport.getElements().add(createElement(invoiceReport, "TITLE", "LABEL", "GLOBAL MEDIA NUSANTARA", "100%",
                "CENTER", "BOLD", null, o++));
        invoiceReport.getElements().add(createElement(invoiceReport, "TITLE", "LABEL",
                "Gedung Cyber, Lantai 15, Jl. Rasuna Said, Jakarta Selatan", "100%", "CENTER", "NORMAL", null, o++));
        invoiceReport.getElements().add(createElement(invoiceReport, "TITLE", "LABEL", "INVOICE PENJUALAN", "100%",
                "CENTER", "BOLD", null, o++));

        // Page Header Band
        invoiceReport.getElements().add(createElement(invoiceReport, "PAGE_HEADER", "LABEL",
                "No. Invoice: INV-2026-0001", "50%", "LEFT", "BOLD", null, o++));
        invoiceReport.getElements().add(createElement(invoiceReport, "PAGE_HEADER", "SYSTEM", "CURRENT_DATE", "50%",
                "RIGHT", "NORMAL", "dd MMMM yyyy", o++));

        // Column Header Band
        invoiceReport.getElements().add(createElement(invoiceReport, "COLUMN_HEADER", "LABEL", "Nama Barang", "40%",
                "LEFT", "BOLD", null, o++));
        invoiceReport.getElements().add(createElement(invoiceReport, "COLUMN_HEADER", "LABEL", "Harga Satuan", "20%",
                "RIGHT", "BOLD", null, o++));
        invoiceReport.getElements()
                .add(createElement(invoiceReport, "COLUMN_HEADER", "LABEL", "Qty", "15%", "CENTER", "BOLD", null, o++));
        invoiceReport.getElements().add(
                createElement(invoiceReport, "COLUMN_HEADER", "LABEL", "Subtotal", "25%", "RIGHT", "BOLD", null, o++));

        // Detail Band
        invoiceReport.getElements()
                .add(createElement(invoiceReport, "DETAIL", "FIELD", "item_name", "40%", "LEFT", "NORMAL", null, o++));
        invoiceReport.getElements().add(
                createElement(invoiceReport, "DETAIL", "FIELD", "price", "20%", "RIGHT", "NORMAL", "Rp #,##0", o++));
        invoiceReport.getElements()
                .add(createElement(invoiceReport, "DETAIL", "FIELD", "qty", "15%", "CENTER", "NORMAL", "#,##0", o++));
        invoiceReport.getElements().add(createElement(invoiceReport, "DETAIL", "FIELD", "total_price", "25%", "RIGHT",
                "NORMAL", "Rp #,##0", o++));

        // Summary Band
        invoiceReport.getElements().add(createElement(invoiceReport, "SUMMARY", "LABEL", "Grand Total Invoice:", "75%",
                "RIGHT", "BOLD", null, o++));
        invoiceReport.getElements().add(createElement(invoiceReport, "SUMMARY", "SYSTEM", "SUM(total_price)", "25%",
                "RIGHT", "BOLD", "Rp #,##0", o++));

        // Page Footer Band
        invoiceReport.getElements().add(createElement(invoiceReport, "PAGE_FOOTER", "LABEL",
                "Terima kasih atas pembelian Anda!", "100%", "CENTER", "NORMAL", null, o++));

        reportMetaRepository.save(invoiceReport);

        System.out.println("Generic dummy data and metadata initialized successfully.");
    }

    private ReportElementMeta createElement(ReportMeta report, String band, String type, String value, String width,
            String align, String weight, String formatPattern, int order) {
        ReportElementMeta el = new ReportElementMeta();
        el.setReportMeta(report);
        el.setBandType(band);
        el.setElementType(type);
        el.setElementValue(value);
        el.setColumnWidth(width);
        el.setAlignment(align);
        el.setFontWeight(weight);
        el.setFormatPattern(formatPattern);
        el.setColOrder(order);
        return el;
    }
}
