package com.vaadinerp.components;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pemeriksa nama di editor script hanya berguna kalau tidak mengeluh untuk script
 * yang benar. False positive lebih merugikan daripada tidak memeriksa sama sekali,
 * karena user akan belajar mengabaikan peringatannya.
 */
class CodeEditorPanelNameCheckTest {

    private static final Set<String> KNOWN = Set.of("header", "row", "rowIndex", "db", "setElementValue");

    private static List<String> check(String script) {
        CompilationUnit cu = new CompilationUnit();
        cu.addSource("Test", script);
        cu.compile(Phases.SEMANTIC_ANALYSIS);
        return CodeEditorPanel.findUnknownNames(cu, KNOWN);
    }

    @Test
    void namaBindingYangDikenalTidakDilaporkan() {
        assertEquals(List.of(), check("setElementValue('a', header.qty)"));
    }

    @Test
    void salahEjaDilaporkan() {
        assertEquals(List.of("heder"), check("row.total = heder.qty"));
    }

    @Test
    void salahHurufBesarKecilPadaFungsiDilaporkan() {
        assertEquals(List.of("setElementReadOnly()"), check("setElementReadOnly('a', true)"));
    }

    @Test
    void variabelLokalTidakDilaporkan() {
        assertEquals(List.of(), check("def total = row.qty * 2\nrow.total = total"));
    }

    @Test
    void penugasanTanpaDefTidakDilaporkan() {
        assertEquals(List.of(), check("subtotal = row.qty * 2\nrow.total = subtotal"));
    }

    @Test
    void parameterClosureDanItTidakDilaporkan() {
        assertEquals(List.of(), check("[1, 2].each { angka -> row.total = angka }\n[1].each { row.total = it }"));
    }

    @Test
    void variabelForLoopDanCatchTidakDilaporkan() {
        assertEquals(List.of(), check(
                "for (i in 1..3) { row.total = i }\ntry { db.find('t', 'c', 1) } catch (ex) { row.err = ex }"));
    }

    @Test
    void printlnBoleh() {
        assertEquals(List.of(), check("println row.qty"));
    }

    @Test
    void tanpaDaftarNamaPemeriksaanDilewati() {
        CompilationUnit cu = new CompilationUnit();
        cu.addSource("Test", "apapun.yang.penting");
        cu.compile(Phases.SEMANTIC_ANALYSIS);
        assertTrue(CodeEditorPanel.findUnknownNames(cu, Set.of()).isEmpty());
    }
}
