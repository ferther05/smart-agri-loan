package com.agriloan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 惠农资讯新增 / 修改入参（审批人员专用）
 *
 * <p>content 段落数组，与查询接口返回结构一致；传单个字符串也会被当作一段处理。
 * 必填校验放在 {@code NewsService#create}，以便修改时只需提交要改的字段。</p>
 */
@Data
public class NewsSaveRequest {

    /** 资讯编号，如 N007；新增时留空自动生成，修改时忽略 */
    @Pattern(regexp = "^$|^[A-Za-z0-9_-]{2,20}$", message = "资讯编号需为 2-20 位字母、数字、下划线或短横线")
    private String id;

    @Size(max = 200, message = "资讯标题不能超过 200 字")
    private String title;

    /** 分类：政策解读 / 金融知识 / 行业动态（也接受 POLICY / KNOWLEDGE / INDUSTRY） */
    private String category;

    @Size(max = 50, message = "来源不能超过 50 字")
    private String source;

    /** 发布日期，如 2026-09-15；留空取当天 */
    @Size(max = 20, message = "发布日期不能超过 20 字")
    private String date;

    @Size(max = 500, message = "摘要不能超过 500 字")
    private String summary;

    /** 正文段落 */
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> content;

    /** 是否上架（false = 下架，前台不再展示） */
    private Boolean enabled;
}
