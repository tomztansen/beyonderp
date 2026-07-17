package com.vaadinerp.security.repository;

import com.vaadinerp.security.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
    Optional<AppUser> findByUsernameAndIsActiveTrue(String username);
    Optional<AppUser> findByUsernameIgnoreCaseAndIsActiveTrue(String username);
    Optional<AppUser> findByUsernameIgnoreCase(String username);
}
