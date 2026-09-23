/**
 * 贷款产品展示页（后端 /api/products）
 *
 * 关键点：
 * 1. 卡片通过 data-product-id 记录产品编号，申请表单与审批端以此为关联依据；
 * 2. canApply / applyTip 由后端按当前用户信用等级计算：
 *    等级不足的产品按钮置灰并给出原因（无权限的功能前端不做展示 / 不可点）。
 */
(function (global) {
  'use strict';

  var CONST = global.APP_CONST;

  var ICON_COLORS = {
    P001: '#e7f6ee',
    P002: '#e8f1fc',
    P003: '#fff5e6',
    P004: '#fdeceb',
    P005: '#eef1fb',
    P006: '#effaf1'
  };

  global.App.mountApp({
    guard: 'any',
    data: function () {
      var db = global.DB.getDB();
      return {
        U: global.U,
        db: db,
        user: global.U.currentUser(db),
        activeType: '全部',
        activeTerm: '',
        sortKey: 'rate',
        detailVisible: false,
        detail: {},
        typeOptions: ['全部', CONST.PRODUCT_TYPE.CREDIT, CONST.PRODUCT_TYPE.MORTGAGE, CONST.PRODUCT_TYPE.POLICY],
        termOptions: [
          { text: '不限', value: '' },
          { text: '6 个月', value: 6 },
          { text: '12 个月', value: 12 },
          { text: '24 个月', value: 24 },
          { text: '36 个月', value: 36 }
        ],
        sortOptions: [
          { text: '利率最低', value: 'rate' },
          { text: '额度最高', value: 'amount' },
          { text: '期限最短', value: 'term' }
        ]
      };
    },
    computed: {
      filteredProducts: function () {
        var that = this;
        var list = this.db.products.filter(function (p) {
          var typeOk = that.activeType === '全部' || p.type === that.activeType;
          var termOk = !that.activeTerm || (p.terms || []).indexOf(Number(that.activeTerm)) > -1;
          return typeOk && termOk;
        });
        return list.sort(function (a, b) {
          if (that.sortKey === 'amount') return b.maxAmount - a.maxAmount;
          if (that.sortKey === 'term') return a.termMonths - b.termMonths;
          return a.rate - b.rate;
        });
      },
      /** 未登录提示条 */
      guestTip: function () {
        return !this.user;
      }
    },
    methods: {
      iconColor: function (id) {
        return ICON_COLORS[id] || '#e7f6ee';
      },
      resetFilter: function () {
        this.activeType = '全部';
        this.activeTerm = '';
        this.sortKey = 'rate';
      },
      canApply: function (product) {
        return product.canApply !== false;
      },
      /** 事件委托：统一监听卡片点击，data-action 区分"详情"与"申请" */
      onGridClick: function (e) {
        var card = e.target.closest('[data-product-id]');
        if (!card) return;
        var productId = card.dataset.productId;
        var btn = e.target.closest('[data-action]');
        var action = btn ? btn.dataset.action : 'detail';
        if (action === 'apply') this.onApply(productId);
        else this.openDetail(productId);
      },
      openDetail: function (productId) {
        var p = global.U.findProduct(this.db, productId);
        if (!p) return;
        this.detail = p;
        this.detailVisible = true;
      },
      /** 选中产品：写入 sessionStorage，供申请表单读取 */
      onApply: function (productId) {
        var p = global.U.findProduct(this.db, productId);
        if (!p) return;
        if (p.canApply === false) {
          global.U.toast(p.applyTip || '当前信用等级暂不符合该产品的准入条件', 'warn');
          return;
        }
        if (!this.user) {
          global.U.toast('请先登录后再申请贷款', 'warn');
          setTimeout(function () {
            location.href = 'login.html?redirect=' + encodeURIComponent('products.html');
          }, 600);
          return;
        }
        try {
          sessionStorage.setItem(CONST.SESSION_KEYS.SELECTED_PRODUCT, productId);
        } catch (err) {
          /* 忽略存储异常 */
        }
        this.detailVisible = false;
        global.U.toast('已选择【' + p.name + '】，正在前往申请表单', 'success');
        setTimeout(function () {
          location.href = 'apply.html';
        }, 400);
      }
    }
  });
})(window);
