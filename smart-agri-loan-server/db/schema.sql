-- =====================================================================
--  智能惠农信贷系统 —— MySQL 建库建表脚本
--  使用方式：
--    1) mysql -uroot -p < db/schema.sql      -- 建库建表
--    2) mvn spring-boot:run                   -- 启动后端
--  说明：
--        ① 库必须预先存在：服务不再自动建库，库不存在会直接启动失败；
--        ② 表结构由 JPA（ddl-auto=update）自动补建 / 补字段，
--           本脚本用于首次建库，或需要重建表结构时执行；
--        ③ 业务数据（产品/资讯/账号/申请单）由 DataInitializer 仅在表为空时灌入。
--  注意：本脚本会 DROP 并重建这 7 张表，重复执行等于清空业务数据。
-- =====================================================================

-- 允许脚本反复执行：先关外键检查，避免删除被引用的父表时报 1217
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS smart_agri_loan
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE smart_agri_loan;

-- ---------------------------------------------------------------------
-- 1. 用户表：农户 / 农业企业 / 审批人员
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username       VARCHAR(50)  NOT NULL COMMENT '登录账号',
  password       VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  name           VARCHAR(100) NOT NULL COMMENT '姓名/主体名称',
  role           VARCHAR(20)  NOT NULL COMMENT 'FARMER/ENTERPRISE/BANK_ADMIN',
  phone          VARCHAR(30)           COMMENT '联系电话',
  id_card        VARCHAR(40)           COMMENT '身份证/统一社会信用代码',
  address        VARCHAR(200)          COMMENT '地址',
  business       VARCHAR(100)          COMMENT '经营内容',
  scale          VARCHAR(50)           COMMENT '经营规模',
  credit_score   INT          NOT NULL DEFAULT 700 COMMENT '信用积分 350-950',
  balance        DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  repay_score    INT          NOT NULL DEFAULT 60 COMMENT '履约能力',
  business_score INT          NOT NULL DEFAULT 60 COMMENT '经营稳定',
  asset_score    INT          NOT NULL DEFAULT 60 COMMENT '资产状况',
  record_score   INT          NOT NULL DEFAULT 60 COMMENT '信用记录',
  policy_score   INT          NOT NULL DEFAULT 60 COMMENT '政策匹配',
  enabled        BIT(1)       NOT NULL DEFAULT b'1' COMMENT '是否启用',
  created_at     DATETIME              COMMENT '创建时间',
  updated_at     DATETIME              COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ---------------------------------------------------------------------
-- 2. 贷款产品表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS loan_product;
CREATE TABLE loan_product (
  id               VARCHAR(20)  NOT NULL COMMENT '产品编号 P001',
  name             VARCHAR(100) NOT NULL COMMENT '产品名称',
  type             VARCHAR(20)  NOT NULL COMMENT 'CREDIT/MORTGAGE/POLICY',
  badge            VARCHAR(20)           COMMENT '角标',
  icon_text        VARCHAR(4)            COMMENT '图标文字',
  rate             DOUBLE       NOT NULL COMMENT '年化利率 %',
  term_months      INT          NOT NULL COMMENT '主推期限（月）',
  terms            VARCHAR(50)           COMMENT '可选期限 6,12,18',
  min_amount       BIGINT       NOT NULL COMMENT '起贷金额',
  max_amount       BIGINT       NOT NULL COMMENT '最高额度',
  repayment        VARCHAR(50)           COMMENT '还款方式',
  guarantee        VARCHAR(100)          COMMENT '担保方式',
  target           VARCHAR(200)          COMMENT '适用对象',
  features         VARCHAR(300)          COMMENT '产品亮点（分号分隔）',
  apply_count      INT          NOT NULL DEFAULT 0 COMMENT '累计申请次数',
  hot              BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否热门',
  min_credit_level VARCHAR(4)   NOT NULL DEFAULT 'C' COMMENT '准入门槛：最低信用等级',
  enabled          BIT(1)       NOT NULL DEFAULT b'1' COMMENT '是否在售',
  created_at       DATETIME,
  updated_at       DATETIME,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贷款产品';

-- ---------------------------------------------------------------------
-- 3. 惠农资讯表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS agri_news;
CREATE TABLE agri_news (
  id         VARCHAR(20)  NOT NULL COMMENT '资讯编号 N001',
  title      VARCHAR(200) NOT NULL COMMENT '标题',
  category   VARCHAR(20)  NOT NULL COMMENT 'POLICY/KNOWLEDGE/INDUSTRY',
  source     VARCHAR(50)           COMMENT '来源',
  date       VARCHAR(20)           COMMENT '发布日期',
  views      INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
  summary    VARCHAR(500)          COMMENT '摘要',
  content    VARCHAR(10000)        COMMENT '正文（\\n 分段）',
  enabled    BIT(1)       NOT NULL DEFAULT b'1' COMMENT '是否上架',
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='惠农资讯';

-- ---------------------------------------------------------------------
-- 4. 贷款申请单表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS loan_application;
CREATE TABLE loan_application (
  id             VARCHAR(30)   NOT NULL COMMENT '申请编号 SQ...',
  product_id     VARCHAR(20)   NOT NULL COMMENT '产品编号',
  product_name   VARCHAR(100)  NOT NULL COMMENT '产品名称快照',
  applicant_id   BIGINT        NOT NULL COMMENT '申请人',
  applicant_name VARCHAR(100)  NOT NULL COMMENT '申请人姓名',
  phone          VARCHAR(30)            COMMENT '联系电话',
  id_card        VARCHAR(40)            COMMENT '脱敏身份证号',
  amount         DECIMAL(18,2) NOT NULL COMMENT '申请金额',
  term           INT           NOT NULL COMMENT '期限（月）',
  purpose        VARCHAR(300)           COMMENT '贷款用途',
  repay_source   VARCHAR(300)           COMMENT '还款来源',
  address        VARCHAR(200)           COMMENT '地址',
  business       VARCHAR(50)            COMMENT '经营类型',
  scale          VARCHAR(50)            COMMENT '经营规模',
  credit_score   INT           NOT NULL DEFAULT 0 COMMENT '申请时信用积分快照',
  status         VARCHAR(20)   NOT NULL COMMENT '待初审/初审通过/已签约/已放款/已结清/已拒绝',
  rate           DOUBLE        NOT NULL DEFAULT 0 COMMENT '利率快照',
  apply_time     DATETIME               COMMENT '申请时间',
  update_time    DATETIME               COMMENT '状态更新时间',
  audit_time     DATETIME               COMMENT '审批时间',
  audit_opinion  VARCHAR(300)           COMMENT '审批意见',
  auditor        VARCHAR(50)            COMMENT '审批人',
  signed_time    DATETIME               COMMENT '签约时间',
  loan_time      DATETIME               COMMENT '放款时间',
  settle_time    DATETIME               COMMENT '结清时间',
  repay_amount   DECIMAL(18,2)          COMMENT '结清本息合计',
  created_at     DATETIME,
  updated_at     DATETIME,
  PRIMARY KEY (id),
  KEY idx_apply_applicant (applicant_id),
  KEY idx_apply_status (status),
  CONSTRAINT fk_apply_product FOREIGN KEY (product_id) REFERENCES loan_product (id),
  CONSTRAINT fk_apply_user FOREIGN KEY (applicant_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贷款申请单';

-- ---------------------------------------------------------------------
-- 5. 申请单流转日志
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS loan_application_log;
CREATE TABLE loan_application_log (
  id             BIGINT      NOT NULL AUTO_INCREMENT,
  application_id VARCHAR(30) NOT NULL COMMENT '申请编号',
  time           DATETIME             COMMENT '发生时间',
  action         VARCHAR(50)          COMMENT '动作：提交申请/初审通过/签署合同/放款成功/贷款结清',
  operator       VARCHAR(50)          COMMENT '操作人',
  note           VARCHAR(300)         COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_log_application (application_id),
  CONSTRAINT fk_log_application FOREIGN KEY (application_id) REFERENCES loan_application (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请单流转日志';

-- ---------------------------------------------------------------------
-- 6. 信用积分变动日志
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS credit_log;
CREATE TABLE credit_log (
  id         VARCHAR(40) NOT NULL COMMENT '日志编号',
  user_id    BIGINT      NOT NULL COMMENT '用户',
  date       VARCHAR(20)          COMMENT '变动日期',
  event      VARCHAR(200)         COMMENT '事件说明',
  delta      INT         NOT NULL DEFAULT 0 COMMENT '变动分值',
  score      INT         NOT NULL DEFAULT 0 COMMENT '变动后积分',
  type       VARCHAR(20)          COMMENT 'repay/business/asset/record/policy',
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id),
  KEY idx_credit_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用积分变动日志';

-- ---------------------------------------------------------------------
-- 7. AOP 操作日志表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  username     VARCHAR(50)           COMMENT '操作账号',
  role         VARCHAR(20)           COMMENT '操作角色',
  module       VARCHAR(50)           COMMENT '业务模块',
  action       VARCHAR(100)          COMMENT '操作描述',
  target       VARCHAR(100)          COMMENT '目标对象',
  method       VARCHAR(200)          COMMENT '方法签名',
  params       VARCHAR(1000)         COMMENT '入参摘要（密码已脱敏）',
  ip           VARCHAR(50)           COMMENT '来源 IP',
  success      BIT(1)       NOT NULL DEFAULT b'1' COMMENT '是否成功',
  error_msg    VARCHAR(500)          COMMENT '异常信息',
  cost_ms      BIGINT                COMMENT '耗时（毫秒）',
  operate_time DATETIME              COMMENT '操作时间',
  PRIMARY KEY (id),
  KEY idx_oplog_username (username),
  KEY idx_oplog_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AOP 操作日志';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
