/**
 * ============================================================
 *  前端 API 层 —— 与 Spring Boot 后端对接的唯一出口
 * ------------------------------------------------------------
 *  约定：
 *  1. 所有页面不得直接使用 fetch / XMLHttpRequest，一律走 API.xxx；
 *  2. JWT 统一保存在 localStorage，请求自动携带 Authorization: Bearer；
 *  3. 后端统一响应 { code, message, data }，非 200 一律抛错并提示；
 *  4. 401（登录失效）自动清理会话并跳转登录页；
 *  5. bootstrap() 会把后端数据组装成与旧 db.js 相同结构的数据池，
 *     页面里的 DB.getDB() / db.products 等写法无需改动。
 * ============================================================
 */
(function (global) {
  'use strict';

  /* 后端地址：可在页面里用 window.API_BASE 覆盖 */
  var API_BASE = global.API_BASE || 'http://localhost:8080';

  var TOKEN_KEY = 'SMART_AGRI_LOAN_TOKEN';
  var USER_KEY = 'SMART_AGRI_LOAN_USER';

  var TOKEN_KEY_LEGACY = null;

  /* ---------- 会话存储 ---------- */

  function getToken() {
    try {
      return localStorage.getItem(TOKEN_KEY) || '';
    } catch (e) {
      return '';
    }
  }

  function setSession(token, user) {
    try {
      localStorage.setItem(TOKEN_KEY, token || '');
      localStorage.setItem(USER_KEY, JSON.stringify(user || null));
    } catch (e) {
      /* 忽略存储异常 */
    }
  }

  function clearSession() {
    try {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    } catch (e) {
      /* 忽略 */
    }
  }

  /** 本地缓存的当前用户（页面首屏可先用它渲染，避免闪烁） */
  function cachedUser() {
    try {
      var raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  }

  function setUser(user) {
    try {
      localStorage.setItem(USER_KEY, JSON.stringify(user || null));
    } catch (e) {
      /* 忽略 */
    }
  }

  /* ---------- 基础请求 ---------- */

  /**
   * 发起请求
   * @param {string} method  HTTP 方法
   * @param {string} path    形如 /api/auth/login
   * @param {Object} [body]  请求体
   * @param {Object} [opts]  { silent: 不弹提示, noAuthRedirect: 401 不跳转 }
   */
  function request(method, path, body, opts) {
    opts = opts || {};
    var headers = { Accept: 'application/json' };
    var token = getToken();
    if (token) headers.Authorization = 'Bearer ' + token;
    var init = { method: method, headers: headers };
    if (body !== undefined && body !== null) {
      headers['Content-Type'] = 'application/json;charset=UTF-8';
      init.body = JSON.stringify(body);
    }

    return fetch(API_BASE + path, init).then(function (response) {
      return response.json().catch(function () {
        return { code: response.status, message: '服务返回格式异常' };
      }).then(function (payload) {
        if (payload && payload.code === 200) {
          return payload.data;
        }
        var error = {
          code: (payload && payload.code) || response.status,
          message: (payload && payload.message) || '请求失败，请稍后重试'
        };
        if (error.code === 401 && !opts.noAuthRedirect) {
          clearSession();
          redirectToLogin();
        }
        if (!opts.silent && global.U && global.U.toast) {
          global.U.toast(error.message, 'error');
        }
        throw error;
      });
    }, function (networkError) {
      if (!opts.silent && global.U && global.U.toast) {
        global.U.toast('无法连接后端服务，请确认已启动 smart-agri-loan-server', 'error');
      }
      throw { code: -1, message: '网络异常', cause: networkError };
    });
  }

  function redirectToLogin() {
    if (global.App && global.App.BASE !== undefined) {
      location.href = global.App.BASE + 'pages/login.html?redirect=' +
        encodeURIComponent(location.pathname + location.search);
      return;
    }
    var path = (location.pathname || '').replace(/\\/g, '/');
    var prefix = /\/pages\//.test(path) ? '' : 'pages/';
    location.href = prefix + 'login.html';
  }

  /* ---------- 认证 ---------- */

  function login(username, password) {
    return request('POST', '/api/auth/login', { username: username, password: password })
      .then(function (data) {
        setSession(data.token, data.user);
        return data;
      });
  }

  function register(payload) {
    return request('POST', '/api/auth/register', payload).then(function (data) {
      setSession(data.token, data.user);
      return data;
    });
  }

  function me() {
    return request('GET', '/api/auth/me').then(function (user) {
      setUser(user);
      return user;
    });
  }

  function logout() {
    clearSession();
    location.href = (global.App ? global.App.BASE : '../') + 'pages/login.html';
  }

  /* ---------- 业务接口 ---------- */

  var API = {
    base: API_BASE,
    getToken: getToken,
    cachedUser: cachedUser,
    setUser: setUser,
    clearSession: clearSession,
    isLogin: function () {
      return !!getToken();
    },
    isBank: function () {
      var user = cachedUser();
      return !!user && user.roleCode === 'BANK_ADMIN';
    },
    isApplicant: function () {
      var user = cachedUser();
      return !!user && (user.roleCode === 'FARMER' || user.roleCode === 'ENTERPRISE');
    },

    request: request,
    login: login,
    register: register,
    me: me,
    logout: logout,

    /* 产品（公开，登录后带自适应准入字段） */
    products: function (params) {
      return request('GET', '/api/products' + query(params), null, { silent: true, noAuthRedirect: true });
    },
    hotProducts: function () {
      return request('GET', '/api/products/hot', null, { silent: true, noAuthRedirect: true });
    },
    productStats: function () {
      return request('GET', '/api/products/stats', null, { silent: true, noAuthRedirect: true });
    },

    /* 资讯（公开） */
    newsList: function (params) {
      return request('GET', '/api/news' + query(params), null, { silent: true, noAuthRedirect: true });
    },
    newsDetail: function (id) {
      return request('GET', '/api/news/' + id, null, { silent: true, noAuthRedirect: true });
    },

    /* 信用 */
    credit: function () {
      return request('GET', '/api/credit/me');
    },

    /* 贷款申请 */
    applications: function (params) {
      return request('GET', '/api/applications' + query(params));
    },
    application: function (id) {
      return request('GET', '/api/applications/' + id);
    },
    createApplication: function (payload) {
      return request('POST', '/api/applications', payload);
    },
    deleteApplication: function (id) {
      return request('DELETE', '/api/applications/' + id);
    },
    sign: function (id) {
      return request('POST', '/api/applications/' + id + '/sign', {});
    },
    loan: function (id) {
      return request('POST', '/api/applications/' + id + '/loan', {});
    },
    settle: function (id) {
      return request('POST', '/api/applications/' + id + '/settle', {});
    },
    audit: function (id, action, opinion) {
      return request('POST', '/api/applications/' + id + '/audit', { action: action, opinion: opinion });
    },
    recharge: function (amount) {
      return request('POST', '/api/account/recharge', { amount: amount });
    },

    /* 管理端 */
    users: function () {
      return request('GET', '/api/users');
    },
    adjustCredit: function (userId, delta, event, type) {
      return request('POST', '/api/users/' + userId + '/credit',
        { delta: delta, event: event, type: type });
    },
    opLogs: function (params) {
      return request('GET', '/api/oplog' + query(params));
    },

    /* 旧变量名兼容（避免页面里的 TOKEN_KEY_LEGACY 之类残留告警） */
    tokenKeyLegacy: TOKEN_KEY_LEGACY,

    /**
     * 页面初始化：一次拉齐页面所需数据，并组装成与旧 db.js 同构的数据池
     * @param {Object} [options] { needApplications: 是否需要申请单, needCredit: 是否需要信用数据 }
     * @returns {Promise<Object>} 数据池
     */
    bootstrap: function (options) {
      options = options || {};
      var pool = {
        version: 3,
        products: [],
        news: [],
        users: [],
        applications: [],
        creditLogs: [],
        creditInfo: null,
        updateTime: ''
      };
      var tasks = [
        API.products().then(function (list) {
          pool.products = list || [];
        }),
        API.newsList().then(function (list) {
          pool.news = list || [];
        })
      ];

      if (getToken()) {
        tasks.push(API.me().then(function (user) {
          pool.users = [user];
        }, function () {
          // token 失效：清掉会话，公共数据仍可使用
          clearSession();
        }));
      }
      if (options.needApplications && getToken()) {
        tasks.push(API.applications().then(function (list) {
          pool.applications = list || [];
        }, function () {
          pool.applications = [];
        }));
      }
      if (options.needCredit && getToken()) {
        tasks.push(API.credit().then(function (info) {
          pool.creditInfo = info;
          pool.creditLogs = info.logs || [];
        }, function () {
          pool.creditInfo = null;
        }));
      }

      return Promise.all(tasks).then(function () {
        pool.updateTime = (global.U && global.U.nowText) ? global.U.nowText() : '';
        if (global.DB && global.DB.setCache) {
          global.DB.setCache(pool);
        }
        return pool;
      });
    }
  };

  /** 拼接查询串 */
  function query(params) {
    if (!params) return '';
    var pairs = [];
    Object.keys(params).forEach(function (key) {
      var value = params[key];
      if (value === undefined || value === null || value === '') return;
      pairs.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
    });
    return pairs.length ? '?' + pairs.join('&') : '';
  }

  global.API = API;
})(window);
