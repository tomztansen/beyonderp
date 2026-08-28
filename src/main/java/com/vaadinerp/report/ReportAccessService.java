package com.vaadinerp.report;

import com.vaadinerp.meta.ReportMeta;
import com.vaadinerp.security.entity.AppUser;
import com.vaadinerp.security.service.SessionSecurityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Menentukan report yang boleh diakses user di Report Runner.
 * Super-admin lihat semua; report tanpa allowedRoles → hanya super-admin.
 */
@Service
public class ReportAccessService {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final SessionSecurityService securityService;

    public ReportAccessService(SessionSecurityService securityService) {
        this.securityService = securityService;
    }

    /** Logika pure. */
    public static boolean canAccess(Set<String> userRoles, boolean superAdmin, Set<String> allowedRoles) {
        if (superAdmin) return true;
        if (allowedRoles == null || allowedRoles.isEmpty()) return false;
        if (userRoles == null) return false;
        for (String r : allowedRoles) {
            if (userRoles.contains(r)) return true;
        }
        return false;
    }

    public boolean canAccess(ReportMeta report) {
        AppUser u = securityService.getCurrentUser();
        Set<String> roles = (u != null && u.getRoles() != null) ? u.getRoles() : Set.of();
        boolean superAdmin = roles.contains(SUPER_ADMIN);
        return canAccess(roles, superAdmin, report.getAllowedRoles());
    }

    public List<ReportMeta> accessibleReports(List<ReportMeta> all) {
        return all.stream().filter(this::canAccess).collect(Collectors.toList());
    }
}
