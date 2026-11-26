package com.vaadinerp.security.config;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.meta.ReportMetaRepository;
import com.vaadinerp.report.ReportAccessService;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.entity.RoleMenuPermission;
import com.vaadinerp.security.repository.RoleMenuPermissionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Guards the /stimulsoft-java/** endpoints. Runs on a plain MVC request thread where
 * VaadinSession is not available, so it reads the logged-in AppUser straight from the
 * HTTP session (SPRING_MVC_USER) and re-implements the permission check instead of
 * calling SessionSecurityService (which resolves the user via VaadinSession.getCurrent()).
 */
@Component
public class ReportSecurityInterceptor implements HandlerInterceptor {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";
    /** Same menu code that gates the Vaadin report-designer route. */
    private static final String DESIGNER_MENU = "REPORT_DESIGNER";
    private static final String RUNNER_MENU = "REPORT_RUNNER";

    private final RoleMenuPermissionRepository permissionRepository;
    private final ReportMetaRepository reportRepository;

    public ReportSecurityInterceptor(RoleMenuPermissionRepository permissionRepository,
                                     ReportMetaRepository reportRepository) {
        this.permissionRepository = permissionRepository;
        this.reportRepository = reportRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Authentication: must be logged in through the main application.
        Object attr = request.getSession().getAttribute("SPRING_MVC_USER");
        if (!(attr instanceof AppUser user)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized access. Please login via the main application first.");
            return false;
        }
        Set<String> roles = user.getRoles() != null ? user.getRoles() : Set.of();
        boolean superAdmin = roles.contains(SUPER_ADMIN);

        String path = request.getRequestURI();

        // Authorization. Only the two entry points carry authorization intent; Stimulsoft's
        // own AJAX callbacks hit sub-paths (often without ?code=) and must pass on authN
        // alone, or the viewer/designer UI breaks. An unauthorized user cannot bootstrap
        // those callbacks because the initial page below is refused.
        if (path.endsWith("/stimulsoft-java/designer")) {
            // Designing a report defines arbitrary SQL/templates — gate it with the same
            // menu permission as the Vaadin report-designer route.
            if (!superAdmin && !hasMenuPermission(roles, DESIGNER_MENU)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "You are not allowed to design reports.");
                return false;
            }
            return true;
        }

        if (path.endsWith("/stimulsoft-java/viewer")) {
            // Per-report access: a logged-in user must not open a report they lack the role
            // for by guessing its code in the URL.
            String code = request.getParameter("code");
            if (code != null && !code.isBlank()) {
                ReportMeta report = reportRepository.findById(code).orElse(null);
                if (report == null
                        || !ReportAccessService.canAccess(roles, superAdmin, report.getAllowedRoles())
                        || (!superAdmin && !hasEntryPoint(roles, report))) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "You are not allowed to access this report.");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * The URL must not be a back door: the user needs one of the UI routes that opens this
     * viewer — Report Runner (RUNNER/BOTH), a form's Print button (FORM/BOTH, can_print), or
     * the Report Designer preview.
     */
    boolean hasEntryPoint(Set<String> roles, ReportMeta report) {
        if (hasMenuPermission(roles, DESIGNER_MENU)) return true;
        if (report.isUsableFrom("RUNNER") && hasMenuPermission(roles, RUNNER_MENU)) return true;
        if (!report.isUsableFrom("FORM")) return false;
        String form = report.getSourceFormCode();
        for (String role : roles) {
            for (RoleMenuPermission p : permissionRepository.findByRoleCode(role)) {
                // ponytail: tanpa source_form_code, can_print di form mana pun dianggap cukup
                // (mencocokkan tabel ke form butuh FormMeta); persempit bila perlu.
                boolean formOk = form == null || form.isBlank() || form.trim().equalsIgnoreCase(p.getMenuCode());
                if (formOk && Boolean.TRUE.equals(p.getCanPrint())) return true;
            }
        }
        return false;
    }

    private boolean hasMenuPermission(Set<String> roles, String menuCode) {
        for (String role : roles) {
            if (permissionRepository.findByRoleCodeAndMenuCode(role, menuCode).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
