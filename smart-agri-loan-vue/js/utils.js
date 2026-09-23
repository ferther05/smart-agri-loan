/**
 * 公共工具方法：金额格式化、状态展示、轻提示、常用查询
 * 只做"读"与"展示"，不直接写本地存储（写数据一律走 DB.saveDB）
 */
(function (global) {
  'use strict';

  var CONST = global.APP_CONST;

  /* ---------- 轻提示 ---------- */
  function toast(message, type) {
    var wrap = document.querySelector('.toast-wrap');
    if (!wrap) {
      wrap = document.createElement('div');
      wrap.className = 'toast-wrap';
      document.body.appendChild(wrap);
    }
    var el = document.createElement('div');
    el.className = 'toast' + (type ? ' toast-' + type : '');
    el.textContent = message;
    wrap.appendChild(el);
    setTimeout(function () {
      el.style.transition = 'opacity .3s';
      el.style.opacity = '0';
      setTimeout(function () {
        el.remove();
      }, 300);
    }, 2200);
  }

  /* ---------- 金额 / 数字 ---------- */
  function thousand(num) {
    var n = Number(num) || 0;
    return n.toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  }

  /** 元 -> 万元文本，如 300000 -> "30 万" */
  function toWan(amount) {
    var n = Number(amount) || 0;
    if (n >= 10000) {
      var w = n / 10000;
      return (w % 1 === 0 ? w : w.toFixed(1)) + ' 万';
    }
    return thousand(n);
  }

  /** 额度区间文本，如 "1 万 ~ 30 万" */
  function amountRange(min, max) {
    return toWan(min) + ' ~ ' + toWan(max);
  }

  /** 两位小数金额：12345.6 -> 12,345.60 */
  function money(num) {
    var n = Number(num) || 0;
    var parts = n.toFixed(2).split('.');
    return parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',') + '.' + parts[1];
  }

  /** 到期应还本息合计：本金 × (1 + 年利率 × 期限月数 / 12) */
  function repayTotal(amount, rate, term) {
    var total = Number(amount) * (1 + ((Number(rate) || 0) / 100) * ((Number(term) || 0) / 12));
    return Math.round(total * 100) / 100;
  }

  /* ---------- 时间 ---------- */
  function pad(n) {
    return n < 10 ? '0' + n : '' + n;
  }

  /** 当前时间文本：yyyy-MM-dd HH:mm */
  function nowText() {
    var d = new Date();
    return (
      d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
      ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
    );
  }

  /* ---------- 校验 ---------- */
  /** 身份证校验：先正则，再按 GB11643 校验位算法验证 */
  function checkIdCard(id) {
    var value = String(id || '').trim();
    if (!CONST.REGEX.ID_CARD.test(value)) return false;
    var weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
    var codes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
    var sum = 0;
    for (var i = 0; i < 17; i++) {
      sum += parseInt(value.charAt(i), 10) * weights[i];
    }
    return codes[sum % 11] === value.charAt(17).toUpperCase();
  }

  /** 生成唯一申请编号：SQ + 年月日 + 时间戳后 6 位 + 3 位随机数 */
  function genApplyId() {
    var d = new Date();
    var day = '' + d.getFullYear() + pad(d.getMonth() + 1) + pad(d.getDate());
    var ts = String(Date.now()).slice(-6);
    var rand = String(Math.floor(Math.random() * 900 + 100));
    return 'SQ' + day + ts + rand;
  }

  /* ---------- 风控 ---------- */
  /** 按信用积分判定红黄绿风险等级 */
  function riskOf(score) {
    var list = CONST.RISK_LIST;
    for (var i = 0; i < list.length; i++) {
      if (score >= list[i].min) return list[i];
    }
    return CONST.RISK_LEVEL.HIGH;
  }

  /* ---------- 状态 ---------- */
  function statusMeta(status) {
    return CONST.STATUS_META[status] || { type: 'muted', desc: '' };
  }

  function statusTagClass(status) {
    return 'tag tag-' + statusMeta(status).type;
  }

  /* ---------- 信用 ---------- */
  function levelOf(score) {
    var list = CONST.CREDIT_LEVEL;
    for (var i = 0; i < list.length; i++) {
      if (score >= list[i].min && score <= list[i].max) return list[i].level;
    }
    return 'C';
  }

  /* ---------- 数据查询（基于 DB.getDB 的结果做内存查询） ---------- */
  function currentUser(db) {
    var users = (db && db.users) || [];
    for (var i = 0; i < users.length; i++) {
      if (users[i].isCurrent) return users[i];
    }
    return users[0] || null;
  }

  function findProduct(db, productId) {
    var list = (db && db.products) || [];
    for (var i = 0; i < list.length; i++) {
      if (list[i].id === productId) return list[i];
    }
    return null;
  }

  /** 按状态统计申请单数量 */
  function countByStatus(applications, status) {
    return (applications || []).filter(function (item) {
      return item.status === status;
    }).length;
  }

  global.U = {
    toast: toast,
    thousand: thousand,
    toWan: toWan,
    amountRange: amountRange,
    money: money,
    repayTotal: repayTotal,
    nowText: nowText,
    checkIdCard: checkIdCard,
    genApplyId: genApplyId,
    riskOf: riskOf,
    statusMeta: statusMeta,
    statusTagClass: statusTagClass,
    levelOf: levelOf,
    currentUser: currentUser,
    findProduct: findProduct,
    countByStatus: countByStatus
  };
})(window);
