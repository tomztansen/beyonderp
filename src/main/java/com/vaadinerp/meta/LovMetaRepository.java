package com.vaadinerp.meta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LovMetaRepository extends JpaRepository<LovMeta, String> {

    @Query("SELECT l FROM LovMeta l WHERE " +
           "LOWER(l.lovCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.lovName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.tableName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<LovMeta> searchAll(@Param("search") String search, Pageable pageable);
}
