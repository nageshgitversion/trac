import {
  DefaultValueAccessor,
  FormBuilder,
  FormControlName,
  FormGroupDirective,
  NgControlStatus,
  NgControlStatusGroup,
  ReactiveFormsModule,
  Validators,
  ɵNgNoValidate
} from "./chunk-DYTDY6K6.js";
import {
  AuthService
} from "./chunk-SK7XFWA5.js";
import {
  ActivatedRoute,
  Router,
  RouterLink
} from "./chunk-VO5CVMHZ.js";
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
  ɵɵnextContext,
  ɵɵproperty,
  ɵɵpureFunction1,
  ɵɵtemplate,
  ɵɵtext,
  ɵɵtextInterpolate,
  ɵɵtextInterpolate1
} from "./chunk-TWKZKYET.js";

// src/app/features/auth/login/login.component.ts
var _c0 = (a0) => ({ "error": a0 });
function LoginComponent_Conditional_18_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 12);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r0 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(ctx_r0.getError("email"));
  }
}
function LoginComponent_Conditional_26_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 12);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r0 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(ctx_r0.getError("password"));
  }
}
function LoginComponent_Conditional_29_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 17);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r0 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1("\u26A0\uFE0F ", ctx_r0.errorMsg(), "");
  }
}
function LoginComponent_Conditional_31_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "span", 21);
    \u0275\u0275text(1, " Signing in... ");
  }
}
function LoginComponent_Conditional_32_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275text(0, " Sign In ");
  }
}
var LoginComponent = class _LoginComponent {
  constructor(fb, authService, toastService, router, route) {
    this.fb = fb;
    this.authService = authService;
    this.toastService = toastService;
    this.router = router;
    this.route = route;
    this.loading = signal(false);
    this.showPassword_ = signal(false);
    this.errorMsg = signal("");
    this.showPassword = this.showPassword_;
    this.form = this.fb.group({
      email: ["", [Validators.required, Validators.email]],
      password: ["", [Validators.required, Validators.minLength(6)]]
    });
  }
  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMsg.set("");
    this.authService.login(this.form.value).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.success) {
          this.toastService.success(`Welcome back, ${res.data?.user.name}!`);
          const returnUrl = this.route.snapshot.queryParams["returnUrl"] || "/home";
          this.router.navigateByUrl(returnUrl);
        } else {
          this.errorMsg.set(res.message || "Login failed. Please try again.");
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err.error?.message || "Network error. Please check your connection.");
      }
    });
  }
  togglePassword() {
    this.showPassword_.update((v) => !v);
  }
  showError(field) {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && (ctrl.dirty || ctrl.touched));
  }
  getError(field) {
    const ctrl = this.form.get(field);
    if (!ctrl?.errors)
      return "";
    if (ctrl.errors["required"])
      return `${field.charAt(0).toUpperCase() + field.slice(1)} is required`;
    if (ctrl.errors["email"])
      return "Enter a valid email address";
    if (ctrl.errors["minlength"])
      return "Password must be at least 6 characters";
    return "Invalid value";
  }
  static {
    this.\u0275fac = function LoginComponent_Factory(t) {
      return new (t || _LoginComponent)(\u0275\u0275directiveInject(FormBuilder), \u0275\u0275directiveInject(AuthService), \u0275\u0275directiveInject(ToastService), \u0275\u0275directiveInject(Router), \u0275\u0275directiveInject(ActivatedRoute));
    };
  }
  static {
    this.\u0275cmp = /* @__PURE__ */ \u0275\u0275defineComponent({ type: _LoginComponent, selectors: [["app-login"]], standalone: true, features: [\u0275\u0275StandaloneFeature], decls: 37, vars: 14, consts: [[1, "auth-screen"], [1, "auth-brand"], [1, "brand-logo"], [1, "brand-icon"], [1, "brand-name"], [1, "brand-tagline"], [1, "auth-card", 3, "ngSubmit", "formGroup"], [1, "auth-title"], [1, "auth-subtitle"], [1, "form-group"], [1, "form-label"], ["type", "email", "formControlName", "email", "placeholder", "arjun@investrac.in", "autocomplete", "email", "inputmode", "email", 1, "form-input", 3, "ngClass"], [1, "form-error"], [1, "input-wrap"], ["formControlName", "password", "placeholder", "Enter your password", "autocomplete", "current-password", 1, "form-input", 3, "ngClass", "type"], ["type", "button", 1, "eye-btn", 3, "click"], ["routerLink", "/auth/forgot-password", 1, "forgot-link"], [1, "alert", "alert-error"], ["type", "submit", 1, "btn", "btn-primary", "btn-full", 3, "disabled"], [1, "auth-switch"], ["routerLink", "/auth/register", 1, "link"], [1, "spinner"]], template: function LoginComponent_Template(rf, ctx) {
      if (rf & 1) {
        \u0275\u0275elementStart(0, "div", 0)(1, "div", 1)(2, "div", 2)(3, "span", 3);
        \u0275\u0275text(4, "\u20B9");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(5, "h1", 4);
        \u0275\u0275text(6, "INVESTRAC");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(7, "p", 5);
        \u0275\u0275text(8, "Your intelligent investment tracker");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(9, "form", 6);
        \u0275\u0275listener("ngSubmit", function LoginComponent_Template_form_ngSubmit_9_listener() {
          return ctx.submit();
        });
        \u0275\u0275elementStart(10, "h2", 7);
        \u0275\u0275text(11, "Welcome back");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(12, "p", 8);
        \u0275\u0275text(13, "Sign in to your account");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(14, "div", 9)(15, "label", 10);
        \u0275\u0275text(16, "Email address");
        \u0275\u0275elementEnd();
        \u0275\u0275element(17, "input", 11);
        \u0275\u0275template(18, LoginComponent_Conditional_18_Template, 2, 1, "span", 12);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(19, "div", 9)(20, "label", 10);
        \u0275\u0275text(21, "Password");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(22, "div", 13);
        \u0275\u0275element(23, "input", 14);
        \u0275\u0275elementStart(24, "button", 15);
        \u0275\u0275listener("click", function LoginComponent_Template_button_click_24_listener() {
          return ctx.togglePassword();
        });
        \u0275\u0275text(25);
        \u0275\u0275elementEnd()();
        \u0275\u0275template(26, LoginComponent_Conditional_26_Template, 2, 1, "span", 12);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(27, "a", 16);
        \u0275\u0275text(28, "Forgot password?");
        \u0275\u0275elementEnd();
        \u0275\u0275template(29, LoginComponent_Conditional_29_Template, 2, 1, "div", 17);
        \u0275\u0275elementStart(30, "button", 18);
        \u0275\u0275template(31, LoginComponent_Conditional_31_Template, 2, 0)(32, LoginComponent_Conditional_32_Template, 1, 0);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(33, "p", 19);
        \u0275\u0275text(34, " Don't have an account? ");
        \u0275\u0275elementStart(35, "a", 20);
        \u0275\u0275text(36, "Create account");
        \u0275\u0275elementEnd()()()();
      }
      if (rf & 2) {
        \u0275\u0275advance(9);
        \u0275\u0275property("formGroup", ctx.form);
        \u0275\u0275advance(8);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(10, _c0, ctx.showError("email")));
        \u0275\u0275advance();
        \u0275\u0275conditional(18, ctx.showError("email") ? 18 : -1);
        \u0275\u0275advance(5);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(12, _c0, ctx.showError("password")))("type", ctx.showPassword() ? "text" : "password");
        \u0275\u0275advance(2);
        \u0275\u0275textInterpolate1(" ", ctx.showPassword() ? "\u{1F648}" : "\u{1F441}\uFE0F", " ");
        \u0275\u0275advance();
        \u0275\u0275conditional(26, ctx.showError("password") ? 26 : -1);
        \u0275\u0275advance(3);
        \u0275\u0275conditional(29, ctx.errorMsg() ? 29 : -1);
        \u0275\u0275advance();
        \u0275\u0275property("disabled", ctx.loading());
        \u0275\u0275advance();
        \u0275\u0275conditional(31, ctx.loading() ? 31 : 32);
      }
    }, dependencies: [ReactiveFormsModule, \u0275NgNoValidate, DefaultValueAccessor, NgControlStatus, NgControlStatusGroup, FormGroupDirective, FormControlName, RouterLink, NgClass], styles: ["\n\n.auth-screen[_ngcontent-%COMP%] {\n  min-height: 100dvh;\n  display: flex;\n  flex-direction: column;\n  align-items: center;\n  justify-content: center;\n  padding: 24px 20px;\n  background: var(--color-bg);\n}\n.auth-brand[_ngcontent-%COMP%] {\n  text-align: center;\n  margin-bottom: 32px;\n}\n.brand-logo[_ngcontent-%COMP%] {\n  width: 64px;\n  height: 64px;\n  background:\n    linear-gradient(\n      135deg,\n      #4F46E5,\n      #7C3AED);\n  border-radius: 20px;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  margin: 0 auto 12px;\n  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.3);\n}\n.brand-icon[_ngcontent-%COMP%] {\n  font-size: 28px;\n  color: #fff;\n  font-weight: 900;\n}\n.brand-name[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 26px;\n  font-weight: 900;\n  color: var(--color-primary);\n  margin: 0 0 4px;\n}\n.brand-tagline[_ngcontent-%COMP%] {\n  font-size: 14px;\n  color: var(--color-text-muted);\n  margin: 0;\n}\n.auth-card[_ngcontent-%COMP%] {\n  width: 100%;\n  max-width: 390px;\n  background: var(--color-card);\n  border: 1.5px solid var(--color-border);\n  border-radius: 24px;\n  padding: 28px 24px;\n  box-shadow: var(--shadow-card);\n}\n.auth-title[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 22px;\n  font-weight: 900;\n  margin: 0 0 4px;\n}\n.auth-subtitle[_ngcontent-%COMP%] {\n  font-size: 14px;\n  color: var(--color-text-muted);\n  margin: 0 0 24px;\n}\n.form-group[_ngcontent-%COMP%] {\n  margin-bottom: 16px;\n}\n.form-label[_ngcontent-%COMP%] {\n  display: block;\n  font-size: 13px;\n  font-weight: 600;\n  color: var(--color-text);\n  margin-bottom: 6px;\n}\n.form-input[_ngcontent-%COMP%] {\n  width: 100%;\n  padding: 12px 14px;\n  background: var(--color-bg);\n  border: 1.5px solid var(--color-border);\n  border-radius: 12px;\n  font-size: 15px;\n  color: var(--color-text);\n  outline: none;\n  box-sizing: border-box;\n  transition: border-color .2s;\n}\n.form-input[_ngcontent-%COMP%]:focus {\n  border-color: var(--color-primary);\n}\n.form-input.error[_ngcontent-%COMP%] {\n  border-color: var(--color-danger);\n}\n.form-error[_ngcontent-%COMP%] {\n  font-size: 12px;\n  color: var(--color-danger);\n  font-weight: 600;\n  margin-top: 4px;\n  display: block;\n}\n.input-wrap[_ngcontent-%COMP%] {\n  position: relative;\n}\n.input-wrap[_ngcontent-%COMP%]   .form-input[_ngcontent-%COMP%] {\n  padding-right: 44px;\n}\n.eye-btn[_ngcontent-%COMP%] {\n  position: absolute;\n  right: 12px;\n  top: 50%;\n  transform: translateY(-50%);\n  background: none;\n  border: none;\n  cursor: pointer;\n  font-size: 18px;\n  padding: 0;\n}\n.forgot-link[_ngcontent-%COMP%] {\n  display: block;\n  text-align: right;\n  font-size: 13px;\n  color: var(--color-primary);\n  font-weight: 600;\n  text-decoration: none;\n  margin: -4px 0 20px;\n}\n.alert[_ngcontent-%COMP%] {\n  padding: 10px 14px;\n  border-radius: 10px;\n  font-size: 13px;\n  font-weight: 600;\n  margin-bottom: 16px;\n}\n.alert-error[_ngcontent-%COMP%] {\n  background: var(--color-danger-light);\n  color: var(--color-danger);\n}\n.btn-primary[_ngcontent-%COMP%] {\n  background: var(--color-primary);\n  color: #fff;\n  border: none;\n  border-radius: 14px;\n  padding: 14px;\n  font-family: var(--font-heading);\n  font-size: 15px;\n  font-weight: 800;\n  cursor: pointer;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  gap: 8px;\n  transition: opacity .2s;\n}\n.btn-primary[_ngcontent-%COMP%]:disabled {\n  opacity: .6;\n  cursor: not-allowed;\n}\n.btn-full[_ngcontent-%COMP%] {\n  width: 100%;\n}\n.spinner[_ngcontent-%COMP%] {\n  width: 16px;\n  height: 16px;\n  border: 2px solid rgba(255, 255, 255, .3);\n  border-top-color: #fff;\n  border-radius: 50%;\n  animation: _ngcontent-%COMP%_spin .6s linear infinite;\n}\n@keyframes _ngcontent-%COMP%_spin {\n  to {\n    transform: rotate(360deg);\n  }\n}\n.auth-switch[_ngcontent-%COMP%] {\n  text-align: center;\n  font-size: 14px;\n  color: var(--color-text-muted);\n  margin: 20px 0 0;\n}\n.link[_ngcontent-%COMP%] {\n  color: var(--color-primary);\n  font-weight: 700;\n  text-decoration: none;\n}\n/*# sourceMappingURL=login.component.css.map */"], changeDetection: 0 });
  }
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && \u0275setClassDebugInfo(LoginComponent, { className: "LoginComponent", filePath: "src/app/features/auth/login/login.component.ts", lineNumber: 136 });
})();
export {
  LoginComponent
};
//# sourceMappingURL=chunk-R6HYHCT2.js.map
