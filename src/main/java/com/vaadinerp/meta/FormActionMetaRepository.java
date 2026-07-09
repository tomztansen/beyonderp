package com.vaadinerp.meta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormActionMetaRepository extends JpaRepository<FormActionMeta, Long> {
    List<FormActionMeta> findByFormMeta_FormCodeAndTargetScope(String formCode, String targetScope);
    List<FormActionMeta> findByFormMeta_FormCode(String formCode);
    List<FormActionMeta> findByFormMeta_FormCodeIgnoreCase(String formCode);
    FormActionMeta findByActionCode(String actionCode);
    FormActionMeta findByActionCodeIgnoreCase(String actionCode);
    List<FormActionMeta> findByFormMetaIsNull();
}
