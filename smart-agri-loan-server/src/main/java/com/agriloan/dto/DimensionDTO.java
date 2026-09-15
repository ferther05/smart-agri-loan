package com.agriloan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 信用五维评分项 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimensionDTO {

    /** repay / business / asset / record / policy */
    private String key;

    /** 中文维度名 */
    private String label;

    /** 0 ~ 100 */
    private Integer value;
}
