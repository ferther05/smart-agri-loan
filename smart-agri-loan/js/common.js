/* ==========================================================
   全局通用逻辑：接口弹窗 / 导航 / 数字滚动 / 滚动动画 / 返回顶部
   ========================================================== */
(function (global) {
  'use strict';

  /* ---------- 1. 接口调用弹窗 ---------- */
  var mask = document.getElementById('apiMask');
  var nameEl = document.getElementById('apiName');
  var closeBtn = document.getElementById('apiClose');

  function openApiModal(apiName) {
    if (!mask) return;
    nameEl.textContent = apiName + '！';
    mask.classList.add('is-open');
  }

  function closeApiModal() {
    if (mask) mask.classList.remove('is-open');
  }

  // 全局事件委托：任何带 data-api 的元素点击后弹出「接口名称！」
  document.addEventListener('click', function (e) {
    var target = e.target.closest('[data-api]');
    if (target) {
      e.preventDefault();
      openApiModal(target.getAttribute('data-api'));
    }
  });

  if (closeBtn) closeBtn.addEventListener('click', closeApiModal);
  if (mask) mask.addEventListener('click', function (e) { if (e.target === mask) closeApiModal(); });
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape') closeApiModal(); });

  global.ApiModal = { open: openApiModal, close: closeApiModal };

  /* ---------- 2. 吸顶导航 ---------- */
  var header = document.getElementById('header');
  var toTop = document.getElementById('toTop');

  function onScroll() {
    var y = window.scrollY || document.documentElement.scrollTop;
    if (header) header.classList.toggle('is-sticky', y > 40);
    if (toTop) toTop.classList.toggle('is-show', y > 400);
  }
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  if (toTop) {
    toTop.addEventListener('click', function () {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }

  /* ---------- 3. 数字滚动 ---------- */
  function animateNumber(el) {
    var target = parseFloat(el.dataset.target);
    var decimals = (String(target).split('.')[1] || '').length;
    var duration = 1400;
    var start = performance.now();

    function frame(now) {
      var p = Math.min((now - start) / duration, 1);
      var eased = 1 - Math.pow(1 - p, 3);
      el.textContent = (target * eased).toFixed(decimals);
      if (p < 1) requestAnimationFrame(frame);
    }
    requestAnimationFrame(frame);
  }

  /* ---------- 4. 滚动入场动画 ---------- */
  var revealEls = document.querySelectorAll('.reveal');
  var counters = document.querySelectorAll('.counter');

  if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-in');
          entry.target.querySelectorAll('.counter').forEach(animateNumber);
          if (entry.target.classList.contains('counter')) animateNumber(entry.target);
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.15 });

    revealEls.forEach(function (el) { io.observe(el); });
    counters.forEach(function (el) { if (!el.closest('.reveal')) io.observe(el); });
  } else {
    revealEls.forEach(function (el) { el.classList.add('is-in'); });
    counters.forEach(animateNumber);
  }

  /* ---------- 5. 搜索 ---------- */
  var searchInput = document.getElementById('searchInput');
  if (searchInput) {
    searchInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' && this.value.trim()) {
        openApiModal('搜索：' + this.value.trim());
      }
    });
  }

  /* ---------- 6. 工具方法 ---------- */
  global.Utils = {
    money: function (n, decimals) {
      var d = decimals === undefined ? 2 : decimals;
      return n.toLocaleString('zh-CN', { minimumFractionDigits: d, maximumFractionDigits: d });
    }
  };
})(window);
