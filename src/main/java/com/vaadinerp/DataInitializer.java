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
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "app.seed-data.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DataInitializer implements CommandLineRunner {

    private final FormMetaRepository formMetaRepository;
    private final LovMetaRepository lovMetaRepository;
    private final ReportMetaRepository reportMetaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final com.vaadinerp.service.DynamicDataService dynamicDataService;
    private final com.vaadinerp.meta.FormActionMetaRepository formActionMetaRepository;

    public DataInitializer(FormMetaRepository formMetaRepository, LovMetaRepository lovMetaRepository,
            ReportMetaRepository reportMetaRepository, JdbcTemplate jdbcTemplate,
            com.vaadinerp.service.DynamicDataService dynamicDataService,
            com.vaadinerp.meta.FormActionMetaRepository formActionMetaRepository) {
        this.formMetaRepository = formMetaRepository;
        this.lovMetaRepository = lovMetaRepository;
        this.reportMetaRepository = reportMetaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dynamicDataService = dynamicDataService;
        this.formActionMetaRepository = formActionMetaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initSecurityCoreTables();

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

        try {
            jdbcTemplate.update("UPDATE meta_field SET show_in_grid = true WHERE form_code = 'TEST_FORM_2' AND field_name = 'skills'");
        } catch (Exception ignored) {}

        if (!formMetaRepository.existsById("TEST_FORM_2")) {
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
        f8.setShowInGrid(true);
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
        }

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

        if (!formMetaRepository.existsById("INVOICE_MD")) {

        // 1. Define Detail Form
        FormMeta dtlForm = new FormMeta();
        dtlForm.setFormCode("INVOICE_MD_DTL");
        dtlForm.setFormTitle("Detail Faktur Penjualan");
        dtlForm.setFormType("SINGLE");
        dtlForm.setTableName("inv_dtl");
        dtlForm.setPrimaryKey("id");
        dtlForm.setLabelWidth("150px");
        dtlForm.setFields(new ArrayList<>());

        // Detail field 1: item_code
        FieldMeta df1 = new FieldMeta();
        df1.setFormMeta(dtlForm);
        df1.setFieldName("item_code");
        df1.setFieldLabel("Kode Barang");
        df1.setComponentType("BANDBOX");
        df1.setLovCode("lov_child");
        df1.setColOrder(10);
        df1.setRequired(true);
        df1.setShowInGrid(true);

        List<FieldFilterMeta> df1Filters = new ArrayList<>();
        FieldFilterMeta dtlFilt = new FieldFilterMeta();
        dtlFilt.setFieldMeta(df1);
        dtlFilt.setFilterColumn("parent_code");
        dtlFilt.setSourceType("FIELD");
        dtlFilt.setSourceName("customer");
        df1Filters.add(dtlFilt);
        df1.setFilters(df1Filters);

        dtlForm.getFields().add(df1);

        // Detail field 2: qty
        FieldMeta df2 = new FieldMeta();
        df2.setFormMeta(dtlForm);
        df2.setFieldName("qty");
        df2.setFieldLabel("Kuantitas");
        df2.setComponentType("INTBOX");
        df2.setColOrder(20);
        df2.setRequired(true);
        df2.setShowInGrid(true);
        dtlForm.getFields().add(df2);

        // Detail field 3: price
        FieldMeta df3 = new FieldMeta();
        df3.setFormMeta(dtlForm);
        df3.setFieldName("price");
        df3.setFieldLabel("Harga Satuan");
        df3.setComponentType("DECIMALBOX");
        df3.setColOrder(30);
        df3.setRequired(true);
        df3.setShowInGrid(true);
        dtlForm.getFields().add(df3);

        // Detail field 4: total_price
        FieldMeta df4 = new FieldMeta();
        df4.setFormMeta(dtlForm);
        df4.setFieldName("total_price");
        df4.setFieldLabel("Subtotal");
        df4.setComponentType("DECIMALBOX");
        df4.setColOrder(40);
        df4.setReadonly(true);
        df4.setShowInGrid(true);
        df4.setFormula("qty * price");
        df4.setSaveOnInsert(true);
        df4.setSaveOnUpdate(true);
        dtlForm.getFields().add(df4);

        formMetaRepository.save(dtlForm);
        dynamicDataService.generatePhysicalTable(dtlForm);

        // 2. Define Master/Parent Form
        FormMeta mdForm = new FormMeta();
        mdForm.setFormCode("INVOICE_MD");
        mdForm.setFormTitle("Faktur Penjualan (Master-Detail)");
        mdForm.setFormType("SINGLE"); // Set to SINGLE because subform grid is self-contained in a field
        mdForm.setTableName("inv_hdr");
        mdForm.setPrimaryKey("id");
        mdForm.setLabelWidth("150px");
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
        mdForm.getFields().add(mf3);

        // Master field 4: Subform Grid pointing to INVOICE_MD_DTL
        FieldMeta mf4 = new FieldMeta();
        mf4.setFormMeta(mdForm);
        mf4.setFieldName("invoice_details");
        mf4.setFieldLabel("Daftar Barang Detail");
        mf4.setComponentType("SUBFORM_GRID");
        mf4.setLovCode("INVOICE_MD_DTL"); // Child form code
        mf4.setRowGroup(3);
        mf4.setColOrder(40);
        mf4.setRequired(false);
        mf4.setShowInGrid(false);
        mf4.setFormula("invoice_id"); // Child FK column
        mdForm.getFields().add(mf4);

        formMetaRepository.save(mdForm);
        dynamicDataService.generatePhysicalTable(mdForm);
        }

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

        if (!formMetaRepository.existsById("SCROLL_MD")) {
            System.out.println("Inserting fresh SCROLL_MD metadata...");

            // 1. Define Detail Form (SCROLL_MD_DTL)
            FormMeta dtlForm = new FormMeta();
            dtlForm.setFormCode("SCROLL_MD_DTL");
            dtlForm.setFormTitle("Detail Barang Banyak Kolom");
            dtlForm.setFormType("SINGLE");
            dtlForm.setTableName("scroll_dtl");
            dtlForm.setPrimaryKey("id");
            dtlForm.setLabelWidth("150px");
            dtlForm.setFields(new ArrayList<>());

            // Detail fields (lots of them!)
            addDetailField(dtlForm, "item_code", "Kode Barang", "BANDBOX", "lov_child", 10, true, true);
            addDetailField(dtlForm, "item_name", "Nama Barang", "TEXTBOX", null, 20, false, true);
            addDetailField(dtlForm, "qty", "Kuantitas", "INTBOX", null, 30, true, true);
            addDetailField(dtlForm, "uom", "Satuan (UOM)", "COMBOBOX", null, 40, false, true);
            addDetailField(dtlForm, "price", "Harga Satuan", "DECIMALBOX", null, 50, true, true);
            addDetailField(dtlForm, "discount_pct", "Diskon %", "DECIMALBOX", null, 60, false, true);
            addDetailField(dtlForm, "discount_amt", "Nilai Diskon", "DECIMALBOX", null, 70, false, true);
            addDetailField(dtlForm, "tax_pct", "Pajak %", "DECIMALBOX", null, 80, false, true);
            addDetailField(dtlForm, "tax_amt", "Nilai Pajak", "DECIMALBOX", null, 90, false, true);
            addDetailField(dtlForm, "weight_grid", "Berat (kg)", "DECIMALBOX", null, 100, false, true);
            addDetailField(dtlForm, "batch_no", "Nomor Batch", "TEXTBOX", null, 110, false, true);
            addDetailField(dtlForm, "expiry_date", "Tgl Kadaluarsa", "DATEBOX", null, 120, false, true);
            addDetailField(dtlForm, "total_price", "Subtotal Bersih", "DECIMALBOX", null, 130, false, true);
            addDetailField(dtlForm, "notes_detail", "Catatan Rincian", "TEXTAREA", null, 140, false, true);

            formMetaRepository.save(dtlForm);
            dynamicDataService.generatePhysicalTable(dtlForm);

            // 2. Define Master Form (SCROLL_MD)
            FormMeta mdForm = new FormMeta();
            mdForm.setFormCode("SCROLL_MD");
            mdForm.setFormTitle("Faktur Pengiriman Lengkap (Auto Scroll)");
            mdForm.setFormType("SINGLE");
            mdForm.setTableName("scroll_hdr");
            mdForm.setPrimaryKey("id");
            mdForm.setLabelWidth("160px");
            mdForm.setFields(new ArrayList<>());

            // Master fields (lots of them!)
            addMasterField(mdForm, "invoice_no", "Nomor Faktur", "TEXTBOX", 1, 10, true);
            addMasterField(mdForm, "invoice_date", "Tanggal Faktur", "DATEBOX", 1, 20, true);
            addMasterField(mdForm, "customer", "Pelanggan", "COMBOBOX", "lov_parent", 1, 30, false);
            addMasterField(mdForm, "npwp", "NPWP Pajak", "TEXTBOX", 2, 40, false);
            addMasterField(mdForm, "payment_term", "Termin Pembayaran", "COMBOBOX", null, 2, 50, false);
            addMasterField(mdForm, "salesperson", "Nama Sales", "TEXTBOX", 2, 60, false);
            addMasterField(mdForm, "shipping_method", "Metode Pengiriman", "TEXTBOX", 3, 70, false);
            addMasterField(mdForm, "tracking_no", "Nomor Resi", "TEXTBOX", 3, 80, false);
            addMasterField(mdForm, "warehouse_code", "Kode Gudang", "TEXTBOX", 3, 90, false);
            addMasterField(mdForm, "currency", "Mata Uang", "TEXTBOX", 4, 100, false);
            addMasterField(mdForm, "exchange_rate", "Kurs", "DECIMALBOX", 4, 110, false);
            addMasterField(mdForm, "notes", "Keterangan", "TEXTAREA", 5, 120, false);

            // Subform Grid pointing to SCROLL_MD_DTL
            FieldMeta subformField = new FieldMeta();
            subformField.setFormMeta(mdForm);
            subformField.setFieldName("details");
            subformField.setFieldLabel("Daftar Item Barang (Rincian)");
            subformField.setComponentType("SUBFORM_GRID");
            subformField.setLovCode("SCROLL_MD_DTL"); // Child form code
            subformField.setRowGroup(6);
            subformField.setColOrder(130);
            subformField.setRequired(false);
            subformField.setShowInGrid(false);
            subformField.setFormula("invoice_id"); // Child FK column
            mdForm.getFields().add(subformField);

            formMetaRepository.save(mdForm);
            dynamicDataService.generatePhysicalTable(mdForm);

            // Insert initial mock data to make it look full and ready
            try {
                jdbcTemplate.update("INSERT INTO dynamic.scroll_hdr (id, invoice_no, invoice_date, customer, npwp, payment_term, salesperson, shipping_method, tracking_no, warehouse_code, currency, exchange_rate, notes) VALUES " +
                        "(101, 'INV/2026/0001', '2026-06-22', 'IT', '01.234.567.8-901.000', 'COD', 'ALEX GMN', 'JNE YES', 'TRK1002345', 'WH-MAIN', 'IDR', 1.0, 'Pengiriman tahap pertama'), " +
                        "(102, 'INV/2026/0002', '2026-06-23', 'HR', '02.456.789.0-123.000', '30 DAYS', 'SARAH JANE', 'DHL EXPRESS', 'TRK9988771', 'WH-EAST', 'USD', 16500.0, 'Prioritas pengiriman kilat')");
                
                jdbcTemplate.update("INSERT INTO dynamic.scroll_dtl (id, invoice_id, item_code, item_name, qty, uom, price, discount_pct, discount_amt, tax_pct, tax_amt, weight_grid, batch_no, expiry_date, total_price, notes_detail) VALUES " +
                        "(201, 101, 'IT-DEV', 'Software Development Service', 5, 'Man-Months', 15000000.00, 10.0, 7500000.00, 11.0, 7425000.00, 0.0, 'BATCH-01', '2027-12-31', 74925000.00, 'Development Phase 1'), " +
                        "(202, 101, 'IT-OPS', 'IT Infrastructure Ops', 10, 'Hours', 850000.00, 0.0, 0.0, 11.0, 935000.00, 2.5, 'BATCH-02', '2026-12-31', 9435000.00, 'Support Setup'), " +
                        "(203, 102, 'HR-REC', 'Senior Recruiter Hour', 20, 'Hours', 1200000.00, 5.0, 1200000.00, 11.0, 2508000.00, 1.2, 'BATCH-R3', '2026-08-31', 25308000.00, 'Recruitment Drive')");
            } catch (Exception e) {
                System.out.println("Mock data insert for SCROLL_MD already exists or failed: " + e.getMessage());
            }
        }

        // Create table master_item if not exists under schema dynamic
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS dynamic;");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dynamic.master_item (" +
                "id SERIAL PRIMARY KEY, " +
                "item_code VARCHAR(50), " +
                "item_name VARCHAR(255), " +
                "category VARCHAR(100), " +
                "uom VARCHAR(50), " +
                "price DECIMAL(19, 2), " +
                "stock_qty INTEGER, " +
                "status VARCHAR(50)" +
                ")");

        // Insert mock data into master_item
        Integer masterItemCheck = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dynamic.master_item", Integer.class);
        if (masterItemCheck == null || masterItemCheck == 0) {
            jdbcTemplate.execute("INSERT INTO dynamic.master_item (item_code, item_name, category, uom, price, stock_qty, status) VALUES " +
                    "('ITM-001', 'MacBook Pro M3 Max 16-inch', 'Electronics', 'Unit', 48000000.00, 10, 'Active'), " +
                    "('ITM-002', 'Logitech MX Master 3S Mouse', 'Accessories', 'Unit', 1650000.00, 50, 'Active'), " +
                    "('ITM-003', 'Dell UltraSharp 32 4K Monitor U3223QE', 'Electronics', 'Unit', 14200000.00, 15, 'Active'), " +
                    "('ITM-004', 'Keychron Q1 Pro Mechanical Keyboard', 'Accessories', 'Unit', 2950000.00, 30, 'Active'), " +
                    "('ITM-005', 'Herman Miller Aeron Chair', 'Furniture', 'Unit', 25000000.00, 5, 'Active')");
        }

        if (!formMetaRepository.existsById("MASTER_ITEM")) {
            System.out.println("Inserting fresh MASTER_ITEM metadata...");

            FormMeta itemForm = new FormMeta();
            itemForm.setFormCode("MASTER_ITEM");
            itemForm.setFormTitle("Master Data Barang");
            itemForm.setTableName("master_item");
            itemForm.setPrimaryKey("id");
            itemForm.setLabelWidth("150px");
            itemForm.setFields(new ArrayList<>());

            addMasterField(itemForm, "item_code", "Kode Barang", "TEXTBOX", 1, 10, true);
            addMasterField(itemForm, "item_name", "Nama Barang", "TEXTBOX", 1, 20, true);
            addMasterField(itemForm, "category", "Kategori", "COMBOBOX", null, 2, 30, false);
            
            FieldMeta uomField = new FieldMeta();
            uomField.setFormMeta(itemForm);
            uomField.setFieldName("uom");
            uomField.setFieldLabel("Satuan");
            uomField.setComponentType("COMBOBOX");
            uomField.setLovCode("GLOBAL_MASTER");
            uomField.setRowGroup(2);
            uomField.setColOrder(40);
            uomField.setRequired(true);
            uomField.setShowInGrid(true);
            uomField.setDetail(false);

            List<FieldFilterMeta> uomFilters = new ArrayList<>();
            FieldFilterMeta uomFilt = new FieldFilterMeta();
            uomFilt.setFieldMeta(uomField);
            uomFilt.setFilterColumn("category");
            uomFilt.setSourceType("STATIC");
            uomFilt.setSourceName("UOM");
            uomFilters.add(uomFilt);
            uomField.setFilters(uomFilters);

            itemForm.getFields().add(uomField);

            addMasterField(itemForm, "price", "Harga Satuan", "DECIMALBOX", 3, 50, true);
            addMasterField(itemForm, "stock_qty", "Stok", "INTBOX", 3, 60, true);
            addMasterField(itemForm, "status", "Status", "COMBOBOX", null, 4, 70, true);

            formMetaRepository.save(itemForm);
            dynamicDataService.generatePhysicalTable(itemForm);
        }

        if (!formMetaRepository.existsById("MASTER_CUSTOMER")) {
            System.out.println("Inserting fresh MASTER_CUSTOMER metadata...");

            FormMeta custForm = new FormMeta();
            custForm.setFormCode("MASTER_CUSTOMER");
            custForm.setFormTitle("Master Data Pelanggan");
            custForm.setTableName("master_customer");
            custForm.setPrimaryKey("id");
            custForm.setLabelWidth("160px");
            custForm.setFields(new ArrayList<>());

            addMasterField(custForm, "customer_code", "Kode Pelanggan", "TEXTBOX", 1, 10, true);
            addMasterField(custForm, "customer_name", "Nama Pelanggan", "TEXTBOX", 1, 20, true);
            addMasterField(custForm, "contact_person", "Contact Person", "TEXTBOX", 2, 30, false);
            addMasterField(custForm, "phone", "No. Telepon / HP", "TEXTBOX", 2, 40, false);
            addMasterField(custForm, "email", "Email", "TEXTBOX", 3, 50, false);
            addMasterField(custForm, "credit_limit", "Batas Kredit", "DECIMALBOX", 3, 60, false);
            addMasterField(custForm, "address", "Alamat Lengkap", "TEXTAREA", 4, 70, false);
            addMasterField(custForm, "status", "Status", "COMBOBOX", null, 5, 80, true);

            formMetaRepository.save(custForm);
            dynamicDataService.generatePhysicalTable(custForm);
        }

        if (!lovMetaRepository.existsById("MASTER_CUSTOMER")) {
            LovMeta custLov = new LovMeta();
            custLov.setLovCode("MASTER_CUSTOMER");
            custLov.setLovName("Daftar Pelanggan");
            custLov.setTableName("master_customer");
            custLov.setValueColumn("customer_code");
            custLov.setLabelColumn("customer_name");
            custLov.setSearchColumn("customer_name");
            custLov.setGridColumns("customer_code:Kode Pelanggan:130px,customer_name:Nama Pelanggan:200px,contact_person:Contact Person:150px,phone:Telepon:130px");
            lovMetaRepository.save(custLov);
        }

        try {
            Integer custCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dynamic.master_customer", Integer.class);
            if (custCount == null || custCount < 1000) {
                jdbcTemplate.execute("DELETE FROM dynamic.master_customer");
                List<Object[]> batchArgs = new ArrayList<>();
                String[] cities = {"Jakarta", "Surabaya", "Bandung", "Medan", "Semarang", "Makassar", "Palembang", "Denpasar", "Yogyakarta", "Balikpapan"};
                String[] companyTypes = {"PT. ", "CV. ", "Toko ", "UD. ", "Fa. "};
                String[] names = {"Sinar Makmur", "Maju Bersama", "Karya Abadi", "Nusantara Jaya", "Mitra Sejati", "Sukses Selalu", "Bintang Harapan", "Citra Mandiri", "Giri Prima", "Buana Tunggal"};
                String[] contacts = {"Budi Santoso", "Siti Aminah", "Hendra Wijaya", "Agus Pratama", "Dewi Lestari", "Rudi Hartono", "Maya Sari", "Andi Saputra", "Rina Marlina", "Joko Surono"};
                for (int i = 1; i <= 1000; i++) {
                    String code = String.format("CUST-%04d", i);
                    String name = companyTypes[i % companyTypes.length] + names[i % names.length] + " " + i;
                    String contact = contacts[i % contacts.length];
                    String phone = String.format("0812%08d", 10000000 + i);
                    String email = "cust" + i + "@example.com";
                    double limit = (10 + (i % 50)) * 1000000.0;
                    String address = "Jl. Sudirman No. " + i + ", " + cities[i % cities.length];
                    String status = (i % 20 == 0) ? "Inactive" : "Active";
                    batchArgs.add(new Object[]{code, name, contact, phone, email, limit, address, status});
                }
                jdbcTemplate.batchUpdate("INSERT INTO dynamic.master_customer (customer_code, customer_name, contact_person, phone, email, credit_limit, address, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", batchArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check if GLOBAL_MASTER needs to be re-initialized for Subform Grid
        FormMeta existingGlobal = formMetaRepository.findById("GLOBAL_MASTER").orElse(null);
        boolean recreateGlobal = false;
        if (existingGlobal != null) {
            boolean hasSubform = existingGlobal.getFields() != null && existingGlobal.getFields().stream()
                    .anyMatch(f -> "SUBFORM_GRID".equalsIgnoreCase(f.getComponentType()));
            if (!hasSubform) {
                recreateGlobal = true;
            }
        } else {
            recreateGlobal = true;
        }

        if (recreateGlobal) {
            if (existingGlobal != null) {
                formMetaRepository.delete(existingGlobal);
                formMetaRepository.flush();
            }
            if (formMetaRepository.existsById("GLOBAL_MASTER_DTL")) {
                formMetaRepository.deleteById("GLOBAL_MASTER_DTL");
                formMetaRepository.flush();
            }
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS dynamic.global_master");
            } catch (Exception e) {}
        }

        try {
            jdbcTemplate.execute("UPDATE meta_form SET table_name = 'global_category' WHERE table_name = 'global_master'");
            jdbcTemplate.execute("UPDATE meta_lov SET table_name = 'global_category' WHERE table_name = 'global_master'");
        } catch (Exception ignored) {}

        // Create table global_category (Master)
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dynamic.global_category (" +
                "id SERIAL PRIMARY KEY, " +
                "category_code VARCHAR(50), " +
                "category_name VARCHAR(255), " +
                "description TEXT, " +
                "status VARCHAR(50)" +
                ")");

        Integer categoryCheck = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dynamic.global_category", Integer.class);
        if (categoryCheck == null || categoryCheck == 0) {
            jdbcTemplate.execute("INSERT INTO dynamic.global_category (category_code, category_name, description, status) VALUES " +
                    "('UOM', 'Unit of Measurement', 'Satuan Ukuran', 'Active'), " +
                    "('CITY', 'City', 'Kota / Kabupaten', 'Active'), " +
                    "('POSTAL_CODE', 'Postal Code', 'Kodepos', 'Active')");
        }

        try {
            jdbcTemplate.execute("CREATE OR REPLACE VIEW dynamic.global_master AS SELECT * FROM dynamic.global_category");
        } catch (Exception ignored) {}

        // Create table global_master_detail (Detail)
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dynamic.global_master_detail (" +
                "id SERIAL PRIMARY KEY, " +
                "global_category_id INT, " +
                "code VARCHAR(50), " +
                "name VARCHAR(255), " +
                "description TEXT, " +
                "status VARCHAR(50)" +
                ")");

        Integer detailCheck = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dynamic.global_master_detail", Integer.class);
        if (detailCheck == null || detailCheck == 0) {
            jdbcTemplate.execute("INSERT INTO dynamic.global_master_detail (global_category_id, code, name, description, status) VALUES " +
                    "(1, 'PCS', 'Pieces', 'Satuan Pieces', 'Active'), " +
                    "(1, 'KG', 'Kilogram', 'Satuan Kilogram', 'Active'), " +
                    "(1, 'LTR', 'Liter', 'Satuan Liter', 'Active'), " +
                    "(2, 'JKT', 'Jakarta', 'Kota Jakarta', 'Active'), " +
                    "(2, 'BDG', 'Bandung', 'Kota Bandung', 'Active'), " +
                    "(2, 'SBY', 'Surabaya', 'Kota Surabaya', 'Active'), " +
                    "(3, '12190', 'Kebayoran Baru', 'Kodepos Kebayoran Baru, Jakarta', 'Active'), " +
                    "(3, '40111', 'Sumurbandung', 'Kodepos Sumurbandung, Bandung', 'Active')");
        }

        if (!formMetaRepository.existsById("GLOBAL_MASTER_DTL")) {
            System.out.println("Inserting GLOBAL_MASTER_DTL metadata...");
            FormMeta dtlForm = new FormMeta();
            dtlForm.setFormCode("GLOBAL_MASTER_DTL");
            dtlForm.setFormTitle("Global Master Data Detail");
            dtlForm.setFormType("SINGLE");
            dtlForm.setTableName("global_master_detail");
            dtlForm.setPrimaryKey("id");
            dtlForm.setLabelWidth("150px");
            dtlForm.setFields(new ArrayList<>());

            FieldMeta df1 = new FieldMeta();
            df1.setFormMeta(dtlForm);
            df1.setFieldName("code");
            df1.setFieldLabel("Kode");
            df1.setComponentType("TEXTBOX");
            df1.setColOrder(10);
            df1.setRequired(true);
            df1.setShowInGrid(true);
            dtlForm.getFields().add(df1);

            FieldMeta df2 = new FieldMeta();
            df2.setFormMeta(dtlForm);
            df2.setFieldName("name");
            df2.setFieldLabel("Nama / Nilai");
            df2.setComponentType("TEXTBOX");
            df2.setColOrder(20);
            df2.setRequired(true);
            df2.setShowInGrid(true);
            dtlForm.getFields().add(df2);

            FieldMeta df3 = new FieldMeta();
            df3.setFormMeta(dtlForm);
            df3.setFieldName("description");
            df3.setFieldLabel("Deskripsi");
            df3.setComponentType("TEXTBOX");
            df3.setColOrder(30);
            df3.setRequired(false);
            df3.setShowInGrid(true);
            dtlForm.getFields().add(df3);

            FieldMeta df4 = new FieldMeta();
            df4.setFormMeta(dtlForm);
            df4.setFieldName("status");
            df4.setFieldLabel("Status");
            df4.setComponentType("COMBOBOX");
            df4.setColOrder(40);
            df4.setRequired(true);
            df4.setShowInGrid(true);
            dtlForm.getFields().add(df4);

            formMetaRepository.save(dtlForm);
            dynamicDataService.generatePhysicalTable(dtlForm);
        }

        if (!formMetaRepository.existsById("GLOBAL_MASTER")) {
            System.out.println("Inserting GLOBAL_MASTER metadata (SINGLE with SUBFORM_GRID)...");

            FormMeta globalForm = new FormMeta();
            globalForm.setFormCode("GLOBAL_MASTER");
            globalForm.setFormTitle("Global Master Data");
            globalForm.setFormType("SINGLE"); // Set to SINGLE because subform grid is self-contained in a field
            globalForm.setTableName("global_category");
            globalForm.setPrimaryKey("id");
            globalForm.setLabelWidth("150px");
            globalForm.setFields(new ArrayList<>());

            addMasterField(globalForm, "category_code", "Kode Kategori", "TEXTBOX", 1, 10, true);
            addMasterField(globalForm, "category_name", "Nama Kategori", "TEXTBOX", 1, 20, true);
            addMasterField(globalForm, "description", "Deskripsi", "TEXTAREA", 2, 30, false);
            addMasterField(globalForm, "status", "Status", "COMBOBOX", null, 3, 40, true);

            FieldMeta subformField = new FieldMeta();
            subformField.setFormMeta(globalForm);
            subformField.setFieldName("details");
            subformField.setFieldLabel("Daftar Detail Nilai");
            subformField.setComponentType("SUBFORM_GRID");
            subformField.setLovCode("GLOBAL_MASTER_DTL"); // Child form code
            subformField.setRowGroup(4);
            subformField.setColOrder(50);
            subformField.setRequired(false);
            subformField.setShowInGrid(false);
            subformField.setFormula("global_category_id"); // Child FK column
            globalForm.getFields().add(subformField);

            formMetaRepository.save(globalForm);
            dynamicDataService.generatePhysicalTable(globalForm);
        }

        System.out.println("Generic dummy data and metadata initialized successfully.");
    }

    private void addDetailField(FormMeta form, String fieldName, String fieldLabel, String componentType, String lovCode, int colOrder, boolean isRequired, boolean showInGrid) {
        FieldMeta f = new FieldMeta();
        f.setFormMeta(form);
        f.setFieldName(fieldName);
        f.setFieldLabel(fieldLabel);
        f.setComponentType(componentType);
        f.setLovCode(lovCode);
        f.setColOrder(colOrder);
        f.setRequired(isRequired);
        f.setShowInGrid(showInGrid);
        f.setDetail(true);
        f.setRowGroup(1);
        form.getFields().add(f);
    }

    private void addMasterField(FormMeta form, String fieldName, String fieldLabel, String componentType, int rowGroup, int colOrder, boolean isRequired) {
        addMasterField(form, fieldName, fieldLabel, componentType, null, rowGroup, colOrder, isRequired);
    }

    private void addMasterField(FormMeta form, String fieldName, String fieldLabel, String componentType, String lovCode, int rowGroup, int colOrder, boolean isRequired) {
        FieldMeta f = new FieldMeta();
        f.setFormMeta(form);
        f.setFieldName(fieldName);
        f.setFieldLabel(fieldLabel);
        f.setComponentType(componentType);
        f.setLovCode(lovCode);
        f.setRowGroup(rowGroup);
        f.setColOrder(colOrder);
        f.setRequired(isRequired);
        f.setShowInGrid(true);
        f.setDetail(false);
        form.getFields().add(f);
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

    private void initSecurityCoreTables() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.app_roles (" +
                "role_code VARCHAR(50) PRIMARY KEY, " +
                "role_name VARCHAR(100), " +
                "description VARCHAR(255)" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.app_users (" +
                "username VARCHAR(50) PRIMARY KEY, " +
                "password_hash VARCHAR(255), " +
                "full_name VARCHAR(100), " +
                "role_code VARCHAR(50), " +
                "is_active BOOLEAN DEFAULT TRUE" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.app_menus (" +
                "menu_code VARCHAR(50) PRIMARY KEY, " +
                "menu_title VARCHAR(100), " +
                "route_path VARCHAR(100), " +
                "icon_name VARCHAR(50), " +
                "parent_menu_code VARCHAR(50), " +
                "display_order INTEGER DEFAULT 10, " +
                "menu_type VARCHAR(20) DEFAULT 'ITEM'" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.app_role_menu_permissions (" +
                "id SERIAL PRIMARY KEY, " +
                "role_code VARCHAR(50) NOT NULL, " +
                "menu_code VARCHAR(50) NOT NULL, " +
                "can_add BOOLEAN DEFAULT TRUE, " +
                "can_edit BOOLEAN DEFAULT TRUE, " +
                "can_delete BOOLEAN DEFAULT TRUE, " +
                "can_print BOOLEAN DEFAULT TRUE, " +
                "UNIQUE(role_code, menu_code)" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.app_user_favorite_menus (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "menu_code VARCHAR(50) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(username, menu_code)" +
                ")");

        // Seed default Roles
        Integer rCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.app_roles", Integer.class);
        if (rCount == null || rCount == 0) {
            jdbcTemplate.execute("INSERT INTO public.app_roles (role_code, role_name, description) VALUES " +
                    "('SUPER_ADMIN', 'Super Administrator', 'Hak akses penuh seluruh sistem'), " +
                    "('STAFF', 'Staff Gudang & Transaksi', 'Hak akses terbatas operasional')");
        }

        // Seed default Users
        Integer uCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.app_users", Integer.class);
        if (uCount == null || uCount == 0) {
            jdbcTemplate.execute("INSERT INTO public.app_users (username, password_hash, full_name, role_code, is_active) VALUES " +
                    "('admin', 'admin', 'Administrator Sistem ERP', 'SUPER_ADMIN', TRUE), " +
                    "('staff', 'staff', 'Jennie Staff Operasional', 'STAFF', TRUE)");
        }

        // Seed default Menus (Tree Structure)
        // GROUP = folder/parent, ITEM = leaf/clickable menu
        Integer mCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.app_menus", Integer.class);
        if (mCount == null || mCount == 0) {
            jdbcTemplate.execute("INSERT INTO public.app_menus (menu_code, menu_title, route_path, icon_name, parent_menu_code, display_order, menu_type) VALUES " +
                    // === Top-level Groups ===
                    "('GRP_DEV_TOOLS', 'Developer Tools', NULL, 'TOOLS', NULL, 10, 'GROUP'), " +
                    "('GRP_REPORTS', 'Report & Cetak', NULL, 'FILE_TEXT', NULL, 20, 'GROUP'), " +
                    "('GRP_SYSTEM', 'Sistem & Keamanan', NULL, 'COG', NULL, 30, 'GROUP'), " +
                    // === Developer Tools children ===
                    "('FORM_BUILDER', 'Form Metadata Builder', 'builder', 'WRENCH', 'GRP_DEV_TOOLS', 10, 'ITEM'), " +
                    "('DB_EXPLORER', 'Database Manager', 'explorer', 'DATABASE', 'GRP_DEV_TOOLS', 20, 'ITEM'), " +
                    "('LOV_BUILDER', 'LOV Metadata Builder', 'lov-builder', 'LIST', 'GRP_DEV_TOOLS', 30, 'ITEM'), " +
                    "('FORM_ACTION_BUILDER', 'Extra Toolbar Builder', 'action-builder', 'BOLT', 'GRP_DEV_TOOLS', 35, 'ITEM'), " +
                    "('STANDARD_FORMAT', 'Konfigurasi Format Standar', 'standard-format', 'SLIDERS', 'GRP_DEV_TOOLS', 40, 'ITEM'), " +
                    // === Report children ===
                    "('REPORT_BUILDER', 'Report Designer', 'report-builder', 'EDIT', 'GRP_REPORTS', 10, 'ITEM'), " +
                    "('REPORT_VIEWER', 'Report Viewer', 'report-viewer', 'PRINT', 'GRP_REPORTS', 20, 'ITEM'), " +
                     // === Sistem & Keamanan children ===
                     "('SECURITY_ADMIN', 'Security & Authority Admin', 'security-admin', 'SHIELD', 'GRP_SYSTEM', 10, 'ITEM'), " +
                     "('FIELD_AUDIT_LOG', 'Field Audit Log Viewer', 'field-audit-log', 'CLOCK', 'GRP_SYSTEM', 20, 'ITEM'), " +
                     "('SYSTEM_LOG_VIEWER', 'Server Log Viewer', 'system-log-viewer', 'FILE_TEXT', 'GRP_SYSTEM', 30, 'ITEM')");
         }

         try {
             String targetParent = "GRP_DEV_TOOLS";
             try {
                 String lovParent = jdbcTemplate.queryForObject("SELECT parent_menu_code FROM public.app_menus WHERE menu_code = 'LOV_BUILDER'", String.class);
                 if (lovParent != null) targetParent = lovParent;
             } catch (Exception ignored) {}

             Integer actionMenuExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.app_menus WHERE menu_code = 'FORM_ACTION_BUILDER'", Integer.class);
             if (actionMenuExists == null || actionMenuExists == 0) {
                 jdbcTemplate.execute("INSERT INTO public.app_menus (menu_code, menu_title, route_path, icon_name, parent_menu_code, display_order, menu_type) " +
                         "VALUES ('FORM_ACTION_BUILDER', 'Extra Toolbar Builder', 'action-builder', 'BOLT', '" + targetParent + "', 35, 'ITEM')");
             } else {
                 jdbcTemplate.execute("UPDATE public.app_menus SET parent_menu_code = '" + targetParent + "', display_order = 35 WHERE menu_code = 'FORM_ACTION_BUILDER'");
             }

             try {
                 String sysParentForAudit = "GRP_SYSTEM";
                 try {
                     String fLogParent = jdbcTemplate.queryForObject("SELECT parent_menu_code FROM public.app_menus WHERE menu_code = 'FIELD_AUDIT_LOG'", String.class);
                     if (fLogParent != null && !fLogParent.trim().isEmpty()) sysParentForAudit = fLogParent;
                 } catch (Exception ignored) {}

                 Integer auditMenuExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.app_menus WHERE menu_code = 'AUDIT_TRAIL_RESTORE'", Integer.class);
                 if (auditMenuExists == null || auditMenuExists == 0) {
                     jdbcTemplate.execute("INSERT INTO public.app_menus (menu_code, menu_title, route_path, icon_name, parent_menu_code, display_order, menu_type) " +
                             "VALUES ('AUDIT_TRAIL_RESTORE', 'Audit Trail & Restore Center', 'audit-trail', 'SHIELD', '" + sysParentForAudit + "', 25, 'ITEM')");
                 } else {
                     jdbcTemplate.execute("UPDATE public.app_menus SET parent_menu_code = '" + sysParentForAudit + "', display_order = 25 WHERE menu_code = 'AUDIT_TRAIL_RESTORE'");
                 }

                 Integer logViewerExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.app_menus WHERE menu_code = 'SYSTEM_LOG_VIEWER'", Integer.class);
                 if (logViewerExists == null || logViewerExists == 0) {
                     jdbcTemplate.execute("INSERT INTO public.app_menus (menu_code, menu_title, route_path, icon_name, parent_menu_code, display_order, menu_type) " +
                             "VALUES ('SYSTEM_LOG_VIEWER', 'Server Log Viewer', 'system-log-viewer', 'FILE_TEXT', '" + sysParentForAudit + "', 30, 'ITEM')");
                 } else {
                     jdbcTemplate.execute("UPDATE public.app_menus SET parent_menu_code = '" + sysParentForAudit + "', display_order = 30 WHERE menu_code = 'SYSTEM_LOG_VIEWER'");
                 }

                 jdbcTemplate.execute("INSERT INTO public.app_role_menu_permissions (role_code, menu_code, can_add, can_edit, can_delete, can_print) " +
                         "SELECT DISTINCT role_code, 'AUDIT_TRAIL_RESTORE', TRUE, TRUE, TRUE, TRUE FROM public.app_role_menu_permissions WHERE role_code IN ('ADMIN', 'SUPER_ADMIN') AND role_code NOT IN (SELECT role_code FROM public.app_role_menu_permissions WHERE menu_code = 'AUDIT_TRAIL_RESTORE')");
                 jdbcTemplate.execute("INSERT INTO public.app_role_menu_permissions (role_code, menu_code, can_add, can_edit, can_delete, can_print) " +
                         "SELECT DISTINCT role_code, 'SYSTEM_LOG_VIEWER', TRUE, TRUE, TRUE, TRUE FROM public.app_role_menu_permissions WHERE role_code IN ('ADMIN', 'SUPER_ADMIN') AND role_code NOT IN (SELECT role_code FROM public.app_role_menu_permissions WHERE menu_code = 'SYSTEM_LOG_VIEWER')");
             } catch (Exception ignored) {}
         } catch (Exception ignored) {}

        try {
            jdbcTemplate.execute("ALTER TABLE public.meta_form_action ALTER COLUMN form_code DROP NOT NULL");
        } catch (Exception ignored) {}

        // Seed permissions for STAFF role
        // STAFF hanya melihat menu yang punya record di sini. Yang tidak ada = tidak muncul di sidebar.
        // Untuk GROUP parent: jika minimal 1 child visible, GROUP otomatis terlihat.
        Integer pCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.app_role_menu_permissions", Integer.class);
        if (pCount == null || pCount == 0) {
            jdbcTemplate.execute("INSERT INTO public.app_role_menu_permissions (role_code, menu_code, can_add, can_edit, can_delete, can_print) VALUES " +
                    "('STAFF', 'DB_EXPLORER', FALSE, FALSE, FALSE, TRUE), " +
                    "('STAFF', 'REPORT_VIEWER', FALSE, FALSE, FALSE, TRUE), " +
                    "('STAFF', 'FIELD_AUDIT_LOG', FALSE, FALSE, FALSE, TRUE)");
        }
        try {
            jdbcTemplate.execute("INSERT INTO public.app_role_menu_permissions (role_code, menu_code, can_add, can_edit, can_delete, can_print) " +
                    "SELECT DISTINCT role_code, 'FORM_ACTION_BUILDER', TRUE, TRUE, TRUE, TRUE FROM public.app_role_menu_permissions WHERE role_code NOT IN (SELECT role_code FROM public.app_role_menu_permissions WHERE menu_code = 'FORM_ACTION_BUILDER')");
        } catch (Exception ignored) {}

        initFormActionMetadata();
        initSequenceMaster();
    }

    private void initFormActionMetadata() {
        if (formActionMetaRepository != null && formActionMetaRepository.count() == 0) {
            System.out.println("Inserting sample FormActionMeta...");
            FormMeta invForm = formMetaRepository.findById("INVOICE_MD").orElse(null);
            if (invForm != null) {
                com.vaadinerp.meta.FormActionMeta act1 = new com.vaadinerp.meta.FormActionMeta();
                act1.setFormMeta(invForm);
                act1.setActionCode("PICK_MASTER_ITEM");
                act1.setActionLabel("Pick Master Item");
                act1.setIconName("CHECK_SQUARE_O");
                act1.setTargetScope("DETAIL_TOOLBAR");
                act1.setSourceLovCode("lov_child");
                act1.setTargetMapping("item_code:code,price:10000,qty:1");
                formActionMetaRepository.save(act1);
            }

            FormMeta globalDtlForm = formMetaRepository.findById("GLOBAL_MASTER_DTL").orElse(null);
            if (globalDtlForm != null) {
                com.vaadinerp.meta.FormActionMeta act2 = new com.vaadinerp.meta.FormActionMeta();
                act2.setFormMeta(globalDtlForm);
                act2.setActionCode("PICK_GLOBAL_CATEGORY");
                act2.setActionLabel("Pick Category Items");
                act2.setIconName("LIST_SELECT");
                act2.setTargetScope("DETAIL_TOOLBAR");
                act2.setSourceLovCode("GLOBAL_MASTER");
                act2.setTargetMapping("code:code,name:name");
                formActionMetaRepository.save(act2);
            }
        }
    }

    private void initSequenceMaster() {
        try {
            jdbcTemplate.execute("ALTER TABLE public.meta_field ADD COLUMN IF NOT EXISTS sequence_code VARCHAR(50)");
            jdbcTemplate.execute("ALTER TABLE public.meta_field ADD COLUMN IF NOT EXISTS on_add_script VARCHAR(2000)");
        } catch (Exception ignored) {}

        // 1. Create table dynamic.md_sequence
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dynamic.md_sequence (" +
                    "id SERIAL PRIMARY KEY, " +
                    "seq_code VARCHAR(50) UNIQUE NOT NULL, " +
                    "seq_name VARCHAR(255) NOT NULL, " +
                    "prefix_format VARCHAR(100), " +
                    "current_val BIGINT DEFAULT 0, " +
                    "padding_len INTEGER DEFAULT 4, " +
                    "reset_period VARCHAR(20) DEFAULT 'NEVER', " +
                    "last_reset_date DATE, " +
                    "status VARCHAR(50) DEFAULT 'Active'" +
                    ")");
            Integer seqCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dynamic.md_sequence", Integer.class);
            if (seqCount == null || seqCount == 0) {
                jdbcTemplate.execute("INSERT INTO dynamic.md_sequence (seq_code, seq_name, prefix_format, current_val, padding_len, reset_period, last_reset_date, status) VALUES " +
                        "('PO_NO', 'Penomoran Purchase Order', 'PO{YY}/{MM}/', 0, 4, 'MONTHLY', CURRENT_DATE, 'Active'), " +
                        "('BOM_CODE', 'Penomoran Bill of Material', 'BOM', 0, 5, 'NEVER', CURRENT_DATE, 'Active'), " +
                        "('INV_NO', 'Penomoran Faktur Penjualan', 'INV/{YYYY}/{MM}/', 0, 4, 'MONTHLY', CURRENT_DATE, 'Active')");
            }
        } catch (Exception e) {
            System.err.println("Failed initializing md_sequence table: " + e.getMessage());
        }

        // 2. Create LOVs for Reset Period and Status
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dynamic.lov_reset_period (code VARCHAR(50) PRIMARY KEY, name VARCHAR(100))");
            jdbcTemplate.execute("INSERT INTO dynamic.lov_reset_period (code, name) VALUES " +
                    "('NEVER', 'Tanpa Reset (Terus Naik)'), " +
                    "('YEARLY', 'Reset Tahunan (Tahun Baru)'), " +
                    "('MONTHLY', 'Reset Bulanan (Bulan Baru)'), " +
                    "('DAILY', 'Reset Harian (Hari Baru)') " +
                    "ON CONFLICT (code) DO NOTHING");
            if (!lovMetaRepository.existsById("RESET_PERIOD_LOV")) {
                LovMeta lov = new LovMeta();
                lov.setLovCode("RESET_PERIOD_LOV");
                lov.setLovName("Daftar Periode Reset");
                lov.setTableName("lov_reset_period");
                lov.setValueColumn("code");
                lov.setLabelColumn("name");
                lov.setSearchColumn("name");
                lov.setGridColumns("code:Kode:100px,name:Keterangan:220px");
                lovMetaRepository.save(lov);
            }
        } catch (Exception ignored) {}

        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dynamic.lov_status (code VARCHAR(50) PRIMARY KEY, name VARCHAR(100))");
            jdbcTemplate.execute("INSERT INTO dynamic.lov_status (code, name) VALUES " +
                    "('Active', 'Aktif / Active'), " +
                    "('Inactive', 'Non-Aktif / Inactive') " +
                    "ON CONFLICT (code) DO NOTHING");
            if (!lovMetaRepository.existsById("STATUS_LOV")) {
                LovMeta lov = new LovMeta();
                lov.setLovCode("STATUS_LOV");
                lov.setLovName("Daftar Status Aktif");
                lov.setTableName("lov_status");
                lov.setValueColumn("code");
                lov.setLabelColumn("name");
                lov.setSearchColumn("name");
                lov.setGridColumns("code:Kode Status:120px,name:Keterangan:200px");
                lovMetaRepository.save(lov);
            }
        } catch (Exception ignored) {}

        // 3. Initialize FormMeta for MD_SEQUENCE
        if (!formMetaRepository.existsById("MD_SEQUENCE")) {
            FormMeta form = new FormMeta();
            form.setFormCode("MD_SEQUENCE");
            form.setFormTitle("Master Penomoran Dokumen (Sequence)");
            form.setTableName("md_sequence");
            form.setFormType("GENERIC");
            form.setFields(new ArrayList<>());

            addMasterField(form, "seq_code", "Kode Sequence", "TEXTBOX", null, 1, 10, true);
            addMasterField(form, "seq_name", "Nama / Keterangan", "TEXTBOX", null, 1, 20, true);
            addMasterField(form, "prefix_format", "Format Awalan (mis. PO{YY}/{MM}/)", "TEXTBOX", null, 2, 30, false);
            addMasterField(form, "current_val", "Angka Terakhir Terpakai", "INTBOX", null, 2, 40, true);
            addMasterField(form, "padding_len", "Panjang Digit (mis. 4 -> 0001)", "INTBOX", null, 3, 50, true);
            addMasterField(form, "reset_period", "Periode Reset", "COMBOBOX", "RESET_PERIOD_LOV", 3, 60, true);
            addMasterField(form, "status", "Status", "COMBOBOX", "STATUS_LOV", 4, 70, true);

            formMetaRepository.save(form);
            dynamicDataService.generatePhysicalTable(form);
        }

        // 4. Seed menu in app_menus
        try {
            String targetParent = "GRP_DEV_TOOLS";
            try {
                String lovParent = jdbcTemplate.queryForObject("SELECT parent_menu_code FROM public.app_menus WHERE menu_code = 'LOV_BUILDER'", String.class);
                if (lovParent != null) targetParent = lovParent;
            } catch (Exception ignored) {}

            Integer seqMenuExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.app_menus WHERE menu_code = 'MD_SEQUENCE'", Integer.class);
            if (seqMenuExists == null || seqMenuExists == 0) {
                jdbcTemplate.execute("INSERT INTO public.app_menus (menu_code, menu_title, route_path, icon_name, parent_menu_code, display_order, menu_type) " +
                        "VALUES ('MD_SEQUENCE', 'Master Penomoran Dokumen', 'MD_SEQUENCE', 'BARCODE', '" + targetParent + "', 45, 'ITEM')");
            } else {
                jdbcTemplate.execute("UPDATE public.app_menus SET parent_menu_code = '" + targetParent + "', menu_title = 'Master Penomoran Dokumen', route_path = 'MD_SEQUENCE', display_order = 45 WHERE menu_code = 'MD_SEQUENCE'");
            }
            jdbcTemplate.execute("INSERT INTO public.app_role_menu_permissions (role_code, menu_code, can_add, can_edit, can_delete, can_print) " +
                    "SELECT DISTINCT role_code, 'MD_SEQUENCE', TRUE, TRUE, TRUE, TRUE FROM public.app_role_menu_permissions WHERE role_code NOT IN (SELECT role_code FROM public.app_role_menu_permissions WHERE menu_code = 'MD_SEQUENCE')");
        } catch (Exception ignored) {}
    }
}
