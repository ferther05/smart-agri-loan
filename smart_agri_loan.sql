/*
 Navicat MySQL Dump SQL

 Source Server         : psc
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : smart_agri_loan

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 15/09/2026 19:56:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agri_news
-- ----------------------------
DROP TABLE IF EXISTS `agri_news`;
CREATE TABLE `agri_news`  (
  `enabled` bit(1) NOT NULL,
  `views` int NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `content` varchar(10000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `category` enum('POLICY','KNOWLEDGE','INDUSTRY') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_news
-- ----------------------------
INSERT INTO `agri_news` VALUES (b'1', 12680, '2026-09-15 18:36:32.538416', '2026-09-15 18:36:32.538416', '2026-08-28', 'N001', '农业农村部', '中央一号文件：持续加大乡村振兴金融支持力度', '文件明确要求金融机构单列涉农信贷计划，保持涉农贷款余额持续增长，鼓励开发专属金融产品支持粮油种植与畜牧水产。', '文件提出，要健全乡村振兴多元投入机制，坚持将农业农村作为一般公共预算优先保障领域，创新乡村振兴投融资机制。\n在信贷支持方面，要求金融机构单列涉农信贷计划，努力实现涉农贷款余额持续增长，并鼓励各地结合实际扩大完全成本保险和种植收入保险政策实施范围。\n文件同时强调，要深化农村信用社改革，坚持县域法人总体稳定，稳妥有序推进村镇银行改革重组，提升涉农金融机构服务能力。', 'POLICY');
INSERT INTO `agri_news` VALUES (b'1', 8342, '2026-09-15 18:36:32.540487', '2026-09-15 18:36:32.540487', '2026-08-21', 'N002', '省农业农村厅', '2026 年农业生产社会化服务补助资金申报开始', '补助资金重点支持粮油等关键薄弱环节的社会化服务，符合条件的合作社、家庭农场可在线提交申报材料。', '补助资金重点支持深耕深松、统防统治、秸秆还田等粮油生产关键薄弱环节的社会化服务，优先支持小农户接受服务。\n申报主体需具备两年以上服务经验、拥有相应农业机械装备，并提供服务合同、作业记录等佐证材料。\n县级农业农村部门将在 15 个工作日内完成初审，市级复核后统一公示，公示无异议即可拨付资金。', 'POLICY');
INSERT INTO `agri_news` VALUES (b'1', 23105, '2026-09-15 18:36:32.541489', '2026-09-15 18:36:32.541489', '2026-08-15', 'N003', '平台金融课堂', '农户必读：个人征信报告怎么看、怎么养', '征信报告是贷款审批的重要依据。本文教你读懂逾期记录、查询次数与负债率三个关键指标，并给出养护建议。', '征信报告核心看三处：一是信贷记录中的逾期情况，连三累六（连续 3 个月或累计 6 次逾期）会显著影响审批；二是查询记录，近 30 天硬查询超过 3 次会被判定为资金紧张；三是负债率，超过月收入的 50% 将影响额度。\n养护建议：按时足额还款是最核心的一条；谨慎点击各类测额度链接，避免产生硬查询；适度使用信用卡并保持稳定账单，有助于积累正向记录。\n若发现报告有误，可向金融机构或人民银行征信中心提出异议申请，一般 20 日内可完成核查处理。', 'KNOWLEDGE');
INSERT INTO `agri_news` VALUES (b'1', 18573, '2026-09-15 18:36:32.541489', '2026-09-15 18:36:32.541489', '2026-08-09', 'N004', '平台风控中心', '警惕三类涉农贷款诈骗，守住钱袋子', '免抵押秒放款、内部渠道提额、缴纳保证金解冻额度均为典型诈骗话术，请通过官方渠道办理贷款。', '第一类：以免抵押、免征信、秒放款为噱头吸引提交资料，随后收取手续费、保证金后失联。\n第二类：冒充银行工作人员，以内部渠道提额、注销校园贷账户为由诱导转账或提供验证码。\n第三类：发送带链接的贷款获批短信，诱导下载仿冒 App 填写银行卡信息。\n防范要点：正规贷款放款前不收取任何费用；本平台不会以私人账户收款；请勿向任何人透露短信验证码。', 'KNOWLEDGE');
INSERT INTO `agri_news` VALUES (b'1', 6421, '2026-09-15 18:36:32.542495', '2026-09-15 18:36:32.542495', '2026-08-02', 'N005', '农产品市场周报', '夏粮收购收官，优质小麦价格稳中有升', '主产区夏粮收购接近尾声，优质专用小麦收购均价同比上涨 4.2%，种植主体收益稳步提升。', '据统计，今年夏粮收购总量同比略增，优质专用小麦因需求旺盛，收购均价较去年同期上涨约 4.2%。\n受此影响，规模化种植主体现金流状况良好，秋播农资采购需求提前释放，相关信贷需求同步上升。\n分析师提示，需关注化肥等农资价格波动对秋冬种成本的影响，建议种植主体提前锁定采购资金。', 'INDUSTRY');
INSERT INTO `agri_news` VALUES (b'1', 5390, '2026-09-15 18:36:32.543501', '2026-09-15 18:36:32.543501', '2026-07-26', 'N006', '农村金融观察', '数字金融下沉：整村授信覆盖行政村超 12 万个', '整村授信模式通过村级评议 + 大数据评分，让无抵押农户获得基础授信额度，线上即可支用。', '整村授信以行政村为单位，由村两委、驻村金融助理共同开展农户信息采集与信用评议，形成白名单。\n评议结果与大数据风控模型结合，为农户生成基础授信额度，农户通过手机即可随借随还。\n目前该模式已覆盖全国超过 12 万个行政村，户均授信额度约 8 万元，平均审批时长缩短至 1.5 天。', 'INDUSTRY');

-- ----------------------------
-- Table structure for credit_log
-- ----------------------------
DROP TABLE IF EXISTS `credit_log`;
CREATE TABLE `credit_log`  (
  `delta` int NOT NULL,
  `score` int NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `id` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `event` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_credit_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of credit_log
-- ----------------------------
INSERT INTO `credit_log` VALUES (60, 716, '2026-09-15 18:36:32.844905', '2026-09-15 18:36:32.844905', 1, '2026-01-09', 'record', 'C001', '首次建立涉农信用档案');
INSERT INTO `credit_log` VALUES (15, 731, '2026-09-15 18:36:32.843808', '2026-09-15 18:36:32.843808', 1, '2026-03-11', 'repay', 'C002', '上年度贷款提前结清');
INSERT INTO `credit_log` VALUES (6, 737, '2026-09-15 18:36:32.843808', '2026-09-15 18:36:32.843808', 1, '2026-04-26', 'business', 'C003', '购买农业保险（茶叶气象指数险）');
INSERT INTO `credit_log` VALUES (12, 749, '2026-09-15 18:36:32.842806', '2026-09-15 18:36:32.842806', 1, '2026-06-02', 'policy', 'C004', '纳入整村授信白名单');
INSERT INTO `credit_log` VALUES (5, 754, '2026-09-15 18:36:32.842806', '2026-09-15 18:36:32.842806', 1, '2026-07-18', 'asset', 'C005', '完成土地确权信息补录');
INSERT INTO `credit_log` VALUES (8, 762, '2026-09-15 18:36:32.839753', '2026-09-15 18:36:32.839753', 1, '2026-09-01', 'repay', 'C006', '春耕贷按时还款 3 期');
INSERT INTO `credit_log` VALUES (660, 660, '2026-09-15 18:55:16.925189', '2026-09-15 18:55:16.925189', 4, '2026-09-15', 'record', 'Ce727de0aee3a', '首次建立涉农信用档案');
INSERT INTO `credit_log` VALUES (80, 795, '2026-09-15 18:36:32.845910', '2026-09-15 18:36:32.845910', 2, '2026-01-05', 'record', 'D001', '首次建立新型经营主体信用档案');
INSERT INTO `credit_log` VALUES (10, 805, '2026-09-15 18:36:32.844905', '2026-09-15 18:36:32.844905', 2, '2026-08-20', 'repay', 'D002', '合作社贷款按时结清');

-- ----------------------------
-- Table structure for loan_application
-- ----------------------------
DROP TABLE IF EXISTS `loan_application`;
CREATE TABLE `loan_application`  (
  `amount` decimal(18, 2) NOT NULL,
  `credit_score` int NOT NULL,
  `rate` double NOT NULL,
  `repay_amount` decimal(18, 2) NULL DEFAULT NULL,
  `term` int NOT NULL,
  `applicant_id` bigint NOT NULL,
  `apply_time` datetime(6) NULL DEFAULT NULL,
  `audit_time` datetime(6) NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `loan_time` datetime(6) NULL DEFAULT NULL,
  `settle_time` datetime(6) NULL DEFAULT NULL,
  `signed_time` datetime(6) NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `product_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `id_card` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `auditor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `business` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `scale` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `applicant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `audit_opinion` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `purpose` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `repay_source` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` enum('PENDING_FIRST','FIRST_PASS','SIGNED','LOANED','SETTLED','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_apply_applicant`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_apply_status`(`status` ASC) USING BTREE,
  INDEX `FKeyryxaov7qn04ctajae14l9sf`(`product_id` ASC) USING BTREE,
  CONSTRAINT `FKeyryxaov7qn04ctajae14l9sf` FOREIGN KEY (`product_id`) REFERENCES `loan_product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKhvk7syl9d4v7t8vh44ig2ukim` FOREIGN KEY (`applicant_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of loan_application
-- ----------------------------
INSERT INTO `loan_application` VALUES (20000.00, 716, 4.35, 20435.00, 6, 1, '2026-01-10 08:42:00.000000', '2026-01-11 10:15:00.000000', '2026-09-15 18:36:32.781215', '2026-01-13 09:00:00.000000', '2026-07-10 09:30:00.000000', '2026-01-12 11:20:00.000000', '2026-07-10 09:30:00.000000', '2026-09-15 18:36:32.781215', 'P004', 'SQ20260110001', '13880666721', '522132**********2317', '李慧·风控审批员', '茶叶种植与初加工', '32 亩', '张大山', '农户小额信用贷', '贵州省遵义市湄潭县永兴镇', '信用良好，同意发放', '春耕农资集中采购周转', '经营收入与农产品销售回款', 'SETTLED');
INSERT INTO `loan_application` VALUES (60000.00, 737, 3.65, 62190.00, 12, 1, '2026-03-12 09:20:00.000000', '2026-03-13 15:40:00.000000', '2026-09-15 18:36:32.795557', '2026-03-16 09:28:00.000000', '2026-08-18 11:05:00.000000', '2026-03-14 10:02:00.000000', '2026-08-18 11:05:00.000000', '2026-09-15 18:36:32.795557', 'P001', 'SQ20260312002', '13880666721', '522132**********2317', '李慧·风控审批员', '茶叶种植与初加工', '32 亩', '张大山', '惠农e贷·春耕贷', '贵州省遵义市湄潭县永兴镇', '经营流水充足，同意发放', '春季茶园追肥与管护人工费', '经营收入与农产品销售回款', 'SETTLED');
INSERT INTO `loan_application` VALUES (150000.00, 805, 3.65, 155475.00, 12, 2, '2026-04-15 10:08:00.000000', '2026-04-16 09:52:00.000000', '2026-09-15 18:36:32.821016', '2026-04-20 10:15:00.000000', '2026-08-20 10:40:00.000000', '2026-04-18 14:30:00.000000', '2026-08-20 10:40:00.000000', '2026-09-15 18:36:32.821016', 'P001', 'SQ20260415003', '085128769988', '915203**********2345', '李慧·风控审批员', '茶叶统购统销', '社员 126 户', '湄潭雲雾茶业专业合作社', '惠农e贷·春耕贷', '贵州省遵义市湄潭县湄江街道', '合作社主体信用良好，同意发放', '春茶鲜叶统购资金周转', '经营收入与农产品销售回款', 'SETTLED');
INSERT INTO `loan_application` VALUES (260000.00, 805, 3.85, 0.00, 36, 2, '2026-05-20 09:15:00.000000', '2026-05-24 11:26:00.000000', '2026-09-15 18:36:32.826098', '2026-06-10 09:40:00.000000', NULL, '2026-06-01 15:03:00.000000', '2026-06-10 09:40:00.000000', '2026-09-15 18:36:32.826098', 'P002', 'SQ20260610005', '085128769988', '915203**********2345', '李慧·风控审批员', '茶叶统购统销', '社员 126 户', '湄潭雲雾茶业专业合作社', '农机购置专项贷', '贵州省遵义市湄潭县湄江街道', '设备购置凭证齐全，同意发放', '购置茶叶加工生产线与冷链转运车', '经营收入与农产品销售回款', 'LOANED');
INSERT INTO `loan_application` VALUES (150000.00, 762, 3.85, 0.00, 24, 1, '2026-06-02 10:21:00.000000', '2026-06-04 16:00:00.000000', '2026-09-15 18:36:32.802216', '2026-06-15 09:12:00.000000', NULL, '2026-06-06 11:08:00.000000', '2026-06-15 09:12:00.000000', '2026-09-15 18:36:32.802216', 'P002', 'SQ20260615004', '13880666721', '522132**********2317', '李慧·风控审批员', '茶叶种植与初加工', '32 亩', '张大山', '农机购置专项贷', '贵州省遵义市湄潭县永兴镇', '购机补贴权益质押完整，同意发放', '购置履带式旋耕机与植保无人机', '经营收入与农产品销售回款', 'LOANED');
INSERT INTO `loan_application` VALUES (400000.00, 805, 4.15, 0.00, 60, 2, '2026-07-19 10:30:00.000000', '2026-07-22 10:30:00.000000', '2026-09-15 18:36:32.830517', NULL, NULL, NULL, '2026-07-22 10:30:00.000000', '2026-09-15 18:36:32.830517', 'P003', 'SQ20260721007', '085128769988', '915203**********2345', '李慧·风控审批员', '茶叶统购统销', '社员 126 户', '湄潭雲雾茶业专业合作社', '乡村振兴产业贷', '贵州省遵义市湄潭县湄江街道', '政银担分险方案落地，同意初审通过', '茶园基地提质改造与区域品牌建设', '经营收入与农产品销售回款', 'FIRST_PASS');
INSERT INTO `loan_application` VALUES (100000.00, 762, 3.65, 0.00, 12, 1, '2026-07-18 09:30:00.000000', '2026-07-20 15:18:00.000000', '2026-09-15 18:36:32.807926', NULL, NULL, '2026-07-26 14:20:00.000000', '2026-07-26 14:20:00.000000', '2026-09-15 18:36:32.807926', 'P001', 'SQ20260726005', '13880666721', '522132**********2317', '李慧·风控审批员', '茶叶种植与初加工', '32 亩', '张大山', '惠农e贷·春耕贷', '贵州省遵义市湄潭县永兴镇', '用途合规、还款来源明确，同意发放', '秋季茶园管护与采工工资周转', '经营收入与农产品销售回款', 'SIGNED');
INSERT INTO `loan_application` VALUES (300000.00, 805, 3.95, 0.00, 36, 2, '2026-07-28 08:45:00.000000', '2026-08-01 10:12:00.000000', '2026-09-15 18:36:32.833697', NULL, NULL, '2026-08-05 16:22:00.000000', '2026-08-05 16:22:00.000000', '2026-09-15 18:36:32.833697', 'P005', 'SQ20260805009', '085128769988', '915203**********2345', '李慧·风控审批员', '茶叶统购统销', '社员 126 户', '湄潭雲雾茶业专业合作社', '冷链仓储建设贷', '贵州省遵义市湄潭县湄江街道', '仓单质押方案可行，同意发放', '茶叶冷链仓储设施扩建工程', '经营收入与农产品销售回款', 'SIGNED');
INSERT INTO `loan_application` VALUES (60000.00, 762, 3.55, 0.00, 18, 1, '2026-08-10 14:12:00.000000', '2026-08-12 15:06:00.000000', '2026-09-15 18:36:32.813005', NULL, NULL, NULL, '2026-08-12 15:06:00.000000', '2026-09-15 18:36:32.813005', 'P006', 'SQ20260812006', '13880666721', '522132**********2317', '李慧·风控审批员', '茶叶种植与初加工', '32 亩', '张大山', '特色养殖扶持贷', '贵州省遵义市湄潭县永兴镇', '配套保险保单齐备，同意初审通过', '茶园套养生态鸡舍扩建', '经营收入与农产品销售回款', 'FIRST_PASS');
INSERT INTO `loan_application` VALUES (30000.00, 762, 4.35, 0.00, 6, 1, '2026-08-28 10:02:00.000000', '2026-08-31 09:41:00.000000', '2026-09-15 18:36:32.815489', NULL, NULL, NULL, '2026-08-31 09:41:00.000000', '2026-09-15 18:36:32.815489', 'P004', 'SQ20260830007', '13880666721', '522132**********2317', '李慧·风控审批员', '茶叶种植与初加工', '32 亩', '张大山', '农户小额信用贷', '贵州省遵义市湄潭县永兴镇', '近 90 天征信查询次数偏多，暂不符合当前准入条件，建议 3 个月后重新申请', '茶园防霜冻设施采购', '经营收入与农产品销售回款', 'REJECTED');
INSERT INTO `loan_application` VALUES (80000.00, 762, 3.65, 0.00, 12, 1, '2026-09-01 09:12:00.000000', NULL, '2026-09-15 18:36:32.819008', NULL, NULL, NULL, '2026-09-01 09:12:00.000000', '2026-09-15 18:36:32.819008', 'P001', 'SQ20260901001', '13880666721', '522132**********2317', NULL, '茶叶种植与初加工', '32 亩', '张大山', '惠农e贷·春耕贷', '贵州省遵义市湄潭县永兴镇', NULL, '购买春耕化肥、茶苗及支付管护人工费', '经营收入与农产品销售回款', 'PENDING_FIRST');
INSERT INTO `loan_application` VALUES (260000.00, 805, 3.55, 0.00, 18, 2, '2026-09-05 10:08:00.000000', NULL, '2026-09-15 18:36:32.836718', NULL, NULL, NULL, '2026-09-05 10:08:00.000000', '2026-09-15 18:36:32.836718', 'P006', 'SQ20260905003', '085128769988', '915203**********2345', NULL, '茶叶统购统销', '社员 126 户', '湄潭雲雾茶业专业合作社', '特色养殖扶持贷', '贵州省遵义市湄潭县湄江街道', NULL, '茶园配套生态养鸡场扩建', '经营收入与农产品销售回款', 'PENDING_FIRST');

-- ----------------------------
-- Table structure for loan_application_log
-- ----------------------------
DROP TABLE IF EXISTS `loan_application_log`;
CREATE TABLE `loan_application_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `time` datetime(6) NULL DEFAULT NULL,
  `application_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `note` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKi7oocgkdfo6as0171syeb5c2d`(`application_id` ASC) USING BTREE,
  CONSTRAINT `FKi7oocgkdfo6as0171syeb5c2d` FOREIGN KEY (`application_id`) REFERENCES `loan_application` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of loan_application_log
-- ----------------------------
INSERT INTO `loan_application_log` VALUES (1, '2026-01-10 08:42:00.000000', 'SQ20260110001', '提交申请', '张大山', '线上提交，资料齐全');
INSERT INTO `loan_application_log` VALUES (2, '2026-01-11 10:15:00.000000', 'SQ20260110001', '初审通过', '李慧·风控审批员', '信用良好，同意发放');
INSERT INTO `loan_application_log` VALUES (3, '2026-01-12 11:20:00.000000', 'SQ20260110001', '签署合同', '张大山', '线上签署借款合同');
INSERT INTO `loan_application_log` VALUES (4, '2026-01-13 09:00:00.000000', 'SQ20260110001', '放款成功', '系统', '贷款资金已发放至借款人账户');
INSERT INTO `loan_application_log` VALUES (5, '2026-07-10 09:30:00.000000', 'SQ20260110001', '贷款结清', '张大山', '归还本息 20,435.00 元');
INSERT INTO `loan_application_log` VALUES (6, '2026-03-12 09:20:00.000000', 'SQ20260312002', '提交申请', '张大山', '财政贴息通道申请');
INSERT INTO `loan_application_log` VALUES (7, '2026-03-13 15:40:00.000000', 'SQ20260312002', '初审通过', '李慧·风控审批员', '经营流水充足，同意发放');
INSERT INTO `loan_application_log` VALUES (8, '2026-03-14 10:02:00.000000', 'SQ20260312002', '签署合同', '张大山', '线上签署借款合同');
INSERT INTO `loan_application_log` VALUES (9, '2026-03-16 09:28:00.000000', 'SQ20260312002', '放款成功', '系统', '贷款资金已发放至借款人账户');
INSERT INTO `loan_application_log` VALUES (10, '2026-08-18 11:05:00.000000', 'SQ20260312002', '贷款结清', '张大山', '归还本息 62,190.00 元');
INSERT INTO `loan_application_log` VALUES (11, '2026-06-02 10:21:00.000000', 'SQ20260615004', '提交申请', '张大山', '申请叠加农机购置补贴');
INSERT INTO `loan_application_log` VALUES (12, '2026-06-04 16:00:00.000000', 'SQ20260615004', '初审通过', '李慧·风控审批员', '购机补贴权益质押完整，同意发放');
INSERT INTO `loan_application_log` VALUES (13, '2026-06-06 11:08:00.000000', 'SQ20260615004', '签署合同', '张大山', '线上签署借款合同');
INSERT INTO `loan_application_log` VALUES (14, '2026-06-15 09:12:00.000000', 'SQ20260615004', '放款成功', '系统', '贷款资金已发放至借款人账户');
INSERT INTO `loan_application_log` VALUES (15, '2026-07-18 09:30:00.000000', 'SQ20260726005', '提交申请', '张大山', '线上提交');
INSERT INTO `loan_application_log` VALUES (16, '2026-07-20 15:18:00.000000', 'SQ20260726005', '初审通过', '李慧·风控审批员', '用途合规、还款来源明确，同意发放');
INSERT INTO `loan_application_log` VALUES (17, '2026-07-26 14:20:00.000000', 'SQ20260726005', '签署合同', '张大山', '线上签署借款合同，等待放款');
INSERT INTO `loan_application_log` VALUES (18, '2026-08-10 14:12:00.000000', 'SQ20260812006', '提交申请', '张大山', '活体抵押 + 保单质押');
INSERT INTO `loan_application_log` VALUES (19, '2026-08-12 15:06:00.000000', 'SQ20260812006', '初审通过', '李慧·风控审批员', '配套保险保单齐备，同意初审通过');
INSERT INTO `loan_application_log` VALUES (20, '2026-08-28 10:02:00.000000', 'SQ20260830007', '提交申请', '张大山', '线上提交');
INSERT INTO `loan_application_log` VALUES (21, '2026-08-31 09:41:00.000000', 'SQ20260830007', '已拒绝', '李慧·风控审批员', '近 90 天征信查询次数偏多，暂不符合准入条件');
INSERT INTO `loan_application_log` VALUES (22, '2026-09-01 09:12:00.000000', 'SQ20260901001', '提交申请', '张大山', '线上提交，资料齐全');
INSERT INTO `loan_application_log` VALUES (23, '2026-04-15 10:08:00.000000', 'SQ20260415003', '提交申请', '湄潭雲雾茶业专业合作社', '附社员收购订单');
INSERT INTO `loan_application_log` VALUES (24, '2026-04-16 09:52:00.000000', 'SQ20260415003', '初审通过', '李慧·风控审批员', '合作社主体信用良好，同意发放');
INSERT INTO `loan_application_log` VALUES (25, '2026-04-18 14:30:00.000000', 'SQ20260415003', '签署合同', '湄潭雲雾茶业专业合作社', '法定代表人线上签署');
INSERT INTO `loan_application_log` VALUES (26, '2026-04-20 10:15:00.000000', 'SQ20260415003', '放款成功', '系统', '贷款资金已发放至对公账户');
INSERT INTO `loan_application_log` VALUES (27, '2026-08-20 10:40:00.000000', 'SQ20260415003', '贷款结清', '湄潭雲雾茶业专业合作社', '归还本息 155,475.00 元');
INSERT INTO `loan_application_log` VALUES (28, '2026-05-20 09:15:00.000000', 'SQ20260610005', '提交申请', '湄潭雲雾茶业专业合作社', '附设备购置清单');
INSERT INTO `loan_application_log` VALUES (29, '2026-05-24 11:26:00.000000', 'SQ20260610005', '初审通过', '李慧·风控审批员', '设备购置凭证齐全，同意发放');
INSERT INTO `loan_application_log` VALUES (30, '2026-06-01 15:03:00.000000', 'SQ20260610005', '签署合同', '湄潭雲雾茶业专业合作社', '法定代表人线上签署');
INSERT INTO `loan_application_log` VALUES (31, '2026-06-10 09:40:00.000000', 'SQ20260610005', '放款成功', '系统', '贷款资金已发放至对公账户');
INSERT INTO `loan_application_log` VALUES (32, '2026-07-19 10:30:00.000000', 'SQ20260721007', '提交申请', '湄潭雲雾茶业专业合作社', '农业担保公司增信');
INSERT INTO `loan_application_log` VALUES (33, '2026-07-22 10:30:00.000000', 'SQ20260721007', '初审通过', '李慧·风控审批员', '政银担分险方案落地，同意初审通过');
INSERT INTO `loan_application_log` VALUES (34, '2026-07-28 08:45:00.000000', 'SQ20260805009', '提交申请', '湄潭雲雾茶业专业合作社', '附仓储建设可行性报告');
INSERT INTO `loan_application_log` VALUES (35, '2026-08-01 10:12:00.000000', 'SQ20260805009', '初审通过', '李慧·风控审批员', '仓单质押方案可行，同意发放');
INSERT INTO `loan_application_log` VALUES (36, '2026-08-05 16:22:00.000000', 'SQ20260805009', '签署合同', '湄潭雲雾茶业专业合作社', '法定代表人线上签署，等待放款');
INSERT INTO `loan_application_log` VALUES (37, '2026-09-05 10:08:00.000000', 'SQ20260905003', '提交申请', '湄潭雲雾茶业专业合作社', '附活体抵押清单');

-- ----------------------------
-- Table structure for loan_product
-- ----------------------------
DROP TABLE IF EXISTS `loan_product`;
CREATE TABLE `loan_product`  (
  `apply_count` int NOT NULL,
  `enabled` bit(1) NOT NULL,
  `hot` bit(1) NOT NULL,
  `icon_text` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `min_credit_level` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `rate` double NOT NULL,
  `term_months` int NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `max_amount` bigint NOT NULL,
  `min_amount` bigint NOT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `badge` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `repayment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `terms` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `guarantee` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `target` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `features` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `type` enum('CREDIT','MORTGAGE','POLICY') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of loan_product
-- ----------------------------
INSERT INTO `loan_product` VALUES (1286, b'1', b'1', '春', 'A', 3.65, 12, '2026-09-15 18:36:32.519415', 300000, 10000, '2026-09-15 18:36:32.519415', '纯信用', 'P001', '到期一次性还本付息', '6,12,18', '信用担保，免抵押', '惠农e贷·春耕贷', '从事粮食、经济作物种植的普通农户', '线上申请 3 分钟;随借随还;财政贴息 1.5%', 'CREDIT');
INSERT INTO `loan_product` VALUES (634, b'1', b'0', '机', 'C', 3.85, 24, '2026-09-15 18:36:32.530746', 800000, 50000, '2026-09-15 18:36:32.530746', '可抵押', 'P002', '等额本息，按月还款', '12,24,36', '购机补贴权益质押', '农机购置专项贷', '农机合作社、种植大户、家庭农场', '额度高至 80 万;农机补贴直达;首年只还息', 'MORTGAGE');
INSERT INTO `loan_product` VALUES (289, b'1', b'1', '兴', 'C', 4.15, 36, '2026-09-15 18:36:32.531755', 3000000, 200000, '2026-09-15 18:36:32.531755', '政策贴息', 'P003', '按季还息，到期还本', '24,36,60', '农业担保公司增信', '乡村振兴产业贷', '农业产业化龙头企业、农民专业合作社', '政银担三方分险;最长 5 年期;绿色审批通道', 'POLICY');
INSERT INTO `loan_product` VALUES (2153, b'1', b'0', '小', 'B', 4.35, 6, '2026-09-15 18:36:32.533125', 100000, 5000, '2026-09-15 18:36:32.533125', '极速批', 'P004', '按月付息，到期还本', '3,6,12', '纯信用，整村授信', '农户小额信用贷', '信用村内的常住农户', '当日申请当日批;10 万以内免担保;续贷无还本', 'CREDIT');
INSERT INTO `loan_product` VALUES (172, b'1', b'0', '仓', 'C', 3.95, 24, '2026-09-15 18:36:32.534166', 1500000, 100000, '2026-09-15 18:36:32.534166', '基建类', 'P005', '等额本金，按月还款', '12,24,36', '仓单质押 + 保证保险', '冷链仓储建设贷', '农产品仓储、冷链物流经营主体', '支持仓单质押;建设期只付息;县域全覆盖', 'MORTGAGE');
INSERT INTO `loan_product` VALUES (458, b'1', b'1', '养', 'C', 3.55, 18, '2026-09-15 18:36:32.535209', 500000, 30000, '2026-09-15 18:36:32.535209', '贴息', 'P006', '灵活还款，可提前结清', '12,18,24', '活体抵押 + 保险单质押', '特色养殖扶持贷', '畜禽、水产规模化养殖场（户）', '活体畜禽可抵押;保险同步承保;免收提前还款违约金', 'POLICY');

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `success` bit(1) NOT NULL,
  `cost_ms` bigint NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `operate_time` datetime(6) NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `target` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `params` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_oplog_username`(`username` ASC) USING BTREE,
  INDEX `idx_oplog_time`(`operate_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------
INSERT INTO `sys_operation_log` VALUES (b'1', 250, 1, '2026-09-15 18:49:21.423661', 'FARMER', '0:0:0:0:0:0:0:1', '认证', 'farmer', '账号登录', 'farmer', 'AuthService#login', NULL, '{\"request\":{\"username\":\"farmer\",\"password\":\"***\"}}');
INSERT INTO `sys_operation_log` VALUES (b'1', 79, 2, '2026-09-15 18:55:16.926192', '', '0:0:0:0:0:0:0:1', '认证', '匿名', '注册账号', '114514', 'AuthService#register', NULL, '{\"request\":{\"username\":\"114514\",\"password\":\"***\",\"name\":\"test\",\"role\":\"FARMER\",\"phone\":\"13041561984\",\"address\":\"比奇堡\",\"business\":\"茶叶创造\",\"scale\":\"32亩\"}}');
INSERT INTO `sys_operation_log` VALUES (b'1', 172, 3, '2026-09-15 19:52:12.311547', 'BANK_ADMIN', '127.0.0.1', '认证', 'auditor', '账号登录', 'auditor', 'AuthService#login', NULL, '{\"request\":{\"username\":\"auditor\",\"password\":\"***\"}}');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `asset_score` int NOT NULL,
  `balance` decimal(18, 2) NOT NULL,
  `business_score` int NOT NULL,
  `credit_score` int NOT NULL,
  `enabled` bit(1) NOT NULL,
  `policy_score` int NOT NULL,
  `record_score` int NOT NULL,
  `repay_score` int NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `id_card` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `scale` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `business` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `role` enum('FARMER','ENTERPRISE','BANK_ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (70, 12860.50, 76, 762, b'1', 80, 92, 88, '2026-09-15 18:36:32.733469', 1, '2026-09-15 18:36:32.733469', '13880666721', '522132199003152317', '32 亩', 'farmer', '茶叶种植与初加工', '张大山', '$2a$10$5J7c7COs/jTZbmx1JICKJu2SxBaQJD3ZbWx9sSGvgpMsjEOCryPsu', '贵州省遵义市湄潭县永兴镇', 'FARMER');
INSERT INTO `sys_user` VALUES (82, 236800.00, 84, 805, b'1', 86, 88, 90, '2026-09-15 18:36:32.762211', 2, '2026-09-15 18:36:32.762211', '085128769988', '91520328MA6AB12345', '社员 126 户', 'coop', '茶叶统购统销', '湄潭雲雾茶业专业合作社', '$2a$10$7f29XLJ5PGd7w9JkDZ0utO7n46nxhb6slgA3u5ZJmG/gTS66eAfAS', '贵州省遵义市湄潭县湄江街道', 'ENTERPRISE');
INSERT INTO `sys_user` VALUES (90, 0.00, 92, 900, b'1', 90, 95, 95, '2026-09-15 18:36:32.763297', 3, '2026-09-15 18:36:32.763297', '13800138000', '', '', 'auditor', '信贷审批', '李慧·风控审批员', '$2a$10$c0YAQvX5Vneq5JyXTbXYTOYPpc.9OePCQXSU4CQ5amh7oj.iVwKee', '湄潭县金融服务中心', 'BANK_ADMIN');
INSERT INTO `sys_user` VALUES (58, 0.00, 60, 660, b'1', 62, 64, 62, '2026-09-15 18:55:16.922142', 4, '2026-09-15 18:55:16.922142', '13041561984', NULL, '32亩', '114514', '茶叶创造', 'test', '$2a$10$Na4PIJs.uVjwX/GZRvI30eoxLTfd7HVXRpkPHKPvX1YxHKaUiUoce', '比奇堡', 'FARMER');

SET FOREIGN_KEY_CHECKS = 1;
