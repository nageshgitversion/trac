import {
  ɵsetClassDebugInfo,
  ɵɵStandaloneFeature,
  ɵɵadvance,
  ɵɵclassProp,
  ɵɵconditional,
  ɵɵdefineComponent,
  ɵɵelement,
  ɵɵelementEnd,
  ɵɵelementStart,
  ɵɵnextContext,
  ɵɵtemplate,
  ɵɵtext,
  ɵɵtextInterpolate
} from "./chunk-TWKZKYET.js";

// src/app/shared/components/loading-spinner/loading-spinner.component.ts
function LoadingSpinnerComponent_Conditional_2_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "p", 2);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r0 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(ctx_r0.message);
  }
}
var LoadingSpinnerComponent = class _LoadingSpinnerComponent {
  constructor() {
    this.fullScreen = false;
    this.message = "";
  }
  static {
    this.\u0275fac = function LoadingSpinnerComponent_Factory(t) {
      return new (t || _LoadingSpinnerComponent)();
    };
  }
  static {
    this.\u0275cmp = /* @__PURE__ */ \u0275\u0275defineComponent({ type: _LoadingSpinnerComponent, selectors: [["app-loading-spinner"]], inputs: { fullScreen: "fullScreen", message: "message" }, standalone: true, features: [\u0275\u0275StandaloneFeature], decls: 3, vars: 3, consts: [[1, "loading-wrap"], [1, "spinner-ring"], [1, "loading-msg"]], template: function LoadingSpinnerComponent_Template(rf, ctx) {
      if (rf & 1) {
        \u0275\u0275elementStart(0, "div", 0);
        \u0275\u0275element(1, "div", 1);
        \u0275\u0275template(2, LoadingSpinnerComponent_Conditional_2_Template, 2, 1, "p", 2);
        \u0275\u0275elementEnd();
      }
      if (rf & 2) {
        \u0275\u0275classProp("full-screen", ctx.fullScreen);
        \u0275\u0275advance(2);
        \u0275\u0275conditional(2, ctx.message ? 2 : -1);
      }
    }, styles: ["\n\n.loading-wrap[_ngcontent-%COMP%] {\n  display: flex;\n  flex-direction: column;\n  align-items: center;\n  justify-content: center;\n  padding: 40px;\n  gap: 12px;\n}\n.loading-wrap.full-screen[_ngcontent-%COMP%] {\n  position: fixed;\n  inset: 0;\n  background: var(--color-bg);\n  z-index: 500;\n}\n.spinner-ring[_ngcontent-%COMP%] {\n  width: 36px;\n  height: 36px;\n  border: 3px solid var(--color-border);\n  border-top-color: var(--color-primary);\n  border-radius: 50%;\n  animation: _ngcontent-%COMP%_spin 0.7s linear infinite;\n}\n@keyframes _ngcontent-%COMP%_spin {\n  to {\n    transform: rotate(360deg);\n  }\n}\n.loading-msg[_ngcontent-%COMP%] {\n  font-size: 14px;\n  color: var(--color-text-muted);\n  font-weight: 600;\n}\n/*# sourceMappingURL=loading-spinner.component.css.map */"], changeDetection: 0 });
  }
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && \u0275setClassDebugInfo(LoadingSpinnerComponent, { className: "LoadingSpinnerComponent", filePath: "src/app/shared/components/loading-spinner/loading-spinner.component.ts", lineNumber: 39 });
})();

export {
  LoadingSpinnerComponent
};
//# sourceMappingURL=chunk-F3IRDWQ3.js.map
