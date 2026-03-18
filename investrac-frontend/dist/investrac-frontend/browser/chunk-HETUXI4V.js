import {
  DefaultValueAccessor,
  FormBuilder,
  FormControlName,
  FormGroupDirective,
  FormsModule,
  MinValidator,
  NgControlStatus,
  NgControlStatusGroup,
  NgModel,
  NumberValueAccessor,
  ReactiveFormsModule,
  Validators,
  ɵNgNoValidate
} from "./chunk-DYTDY6K6.js";
import {
  WalletService
} from "./chunk-DNKGIIYI.js";
import {
  LoadingSpinnerComponent
} from "./chunk-F3IRDWQ3.js";
import {
  InrFormatPipe
} from "./chunk-IDOJ465G.js";
import "./chunk-QGHRW6JC.js";
import {
  ToastComponent
} from "./chunk-TLGCWV2M.js";
import {
  NgClass,
  ToastService,
  signal,
  ɵsetClassDebugInfo,
  ɵɵStandaloneFeature,
  ɵɵadvance,
  ɵɵconditional,
  ɵɵdefineComponent,
  ɵɵdirectiveInject,
  ɵɵelement,
  ɵɵelementEnd,
  ɵɵelementStart,
  ɵɵgetCurrentView,
  ɵɵlistener,
  ɵɵnextContext,
  ɵɵpipe,
  ɵɵpipeBind1,
  ɵɵpipeBind2,
  ɵɵproperty,
  ɵɵpureFunction1,
  ɵɵrepeater,
  ɵɵrepeaterCreate,
  ɵɵresetView,
  ɵɵrestoreView,
  ɵɵstyleProp,
  ɵɵtemplate,
  ɵɵtext,
  ɵɵtextInterpolate,
  ɵɵtextInterpolate1,
  ɵɵtwoWayBindingSet,
  ɵɵtwoWayListener,
  ɵɵtwoWayProperty
} from "./chunk-TWKZKYET.js";

// src/app/features/wallet/wallet.component.ts
var _forTrack0 = ($index, $item) => $item.key;
var _forTrack1 = ($index, $item) => $item.id;
var _c0 = (a0) => ({ "env-over": a0 });
function WalletComponent_Conditional_5_Template(rf, ctx) {
  if (rf & 1) {
    const _r1 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "button", 5);
    \u0275\u0275listener("click", function WalletComponent_Conditional_5_Template_button_click_0_listener() {
      \u0275\u0275restoreView(_r1);
      const ctx_r1 = \u0275\u0275nextContext();
      return \u0275\u0275resetView(ctx_r1.showTopUp.set(!ctx_r1.showTopUp()));
    });
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1(" ", ctx_r1.showTopUp() ? "\u2715" : "+ Top Up", " ");
  }
}
function WalletComponent_Conditional_6_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "app-loading-spinner", 4);
  }
}
function WalletComponent_Conditional_7_For_17_Template(rf, ctx) {
  if (rf & 1) {
    const _r4 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "div", 17)(1, "span", 20);
    \u0275\u0275text(2);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "label", 21);
    \u0275\u0275text(4);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "input", 22);
    \u0275\u0275listener("input", function WalletComponent_Conditional_7_For_17_Template_input_input_5_listener($event) {
      const env_r5 = \u0275\u0275restoreView(_r4).$implicit;
      const ctx_r1 = \u0275\u0275nextContext(2);
      return \u0275\u0275resetView(ctx_r1.setEnvelopeBudget(env_r5.key, $event));
    });
    \u0275\u0275elementEnd()();
  }
  if (rf & 2) {
    const env_r5 = ctx.$implicit;
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(env_r5.icon);
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(env_r5.name);
    \u0275\u0275advance();
    \u0275\u0275property("placeholder", env_r5.suggested.toString());
  }
}
function WalletComponent_Conditional_7_Conditional_19_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "span", 19);
  }
}
function WalletComponent_Conditional_7_Template(rf, ctx) {
  if (rf & 1) {
    const _r3 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "div", 6)(1, "div", 7);
    \u0275\u0275text(2, "\u{1F45B}");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "h2", 8);
    \u0275\u0275text(4, "Set Up Your Monthly Wallet");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "p", 9);
    \u0275\u0275text(6, "Add your income and we'll help you budget smartly");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(7, "form", 10);
    \u0275\u0275listener("ngSubmit", function WalletComponent_Conditional_7_Template_form_ngSubmit_7_listener() {
      \u0275\u0275restoreView(_r3);
      const ctx_r1 = \u0275\u0275nextContext();
      return \u0275\u0275resetView(ctx_r1.createWallet());
    });
    \u0275\u0275elementStart(8, "div", 11)(9, "label", 12);
    \u0275\u0275text(10, "Monthly Income (\u20B9)");
    \u0275\u0275elementEnd();
    \u0275\u0275element(11, "input", 13);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(12, "div", 14)(13, "span", 15);
    \u0275\u0275text(14, "Envelope Budgets (optional)");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(15, "div", 16);
    \u0275\u0275repeaterCreate(16, WalletComponent_Conditional_7_For_17_Template, 6, 3, "div", 17, _forTrack0);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(18, "button", 18);
    \u0275\u0275template(19, WalletComponent_Conditional_7_Conditional_19_Template, 1, 0, "span", 19);
    \u0275\u0275text(20, " Activate Wallet ");
    \u0275\u0275elementEnd()()();
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext();
    \u0275\u0275advance(7);
    \u0275\u0275property("formGroup", ctx_r1.setupForm);
    \u0275\u0275advance(9);
    \u0275\u0275repeater(ctx_r1.defaultEnvelopes);
    \u0275\u0275advance(2);
    \u0275\u0275property("disabled", ctx_r1.setupForm.invalid || ctx_r1.creating());
    \u0275\u0275advance();
    \u0275\u0275conditional(19, ctx_r1.creating() ? 19 : -1);
  }
}
function WalletComponent_Conditional_8_Conditional_0_Conditional_8_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "span", 19);
  }
}
function WalletComponent_Conditional_8_Conditional_0_Template(rf, ctx) {
  if (rf & 1) {
    const _r6 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "div", 23)(1, "h3", 38);
    \u0275\u0275text(2, "Top Up Wallet");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "div", 11)(4, "label", 12);
    \u0275\u0275text(5, "Amount (\u20B9)");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(6, "input", 39);
    \u0275\u0275twoWayListener("ngModelChange", function WalletComponent_Conditional_8_Conditional_0_Template_input_ngModelChange_6_listener($event) {
      \u0275\u0275restoreView(_r6);
      const ctx_r1 = \u0275\u0275nextContext(2);
      \u0275\u0275twoWayBindingSet(ctx_r1.topUpAmount, $event) || (ctx_r1.topUpAmount = $event);
      return \u0275\u0275resetView($event);
    });
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(7, "button", 40);
    \u0275\u0275listener("click", function WalletComponent_Conditional_8_Conditional_0_Template_button_click_7_listener() {
      \u0275\u0275restoreView(_r6);
      const ctx_r1 = \u0275\u0275nextContext(2);
      return \u0275\u0275resetView(ctx_r1.doTopUp());
    });
    \u0275\u0275template(8, WalletComponent_Conditional_8_Conditional_0_Conditional_8_Template, 1, 0, "span", 19);
    \u0275\u0275text(9);
    \u0275\u0275pipe(10, "inr");
    \u0275\u0275elementEnd()();
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext(2);
    \u0275\u0275advance(6);
    \u0275\u0275twoWayProperty("ngModel", ctx_r1.topUpAmount);
    \u0275\u0275advance();
    \u0275\u0275property("disabled", !ctx_r1.topUpAmount || ctx_r1.topping());
    \u0275\u0275advance();
    \u0275\u0275conditional(8, ctx_r1.topping() ? 8 : -1);
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1(" Add \u20B9", \u0275\u0275pipeBind1(10, 4, ctx_r1.topUpAmount), " ");
  }
}
function WalletComponent_Conditional_8_Conditional_34_For_5_Conditional_10_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 50);
    \u0275\u0275text(1, "Over");
    \u0275\u0275elementEnd();
  }
}
function WalletComponent_Conditional_8_Conditional_34_For_5_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 44)(1, "div", 45)(2, "span", 46);
    \u0275\u0275text(3);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(4, "div", 47)(5, "div", 48);
    \u0275\u0275text(6);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(7, "div", 49);
    \u0275\u0275text(8);
    \u0275\u0275pipe(9, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275template(10, WalletComponent_Conditional_8_Conditional_34_For_5_Conditional_10_Template, 2, 0, "span", 50);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(11, "div", 51);
    \u0275\u0275element(12, "div", 30);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(13, "div", 52)(14, "span", 53);
    \u0275\u0275text(15);
    \u0275\u0275pipe(16, "inr");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(17, "span", 54);
    \u0275\u0275text(18);
    \u0275\u0275pipe(19, "inr");
    \u0275\u0275elementEnd()()();
  }
  if (rf & 2) {
    const env_r7 = ctx.$implicit;
    \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(18, _c0, env_r7.overBudget));
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(env_r7.icon);
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(env_r7.categoryName);
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate1("Budget: ", \u0275\u0275pipeBind1(9, 12, env_r7.budget), "");
    \u0275\u0275advance(2);
    \u0275\u0275conditional(10, env_r7.overBudget ? 10 : -1);
    \u0275\u0275advance(2);
    \u0275\u0275styleProp("width", env_r7.usedPercent + "%")("background", env_r7.overBudget ? "var(--color-danger)" : "var(--color-primary)");
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate1("Spent: ", \u0275\u0275pipeBind1(16, 14, env_r7.spent), "");
    \u0275\u0275advance(2);
    \u0275\u0275property("ngClass", env_r7.overBudget ? "text-danger" : "text-success");
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1(" ", \u0275\u0275pipeBind1(19, 16, env_r7.remaining), " ");
  }
}
function WalletComponent_Conditional_8_Conditional_34_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 41)(1, "span", 42);
    \u0275\u0275text(2, "Envelopes");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(3, "div", 43);
    \u0275\u0275repeaterCreate(4, WalletComponent_Conditional_8_Conditional_34_For_5_Template, 20, 20, "div", 44, _forTrack1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext(2);
    \u0275\u0275advance(4);
    \u0275\u0275repeater(ctx_r1.wallet().envelopes);
  }
}
function WalletComponent_Conditional_8_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275template(0, WalletComponent_Conditional_8_Conditional_0_Template, 11, 6, "div", 23);
    \u0275\u0275elementStart(1, "div", 24)(2, "div", 25)(3, "div")(4, "div", 26);
    \u0275\u0275text(5, "Available Balance");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(6, "div", 27);
    \u0275\u0275text(7);
    \u0275\u0275pipe(8, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(9, "div", 28);
    \u0275\u0275text(10);
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(11, "div", 29);
    \u0275\u0275element(12, "div", 30);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(13, "div", 31)(14, "span");
    \u0275\u0275text(15);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(16, "span");
    \u0275\u0275text(17);
    \u0275\u0275pipe(18, "inr");
    \u0275\u0275elementEnd()()();
    \u0275\u0275elementStart(19, "div", 32)(20, "div", 33)(21, "div", 34);
    \u0275\u0275text(22, "Income");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(23, "div", 35);
    \u0275\u0275text(24);
    \u0275\u0275pipe(25, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(26, "div", 33)(27, "div", 34);
    \u0275\u0275text(28, "Committed");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(29, "div", 36);
    \u0275\u0275text(30);
    \u0275\u0275pipe(31, "inr");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(32, "div", 37);
    \u0275\u0275text(33, "EMI + SIP");
    \u0275\u0275elementEnd()()();
    \u0275\u0275template(34, WalletComponent_Conditional_8_Conditional_34_Template, 6, 0);
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext();
    \u0275\u0275conditional(0, ctx_r1.showTopUp() ? 0 : -1);
    \u0275\u0275advance(7);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(8, 12, ctx_r1.wallet().balance, true));
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(ctx_r1.wallet().month);
    \u0275\u0275advance(2);
    \u0275\u0275styleProp("width", ctx_r1.wallet().usedPercent + "%")("background", ctx_r1.usedColor());
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate1("", ctx_r1.wallet().usedPercent, "% used");
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate1("Free to spend: ", \u0275\u0275pipeBind2(18, 15, ctx_r1.wallet().freeToSpend, true), "");
    \u0275\u0275advance(7);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(25, 18, ctx_r1.wallet().income, true));
    \u0275\u0275advance(6);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(31, 21, ctx_r1.wallet().committed, true));
    \u0275\u0275advance(4);
    \u0275\u0275conditional(34, ctx_r1.wallet().envelopes.length ? 34 : -1);
  }
}
var WalletComponent = class _WalletComponent {
  constructor(walletService, toastService, fb) {
    this.walletService = walletService;
    this.toastService = toastService;
    this.fb = fb;
    this.wallet = this.walletService.wallet;
    this.loading = signal(false);
    this.creating = signal(false);
    this.showTopUp = signal(false);
    this.topping = signal(false);
    this.topUpAmount = null;
    this.envelopeBudgets = {};
    this.defaultEnvelopes = [
      { key: "food", name: "Food & Dining", icon: "\u{1F355}", suggested: 8e3 },
      { key: "groceries", name: "Groceries", icon: "\u{1F6D2}", suggested: 6e3 },
      { key: "transport", name: "Transport", icon: "\u{1F697}", suggested: 3e3 },
      { key: "shopping", name: "Shopping", icon: "\u{1F6CD}\uFE0F", suggested: 4e3 },
      { key: "ent", name: "Entertainment", icon: "\u{1F3AC}", suggested: 2e3 },
      { key: "health", name: "Healthcare", icon: "\u{1F48A}", suggested: 1500 }
    ];
    this.setupForm = this.fb.group({
      income: [null, [Validators.required, Validators.min(1)]]
    });
  }
  ngOnInit() {
    this.loading.set(true);
    this.walletService.getCurrentWallet().subscribe({
      next: () => this.loading.set(false),
      error: () => this.loading.set(false)
    });
  }
  setEnvelopeBudget(key, event) {
    const val = parseFloat(event.target.value);
    if (val > 0)
      this.envelopeBudgets[key] = val;
    else
      delete this.envelopeBudgets[key];
  }
  createWallet() {
    if (this.setupForm.invalid)
      return;
    this.creating.set(true);
    const month = (/* @__PURE__ */ new Date()).toISOString().slice(0, 7);
    this.walletService.createWallet({
      month,
      income: this.setupForm.value.income,
      envelopes: Object.keys(this.envelopeBudgets).length ? this.envelopeBudgets : void 0
    }).subscribe({
      next: (res) => {
        this.creating.set(false);
        if (res.success)
          this.toastService.success("Wallet activated for " + month + " \u{1F389}");
        else
          this.toastService.error(res.message || "Failed to create wallet");
      },
      error: (err) => {
        this.creating.set(false);
        this.toastService.error(err.error?.message || "Network error");
      }
    });
  }
  doTopUp() {
    if (!this.topUpAmount || this.topUpAmount <= 0)
      return;
    this.topping.set(true);
    this.walletService.topUp(this.topUpAmount).subscribe({
      next: (res) => {
        this.topping.set(false);
        if (res.success) {
          this.showTopUp.set(false);
          this.topUpAmount = null;
          this.toastService.success("Wallet topped up \u2705");
        }
      },
      error: () => {
        this.topping.set(false);
        this.toastService.error("Top-up failed");
      }
    });
  }
  usedColor() {
    const p = this.wallet()?.usedPercent ?? 0;
    if (p >= 90)
      return "var(--color-danger)";
    if (p >= 70)
      return "var(--color-warning)";
    return "var(--color-success)";
  }
  static {
    this.\u0275fac = function WalletComponent_Factory(t) {
      return new (t || _WalletComponent)(\u0275\u0275directiveInject(WalletService), \u0275\u0275directiveInject(ToastService), \u0275\u0275directiveInject(FormBuilder));
    };
  }
  static {
    this.\u0275cmp = /* @__PURE__ */ \u0275\u0275defineComponent({ type: _WalletComponent, selectors: [["app-wallet"]], standalone: true, features: [\u0275\u0275StandaloneFeature], decls: 9, vars: 2, consts: [[1, "page"], [1, "page-header"], [1, "page-title"], [1, "btn", "btn-soft", "btn-sm"], ["message", "Loading wallet..."], [1, "btn", "btn-soft", "btn-sm", 3, "click"], [1, "setup-hero", "card"], [1, "setup-emoji"], [1, "setup-title"], [1, "setup-body"], [3, "ngSubmit", "formGroup"], [1, "form-group"], [1, "form-label"], ["type", "number", "inputmode", "decimal", "formControlName", "income", "placeholder", "115000", "min", "1", 1, "form-input"], [1, "section-header", 2, "margin-top", "16px"], [1, "section-title", 2, "font-size", "14px"], [1, "envelope-setup"], [1, "env-input"], ["type", "submit", 1, "btn", "btn-primary", "btn-full", 2, "margin-top", "20px", 3, "disabled"], [1, "spinner"], [1, "env-icon"], [1, "env-label"], ["type", "number", "inputmode", "decimal", 1, "form-input", "env-amt", 3, "input", "placeholder"], [1, "topup-card", "card", 2, "margin-bottom", "12px"], [1, "wallet-hero", "card"], [1, "wh-row"], [1, "wh-label"], [1, "wh-amount"], [1, "wh-month"], [1, "progress-bar", 2, "margin", "14px 0 6px"], [1, "progress-fill"], [1, "wh-stats"], [1, "stat-row", 2, "margin-top", "10px"], [1, "stat-card"], [1, "stat-label"], [1, "stat-value", "text-success"], [1, "stat-value", "text-danger"], [1, "stat-sub"], [2, "font-family", "var(--font-heading)", "font-weight", "900", "margin-bottom", "12px"], ["type", "number", "inputmode", "decimal", "placeholder", "5000", 1, "form-input", 3, "ngModelChange", "ngModel"], [1, "btn", "btn-success", "btn-full", 3, "click", "disabled"], [1, "section-header"], [1, "section-title"], [1, "envelopes-grid"], [1, "env-card", "card", 3, "ngClass"], [1, "env-top"], [1, "env-ico"], [1, "env-info"], [1, "env-name"], [1, "env-budget"], [1, "badge", "badge-danger"], [1, "progress-bar", 2, "margin", "8px 0 4px"], [1, "env-row"], [1, "text-muted", 2, "font-size", "12px"], [2, "font-size", "13px", "font-weight", "800", 3, "ngClass"]], template: function WalletComponent_Template(rf, ctx) {
      if (rf & 1) {
        \u0275\u0275elementStart(0, "div", 0);
        \u0275\u0275element(1, "app-toast");
        \u0275\u0275elementStart(2, "header", 1)(3, "h1", 2);
        \u0275\u0275text(4, "Wallet");
        \u0275\u0275elementEnd();
        \u0275\u0275template(5, WalletComponent_Conditional_5_Template, 2, 1, "button", 3);
        \u0275\u0275elementEnd();
        \u0275\u0275template(6, WalletComponent_Conditional_6_Template, 1, 0, "app-loading-spinner", 4)(7, WalletComponent_Conditional_7_Template, 21, 3)(8, WalletComponent_Conditional_8_Template, 35, 24);
        \u0275\u0275elementEnd();
      }
      if (rf & 2) {
        \u0275\u0275advance(5);
        \u0275\u0275conditional(5, ctx.wallet() ? 5 : -1);
        \u0275\u0275advance();
        \u0275\u0275conditional(6, ctx.loading() ? 6 : !ctx.wallet() ? 7 : 8);
      }
    }, dependencies: [ReactiveFormsModule, \u0275NgNoValidate, DefaultValueAccessor, NumberValueAccessor, NgControlStatus, NgControlStatusGroup, MinValidator, FormGroupDirective, FormControlName, FormsModule, NgModel, NgClass, ToastComponent, InrFormatPipe, LoadingSpinnerComponent], styles: ["\n\n.setup-hero[_ngcontent-%COMP%] {\n  text-align: center;\n  padding: 28px 20px;\n}\n.setup-emoji[_ngcontent-%COMP%] {\n  font-size: 48px;\n  margin-bottom: 12px;\n}\n.setup-title[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 20px;\n  font-weight: 900;\n  margin-bottom: 6px;\n}\n.setup-body[_ngcontent-%COMP%] {\n  font-size: 14px;\n  color: var(--color-text-muted);\n  margin-bottom: 24px;\n}\n.envelope-setup[_ngcontent-%COMP%] {\n  display: flex;\n  flex-direction: column;\n  gap: 8px;\n}\n.env-input[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 10px;\n}\n.env-icon[_ngcontent-%COMP%] {\n  font-size: 20px;\n  width: 24px;\n  text-align: center;\n}\n.env-label[_ngcontent-%COMP%] {\n  font-size: 13px;\n  font-weight: 600;\n  flex: 1;\n}\n.env-amt[_ngcontent-%COMP%] {\n  width: 100px;\n  padding: 8px 10px;\n  font-size: 14px;\n}\n.wallet-hero[_ngcontent-%COMP%] {\n  padding: 20px;\n}\n.wh-row[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: space-between;\n  align-items: flex-start;\n}\n.wh-label[_ngcontent-%COMP%] {\n  font-size: 12px;\n  color: var(--color-text-muted);\n  font-weight: 600;\n  text-transform: uppercase;\n  letter-spacing: 0.5px;\n}\n.wh-amount[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 32px;\n  font-weight: 900;\n}\n.wh-month[_ngcontent-%COMP%] {\n  background: var(--color-primary-light);\n  color: var(--color-primary);\n  padding: 4px 10px;\n  border-radius: 99px;\n  font-size: 12px;\n  font-weight: 700;\n}\n.wh-stats[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: space-between;\n  font-size: 12px;\n  color: var(--color-text-muted);\n  font-weight: 600;\n}\n.envelopes-grid[_ngcontent-%COMP%] {\n  display: flex;\n  flex-direction: column;\n  gap: 8px;\n  margin-bottom: 16px;\n}\n.env-card[_ngcontent-%COMP%] {\n  padding: 14px;\n}\n.env-card.env-over[_ngcontent-%COMP%] {\n  border-color: var(--color-danger);\n  background: var(--color-danger-light);\n}\n.env-top[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 10px;\n}\n.env-ico[_ngcontent-%COMP%] {\n  font-size: 22px;\n}\n.env-info[_ngcontent-%COMP%] {\n  flex: 1;\n}\n.env-name[_ngcontent-%COMP%] {\n  font-weight: 700;\n  font-size: 14px;\n}\n.env-budget[_ngcontent-%COMP%] {\n  font-size: 12px;\n  color: var(--color-text-muted);\n}\n.env-row[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: space-between;\n  align-items: center;\n}\n/*# sourceMappingURL=wallet.component.css.map */"], changeDetection: 0 });
  }
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && \u0275setClassDebugInfo(WalletComponent, { className: "WalletComponent", filePath: "src/app/features/wallet/wallet.component.ts", lineNumber: 182 });
})();
export {
  WalletComponent
};
//# sourceMappingURL=chunk-HETUXI4V.js.map
