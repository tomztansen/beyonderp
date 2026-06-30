package com.vaadinerp.security.repository;

import com.vaadinerp.security.entity.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, String> {
}
