import {
  AuthService
} from "./chunk-SK7XFWA5.js";
import "./chunk-VO5CVMHZ.js";
import {
  ApiService
} from "./chunk-QGHRW6JC.js";
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
  ɵɵlistener,
  ɵɵproperty,
  ɵɵpureFunction1,
  ɵɵtemplate,
  ɵɵtext,
  ɵɵtextInterpolate,
  ɵɵtextInterpolate1
} from "./chunk-TWKZKYET.js";

// src/app/features/settings/settings.component.ts
var _c0 = (a0) => ({ "active": a0 });
var _c1 = (a0) => ({ "on": a0 });
function SettingsComponent_Conditional_13_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 8);
    \u0275\u0275text(1, "KYC Verified \u2713");
    \u0275\u0275elementEnd();
  }
}
function SettingsComponent_Conditional_14_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 23);
    \u0275\u0275text(1, "KYC Pending");
    \u0275\u0275elementEnd();
  }
}
function SettingsComponent_Conditional_79_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 23);
    \u0275\u0275text(1, "Required");
    \u0275\u0275elementEnd();
  }
}
var SettingsComponent = class _SettingsComponent {
  constructor(authService, toastService, apiService) {
    this.authService = authService;
    this.toastService = toastService;
    this.apiService = apiService;
    this.profile = signal(null);
    this.theme = signal("light");
    this.showLakhs = signal(true);
    this.aiInsights = signal(true);
  }
  ngOnInit() {
    this.apiService.get("/users/me").subscribe({
      next: (res) => {
        if (res.success && res.data)
          this.profile.set(res.data);
      }
    });
  }
  toggleLakhs() {
    this.showLakhs.update((v) => !v);
  }
  toggleAiInsights() {
    this.aiInsights.update((v) => !v);
  }
  initials() {
    return (this.authService.userName() || "U").split(" ").map((n) => n[0]).join("").slice(0, 2).toUpperCase();
  }
  setTheme(t) {
    this.theme.set(t);
    document.documentElement.setAttribute("data-theme", t);
  }
  logout() {
    if (confirm("Are you sure you want to sign out?")) {
      this.authService.logout();
    }
  }
  static {
    this.\u0275fac = function SettingsComponent_Factory(t) {
      return new (t || _SettingsComponent)(\u0275\u0275directiveInject(AuthService), \u0275\u0275directiveInject(ToastService), \u0275\u0275directiveInject(ApiService));
    };
  }
  static {
    this.\u0275cmp = /* @__PURE__ */ \u0275\u0275defineComponent({ type: _SettingsComponent, selectors: [["app-settings"]], standalone: true, features: [\u0275\u0275StandaloneFeature], decls: 95, vars: 20, consts: [[1, "page"], [1, "page-header"], [1, "page-title"], [1, "profile-card", "card"], [1, "profile-avatar"], [1, "profile-info"], [1, "profile-name"], [1, "profile-email"], [1, "badge", "badge-success"], [1, "section-header"], [1, "section-title"], [1, "settings-group", "card"], [1, "settings-row"], [1, "sr-label"], [1, "sr-value"], [1, "divider"], [1, "theme-toggle"], [1, "tt-btn", 3, "click", "ngClass"], [1, "sr-sub"], [1, "toggle", 3, "click", "ngClass"], [1, "toggle-thumb"], [1, "settings-row", "settings-btn"], [1, "sr-chevron"], [1, "badge", "badge-warning"], [1, "app-info"], [1, "btn", "btn-danger", "btn-full", 2, "margin-bottom", "24px", 3, "click"]], template: function SettingsComponent_Template(rf, ctx) {
      if (rf & 1) {
        \u0275\u0275elementStart(0, "div", 0);
        \u0275\u0275element(1, "app-toast");
        \u0275\u0275elementStart(2, "header", 1)(3, "h1", 2);
        \u0275\u0275text(4, "Settings");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(5, "div", 3)(6, "div", 4);
        \u0275\u0275text(7);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(8, "div", 5)(9, "div", 6);
        \u0275\u0275text(10);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(11, "div", 7);
        \u0275\u0275text(12);
        \u0275\u0275elementEnd();
        \u0275\u0275template(13, SettingsComponent_Conditional_13_Template, 2, 0, "span", 8)(14, SettingsComponent_Conditional_14_Template, 2, 0);
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(15, "div", 9)(16, "span", 10);
        \u0275\u0275text(17, "Financial Profile");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(18, "div", 11)(19, "div", 12)(20, "span", 13);
        \u0275\u0275text(21, "Risk Profile");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(22, "span", 14);
        \u0275\u0275text(23);
        \u0275\u0275elementEnd()();
        \u0275\u0275element(24, "div", 15);
        \u0275\u0275elementStart(25, "div", 12)(26, "span", 13);
        \u0275\u0275text(27, "Tax Regime");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(28, "span", 14);
        \u0275\u0275text(29);
        \u0275\u0275elementEnd()();
        \u0275\u0275element(30, "div", 15);
        \u0275\u0275elementStart(31, "div", 12)(32, "span", 13);
        \u0275\u0275text(33, "Monthly Income");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(34, "span", 14);
        \u0275\u0275text(35);
        \u0275\u0275elementEnd()()();
        \u0275\u0275elementStart(36, "div", 9)(37, "span", 10);
        \u0275\u0275text(38, "App Preferences");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(39, "div", 11)(40, "div", 12)(41, "span", 13);
        \u0275\u0275text(42, "Theme");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(43, "div", 16)(44, "button", 17);
        \u0275\u0275listener("click", function SettingsComponent_Template_button_click_44_listener() {
          return ctx.setTheme("light");
        });
        \u0275\u0275text(45, "\u2600\uFE0F Light");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(46, "button", 17);
        \u0275\u0275listener("click", function SettingsComponent_Template_button_click_46_listener() {
          return ctx.setTheme("dark");
        });
        \u0275\u0275text(47, "\u{1F319} Dark");
        \u0275\u0275elementEnd()()();
        \u0275\u0275element(48, "div", 15);
        \u0275\u0275elementStart(49, "div", 12)(50, "div")(51, "span", 13);
        \u0275\u0275text(52, "Show amounts in Lakhs");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(53, "div", 18);
        \u0275\u0275text(54, "\u20B91.15L instead of \u20B91,15,000");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(55, "button", 19);
        \u0275\u0275listener("click", function SettingsComponent_Template_button_click_55_listener() {
          return ctx.toggleLakhs();
        });
        \u0275\u0275element(56, "span", 20);
        \u0275\u0275elementEnd()();
        \u0275\u0275element(57, "div", 15);
        \u0275\u0275elementStart(58, "div", 12)(59, "div")(60, "span", 13);
        \u0275\u0275text(61, "AI Insights");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(62, "div", 18);
        \u0275\u0275text(63, "Nightly financial analysis");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(64, "button", 19);
        \u0275\u0275listener("click", function SettingsComponent_Template_button_click_64_listener() {
          return ctx.toggleAiInsights();
        });
        \u0275\u0275element(65, "span", 20);
        \u0275\u0275elementEnd()()();
        \u0275\u0275elementStart(66, "div", 9)(67, "span", 10);
        \u0275\u0275text(68, "Security & Account");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(69, "div", 11)(70, "button", 21)(71, "span", 13);
        \u0275\u0275text(72, "Change Password");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(73, "span", 22);
        \u0275\u0275text(74, "\u203A");
        \u0275\u0275elementEnd()();
        \u0275\u0275element(75, "div", 15);
        \u0275\u0275elementStart(76, "button", 21)(77, "span", 13);
        \u0275\u0275text(78, "Update KYC");
        \u0275\u0275elementEnd();
        \u0275\u0275template(79, SettingsComponent_Conditional_79_Template, 2, 0, "span", 23);
        \u0275\u0275elementStart(80, "span", 22);
        \u0275\u0275text(81, "\u203A");
        \u0275\u0275elementEnd()();
        \u0275\u0275element(82, "div", 15);
        \u0275\u0275elementStart(83, "button", 21)(84, "span", 13);
        \u0275\u0275text(85, "Notification Preferences");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(86, "span", 22);
        \u0275\u0275text(87, "\u203A");
        \u0275\u0275elementEnd()()();
        \u0275\u0275elementStart(88, "div", 24)(89, "div");
        \u0275\u0275text(90, "INVESTRAC v1.0.0");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(91, "div");
        \u0275\u0275text(92, "Made with \u2764\uFE0F in India");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(93, "button", 25);
        \u0275\u0275listener("click", function SettingsComponent_Template_button_click_93_listener() {
          return ctx.logout();
        });
        \u0275\u0275text(94, " Sign Out ");
        \u0275\u0275elementEnd()();
      }
      if (rf & 2) {
        let tmp_2_0;
        let tmp_3_0;
        let tmp_4_0;
        let tmp_5_0;
        let tmp_6_0;
        let tmp_11_0;
        \u0275\u0275advance(7);
        \u0275\u0275textInterpolate1(" ", ctx.initials(), " ");
        \u0275\u0275advance(3);
        \u0275\u0275textInterpolate(ctx.authService.userName());
        \u0275\u0275advance(2);
        \u0275\u0275textInterpolate((tmp_2_0 = (tmp_2_0 = ctx.profile()) == null ? null : tmp_2_0.email) !== null && tmp_2_0 !== void 0 ? tmp_2_0 : "");
        \u0275\u0275advance();
        \u0275\u0275conditional(13, ((tmp_3_0 = ctx.profile()) == null ? null : tmp_3_0.kycVerified) ? 13 : 14);
        \u0275\u0275advance(10);
        \u0275\u0275textInterpolate((tmp_4_0 = (tmp_4_0 = ctx.profile()) == null ? null : tmp_4_0.riskProfile) !== null && tmp_4_0 !== void 0 ? tmp_4_0 : "\u2014");
        \u0275\u0275advance(6);
        \u0275\u0275textInterpolate1("", (tmp_5_0 = (tmp_5_0 = ctx.profile()) == null ? null : tmp_5_0.taxRegime) !== null && tmp_5_0 !== void 0 ? tmp_5_0 : "\u2014", " Regime");
        \u0275\u0275advance(6);
        \u0275\u0275textInterpolate1(" ", ((tmp_6_0 = ctx.profile()) == null ? null : tmp_6_0.monthlyIncome) ? "\u20B9" + (ctx.profile().monthlyIncome / 1e3).toFixed(0) + "K" : "\u2014", " ");
        \u0275\u0275advance(9);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(12, _c0, ctx.theme() === "light"));
        \u0275\u0275advance(2);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(14, _c0, ctx.theme() === "dark"));
        \u0275\u0275advance(9);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(16, _c1, ctx.showLakhs()));
        \u0275\u0275advance(9);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(18, _c1, ctx.aiInsights()));
        \u0275\u0275advance(15);
        \u0275\u0275conditional(79, !((tmp_11_0 = ctx.profile()) == null ? null : tmp_11_0.kycVerified) ? 79 : -1);
      }
    }, dependencies: [NgClass, ToastComponent], styles: ["\n\n.profile-card[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 14px;\n  padding: 16px;\n  margin-bottom: 6px;\n}\n.profile-avatar[_ngcontent-%COMP%] {\n  width: 56px;\n  height: 56px;\n  background: var(--color-primary);\n  color: #fff;\n  border-radius: 18px;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  font-family: var(--font-heading);\n  font-size: 20px;\n  font-weight: 900;\n  flex-shrink: 0;\n}\n.profile-name[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 18px;\n  font-weight: 900;\n}\n.profile-email[_ngcontent-%COMP%] {\n  font-size: 13px;\n  color: var(--color-text-muted);\n  margin: 2px 0 6px;\n}\n.settings-group[_ngcontent-%COMP%] {\n  padding: 0;\n  overflow: hidden;\n}\n.settings-row[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  justify-content: space-between;\n  padding: 14px 16px;\n  width: 100%;\n  background: none;\n  border: none;\n  cursor: default;\n}\n.settings-btn[_ngcontent-%COMP%] {\n  cursor: pointer;\n}\n.settings-btn[_ngcontent-%COMP%]:hover {\n  background: var(--color-card-alt);\n}\n.sr-label[_ngcontent-%COMP%] {\n  font-size: 14px;\n  font-weight: 600;\n}\n.sr-sub[_ngcontent-%COMP%] {\n  font-size: 12px;\n  color: var(--color-text-muted);\n  margin-top: 2px;\n}\n.sr-value[_ngcontent-%COMP%] {\n  font-size: 14px;\n  color: var(--color-text-muted);\n}\n.sr-chevron[_ngcontent-%COMP%] {\n  font-size: 20px;\n  color: var(--color-text-muted);\n}\n.theme-toggle[_ngcontent-%COMP%] {\n  display: flex;\n  gap: 6px;\n}\n.tt-btn[_ngcontent-%COMP%] {\n  padding: 6px 12px;\n  border-radius: 8px;\n  border: 1.5px solid var(--color-border);\n  background: none;\n  font-size: 12px;\n  font-weight: 700;\n  cursor: pointer;\n}\n.tt-btn.active[_ngcontent-%COMP%] {\n  background: var(--color-primary);\n  border-color: var(--color-primary);\n  color: #fff;\n}\n.toggle[_ngcontent-%COMP%] {\n  width: 44px;\n  height: 24px;\n  background: var(--color-border);\n  border: none;\n  border-radius: 99px;\n  position: relative;\n  cursor: pointer;\n  transition: background .2s;\n  padding: 0;\n}\n.toggle.on[_ngcontent-%COMP%] {\n  background: var(--color-primary);\n}\n.toggle-thumb[_ngcontent-%COMP%] {\n  position: absolute;\n  left: 2px;\n  top: 2px;\n  width: 20px;\n  height: 20px;\n  background: #fff;\n  border-radius: 50%;\n  transition: transform .2s;\n  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);\n}\n.toggle.on[_ngcontent-%COMP%]   .toggle-thumb[_ngcontent-%COMP%] {\n  transform: translateX(20px);\n}\n.app-info[_ngcontent-%COMP%] {\n  text-align: center;\n  font-size: 12px;\n  color: var(--color-text-muted);\n  padding: 16px 0 12px;\n}\n/*# sourceMappingURL=settings.component.css.map */"], changeDetection: 0 });
  }
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && \u0275setClassDebugInfo(SettingsComponent, { className: "SettingsComponent", filePath: "src/app/features/settings/settings.component.ts", lineNumber: 145 });
})();
export {
  SettingsComponent
};
//# sourceMappingURL=chunk-TPSFARHT.js.map
