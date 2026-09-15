package com.agriloan.domain;

import com.fasterxml.jackson.annotation.JsonValue;

/** 惠农资讯分类 */
public enum NewsCategory {

    POLICY("政策解读"),
    KNOWLEDGE("金融知识"),
    INDUSTRY("行业动态");

    private final String label;

    NewsCategory(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
