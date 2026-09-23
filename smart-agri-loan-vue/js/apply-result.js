/**
 * 申请反馈页：展示申请编号、申请摘要与办理进度
 *
 * 数据来源：GET /api/applications（缓存命中直接用，否则按编号回源查询）
 */
(function (global) {
  'use strict';

  var CONST = global.APP_CONST;

  global.App.mountApp({
    guard: 'applicant',
    needApplications: true,
    data: function () {
      var db = global.DB.getDB();
      var id = new URLSearchParams(location.search).get('id') || '';
      var application = null;
      for (var i = 0; i < db.applications.length; i++) {
        if (db.applications[i].id === id) {
          application = db.applications[i];
          break;
        }
      }
      return {
        U: global.U,
        applicationId: id,
        application: application,
        flowList: CONST.STATUS_FLOW
      };
    },
    computed: {
      currentStep: function () {
        if (!this.application) return 0;
        var idx = CONST.STATUS_FLOW.indexOf(this.application.status);
        return idx > -1 ? idx : 0;
      }
    },
    mounted: function () {
      // 缓存里没有（例如直接打开链接）则按编号回源查询
      var that = this;
      if (!this.application && this.applicationId) {
        global.API.application(this.applicationId).then(function (data) {
          that.application = data;
        }).catch(function () {
          /* 接口已提示错误 */
        });
      }
    },
    methods: {
      copyId: function () {
        var text = this.application.id;
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(text);
        }
        global.U.toast('申请编号已复制：' + text, 'success');
      }
    }
  });
})(window);
