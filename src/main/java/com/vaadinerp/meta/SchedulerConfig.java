package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "meta_scheduler_config", schema = "public")
@Getter
@Setter
public class SchedulerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "form_code", length = 50, nullable = false, unique = true)
    private String formCode;

    @Column(name = "scheduler_query", columnDefinition = "TEXT")
    private String schedulerQuery;

    @Column(name = "col_resource", length = 100)
    private String colResource;

    @Column(name = "col_resource_group", length = 100)
    private String colResourceGroup;

    @Column(name = "col_task_name", length = 100)
    private String colTaskName;

    @Column(name = "col_start_date", length = 100)
    private String colStartDate;

    @Column(name = "col_end_date", length = 100)
    private String colEndDate;

    @Column(name = "col_qty", length = 100)
    private String colQty;

    @Column(name = "col_max_capacity", length = 100)
    private String colMaxCapacity;

    @Column(name = "col_group_id", length = 100)
    private String colGroupId;

    @Column(name = "col_split_group", length = 100)
    private String colSplitGroup;

    @Column(name = "col_dependency_id", length = 100)
    private String colDependencyId;

    @Column(name = "col_sequence", length = 100)
    private String colSequence;

    @Column(name = "col_lead_day", length = 100)
    private String colLeadDay;

    @Column(name = "col_weight", length = 100)
    private String colWeight;

    @Column(name = "col_max_capacity_weight", length = 100)
    private String colMaxCapacityWeight;

    @Column(name = "col_primary_key", length = 100)
    private String colPrimaryKey;

    @Column(name = "update_table", length = 100)
    private String updateTable;

    @Column(name = "update_date_column", length = 100)
    private String updateDateColumn;

    @Column(name = "default_capacity_mode", length = 20)
    private String defaultCapacityMode = "QTYBOX";

    @Column(name = "on_drag_script", columnDefinition = "TEXT")
    private String onDragScript;
}
