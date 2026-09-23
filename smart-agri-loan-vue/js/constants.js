/**
 * ============================================================
 *  智能惠农信贷系统 —— 全局常量文件
 * ------------------------------------------------------------
 *  约定（团队开会定死的规矩，后续任何人不得违反）：
 *  1. 本地存储数据池只有一个主键 STORAGE_KEY，所有数据都挂在它下面；
 *  2. 申请单状态只允许引用 APPLY_STATUS 中的常量，禁止在页面里
 *     手写 "待初审" / "已放款" 这类字符串，否则后台改了状态，
 *     用户端读不到，两端联动直接断掉；
 *  3. 所有字典项（角色、产品类型、资讯分类、信用等级）同样只在此定义。
 * ============================================================
 */
(function (global) {
  'use strict';

  /** 本地存储数据池主键（唯一入口，全项目只此一份） */
  var STORAGE_KEY = 'SMART_AGRI_LOAN_DB_V1';

  /** 数据池版本号，种子数据结构升级时 +1
   *  v2：用户账户余额、签约/放款/结清时间
   *  v3：申请单种子数据扩充至"待初审→已结清/已拒绝"全阶段，供申请进度页与审批端展示
   */
  var DB_VERSION = 3;

  /**
   * 申请单状态常量（值为最终落库的中文状态值）
   * 使用方式：APPLY_STATUS.PENDING_FIRST
   */
  var APPLY_STATUS = {
    PENDING_FIRST: '待初审', // 用户提交申请，等待金融机构初审
    FIRST_PASS: '初审通过', // 初审通过，等待签约
    SIGNED: '已签约', // 合同已签署，等待放款
    LOANED: '已放款', // 资金已到账，还款中
    SETTLED: '已结清', // 本息结清，流程结束
    REJECTED: '已拒绝' // 审核未通过，流程终止
  };

  /** 状态展示元数据：颜色主题 + 说明（供标签、进度条共用） */
  var STATUS_META = {};
  STATUS_META[APPLY_STATUS.PENDING_FIRST] = { type: 'warn', desc: '资料已提交，等待金融机构初审' };
  STATUS_META[APPLY_STATUS.FIRST_PASS] = { type: 'primary', desc: '初审已通过，请尽快完成签约' };
  STATUS_META[APPLY_STATUS.SIGNED] = { type: 'info', desc: '合同已签署，等待银行放款' };
  STATUS_META[APPLY_STATUS.LOANED] = { type: 'success', desc: '贷款已发放，请按期还款' };
  STATUS_META[APPLY_STATUS.SETTLED] = { type: 'muted', desc: '本息已结清，感谢您的信任' };
  STATUS_META[APPLY_STATUS.REJECTED] = { type: 'danger', desc: '很遗憾，本次申请未通过审核' };

  /** 正常业务流转链路（不含"已拒绝"这个终态分支） */
  var STATUS_FLOW = [
    APPLY_STATUS.PENDING_FIRST,
    APPLY_STATUS.FIRST_PASS,
    APPLY_STATUS.SIGNED,
    APPLY_STATUS.LOANED,
    APPLY_STATUS.SETTLED
  ];

  /** 所有状态（含终态），供筛选下拉使用 */
  var STATUS_LIST = STATUS_FLOW.concat([APPLY_STATUS.REJECTED]);

  /**
   * 用户端四步进度条：每一步对应一个状态，并指定亮灯颜色
   * orange 待初审 / blue 初审通过 / cyan 已签约 / green 已放款
   */
  var PROGRESS_STEPS = [
    { status: APPLY_STATUS.PENDING_FIRST, label: '提交申请', tip: '资料已提交，等待金融机构初审', color: 'orange' },
    { status: APPLY_STATUS.FIRST_PASS, label: '初审通过', tip: '初审已通过，请尽快完成线上签约', color: 'blue' },
    { status: APPLY_STATUS.SIGNED, label: '签署合同', tip: '合同已签署，等待银行放款', color: 'cyan' },
    { status: APPLY_STATUS.LOANED, label: '放款到账', tip: '贷款已发放，请按期还款', color: 'green' }
  ];

  /** 用户角色 */
  var USER_ROLE = {
    FARMER: '农户',
    ENTERPRISE: '农业企业',
    BANK_STAFF: '审批人员'
  };

  /** 贷款产品类型 */
  var PRODUCT_TYPE = {
    CREDIT: '信用贷',
    MORTGAGE: '抵押贷',
    POLICY: '政策贷'
  };

  /** 资讯分类 */
  var NEWS_CATEGORY = {
    POLICY: '政策解读',
    KNOWLEDGE: '金融知识',
    INDUSTRY: '行业动态'
  };

  /** 信用等级（与积分区间挂钩） */
  var CREDIT_LEVEL = [
    { level: 'AAA', min: 850, max: 950 },
    { level: 'AA', min: 750, max: 849 },
    { level: 'A', min: 650, max: 749 },
    { level: 'B', min: 550, max: 649 },
    { level: 'C', min: 0, max: 549 }
  ];

  /** 表单校验正则（统一维护，页面不得自行拼接正则） */
  var REGEX = {
    ID_CARD: /^[1-9]\d{5}(?:19|20)\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/,
    PHONE: /^1[3-9]\d{9}$/,
    AMOUNT: /^[1-9]\d*$/,
    NAME: /^[\u4e00-\u9fa5]{2,20}$|^[a-zA-Z\s]{2,40}$/
  };

  /** 风控风险等级（红黄绿三档，按信用积分划分） */
  var RISK_LEVEL = {
    LOW: { key: 'LOW', label: '低风险', color: 'green', type: 'success', min: 750 },
    MID: { key: 'MID', label: '中风险', color: 'yellow', type: 'warn', min: 650 },
    HIGH: { key: 'HIGH', label: '高风险', color: 'red', type: 'danger', min: 0 }
  };

  var RISK_LIST = [RISK_LEVEL.LOW, RISK_LEVEL.MID, RISK_LEVEL.HIGH];

  /** 信用积分雷达图五个评估维度 */
  var CREDIT_DIMENSIONS = [
    { key: 'repay', label: '履约能力' },
    { key: 'business', label: '经营稳定' },
    { key: 'asset', label: '资产状况' },
    { key: 'record', label: '信用记录' },
    { key: 'policy', label: '政策匹配' }
  ];

  /** 本地存储其它辅助键（非数据池，仅用于页面间临时传参） */
  var SESSION_KEYS = {
    SELECTED_PRODUCT: 'SMART_AGRI_LOAN_SELECTED_PRODUCT'
  };

  global.APP_CONST = {
    STORAGE_KEY: STORAGE_KEY,
    DB_VERSION: DB_VERSION,
    APPLY_STATUS: APPLY_STATUS,
    STATUS_META: STATUS_META,
    STATUS_FLOW: STATUS_FLOW,
    STATUS_LIST: STATUS_LIST,
    PROGRESS_STEPS: PROGRESS_STEPS,
    USER_ROLE: USER_ROLE,
    PRODUCT_TYPE: PRODUCT_TYPE,
    NEWS_CATEGORY: NEWS_CATEGORY,
    CREDIT_LEVEL: CREDIT_LEVEL,
    CREDIT_DIMENSIONS: CREDIT_DIMENSIONS,
    REGEX: REGEX,
    RISK_LEVEL: RISK_LEVEL,
    RISK_LIST: RISK_LIST,
    SESSION_KEYS: SESSION_KEYS
  };
})(window);
