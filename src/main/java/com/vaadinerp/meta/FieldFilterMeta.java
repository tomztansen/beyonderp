package com.vaadinerp.meta;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "meta_field_filter")
@Data
@ToString(exclude = "fieldMeta")
public class FieldFilterMeta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "field_id", nullable = false)
    private FieldMeta fieldMeta;

    @Column(name = "filter_column", length = 50, nullable = false)
    private String filterColumn;

    @Column(name = "source_type", length = 50, nullable = false)
    private String sourceType; // "FIELD", "QUERY", "STATIC"

    @Column(name = "source_name", length = 50, nullable = false)
    private String sourceName;
}
