/**
 * 信用积分展示页
 *
 * 对接后端：数据全部来自 GET /api/credit/me
 *  - 积分、等级、评价：creditInfo.score / level / comment
 *  - 五维画像：creditInfo.dimensions（后端按用户档案下发，不再是演示死值）
 *  - 积分变动日志：creditInfo.logs（已由 db 池注入 db.creditLogs）
 *  - 我的信用权益：creditInfo.benefits（按等级差异化下发）
 *  - 授信政策：creditInfo.policy（最高纯信用额度 / 利率下浮 / 审批时效 / 是否需担保）
 */
(function (global) {
  'use strict';

  /* ---------- 图形计算 ---------- */
  var SCORE_MIN = 350; // 仪表盘最小刻度（后端也会下发，作为兜底）
  var SCORE_MAX = 950;
  var CX = 150;
  var CY = 125;
  var R = 72;
  var GAUGE_LENGTH = Math.PI * 90;

  function polar(radius, angleDeg) {
    var rad = (angleDeg * Math.PI) / 180;
    return {
      x: CX + radius * Math.cos(rad),
      y: CY + radius * Math.sin(rad)
    };
  }

  function angleOf(index, total) {
    return -90 + (360 / total) * index;
  }

  global.App.mountApp({
    guard: 'applicant',
    needCredit: true,
    data: function () {
      var db = global.DB.getDB();
      var user = global.U.currentUser(db);
      var info = db.creditInfo || {};
      var dimensions = (info.dimensions && info.dimensions.length)
        ? info.dimensions
        : global.APP_CONST.CREDIT_DIMENSIONS.map(function (d) {
            return { key: d.key, label: d.label, value: 60 };
          });

      return {
        U: global.U,
        db: db,
        user: user,
        info: info,
        creditScore: info.score != null ? info.score : (user ? user.creditScore : 0),
        scoreMin: info.scoreMin != null ? info.scoreMin : SCORE_MIN,
        scoreMax: info.scoreMax != null ? info.scoreMax : SCORE_MAX,
        gaugeReady: false,
        dimensions: dimensions,
        /* 信用积分用途与阈值详情弹窗开关（卡片内入口按钮触发） */
        rulesVisible: false,
        uses: [
          { t: '授信额度评估', d: '银行核定贷款额度范围的核心依据，积分越高额度越高' },
          { t: '利率差异化定价', d: '信用等级 AA 及以上可享首贷利率下浮优惠' },
          { t: '极速审批通道', d: '信用达标自动进入绿色审批队列，最快 1 天放款' },
          { t: '纯信用贷款准入', d: '积分达标即可免抵押申请信用贷，无需提供抵押物' },
          { t: '续贷与增信支持', d: '结清贷款加分提升，无还本续贷、担保增信评估更易通过' }
        ],
        levels: global.APP_CONST.CREDIT_LEVEL.map(function (lv) {
          var desc = '';
          if (lv.level === 'AAA') desc = '信用极好，可获最高 50 万纯信用额度与最优利率';
          else if (lv.level === 'AA') desc = '信用良好，最高 30 万额度并享利率优惠';
          else if (lv.level === 'A') desc = '信用一般，额度适中，建议补充经营材料提额';
          else if (lv.level === 'B') desc = '信用待提升，建议先修复征信记录再申请';
          else desc = '暂不满足纯信用授信条件，建议补充担保增信';
          return { level: lv.level, min: lv.min, max: lv.max, desc: desc };
        }),
        tips: [
          { no: '01', title: '保持按时还款', desc: '每按时归还一期贷款，履约能力维度稳定提升', score: 8 },
          { no: '02', title: '完善土地确权信息', desc: '补充土地承包经营权证，资产状况维度加分', score: 5 },
          { no: '03', title: '购买农业保险', desc: '投保政策性农业保险，降低经营风险评级', score: 6 },
          { no: '04', title: '申报新型经营主体', desc: '认定为家庭农场或合作社，享受政策匹配加分', score: 10 }
        ]
      };
    },
    computed: {
      /** 后端返回的授信政策（自适应核心数据） */
      policy: function () {
        return this.info.policy || {};
      },
      rights: function () {
        // 权益由后端按信用等级下发，前端只负责展示
        return this.info.benefits || [];
      },
      /** 距离下一等级提示 */
      levelTip: function () {
        if (!this.info.nextLevel) return '已是最高信用等级，保持良好履约记录即可长期享受最优政策';
        return '距离 ' + this.info.nextLevel + ' 级还差 ' + this.info.toNextLevel + ' 分';
      },
      maxCreditText: function () {
        var amount = this.policy.maxPureCreditAmount;
        if (amount == null) return '-';
        return Number(amount) === 0 ? '暂不支持纯信用授信' : global.U.toWan(amount) + ' 元';
      },
      creditLevel: function () {
        return this.info.level || global.U.levelOf(this.creditScore);
      },
      creditComment: function () {
        return this.info.comment || '';
      },
      dashArray: function () {
        return GAUGE_LENGTH;
      },
      dashOffset: function () {
        if (!this.gaugeReady) return GAUGE_LENGTH;
        var ratio = (this.creditScore - this.scoreMin) / (this.scoreMax - this.scoreMin);
        ratio = Math.max(0, Math.min(1, ratio));
        return GAUGE_LENGTH * (1 - ratio);
      },
      radarRings: function () {
        var that = this;
        return [1, 0.66, 0.33].map(function (scale) {
          return that.dimensions
            .map(function (d, i) {
              return that.pointAt(R * scale, i);
            })
            .join(' ');
        });
      },
      radarAxes: function () {
        var that = this;
        var total = this.dimensions.length;
        return this.dimensions.map(function (d, i) {
          var angle = angleOf(i, total);
          var end = polar(R, angle);
          var label = polar(R + 20, angle);
          return {
            label: d.label,
            x1: CX,
            y1: CY,
            x2: end.x.toFixed(1),
            y2: end.y.toFixed(1),
            lx: label.x.toFixed(1),
            ly: (label.y + 4).toFixed(1),
            anchor: label.x > CX + 6 ? 'start' : label.x < CX - 6 ? 'end' : 'middle'
          };
        });
      },
      radarPoints: function () {
        var that = this;
        return this.dimensions
          .map(function (d, i) {
            return that.pointAt((R * (d.value || 0)) / 100, i);
          })
          .join(' ');
      },
      radarDots: function () {
        var that = this;
        return this.dimensions.map(function (d, i) {
          var p = polar((R * (d.value || 0)) / 100, angleOf(i, that.dimensions.length));
          return { x: p.x.toFixed(1), y: p.y.toFixed(1) };
        });
      },
      /** 积分变动日志：后端按时间正序下发，展示时倒序，让最近一次变动置顶 */
      logsDesc: function () {
        var logs = (this.db && this.db.creditLogs) || [];
        return logs.slice().reverse();
      }
    },
    mounted: function () {
      var that = this;
      setTimeout(function () {
        that.gaugeReady = true;
      }, 80);
    },
    methods: {
      pointAt: function (radius, index) {
        var p = polar(radius, angleOf(index, this.dimensions.length));
        return p.x.toFixed(1) + ',' + p.y.toFixed(1);
      }
    }
  });
})(window);
