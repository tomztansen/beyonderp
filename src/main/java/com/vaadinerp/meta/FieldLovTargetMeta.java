package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "meta_field_lov_target")
@Data
@ToString(exclude = "fieldMeta")
public class FieldLovTargetMeta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "field_id", nullable = false)
    private FieldMeta fieldMeta;

    @Column(name = "source_column", length = 50, nullable = false)
    private String sourceColumn;

    @Column(name = "target_field", length = 50, nullable = false)
    private String targetField;

    @Column(name = "action_type", length = 50)
    private String actionType = "COPY";

    @Column(name = "lookup_column", length = 50)
    private String lookupColumn;
}
