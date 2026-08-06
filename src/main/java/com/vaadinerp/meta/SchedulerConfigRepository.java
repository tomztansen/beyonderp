package com.vaadinerp.meta;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SchedulerConfigRepository extends JpaRepository<SchedulerConfig, Integer> {
    Optional<SchedulerConfig> findByFormCode(String formCode);
}
