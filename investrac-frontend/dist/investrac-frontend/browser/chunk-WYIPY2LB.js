import {
  RelativeDatePipe,
  TransactionService
} from "./chunk-LNTPLZMI.js";
import {
  WalletService
} from "./chunk-DNKGIIYI.js";
import {
  LoadingSpinnerComponent
} from "./chunk-F3IRDWQ3.js";
import {
  InrFormatPipe
} from "./chunk-IDOJ465G.js";
import {
  AuthService
} from "./chunk-SK7XFWA5.js";
import {
  RouterLink
} from "./chunk-VO5CVMHZ.js";
import {
  ApiService
} from "./chunk-QGHRW6JC.js";
import {
  ToastComponent
} from "./chunk-TLGCWV2M.js";
import {
  NgClass,
  signal,
  tap,
  ɵsetClassDebugInfo,
  ɵɵStandaloneFeature,
  ɵɵadvance,
  ɵɵconditional,
  ɵɵdefineComponent,
  ɵɵdefineInjectable,
  ɵɵdirectiveInject,
  ɵɵelement,
  ɵɵelementEnd,
  ɵɵelementStart,
  ɵɵgetInheritedFactory,
  ɵɵnextContext,
  ɵɵpipe,
  ɵɵpipeBind1,
  ɵɵpipeBind2,
  ɵɵproperty,
  ɵɵpureFunction1,
  ɵɵrepeater,
  ɵɵrepeaterCreate,
  ɵɵstyleProp,
  ɵɵtemplate,
  ɵɵtext,
  ɵɵtextInterpolate,
  ɵɵtextInterpolate1,
  ɵɵtextInterpolate2
} from "./chunk-TWKZKYET.js";

// src/app/core/services/notification.service.ts
var NotificationService = class _NotificationService extends ApiService {
  constructor() {
    super(...arguments);
    this._unreadCount = signal(0);
    this.unreadCount = this._unreadCount.asReadonly();
  }
  getNotifications(page = 0, size = 20) {
    return this.get("/notifications", { page, size }).pipe(tap((res) => {
      if (res.success && res.data) {
        this._unreadCount.set(res.data.unreadCount);
      }
    }));
  }
  getUnreadCount() {
    return this.get("/notifications/unread-count").pipe(tap((res) => {
      if (res.success && res.data !== void 0)
        this._unreadCount.set(res.data);
    }));
  }
  markRead(id) {
    return this.patch(`/notifications/${id}/read`);
  }
  markAllRead() {
    return this.patch("/notifications/read-all");
  }
  updateFcmToken(fcmToken) {
    return this.put("/notifications/preferences", { fcmToken });
  }
  static {
    this.\u0275fac = /* @__PURE__ */ (() => {
      let \u0275NotificationService_BaseFactory;
      return function NotificationService_Factory(t) {
        return (\u0275NotificationService_BaseFactory || (\u0275NotificationService_BaseFactory = \u0275\u0275getInheritedFactory(_NotificationService)))(t || _NotificationService);
      };
    })();
  }
  static {
    this.\u0275prov = /* @__PURE__ */ \u0275\u0275defineInjectable({ token: _NotificationService, factory: _NotificationService.\u0275fac, providedIn: "root" });
  }
};

// src/app/features/home/home.component.ts
var _forTrack0 = ($index, $item) => $item.id;
var _c0 = (a0) => ({ "env-over": a0 });
function HomeComponent_Conditional_10_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "app-loading-spinner", 5);
  }
}
function HomeComponent_Conditional_11_Conditional_0_Conditional_18_For_2_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 31)(1, "span", 32);
    \u0275\u0275text(2);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "div")(4, "div", 33);
    \u0275\u0275text(5);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(6, "div", 34);
    \u0275\u0275element(7, "div", 35);
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(8, "span", 36);
    \u0275\u0275text(9);
    \u0275\u0275pipe(10, "inr");
    \u0275\u0275elementEnd()();
  }
  if (rf & 2) {
    const env_r1 = ctx.$implicit;
    \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(11, _c0, env_r1.overBudget));
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(env_r1.icon);
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(env_r1.categoryName);
    \u0275\u0275advance(2);
    \u0275\u0275styleProp("width", env_r1.usedPercent + "%")("background", env_r1.overBudget ? "var(--color-danger)" : "var(--color-primary)");
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(10, 8, env_r1.remaining, true));
  }
}
function HomeComponent_Conditional_11_Conditional_0_Conditional_18_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 27);
    \u0275\u0275repeaterCreate(1, HomeComponent_Conditional_11_Conditional_0_Conditional_18_For_2_Template, 11, 13, "div", 31, _forTrack0);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext(3);
    \u0275\u0275advance();
    \u0275\u0275repeater(ctx_r1.wallet().envelopes.slice(0, 4));
  }
}
function HomeComponent_Conditional_11_Conditional_0_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 6)(1, "div", 19)(2, "div")(3, "p", 20);
    \u0275\u0275text(4, "Wallet Balance");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "h2", 21);
    \u0275\u0275text(6);
    \u0275\u0275pipe(7, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(8, "span", 22);
    \u0275\u0275text(9);
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(10, "div", 23);
    \u0275\u0275element(11, "div", 24);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(12, "div", 25)(13, "span");
    \u0275\u0275text(14);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(15, "span", 26);
    \u0275\u0275text(16);
    \u0275\u0275pipe(17, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275template(18, HomeComponent_Conditional_11_Conditional_0_Conditional_18_Template, 3, 0, "div", 27);
    \u0275\u0275elementStart(19, "div", 28)(20, "a", 29);
    \u0275\u0275text(21, "View Wallet");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(22, "a", 30);
    \u0275\u0275text(23, "Add Expense");
    \u0275\u0275elementEnd()()();
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext(2);
    \u0275\u0275property("ngClass", ctx_r1.heroClass());
    \u0275\u0275advance(6);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(7, 10, ctx_r1.wallet().balance, true));
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(ctx_r1.wallet().month);
    \u0275\u0275advance(2);
    \u0275\u0275styleProp("width", ctx_r1.wallet().usedPercent + "%")("background", ctx_r1.progressColor());
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate1("", ctx_r1.wallet().usedPercent, "% used");
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate1("Free: ", \u0275\u0275pipeBind2(17, 13, ctx_r1.wallet().freeToSpend, true), "");
    \u0275\u0275advance(2);
    \u0275\u0275conditional(18, ctx_r1.wallet().envelopes.length ? 18 : -1);
  }
}
function HomeComponent_Conditional_11_Conditional_1_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 37)(1, "div", 38);
    \u0275\u0275text(2, "\u{1F45B}");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "h3", 39);
    \u0275\u0275text(4, "Setup Your Wallet");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "p", 40);
    \u0275\u0275text(6, " Add your monthly income to start tracking expenses ");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(7, "a", 41);
    \u0275\u0275text(8, "Set Up Wallet \u2192");
    \u0275\u0275elementEnd()();
  }
}
function HomeComponent_Conditional_11_Conditional_46_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 18)(1, "div", 42);
    \u0275\u0275text(2, "\u{1F4B3}");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "div", 43);
    \u0275\u0275text(4, "No transactions yet");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "div", 44);
    \u0275\u0275text(6, "Add your first expense to get started");
    \u0275\u0275elementEnd()();
  }
}
function HomeComponent_Conditional_11_Conditional_47_For_2_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 46)(1, "div", 47);
    \u0275\u0275text(2);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "div", 48)(4, "div", 49);
    \u0275\u0275text(5);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(6, "div", 50)(7, "span", 51);
    \u0275\u0275text(8);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(9, "span", 52);
    \u0275\u0275text(10);
    \u0275\u0275pipe(11, "relativeDate");
    \u0275\u0275elementEnd()()();
    \u0275\u0275elementStart(12, "div", 53);
    \u0275\u0275text(13);
    \u0275\u0275pipe(14, "inr");
    \u0275\u0275elementEnd()();
  }
  if (rf & 2) {
    const tx_r3 = ctx.$implicit;
    const ctx_r1 = \u0275\u0275nextContext(3);
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(ctx_r1.getCategoryIcon(tx_r3.category));
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(tx_r3.name);
    \u0275\u0275advance(2);
    \u0275\u0275property("ngClass", ctx_r1.getBadgeClass(tx_r3.type));
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(tx_r3.type);
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind1(11, 8, tx_r3.txDate));
    \u0275\u0275advance(2);
    \u0275\u0275property("ngClass", ctx_r1.getAmountClass(tx_r3.type));
    \u0275\u0275advance();
    \u0275\u0275textInterpolate2(" ", tx_r3.type === "INCOME" ? "+" : "-", "", \u0275\u0275pipeBind1(14, 10, tx_r3.amount), " ");
  }
}
function HomeComponent_Conditional_11_Conditional_47_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 45);
    \u0275\u0275repeaterCreate(1, HomeComponent_Conditional_11_Conditional_47_For_2_Template, 15, 12, "div", 46, _forTrack0);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext(2);
    \u0275\u0275advance();
    \u0275\u0275repeater(ctx_r1.recentTx());
  }
}
function HomeComponent_Conditional_11_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275template(0, HomeComponent_Conditional_11_Conditional_0_Template, 24, 16, "div", 6)(1, HomeComponent_Conditional_11_Conditional_1_Template, 9, 0);
    \u0275\u0275elementStart(2, "div", 7)(3, "span", 8);
    \u0275\u0275text(4, "This Month");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "a", 9);
    \u0275\u0275text(6, "See all");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(7, "div", 10)(8, "div", 11)(9, "div", 12);
    \u0275\u0275text(10, "Income");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(11, "div", 13);
    \u0275\u0275text(12);
    \u0275\u0275pipe(13, "inr");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(14, "div", 14);
    \u0275\u0275text(15);
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(16, "div", 11)(17, "div", 12);
    \u0275\u0275text(18, "Expenses");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(19, "div", 15);
    \u0275\u0275text(20);
    \u0275\u0275pipe(21, "inr");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(22, "div", 14);
    \u0275\u0275text(23);
    \u0275\u0275elementEnd()()();
    \u0275\u0275elementStart(24, "div", 10)(25, "div", 11)(26, "div", 12);
    \u0275\u0275text(27, "Investments");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(28, "div", 16);
    \u0275\u0275text(29);
    \u0275\u0275pipe(30, "inr");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(31, "div", 14);
    \u0275\u0275text(32, "This month");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(33, "div", 11)(34, "div", 12);
    \u0275\u0275text(35, "Net Savings");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(36, "div", 17);
    \u0275\u0275text(37);
    \u0275\u0275pipe(38, "inr");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(39, "div", 14);
    \u0275\u0275text(40, "Income \u2013 Expense");
    \u0275\u0275elementEnd()()();
    \u0275\u0275elementStart(41, "div", 7)(42, "span", 8);
    \u0275\u0275text(43, "Recent");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(44, "a", 9);
    \u0275\u0275text(45, "All transactions");
    \u0275\u0275elementEnd()();
    \u0275\u0275template(46, HomeComponent_Conditional_11_Conditional_46_Template, 7, 0, "div", 18)(47, HomeComponent_Conditional_11_Conditional_47_Template, 3, 0);
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext();
    \u0275\u0275conditional(0, ctx_r1.wallet() ? 0 : 1);
    \u0275\u0275advance(12);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(13, 9, ctx_r1.summary.income, true));
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate1("", ctx_r1.summary.txCount.income, " transactions");
    \u0275\u0275advance(5);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(21, 12, ctx_r1.summary.expense, true));
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate1("Savings: ", ctx_r1.summary.savingsRate, "%");
    \u0275\u0275advance(6);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(30, 15, ctx_r1.summary.investment, true));
    \u0275\u0275advance(7);
    \u0275\u0275property("ngClass", ctx_r1.summary.netSavings >= 0 ? "text-success" : "text-danger");
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1(" ", \u0275\u0275pipeBind2(38, 18, ctx_r1.summary.netSavings, true), " ");
    \u0275\u0275advance(9);
    \u0275\u0275conditional(46, ctx_r1.recentTx().length === 0 ? 46 : 47);
  }
}
var HomeComponent = class _HomeComponent {
  constructor(authService, walletService, transactionService, notificationService) {
    this.authService = authService;
    this.walletService = walletService;
    this.transactionService = transactionService;
    this.notificationService = notificationService;
    this.loading = signal(true);
    this.wallet = signal(null);
    this.recentTx = signal([]);
    this.summary = {
      income: 0,
      expense: 0,
      investment: 0,
      netSavings: 0,
      savingsRate: 0,
      txCount: { income: 0 }
    };
  }
  ngOnInit() {
    this.loadDashboard();
    this.notificationService.getUnreadCount().subscribe();
  }
  loadDashboard() {
    this.loading.set(true);
    this.walletService.getCurrentWallet().subscribe({
      next: (res) => {
        if (res.success && res.data)
          this.wallet.set(res.data);
      },
      error: () => {
      }
      // No wallet yet — show setup card
    });
    this.transactionService.getRecent(8).subscribe({
      next: (res) => {
        if (res.success && res.data)
          this.recentTx.set(res.data);
      },
      error: () => {
      }
    });
    this.transactionService.getMonthlySummary().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const d = res.data;
          this.summary = {
            income: d.totalIncome,
            expense: d.totalExpense,
            investment: d.totalInvestment,
            netSavings: d.netSavings,
            savingsRate: d.savingsRatePercent,
            txCount: { income: 0 }
          };
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
  greeting() {
    const h = (/* @__PURE__ */ new Date()).getHours();
    if (h < 12)
      return "Good morning \u{1F305}";
    if (h < 17)
      return "Good afternoon \u2600\uFE0F";
    if (h < 20)
      return "Good evening \u{1F306}";
    return "Good night \u{1F319}";
  }
  heroClass() {
    const pct = this.wallet()?.usedPercent ?? 0;
    if (pct >= 90)
      return "hero-over";
    if (pct >= 70)
      return "hero-warn";
    return "hero-ok";
  }
  progressColor() {
    const pct = this.wallet()?.usedPercent ?? 0;
    if (pct >= 90)
      return "var(--color-danger)";
    if (pct >= 70)
      return "var(--color-warning)";
    return "var(--color-success)";
  }
  getCategoryIcon(cat) {
    const icons = {
      "Food & Dining": "\u{1F355}",
      "Groceries": "\u{1F6D2}",
      "Transport": "\u{1F697}",
      "Shopping": "\u{1F6CD}\uFE0F",
      "Healthcare": "\u{1F48A}",
      "Entertainment": "\u{1F3AC}",
      "Income": "\u{1F4B0}",
      "Salary": "\u{1F4B0}",
      "Investment": "\u{1F4C8}",
      "EMI": "\u{1F3E0}",
      "SIP": "\u{1F4CA}",
      "Others": "\u{1F4E6}"
    };
    return icons[cat] ?? "\u{1F4B3}";
  }
  getBadgeClass(type) {
    return {
      INCOME: "badge-success",
      EXPENSE: "badge-danger",
      INVESTMENT: "badge-info",
      SAVINGS: "badge-warning"
    }[type] ?? "badge-neutral";
  }
  getAmountClass(type) {
    return type === "INCOME" ? "amount-income" : "amount-expense";
  }
  static {
    this.\u0275fac = function HomeComponent_Factory(t) {
      return new (t || _HomeComponent)(\u0275\u0275directiveInject(AuthService), \u0275\u0275directiveInject(WalletService), \u0275\u0275directiveInject(TransactionService), \u0275\u0275directiveInject(NotificationService));
    };
  }
  static {
    this.\u0275cmp = /* @__PURE__ */ \u0275\u0275defineComponent({ type: _HomeComponent, selectors: [["app-home"]], standalone: true, features: [\u0275\u0275StandaloneFeature], decls: 12, vars: 3, consts: [[1, "page"], [1, "page-header"], [1, "greeting"], [1, "page-title"], ["routerLink", "/settings", "aria-label", "Settings", 1, "btn", "btn-icon"], ["message", "Loading your dashboard..."], [1, "hero-card", "card", 3, "ngClass"], [1, "section-header"], [1, "section-title"], ["routerLink", "/transactions", 1, "section-action"], [1, "stat-row"], [1, "stat-card"], [1, "stat-label"], [1, "stat-value", "text-success"], [1, "stat-sub"], [1, "stat-value", "text-danger"], [1, "stat-value", 2, "color", "var(--color-info)"], [1, "stat-value", 3, "ngClass"], [1, "empty-state", 2, "padding", "24px 0"], [1, "hero-top"], [1, "hero-label"], [1, "hero-amount"], [1, "hero-month"], [1, "progress-bar", 2, "margin", "14px 0 8px"], [1, "progress-fill"], [1, "hero-stats"], [1, "text-muted"], [1, "envelopes"], [1, "hero-actions"], ["routerLink", "/wallet", 1, "btn", "btn-soft", "btn-sm"], ["routerLink", "/transactions", 1, "btn", "btn-soft", "btn-sm"], [1, "env-chip", 3, "ngClass"], [1, "env-icon"], [1, "env-name"], [1, "env-bar"], [1, "env-fill"], [1, "env-amount"], [1, "card", "setup-card"], [1, "setup-icon"], [1, "fw-900"], [1, "text-muted", 2, "font-size", "14px", "margin", "6px 0 14px"], ["routerLink", "/wallet", 1, "btn", "btn-primary", "btn-sm"], [1, "empty-icon"], [1, "empty-title"], [1, "empty-body"], [1, "tx-list"], [1, "tx-item", "card", "card-sm"], [1, "tx-icon"], [1, "tx-info"], [1, "tx-name"], [1, "tx-meta"], [1, "badge", 3, "ngClass"], [1, "tx-date"], [1, "tx-amount", 3, "ngClass"]], template: function HomeComponent_Template(rf, ctx) {
      if (rf & 1) {
        \u0275\u0275elementStart(0, "div", 0);
        \u0275\u0275element(1, "app-toast");
        \u0275\u0275elementStart(2, "header", 1)(3, "div")(4, "p", 2);
        \u0275\u0275text(5);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(6, "h1", 3);
        \u0275\u0275text(7);
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(8, "a", 4);
        \u0275\u0275text(9, "\u2699\uFE0F");
        \u0275\u0275elementEnd()();
        \u0275\u0275template(10, HomeComponent_Conditional_10_Template, 1, 0, "app-loading-spinner", 5)(11, HomeComponent_Conditional_11_Template, 48, 21);
        \u0275\u0275elementEnd();
      }
      if (rf & 2) {
        \u0275\u0275advance(5);
        \u0275\u0275textInterpolate(ctx.greeting());
        \u0275\u0275advance(2);
        \u0275\u0275textInterpolate(ctx.authService.userName());
        \u0275\u0275advance(3);
        \u0275\u0275conditional(10, ctx.loading() ? 10 : 11);
      }
    }, dependencies: [RouterLink, NgClass, ToastComponent, LoadingSpinnerComponent, InrFormatPipe, RelativeDatePipe], styles: ["\n\n.greeting[_ngcontent-%COMP%] {\n  font-size: 13px;\n  color: var(--color-text-muted);\n  font-weight: 600;\n}\n.hero-card[_ngcontent-%COMP%] {\n  padding: 18px;\n  margin-bottom: 6px;\n}\n.hero-card.hero-ok[_ngcontent-%COMP%] {\n  border-color: var(--color-success);\n}\n.hero-card.hero-warn[_ngcontent-%COMP%] {\n  border-color: var(--color-warning);\n}\n.hero-card.hero-over[_ngcontent-%COMP%] {\n  border-color: var(--color-danger);\n}\n.hero-top[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: space-between;\n  align-items: flex-start;\n}\n.hero-label[_ngcontent-%COMP%] {\n  font-size: 12px;\n  color: var(--color-text-muted);\n  font-weight: 600;\n  text-transform: uppercase;\n  letter-spacing: 0.5px;\n}\n.hero-amount[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 30px;\n  font-weight: 900;\n  margin-top: 2px;\n}\n.hero-month[_ngcontent-%COMP%] {\n  font-size: 12px;\n  background: var(--color-primary-light);\n  color: var(--color-primary);\n  padding: 4px 10px;\n  border-radius: 99px;\n  font-weight: 700;\n}\n.hero-stats[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: space-between;\n  font-size: 12px;\n  font-weight: 600;\n  color: var(--color-text-muted);\n}\n.envelopes[_ngcontent-%COMP%] {\n  display: flex;\n  flex-direction: column;\n  gap: 8px;\n  margin: 12px 0;\n}\n.env-chip[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 10px;\n  padding: 8px;\n  background: var(--color-card-alt);\n  border-radius: 10px;\n}\n.env-chip.env-over[_ngcontent-%COMP%] {\n  background: var(--color-danger-light);\n}\n.env-icon[_ngcontent-%COMP%] {\n  font-size: 18px;\n}\n.env-name[_ngcontent-%COMP%] {\n  font-size: 12px;\n  font-weight: 600;\n}\n.env-bar[_ngcontent-%COMP%] {\n  height: 3px;\n  background: var(--color-border);\n  border-radius: 3px;\n  overflow: hidden;\n  margin-top: 3px;\n}\n.env-fill[_ngcontent-%COMP%] {\n  height: 100%;\n  border-radius: 3px;\n  transition: width .3s;\n}\n.env-amount[_ngcontent-%COMP%] {\n  font-size: 13px;\n  font-weight: 800;\n  font-family: var(--font-heading);\n  margin-left: auto;\n}\n.hero-actions[_ngcontent-%COMP%] {\n  display: flex;\n  gap: 8px;\n  margin-top: 14px;\n}\n.setup-card[_ngcontent-%COMP%] {\n  text-align: center;\n  padding: 28px 20px;\n}\n.setup-icon[_ngcontent-%COMP%] {\n  font-size: 40px;\n  margin-bottom: 10px;\n}\n.tx-list[_ngcontent-%COMP%] {\n  display: flex;\n  flex-direction: column;\n  gap: 8px;\n  margin-bottom: 16px;\n}\n.tx-item[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 12px;\n}\n.tx-icon[_ngcontent-%COMP%] {\n  width: 40px;\n  height: 40px;\n  background: var(--color-card-alt);\n  border-radius: 12px;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  font-size: 18px;\n  flex-shrink: 0;\n}\n.tx-info[_ngcontent-%COMP%] {\n  flex: 1;\n  min-width: 0;\n}\n.tx-name[_ngcontent-%COMP%] {\n  font-weight: 600;\n  font-size: 14px;\n  white-space: nowrap;\n  overflow: hidden;\n  text-overflow: ellipsis;\n}\n.tx-meta[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 8px;\n  margin-top: 3px;\n}\n.tx-date[_ngcontent-%COMP%] {\n  font-size: 11px;\n  color: var(--color-text-muted);\n}\n.tx-amount[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 15px;\n  font-weight: 900;\n  white-space: nowrap;\n}\n/*# sourceMappingURL=home.component.css.map */"], changeDetection: 0 });
  }
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && \u0275setClassDebugInfo(HomeComponent, { className: "HomeComponent", filePath: "src/app/features/home/home.component.ts", lineNumber: 194 });
})();
export {
  HomeComponent
};
//# sourceMappingURL=chunk-WYIPY2LB.js.map
