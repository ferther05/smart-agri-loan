/**
 * ============================================================
 *  智能惠农信贷系统 —— 统一数据读写工具（本地存储数据池）
 * ------------------------------------------------------------
 *  规矩：
 *  1. 对外只暴露 getDB() / saveDB() 两个方法；
 *  2. 全项目任何页面都不准直接调用 localStorage，
 *     一律 getDB() 取数据、改内存对象、saveDB() 写回；
 *  3. 首次进入（或数据被清空）时自动灌入种子数据，
 *     为后续所有页面提供展示内容与联调基础。
 *
 *  数据池结构：
 *  {
 *    version, products[], news[], users[], applications[], creditLogs[], updateTime
 *  }
 *
 *  申请单对象字段（严格约定，不允许随意增删）：
 *  {
 *    id,            // 唯一编号，如 SQ20260901001
 *    productId,     // 关联产品编号 -> products[].id
 *    productName,   // 冗余产品名称，列表展示免联表
 *    applicantId,   // 关联用户编号 -> users[].id
 *    applicantName, // 申请人姓名
 *    phone,         // 联系电话
 *    amount,        // 申请金额（单位：元）
 *    term,          // 申请期限（单位：月）
 *    purpose,       // 贷款用途
 *    creditScore,   // 申请时信用积分快照
 *    status,        // 当前状态，必须引用 APP_CONST.APPLY_STATUS
 *    applyTime,     // 申请提交时间
 *    updateTime,    // 最近一次状态变更时间
 *    auditTime,     // 审批时间（未审批为空串）
 *    auditOpinion,  // 审批意见（未审批为空串）
 *    auditor,       // 审批人
 *    rate,          // 利率快照（取自申请时的产品）
 *    signedTime,    // 签约时间
 *    loanTime,      // 放款时间
 *    settleTime,    // 结清时间
 *    repayAmount,   // 结清时实际归还的本息合计（元）
 *    logs: [{ time, action, operator, note }] // 流转日志
 *  }
 * ============================================================
 */
(function (global) {
  'use strict';

  var CONST = global.APP_CONST;
  var KEY = CONST.STORAGE_KEY;
  var STATUS = CONST.APPLY_STATUS;

  /* ---------------------------------------------------------
   *  种子数据
   * ------------------------------------------------------- */

  /** 贷款产品：不同利率、不同期限，覆盖信用/抵押/政策三类 */
  function seedProducts() {
    return [
      {
        id: 'P001',
        name: '惠农e贷·春耕贷',
        type: CONST.PRODUCT_TYPE.CREDIT,
        badge: '纯信用',
        iconText: '春',
        rate: 3.65,
        termMonths: 12,
        terms: [6, 12, 18],
        minAmount: 10000,
        maxAmount: 300000,
        repayment: '到期一次性还本付息',
        guarantee: '信用担保，免抵押',
        target: '从事粮食、经济作物种植的普通农户',
        features: ['线上申请 3 分钟', '随借随还', '财政贴息 1.5%'],
        applyCount: 1286,
        hot: true
      },
      {
        id: 'P002',
        name: '农机购置专项贷',
        type: CONST.PRODUCT_TYPE.MORTGAGE,
        badge: '可抵押',
        iconText: '机',
        rate: 3.85,
        termMonths: 24,
        terms: [12, 24, 36],
        minAmount: 50000,
        maxAmount: 800000,
        repayment: '等额本息，按月还款',
        guarantee: '购机补贴权益质押',
        target: '农机合作社、种植大户、家庭农场',
        features: ['额度高至 80 万', '农机补贴直达', '首年只还息'],
        applyCount: 634,
        hot: false
      },
      {
        id: 'P003',
        name: '乡村振兴产业贷',
        type: CONST.PRODUCT_TYPE.POLICY,
        badge: '政策贴息',
        iconText: '兴',
        rate: 4.15,
        termMonths: 36,
        terms: [24, 36, 60],
        minAmount: 200000,
        maxAmount: 3000000,
        repayment: '按季还息，到期还本',
        guarantee: '农业担保公司增信',
        target: '农业产业化龙头企业、农民专业合作社',
        features: ['政银担三方分险', '最长 5 年期', '绿色审批通道'],
        applyCount: 289,
        hot: true
      },
      {
        id: 'P004',
        name: '农户小额信用贷',
        type: CONST.PRODUCT_TYPE.CREDIT,
        badge: '极速批',
        iconText: '小',
        rate: 4.35,
        termMonths: 6,
        terms: [3, 6, 12],
        minAmount: 5000,
        maxAmount: 100000,
        repayment: '按月付息，到期还本',
        guarantee: '纯信用，整村授信',
        target: '信用村内的常住农户',
        features: ['当日申请当日批', '10 万以内免担保', '续贷无还本'],
        applyCount: 2153,
        hot: false
      },
      {
        id: 'P005',
        name: '冷链仓储建设贷',
        type: CONST.PRODUCT_TYPE.MORTGAGE,
        badge: '基建类',
        iconText: '仓',
        rate: 3.95,
        termMonths: 24,
        terms: [12, 24, 36],
        minAmount: 100000,
        maxAmount: 1500000,
        repayment: '等额本金，按月还款',
        guarantee: '仓单质押 + 保证保险',
        target: '农产品仓储、冷链物流经营主体',
        features: ['支持仓单质押', '建设期只付息', '县域全覆盖'],
        applyCount: 172,
        hot: false
      },
      {
        id: 'P006',
        name: '特色养殖扶持贷',
        type: CONST.PRODUCT_TYPE.POLICY,
        badge: '贴息',
        iconText: '养',
        rate: 3.55,
        termMonths: 18,
        terms: [12, 18, 24],
        minAmount: 30000,
        maxAmount: 500000,
        repayment: '灵活还款，可提前结清',
        guarantee: '活体抵押 + 保险单质押',
        target: '畜禽、水产规模化养殖场（户）',
        features: ['活体畜禽可抵押', '保险同步承保', '免收提前还款违约金'],
        applyCount: 458,
        hot: true
      }
    ];
  }

  /** 惠农资讯 */
  function seedNews() {
    return [
      {
        id: 'N001',
        title: '中央一号文件：持续加大乡村振兴金融支持力度',
        category: CONST.NEWS_CATEGORY.POLICY,
        source: '农业农村部',
        date: '2026-08-28',
        views: 12680,
        summary: '文件明确要求金融机构单列涉农信贷计划，保持涉农贷款余额持续增长，鼓励开发专属金融产品支持粮油种植与畜牧水产。',
        content: [
          '文件提出，要健全乡村振兴多元投入机制，坚持将农业农村作为一般公共预算优先保障领域，创新乡村振兴投融资机制。',
          '在信贷支持方面，要求金融机构单列涉农信贷计划，努力实现涉农贷款余额持续增长，并鼓励各地结合实际扩大完全成本保险和种植收入保险政策实施范围。',
          '文件同时强调，要深化农村信用社改革，坚持县域法人总体稳定，稳妥有序推进村镇银行改革重组，提升涉农金融机构服务能力。'
        ]
      },
      {
        id: 'N002',
        title: '2026 年农业生产社会化服务补助资金申报开始',
        category: CONST.NEWS_CATEGORY.POLICY,
        source: '省农业农村厅',
        date: '2026-08-21',
        views: 8342,
        summary: '补助资金重点支持粮油等关键薄弱环节的社会化服务，符合条件的合作社、家庭农场可在线提交申报材料。',
        content: [
          '补助资金重点支持深耕深松、统防统治、秸秆还田等粮油生产关键薄弱环节的社会化服务，优先支持小农户接受服务。',
          '申报主体需具备两年以上服务经验、拥有相应农业机械装备，并提供服务合同、作业记录等佐证材料。',
          '县级农业农村部门将在 15 个工作日内完成初审，市级复核后统一公示，公示无异议即可拨付资金。'
        ]
      },
      {
        id: 'N003',
        title: '农户必读：个人征信报告怎么看、怎么养',
        category: CONST.NEWS_CATEGORY.KNOWLEDGE,
        source: '平台金融课堂',
        date: '2026-08-15',
        views: 23105,
        summary: '征信报告是贷款审批的重要依据。本文教你读懂逾期记录、查询次数与负债率三个关键指标，并给出养护建议。',
        content: [
          '征信报告核心看三处：一是信贷记录中的逾期情况，连三累六（连续 3 个月或累计 6 次逾期）会显著影响审批；二是查询记录，近 30 天硬查询超过 3 次会被判定为资金紧张；三是负债率，超过月收入的 50% 将影响额度。',
          '养护建议：按时足额还款是最核心的一条；谨慎点击各类"测额度"链接，避免产生硬查询；适度使用信用卡并保持稳定账单，有助于积累正向记录。',
          '若发现报告有误，可向金融机构或人民银行征信中心提出异议申请，一般 20 日内可完成核查处理。'
        ]
      },
      {
        id: 'N004',
        title: '警惕三类涉农贷款诈骗，守住钱袋子',
        category: CONST.NEWS_CATEGORY.KNOWLEDGE,
        source: '平台风控中心',
        date: '2026-08-09',
        views: 18573,
        summary: '"免抵押秒放款""内部渠道提额""缴纳保证金解冻额度"均为典型诈骗话术，请通过官方渠道办理贷款。',
        content: [
          '第一类：以"免抵押、免征信、秒放款"为噱头吸引提交资料，随后收取手续费、保证金后失联。',
          '第二类：冒充银行工作人员，以"内部渠道提额""注销校园贷账户"为由诱导转账或提供验证码。',
          '第三类：发送带链接的"贷款获批"短信，诱导下载仿冒 App 填写银行卡信息。',
          '防范要点：正规贷款放款前不收取任何费用；本平台不会以私人账户收款；请勿向任何人透露短信验证码。'
        ]
      },
      {
        id: 'N005',
        title: '夏粮收购收官，优质小麦价格稳中有升',
        category: CONST.NEWS_CATEGORY.INDUSTRY,
        source: '农产品市场周报',
        date: '2026-08-02',
        views: 6421,
        summary: '主产区夏粮收购接近尾声，优质专用小麦收购均价同比上涨 4.2%，种植主体收益稳步提升。',
        content: [
          '据统计，今年夏粮收购总量同比略增，优质专用小麦因需求旺盛，收购均价较去年同期上涨约 4.2%。',
          '受此影响，规模化种植主体现金流状况良好，秋播农资采购需求提前释放，相关信贷需求同步上升。',
          '分析师提示，需关注化肥等农资价格波动对秋冬种成本的影响，建议种植主体提前锁定采购资金。'
        ]
      },
      {
        id: 'N006',
        title: '数字金融下沉：整村授信覆盖行政村超 12 万个',
        category: CONST.NEWS_CATEGORY.INDUSTRY,
        source: '农村金融观察',
        date: '2026-07-26',
        views: 5390,
        summary: '"整村授信"模式通过村级评议 + 大数据评分，让无抵押农户获得基础授信额度，线上即可支用。',
        content: [
          '"整村授信"以行政村为单位，由村两委、驻村金融助理共同开展农户信息采集与信用评议，形成白名单。',
          '评议结果与大数据风控模型结合，为农户生成基础授信额度，农户通过手机即可随借随还。',
          '目前该模式已覆盖全国超过 12 万个行政村，户均授信额度约 8 万元，平均审批时长缩短至 1.5 天。'
        ]
      }
    ];
  }

  /** 默认模拟用户（当前登录人） */
  function seedUsers() {
    return [
      {
        id: 'U001',
        name: '张大山',
        role: CONST.USER_ROLE.FARMER,
        phone: '138****6721',
        idCard: '5221**********2317',
        address: '贵州省遵义市湄潭县永兴镇',
        business: '茶叶种植与初加工',
        scale: '32 亩',
        creditScore: 762,
        creditLevel: 'AA',
        balance: 12860.5, // 账户余额（元），放款与还款均作用于该字段
        isCurrent: true
      },
      {
        id: 'U002',
        name: '湄潭雲雾茶业专业合作社',
        role: CONST.USER_ROLE.ENTERPRISE,
        phone: '0851-2876****',
        idCard: '91520328MA6*******',
        address: '贵州省遵义市湄潭县湄江街道',
        business: '茶叶统购统销',
        scale: '社员 126 户',
        creditScore: 805,
        creditLevel: 'AAA',
        balance: 236800,
        isCurrent: false
      }
    ];
  }

  /**
   * 测试申请单：覆盖"待初审 → 已结清 / 已拒绝"全生命周期各阶段数据，
   * 供"我的申请"进度页与"审批端"分栏展示 / 联调操作。
   */
  function seedApplications() {
    function L(time, action, operator, note) {
      return { time: time, action: action, operator: operator, note: note };
    }
    function app(cfg, logs) {
      var base = {
        auditTime: '',
        auditOpinion: '',
        auditor: '',
        signedTime: '',
        loanTime: '',
        settleTime: '',
        repayAmount: 0,
        logs: logs || []
      };
      for (var k in cfg) base[k] = cfg[k];
      return base;
    }
    var ME = '张大山';
    var COOP = '合作社财务';
    var AUDITOR = '风控审批员·李';

    return [
      /* ---------- 张大山：历史已结清（进度闭环展示） ---------- */
      app({
        id: 'SQ20260110001', productId: 'P004', productName: '农户小额信用贷',
        applicantId: 'U001', applicantName: ME, phone: '138****6721',
        amount: 20000, term: 6, purpose: '春耕农资集中采购周转',
        creditScore: 716, status: STATUS.SETTLED, rate: 4.35,
        applyTime: '2026-01-10 08:42', updateTime: '2026-07-10 09:30',
        auditTime: '2026-01-11 10:15', auditOpinion: '信用良好，同意发放', auditor: AUDITOR,
        signedTime: '2026-01-12 11:20', loanTime: '2026-01-13 09:00',
        settleTime: '2026-07-10 09:30', repayAmount: 20435
      }, [
        L('2026-01-10 08:42', '提交申请', ME, '线上提交，资料齐全'),
        L('2026-01-11 10:15', '初审通过', AUDITOR, '信用良好，同意发放'),
        L('2026-01-12 11:20', '签署合同', ME, '线上签署借款合同'),
        L('2026-01-13 09:00', '放款成功', '系统', '贷款资金已发放至借款人账户'),
        L('2026-07-10 09:30', '贷款结清', ME, '归还本息 20,435.00 元')
      ]),
      app({
        id: 'SQ20260312002', productId: 'P001', productName: '惠农e贷·春耕贷',
        applicantId: 'U001', applicantName: ME, phone: '138****6721',
        amount: 60000, term: 12, purpose: '春季茶园追肥与管护人工费',
        creditScore: 737, status: STATUS.SETTLED, rate: 3.65,
        applyTime: '2026-03-12 09:20', updateTime: '2026-08-18 11:05',
        auditTime: '2026-03-13 15:40', auditOpinion: '经营流水充足，同意发放', auditor: AUDITOR,
        signedTime: '2026-03-14 10:02', loanTime: '2026-03-16 09:28',
        settleTime: '2026-08-18 11:05', repayAmount: 62190
      }, [
        L('2026-03-12 09:20', '提交申请', ME, '财政贴息通道申请'),
        L('2026-03-13 15:40', '初审通过', AUDITOR, '经营流水充足，同意发放'),
        L('2026-03-14 10:02', '签署合同', ME, '线上签署借款合同'),
        L('2026-03-16 09:28', '放款成功', '系统', '贷款资金已发放至借款人账户'),
        L('2026-08-18 11:05', '贷款结清', ME, '归还本息 62,190.00 元')
      ]),
      /* ---------- 湄潭雲雾茶业专业合作社：历史已结清 ---------- */
      app({
        id: 'SQ20260415003', productId: 'P001', productName: '惠农e贷·春耕贷',
        applicantId: 'U002', applicantName: '湄潭雲雾茶业专业合作社', phone: '0851-2876****',
        amount: 150000, term: 12, purpose: '春茶鲜叶统购资金周转',
        creditScore: 805, status: STATUS.SETTLED, rate: 3.65,
        applyTime: '2026-04-15 10:08', updateTime: '2026-08-20 10:40',
        auditTime: '2026-04-16 09:52', auditOpinion: '合作社主体信用良好，同意发放', auditor: AUDITOR,
        signedTime: '2026-04-18 14:30', loanTime: '2026-04-20 10:15',
        settleTime: '2026-08-20 10:40', repayAmount: 155475
      }, [
        L('2026-04-15 10:08', '提交申请', COOP, '附社员收购订单'),
        L('2026-04-16 09:52', '初审通过', AUDITOR, '合作社主体信用良好，同意发放'),
        L('2026-04-18 14:30', '签署合同', COOP, '法定代表人线上签署'),
        L('2026-04-20 10:15', '放款成功', '系统', '贷款资金已发放至对公账户'),
        L('2026-08-20 10:40', '贷款结清', COOP, '归还本息 155,475.00 元')
      ]),
      /* ---------- 张大山：已放款，还款中 ---------- */
      app({
        id: 'SQ20260615004', productId: 'P002', productName: '农机购置专项贷',
        applicantId: 'U001', applicantName: ME, phone: '138****6721',
        amount: 150000, term: 24, purpose: '购置履带式旋耕机与植保无人机',
        creditScore: 762, status: STATUS.LOANED, rate: 3.85,
        applyTime: '2026-06-02 10:21', updateTime: '2026-06-15 09:12',
        auditTime: '2026-06-04 16:00', auditOpinion: '购机补贴权益质押完整，同意发放', auditor: AUDITOR,
        signedTime: '2026-06-06 11:08', loanTime: '2026-06-15 09:12',
        settleTime: '', repayAmount: 0
      }, [
        L('2026-06-02 10:21', '提交申请', ME, '申请叠加农机购置补贴'),
        L('2026-06-04 16:00', '初审通过', AUDITOR, '购机补贴权益质押完整，同意发放'),
        L('2026-06-06 11:08', '签署合同', ME, '线上签署借款合同'),
        L('2026-06-15 09:12', '放款成功', '系统', '贷款资金已发放至借款人账户')
      ]),
      /* ---------- 张大山：已签约，等待放款 ---------- */
      app({
        id: 'SQ20260726005', productId: 'P001', productName: '惠农e贷·春耕贷',
        applicantId: 'U001', applicantName: ME, phone: '138****6721',
        amount: 100000, term: 12, purpose: '秋季茶园管护与采工工资周转',
        creditScore: 762, status: STATUS.SIGNED, rate: 3.65,
        applyTime: '2026-07-18 09:30', updateTime: '2026-07-26 14:20',
        auditTime: '2026-07-20 15:18', auditOpinion: '用途合规、还款来源明确，同意发放', auditor: AUDITOR,
        signedTime: '2026-07-26 14:20', loanTime: '',
        settleTime: '', repayAmount: 0
      }, [
        L('2026-07-18 09:30', '提交申请', ME, '线上提交'),
        L('2026-07-20 15:18', '初审通过', AUDITOR, '用途合规、还款来源明确，同意发放'),
        L('2026-07-26 14:20', '签署合同', ME, '线上签署借款合同，等待放款')
      ]),
      /* ---------- 张大山：初审通过，等待签约 ---------- */
      app({
        id: 'SQ20260812006', productId: 'P006', productName: '特色养殖扶持贷',
        applicantId: 'U001', applicantName: ME, phone: '138****6721',
        amount: 60000, term: 18, purpose: '茶园套养生态鸡舍扩建',
        creditScore: 762, status: STATUS.FIRST_PASS, rate: 3.55,
        applyTime: '2026-08-10 14:12', updateTime: '2026-08-12 15:06',
        auditTime: '2026-08-12 15:06', auditOpinion: '配套保险保单齐备，同意初审通过', auditor: AUDITOR,
        signedTime: '', loanTime: '', settleTime: '', repayAmount: 0
      }, [
        L('2026-08-10 14:12', '提交申请', ME, '活体抵押 + 保单质押'),
        L('2026-08-12 15:06', '初审通过', AUDITOR, '配套保险保单齐备，同意初审通过')
      ]),
      /* ---------- 张大山：已拒绝（终态分支） ---------- */
      app({
        id: 'SQ20260830007', productId: 'P004', productName: '农户小额信用贷',
        applicantId: 'U001', applicantName: ME, phone: '138****6721',
        amount: 30000, term: 6, purpose: '茶园防霜冻设施采购',
        creditScore: 762, status: STATUS.REJECTED, rate: 4.35,
        applyTime: '2026-08-28 10:02', updateTime: '2026-08-31 09:41',
        auditTime: '2026-08-31 09:41',
        auditOpinion: '近 90 天征信查询次数偏多，暂不符合当前准入条件，建议 3 个月后重新申请',
        auditor: AUDITOR, signedTime: '', loanTime: '', settleTime: '', repayAmount: 0
      }, [
        L('2026-08-28 10:02', '提交申请', ME, '线上提交'),
        L('2026-08-31 09:41', '已拒绝', AUDITOR, '近 90 天征信查询次数偏多，暂不符合准入条件')
      ]),
      /* ---------- 张大山：新提交待初审（保持首单可操作闭环） ---------- */
      app({
        id: 'SQ20260901001', productId: 'P001', productName: '惠农e贷·春耕贷',
        applicantId: 'U001', applicantName: ME, phone: '138****6721',
        amount: 80000, term: 12, purpose: '购买春耕化肥、茶苗及支付管护人工费',
        creditScore: 762, status: STATUS.PENDING_FIRST, rate: 3.65,
        applyTime: '2026-09-01 09:12', updateTime: '2026-09-01 09:12'
      }, [
        L('2026-09-01 09:12', '提交申请', ME, '线上提交，资料齐全')
      ]),
      app({
        id: 'SQ20260903002', productId: 'P004', productName: '农户小额信用贷',
        applicantId: 'U001', applicantName: ME, phone: '138****6721',
        amount: 30000, term: 6, purpose: '茶园防霜冻设施采购',
        creditScore: 762, status: STATUS.PENDING_FIRST, rate: 4.35,
        applyTime: '2026-09-03 14:35', updateTime: '2026-09-03 14:35'
      }, [
        L('2026-09-03 14:35', '提交申请', ME, '整村授信白名单客户')
      ]),
      /* ---------- 湄潭雲雾茶业专业合作社：多个在途阶段 ---------- */
      app({
        id: 'SQ20260610005', productId: 'P002', productName: '农机购置专项贷',
        applicantId: 'U002', applicantName: '湄潭雲雾茶业专业合作社', phone: '0851-2876****',
        amount: 260000, term: 36, purpose: '购置茶叶加工生产线与冷链转运车',
        creditScore: 805, status: STATUS.LOANED, rate: 3.85,
        applyTime: '2026-05-20 09:15', updateTime: '2026-06-10 09:40',
        auditTime: '2026-05-24 11:26', auditOpinion: '设备购置凭证齐全，同意发放', auditor: AUDITOR,
        signedTime: '2026-06-01 15:03', loanTime: '2026-06-10 09:40',
        settleTime: '', repayAmount: 0
      }, [
        L('2026-05-20 09:15', '提交申请', COOP, '附设备购置清单'),
        L('2026-05-24 11:26', '初审通过', AUDITOR, '设备购置凭证齐全，同意发放'),
        L('2026-06-01 15:03', '签署合同', COOP, '法定代表人线上签署'),
        L('2026-06-10 09:40', '放款成功', '系统', '贷款资金已发放至对公账户')
      ]),
      app({
        id: 'SQ20260721007', productId: 'P003', productName: '乡村振兴产业贷',
        applicantId: 'U002', applicantName: '湄潭雲雾茶业专业合作社', phone: '0851-2876****',
        amount: 400000, term: 60, purpose: '茶园基地提质改造与区域品牌建设',
        creditScore: 805, status: STATUS.FIRST_PASS, rate: 4.15,
        applyTime: '2026-07-19 10:30', updateTime: '2026-07-22 10:30',
        auditTime: '2026-07-22 10:30', auditOpinion: '政银担分险方案落地，同意初审通过', auditor: AUDITOR,
        signedTime: '', loanTime: '', settleTime: '', repayAmount: 0
      }, [
        L('2026-07-19 10:30', '提交申请', COOP, '农业担保公司增信'),
        L('2026-07-22 10:30', '初审通过', AUDITOR, '政银担分险方案落地，同意初审通过')
      ]),
      app({
        id: 'SQ20260805009', productId: 'P005', productName: '冷链仓储建设贷',
        applicantId: 'U002', applicantName: '湄潭雲雾茶业专业合作社', phone: '0851-2876****',
        amount: 300000, term: 36, purpose: '茶叶冷链仓储设施扩建工程',
        creditScore: 805, status: STATUS.SIGNED, rate: 3.95,
        applyTime: '2026-07-28 08:45', updateTime: '2026-08-05 16:22',
        auditTime: '2026-08-01 10:12', auditOpinion: '仓单质押方案可行，同意发放', auditor: AUDITOR,
        signedTime: '2026-08-05 16:22', loanTime: '', settleTime: '', repayAmount: 0
      }, [
        L('2026-07-28 08:45', '提交申请', COOP, '附仓储建设可行性报告'),
        L('2026-08-01 10:12', '初审通过', AUDITOR, '仓单质押方案可行，同意发放'),
        L('2026-08-05 16:22', '签署合同', COOP, '法定代表人线上签署，等待放款')
      ]),
      app({
        id: 'SQ20260814008', productId: 'P003', productName: '乡村振兴产业贷',
        applicantId: 'U002', applicantName: '湄潭雲雾茶业专业合作社', phone: '0851-2876****',
        amount: 500000, term: 60, purpose: '夏秋茶生产流动资金补充',
        creditScore: 805, status: STATUS.FIRST_PASS, rate: 4.15,
        applyTime: '2026-08-12 13:20', updateTime: '2026-08-14 11:08',
        auditTime: '2026-08-14 11:08', auditOpinion: '订单充足，还款来源明确，同意初审通过', auditor: AUDITOR,
        signedTime: '', loanTime: '', settleTime: '', repayAmount: 0
      }, [
        L('2026-08-12 13:20', '提交申请', COOP, '茶青订单质押'),
        L('2026-08-14 11:08', '初审通过', AUDITOR, '订单充足，还款来源明确，同意初审通过')
      ]),
      app({
        id: 'SQ20260905003', productId: 'P006', productName: '特色养殖扶持贷',
        applicantId: 'U002', applicantName: '湄潭雲雾茶业专业合作社', phone: '0851-2876****',
        amount: 260000, term: 18, purpose: '茶园配套生态养鸡场扩建',
        creditScore: 805, status: STATUS.PENDING_FIRST, rate: 3.55,
        applyTime: '2026-09-05 10:08', updateTime: '2026-09-05 10:08'
      }, [
        L('2026-09-05 10:08', '提交申请', COOP, '附活体抵押清单')
      ])
    ];
  }

  /** 信用积分变动日志（假数据，仅用于积分页展示） */
  function seedCreditLogs() {
    return [
      { id: 'C006', date: '2026-09-01', event: '春耕贷按时还款 3 期', delta: 8, score: 762, type: 'repay' },
      { id: 'C005', date: '2026-07-18', event: '完成土地确权信息补录', delta: 5, score: 754, type: 'asset' },
      { id: 'C004', date: '2026-06-02', event: '纳入整村授信白名单', delta: 12, score: 749, type: 'policy' },
      { id: 'C003', date: '2026-04-26', event: '购买农业保险（茶叶气象指数险）', delta: 6, score: 737, type: 'business' },
      { id: 'C002', date: '2026-03-11', event: '上年度贷款提前结清', delta: 15, score: 731, type: 'repay' },
      { id: 'C001', date: '2026-01-09', event: '首次建立涉农信用档案', delta: 60, score: 716, type: 'record' }
    ];
  }

  /** 组装完整数据池 */
  function createSeedDB() {
    return {
      version: CONST.DB_VERSION,
      products: seedProducts(),
      news: seedNews(),
      users: seedUsers(),
      applications: seedApplications(),
      creditLogs: seedCreditLogs(),
      updateTime: '2026-09-05 10:08'
    };
  }

  /** 兜底：localStorage 不可用时降级为内存存储，避免整站白屏 */
  var memoryStore = null;

  function rawGet() {
    try {
      return global.localStorage.getItem(KEY);
    } catch (e) {
      return memoryStore;
    }
  }

  function rawSet(value) {
    try {
      global.localStorage.setItem(KEY, value);
    } catch (e) {
      memoryStore = value;
    }
  }

  /**
   * 把本次版本新增的阶段化种子申请单合并进已有数据（按 id 去重）。
   * 用户已推进过的申请（状态可能已变化）不会被覆盖，只补充缺失的新记录，
   * 从而在升级后自动看到更丰富的"各阶段贷款数据"。
   */
  function mergeSeedApplications(list) {
    var seed = seedApplications();
    var seen = {};
    var i;
    for (i = 0; i < list.length; i++) seen[list[i].id] = 1;
    for (i = 0; i < seed.length; i++) {
      if (!seen[seed[i].id]) {
        list.push(JSON.parse(JSON.stringify(seed[i])));
      }
    }
    return list;
  }

  /** 版本迁移：为旧版本数据补齐新增字段，避免升级后页面读到 undefined */
  function migrate(db) {
    var i;
    var users = db.users || [];
    for (i = 0; i < users.length; i++) {
      if (typeof users[i].balance !== 'number') users[i].balance = 0;
    }
    var list = db.applications || [];
    var extra = ['auditTime', 'auditOpinion', 'auditor', 'signedTime', 'loanTime', 'settleTime'];
    for (i = 0; i < list.length; i++) {
      var item = list[i];
      for (var k = 0; k < extra.length; k++) {
        if (typeof item[extra[k]] !== 'string') item[extra[k]] = '';
      }
      if (typeof item.rate !== 'number') item.rate = 0;
      if (typeof item.repayAmount !== 'number') item.repayAmount = 0;
      if (!item.logs) item.logs = [];
    }
    // v3 起：补入各生命周期阶段的种子申请单，让进度页 / 审批端数据更丰富
    if (!db.applications) db.applications = [];
    db.applications = mergeSeedApplications(db.applications);
    if (!db.creditLogs) db.creditLogs = [];
    db.version = CONST.DB_VERSION;
    return db;
  }

  /**
   * 后端数据池缓存
   *
   * 接入后端后，页面数据统一由 API.bootstrap() 从服务端拉取并写入此缓存；
   * DB.getDB() 仍保持同步返回，因此页面里 `var db = DB.getDB()` 的写法完全不用改。
   * 后端不可用时自动回退到本地种子数据（离线演示）。
   */
  var cachePool = null;

  /**
   * 写入后端数据池（由 js/api.js 在 bootstrap 完成后调用）
   * @param {Object} pool 与 createSeedDB() 同构的数据池
   */
  function setCache(pool) {
    cachePool = pool || null;
    return cachePool;
  }

  /** 是否已接入后端数据 */
  function hasCache() {
    return !!cachePool;
  }

  /**
   * 获取数据库（唯一读入口）
   * 优先返回后端数据池；未接入后端时回退到本地种子数据
   * @returns {Object} 完整数据池对象
   */
  function getDB() {
    if (cachePool) {
      return cachePool;
    }
    var raw = rawGet();
    if (raw) {
      try {
        var db = JSON.parse(raw);
        if (db && db.products && db.applications) {
          // 版本不一致时先迁移再返回，保证老数据不丢
          return db.version === CONST.DB_VERSION ? db : saveDB(migrate(db));
        }
      } catch (e) {
        // 数据被污染，直接重建
      }
    }
    var seed = createSeedDB();
    saveDB(seed);
    return seed;
  }

  /**
   * 保存数据库（本地离线兜底写入口）
   * 接入后端后，增删改一律通过 API 提交，本方法仅同步内存缓存，
   * 保证页面在等待接口返回期间的数据一致。
   * @param {Object} db 完整数据池对象
   * @returns {Object} 保存后的数据池
   */
  function saveDB(db) {
    if (!db) return getDB();
    // 时间格式化统一使用工具方法，避免多处重复实现
    db.updateTime = global.U && global.U.nowText ? global.U.nowText() : '';
    if (!cachePool) {
      rawSet(JSON.stringify(db));
    } else {
      cachePool = db;
    }
    return db;
  }

  /** 教学/调试用：清空并重新灌入种子数据 */
  function resetDB() {
    try {
      global.localStorage.removeItem(KEY);
    } catch (e) {
      memoryStore = null;
    }
    return getDB();
  }

  global.DB = {
    getDB: getDB,
    saveDB: saveDB,
    resetDB: resetDB,
    setCache: setCache,
    hasCache: hasCache
  };
})(window);
