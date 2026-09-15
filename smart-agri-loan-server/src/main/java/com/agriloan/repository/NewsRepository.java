package com.agriloan.repository;

import com.agriloan.domain.News;
import com.agriloan.domain.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, String> {

    List<News> findByEnabledTrueOrderByDateDesc();

    List<News> findByEnabledTrueAndCategoryOrderByDateDesc(NewsCategory category);

    List<News> findTop3ByEnabledTrueOrderByDateDesc();
}
