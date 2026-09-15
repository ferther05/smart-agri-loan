package com.agriloan.dto;

import lombok.Data;

import java.util.List;

/** 惠农资讯（content 已按段落拆分为数组，前端直接 v-for 渲染） */
@Data
public class NewsDTO {

    private String id;

    private String title;

    private String category;

    private String source;

    private String date;

    private Integer views;

    private String summary;

    private List<String> content;

    /** 是否上架（false = 已下架；管理端列表据此区分） */
    private Boolean enabled;
}
