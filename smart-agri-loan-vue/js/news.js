/**
 * 惠农资讯页（只读页面）
 * 列表渲染 + 点击标题弹窗展示详情，无任何外部依赖
 */
(function (global) {
  'use strict';

  var CONST = global.APP_CONST;

  global.App.mountApp({
    guard: 'any',
    data: function () {
      return {
        U: global.U,
        db: global.DB.getDB(),
        activeCategory: '全部',
        categories: ['全部', CONST.NEWS_CATEGORY.POLICY, CONST.NEWS_CATEGORY.KNOWLEDGE, CONST.NEWS_CATEGORY.INDUSTRY],
        detailVisible: false,
        detail: {}
      };
    },
    computed: {
      filteredNews: function () {
        var that = this;
        if (this.activeCategory === '全部') return this.db.news;
        return this.db.news.filter(function (n) {
          return n.category === that.activeCategory;
        });
      }
    },
    mounted: function () {
      // 支持从首页 ?id=N001 直接进入详情
      var id = new URLSearchParams(location.search).get('id');
      if (id) this.openDetail(id);
    },
    methods: {
      categoryClass: function (category) {
        if (category === CONST.NEWS_CATEGORY.POLICY) return 'tag-primary';
        if (category === CONST.NEWS_CATEGORY.KNOWLEDGE) return 'tag-info';
        return 'tag-warn';
      },
      openDetail: function (id) {
        var target = this.db.news.filter(function (n) {
          return n.id === id;
        })[0];
        if (!target) return;
        this.detail = target;
        this.detailVisible = true;
      },
      onGoProducts: function () {
        this.detailVisible = false;
        location.href = 'products.html';
      }
    }
  });
})(window);
