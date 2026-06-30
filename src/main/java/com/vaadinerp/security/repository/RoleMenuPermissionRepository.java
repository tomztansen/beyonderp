package com.vaadinerp.security.repository;

import com.vaadinerp.security.entity.RoleMenuPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoleMenuPermissionRepository extends JpaRepository<RoleMenuPermission, Long> {
    Optional<RoleMenuPermission> findByRoleCodeAndMenuCode(String roleCode, String menuCode);
    List<RoleMenuPermission> findByRoleCode(String roleCode);
}
