package com.vaadinerp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vaadinerp.config.SpringContextHolder;
import com.vaadinerp.meta.FieldMeta;
import com.vaadinerp.meta.FormMeta;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuditEntityListener {

    private DynamicDataService getDynamicDataService() {
        return SpringContextHolder.getBean(DynamicDataService.class);
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return om;
    }

    private Class<?> getTargetClass(Object entity) {
        if (entity == null)
            return Object.class;
        Class<?> clazz = entity.getClass();
        if (org.hibernate.proxy.HibernateProxy.class.isAssignableFrom(clazz)) {
            return ((org.hibernate.proxy.HibernateProxy) entity).getHibernateLazyInitializer().getPersistentClass();
        }
        return clazz;
    }

    private String getTableName(Object entity) {
        if (entity == null)
            return "unknown";
        Class<?> targetClass = getTargetClass(entity);
        Table tableAnn = targetClass.getAnnotation(Table.class);
        if (tableAnn != null && !tableAnn.name().isEmpty()) {
            return tableAnn.name();
        }
        return targetClass.getSimpleName().toLowerCase();
    }

    private String getRecordId(Object entity) {
        if (entity == null)
            return "UNKNOWN";
        try {
            Class<?> targetClass = getTargetClass(entity);
            Class<?> current = targetClass;
            while (current != null && current != Object.class) {
                for (java.lang.reflect.Field field : current.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Id.class)) {
                        field.setAccessible(true);
                        Object val = field.get(entity);
                        return val != null ? val.toString() : "UNKNOWN";
                    }
                }
                current = current.getSuperclass();
            }
        } catch (Exception ignored) {
        }
        return "UNKNOWN";
    }

    private Map<String, Object> entityToMap(Object entity) {
        if (entity == null)
            return null;
        try {
            if (entity instanceof FormMeta fm) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("formCode", fm.getFormCode());
                map.put("formTitle", fm.getFormTitle());
                map.put("tableName", fm.getTableName());
                map.put("viewTable", fm.getViewTable());
                map.put("primaryKey", fm.getPrimaryKey());
                map.put("labelWidth", fm.getLabelWidth());
                map.put("defaultSortField", fm.getDefaultSortField());
                map.put("defaultSortDirection", fm.getDefaultSortDirection());
                map.put("formType", fm.getFormType());
                map.put("detailTableName", fm.getDetailTableName());
                map.put("detailPrimaryKey", fm.getDetailPrimaryKey());
                map.put("detailForeignKey", fm.getDetailForeignKey());
                map.put("extraToolbars", fm.getExtraToolbars());
                List<Map<String, Object>> fieldsList = new ArrayList<>();
                if (fm.getFields() != null) {
                    for (FieldMeta f : fm.getFields()) {
                        fieldsList.add(entityToMap(f));
                    }
                }
                map.put("fields", fieldsList);
                return map;
            } else if (entity instanceof FieldMeta fm) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", fm.getId());
                map.put("formCode", fm.getFormMeta() != null ? fm.getFormMeta().getFormCode() : null);
                map.put("fieldName", fm.getFieldName());
                map.put("fieldLabel", fm.getFieldLabel());
                map.put("componentType", fm.getComponentType());
                map.put("rowGroup", fm.getRowGroup());
                map.put("colOrder", fm.getColOrder());
                map.put("isRequired", fm.isRequired());
                map.put("isReadonly", fm.isReadonly());
                map.put("lovCode", fm.getLovCode());
                map.put("showInGrid", fm.isShowInGrid());
                map.put("hideInForm", fm.isHideInForm());
                map.put("isDetail", fm.isDetail());
                map.put("isSortable", fm.isSortable());
                map.put("formula", fm.getFormula());
                map.put("saveOnInsert", fm.isSaveOnInsert());
                map.put("saveOnUpdate", fm.isSaveOnUpdate());
                map.put("validationRule", fm.getValidationRule());
                map.put("displayFormat", fm.getDisplayFormat());
                map.put("sequenceCode", fm.getSequenceCode());
                map.put("isAuditLog", fm.isAuditLog());
                map.put("onAddScript", fm.getOnAddScript());
                return map;
            }
            String json = getObjectMapper().writeValueAsString(entity);
            return getObjectMapper().readValue(json,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            return Map.of("entity_string", entity.toString());
        }
    }

    @PostPersist
    public void onPostPersist(Object entity) {
        logToAudit(entity, "INSERT", null, entityToMap(entity));
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        logToAudit(entity, "UPDATE", null, entityToMap(entity));
    }

    @PreRemove
    public void onPreRemove(Object entity) {
        logToAudit(entity, "DELETE", entityToMap(entity), null);
    }

    private void logToAudit(Object entity, String actionType, Map<String, Object> oldData,
            Map<String, Object> newData) {
        try {
            DynamicDataService dds = getDynamicDataService();
            if (dds != null) {
                String tableName = getTableName(entity);
                String recordId = getRecordId(entity);
                dds.logAuditTrail(tableName, recordId, actionType, oldData, newData);

                if ("DELETE".equals(actionType)) {
                    String currentUser = dds.getCurrentLoggedUser();
                    if (entity instanceof FormMeta fm && fm.getFields() != null) {
                        for (FieldMeta f : fm.getFields()) {
                            Object fId = f.getId() != null ? f.getId()
                                    : (f.getFieldName() != null ? f.getFieldName() : "UNKNOWN");
                            dds.recordFieldAuditLog(fm.getFormCode(), "meta_field", fId, "DELETE",
                                    f.getFieldName() != null ? f.getFieldName() : "field", entityToMap(f), null,
                                    currentUser);
                        }
                    } else if (entity instanceof FieldMeta fm) {
                        Object fId = fm.getId() != null ? fm.getId()
                                : (fm.getFieldName() != null ? fm.getFieldName() : "UNKNOWN");
                        String formCode = fm.getFormMeta() != null ? fm.getFormMeta().getFormCode() : "FORM_BUILDER";
                        dds.recordFieldAuditLog(formCode, "meta_field", fId, "DELETE",
                                fm.getFieldName() != null ? fm.getFieldName() : "field", oldData, null, currentUser);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to record audit log for entity " + getTargetClass(entity).getSimpleName() + ": "
                    + e.getMessage());
        }
    }
}
