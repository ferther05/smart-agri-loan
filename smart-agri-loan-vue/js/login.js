/**
 * 登录 / 注册页
 *
 * 说明：
 * - 使用 App.mountBare 挂载（登录页不做鉴权守卫，也不预取业务数据）；
 * - 登录成功后按角色分流：审批人员 → 审批端，农户 / 企业 → 回跳原页面或首页；
 * - 页面开放注册，但只能注册"农户 / 农业企业"，审批人员由后台开通。
 */
(function (global) {
  'use strict';

  global.App.mountBare({
    data: function () {
      return {
        mode: 'login',
        loading: false,
        error: '',
        loginForm: { username: '', password: '' },
        registerForm: {
          username: '',
          password: '',
          role: 'FARMER',
          name: '',
          phone: '',
          address: '',
          business: '',
          scale: ''
        }
      };
    },
    methods: {
      /** 演示账号一键填充 */
      quickFill: function (kind) {
        var preset = {
          farmer: 'farmer',
          coop: 'coop',
          auditor: 'auditor'
        }[kind] || 'farmer';
        this.mode = 'login';
        this.loginForm.username = preset;
        this.loginForm.password = '123456';
        this.error = '';
        global.U.toast('已填入演示账号，点击登录即可体验', 'success');
      },

      submitLogin: function () {
        var that = this;
        if (!this.loginForm.username || !this.loginForm.password) {
          this.error = '请输入账号与密码';
          return;
        }
        this.error = '';
        this.loading = true;
        global.API.login(this.loginForm.username, this.loginForm.password).then(function (data) {
          that.loading = false;
          global.U.toast('欢迎回来，' + data.user.name, 'success');
          setTimeout(function () {
            that.redirectAfterLogin(data.user);
          }, 500);
        }).catch(function (error) {
          that.loading = false;
          that.error = error.message || '登录失败，请重试';
        });
      },

      submitRegister: function () {
        var form = this.registerForm;
        if (!/^[a-zA-Z0-9_]{4,20}$/.test(form.username)) {
          this.error = '账号需为 4-20 位字母、数字或下划线';
          return;
        }
        if (!form.password || form.password.length < 6) {
          this.error = '密码长度需不少于 6 位';
          return;
        }
        if (!form.name) {
          this.error = '请填写姓名或主体名称';
          return;
        }
        if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
          this.error = '手机号格式不正确';
          return;
        }
        var that = this;
        this.error = '';
        this.loading = true;
        global.API.register({
          username: form.username,
          password: form.password,
          name: form.name,
          role: form.role,
          phone: form.phone,
          address: form.address,
          business: form.business,
          scale: form.scale
        }).then(function (data) {
          that.loading = false;
          global.U.toast('注册成功，已自动登录', 'success');
          setTimeout(function () {
            that.redirectAfterLogin(data.user);
          }, 600);
        }).catch(function (error) {
          that.loading = false;
          that.error = error.message || '注册失败，请重试';
        });
      },

      /** 登录后分流：审批人员进审批端，其余回跳来源页 */
      redirectAfterLogin: function (user) {
        var base = global.App.BASE;
        if (user && user.roleCode === 'BANK_ADMIN') {
          location.href = base + 'pages/audit.html';
          return;
        }
        var redirect = new URLSearchParams(location.search).get('redirect');
        if (redirect) {
          location.href = redirect;
          return;
        }
        location.href = base + 'index.html';
      }
    }
  });
})(window);
