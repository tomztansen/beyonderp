package com.vaadinerp.security.repository;

import com.vaadinerp.security.entity.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, String> {
    org.springframework.data.domain.Page<AppRole> findByRoleCodeContainingIgnoreCase(String roleCode, org.springframework.data.domain.Pageable pageable);
    long countByRoleCodeContainingIgnoreCase(String roleCode);
}
