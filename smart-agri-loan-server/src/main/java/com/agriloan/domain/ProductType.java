package com.agriloan.domain;

import com.fasterxml.jackson.annotation.JsonValue;

/** 贷款产品类型 */
public enum ProductType {

    CREDIT("信用贷"),
    MORTGAGE("抵押贷"),
    POLICY("政策贷");

    private final String label;

    ProductType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
