package com.vaadinerp.security.repository;

import com.vaadinerp.security.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
    Optional<AppUser> findByUsernameAndIsActiveTrue(String username);
    Optional<AppUser> findByUsernameIgnoreCaseAndIsActiveTrue(String username);
    Optional<AppUser> findByUsernameIgnoreCase(String username);
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) > 0 FROM AppUser u JOIN u.roles r WHERE r = :roleCode")
    boolean existsByRoleCode(@org.springframework.data.repository.query.Param("roleCode") String roleCode);
}
