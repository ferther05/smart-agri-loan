/* ==========================================================
   首页交互：轮播 / 产品筛选 / 贷款计算器 / 资讯切换
   ========================================================== */
(function () {
  'use strict';

  /* ================= 一、数据 ================= */
  var PRODUCTS = [
    {
      type: 'plant', icon: 'i-leaf', name: '惠农种植贷', bank: '中国农业银行',
      rate: '3.65', limit: '50 万元', term: '3 年',
      tags: [['免抵押', ''], ['财政贴息', 'yellow'], ['随借随还', '']],
      desc: '面向粮食、果蔬、中药材种植户及家庭农场，凭土地经营权与经营流水即可申请，T+3 放款。'
    },
    {
      type: 'plant', icon: 'i-farm', name: '高标准农田建设贷', bank: '农业发展银行',
      rate: '3.15', limit: '300 万元', term: '5 年',
      tags: [['政府贴息', 'yellow'], ['期限长', ''], ['额度高', '']],
      desc: '支持高标准农田建设、土地平整、灌溉设施改造，享受中央财政贴息，最低可至 3.15%。'
    },
    {
      type: 'breed', icon: 'i-cow', name: '畜牧养殖贷', bank: '农村信用合作社',
      rate: '3.85', limit: '80 万元', term: '2 年',
      tags: [['活体抵押', ''], ['快速审批', 'yellow']],
      desc: '针对生猪、肉牛、奶牛养殖主体，支持活体畜禽抵押登记，最快 48 小时完成审批放款。'
    },
    {
      type: 'breed', icon: 'i-sprout', name: '绿色水产贷', bank: '中国邮政储蓄银行',
      rate: '4.15', limit: '120 万元', term: '3 年',
      tags: [['生态养殖', ''], ['循环额度', '']],
      desc: '支持池塘标准化改造、稻渔综合种养、工厂化循环水养殖，一次授信三年循环使用。'
    },
    {
      type: 'machine', icon: 'i-tractor', name: '农机购置贷', bank: '中国农业银行',
      rate: '3.45', limit: '100 万元', term: '3 年',
      tags: [['农机补贴', 'yellow'], ['零首付', ''], ['当天放款', '']],
      desc: '覆盖拖拉机、收割机、植保无人机等农机具购置，可叠加农机购置补贴，最高零首付提机。'
    },
    {
      type: 'chain', icon: 'i-truck', name: '农产品供应链贷', bank: '各地农商银行',
      rate: '4.35', limit: '500 万元', term: '1 年',
      tags: [['应收账款', ''], ['线上秒批', 'yellow']],
      desc: '面向农业产业化龙头企业、合作社及收购商，基于订单与应收账款融资，全线上化操作。'
    }
  ];

  var NEWS = {
    agri: [
      { d: '02', m: '9月', title: '全国秋粮收购工作全面启动 主产区收储库点超 2 万个', desc: '国家粮食和物资储备局部署秋粮收购工作，要求各主产区合理布设收储库点，严格执行质价政策，确保"种粮卖得出"。', src: '农业农村部', view: '1.2万' },
      { d: '31', m: '8月', title: '高标准农田建设累计突破 12 亿亩 亩均产能提升 10%', desc: '截至目前全国已建成高标准农田超 12 亿亩，项目区耕地质量等级平均提升约 1 个等级，亩均粮食产能增加 10%—20%。', src: '农业要闻', view: '9863' },
      { d: '28', m: '8月', title: '智慧农机加速普及 北斗终端装机量突破 220 万台', desc: '搭载北斗导航的拖拉机、播种机、植保无人机在夏收夏种中大规模应用，作业精度与效率显著提升，人力成本降低三成。', src: '数字农业', view: '8421' },
      { d: '25', m: '8月', title: '种业振兴行动进展发布 自主选育品种占比超 95%', desc: '农业农村部通报种业振兴行动阶段性成果，农作物自主选育品种面积占比超过 95%，畜禽核心种源自给率稳步提升。', src: '政策发布', view: '7530' },
      { d: '22', m: '8月', title: '农产品冷链物流体系建设提速 产地仓覆盖千余县', desc: '产地冷藏保鲜设施建设持续推进，已支持建设超 8 万个产地仓储保鲜设施，果蔬产后损耗率明显下降。', src: '产业观察', view: '6104' }
    ],
    loan: [
      { d: '01', m: '9月', title: '涉农贷款余额突破 62 万亿元 同比增长 12.6%', desc: '金融监管总局数据显示，全国涉农贷款余额持续稳步增长，普惠型涉农贷款增速高于各项贷款平均增速。', src: '金融监管总局', view: '2.3万' },
      { d: '30', m: '8月', title: '2026 年中央财政农业信贷担保贴息政策实施细则发布', desc: '明确对符合条件的新型农业经营主体给予不超过贷款金额 3% 的贴息支持，单个主体年度贴息上限 50 万元。', src: '财政部', view: '1.8万' },
      { d: '27', m: '8月', title: '活体畜禽抵押融资试点扩大至 28 个省份', desc: '通过电子耳标、生物识别与区块链存证，实现活体资产确权与动态监管，破解养殖户缺少抵押物难题。', src: '人民银行', view: '1.1万' },
      { d: '24', m: '8月', title: '信用村信用户评定覆盖 68 万个行政村 授信超 3 万亿元', desc: '整村授信模式持续推进，农户凭信用即可获得最高 30 万元免抵押额度，平均审批时长缩短至 2 天。', src: '农村金融', view: '9677' },
      { d: '20', m: '8月', title: '"农业保险 + 信贷"联动模式落地 风险分担机制更完善', desc: '以保险保单作为增信手段，银行给予利率优惠与额度加成，有效缓解自然灾害带来的信贷风险。', src: '行业动态', view: '7032' }
    ]
  };

  var POLICIES = [
    {
      img: 'assets/images/policy-1.svg',
      cat: '财政贴息', catType: 'yellow',
      title: '2026 年农业信贷担保贷款贴息实施细则',
      desc: '对符合条件的家庭农场、农民合作社、农业社会化服务组织给予贷款贴息，贴息比例不超过 LPR 的 50%，单户年度上限 50 万元。',
      date: '2026-08-30', from: '财政部 / 农业农村部'
    },
    {
      img: 'assets/images/policy-2.svg',
      cat: '银农对接', catType: 'green',
      title: '关于开展新型农业经营主体信贷直通车活动的通知',
      desc: '建立"主体清单 + 产品清单"双向推送机制，银行机构在 5 个工作日内响应融资需求，实现银农精准对接。',
      date: '2026-08-24', from: '农业农村部办公厅'
    },
    {
      img: 'assets/images/policy-3.svg',
      cat: '数字普惠', catType: 'red',
      title: '数字普惠金融助力乡村振兴专项行动方案',
      desc: '推动涉农数据归集共享，建设农业经营主体信用画像体系，力争三年内实现县域数字金融服务全覆盖。',
      date: '2026-08-18', from: '中国人民银行'
    }
  ];

  var HOT = [
    '2026 年惠农贷款贴息怎么申请？一文看懂',
    '家庭农场能贷多少？额度测算指南',
    '没有抵押物也能贷款？活体抵押全解析',
    '农机购置补贴与贷款如何叠加使用',
    '整村授信：信用村能带来哪些实惠',
    '涉农贷款被拒的 6 个常见原因'
  ];

  /* ================= 二、轮播 ================= */
  var slides = document.querySelectorAll('#heroWrap .slide');
  var dotsBox = document.getElementById('heroDots');
  var current = 0;
  var timer = null;

  slides.forEach(function (_, i) {
    var dot = document.createElement('button');
    dot.className = 'hero-dot' + (i === 0 ? ' is-active' : '');
    dot.setAttribute('aria-label', '第 ' + (i + 1) + ' 张');
    dot.addEventListener('click', function () { goSlide(i); });
    dotsBox.appendChild(dot);
  });

  function goSlide(index) {
    slides[current].classList.remove('is-active');
    dotsBox.children[current].classList.remove('is-active');
    current = (index + slides.length) % slides.length;
    slides[current].classList.add('is-active');
    dotsBox.children[current].classList.add('is-active');
  }

  function play() { timer = setInterval(function () { goSlide(current + 1); }, 5000); }
  function stop() { clearInterval(timer); }

  document.getElementById('heroPrev').addEventListener('click', function () { goSlide(current - 1); });
  document.getElementById('heroNext').addEventListener('click', function () { goSlide(current + 1); });

  var heroWrap = document.getElementById('heroWrap');
  heroWrap.addEventListener('mouseenter', stop);
  heroWrap.addEventListener('mouseleave', play);
  play();

  /* ================= 三、贷款产品 ================= */
  var productList = document.getElementById('productList');

  function renderProducts(type) {
    var list = type === 'all' ? PRODUCTS : PRODUCTS.filter(function (p) { return p.type === type; });
    productList.innerHTML = list.map(function (p) {
      var tags = p.tags.map(function (t) {
        return '<span class="tag' + (t[1] ? ' tag--' + t[1] : '') + '">' + t[0] + '</span>';
      }).join('');
      return '' +
        '<article class="product-card reveal is-in">' +
          '<div class="product-top">' +
            '<span class="product-logo"><svg><use href="#' + p.icon + '"/></svg></span>' +
            '<div><h3 class="product-name">' + p.name + '</h3><p class="product-bank">' + p.bank + '</p></div>' +
          '</div>' +
          '<div class="product-rate">' +
            '<span class="rate-num">' + p.rate + '</span><span class="rate-unit">%</span>' +
            '<span class="rate-label">年化利率（单利）</span>' +
          '</div>' +
          '<div class="product-info">' +
            '<div>最高额度<b>' + p.limit + '</b></div>' +
            '<div>贷款期限<b>最长 ' + p.term + '</b></div>' +
          '</div>' +
          '<div class="product-tags">' + tags + '</div>' +
          '<p class="product-desc">' + p.desc + '</p>' +
          '<div class="product-actions">' +
            '<button class="btn btn--primary btn--sm" data-api="立即申请">立即申请</button>' +
            '<button class="btn btn--ghost btn--sm" data-api="查看详情">查看详情</button>' +
          '</div>' +
        '</article>';
    }).join('');
  }

  renderProducts('all');

  document.getElementById('productTabs').addEventListener('click', function (e) {
    var tab = e.target.closest('.tab');
    if (!tab) return;
    this.querySelectorAll('.tab').forEach(function (t) { t.classList.remove('is-active'); });
    tab.classList.add('is-active');
    renderProducts(tab.dataset.type);
  });

  /* ================= 四、贷款计算器 ================= */
  var amountInput = document.getElementById('amountInput');
  var amountSlider = document.getElementById('amountSlider');
  var amountEcho = document.getElementById('amountEcho');
  var state = { amount: 300000, term: 24, rate: 3.65, method: 'equal' };

  function fmt(n) { return n.toLocaleString('zh-CN'); }

  function syncSlider() {
    var p = (state.amount - amountSlider.min) / (amountSlider.max - amountSlider.min) * 100;
    amountSlider.style.setProperty('--p', p + '%');
  }

  function calculate() {
    var P = state.amount;
    var n = state.term;
    var i = state.rate / 100 / 12;
    var monthly = 0, interest = 0, total = 0, desc = '';

    if (state.method === 'equal') {
      if (i === 0) { monthly = P / n; } else {
        monthly = P * i * Math.pow(1 + i, n) / (Math.pow(1 + i, n) - 1);
      }
      total = monthly * n;
      interest = total - P;
      desc = '等额本息 · 每月固定还款';
    } else if (state.method === 'interest') {
      monthly = P * i;
      interest = monthly * n;
      total = P + interest;
      desc = '先息后本 · 每月付息 ' + fmt(Math.round(monthly)) + ' 元，到期归还本金';
    } else {
      interest = P * i * n;
      total = P + interest;
      monthly = total;
      desc = '到期一次性还本付息 · 第 ' + n + ' 期结清';
    }

    var pct = total > 0 ? (interest / total * 100) : 0;

    document.getElementById('monthlyPay').textContent = fmt(monthly.toFixed(2));
    document.getElementById('monthlyDesc').textContent = desc;
    document.getElementById('totalPrincipal').textContent = '¥ ' + fmt(P);
    document.getElementById('totalInterest').textContent = '¥ ' + fmt(interest.toFixed(2));
    document.getElementById('totalRepay').textContent = '¥ ' + fmt(total.toFixed(2));
    document.getElementById('totalTerm').textContent = n + ' 期';
    document.getElementById('ring').style.setProperty('--pct', pct.toFixed(1) + '%');
    document.getElementById('ringPct').textContent = pct.toFixed(1) + '%';
    document.getElementById('calcSummary').textContent =
      (P / 10000).toFixed(P % 10000 === 0 ? 0 : 1) + ' 万元 · ' + n + ' 期 · 年化 ' + state.rate + '%';
  }

  amountInput.addEventListener('input', function () {
    var v = parseInt(this.value, 10);
    if (isNaN(v) || v < 10000) v = 10000;
    if (v > 3000000) v = 3000000;
    state.amount = v;
    amountSlider.value = v;
    amountEcho.textContent = fmt(v) + ' 元';
    syncSlider();
    calculate();
  });

  amountInput.addEventListener('blur', function () {
    this.value = state.amount;
    amountEcho.textContent = fmt(state.amount) + ' 元';
  });

  amountSlider.addEventListener('input', function () {
    state.amount = parseInt(this.value, 10);
    amountInput.value = state.amount;
    amountEcho.textContent = fmt(state.amount) + ' 元';
    syncSlider();
    calculate();
  });

  function bindChips(wrapId, key, parser) {
    document.getElementById(wrapId).addEventListener('click', function (e) {
      var chip = e.target.closest('.chip');
      if (!chip) return;
      this.querySelectorAll('.chip').forEach(function (c) { c.classList.remove('is-active'); });
      chip.classList.add('is-active');
      state[key] = parser(chip.dataset[key]);
      calculate();
    });
  }

  bindChips('termChips', 'term', parseInt);
  bindChips('rateChips', 'rate', parseFloat);
  bindChips('methodChips', 'method', String);

  syncSlider();
  calculate();

  /* ================= 五、新闻资讯 ================= */
  var newsList = document.getElementById('newsList');

  function renderNews(kind) {
    newsList.innerHTML = NEWS[kind].map(function (n) {
      return '' +
        '<article class="news-item" data-api="查看资讯详情">' +
          '<div class="news-date"><b>' + n.d + '</b><span>' + n.m + '</span></div>' +
          '<div class="news-item-body">' +
            '<h3 class="news-item-title">' + n.title + '</h3>' +
            '<p class="news-item-desc">' + n.desc + '</p>' +
            '<div class="news-item-meta">' +
              '<span class="tag">' + n.src + '</span>' +
              '<span>阅读量 ' + n.view + '</span>' +
            '</div>' +
          '</div>' +
        '</article>';
    }).join('');
  }

  renderNews('agri');

  document.getElementById('newsTabs').addEventListener('click', function (e) {
    var tab = e.target.closest('.tab');
    if (!tab) return;
    this.querySelectorAll('.tab').forEach(function (t) { t.classList.remove('is-active'); });
    tab.classList.add('is-active');
    renderNews(tab.dataset.news);
  });

  document.getElementById('hotRank').innerHTML = HOT.map(function (t, i) {
    return '<div class="rank-item" data-api="查看资讯详情">' +
      '<span class="rank-no">' + (i + 1) + '</span>' +
      '<span class="rank-text">' + t + '</span></div>';
  }).join('');

  /* ================= 六、政策资讯 ================= */
  document.getElementById('policyList').innerHTML = POLICIES.map(function (p) {
    var cls = p.catType === 'yellow' ? 'tag--yellow' : (p.catType === 'red' ? 'tag--red' : '');
    return '' +
      '<article class="policy-card reveal is-in" data-api="查看政策详情">' +
        '<div class="policy-img"><img src="' + p.img + '" alt="' + p.title + '"></div>' +
        '<div class="policy-body">' +
          '<span class="tag ' + cls + '">' + p.cat + '</span>' +
          '<h3>' + p.title + '</h3>' +
          '<p>' + p.desc + '</p>' +
          '<div class="policy-foot"><span>' + p.date + ' · ' + p.from + '</span><a href="#">阅读全文</a></div>' +
        '</div>' +
      '</article>';
  }).join('');
})();
