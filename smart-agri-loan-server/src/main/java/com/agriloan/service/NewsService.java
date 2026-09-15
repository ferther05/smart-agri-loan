package com.agriloan.service;

import com.agriloan.aop.OpLog;
import com.agriloan.common.BusinessException;
import com.agriloan.domain.News;
import com.agriloan.domain.NewsCategory;
import com.agriloan.dto.NewsDTO;
import com.agriloan.dto.NewsSaveRequest;
import com.agriloan.repository.NewsRepository;
import com.agriloan.support.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 惠农资讯服务：公开查询 + 审批人员维护（新增 / 修改 / 上下架）
 */
@Service
@RequiredArgsConstructor
public class NewsService {

    /** 与 News.content 的字段长度保持一致 */
    private static final int MAX_CONTENT_LENGTH = 10000;

    private final NewsRepository newsRepository;

    private final DtoMapper dtoMapper;

    public List<NewsDTO> list(String category) {
        List<News> list;
        if (category != null && !category.isBlank() && !"全部".equals(category)) {
            list = newsRepository.findByEnabledTrueAndCategoryOrderByDateDesc(parseCategory(category));
        } else {
            list = newsRepository.findByEnabledTrueOrderByDateDesc();
        }
        return list.stream().map(dtoMapper::toNewsDTO).toList();
    }

    public List<NewsDTO> latest() {
        return newsRepository.findTop3ByEnabledTrueOrderByDateDesc().stream()
                .map(dtoMapper::toNewsDTO).toList();
    }

    /** 阅读详情：浏览量 +1 */
    @Transactional
    public NewsDTO detail(String id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException("资讯不存在或已下架"));
        news.setViews(news.getViews() + 1);
        newsRepository.save(news);
        return dtoMapper.toNewsDTO(news);
    }

    private NewsCategory parseCategory(String category) {
        for (NewsCategory item : NewsCategory.values()) {
            if (item.getLabel().equals(category) || item.name().equalsIgnoreCase(category)) {
                return item;
            }
        }
        throw new BusinessException("未知资讯分类：" + category);
    }

    /* ==================== 管理端：新增 / 修改 ==================== */

    /** 管理端资讯列表：含已下架资讯，供后台维护 */
    @Transactional(readOnly = true)
    public List<NewsDTO> listAll() {
        return newsRepository.findAll().stream()
                .sorted(Comparator.comparing(News::getId))
                .map(dtoMapper::toNewsDTO)
                .toList();
    }

    /** 新增资讯：编号留空时按 N001 规则自动生成，发布日期留空取当天 */
    @OpLog(module = "资讯管理", action = "新增惠农资讯", target = "#request.title")
    @Transactional
    public NewsDTO create(NewsSaveRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BusinessException("请输入资讯标题");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new BusinessException("请选择资讯分类");
        }
        String id = request.getId() == null || request.getId().isBlank()
                ? nextId()
                : request.getId().trim();
        if (newsRepository.existsById(id)) {
            throw new BusinessException("资讯编号 " + id + " 已存在，请更换");
        }
        News news = new News();
        news.setId(id);
        news.setViews(0);
        fill(news, request);
        newsRepository.save(news);
        return dtoMapper.toNewsDTO(news);
    }

    /** 修改资讯：未传的字段保持原值，enabled=false 即下架 */
    @OpLog(module = "资讯管理", action = "修改惠农资讯", target = "#id")
    @Transactional
    public NewsDTO update(String id, NewsSaveRequest request) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException("资讯不存在：" + id));
        fill(news, request);
        newsRepository.save(news);
        return dtoMapper.toNewsDTO(news);
    }

    /** 请求参数 -> 实体（增量语义：未传的字段保持原值，文本字段传空串即清空） */
    private void fill(News news, NewsSaveRequest request) {
        if (request.getTitle() != null) {
            news.setTitle(request.getTitle());
        }
        if (request.getCategory() != null) {
            news.setCategory(parseCategory(request.getCategory().trim()));
        }
        if (request.getSource() != null) {
            news.setSource(trimToNull(request.getSource()));
        }
        if (request.getDate() != null && !request.getDate().isBlank()) {
            news.setDate(request.getDate().trim());
        }
        if (news.getDate() == null) {
            news.setDate(LocalDate.now().toString());
        }
        if (request.getSummary() != null) {
            news.setSummary(trimToNull(request.getSummary()));
        }
        if (request.getContent() != null) {
            news.setContent(joinParagraphs(request.getContent()));
        }
        if (request.getEnabled() != null) {
            news.setEnabled(request.getEnabled());
        }
    }

    /** 生成下一个资讯编号，如已存在 N001~N006 则返回 N007 */
    private String nextId() {
        int max = 0;
        for (News news : newsRepository.findAll()) {
            String id = news.getId();
            if (id == null || !id.regionMatches(true, 0, "N", 0, 1)) {
                continue;
            }
            try {
                max = Math.max(max, Integer.parseInt(id.substring(1)));
            } catch (NumberFormatException ignored) {
                // 非 N + 数字 形式的编号不参与自增
            }
        }
        return "N" + String.format("%03d", max + 1);
    }

    /** 正文段落归一化：落库时用 \n 连接，读取时再拆回数组 */
    private String joinParagraphs(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> paragraphs = new ArrayList<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            for (String item : value.split("\\r?\\n")) {
                String text = item.trim();
                if (!text.isEmpty()) {
                    paragraphs.add(text);
                }
            }
        }
        if (paragraphs.isEmpty()) {
            return null;
        }
        String content = String.join("\n", paragraphs);
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException("正文不能超过 " + MAX_CONTENT_LENGTH + " 字，当前 " + content.length() + " 字");
        }
        return content;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
