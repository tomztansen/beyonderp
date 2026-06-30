package com.vaadinerp.components;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterCondition {
    private String filterId;
    private String filterColumn;
    private Object value;
    private String logicalOperator;
    private String comparisonOperator;
}
