/**
 * Playwright 自动化测试配置
 * 运行方式（需先启动本地静态服务：python -m http.server 5500）：
 *   npx playwright test --config tests/playwright.config.js
 * 说明：若环境无法下载 Playwright 自带浏览器，可通过 CHROME_PATH 指定本机 Chrome/Edge
 */
const CHROME_PATH =
  process.env.CHROME_PATH || 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

/** 直接导出配置对象（兼容未导出 defineConfig 的运行环境） */
module.exports = {
  testDir: '.',
  // legacy/ 下是"前端本地 localStorage 数据池"时代的用例，与后端架构不兼容，仅作归档
  testIgnore: ['legacy/**'],
  timeout: 30000,
  expect: { timeout: 6000 },
  reporter: [['list']],
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:5500',
    headless: true,
    viewport: { width: 1440, height: 900 },
    launchOptions: { executablePath: CHROME_PATH }
  }
};
