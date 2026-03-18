import {
  LoadingSpinnerComponent
} from "./chunk-F3IRDWQ3.js";
import {
  InrFormatPipe
} from "./chunk-IDOJ465G.js";
import "./chunk-VO5CVMHZ.js";
import {
  ApiService
} from "./chunk-QGHRW6JC.js";
import {
  ToastComponent
} from "./chunk-TLGCWV2M.js";
import {
  DecimalPipe,
  NgClass,
  SlicePipe,
  ToastService,
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
  ɵɵgetCurrentView,
  ɵɵgetInheritedFactory,
  ɵɵlistener,
  ɵɵnextContext,
  ɵɵpipe,
  ɵɵpipeBind1,
  ɵɵpipeBind2,
  ɵɵpipeBind3,
  ɵɵproperty,
  ɵɵrepeater,
  ɵɵrepeaterCreate,
  ɵɵresetView,
  ɵɵrestoreView,
  ɵɵstyleProp,
  ɵɵtemplate,
  ɵɵtext,
  ɵɵtextInterpolate,
  ɵɵtextInterpolate1,
  ɵɵtextInterpolate2
} from "./chunk-TWKZKYET.js";

// src/app/core/services/portfolio.service.ts
var PortfolioService = class _PortfolioService extends ApiService {
  constructor() {
    super(...arguments);
    this._summary = signal(null);
    this.summary = this._summary.asReadonly();
  }
  getSummary() {
    return this.get("/portfolio").pipe(tap((res) => {
      if (res.success && res.data)
        this._summary.set(res.data);
    }));
  }
  getHoldings() {
    return this.get("/portfolio/holdings");
  }
  createHolding(req) {
    return this.post("/portfolio/holdings", req);
  }
  updateHolding(id, req) {
    return this.put(`/portfolio/holdings/${id}`, req);
  }
  deleteHolding(id) {
    return this.delete(`/portfolio/holdings/${id}`);
  }
  triggerSync() {
    return this.post("/portfolio/sync");
  }
  static {
    this.\u0275fac = /* @__PURE__ */ (() => {
      let \u0275PortfolioService_BaseFactory;
      return function PortfolioService_Factory(t) {
        return (\u0275PortfolioService_BaseFactory || (\u0275PortfolioService_BaseFactory = \u0275\u0275getInheritedFactory(_PortfolioService)))(t || _PortfolioService);
      };
    })();
  }
  static {
    this.\u0275prov = /* @__PURE__ */ \u0275\u0275defineInjectable({ token: _PortfolioService, factory: _PortfolioService.\u0275fac, providedIn: "root" });
  }
};

// src/app/features/portfolio/portfolio.component.ts
var _forTrack0 = ($index, $item) => $item.id;
var _forTrack1 = ($index, $item) => $item.type;
function PortfolioComponent_Conditional_7_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "app-loading-spinner", 4);
  }
}
function PortfolioComponent_Conditional_8_Template(rf, ctx) {
  if (rf & 1) {
    const _r1 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "div", 5)(1, "div", 6);
    \u0275\u0275text(2, "\u{1F4C8}");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "div", 7);
    \u0275\u0275text(4, "No holdings yet");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "div", 8);
    \u0275\u0275text(6, "Add your mutual funds, stocks, FDs to track returns");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(7, "button", 9);
    \u0275\u0275listener("click", function PortfolioComponent_Conditional_8_Template_button_click_7_listener() {
      \u0275\u0275restoreView(_r1);
      const ctx_r1 = \u0275\u0275nextContext();
      return \u0275\u0275resetView(ctx_r1.showAdd.set(true));
    });
    \u0275\u0275text(8, " + Add First Holding ");
    \u0275\u0275elementEnd()();
  }
}
function PortfolioComponent_Conditional_9_Conditional_30_For_6_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "div", 31);
  }
  if (rf & 2) {
    const a_r4 = ctx.$implicit;
    \u0275\u0275styleProp("width", a_r4.percent + "%")("background", a_r4.color);
    \u0275\u0275property("title", a_r4.type + " " + a_r4.percent + "%");
  }
}
function PortfolioComponent_Conditional_9_Conditional_30_For_9_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 30);
    \u0275\u0275element(1, "span", 32);
    \u0275\u0275elementStart(2, "span", 33);
    \u0275\u0275text(3);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(4, "span", 34);
    \u0275\u0275text(5);
    \u0275\u0275pipe(6, "number");
    \u0275\u0275elementEnd()();
  }
  if (rf & 2) {
    const a_r5 = ctx.$implicit;
    \u0275\u0275advance();
    \u0275\u0275styleProp("background", a_r5.color);
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(a_r5.type);
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate1("", \u0275\u0275pipeBind2(6, 4, a_r5.percent, "1.0-0"), "%");
  }
}
function PortfolioComponent_Conditional_9_Conditional_30_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 21)(1, "span", 22);
    \u0275\u0275text(2, "Asset Allocation");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(3, "div", 26)(4, "div", 27);
    \u0275\u0275repeaterCreate(5, PortfolioComponent_Conditional_9_Conditional_30_For_6_Template, 1, 5, "div", 28, _forTrack1);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(7, "div", 29);
    \u0275\u0275repeaterCreate(8, PortfolioComponent_Conditional_9_Conditional_30_For_9_Template, 7, 7, "div", 30, _forTrack1);
    \u0275\u0275elementEnd()();
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext(2);
    \u0275\u0275advance(5);
    \u0275\u0275repeater(ctx_r1.summary().assetAllocation);
    \u0275\u0275advance(3);
    \u0275\u0275repeater(ctx_r1.summary().assetAllocation);
  }
}
function PortfolioComponent_Conditional_9_For_38_Conditional_7_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 39);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const h_r7 = \u0275\u0275nextContext().$implicit;
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(h_r7.symbol);
  }
}
function PortfolioComponent_Conditional_9_For_38_Conditional_19_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 45);
    \u0275\u0275text(1);
    \u0275\u0275pipe(2, "slice");
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const h_r7 = \u0275\u0275nextContext().$implicit;
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1(" Synced ", \u0275\u0275pipeBind3(2, 1, h_r7.lastSynced, 0, 10), " ");
  }
}
function PortfolioComponent_Conditional_9_For_38_Template(rf, ctx) {
  if (rf & 1) {
    const _r6 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "div", 25)(1, "div", 35)(2, "div", 36);
    \u0275\u0275text(3);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(4, "div", 37)(5, "div", 38);
    \u0275\u0275text(6);
    \u0275\u0275elementEnd();
    \u0275\u0275template(7, PortfolioComponent_Conditional_9_For_38_Conditional_7_Template, 2, 1, "div", 39);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(8, "div", 40)(9, "div", 41);
    \u0275\u0275text(10);
    \u0275\u0275pipe(11, "inr");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(12, "div", 42);
    \u0275\u0275text(13);
    \u0275\u0275pipe(14, "number");
    \u0275\u0275elementEnd()()();
    \u0275\u0275elementStart(15, "div", 43)(16, "span", 44);
    \u0275\u0275text(17);
    \u0275\u0275pipe(18, "inr");
    \u0275\u0275elementEnd();
    \u0275\u0275template(19, PortfolioComponent_Conditional_9_For_38_Conditional_19_Template, 3, 5, "span", 45);
    \u0275\u0275elementStart(20, "button", 46);
    \u0275\u0275listener("click", function PortfolioComponent_Conditional_9_For_38_Template_button_click_20_listener() {
      const h_r7 = \u0275\u0275restoreView(_r6).$implicit;
      const ctx_r1 = \u0275\u0275nextContext(2);
      return \u0275\u0275resetView(ctx_r1.deleteHolding(h_r7.id));
    });
    \u0275\u0275text(21, "\u{1F5D1}\uFE0F");
    \u0275\u0275elementEnd()()();
  }
  if (rf & 2) {
    const h_r7 = ctx.$implicit;
    const ctx_r1 = \u0275\u0275nextContext(2);
    \u0275\u0275advance(2);
    \u0275\u0275styleProp("background", ctx_r1.getTypeColor(h_r7.type));
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1(" ", ctx_r1.getTypeShort(h_r7.type), " ");
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(h_r7.name);
    \u0275\u0275advance();
    \u0275\u0275conditional(7, h_r7.symbol ? 7 : -1);
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(11, 10, h_r7.currentValue, true));
    \u0275\u0275advance(2);
    \u0275\u0275property("ngClass", h_r7.currentValue >= h_r7.invested ? "text-success" : "text-danger");
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1(" ", \u0275\u0275pipeBind2(14, 13, ctx_r1.getReturnPct(h_r7), "1.1-1"), "% ");
    \u0275\u0275advance(4);
    \u0275\u0275textInterpolate1(" Invested: ", \u0275\u0275pipeBind1(18, 16, h_r7.invested), " ");
    \u0275\u0275advance(2);
    \u0275\u0275conditional(19, h_r7.isUpdatable && h_r7.lastSynced ? 19 : -1);
  }
}
function PortfolioComponent_Conditional_9_Template(rf, ctx) {
  if (rf & 1) {
    const _r3 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "div", 10)(1, "div", 11)(2, "div")(3, "div", 12);
    \u0275\u0275text(4, "Current Value");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "div", 13);
    \u0275\u0275text(6);
    \u0275\u0275pipe(7, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(8, "div", 14);
    \u0275\u0275text(9);
    \u0275\u0275pipe(10, "number");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(11, "div", 15)(12, "div", 16)(13, "div", 17);
    \u0275\u0275text(14, "Invested");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(15, "div", 18);
    \u0275\u0275text(16);
    \u0275\u0275pipe(17, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(18, "div", 16)(19, "div", 17);
    \u0275\u0275text(20, "Gain/Loss");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(21, "div", 19);
    \u0275\u0275text(22);
    \u0275\u0275pipe(23, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(24, "div", 16)(25, "div", 17);
    \u0275\u0275text(26, "XIRR");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(27, "div", 20);
    \u0275\u0275text(28);
    \u0275\u0275pipe(29, "number");
    \u0275\u0275elementEnd()()()();
    \u0275\u0275template(30, PortfolioComponent_Conditional_9_Conditional_30_Template, 10, 0);
    \u0275\u0275elementStart(31, "div", 21)(32, "span", 22);
    \u0275\u0275text(33, "Holdings");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(34, "button", 23);
    \u0275\u0275listener("click", function PortfolioComponent_Conditional_9_Template_button_click_34_listener() {
      \u0275\u0275restoreView(_r3);
      const ctx_r1 = \u0275\u0275nextContext();
      return \u0275\u0275resetView(ctx_r1.showAdd.set(true));
    });
    \u0275\u0275text(35, "+ Add");
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(36, "div", 24);
    \u0275\u0275repeaterCreate(37, PortfolioComponent_Conditional_9_For_38_Template, 22, 18, "div", 25, _forTrack0);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r1 = \u0275\u0275nextContext();
    \u0275\u0275advance(6);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(7, 9, ctx_r1.summary().totalValue, true));
    \u0275\u0275advance(2);
    \u0275\u0275property("ngClass", ctx_r1.summary().totalReturnPercent >= 0 ? "positive" : "negative");
    \u0275\u0275advance();
    \u0275\u0275textInterpolate2(" ", ctx_r1.summary().totalReturnPercent >= 0 ? "\u25B2" : "\u25BC", " ", \u0275\u0275pipeBind2(10, 12, ctx_r1.summary().totalReturnPercent, "1.1-1"), "% ");
    \u0275\u0275advance(7);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(17, 15, ctx_r1.summary().totalInvested, true));
    \u0275\u0275advance(5);
    \u0275\u0275property("ngClass", ctx_r1.gain() >= 0 ? "text-success" : "text-danger");
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1(" ", \u0275\u0275pipeBind2(23, 18, ctx_r1.gain(), true), " ");
    \u0275\u0275advance(6);
    \u0275\u0275textInterpolate1("", \u0275\u0275pipeBind2(29, 21, ctx_r1.summary().weightedXirr, "1.1-1"), "%");
    \u0275\u0275advance(2);
    \u0275\u0275conditional(30, ctx_r1.summary().assetAllocation.length ? 30 : -1);
    \u0275\u0275advance(7);
    \u0275\u0275repeater(ctx_r1.holdings());
  }
}
var PortfolioComponent = class _PortfolioComponent {
  constructor(portfolioService, toastService) {
    this.portfolioService = portfolioService;
    this.toastService = toastService;
    this.summary = this.portfolioService.summary;
    this.holdings = signal([]);
    this.loading = signal(false);
    this.syncing = signal(false);
    this.showAdd = signal(false);
    this.typeColors = {
      EQUITY_MF: "#4F46E5",
      STOCKS: "#7C3AED",
      DEBT_MF: "#06B6D4",
      NPS_PPF: "#10B981",
      GOLD_SGB: "#F59E0B",
      FD: "#3B82F6",
      OTHER: "#6B7280"
    };
  }
  ngOnInit() {
    this.loading.set(true);
    this.portfolioService.getSummary().subscribe({
      next: (res) => {
        if (res.success && res.data)
          this.holdings.set(res.data.holdings ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
  sync() {
    this.syncing.set(true);
    this.portfolioService.triggerSync().subscribe({
      next: () => {
        this.syncing.set(false);
        this.toastService.success("Portfolio sync triggered \u2014 prices update after market close");
      },
      error: () => {
        this.syncing.set(false);
        this.toastService.error("Sync failed");
      }
    });
  }
  deleteHolding(id) {
    if (!confirm("Remove this holding?"))
      return;
    this.portfolioService.deleteHolding(id).subscribe({
      next: (res) => {
        if (res.success) {
          this.holdings.update((h) => h.filter((x) => x.id !== id));
          this.toastService.success("Holding removed");
        }
      },
      error: () => this.toastService.error("Failed to delete")
    });
  }
  gain() {
    const s = this.summary();
    return s ? s.totalValue - s.totalInvested : 0;
  }
  getReturnPct(h) {
    if (!h.invested || h.invested === 0)
      return 0;
    return (h.currentValue - h.invested) / h.invested * 100;
  }
  getTypeColor(type) {
    return this.typeColors[type] ?? "#6B7280";
  }
  getTypeShort(type) {
    return {
      EQUITY_MF: "MF",
      STOCKS: "EQ",
      DEBT_MF: "DEBT",
      NPS_PPF: "NPS",
      GOLD_SGB: "GOLD",
      FD: "FD",
      OTHER: "OTH"
    }[type] ?? type.slice(0, 3);
  }
  static {
    this.\u0275fac = function PortfolioComponent_Factory(t) {
      return new (t || _PortfolioComponent)(\u0275\u0275directiveInject(PortfolioService), \u0275\u0275directiveInject(ToastService));
    };
  }
  static {
    this.\u0275cmp = /* @__PURE__ */ \u0275\u0275defineComponent({ type: _PortfolioComponent, selectors: [["app-portfolio"]], standalone: true, features: [\u0275\u0275StandaloneFeature], decls: 10, vars: 3, consts: [[1, "page"], [1, "page-header"], [1, "page-title"], ["title", "Sync prices", 1, "btn", "btn-icon", 3, "click", "disabled"], ["message", "Loading portfolio..."], [1, "empty-state"], [1, "empty-icon"], [1, "empty-title"], [1, "empty-body"], [1, "btn", "btn-primary", "btn-sm", 2, "margin-top", "16px", 3, "click"], [1, "port-hero", "card"], [1, "ph-row"], [1, "ph-label"], [1, "ph-amount"], [1, "ph-return", 3, "ngClass"], [1, "ph-stats"], [1, "ph-stat"], [1, "ph-stat-label"], [1, "ph-stat-val"], [1, "ph-stat-val", 3, "ngClass"], [1, "ph-stat-val", "text-success"], [1, "section-header"], [1, "section-title"], [1, "section-action", 3, "click"], [1, "holdings-list"], [1, "holding-card", "card", "card-sm"], [1, "alloc-bar-wrap", "card", "card-sm"], [1, "alloc-bar"], [1, "alloc-seg", 3, "width", "background", "title"], [1, "alloc-legend"], [1, "alloc-item"], [1, "alloc-seg", 3, "title"], [1, "alloc-dot"], [1, "alloc-label"], [1, "alloc-pct"], [1, "hc-top"], [1, "hc-type-badge"], [1, "hc-info"], [1, "hc-name"], [1, "hc-symbol"], [1, "hc-value"], [1, "hc-current"], [2, "font-size", "12px", "font-weight", "700", 3, "ngClass"], [1, "hc-bottom"], [1, "text-muted", 2, "font-size", "12px"], [1, "text-muted", 2, "font-size", "11px"], [1, "del-btn", 3, "click"]], template: function PortfolioComponent_Template(rf, ctx) {
      if (rf & 1) {
        \u0275\u0275elementStart(0, "div", 0);
        \u0275\u0275element(1, "app-toast");
        \u0275\u0275elementStart(2, "header", 1)(3, "h1", 2);
        \u0275\u0275text(4, "Portfolio");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(5, "button", 3);
        \u0275\u0275listener("click", function PortfolioComponent_Template_button_click_5_listener() {
          return ctx.sync();
        });
        \u0275\u0275text(6);
        \u0275\u0275elementEnd()();
        \u0275\u0275template(7, PortfolioComponent_Conditional_7_Template, 1, 0, "app-loading-spinner", 4)(8, PortfolioComponent_Conditional_8_Template, 9, 0)(9, PortfolioComponent_Conditional_9_Template, 39, 24);
        \u0275\u0275elementEnd();
      }
      if (rf & 2) {
        \u0275\u0275advance(5);
        \u0275\u0275property("disabled", ctx.syncing());
        \u0275\u0275advance();
        \u0275\u0275textInterpolate1(" ", ctx.syncing() ? "\u23F3" : "\u{1F504}", " ");
        \u0275\u0275advance();
        \u0275\u0275conditional(7, ctx.loading() ? 7 : !ctx.summary() ? 8 : 9);
      }
    }, dependencies: [NgClass, ToastComponent, LoadingSpinnerComponent, InrFormatPipe, DecimalPipe, SlicePipe], styles: ["\n\n.port-hero[_ngcontent-%COMP%] {\n  padding: 20px;\n}\n.ph-row[_ngcontent-%COMP%] {\n  display: flex;\n  justify-content: space-between;\n  align-items: flex-start;\n}\n.ph-label[_ngcontent-%COMP%] {\n  font-size: 12px;\n  color: var(--color-text-muted);\n  font-weight: 600;\n  text-transform: uppercase;\n}\n.ph-amount[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 30px;\n  font-weight: 900;\n}\n.ph-return[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 20px;\n  font-weight: 900;\n  padding: 6px 12px;\n  border-radius: 12px;\n}\n.ph-return.positive[_ngcontent-%COMP%] {\n  background: var(--color-success-light);\n  color: var(--color-success);\n}\n.ph-return.negative[_ngcontent-%COMP%] {\n  background: var(--color-danger-light);\n  color: var(--color-danger);\n}\n.ph-stats[_ngcontent-%COMP%] {\n  display: flex;\n  gap: 0;\n  margin-top: 16px;\n}\n.ph-stat[_ngcontent-%COMP%] {\n  flex: 1;\n  text-align: center;\n  border-right: 1px solid var(--color-border);\n}\n.ph-stat[_ngcontent-%COMP%]:last-child {\n  border-right: none;\n}\n.ph-stat-label[_ngcontent-%COMP%] {\n  font-size: 11px;\n  color: var(--color-text-muted);\n  font-weight: 600;\n}\n.ph-stat-val[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 16px;\n  font-weight: 900;\n  margin-top: 2px;\n}\n.alloc-bar-wrap[_ngcontent-%COMP%] {\n  padding: 14px;\n}\n.alloc-bar[_ngcontent-%COMP%] {\n  display: flex;\n  height: 10px;\n  border-radius: 99px;\n  overflow: hidden;\n  gap: 2px;\n  margin-bottom: 10px;\n}\n.alloc-seg[_ngcontent-%COMP%] {\n  border-radius: 99px;\n  transition: width .3s;\n}\n.alloc-legend[_ngcontent-%COMP%] {\n  display: flex;\n  flex-wrap: wrap;\n  gap: 10px;\n}\n.alloc-item[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 5px;\n  font-size: 12px;\n}\n.alloc-dot[_ngcontent-%COMP%] {\n  width: 8px;\n  height: 8px;\n  border-radius: 50%;\n}\n.alloc-label[_ngcontent-%COMP%] {\n  color: var(--color-text-secondary);\n}\n.alloc-pct[_ngcontent-%COMP%] {\n  font-weight: 700;\n}\n.holdings-list[_ngcontent-%COMP%] {\n  display: flex;\n  flex-direction: column;\n  gap: 8px;\n  margin-bottom: 16px;\n}\n.holding-card[_ngcontent-%COMP%] {\n  padding: 12px 14px;\n}\n.hc-top[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 10px;\n}\n.hc-type-badge[_ngcontent-%COMP%] {\n  padding: 4px 8px;\n  border-radius: 8px;\n  font-size: 10px;\n  font-weight: 900;\n  color: #fff;\n  flex-shrink: 0;\n}\n.hc-info[_ngcontent-%COMP%] {\n  flex: 1;\n  min-width: 0;\n}\n.hc-name[_ngcontent-%COMP%] {\n  font-weight: 700;\n  font-size: 13px;\n  white-space: nowrap;\n  overflow: hidden;\n  text-overflow: ellipsis;\n}\n.hc-symbol[_ngcontent-%COMP%] {\n  font-size: 11px;\n  color: var(--color-text-muted);\n}\n.hc-value[_ngcontent-%COMP%] {\n  text-align: right;\n}\n.hc-current[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 15px;\n  font-weight: 900;\n}\n.hc-bottom[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  justify-content: space-between;\n  margin-top: 8px;\n}\n.del-btn[_ngcontent-%COMP%] {\n  background: none;\n  border: none;\n  cursor: pointer;\n  font-size: 14px;\n  opacity: 0.5;\n}\n/*# sourceMappingURL=portfolio.component.css.map */"], changeDetection: 0 });
  }
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && \u0275setClassDebugInfo(PortfolioComponent, { className: "PortfolioComponent", filePath: "src/app/features/portfolio/portfolio.component.ts", lineNumber: 169 });
})();
export {
  PortfolioComponent
};
//# sourceMappingURL=chunk-WI4XAFH5.js.map
