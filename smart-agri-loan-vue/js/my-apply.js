/**
 * 我的申请（业务进度）：四步亮灯进度条 + 签约 / 放款 / 结清闭环
 *
 * 对接后端：
 *  - 列表：GET /api/applications（后端按角色只返回本人的单子）
 *  - 签约：POST /api/applications/{id}/sign
 *  - 放款：POST /api/applications/{id}/loan（后端自动把资金计入账户余额）
 *  - 结清：POST /api/applications/{id}/settle（后端校验余额、扣本息、信用 +5）
 *  - 充值：POST /api/account/recharge
 *  - 撤回：DELETE /api/applications/{id}（仅待初审的申请，本人可撤回）
 */
(function (global) {
  'use strict';

  var CONST = global.APP_CONST;
  var STATUS = CONST.APPLY_STATUS;
  var RECHARGE_AMOUNT = 10000;

  global.App.mountApp({
    guard: 'applicant',
    needApplications: true,
    data: function () {
      var db = global.DB.getDB();
      return {
        U: global.U,
        STATUS: STATUS,
        db: db,
        user: global.U.currentUser(db),
        activeStatus: '',
        onlyMine: true,
        expanded: '',
        focusId: new URLSearchParams(location.search).get('id') || '',
        statusList: CONST.STATUS_LIST,
        flowSteps: CONST.PROGRESS_STEPS,
        errorMap: {},
        acting: ''
      };
    },
    computed: {
      myList: function () {
        var that = this;
        return this.db.applications.filter(function (item) {
          return !that.onlyMine || !that.user || item.applicantId === that.user.id;
        });
      },
      filteredList: function () {
        var that = this;
        if (!this.activeStatus) return this.myList;
        return this.myList.filter(function (item) {
          return item.status === that.activeStatus;
        });
      },
      processingCount: function () {
        var that = this;
        return this.myList.filter(function (item) {
          return [that.STATUS.PENDING_FIRST, that.STATUS.FIRST_PASS, that.STATUS.SIGNED].indexOf(item.status) > -1;
        }).length;
      },
      loanedCount: function () {
        var that = this;
        return this.myList.filter(function (item) {
          return item.status === that.STATUS.LOANED || item.status === that.STATUS.SETTLED;
        }).length;
      }
    },
    methods: {
      countOf: function (status) {
        return global.U.countByStatus(this.myList, status);
      },
      isMine: function (item) {
        return !this.user || item.applicantId === this.user.id;
      },
      toggleLogs: function (id) {
        this.expanded = this.expanded === id ? '' : id;
      },
      repayTotalOf: function (item) {
        // 后端已算好应还本息，直接使用；兜底本地公式
        return item.repayTotal != null
          ? Number(item.repayTotal)
          : global.U.repayTotal(item.amount, item.rate, item.term);
      },
      /* ---------- 进度条 ---------- */
      stepIndex: function (item) {
        if (item.status === STATUS.SETTLED) return CONST.PROGRESS_STEPS.length;
        for (var i = 0; i < CONST.PROGRESS_STEPS.length; i++) {
          if (CONST.PROGRESS_STEPS[i].status === item.status) return i;
        }
        return 0;
      },
      flowState: function (item, index) {
        var current = this.stepIndex(item);
        if (index < current) return 'done';
        return index === current ? 'current' : 'todo';
      },
      flowTip: function (item) {
        var current = this.stepIndex(item);
        if (item.status === STATUS.SETTLED) return '贷款已结清，感谢您的信任';
        var step = CONST.PROGRESS_STEPS[current];
        return step ? step.tip : '';
      },
      setError: function (id, message) {
        if (message) this.errorMap[id] = message;
        else delete this.errorMap[id];
      },
      /** 操作成功后重新拉取列表与用户信息（余额、积分实时更新） */
      reload: function () {
        var that = this;
        return Promise.all([global.API.applications(), global.API.me()]).then(function (result) {
          that.db.applications = result[0] || [];
          that.user = result[1];
          that.db.users = [result[1]];
        });
      },
      /* ---------- 闭环操作（全部走后端接口） ---------- */
      doSign: function (item) {
        var that = this;
        if (this.acting) return;
        this.acting = item.id;
        global.API.sign(item.id).then(function () {
          that.acting = '';
          that.setError(item.id, '');
          global.U.toast('签约成功，等待放款', 'success');
          return that.reload();
        }).catch(function (error) {
          that.acting = '';
          that.setError(item.id, error.message);
        });
      },
      doLoan: function (item) {
        var that = this;
        if (this.acting) return;
        this.acting = item.id;
        global.API.loan(item.id).then(function () {
          that.acting = '';
          that.setError(item.id, '');
          global.U.toast('放款成功，资金已进入账户余额', 'success');
          return that.reload();
        }).catch(function (error) {
          that.acting = '';
          that.setError(item.id, error.message);
        });
      },
      doRepay: function (item) {
        var that = this;
        if (this.acting) return;
        this.acting = item.id;
        global.API.settle(item.id).then(function () {
          that.acting = '';
          that.setError(item.id, '');
          global.U.toast('还款成功，信用积分 +5', 'success');
          return that.reload();
        }).catch(function (error) {
          that.acting = '';
          that.setError(item.id, error.message);
        });
      },
      /** 撤回申请：删除本人尚未进入审批流程（待初审）的申请单 */
      doWithdraw: function (item) {
        var that = this;
        if (this.acting) return;
        if (global.confirm && !global.confirm('确认撤回编号 ' + item.id + ' 的申请？撤回后不可恢复。')) return;
        this.acting = item.id;
        global.API.deleteApplication(item.id).then(function () {
          that.acting = '';
          that.setError(item.id, '');
          global.U.toast('已撤回申请 ' + item.id, 'success');
          return that.reload();
        }).catch(function (error) {
          that.acting = '';
          that.setError(item.id, error.message);
        });
      },
      /** 演示用：账户充值，便于构造余额不足 / 充足的还款场景 */
      recharge: function () {
        var that = this;
        global.API.recharge(RECHARGE_AMOUNT).then(function (data) {
          global.U.toast('已充值 ' + global.U.thousand(RECHARGE_AMOUNT) + ' 元，当前余额 ' +
            global.U.money(data.balance) + ' 元', 'success');
          return that.reload();
        });
      }
    }
  });
})(window);
