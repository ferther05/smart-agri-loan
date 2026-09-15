package com.agriloan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 惠农资讯（含政策解读、金融知识、行业动态）
 */
@Getter
@Setter
@Entity
@Table(name = "agri_news")
public class News extends Auditable {

    /** 资讯编号，如 N001 */
    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsCategory category;

    @Column(length = 50)
    private String source;

    /** 发布日期，如 2026-08-28 */
    @Column(length = 20)
    private String date;

    @Column(nullable = false)
    private Integer views = 0;

    @Column(length = 500)
    private String summary;

    /**
     * 正文段落，使用 \n 分隔，前端按段落渲染
     *
     * <p>刻意不用 {@code @Lob}：它在 MySQL 下生成的字段类型装不下正文。
     * 用长度 10000 的字符串（utf8mb4 约 40KB，仍在单行 65535 字节限制内）；
     * 超长正文由 NewsService 提前拦下并给出提示。</p>
     */
    @Column(length = 10000)
    private String content;

    @Column(nullable = false)
    private Boolean enabled = true;
}
