package com.vaadinerp.report;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class ReportAccessServiceTest {

    @Test
    void superAdminSeesEverythingIncludingNoRoles() {
        assertThat(ReportAccessService.canAccess(Set.of("X"), true, Set.of())).isTrue();
        assertThat(ReportAccessService.canAccess(Set.of("X"), true, Set.of("ADMIN"))).isTrue();
    }

    @Test
    void emptyAllowedRolesVisibleOnlyToSuperAdmin() {
        assertThat(ReportAccessService.canAccess(Set.of("ADMIN"), false, Set.of())).isFalse();
        assertThat(ReportAccessService.canAccess(Set.of("ADMIN"), false, null)).isFalse();
    }

    @Test
    void roleIntersectionGrantsAccess() {
        assertThat(ReportAccessService.canAccess(Set.of("SALES", "HR"), false, Set.of("SALES"))).isTrue();
        assertThat(ReportAccessService.canAccess(Set.of("HR"), false, Set.of("SALES"))).isFalse();
    }
}
