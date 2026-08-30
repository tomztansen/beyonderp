package com.vaadinerp.security.config;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.entity.RoleMenuPermission;
import com.vaadinerp.security.repository.RoleMenuPermissionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportSecurityInterceptorTest {

    private RoleMenuPermissionRepository perms;
    private ReportMetaRepository reports;
    private ReportSecurityInterceptor interceptor;
    private HttpServletRequest req;
    private HttpServletResponse res;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        perms = mock(RoleMenuPermissionRepository.class);
        reports = mock(ReportMetaRepository.class);
        interceptor = new ReportSecurityInterceptor(perms, reports);
        req = mock(HttpServletRequest.class);
        res = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(req.getSession()).thenReturn(session);
    }

    private void loginWithRoles(String... roles) {
        AppUser u = new AppUser();
        u.setRoles(Set.of(roles));
        when(session.getAttribute("SPRING_MVC_USER")).thenReturn(u);
    }

    @Test
    void rejectsAnonymous() throws Exception {
        when(session.getAttribute("SPRING_MVC_USER")).thenReturn(null);
        when(req.getRequestURI()).thenReturn("/stimulsoft-java/viewer");

        assertThat(interceptor.preHandle(req, res, new Object())).isFalse();
        verify(res).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
    }

    @Test
    void blocksDesignerWithoutMenuPermission() throws Exception {
        loginWithRoles("SALES");
        when(req.getRequestURI()).thenReturn("/stimulsoft-java/designer");
        when(perms.findByRoleCodeAndMenuCode("SALES", "REPORT_DESIGNER")).thenReturn(Optional.empty());

        assertThat(interceptor.preHandle(req, res, new Object())).isFalse();
        verify(res).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    void allowsDesignerWithMenuPermission() throws Exception {
        loginWithRoles("REPORT_ADMIN");
        when(req.getRequestURI()).thenReturn("/stimulsoft-java/designer");
        when(perms.findByRoleCodeAndMenuCode("REPORT_ADMIN", "REPORT_DESIGNER"))
                .thenReturn(Optional.of(new RoleMenuPermission()));

        assertThat(interceptor.preHandle(req, res, new Object())).isTrue();
        verify(res, never()).sendError(anyInt(), anyString());
    }

    @Test
    void superAdminBypassesDesignerGate() throws Exception {
        loginWithRoles("SUPER_ADMIN");
        when(req.getRequestURI()).thenReturn("/stimulsoft-java/designer");

        assertThat(interceptor.preHandle(req, res, new Object())).isTrue();
        verifyNoInteractions(perms);
    }

    @Test
    void blocksViewerForReportUserCannotAccess() throws Exception {
        loginWithRoles("SALES");
        when(req.getRequestURI()).thenReturn("/stimulsoft-java/viewer");
        when(req.getParameter("code")).thenReturn("RPT_SECRET");
        ReportMeta secret = new ReportMeta();
        secret.setAllowedRoles(Set.of("FINANCE"));
        when(reports.findById("RPT_SECRET")).thenReturn(Optional.of(secret));

        assertThat(interceptor.preHandle(req, res, new Object())).isFalse();
        verify(res).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    void allowsViewerForReportUserCanAccess() throws Exception {
        loginWithRoles("FINANCE");
        when(req.getRequestURI()).thenReturn("/stimulsoft-java/viewer");
        when(req.getParameter("code")).thenReturn("RPT_OK");
        ReportMeta ok = new ReportMeta();
        ok.setAllowedRoles(Set.of("FINANCE"));
        when(reports.findById("RPT_OK")).thenReturn(Optional.of(ok));

        assertThat(interceptor.preHandle(req, res, new Object())).isTrue();
        verify(res, never()).sendError(anyInt(), anyString());
    }

    @Test
    void letsCodelessCallbackThroughOnAuthOnly() throws Exception {
        // Stimulsoft's AJAX callbacks hit sub-paths without ?code= — must not be blocked,
        // or the viewer UI breaks. Authentication alone is enough here.
        loginWithRoles("SALES");
        when(req.getRequestURI()).thenReturn("/stimulsoft-java/viewer");
        when(req.getParameter("code")).thenReturn(null);

        assertThat(interceptor.preHandle(req, res, new Object())).isTrue();
        verify(reports, never()).findById(anyString());
        verify(res, never()).sendError(anyInt(), anyString());
    }
}
