package com.agriloan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户新增 / 修改入参（审批人员专用）
 *
 * <p>新增：username、password、name、role 必填（由 {@code UserService#create} 校验）；
 * 修改：username 不可改，password 留空表示不修改密码，未填写的字段保持原值。</p>
 */
@Data
public class UserSaveRequest {

    /** 登录账号：仅新增时使用，修改时忽略 */
    @Pattern(regexp = "^$|^[a-zA-Z0-9_]{4,20}$", message = "账号需为 4-20 位字母、数字或下划线")
    private String username;

    /** 登录密码：新增必填；修改留空则不改密码 */
    @Pattern(regexp = "^$|^.{6,30}$", message = "密码长度需在 6-30 位之间")
    private String password;

    @Size(max = 100, message = "姓名不能超过 100 字")
    private String name;

    /** 角色：农户 / 农业企业 / 审批人员（也接受 FARMER / ENTERPRISE / BANK_ADMIN） */
    private String role;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$", message = "联系电话格式不正确")
    private String phone;

    @Size(max = 40, message = "证件号不能超过 40 字")
    private String idCard;

    @Size(max = 200, message = "地址不能超过 200 字")
    private String address;

    @Size(max = 100, message = "经营内容不能超过 100 字")
    private String business;

    @Size(max = 50, message = "经营规模不能超过 50 字")
    private String scale;

    /** 信用积分 350-950 */
    @Min(value = 350, message = "信用积分不能低于 350")
    @Max(value = 950, message = "信用积分不能高于 950")
    private Integer creditScore;

    /** 账户余额 */
    @DecimalMin(value = "0.0", message = "账户余额不能为负数")
    private BigDecimal balance;

    /** 五维评分 0-100 */
    @Min(value = 0, message = "履约能力评分范围 0-100")
    @Max(value = 100, message = "履约能力评分范围 0-100")
    private Integer repayScore;

    @Min(value = 0, message = "经营稳定评分范围 0-100")
    @Max(value = 100, message = "经营稳定评分范围 0-100")
    private Integer businessScore;

    @Min(value = 0, message = "资产状况评分范围 0-100")
    @Max(value = 100, message = "资产状况评分范围 0-100")
    private Integer assetScore;

    @Min(value = 0, message = "信用记录评分范围 0-100")
    @Max(value = 100, message = "信用记录评分范围 0-100")
    private Integer recordScore;

    @Min(value = 0, message = "政策匹配评分范围 0-100")
    @Max(value = 100, message = "政策匹配评分范围 0-100")
    private Integer policyScore;

    /** 是否启用（false = 停用，停用后无法登录） */
    private Boolean enabled;
}
