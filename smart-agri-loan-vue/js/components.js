/**
 * 公共页面骨架：顶部导航栏 + 底部页脚 + 应用挂载工具
 *
 * 与后端对接后的三条规矩：
 * 1. 所有页面统一调用 App.mountApp({ ... }) 启动，由它负责
 *    ① 用 JWT 拉取数据（API.bootstrap）
 *    ② 按角色做页面级权限守卫
 *    ③ 再挂载 Vue 实例（页面里的 DB.getDB() 此时已能拿到后端数据）
 * 2. 导航菜单与操作按钮按角色显隐：无权限的功能前端直接不显示；
 * 3. 未登录访问受限页面 → 自动跳转登录页并带上 redirect 回跳参数。
 */
(function (global) {
  'use strict';

  var CONST = global.APP_CONST;

  /** 站点根路径：pages/ 下的页面需要 ../ 前缀 */
  function getBase() {
    if (global.APP_BASE) return global.APP_BASE;
    var path = (location.pathname || '').replace(/\\/g, '/');
    return /\/pages\//.test(path) ? '../' : '';
  }

  var BASE = getBase();

  /* ---------------- 角色可见性 ---------------- */

  var ROLE = {
    FARMER: 'FARMER',
    ENTERPRISE: 'ENTERPRISE',
    BANK_ADMIN: 'BANK_ADMIN'
  };

  function roleCode(user) {
    return user && user.roleCode ? user.roleCode : '';
  }

  function isBank(user) {
    return roleCode(user) === ROLE.BANK_ADMIN;
  }

  function isApplicant(user) {
    return roleCode(user) === ROLE.FARMER || roleCode(user) === ROLE.ENTERPRISE;
  }

  /**
   * 导航菜单：roles 为空表示所有人可见
   * 无权限的菜单项不会出现在页面上（前端不做展示）
   */
  var NAV_ITEMS = [
    { key: 'home', text: '首页', href: BASE + 'index.html' },
    { key: 'products', text: '贷款产品', href: BASE + 'pages/products.html' },
    { key: 'credit', text: '信用积分', href: BASE + 'pages/credit.html', roles: [ROLE.FARMER, ROLE.ENTERPRISE] },
    { key: 'news', text: '惠农资讯', href: BASE + 'pages/news.html' },
    { key: 'my-apply', text: '我的申请', href: BASE + 'pages/my-apply.html', roles: [ROLE.FARMER, ROLE.ENTERPRISE] },
    { key: 'op-logs', text: '操作日志', href: BASE + 'pages/op-logs.html', roles: [ROLE.BANK_ADMIN] }
  ];

  /** 按角色过滤菜单 */
  function navFor(user) {
    var code = roleCode(user);
    return NAV_ITEMS.filter(function (item) {
      if (!item.roles) return true;
      // 未登录时仅展示公共菜单
      if (!code) return false;
      return item.roles.indexOf(code) > -1;
    });
  }

  /* ---------------- 顶部导航组件 ---------------- */

  var AppHeader = {
    name: 'AppHeader',
    props: {
      active: { type: String, default: '' }
    },
    data: function () {
      return {
        navItems: navFor(global.API.cachedUser()),
        base: BASE,
        user: global.API.cachedUser(),
        menuOpen: false,
        pendingGo: ''
      };
    },
    computed: {
      loggedIn: function () {
        return !!this.user && global.API.isLogin();
      },
      isBankUser: function () {
        return isBank(this.user);
      },
      isApplicantUser: function () {
        return isApplicant(this.user);
      },
      roleTag: function () {
        if (!this.user) return '';
        return this.user.role + ' · ' + this.user.creditLevel;
      }
    },
    mounted: function () {
      var that = this;
      // 登录态刷新：进入页面时重新校验一次 token
      if (global.API.isLogin()) {
        global.API.me().then(function (user) {
          that.user = user;
          that.navItems = navFor(user);
        }, function () {
          that.user = null;
          that.navItems = navFor(null);
        });
      }
    },
    methods: {
      onComing: function () {
        global.U.toast('该功能将在下一阶段开放，敬请期待', 'warn');
      },
      toggleMenu: function () {
        this.menuOpen = !this.menuOpen;
      },
      goLogin: function () {
        location.href = this.base + 'pages/login.html';
      },
      logout: function () {
        global.API.clearSession();
        global.U.toast('已安全退出登录', 'success');
        var that = this;
        setTimeout(function () {
          location.href = that.base + 'pages/login.html';
        }, 400);
      },
      /** 申请贷款 / 审批端：点击先给按压反馈，再跳转并带 nav=1 提示"已到达" */
      navGo: function (kind, e) {
        if (this.pendingGo) return;
        var target = kind === 'audit'
          ? this.base + 'pages/audit.html'
          : this.base + 'pages/apply.html';
        this.pendingGo = kind;
        var el = e && e.currentTarget;
        if (el) el.classList.add('is-pressed');
        var that = this;
        setTimeout(function () {
          that.pendingGo = '';
          location.href = target + '?nav=1';
        }, 420);
      }
    },
    template: [
      '<header class="site-header">',
      '  <div class="container header-inner">',
      '    <a class="brand" :href="navItems.length ? navItems[0].href : base + \'index.html\'">',
      '      <span class="brand-logo">惠</span>',
      '      <span>智能惠农信贷<span class="brand-sub">Smart Agri-Loan</span></span>',
      '    </a>',
      '    <nav class="nav-links">',
      '      <a v-for="item in navItems" :key="item.key" class="nav-link"',
      '         :class="{ active: item.key === active }"',
      '         :href="item.soon ? \'javascript:void(0)\' : item.href"',
      '         @click="item.soon && onComing()">{{ item.text }}</a>',
      '    </nav>',
      '    <div class="nav-actions">',
      '      <a v-if="isApplicantUser" class="btn btn-primary btn-sm nav-go"',
      '         :class="{ \'is-on\': active === \'apply-entry\', \'is-loading\': pendingGo === \'apply\' }"',
      '         href="javascript:void(0)" @click="navGo(\'apply\', $event)">',
      '        <span v-if="pendingGo === \'apply\'" class="nav-go-spinner"></span>',
      '        <span class="nav-go-text">{{ pendingGo === \'apply\' ? \'正在进入...\' : \'申请贷款\' }}</span>',
      '      </a>',
      '      <a v-if="isBankUser" class="btn btn-outline btn-sm nav-go"',
      '         :class="{ \'is-on\': active === \'audit-entry\', \'is-loading\': pendingGo === \'audit\' }"',
      '         href="javascript:void(0)" @click="navGo(\'audit\', $event)">',
      '        <span v-if="pendingGo === \'audit\'" class="nav-go-spinner"></span>',
      '        <span class="nav-go-text">{{ pendingGo === \'audit\' ? \'正在进入...\' : \'审批端\' }}</span>',
      '      </a>',
      '      <a v-if="!loggedIn" class="btn btn-outline btn-sm" :href="base + \'pages/login.html\'">登录 / 注册</a>',
      '      <div class="user-chip" v-if="loggedIn" @click="toggleMenu">',
      '        <span class="user-avatar">{{ (user.name || \'?\').charAt(0) }}</span>',
      '        <span class="user-name">{{ user.name }}</span>',
      '        <span class="user-level">{{ roleTag }}</span>',
      '        <div class="user-menu" v-if="menuOpen">',
      '          <div class="user-menu-item" @click.stop="logout">退出登录</div>',
      '        </div>',
      '      </div>',
      '    </div>',
      '  </div>',
      '</header>'
    ].join('')
  };

  /* ---------------- 底部页脚组件 ---------------- */

  var AppFooter = {
    name: 'AppFooter',
    template: [
      '<footer class="site-footer">',
      '  <div class="container footer-inner">',
      '    <div>',
      '      <div class="footer-title">智能惠农信贷系统</div>',
      '      <div class="footer-text">',
      '        依托国家深化农村改革、扎实推进乡村全面振兴的政策背景，为农户、农业企业与金融机构',
      '        提供高效、便捷、安全的线上融资服务，助力农业信贷服务数字化、智能化、普惠化。',
      '      </div>',
      '    </div>',
      '    <div>',
      '      <div class="footer-title">快捷入口</div>',
      '      <ul class="footer-links">',
      '        <li>贷款产品大全</li>',
      '        <li>信用积分查询</li>',
      '        <li>惠农政策资讯</li>',
      '        <li>业务进度查询</li>',
      '      </ul>',
      '    </div>',
      '    <div>',
      '      <div class="footer-title">服务支持</div>',
      '      <ul class="footer-links">',
      '        <li>申请指引</li>',
      '        <li>常见问题</li>',
      '        <li>反诈提示</li>',
      '        <li>意见反馈</li>',
      '      </ul>',
      '    </div>',
      '    <div>',
      '      <div class="footer-title">联系我们</div>',
      '      <div class="footer-text">',
      '        客服热线：400-820-1888<br/>',
      '        服务时间：周一至周日 8:30 - 20:30<br/>',
      '        邮箱：service@agri-loan.cn<br/>',
      '        地址：贵州省遵义市湄潭县金融服务中心',
      '      </div>',
      '    </div>',
      '  </div>',
      '  <div class="container footer-bottom">',
      '    © 2026 智能惠农信贷系统 ｜ 本项目为 Web 前端设计与开发课程配套实训案例 ｜ 数据均为演示用途',
      '  </div>',
      '</footer>'
    ].join('')
  };

  /* ---------------- 权限守卫 ---------------- */

  function redirectLogin() {
    var back = location.pathname + location.search;
    location.href = BASE + 'pages/login.html?redirect=' + encodeURIComponent(back);
  }

  function goHome(message) {
    if (message) global.U.toast(message, 'error');
    setTimeout(function () {
      location.href = BASE + 'index.html';
    }, 900);
  }

  /**
   * 校验页面访问权限
   * @param {string} guard any / auth / applicant / bank / guest
   * @param {Object} user  当前登录人
   * @returns {boolean} true 表示放行
   */
  function checkGuard(guard, user) {
    if (guard === 'auth' && !user) {
      global.U.toast('请先登录后再访问该页面', 'warn');
      redirectLogin();
      return false;
    }
    if (guard === 'applicant') {
      if (!user) {
        global.U.toast('请先登录后再访问该页面', 'warn');
        redirectLogin();
        return false;
      }
      if (!isApplicant(user)) {
        goHome('审批人员账号无贷款申请权限，已返回首页');
        return false;
      }
    }
    if (guard === 'bank') {
      if (!user) {
        global.U.toast('请先登录后再访问审批端', 'warn');
        redirectLogin();
        return false;
      }
      if (!isBank(user)) {
        goHome('当前角色无权访问审批端，已返回首页');
        return false;
      }
    }
    if (guard === 'guest' && user) {
      // 登录页：已登录直接回首页
      location.href = BASE + 'index.html';
      return false;
    }
    return true;
  }

  /**
   * 统一挂载入口（带鉴权与数据预取）
   * @param {Object} options Vue 根组件选项
   *   options.guard            any / auth / applicant / bank / guest
   *   options.needApplications 是否需要申请单列表
   *   options.needCredit       是否需要信用积分数据
   * @returns {Promise} 挂载结果
   */
  function mountApp(options) {
    options = options || {};
    var guard = options.guard || 'any';

    return global.API.bootstrap({
      needApplications: !!options.needApplications,
      needCredit: !!options.needCredit
    }).then(function (pool) {
      var user = pool.users && pool.users.length ? pool.users[0] : null;
      if (!checkGuard(guard, user)) {
        return null;
      }
      var app = global.Vue.createApp(options);
      app.component('app-header', AppHeader);
      app.component('app-footer', AppFooter);
      return app.mount('#app');
    });
  }

  /** 无鉴权、无数据预取的裸挂载（登录页使用） */
  function mountBare(options) {
    var app = global.Vue.createApp(options);
    return app.mount('#app');
  }

  global.App = {
    BASE: BASE,
    NAV_ITEMS: NAV_ITEMS,
    ROLE: ROLE,
    isBank: isBank,
    isApplicant: isApplicant,
    navFor: navFor,
    mountApp: mountApp,
    mountBare: mountBare
  };
})(window);
