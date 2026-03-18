import {
  NgClass,
  ToastService,
  ɵsetClassDebugInfo,
  ɵɵStandaloneFeature,
  ɵɵadvance,
  ɵɵdefineComponent,
  ɵɵdirectiveInject,
  ɵɵelementEnd,
  ɵɵelementStart,
  ɵɵgetCurrentView,
  ɵɵlistener,
  ɵɵnextContext,
  ɵɵproperty,
  ɵɵrepeater,
  ɵɵrepeaterCreate,
  ɵɵresetView,
  ɵɵrestoreView,
  ɵɵtext,
  ɵɵtextInterpolate
} from "./chunk-TWKZKYET.js";

// src/app/shared/components/toast/toast.component.ts
var _forTrack0 = ($index, $item) => $item.id;
function ToastComponent_For_2_Template(rf, ctx) {
  if (rf & 1) {
    const _r1 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "div", 1)(1, "span", 2);
    \u0275\u0275text(2);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "span", 3);
    \u0275\u0275text(4);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "button", 4);
    \u0275\u0275listener("click", function ToastComponent_For_2_Template_button_click_5_listener() {
      const toast_r2 = \u0275\u0275restoreView(_r1).$implicit;
      const ctx_r2 = \u0275\u0275nextContext();
      return \u0275\u0275resetView(ctx_r2.toastService.dismiss(toast_r2.id));
    });
    \u0275\u0275text(6, "\u2715");
    \u0275\u0275elementEnd()();
  }
  if (rf & 2) {
    const toast_r2 = ctx.$implicit;
    const ctx_r2 = \u0275\u0275nextContext();
    \u0275\u0275property("ngClass", "toast-" + toast_r2.type);
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(ctx_r2.getIcon(toast_r2.type));
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(toast_r2.message);
  }
}
var ToastComponent = class _ToastComponent {
  constructor(toastService) {
    this.toastService = toastService;
  }
  getIcon(type) {
    return { success: "\u2705", error: "\u274C", warning: "\u26A0\uFE0F", info: "\u2139\uFE0F" }[type] ?? "\u2139\uFE0F";
  }
  static {
    this.\u0275fac = function ToastComponent_Factory(t) {
      return new (t || _ToastComponent)(\u0275\u0275directiveInject(ToastService));
    };
  }
  static {
    this.\u0275cmp = /* @__PURE__ */ \u0275\u0275defineComponent({ type: _ToastComponent, selectors: [["app-toast"]], standalone: true, features: [\u0275\u0275StandaloneFeature], decls: 3, vars: 0, consts: [[1, "toast-container"], ["role", "alert", 1, "toast", 3, "ngClass"], [1, "toast-icon"], [1, "toast-msg"], [1, "toast-close", 3, "click"]], template: function ToastComponent_Template(rf, ctx) {
      if (rf & 1) {
        \u0275\u0275elementStart(0, "div", 0);
        \u0275\u0275repeaterCreate(1, ToastComponent_For_2_Template, 7, 3, "div", 1, _forTrack0);
        \u0275\u0275elementEnd();
      }
      if (rf & 2) {
        \u0275\u0275advance();
        \u0275\u0275repeater(ctx.toastService.toasts());
      }
    }, dependencies: [NgClass], styles: ["\n\n.toast-container[_ngcontent-%COMP%] {\n  position: fixed;\n  top: 16px;\n  left: 50%;\n  transform: translateX(-50%);\n  z-index: 9999;\n  display: flex;\n  flex-direction: column;\n  gap: 8px;\n  width: calc(100% - 32px);\n  max-width: 390px;\n  pointer-events: none;\n}\n.toast[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 10px;\n  padding: 12px 14px;\n  border-radius: 14px;\n  font-size: 14px;\n  font-weight: 600;\n  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);\n  animation: _ngcontent-%COMP%_slideDown 0.3s cubic-bezier(0.4, 0, 0.2, 1);\n  pointer-events: all;\n}\n@keyframes _ngcontent-%COMP%_slideDown {\n  from {\n    opacity: 0;\n    transform: translateY(-12px);\n  }\n  to {\n    opacity: 1;\n    transform: translateY(0);\n  }\n}\n.toast-success[_ngcontent-%COMP%] {\n  background: #ECFDF5;\n  color: #065F46;\n  border: 1.5px solid #A7F3D0;\n}\n.toast-error[_ngcontent-%COMP%] {\n  background: #FEF2F2;\n  color: #991B1B;\n  border: 1.5px solid #FECACA;\n}\n.toast-warning[_ngcontent-%COMP%] {\n  background: #FFFBEB;\n  color: #92400E;\n  border: 1.5px solid #FDE68A;\n}\n.toast-info[_ngcontent-%COMP%] {\n  background: #EFF6FF;\n  color: #1E40AF;\n  border: 1.5px solid #BFDBFE;\n}\n.toast-icon[_ngcontent-%COMP%] {\n  font-size: 16px;\n  flex-shrink: 0;\n}\n.toast-msg[_ngcontent-%COMP%] {\n  flex: 1;\n}\n.toast-close[_ngcontent-%COMP%] {\n  background: none;\n  border: none;\n  cursor: pointer;\n  opacity: 0.6;\n  font-size: 12px;\n  padding: 2px;\n  margin-left: auto;\n}\n/*# sourceMappingURL=toast.component.css.map */"], changeDetection: 0 });
  }
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && \u0275setClassDebugInfo(ToastComponent, { className: "ToastComponent", filePath: "src/app/shared/components/toast/toast.component.ts", lineNumber: 60 });
})();

export {
  ToastComponent
};
//# sourceMappingURL=chunk-TLGCWV2M.js.map
