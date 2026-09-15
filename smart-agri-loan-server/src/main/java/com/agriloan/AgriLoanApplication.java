package com.agriloan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 智能惠农信贷系统 —— 后端启动类
 *
 * <p>技术栈：Spring Boot 3.2 + Spring Security + JWT + Spring Data JPA + AOP</p>
 *
 * <p>启动流程：连接 MySQL（库需预先存在，见 db/schema.sql）→ JPA 补建表结构
 * → 首次灌入演示数据 → 提供服务。</p>
 */
@EnableJpaAuditing
@SpringBootApplication
public class AgriLoanApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgriLoanApplication.class, args);
    }
}
