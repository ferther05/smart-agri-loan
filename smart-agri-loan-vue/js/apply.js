/**
 * 贷款申请页：多步骤表单 + 正则校验 + 提交到后端
 *
 * 对接说明：
 * - 产品下拉选项来自 /api/products（与产品页同源，不在页面写死）；
 * - 提交调用 POST /api/applications，成功后跳转反馈页；
 * - 顶部按当前信用等级展示差异化提示（额度上限、是否需要担保、审批时效）。
 */
(function (global) {
  'use strict';

  var CONST = global.APP_CONST;

  global.App.mountApp({
    guard: 'applicant',
    needCredit: true,
    data: function () {
      var db = global.DB.getDB();
      var user = global.U.currentUser(db);
      var info = db.creditInfo || {};
      var presetProductId = '';
      try {
        presetProductId = sessionStorage.getItem(CONST.SESSION_KEYS.SELECTED_PRODUCT) || '';
      } catch (e) {
        presetProductId = '';
      }
      return {
        U: global.U,
        db: db,
        user: user,
        policy: info.policy || {},
        step: 1,
        stepTitles: ['选择产品与金额', '填写申请人信息', '确认并提交'],
        businessOptions: ['种植业', '养殖业', '农产品加工', '农机服务', '农资经销', '其他'],
        submitting: false,
        errors: {},
        form: {
          productId: presetProductId,
          amount: '',
          term: '',
          purpose: '',
          name: user ? user.name : '',
          idCard: '',
          phone: user ? user.phone : '',
          address: user ? user.address : '',
          business: '',
          scale: user ? user.scale : '',
          repaySource: '',
          agreeCredit: false,
          agreeTruth: false
        }
      };
    },
    computed: {
      currentProduct: function () {
        return global.U.findProduct(this.db, this.form.productId);
      },
      termOptions: function () {
        return this.currentProduct ? this.currentProduct.terms : [];
      },
      /** 自适应提示：按后端下发的信用政策渲染 */
      creditHint: function () {
        var p = this.policy;
        if (!p || !p.level) return '';
        var parts = ['当前信用等级 ' + p.level + '（' + (p.levelName || '') + '）'];
        if (p.maxPureCreditAmount != null) {
          parts.push(Number(p.maxPureCreditAmount) > 0
            ? '最高纯信用额度 ' + global.U.toWan(p.maxPureCreditAmount) + ' 元'
            : '暂不支持纯信用贷款');
        }
        if (p.rateDiscountBp) parts.push('利率下浮 ' + p.rateDiscountBp + 'BP');
        if (p.requiresGuarantee) parts.push('需补充担保 / 增信材料');
        if (p.approvalDesc) parts.push(p.approvalDesc);
        return parts.join('，');
      },
      /** 当前选中产品的准入提示 */
      productTip: function () {
        var p = this.currentProduct;
        if (!p) return '';
        return p.applyTip || '';
      },
      canSubmit: function () {
        var p = this.currentProduct;
        return !!p && p.canApply !== false;
      },
      amountTip: function () {
        var p = this.currentProduct;
        var amount = Number(this.form.amount);
        var term = Number(this.form.term);
        if (!p || !amount || !term) return '请先填写金额与期限';
        var monthly = (amount * (1 + (p.rate / 100) * (term / 12))) / term;
        return '按 ' + term + ' 期估算，月供约 ' + global.U.thousand(Math.round(monthly)) + ' 元';
      },
      maskIdCard: function () {
        return this.form.idCard.replace(/^(.{6}).*(.{4})$/, '$1********$2');
      },
      maskPhone: function () {
        return this.form.phone.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2');
      }
    },
    mounted: function () {
      if (new URLSearchParams(location.search).get('nav') === '1') {
        global.U.toast('已进入贷款申请页，请选择产品开始办理', 'success');
      }
    },
    methods: {
      rules: function () {
        var that = this;
        return {
          productId: function () {
            if (!that.form.productId) return '请选择贷款产品';
            if (!that.canSubmit) return that.productTip || '当前信用等级暂不符合该产品准入条件';
            return '';
          },
          amount: function () {
            var val = String(that.form.amount).trim();
            if (!val) return '请输入申请金额';
            if (!CONST.REGEX.AMOUNT.test(val)) return '申请金额必须为大于 0 的整数（单位：元）';
            var p = that.currentProduct;
            var num = Number(val);
            if (p && (num < p.minAmount || num > p.maxAmount)) {
              return '申请金额需在 ' + global.U.thousand(p.minAmount) + ' ~ ' + global.U.thousand(p.maxAmount) + ' 元之间';
            }
            return '';
          },
          term: function () {
            return that.form.term ? '' : '请选择贷款期限';
          },
          purpose: function () {
            var val = that.form.purpose;
            if (!val) return '请填写贷款用途';
            return val.length < 5 ? '贷款用途不少于 5 个字，请具体描述' : '';
          },
          name: function () {
            var val = that.form.name;
            if (!val) return '请输入申请人姓名';
            return CONST.REGEX.NAME.test(val) ? '' : '姓名格式不正确（2-20 位中文或英文）';
          },
          idCard: function () {
            var val = that.form.idCard;
            if (!val) return '请输入身份证号';
            if (!CONST.REGEX.ID_CARD.test(val)) return '身份证号格式不正确，请输入 18 位号码';
            return global.U.checkIdCard(val) ? '' : '身份证号校验位不通过，请核对后重填';
          },
          phone: function () {
            var val = that.form.phone;
            if (!val) return '请输入手机号码';
            return CONST.REGEX.PHONE.test(val) ? '' : '手机号格式不正确，请输入 11 位手机号';
          },
          address: function () {
            var val = that.form.address;
            if (!val) return '请填写家庭住址';
            return val.length < 5 ? '请填写完整的家庭住址' : '';
          },
          business: function () {
            return that.form.business ? '' : '请选择经营类型';
          },
          repaySource: function () {
            var val = that.form.repaySource;
            if (!val) return '请填写还款来源';
            return val.length < 5 ? '还款来源不少于 5 个字' : '';
          },
          agreeCredit: function () {
            return that.form.agreeCredit ? '' : '请先勾选征信查询授权';
          },
          agreeTruth: function () {
            return that.form.agreeTruth ? '' : '请先确认信息真实性承诺';
          }
        };
      },
      validateField: function (field) {
        var rule = this.rules()[field];
        if (!rule) return true;
        var msg = rule();
        if (msg) this.errors[field] = msg;
        else delete this.errors[field];
        return !msg;
      },
      stepFields: function (step) {
        return [
          ['productId', 'amount', 'term', 'purpose'],
          ['name', 'idCard', 'phone', 'address', 'business'],
          ['repaySource', 'agreeCredit', 'agreeTruth']
        ][step - 1];
      },
      validateStep: function (step) {
        var fields = this.stepFields(step);
        var ok = true;
        var firstError = '';
        for (var i = 0; i < fields.length; i++) {
          if (!this.validateField(fields[i])) {
            ok = false;
            if (!firstError) firstError = this.errors[fields[i]];
          }
        }
        return { ok: ok, message: firstError };
      },
      nextStep: function () {
        var result = this.validateStep(this.step);
        if (!result.ok) {
          global.U.toast(result.message, 'error');
          return;
        }
        this.step += 1;
        window.scrollTo(0, 0);
      },
      prevStep: function () {
        if (this.step > 1) this.step -= 1;
        window.scrollTo(0, 0);
      },
      onProductChange: function () {
        if (this.termOptions.indexOf(Number(this.form.term)) === -1) this.form.term = '';
        this.validateField('productId');
        if (this.form.amount) this.validateField('amount');
        if (this.form.term) this.validateField('term');
      },
      onSubmit: function () {
        for (var s = 1; s <= 3; s++) {
          var result = this.validateStep(s);
          if (!result.ok) {
            this.step = s;
            global.U.toast('第 ' + s + ' 步：' + result.message, 'error');
            return;
          }
        }
        this.submit();
      },
      /** 提交到后端：POST /api/applications */
      submit: function () {
        var that = this;
        this.submitting = true;
        global.API.createApplication({
          productId: this.currentProduct.id,
          amount: Number(this.form.amount),
          term: Number(this.form.term),
          purpose: this.form.purpose,
          name: this.form.name,
          idCard: this.form.idCard,
          phone: this.form.phone,
          address: this.form.address,
          business: this.form.business,
          scale: this.form.scale,
          repaySource: this.form.repaySource,
          agreeCredit: this.form.agreeCredit,
          agreeTruth: this.form.agreeTruth
        }).then(function (application) {
          that.submitting = false;
          try {
            sessionStorage.removeItem(CONST.SESSION_KEYS.SELECTED_PRODUCT);
          } catch (e) {
            /* 忽略 */
          }
          global.U.toast('申请提交成功，编号 ' + application.id, 'success');
          setTimeout(function () {
            location.href = 'apply-result.html?id=' + application.id;
          }, 500);
        }).catch(function () {
          that.submitting = false;
        });
      }
    }
  });
})(window);
