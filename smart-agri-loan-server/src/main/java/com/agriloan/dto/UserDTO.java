package com.agriloan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户信息（不含密码）：前端导航、信用页、自适应界面均基于此渲染
 */
@Data
public class UserDTO {

    private Long id;

    private String username;

    private String name;

    /** 角色中文标签：农户 / 农业企业 / 审批人员 */
    private String role;

    /** 角色英文码，便于前端做权限判断 */
    private String roleCode;

    private String phone;

    private String idCard;

    private String address;

    private String business;

    private String scale;

    private Integer creditScore;

    /** AAA / AA / A / B / C */
    private String creditLevel;

    /** 信用评价文案，如"信用良好，可直接申请纯信用贷款" */
    private String creditComment;

    private BigDecimal balance;

    /** 五维评分，供雷达图渲染 */
    private List<DimensionDTO> dimensions;

    /** 当前等级可享受的权益（后端按等级下发，前端只负责展示） */
    private List<String> benefits;
}
