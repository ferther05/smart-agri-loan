/**
 * 智能惠农信贷系统 —— Playwright 端到端自动化测试
 * 覆盖：产品浏览 → 贷款申请 → 后台审批 → 模拟签约/放款/还款 全链路，
 *      并包含表单校验、余额不足、数据污染等异常容错场景与响应式检查。
 * 运行：npx playwright test --config tests/playwright.config.js
 */
const { test, expect } = require('@playwright/test');

const DB_KEY = 'SMART_AGRI_LOAN_DB_V1';
const VALID_ID_CARD = '11010519491231002X'; // 校验位合法
const INVALID_ID_CARD = '110105194912310021'; // 校验位错误

/** 读取数据池（页面内执行） */
async function readDB(page) {
  return page.evaluate((key) => JSON.parse(localStorage.getItem(key)), DB_KEY);
}

/** 写入数据池后刷新页面 */
async function writeDB(page, db) {
  await page.evaluate(([key, value]) => localStorage.setItem(key, value), [DB_KEY, JSON.stringify(db)]);
  await page.reload();
  await page.waitForSelector('#app:not([style*="display"])');
}

test.describe.configure({ mode: 'serial' });

let applyId = ''; // 主链路中生成的申请编号，供后续用例复用

/* ---------------- 只读展示 ---------------- */
test('TC-01 首页渲染统计、热门产品与资讯', async ({ page }) => {
  await page.goto('/index.html');
  await expect(page.locator('.stat-card')).toHaveCount(4);
  await expect(page.locator('.product-grid .product-card')).toHaveCount(3);
  await expect(page.locator('.steps .step')).toHaveCount(4);
  await expect(page.locator('.news-mini-item')).toHaveCount(3);
});

test('TC-02/03/04 产品列表、筛选与详情弹窗', async ({ page }) => {
  await page.goto('/pages/products.html');
  const cards = page.locator('.product-card');
  await expect(cards).toHaveCount(6);
  await expect(cards.first()).toHaveAttribute('data-product-id', /^P\d{3}$/);

  await page.locator('.filter-group', { hasText: '产品类型' }).getByText('政策贷', { exact: true }).click();
  await expect(cards).toHaveCount(2);

  await cards.first().locator('[data-action="detail"]').click();
  await expect(page.locator('.modal')).toBeVisible();
  await page.locator('.modal-close').click();
  await expect(page.locator('.modal')).toBeHidden();
});

test('TC-05/06 资讯列表、分类切换与详情弹窗', async ({ page }) => {
  await page.goto('/pages/news.html');
  await expect(page.locator('.news-item')).toHaveCount(6);
  await page.locator('.news-tabs .chip', { hasText: '政策解读' }).click();
  await expect(page.locator('.news-item')).toHaveCount(2);
  await page.locator('.news-item-title').first().click();
  await expect(page.locator('.modal')).toBeVisible();
  await expect(page.locator('.modal-body p')).not.toHaveCount(1);
});

test('TC-07 信用积分页图表与日志', async ({ page }) => {
  await page.goto('/pages/credit.html');
  const offset = await page.locator('.gauge-fg').getAttribute('stroke-dashoffset');
  expect(Number(offset)).toBeGreaterThan(0);
  const points = await page.locator('.radar-area').getAttribute('points');
  expect(points.trim().split(/\s+/).length).toBe(5);
  await expect(page.locator('.table tbody tr')).toHaveCount(6);
});

/* ---------------- 贷款申请 ---------------- */
test('TC-08/09/10/11/12 多步骤表单与异常校验', async ({ page }) => {
  await page.goto('/pages/apply.html');
  const next = page.locator('.form-actions button', { hasText: '下一步' });
  const prev = page.locator('.form-actions button', { hasText: '上一步' });

  // 产品下拉数据来源于数据池
  await expect(page.locator('.apply-form select').first().locator('option')).toHaveCount(7);

  // TC-09 必填项留空
  await next.click();
  await expect(page.locator('.form-error', { hasText: '请选择贷款产品' })).toBeVisible();
  await expect(page.locator('.step-bar-item.active .step-bar-text')).toHaveText('选择产品与金额');

  // TC-12 金额越界
  await page.locator('.apply-form select').first().selectOption('P001');
  await page.locator('.apply-form input').first().fill('500');
  await page.locator('.apply-form select').nth(1).selectOption('12');
  await page.locator('.apply-form textarea').first().fill('购买春耕化肥与茶苗');
  await next.click();
  await expect(page.locator('.form-error', { hasText: '申请金额需在' })).toBeVisible();

  // 修正金额后进入第二步
  await page.locator('.apply-form input').first().fill('80000');
  await next.click();
  await expect(page.locator('.step-bar-item.active .step-bar-text')).toHaveText('填写申请人信息');

  // TC-10 身份证校验位错误
  const inputs = page.locator('.apply-form input');
  await inputs.nth(1).fill('张大山');
  await inputs.nth(2).fill(INVALID_ID_CARD);
  await next.click();
  await expect(page.locator('.form-error', { hasText: '校验位不通过' })).toBeVisible();

  // TC-11 手机号格式错误
  await inputs.nth(2).fill(VALID_ID_CARD);
  await inputs.nth(3).fill('12345');
  await next.click();
  await expect(page.locator('.form-error', { hasText: '手机号格式不正确' })).toBeVisible();

  // TC-08 上一步 / 下一步切换
  await prev.click();
  await expect(page.locator('.step-bar-item.active .step-bar-text')).toHaveText('选择产品与金额');
  await next.click();

  // 补齐第二步
  await inputs.nth(3).fill('13812345678');
  await inputs.nth(4).fill('贵州省遵义市湄潭县永兴镇');
  await page.locator('.apply-form select').nth(2).selectOption('种植业');
  await next.click();
  await expect(page.locator('.step-bar-item.active .step-bar-text')).toHaveText('确认并提交');
});

test('TC-13/14 提交申请并跳转反馈页', async ({ page }) => {
  await page.goto('/pages/apply.html');
  const next = page.locator('.form-actions button', { hasText: '下一步' });
  const inputs = page.locator('.apply-form input');

  await page.locator('.apply-form select').first().selectOption('P001');
  await inputs.nth(0).fill('80000');
  await page.locator('.apply-form select').nth(1).selectOption('12');
  await page.locator('.apply-form textarea').first().fill('购买春耕化肥与茶苗');
  await next.click();

  await inputs.nth(1).fill('张大山');
  await inputs.nth(2).fill(VALID_ID_CARD);
  await inputs.nth(3).fill('13812345678');
  await inputs.nth(4).fill('贵州省遵义市湄潭县永兴镇');
  await page.locator('.apply-form select').nth(2).selectOption('种植业');
  await next.click();

  // 填写还款来源后主动失焦，等待错误提示收起、布局稳定
  await page.locator('.apply-form textarea').nth(1).fill('茶叶销售收入与合作社分红');
  await page.locator('.apply-form textarea').nth(1).blur();
  await page.waitForTimeout(300);

  // 未勾选授权直接提交 -> 阻断
  await page.locator('button[type=submit]').click();
  await expect(page.locator('.form-error', { hasText: '请先勾选征信查询授权' })).toBeVisible();

  await page.locator('.apply-form input[type=checkbox]').nth(0).check();
  await page.locator('.apply-form input[type=checkbox]').nth(1).check();
  await page.locator('button[type=submit]').click();

  await page.waitForURL(/apply-result\.html\?id=/);
  await expect(page.locator('.result-id b')).toHaveText(/^SQ\d{17}$/);
  applyId = (await page.locator('.result-id b').textContent()).trim();

  const db = await readDB(page);
  const added = db.applications.find((a) => a.id === applyId);
  expect(added.status).toBe('待初审');
  expect(added.amount).toBe(80000);
  expect(added.term).toBe(12);
});

/* ---------------- 后台审批 ---------------- */
test('TC-15/16/17/18/19 审批台：通过、拒绝与风控看板', async ({ page }) => {
  await page.goto('/pages/audit.html');
  const row = page.locator('table tbody tr', { hasText: applyId });
  await expect(row).toBeVisible();
  await expect(row.locator('td').nth(6)).toHaveText('待初审');

  // TC-19 风控看板
  await row.locator('.btn-text', { hasText: '详情' }).click();
  await expect(page.locator('.risk-panel .risk-score-num')).toBeVisible();
  await expect(page.locator('.risk-badge')).toHaveText(/低风险|中风险|高风险/);
  await expect(page.locator('.modal .log-list li')).not.toHaveCount(0);

  // TC-16 初审通过
  await page.locator('.modal-foot button', { hasText: '初审通过' }).click();
  await page.locator('.modal-foot button', { hasText: '确认通过' }).click();
  await page.locator('.modal-close').click();

  const afterPass = await readDB(page);
  const passed = afterPass.applications.find((a) => a.id === applyId);
  expect(passed.status).toBe('初审通过');
  expect(passed.auditTime).not.toBe('');
  expect(passed.auditor).not.toBe('');
  expect(passed.logs.length).toBe(2);

  // TC-17/18 拒绝：先验证空意见被拦截，再验证拒绝成功（使用另一条待初审单据）
  const pendingRow = page.locator('table tbody tr', { hasText: '待初审' }).first();
  await pendingRow.locator('.btn-text', { hasText: '拒绝' }).click();
  await page.locator('.modal-foot button', { hasText: '确认拒绝' }).click();
  await expect(page.locator('.form-error', { hasText: '必须填写审批意见' })).toBeVisible();
  await page.locator('.modal .modal-body .textarea').fill('申请材料不完整，请补充经营流水');
  await page.locator('.modal-foot button', { hasText: '确认拒绝' }).click();

  const afterReject = await readDB(page);
  expect(afterReject.applications.some((a) => a.status === '已拒绝')).toBeTruthy();
});

/* ---------------- 业务闭环 ---------------- */
test('TC-20/21 模拟签约与模拟放款', async ({ page }) => {
  await page.goto('/pages/my-apply.html');
  const card = page.locator('.apply-item', { hasText: applyId });
  await expect(card).toBeVisible();

  await card.locator('.apply-actions button', { hasText: '模拟签约' }).click();
  await expect(card.locator('.apply-actions button', { hasText: '模拟放款' })).toBeVisible();

  const beforeLoan = await readDB(page);
  const balanceBefore = beforeLoan.users[0].balance;
  await card.locator('.apply-actions button', { hasText: '模拟放款' }).click();

  const afterLoan = await readDB(page);
  const target = afterLoan.applications.find((a) => a.id === applyId);
  expect(target.status).toBe('已放款');
  expect(afterLoan.users[0].balance).toBeCloseTo(balanceBefore + 80000, 2);
});

test('TC-22 余额不足时还款被阻断', async ({ page }) => {
  await page.goto('/pages/my-apply.html');
  const db = await readDB(page);
  db.users[0].balance = 0;
  await writeDB(page, db);

  const card = page.locator('.apply-item', { hasText: applyId });
  await card.locator('.apply-actions button', { hasText: '模拟还款' }).click();
  await expect(card.locator('.reject-error')).toContainText('账户余额不足');

  const after = await readDB(page);
  expect(after.applications.find((a) => a.id === applyId).status).toBe('已放款');
});

test('TC-23 模拟还款：扣本息 + 结清 + 积分 +5', async ({ page }) => {
  await page.goto('/pages/my-apply.html');
  const db = await readDB(page);
  db.users[0].balance = 100000;
  await writeDB(page, db);

  const before = await readDB(page);
  const scoreBefore = before.users[0].creditScore;
  const logCountBefore = before.creditLogs.length;
  const card = page.locator('.apply-item', { hasText: applyId });
  await card.locator('.apply-actions button', { hasText: '模拟还款' }).click();

  const after = await readDB(page);
  const target = after.applications.find((a) => a.id === applyId);
  expect(target.status).toBe('已结清');
  expect(target.repayAmount).toBeCloseTo(82920, 2);
  expect(after.users[0].balance).toBeCloseTo(100000 - 82920, 2);
  expect(after.users[0].creditScore).toBe(scoreBefore + 5);
  expect(after.creditLogs.length).toBe(logCountBefore + 1);
  expect(after.creditLogs[0].delta).toBe(5);

  // 信用积分页联动
  await page.goto('/pages/credit.html');
  await expect(page.locator('.gauge-score')).toHaveText(String(scoreBefore + 5));
});

/* ---------------- 响应式与容错 ---------------- */
test('TC-24 手机视口下无横向溢出', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  for (const url of ['/index.html', '/pages/products.html', '/pages/my-apply.html', '/pages/audit.html']) {
    await page.goto(url);
    const overflow = await page.evaluate(
      () => document.documentElement.scrollWidth - document.documentElement.clientWidth
    );
    expect(overflow, `${url} 不应出现横向滚动`).toBeLessThanOrEqual(2);
  }
});

test('TC-25 数据池被污染后自动重建', async ({ page }) => {
  await page.goto('/index.html');
  await page.evaluate((key) => localStorage.setItem(key, 'not-a-json'), DB_KEY);
  await page.reload();
  await expect(page.locator('.stat-card')).toHaveCount(4);
  await expect(page.locator('.product-grid .product-card')).toHaveCount(3);
});
