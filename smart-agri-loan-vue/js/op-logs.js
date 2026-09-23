/**
 * 操作日志管理页（仅审批人员）
 *
 * 数据来源：GET /api/oplog（分页 + 关键字 / 模块 / 结果筛选）
 * 权限：页面级守卫 guard:'bank'，接口侧由 Spring Security 再次校验
 */
(function (global) {
  'use strict';

  var ROLE_LABEL = {
    FARMER: '农户',
    ENTERPRISE: '农业企业',
    BANK_ADMIN: '审批人员',
    '': '系统'
  };

  global.App.mountApp({
    guard: 'bank',
    data: function () {
      return {
        U: global.U,
        list: [],
        total: 0,
        page: 1,
        size: 10,
        loading: false,
        expanded: '',
        modules: ['认证', '贷款申请', '审批', '账户', '信用'],
        filters: { keyword: '', module: '', success: '' }
      };
    },
    computed: {
      totalPages: function () {
        return Math.max(1, Math.ceil(this.total / this.size));
      }
    },
    mounted: function () {
      this.load();
    },
    methods: {
      roleLabel: function (code) {
        return ROLE_LABEL[code] || code || '系统';
      },
      load: function () {
        var that = this;
        this.loading = true;
        var params = { page: this.page, size: this.size };
        if (this.filters.keyword) params.keyword = this.filters.keyword;
        if (this.filters.module) params.module = this.filters.module;
        if (this.filters.success !== '') params.success = this.filters.success;
        global.API.opLogs(params).then(function (data) {
          that.loading = false;
          that.list = (data && data.list) || [];
          that.total = (data && data.total) || 0;
          that.expanded = '';
        }).catch(function () {
          that.loading = false;
        });
      },
      search: function () {
        this.page = 1;
        this.load();
      },
      reset: function () {
        this.filters = { keyword: '', module: '', success: '' };
        this.page = 1;
        this.load();
      },
      go: function (page) {
        if (page < 1 || page > this.totalPages) return;
        this.page = page;
        this.load();
      },
      toggle: function (id) {
        this.expanded = this.expanded === id ? '' : id;
      }
    }
  });
})(window);
