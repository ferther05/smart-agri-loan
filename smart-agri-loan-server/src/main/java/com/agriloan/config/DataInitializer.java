package com.agriloan.config;

import com.agriloan.domain.*;
import com.agriloan.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 种子数据初始化：首次启动自动灌入演示数据（与前端原有假数据保持一致）
 *
 * <p>包含：6 款贷款产品、6 条惠农资讯、3 个角色账号、覆盖全生命周期的申请单、
 * 信用积分变动日志。已存在数据时不会重复初始化。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final DateTimeFormatter MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserRepository userRepository;

    private final ProductRepository productRepository;

    private final NewsRepository newsRepository;

    private final LoanApplicationRepository applicationRepository;

    private final CreditLogRepository creditLogRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("检测到已有数据，跳过初始化");
            return;
        }
        seedProducts();
        seedNews();
        seedUsersAndApplications();
        log.info("种子数据初始化完成：默认账号 farmer / coop / auditor，密码均为 123456");
    }

    /* ==================== 产品 ==================== */

    private void seedProducts() {
        productRepository.saveAll(List.of(
                product("P001", "惠农e贷·春耕贷", ProductType.CREDIT, "纯信用", "春", 3.65, 12, "6,12,18",
                        10000L, 300000L, "到期一次性还本付息", "信用担保，免抵押",
                        "从事粮食、经济作物种植的普通农户", "线上申请 3 分钟;随借随还;财政贴息 1.5%",
                        1286, true, "A"),
                product("P002", "农机购置专项贷", ProductType.MORTGAGE, "可抵押", "机", 3.85, 24, "12,24,36",
                        50000L, 800000L, "等额本息，按月还款", "购机补贴权益质押",
                        "农机合作社、种植大户、家庭农场", "额度高至 80 万;农机补贴直达;首年只还息",
                        634, false, "C"),
                product("P003", "乡村振兴产业贷", ProductType.POLICY, "政策贴息", "兴", 4.15, 36, "24,36,60",
                        200000L, 3000000L, "按季还息，到期还本", "农业担保公司增信",
                        "农业产业化龙头企业、农民专业合作社", "政银担三方分险;最长 5 年期;绿色审批通道",
                        289, true, "C"),
                product("P004", "农户小额信用贷", ProductType.CREDIT, "极速批", "小", 4.35, 6, "3,6,12",
                        5000L, 100000L, "按月付息，到期还本", "纯信用，整村授信",
                        "信用村内的常住农户", "当日申请当日批;10 万以内免担保;续贷无还本",
                        2153, false, "B"),
                product("P005", "冷链仓储建设贷", ProductType.MORTGAGE, "基建类", "仓", 3.95, 24, "12,24,36",
                        100000L, 1500000L, "等额本金，按月还款", "仓单质押 + 保证保险",
                        "农产品仓储、冷链物流经营主体", "支持仓单质押;建设期只付息;县域全覆盖",
                        172, false, "C"),
                product("P006", "特色养殖扶持贷", ProductType.POLICY, "贴息", "养", 3.55, 18, "12,18,24",
                        30000L, 500000L, "灵活还款，可提前结清", "活体抵押 + 保险单质押",
                        "畜禽、水产规模化养殖场（户）", "活体畜禽可抵押;保险同步承保;免收提前还款违约金",
                        458, true, "C")));
    }

    private Product product(String id, String name, ProductType type, String badge, String iconText,
                            double rate, int termMonths, String terms, long min, long max,
                            String repayment, String guarantee, String target, String features,
                            int applyCount, boolean hot, String minCreditLevel) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setType(type);
        p.setBadge(badge);
        p.setIconText(iconText);
        p.setRate(rate);
        p.setTermMonths(termMonths);
        p.setTerms(terms);
        p.setMinAmount(min);
        p.setMaxAmount(max);
        p.setRepayment(repayment);
        p.setGuarantee(guarantee);
        p.setTarget(target);
        p.setFeatures(features);
        p.setApplyCount(applyCount);
        p.setHot(hot);
        p.setMinCreditLevel(minCreditLevel);
        return p;
    }

    /* ==================== 资讯 ==================== */

    private void seedNews() {
        newsRepository.saveAll(List.of(
                news("N001", "中央一号文件：持续加大乡村振兴金融支持力度", NewsCategory.POLICY, "农业农村部",
                        "2026-08-28", 12680,
                        "文件明确要求金融机构单列涉农信贷计划，保持涉农贷款余额持续增长，鼓励开发专属金融产品支持粮油种植与畜牧水产。",
                        "文件提出，要健全乡村振兴多元投入机制，坚持将农业农村作为一般公共预算优先保障领域，创新乡村振兴投融资机制。\n"
                                + "在信贷支持方面，要求金融机构单列涉农信贷计划，努力实现涉农贷款余额持续增长，并鼓励各地结合实际扩大完全成本保险和种植收入保险政策实施范围。\n"
                                + "文件同时强调，要深化农村信用社改革，坚持县域法人总体稳定，稳妥有序推进村镇银行改革重组，提升涉农金融机构服务能力。"),
                news("N002", "2026 年农业生产社会化服务补助资金申报开始", NewsCategory.POLICY, "省农业农村厅",
                        "2026-08-21", 8342,
                        "补助资金重点支持粮油等关键薄弱环节的社会化服务，符合条件的合作社、家庭农场可在线提交申报材料。",
                        "补助资金重点支持深耕深松、统防统治、秸秆还田等粮油生产关键薄弱环节的社会化服务，优先支持小农户接受服务。\n"
                                + "申报主体需具备两年以上服务经验、拥有相应农业机械装备，并提供服务合同、作业记录等佐证材料。\n"
                                + "县级农业农村部门将在 15 个工作日内完成初审，市级复核后统一公示，公示无异议即可拨付资金。"),
                news("N003", "农户必读：个人征信报告怎么看、怎么养", NewsCategory.KNOWLEDGE, "平台金融课堂",
                        "2026-08-15", 23105,
                        "征信报告是贷款审批的重要依据。本文教你读懂逾期记录、查询次数与负债率三个关键指标，并给出养护建议。",
                        "征信报告核心看三处：一是信贷记录中的逾期情况，连三累六（连续 3 个月或累计 6 次逾期）会显著影响审批；二是查询记录，近 30 天硬查询超过 3 次会被判定为资金紧张；三是负债率，超过月收入的 50% 将影响额度。\n"
                                + "养护建议：按时足额还款是最核心的一条；谨慎点击各类测额度链接，避免产生硬查询；适度使用信用卡并保持稳定账单，有助于积累正向记录。\n"
                                + "若发现报告有误，可向金融机构或人民银行征信中心提出异议申请，一般 20 日内可完成核查处理。"),
                news("N004", "警惕三类涉农贷款诈骗，守住钱袋子", NewsCategory.KNOWLEDGE, "平台风控中心",
                        "2026-08-09", 18573,
                        "免抵押秒放款、内部渠道提额、缴纳保证金解冻额度均为典型诈骗话术，请通过官方渠道办理贷款。",
                        "第一类：以免抵押、免征信、秒放款为噱头吸引提交资料，随后收取手续费、保证金后失联。\n"
                                + "第二类：冒充银行工作人员，以内部渠道提额、注销校园贷账户为由诱导转账或提供验证码。\n"
                                + "第三类：发送带链接的贷款获批短信，诱导下载仿冒 App 填写银行卡信息。\n"
                                + "防范要点：正规贷款放款前不收取任何费用；本平台不会以私人账户收款；请勿向任何人透露短信验证码。"),
                news("N005", "夏粮收购收官，优质小麦价格稳中有升", NewsCategory.INDUSTRY, "农产品市场周报",
                        "2026-08-02", 6421,
                        "主产区夏粮收购接近尾声，优质专用小麦收购均价同比上涨 4.2%，种植主体收益稳步提升。",
                        "据统计，今年夏粮收购总量同比略增，优质专用小麦因需求旺盛，收购均价较去年同期上涨约 4.2%。\n"
                                + "受此影响，规模化种植主体现金流状况良好，秋播农资采购需求提前释放，相关信贷需求同步上升。\n"
                                + "分析师提示，需关注化肥等农资价格波动对秋冬种成本的影响，建议种植主体提前锁定采购资金。"),
                news("N006", "数字金融下沉：整村授信覆盖行政村超 12 万个", NewsCategory.INDUSTRY, "农村金融观察",
                        "2026-07-26", 5390,
                        "整村授信模式通过村级评议 + 大数据评分，让无抵押农户获得基础授信额度，线上即可支用。",
                        "整村授信以行政村为单位，由村两委、驻村金融助理共同开展农户信息采集与信用评议，形成白名单。\n"
                                + "评议结果与大数据风控模型结合，为农户生成基础授信额度，农户通过手机即可随借随还。\n"
                                + "目前该模式已覆盖全国超过 12 万个行政村，户均授信额度约 8 万元，平均审批时长缩短至 1.5 天。")));
    }

    private News news(String id, String title, NewsCategory category, String source, String date,
                      int views, String summary, String content) {
        News n = new News();
        n.setId(id);
        n.setTitle(title);
        n.setCategory(category);
        n.setSource(source);
        n.setDate(date);
        n.setViews(views);
        n.setSummary(summary);
        n.setContent(content);
        return n;
    }

    /* ==================== 用户 + 申请单 ==================== */

    private void seedUsersAndApplications() {
        User farmer = user("farmer", "张大山", Role.FARMER, "13880666721", "522132199003152317",
                "贵州省遵义市湄潭县永兴镇", "茶叶种植与初加工", "32 亩",
                762, new BigDecimal("12860.50"), 88, 76, 70, 92, 80);
        User coop = user("coop", "湄潭雲雾茶业专业合作社", Role.ENTERPRISE, "085128769988", "91520328MA6AB12345",
                "贵州省遵义市湄潭县湄江街道", "茶叶统购统销", "社员 126 户",
                805, new BigDecimal("236800.00"), 90, 84, 82, 88, 86);
        User auditor = user("auditor", "李慧·风控审批员", Role.BANK_ADMIN, "13800138000", "",
                "湄潭县金融服务中心", "信贷审批", "", 900, BigDecimal.ZERO, 95, 92, 90, 95, 90);
        userRepository.saveAll(List.of(farmer, coop, auditor));

        String auditorName = auditor.getName();
        String farmerName = farmer.getName();
        String coopName = coop.getName();

        /* --- 张大山：已结清（历史闭环） --- */
        LoanApplication a1 = base(farmer, "P004", "SQ20260110001", "20000", 6, "春耕农资集中采购周转",
                716, ApplyStatus.SETTLED, 4.35, "2026-01-10 08:42");
        setAudit(a1, "2026-01-11 10:15", "信用良好，同意发放", auditorName);
        a1.setSignedTime(at("2026-01-12 11:20"));
        a1.setLoanTime(at("2026-01-13 09:00"));
        a1.setSettleTime(at("2026-07-10 09:30"));
        a1.setRepayAmount(new BigDecimal("20435.00"));
        a1.setUpdateTime(at("2026-07-10 09:30"));
        a1.addLogAt(at("2026-01-10 08:42"), "提交申请", farmerName, "线上提交，资料齐全");
        a1.addLogAt(at("2026-01-11 10:15"), "初审通过", auditorName, "信用良好，同意发放");
        a1.addLogAt(at("2026-01-12 11:20"), "签署合同", farmerName, "线上签署借款合同");
        a1.addLogAt(at("2026-01-13 09:00"), "放款成功", "系统", "贷款资金已发放至借款人账户");
        a1.addLogAt(at("2026-07-10 09:30"), "贷款结清", farmerName, "归还本息 20,435.00 元");

        LoanApplication a2 = base(farmer, "P001", "SQ20260312002", "60000", 12, "春季茶园追肥与管护人工费",
                737, ApplyStatus.SETTLED, 3.65, "2026-03-12 09:20");
        setAudit(a2, "2026-03-13 15:40", "经营流水充足，同意发放", auditorName);
        a2.setSignedTime(at("2026-03-14 10:02"));
        a2.setLoanTime(at("2026-03-16 09:28"));
        a2.setSettleTime(at("2026-08-18 11:05"));
        a2.setRepayAmount(new BigDecimal("62190.00"));
        a2.setUpdateTime(at("2026-08-18 11:05"));
        a2.addLogAt(at("2026-03-12 09:20"), "提交申请", farmerName, "财政贴息通道申请");
        a2.addLogAt(at("2026-03-13 15:40"), "初审通过", auditorName, "经营流水充足，同意发放");
        a2.addLogAt(at("2026-03-14 10:02"), "签署合同", farmerName, "线上签署借款合同");
        a2.addLogAt(at("2026-03-16 09:28"), "放款成功", "系统", "贷款资金已发放至借款人账户");
        a2.addLogAt(at("2026-08-18 11:05"), "贷款结清", farmerName, "归还本息 62,190.00 元");

        /* --- 张大山：还款中 --- */
        LoanApplication a3 = base(farmer, "P002", "SQ20260615004", "150000", 24, "购置履带式旋耕机与植保无人机",
                762, ApplyStatus.LOANED, 3.85, "2026-06-02 10:21");
        setAudit(a3, "2026-06-04 16:00", "购机补贴权益质押完整，同意发放", auditorName);
        a3.setSignedTime(at("2026-06-06 11:08"));
        a3.setLoanTime(at("2026-06-15 09:12"));
        a3.setUpdateTime(at("2026-06-15 09:12"));
        a3.addLogAt(at("2026-06-02 10:21"), "提交申请", farmerName, "申请叠加农机购置补贴");
        a3.addLogAt(at("2026-06-04 16:00"), "初审通过", auditorName, "购机补贴权益质押完整，同意发放");
        a3.addLogAt(at("2026-06-06 11:08"), "签署合同", farmerName, "线上签署借款合同");
        a3.addLogAt(at("2026-06-15 09:12"), "放款成功", "系统", "贷款资金已发放至借款人账户");

        /* --- 张大山：已签约待放款 --- */
        LoanApplication a4 = base(farmer, "P001", "SQ20260726005", "100000", 12, "秋季茶园管护与采工工资周转",
                762, ApplyStatus.SIGNED, 3.65, "2026-07-18 09:30");
        setAudit(a4, "2026-07-20 15:18", "用途合规、还款来源明确，同意发放", auditorName);
        a4.setSignedTime(at("2026-07-26 14:20"));
        a4.setUpdateTime(at("2026-07-26 14:20"));
        a4.addLogAt(at("2026-07-18 09:30"), "提交申请", farmerName, "线上提交");
        a4.addLogAt(at("2026-07-20 15:18"), "初审通过", auditorName, "用途合规、还款来源明确，同意发放");
        a4.addLogAt(at("2026-07-26 14:20"), "签署合同", farmerName, "线上签署借款合同，等待放款");

        /* --- 张大山：初审通过待签约 --- */
        LoanApplication a5 = base(farmer, "P006", "SQ20260812006", "60000", 18, "茶园套养生态鸡舍扩建",
                762, ApplyStatus.FIRST_PASS, 3.55, "2026-08-10 14:12");
        setAudit(a5, "2026-08-12 15:06", "配套保险保单齐备，同意初审通过", auditorName);
        a5.setUpdateTime(at("2026-08-12 15:06"));
        a5.addLogAt(at("2026-08-10 14:12"), "提交申请", farmerName, "活体抵押 + 保单质押");
        a5.addLogAt(at("2026-08-12 15:06"), "初审通过", auditorName, "配套保险保单齐备，同意初审通过");

        /* --- 张大山：已拒绝 --- */
        LoanApplication a6 = base(farmer, "P004", "SQ20260830007", "30000", 6, "茶园防霜冻设施采购",
                762, ApplyStatus.REJECTED, 4.35, "2026-08-28 10:02");
        setAudit(a6, "2026-08-31 09:41", "近 90 天征信查询次数偏多，暂不符合当前准入条件，建议 3 个月后重新申请", auditorName);
        a6.setUpdateTime(at("2026-08-31 09:41"));
        a6.addLogAt(at("2026-08-28 10:02"), "提交申请", farmerName, "线上提交");
        a6.addLogAt(at("2026-08-31 09:41"), "已拒绝", auditorName, "近 90 天征信查询次数偏多，暂不符合准入条件");

        /* --- 张大山：待初审 --- */
        LoanApplication a7 = base(farmer, "P001", "SQ20260901001", "80000", 12, "购买春耕化肥、茶苗及支付管护人工费",
                762, ApplyStatus.PENDING_FIRST, 3.65, "2026-09-01 09:12");
        a7.addLogAt(at("2026-09-01 09:12"), "提交申请", farmerName, "线上提交，资料齐全");

        /* --- 合作社：全阶段 --- */
        LoanApplication c1 = base(coop, "P001", "SQ20260415003", "150000", 12, "春茶鲜叶统购资金周转",
                805, ApplyStatus.SETTLED, 3.65, "2026-04-15 10:08");
        setAudit(c1, "2026-04-16 09:52", "合作社主体信用良好，同意发放", auditorName);
        c1.setSignedTime(at("2026-04-18 14:30"));
        c1.setLoanTime(at("2026-04-20 10:15"));
        c1.setSettleTime(at("2026-08-20 10:40"));
        c1.setRepayAmount(new BigDecimal("155475.00"));
        c1.setUpdateTime(at("2026-08-20 10:40"));
        c1.addLogAt(at("2026-04-15 10:08"), "提交申请", coopName, "附社员收购订单");
        c1.addLogAt(at("2026-04-16 09:52"), "初审通过", auditorName, "合作社主体信用良好，同意发放");
        c1.addLogAt(at("2026-04-18 14:30"), "签署合同", coopName, "法定代表人线上签署");
        c1.addLogAt(at("2026-04-20 10:15"), "放款成功", "系统", "贷款资金已发放至对公账户");
        c1.addLogAt(at("2026-08-20 10:40"), "贷款结清", coopName, "归还本息 155,475.00 元");

        LoanApplication c2 = base(coop, "P002", "SQ20260610005", "260000", 36, "购置茶叶加工生产线与冷链转运车",
                805, ApplyStatus.LOANED, 3.85, "2026-05-20 09:15");
        setAudit(c2, "2026-05-24 11:26", "设备购置凭证齐全，同意发放", auditorName);
        c2.setSignedTime(at("2026-06-01 15:03"));
        c2.setLoanTime(at("2026-06-10 09:40"));
        c2.setUpdateTime(at("2026-06-10 09:40"));
        c2.addLogAt(at("2026-05-20 09:15"), "提交申请", coopName, "附设备购置清单");
        c2.addLogAt(at("2026-05-24 11:26"), "初审通过", auditorName, "设备购置凭证齐全，同意发放");
        c2.addLogAt(at("2026-06-01 15:03"), "签署合同", coopName, "法定代表人线上签署");
        c2.addLogAt(at("2026-06-10 09:40"), "放款成功", "系统", "贷款资金已发放至对公账户");

        LoanApplication c3 = base(coop, "P003", "SQ20260721007", "400000", 60, "茶园基地提质改造与区域品牌建设",
                805, ApplyStatus.FIRST_PASS, 4.15, "2026-07-19 10:30");
        setAudit(c3, "2026-07-22 10:30", "政银担分险方案落地，同意初审通过", auditorName);
        c3.setUpdateTime(at("2026-07-22 10:30"));
        c3.addLogAt(at("2026-07-19 10:30"), "提交申请", coopName, "农业担保公司增信");
        c3.addLogAt(at("2026-07-22 10:30"), "初审通过", auditorName, "政银担分险方案落地，同意初审通过");

        LoanApplication c4 = base(coop, "P005", "SQ20260805009", "300000", 36, "茶叶冷链仓储设施扩建工程",
                805, ApplyStatus.SIGNED, 3.95, "2026-07-28 08:45");
        setAudit(c4, "2026-08-01 10:12", "仓单质押方案可行，同意发放", auditorName);
        c4.setSignedTime(at("2026-08-05 16:22"));
        c4.setUpdateTime(at("2026-08-05 16:22"));
        c4.addLogAt(at("2026-07-28 08:45"), "提交申请", coopName, "附仓储建设可行性报告");
        c4.addLogAt(at("2026-08-01 10:12"), "初审通过", auditorName, "仓单质押方案可行，同意发放");
        c4.addLogAt(at("2026-08-05 16:22"), "签署合同", coopName, "法定代表人线上签署，等待放款");

        LoanApplication c5 = base(coop, "P006", "SQ20260905003", "260000", 18, "茶园配套生态养鸡场扩建",
                805, ApplyStatus.PENDING_FIRST, 3.55, "2026-09-05 10:08");
        c5.addLogAt(at("2026-09-05 10:08"), "提交申请", coopName, "附活体抵押清单");

        applicationRepository.saveAll(List.of(a1, a2, a3, a4, a5, a6, a7, c1, c2, c3, c4, c5));

        /* --- 张大山信用积分日志 --- */
        creditLogRepository.saveAll(List.of(
                credit("C006", farmer.getId(), "2026-09-01", "春耕贷按时还款 3 期", 8, 762, "repay"),
                credit("C005", farmer.getId(), "2026-07-18", "完成土地确权信息补录", 5, 754, "asset"),
                credit("C004", farmer.getId(), "2026-06-02", "纳入整村授信白名单", 12, 749, "policy"),
                credit("C003", farmer.getId(), "2026-04-26", "购买农业保险（茶叶气象指数险）", 6, 737, "business"),
                credit("C002", farmer.getId(), "2026-03-11", "上年度贷款提前结清", 15, 731, "repay"),
                credit("C001", farmer.getId(), "2026-01-09", "首次建立涉农信用档案", 60, 716, "record"),
                credit("D002", coop.getId(), "2026-08-20", "合作社贷款按时结清", 10, 805, "repay"),
                credit("D001", coop.getId(), "2026-01-05", "首次建立新型经营主体信用档案", 80, 795, "record")));
    }

    private User user(String username, String name, Role role, String phone, String idCard, String address,
                      String business, String scale, int creditScore, BigDecimal balance,
                      int repay, int businessScore, int asset, int record, int policy) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode("123456"));
        u.setName(name);
        u.setRole(role);
        u.setPhone(phone);
        u.setIdCard(idCard);
        u.setAddress(address);
        u.setBusiness(business);
        u.setScale(scale);
        u.setCreditScore(creditScore);
        u.setBalance(balance);
        u.setRepayScore(repay);
        u.setBusinessScore(businessScore);
        u.setAssetScore(asset);
        u.setRecordScore(record);
        u.setPolicyScore(policy);
        return u;
    }

    private LoanApplication base(User user, String productId, String id, String amount, int term,
                                 String purpose, int creditScore, ApplyStatus status, double rate,
                                 String applyTime) {
        Product product = productRepository.findById(productId).orElseThrow();
        LoanApplication a = new LoanApplication();
        a.setId(id);
        a.setProduct(product);
        a.setProductName(product.getName());
        a.setApplicant(user);
        a.setApplicantName(user.getName());
        a.setPhone(user.getPhone());
        a.setIdCard(mask(user.getIdCard()));
        a.setAmount(new BigDecimal(amount));
        a.setTerm(term);
        a.setPurpose(purpose);
        a.setBusiness(user.getBusiness());
        a.setScale(user.getScale());
        a.setAddress(user.getAddress());
        a.setRepaySource("经营收入与农产品销售回款");
        a.setCreditScore(creditScore);
        a.setStatus(status);
        a.setRate(rate);
        a.setApplyTime(at(applyTime));
        a.setUpdateTime(at(applyTime));
        a.setRepayAmount(BigDecimal.ZERO);
        return a;
    }

    private void setAudit(LoanApplication a, String time, String opinion, String auditor) {
        a.setAuditTime(at(time));
        a.setAuditOpinion(opinion);
        a.setAuditor(auditor);
    }

    private CreditLog credit(String id, Long userId, String date, String event, int delta, int score, String type) {
        CreditLog log = new CreditLog();
        log.setId(id);
        log.setUserId(userId);
        log.setDate(date);
        log.setEvent(event);
        log.setDelta(delta);
        log.setScore(score);
        log.setType(type);
        return log;
    }

    private LocalDateTime at(String text) {
        return LocalDateTime.parse(text, MINUTE);
    }

    private String mask(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "**********" + idCard.substring(idCard.length() - 4);
    }
}
