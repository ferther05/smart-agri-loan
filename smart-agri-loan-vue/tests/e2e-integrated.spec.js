/**
 * 智能惠农信贷系统 —— 前后端联调端到端测试（对接 Spring Boot 后端）
 *
 * 前置条件：
 *   1. 后端已启动：cd smart-agri-loan-server && mvn spring-boot:run（端口 8080）
 *   2. 前端静态服务：python -m http.server 5500（在 smart-agri-loan-vue 目录下）
 *   3. 运行：npx playwright test tests/e2e-integrated.spec.js --config tests/playwright.config.js
 *
 * 覆盖：
 *   - 未登录访问受限页面被拦截并跳转登录页
 *   - 农户 / 审批人员两种角色的导航显隐（无权限的功能前端不展示）
 *   - 产品页按信用等级下发准入提示；审批人员不可申请
 *   - 信用页数据来自后端（积分、等级、五维、权益、授信政策）
 *   - 完整业务闭环：申请 → 审批 → 签约 → 放款 → 结清（积分 +5）
 *   - AOP 操作日志留痕与越权拦截
 */
const { test, expect } = require('@playwright/test');

const FARMER = { username: 'farmer', password: '123456' };
const AUDITOR = { username: 'auditor', password: '123456' };
const VALID_ID_CARD = '11010519491231002X';

/**
 * 一次性农户账号：注册接口 + 完整业务闭环都跑在它身上。
 *
 * 为什么不直接用种子账号 farmer？因为闭环会把积分从 762 改成 767，
 * 而种子账号还被 TC-I02 / I03 / I05 断言为「AA 级 762 分」。
 * 种子数据被改写后用例就不可重复执行了（第二次跑必然失败）。
 * 改用每次运行唯一注册的新农户，所有断言都自洽且幂等。
 */
const RUN_ID = String(Date.now()).slice(-9);
const CLOSER = {
  username: 'e2e' + RUN_ID,
  password: '123456',
  name: 'e2e 试验农户'
};

test.describe.configure({ mode: 'serial' });

/** 通过登录页完成登录（农户 / 审批人员） */
async function login(page, account) {
  await page.goto('/pages/login.html');
  await page.fill('input[autocomplete="username"]', account.username);
  await page.fill('input[autocomplete="current-password"]', account.password);
  await page.click('button[type="submit"]');
  await page.waitForURL(/index\.html|audit\.html/, { timeout: 15000 });
}

/** 注册一个全新的农户账号（初始信用 660 分 / A 级），注册成功后自动登录 */
async function registerFarmer(page) {
  await page.goto('/pages/login.html');
  await page.click('.auth-tabs button:has-text("注册账号")');
  await page.fill('input[placeholder="4-20 位字母 / 数字 / 下划线"]', CLOSER.username);
  await page.fill('input[placeholder="6-30 位"]', CLOSER.password);
  await page.fill('input[placeholder="如：张大山 / XX 合作社"]', CLOSER.name);
  await page.fill('input[placeholder="11 位手机号"]', '13800138001');
  await page.fill('input[placeholder="如：32 亩 / 社员 126 户"]', '18 亩');
  await page.fill('input[placeholder="如：茶叶种植与初加工"]', '水稻种植与稻虾共作');
  await page.fill('input[placeholder="如：贵州省遵义市湄潭县永兴镇"]', '贵州省遵义市湄潭县永兴镇');
  await page.click('button:has-text("注册并登录")');
  await page.waitForURL(/index\.html/, { timeout: 15000 });
}

test('TC-I01 未登录访问「我的申请」被拦截到登录页', async ({ page }) => {
  await page.goto('/pages/my-apply.html');
  await page.waitForURL(/login\.html/, { timeout: 15000 });
  expect(page.url()).toContain('redirect=');
  await expect(page.locator('.auth-tabs')).toBeVisible();
});

test('TC-I02 农户登录后导航按角色显隐，首页展示信用自适应卡片', async ({ page }) => {
  await login(page, FARMER);

  // 导航：只有农户可见的菜单，且不含审批端专属入口
  const navText = await page.locator('.nav-links').innerText();
  expect(navText).toContain('信用积分');
  expect(navText).toContain('我的申请');
  expect(navText).not.toContain('操作日志');
  await expect(page.locator('.nav-actions')).not.toContainText('审批端');
  await expect(page.locator('.nav-actions')).toContainText('申请贷款');

  // 首页信用概览来自后端：等级 + 等级权益
  await expect(page.locator('.hero-card')).toContainText('我的信用概览');
  await expect(page.locator('.hero-card')).toContainText('AA');
  await expect(page.locator('.hero-benefits li')).toHaveCount(4);
});

test('TC-I03 产品页准入提示由后端按信用等级下发', async ({ page }) => {
  await login(page, FARMER);
  await page.goto('/pages/products.html');
  await expect(page.locator('.product-card')).toHaveCount(6);
  // 每张卡片都有后端下发的准入提示
  await expect(page.locator('.product-credit-tip')).toHaveCount(6);
  await expect(page.locator('.product-credit-tip').first()).toContainText('信用等级 AA');
});

test('TC-I04 审批人员打开产品页：不可申请（权限与角色自适应）', async ({ page }) => {
  await login(page, AUDITOR);
  // 审批人员登录后直接进入审批端
  expect(page.url()).toContain('audit.html');

  await page.goto('/pages/products.html');
  await expect(page.locator('.product-card [data-action="apply"]').first())
    .toContainText('暂不可申请');
  await expect(page.locator('.product-credit-tip').first())
    .toContainText('审批人员账号不参与贷款申请');
});

test('TC-I05 信用页数据来自后端：积分、等级、五维、权益与授信政策', async ({ page }) => {
  await login(page, FARMER);
  await page.goto('/pages/credit.html');

  await expect(page.locator('.credit-gauge-card')).toContainText('762');
  await expect(page.locator('.credit-gauge-card')).toContainText('AA');
  await expect(page.locator('.radar-values .radar-value')).toHaveCount(5);

  // 我的信用权益：后端按 AA 等级下发 4 条
  await expect(page.locator('.rights-list li')).toHaveCount(4);
  await expect(page.locator('.rights-list')).toContainText('最高 30 万纯信用额度');

  // 授信政策（自适应核心）
  await expect(page.locator('.policy-banner')).toContainText('信用良好');
  await expect(page.locator('.policy-card').nth(0)).toContainText('30 万');
  await expect(page.locator('.policy-card').nth(1)).toContainText('15 BP');
  await expect(page.locator('.policy-card').nth(2)).toContainText('极速审批');

  // 积分变动日志来自数据库
  await expect(page.locator('table.table tbody tr').first()).toContainText('2026-');
});

let newApplyId = '';

test('TC-I06 注册新农户并提交贷款申请写入数据库', async ({ page }) => {
  await registerFarmer(page);
  await page.goto('/pages/apply.html');

  // 第一步：产品 / 金额 / 期限 / 用途
  // 新农户初始 660 分 → A 级，准入提示由后端按等级下发（与种子账号 AA 级不同）
  await expect(page.locator('.credit-hint')).toContainText('当前信用等级 A');
  await expect(page.locator('.credit-hint')).toContainText('信用一般');
  await page.locator('select.select').nth(0).selectOption('P001');
  await page.fill('input[placeholder="请输入整数金额"]', '50000');
  await page.locator('select.select').nth(1).selectOption('12');
  await page.locator('textarea.textarea').nth(0).fill('春茶采摘人工费与茶苗采购周转');
  await page.click('button:has-text("下一步")');

  // 第二步：申请人信息
  await page.fill('input[placeholder="请输入真实姓名"]', '张大山');
  await page.fill('input[placeholder="请输入 18 位身份证号"]', VALID_ID_CARD);
  await page.fill('input[placeholder="请输入 11 位手机号"]', '13880666721');
  await page.fill('input[placeholder="省 / 市 / 县 / 乡镇"]', '贵州省遵义市湄潭县永兴镇');
  await page.locator('select.select').nth(2).selectOption({ index: 1 });
  await page.click('button:has-text("下一步")');

  // 第三步：还款来源 + 授权
  await page.locator('textarea.textarea').nth(1).fill('茶叶销售回款与合作社分红');
  await page.check('#agreeCredit');
  await page.check('#agreeTruth');
  await page.click('button[type="submit"]');

  // 跳转反馈页，取出申请编号
  await page.waitForURL(/apply-result\.html\?id=/, { timeout: 15000 });
  newApplyId = new URL(page.url()).searchParams.get('id') || '';
  expect(newApplyId).toMatch(/^SQ\d+$/);
  await expect(page.locator('body')).toContainText('待初审');
});

test('TC-I07 审批端初审通过并写入流转日志', async ({ page }) => {
  await login(page, AUDITOR);
  await page.goto('/pages/audit.html');
  await page.locator('.news-tabs button', { hasText: '待初审' }).click();

  const row = page.locator('tr', { hasText: newApplyId });
  await expect(row).toBeVisible();
  await row.locator('button', { hasText: '通过' }).click();
  await expect(page.locator('.modal')).toBeVisible();
  await page.click('button:has-text("确认通过")');

  await page.waitForTimeout(1200);
  await page.locator('.news-tabs button', { hasText: '初审通过' }).click();
  const passed = page.locator('tr', { hasText: newApplyId });
  await expect(passed).toBeVisible();
});

test('TC-I08 农户签约 → 放款入账 → 结清，信用积分 +5', async ({ page }) => {
  await login(page, CLOSER);

  // 记录结清前积分（新农户 660），后续断言 +5 用相对值，避免污染种子账号
  // 注意：.gauge-score 是 SVG <text>，只能取 textContent（innerText 仅适用于 HTMLElement）
  await page.goto('/pages/credit.html');
  // 先等积分真正渲染出来再读取，避免 v-cloak 未卸载时读到空文本
  await expect(page.locator('.gauge-score')).toHaveText(/\d+/, { timeout: 15000 });
  const scoreBefore = Number((await page.locator('.gauge-score').textContent()).trim());
  expect(scoreBefore).toBe(660);

  await page.goto('/pages/my-apply.html');
  const card = page.locator('.apply-item', { hasText: newApplyId }).first();
  await expect(card).toBeVisible({ timeout: 15000 });

  // 签约
  await card.locator('button', { hasText: '在线签约' }).click();
  await expect(card.locator('button', { hasText: '确认放款（资金入账）' })).toBeVisible({ timeout: 15000 });

  // 放款：资金进入账户余额
  await card.locator('button', { hasText: '确认放款（资金入账）' }).click();
  await expect(card.locator('button', { hasText: '立即还款' })).toBeVisible({ timeout: 15000 });

  // 异常分支：放款到账 5 万，不足以覆盖含息应还额 → 还款被后端阻断
  await card.locator('button', { hasText: '立即还款' }).click();
  await expect(card.locator('.reject-error')).toBeVisible({ timeout: 10000 });
  await expect(card.locator('button', { hasText: '立即还款' })).toHaveCount(1);

  // 充值后余额充足，正常结清
  await page.locator('button', { hasText: '账户充值' }).click();
  await page.waitForTimeout(1000);

  await card.locator('button', { hasText: '立即还款' }).click();
  await expect(card.locator('button', { hasText: '立即还款' })).toHaveCount(0, { timeout: 15000 });
  await expect(card).toContainText('结清');

  // 结清后信用积分 +5，且积分日志追加了本次结清记录
  await page.goto('/pages/credit.html');
  await expect(page.locator('.gauge-score')).toHaveText(String(scoreBefore + 5));
  await expect(page.locator('table.table tbody tr', { hasText: '结清' }).first()).toBeVisible();
});

test('TC-I09 AOP 操作日志可查询，且农户越权访问被拒', async ({ page }) => {
  await login(page, AUDITOR);
  await page.goto('/pages/op-logs.html');

  await expect(page.locator('.log-filter')).toBeVisible();
  const table = page.locator('table.table');
  await expect(table).toContainText('贷款初审');
  await expect(table).toContainText('auditor');

  // 关键字查询：只看贷款申请模块
  await page.selectOption('.log-filter select', { index: 1 });
  await page.click('button:has-text("查询")');
  await expect(table).toContainText('提交贷款申请');

  // 农户越权访问日志页：被页面守卫拦截回首页
  await page.evaluate(() => localStorage.clear());
  await login(page, FARMER);
  await page.goto('/pages/op-logs.html');
  await page.waitForURL(/index\.html/, { timeout: 15000 });
});
