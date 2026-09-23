/**
 * 金融机构审批端：按状态分栏、初审通过 / 拒绝、风控看板
 *
 * 对接后端：
 *  - 列表：GET /api/applications（审批员可见全部申请）
 *  - 审批：POST /api/applications/{id}/audit { action: pass|reject, opinion }
 *  - 页面级权限：guard:'bank'，非审批人员访问会被拦截并返回首页
 */
(function (global) {
  'use strict';

  var CONST = global.APP_CONST;
  var STATUS = CONST.APPLY_STATUS;

  var QUICK_OPINIONS = {
    pass: ['资料齐全，符合惠农贷准入条件', '经营稳定，还款来源有保障，同意初审通过', '整村授信白名单客户，自动初审通过'],
    reject: ['申请材料不完整，请补充后重新提交', '征信存在逾期记录，暂不符合准入条件', '经营流水不足以覆盖还款，建议降低额度后再申请']
  };

  global.App.mountApp({
    guard: 'bank',
    needApplications: true,
    data: function () {
      var db = global.DB.getDB();
      return {
        U: global.U,
        STATUS: STATUS,
        db: db,
        user: global.U.currentUser(db),
        activeStatus: STATUS.PENDING_FIRST,
        detailVisible: false,
        detail: {},
        actionVisible: false,
        actionType: 'pass',
        current: {},
        opinion: '',
        opinionError: '',
        submitting: false
      };
    },
    computed: {
      tabs: function () {
        var that = this;
        var list = [{ text: '全部', value: '', count: this.db.applications.length }];
        CONST.STATUS_LIST.forEach(function (status) {
          list.push({
            text: status,
            value: status,
            count: global.U.countByStatus(that.db.applications, status)
          });
        });
        return list;
      },
      filteredList: function () {
        var that = this;
        if (!this.activeStatus) return this.db.applications;
        return this.db.applications.filter(function (item) {
          return item.status === that.activeStatus;
        });
      },
      quickOpinions: function () {
        return QUICK_OPINIONS[this.actionType];
      },
      /** 顶部风控看板 */
      board: function () {
        var list = this.db.applications;
        var pending = global.U.countByStatus(list, STATUS.PENDING_FIRST);
        var passed = global.U.countByStatus(list, STATUS.FIRST_PASS);
        var settled = global.U.countByStatus(list, STATUS.SETTLED);
        var loaned = global.U.countByStatus(list, STATUS.LOANED);
        var signed = global.U.countByStatus(list, STATUS.SIGNED);
        var rejected = global.U.countByStatus(list, STATUS.REJECTED);
        var handled = passed + signed + loaned + settled + rejected;
        var highRisk = list.filter(function (item) {
          return global.U.riskOf(item.creditScore).key === 'HIGH';
        }).length;
        var passRate = handled ? Math.round((handled - rejected) / handled * 100) : 0;
        var riskRatio = list.length ? Math.round((highRisk / list.length) * 100) : 0;
        return [
          { icon: '待', label: '待初审', value: pending, unit: ' 笔', bg: '#fff5e6', color: '#b5760c' },
          { icon: '过', label: '已通过初审', value: passed, unit: ' 笔', bg: '#e7f6ee', color: '#17865a' },
          { icon: '率', label: '审批通过率', value: passRate, unit: ' %', bg: '#e8f1fc', color: '#1f5fa8' },
          { icon: '险', label: '高风险占比', value: riskRatio, unit: ' %', bg: '#fdeceb', color: '#c33c27' }
        ];
      }
    },
    mounted: function () {
      if (new URLSearchParams(location.search).get('nav') === '1') {
        global.U.toast('已进入审批端工作台，请处理待初审申请', 'success');
      }
    },
    methods: {
      /** 申请人信用积分：直接使用申请单上的积分快照（后端已固化） */
      scoreOf: function (item) {
        return item.creditScore || 0;
      },
      riskOf: function (item) {
        return global.U.riskOf(this.scoreOf(item));
      },
      /** 依据信用积分给出建议授信额度 */
      suggestAmount: function (item) {
        var score = this.scoreOf(item);
        if (score >= 850) return 500000;
        if (score >= 750) return 300000;
        if (score >= 650) return 150000;
        return 50000;
      },
      openDetail: function (item) {
        this.detail = item;
        this.detailVisible = true;
      },
      openAction: function (item, type) {
        this.current = item;
        this.actionType = type;
        this.opinion = type === 'pass' ? '资料齐全，符合惠农贷准入条件' : '';
        this.opinionError = '';
        this.actionVisible = true;
      },
      /** 执行审批：调用后端接口，成功后刷新列表 */
      confirmAction: function () {
        if (!this.opinion) {
          this.opinionError = this.actionType === 'reject' ? '拒绝申请必须填写审批意见' : '请填写审批意见';
          return;
        }
        var that = this;
        var item = this.current;
        var pass = this.actionType === 'pass';
        this.submitting = true;
        global.API.audit(item.id, this.actionType, this.opinion).then(function () {
          that.submitting = false;
          that.actionVisible = false;
          global.U.toast('已' + (pass ? '通过' : '拒绝') + '申请 ' + item.id, pass ? 'success' : 'warn');
          return that.reload();
        }).catch(function () {
          that.submitting = false;
        });
      },
      reload: function () {
        var that = this;
        return global.API.applications().then(function (list) {
          that.db.applications = list || [];
        });
      }
    }
  });
})(window);
