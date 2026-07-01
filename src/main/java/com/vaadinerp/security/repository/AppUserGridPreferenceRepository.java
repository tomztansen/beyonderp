package com.vaadinerp.security.repository;

import com.vaadinerp.security.entity.AppUserGridPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface AppUserGridPreferenceRepository extends JpaRepository<AppUserGridPreference, Long> {
    Optional<AppUserGridPreference> findByUsernameAndFormCodeAndGridId(String username, String formCode, String gridId);

    @Transactional
    void deleteByUsernameAndFormCodeAndGridId(String username, String formCode, String gridId);
}
