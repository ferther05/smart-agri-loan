package com.agriloan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 贷款产品新增 / 修改入参（审批人员专用）
 *
 * <p>terms 与 features 兼容两种写法：数组 ["6","12"] 或整串 ["6,12,18"]，服务端统一归一化。</p>
 *
 * <p>这里不加 {@code @NotNull / @NotBlank}：那会让"只改一个字段"的修改请求被 400 拦下。
 * 新增时的必填校验由 {@code ProductService#create} 负责，修改时未传的字段一律保持原值。</p>
 */
@Data
public class ProductSaveRequest {

    /** 产品编号，如 P007；新增时留空自动生成，修改时忽略 */
    @Pattern(regexp = "^$|^[A-Za-z0-9_-]{2,20}$", message = "产品编号需为 2-20 位字母、数字、下划线或短横线")
    private String id;

    @Size(max = 100, message = "产品名称不能超过 100 字")
    private String name;

    /** 产品类型：信用贷 / 抵押贷 / 政策贷（也接受 CREDIT / MORTGAGE / POLICY） */
    private String type;

    /** 角标文案，如"纯信用""政策贴息" */
    @Size(max = 20, message = "角标不能超过 20 字")
    private String badge;

    /** 卡片图标文字，如"春" */
    @Size(max = 4, message = "图标文字不能超过 4 字")
    private String iconText;

    /** 年化利率（%） */
    @DecimalMin(value = "0.0", message = "年化利率不能为负数")
    private Double rate;

    /** 主推期限（月） */
    @Min(value = 1, message = "主推期限至少 1 个月")
    private Integer termMonths;

    /** 可选期限，如 ["6","12","18"] */
    private List<String> terms;

    @Min(value = 0, message = "起贷金额不能为负数")
    private Long minAmount;

    @Min(value = 1, message = "最高额度必须大于 0")
    private Long maxAmount;

    @Size(max = 50, message = "还款方式不能超过 50 字")
    private String repayment;

    @Size(max = 100, message = "担保方式不能超过 100 字")
    private String guarantee;

    @Size(max = 200, message = "适用对象不能超过 200 字")
    private String target;

    /** 产品亮点，如 ["线上申请 3 分钟","随借随还"] */
    private List<String> features;

    /** 是否热门 */
    private Boolean hot;

    /** 准入门槛：AAA / AA / A / B / C */
    @Pattern(regexp = "^$|^(AAA|AA|A|B|C)$", message = "准入门槛只能是 AAA、AA、A、B、C")
    private String minCreditLevel;

    /** 是否在售（false = 下架，前台不再展示） */
    private Boolean enabled;
}
