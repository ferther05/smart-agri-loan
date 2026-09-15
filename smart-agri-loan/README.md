# 智能惠农信贷系统 · Web 前端

纯静态前端原型：HTML5 + CSS3 + 原生 JavaScript，后端暂不接入。

## 交互约定
所有需要与后端交互的按钮，统一使用 `data-api="接口名称"` 属性，
点击后弹出「接口名称 + ！」的提示弹窗（如：立即申请！），由 `js/common.js` 全局委托处理。

## 目录结构
```
smart-agri-loan/
├─ index.html              首页
├─ css/
│  ├─ common.css           全局变量、重置、通用组件（按钮/卡片/弹窗）
│  └─ index.css            首页样式
├─ js/
│  ├─ common.js            接口弹窗、导航交互、数字滚动、返回顶部
│  └─ index.js             轮播、产品筛选、贷款计算器、资讯切换
└─ assets/images/          绿色系农业主题 SVG 插画
```

## 设计规范
- 主色：绿色系 `#1B5E20 / #2E7D32 / #43A047 / #81C784 / #E8F5E9`
- 辅色：红 `#E53935`、黄 `#FDD835 / #F9A825`
- 圆角：卡片 22px，按钮胶囊形；阴影统一低饱和绿调

## 运行
双击 `index.html` 即可，或使用 VS Code Live Server。
