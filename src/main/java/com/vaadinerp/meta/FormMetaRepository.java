package com.vaadinerp.meta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FormMetaRepository extends JpaRepository<FormMeta, String> {
    Optional<FormMeta> findByTableName(String tableName);
}
