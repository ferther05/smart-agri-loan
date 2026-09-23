/**
 * 首页：门户概览
 *
 * 对接后端后：产品、资讯来自 /api/products、/api/news；
 * 顶部横幅按当前登录人的信用等级自适应展示（未登录展示引导注册文案）。
 */
(function (global) {
  'use strict';

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
        steps: [
          { title: '选择产品', desc: '对比利率、额度与期限，挑选合适的惠农贷款产品' },
          { title: '在线申请', desc: '填写金额、期限与用途，上传必要的经营证明材料' },
          { title: '智能审批', desc: '系统结合信用积分与大数据风控模型自动初审' },
          { title: '签约放款', desc: '线上签署合同，资金直达账户，全程进度可查' }
        ]
      };
    },
    computed: {
      /** 首页只展示 3 款热门产品 */
      featuredProducts: function () {
        return this.db.products.slice().sort(function (a, b) {
          return (b.hot ? 1 : 0) - (a.hot ? 1 : 0);
        }).slice(0, 3);
      },
      latestNews: function () {
        return this.db.news.slice(0, 3);
      },
      /** 登录用户：展示其专属信用提示（自适应界面） */
      creditBanner: function () {
        var user = this.user;
        if (!user || !user.creditLevel) return null;
        return {
          level: user.creditLevel,
          comment: user.creditComment,
          // 后端按等级下发的差异化提示
          benefits: user.benefits || []
        };
      },
      stats: function () {
        var products = this.db.products;
        if (!products.length) return [];
        var rates = products.map(function (p) {
          return p.rate;
        });
        var minRate = Math.min.apply(null, rates);
        var totalApply = products.reduce(function (sum, p) {
          return sum + (p.applyCount || 0);
        }, 0);
        return [
          { icon: '产', value: products.length, unit: ' 款', label: '在售贷款产品' },
          { icon: '户', value: global.U.thousand(totalApply), unit: ' 次', label: '累计服务申请' },
          { icon: '率', value: minRate.toFixed(2), unit: ' %', label: '最低年化利率' },
          { icon: '时', value: '1.5', unit: ' 天', label: '平均审批时长' }
        ];
      }
    },
    methods: {
      iconColor: function (id) {
        return ICON_COLORS[id] || '#e7f6ee';
      }
    }
  });
})(window);
