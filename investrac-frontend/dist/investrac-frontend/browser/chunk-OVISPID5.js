import {
  DefaultValueAccessor,
  FormBuilder,
  FormControlName,
  FormGroupDirective,
  MaxLengthValidator,
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
  ɵɵstyleProp,
  ɵɵtemplate,
  ɵɵtext,
  ɵɵtextInterpolate,
  ɵɵtextInterpolate1
} from "./chunk-TWKZKYET.js";

// src/app/features/auth/register/register.component.ts
var _c0 = (a0) => ({ "error": a0 });
function RegisterComponent_Conditional_18_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 12);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r0 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(ctx_r0.getError("name"));
  }
}
function RegisterComponent_Conditional_23_Template(rf, ctx) {
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
function RegisterComponent_Conditional_33_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 12);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r0 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(ctx_r0.getError("phone"));
  }
}
function RegisterComponent_Conditional_41_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 27);
    \u0275\u0275element(1, "div", 28);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(2, "span", 29);
    \u0275\u0275text(3);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r0 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275styleProp("width", ctx_r0.strengthPct() + "%")("background", ctx_r0.strengthColor());
    \u0275\u0275advance();
    \u0275\u0275styleProp("color", ctx_r0.strengthColor());
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(ctx_r0.strengthLabel());
  }
}
function RegisterComponent_Conditional_42_Template(rf, ctx) {
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
function RegisterComponent_Conditional_43_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 21);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r0 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1("\u26A0\uFE0F ", ctx_r0.errorMsg(), "");
  }
}
function RegisterComponent_Conditional_45_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "span", 30);
    \u0275\u0275text(1, " Creating account... ");
  }
}
function RegisterComponent_Conditional_46_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275text(0, " Create Account ");
  }
}
function strongPasswordValidator(ctrl) {
  const val = ctrl.value;
  if (!val)
    return null;
  const ok = val.length >= 8 && /[A-Z]/.test(val) && /[a-z]/.test(val) && /\d/.test(val) && /[@#$%^&+=!*()_\-]/.test(val);
  return ok ? null : { weakPassword: true };
}
var RegisterComponent = class _RegisterComponent {
  constructor(fb, authService, toastService, router) {
    this.fb = fb;
    this.authService = authService;
    this.toastService = toastService;
    this.router = router;
    this.loading = signal(false);
    this.showPwd = signal(false);
    this.errorMsg = signal("");
    this.form = this.fb.group({
      name: ["", [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
        Validators.pattern(/^[a-zA-Z\s''-]+$/)
      ]],
      email: ["", [Validators.required, Validators.email]],
      phone: ["", [Validators.pattern(/^[6-9]\d{9}$/)]],
      password: ["", [Validators.required, strongPasswordValidator]]
    });
  }
  togglePwd() {
    this.showPwd.update((v) => !v);
  }
  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMsg.set("");
    const val = this.form.value;
    this.authService.register({
      name: val.name.trim(),
      email: val.email.toLowerCase().trim(),
      phone: val.phone || void 0,
      password: val.password
    }).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.success) {
          this.toastService.success("Account created! Welcome to INVESTRAC \u{1F389}");
          this.router.navigate(["/home"]);
        } else {
          this.errorMsg.set(res.message || "Registration failed.");
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err.error?.message || "Registration failed. Please try again.");
      }
    });
  }
  strengthPct() {
    const pwd = this.form.get("password")?.value || "";
    let score = 0;
    if (pwd.length >= 8)
      score += 25;
    if (/[A-Z]/.test(pwd))
      score += 25;
    if (/\d/.test(pwd))
      score += 25;
    if (/[@#$%^&+=!*()_\-]/.test(pwd))
      score += 25;
    return score;
  }
  strengthColor() {
    const p = this.strengthPct();
    if (p <= 25)
      return "#EF4444";
    if (p <= 50)
      return "#F59E0B";
    if (p <= 75)
      return "#3B82F6";
    return "#10B981";
  }
  strengthLabel() {
    const p = this.strengthPct();
    if (p <= 25)
      return "Weak";
    if (p <= 50)
      return "Fair";
    if (p <= 75)
      return "Good";
    return "Strong \u2713";
  }
  showError(field) {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && (ctrl.dirty || ctrl.touched));
  }
  getError(field) {
    const ctrl = this.form.get(field);
    if (!ctrl?.errors)
      return "";
    const e = ctrl.errors;
    if (e["required"])
      return `This field is required`;
    if (e["email"])
      return "Enter a valid email address";
    if (e["minlength"])
      return `Minimum ${e["minlength"].requiredLength} characters`;
    if (e["maxlength"])
      return `Maximum ${e["maxlength"].requiredLength} characters`;
    if (e["pattern"] && field === "phone")
      return "Enter 10-digit Indian mobile number";
    if (e["pattern"] && field === "name")
      return "Name can only contain letters and spaces";
    if (e["weakPassword"])
      return "Must have uppercase, lowercase, number and special character (@#$%^&+=!*)";
    return "Invalid value";
  }
  static {
    this.\u0275fac = function RegisterComponent_Factory(t) {
      return new (t || _RegisterComponent)(\u0275\u0275directiveInject(FormBuilder), \u0275\u0275directiveInject(AuthService), \u0275\u0275directiveInject(ToastService), \u0275\u0275directiveInject(Router));
    };
  }
  static {
    this.\u0275cmp = /* @__PURE__ */ \u0275\u0275defineComponent({ type: _RegisterComponent, selectors: [["app-register"]], standalone: true, features: [\u0275\u0275StandaloneFeature], decls: 58, vars: 23, consts: [[1, "auth-screen"], [1, "auth-brand"], [1, "brand-logo"], [1, "brand-icon"], [1, "brand-name"], [1, "brand-tagline"], [1, "auth-card", 3, "ngSubmit", "formGroup"], [1, "auth-title"], [1, "auth-subtitle"], [1, "form-group"], [1, "form-label"], ["type", "text", "formControlName", "name", "placeholder", "Arjun Kumar", "autocomplete", "name", 1, "form-input", 3, "ngClass"], [1, "form-error"], ["type", "email", "formControlName", "email", "placeholder", "arjun@investrac.in", "autocomplete", "email", "inputmode", "email", 1, "form-input", 3, "ngClass"], [1, "optional"], [1, "input-phone"], [1, "phone-code"], ["type", "tel", "formControlName", "phone", "placeholder", "98765 43210", "autocomplete", "tel", "inputmode", "numeric", "maxlength", "10", 1, "form-input", 3, "ngClass"], [1, "input-wrap"], ["formControlName", "password", "placeholder", "Min 8 chars, uppercase, number, special", "autocomplete", "new-password", 1, "form-input", 3, "ngClass", "type"], ["type", "button", 1, "eye-btn", 3, "click"], [1, "alert", "alert-error"], ["type", "submit", 1, "btn", "btn-primary", "btn-full", 3, "disabled"], [1, "auth-terms"], ["href", "#", 1, "link"], [1, "auth-switch"], ["routerLink", "/auth/login", 1, "link"], [1, "strength-bar"], [1, "strength-fill"], [1, "strength-label"], [1, "spinner"]], template: function RegisterComponent_Template(rf, ctx) {
      if (rf & 1) {
        \u0275\u0275elementStart(0, "div", 0)(1, "div", 1)(2, "div", 2)(3, "span", 3);
        \u0275\u0275text(4, "\u20B9");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(5, "h1", 4);
        \u0275\u0275text(6, "INVESTRAC");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(7, "p", 5);
        \u0275\u0275text(8, "Start your financial journey");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(9, "form", 6);
        \u0275\u0275listener("ngSubmit", function RegisterComponent_Template_form_ngSubmit_9_listener() {
          return ctx.submit();
        });
        \u0275\u0275elementStart(10, "h2", 7);
        \u0275\u0275text(11, "Create account");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(12, "p", 8);
        \u0275\u0275text(13, "Join thousands of smart investors");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(14, "div", 9)(15, "label", 10);
        \u0275\u0275text(16, "Full Name");
        \u0275\u0275elementEnd();
        \u0275\u0275element(17, "input", 11);
        \u0275\u0275template(18, RegisterComponent_Conditional_18_Template, 2, 1, "span", 12);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(19, "div", 9)(20, "label", 10);
        \u0275\u0275text(21, "Email address");
        \u0275\u0275elementEnd();
        \u0275\u0275element(22, "input", 13);
        \u0275\u0275template(23, RegisterComponent_Conditional_23_Template, 2, 1, "span", 12);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(24, "div", 9)(25, "label", 10);
        \u0275\u0275text(26, "Mobile Number ");
        \u0275\u0275elementStart(27, "span", 14);
        \u0275\u0275text(28, "(optional)");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(29, "div", 15)(30, "span", 16);
        \u0275\u0275text(31, "+91");
        \u0275\u0275elementEnd();
        \u0275\u0275element(32, "input", 17);
        \u0275\u0275elementEnd();
        \u0275\u0275template(33, RegisterComponent_Conditional_33_Template, 2, 1, "span", 12);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(34, "div", 9)(35, "label", 10);
        \u0275\u0275text(36, "Password");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(37, "div", 18);
        \u0275\u0275element(38, "input", 19);
        \u0275\u0275elementStart(39, "button", 20);
        \u0275\u0275listener("click", function RegisterComponent_Template_button_click_39_listener() {
          return ctx.togglePwd();
        });
        \u0275\u0275text(40);
        \u0275\u0275elementEnd()();
        \u0275\u0275template(41, RegisterComponent_Conditional_41_Template, 4, 7)(42, RegisterComponent_Conditional_42_Template, 2, 1, "span", 12);
        \u0275\u0275elementEnd();
        \u0275\u0275template(43, RegisterComponent_Conditional_43_Template, 2, 1, "div", 21);
        \u0275\u0275elementStart(44, "button", 22);
        \u0275\u0275template(45, RegisterComponent_Conditional_45_Template, 2, 0)(46, RegisterComponent_Conditional_46_Template, 1, 0);
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(47, "p", 23);
        \u0275\u0275text(48, " By registering you agree to our ");
        \u0275\u0275elementStart(49, "a", 24);
        \u0275\u0275text(50, "Terms of Service");
        \u0275\u0275elementEnd();
        \u0275\u0275text(51, " and ");
        \u0275\u0275elementStart(52, "a", 24);
        \u0275\u0275text(53, "Privacy Policy");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(54, "p", 25);
        \u0275\u0275text(55, " Already have an account? ");
        \u0275\u0275elementStart(56, "a", 26);
        \u0275\u0275text(57, "Sign in");
        \u0275\u0275elementEnd()()()();
      }
      if (rf & 2) {
        let tmp_10_0;
        \u0275\u0275advance(9);
        \u0275\u0275property("formGroup", ctx.form);
        \u0275\u0275advance(8);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(15, _c0, ctx.showError("name")));
        \u0275\u0275advance();
        \u0275\u0275conditional(18, ctx.showError("name") ? 18 : -1);
        \u0275\u0275advance(4);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(17, _c0, ctx.showError("email")));
        \u0275\u0275advance();
        \u0275\u0275conditional(23, ctx.showError("email") ? 23 : -1);
        \u0275\u0275advance(9);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(19, _c0, ctx.showError("phone")));
        \u0275\u0275advance();
        \u0275\u0275conditional(33, ctx.showError("phone") ? 33 : -1);
        \u0275\u0275advance(5);
        \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(21, _c0, ctx.showError("password")))("type", ctx.showPwd() ? "text" : "password");
        \u0275\u0275advance(2);
        \u0275\u0275textInterpolate1(" ", ctx.showPwd() ? "\u{1F648}" : "\u{1F441}\uFE0F", " ");
        \u0275\u0275advance();
        \u0275\u0275conditional(41, ((tmp_10_0 = ctx.form.get("password")) == null ? null : tmp_10_0.value) ? 41 : -1);
        \u0275\u0275advance();
        \u0275\u0275conditional(42, ctx.showError("password") ? 42 : -1);
        \u0275\u0275advance();
        \u0275\u0275conditional(43, ctx.errorMsg() ? 43 : -1);
        \u0275\u0275advance();
        \u0275\u0275property("disabled", ctx.loading());
        \u0275\u0275advance();
        \u0275\u0275conditional(45, ctx.loading() ? 45 : 46);
      }
    }, dependencies: [ReactiveFormsModule, \u0275NgNoValidate, DefaultValueAccessor, NgControlStatus, NgControlStatusGroup, MaxLengthValidator, FormGroupDirective, FormControlName, RouterLink, NgClass], styles: ["\n\n.auth-screen[_ngcontent-%COMP%] {\n  min-height: 100dvh;\n  display: flex;\n  flex-direction: column;\n  align-items: center;\n  justify-content: center;\n  padding: 24px 20px;\n  background: var(--color-bg);\n}\n.auth-brand[_ngcontent-%COMP%] {\n  text-align: center;\n  margin-bottom: 28px;\n}\n.brand-logo[_ngcontent-%COMP%] {\n  width: 56px;\n  height: 56px;\n  background:\n    linear-gradient(\n      135deg,\n      #4F46E5,\n      #7C3AED);\n  border-radius: 18px;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  margin: 0 auto 10px;\n  box-shadow: 0 6px 20px rgba(79, 70, 229, .3);\n}\n.brand-icon[_ngcontent-%COMP%] {\n  font-size: 24px;\n  color: #fff;\n  font-weight: 900;\n}\n.brand-name[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 24px;\n  font-weight: 900;\n  color: var(--color-primary);\n  margin: 0 0 2px;\n}\n.brand-tagline[_ngcontent-%COMP%] {\n  font-size: 13px;\n  color: var(--color-text-muted);\n  margin: 0;\n}\n.auth-card[_ngcontent-%COMP%] {\n  width: 100%;\n  max-width: 390px;\n  background: var(--color-card);\n  border: 1.5px solid var(--color-border);\n  border-radius: 24px;\n  padding: 24px;\n  box-shadow: var(--shadow-card);\n}\n.auth-title[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 20px;\n  font-weight: 900;\n  margin: 0 0 4px;\n}\n.auth-subtitle[_ngcontent-%COMP%] {\n  font-size: 13px;\n  color: var(--color-text-muted);\n  margin: 0 0 20px;\n}\n.form-group[_ngcontent-%COMP%] {\n  margin-bottom: 14px;\n}\n.form-label[_ngcontent-%COMP%] {\n  display: block;\n  font-size: 13px;\n  font-weight: 600;\n  color: var(--color-text);\n  margin-bottom: 5px;\n}\n.optional[_ngcontent-%COMP%] {\n  font-weight: 400;\n  color: var(--color-text-muted);\n}\n.form-input[_ngcontent-%COMP%] {\n  width: 100%;\n  padding: 11px 13px;\n  background: var(--color-bg);\n  border: 1.5px solid var(--color-border);\n  border-radius: 11px;\n  font-size: 15px;\n  color: var(--color-text);\n  outline: none;\n  box-sizing: border-box;\n  transition: border-color .2s;\n}\n.form-input[_ngcontent-%COMP%]:focus {\n  border-color: var(--color-primary);\n}\n.form-input.error[_ngcontent-%COMP%] {\n  border-color: var(--color-danger);\n}\n.form-error[_ngcontent-%COMP%] {\n  font-size: 12px;\n  color: var(--color-danger);\n  font-weight: 600;\n  margin-top: 3px;\n  display: block;\n}\n.input-wrap[_ngcontent-%COMP%] {\n  position: relative;\n}\n.input-wrap[_ngcontent-%COMP%]   .form-input[_ngcontent-%COMP%] {\n  padding-right: 44px;\n}\n.eye-btn[_ngcontent-%COMP%] {\n  position: absolute;\n  right: 12px;\n  top: 50%;\n  transform: translateY(-50%);\n  background: none;\n  border: none;\n  cursor: pointer;\n  font-size: 18px;\n  padding: 0;\n}\n.input-phone[_ngcontent-%COMP%] {\n  display: flex;\n  gap: 8px;\n  align-items: center;\n}\n.phone-code[_ngcontent-%COMP%] {\n  font-size: 15px;\n  color: var(--color-text-muted);\n  font-weight: 600;\n  white-space: nowrap;\n}\n.input-phone[_ngcontent-%COMP%]   .form-input[_ngcontent-%COMP%] {\n  flex: 1;\n}\n.strength-bar[_ngcontent-%COMP%] {\n  height: 4px;\n  background: var(--color-border);\n  border-radius: 4px;\n  overflow: hidden;\n  margin-top: 6px;\n}\n.strength-fill[_ngcontent-%COMP%] {\n  height: 100%;\n  border-radius: 4px;\n  transition: width .3s, background .3s;\n}\n.strength-label[_ngcontent-%COMP%] {\n  font-size: 11px;\n  font-weight: 700;\n  margin-top: 3px;\n  display: block;\n}\n.alert[_ngcontent-%COMP%] {\n  padding: 10px 14px;\n  border-radius: 10px;\n  font-size: 13px;\n  font-weight: 600;\n  margin-bottom: 14px;\n}\n.alert-error[_ngcontent-%COMP%] {\n  background: var(--color-danger-light);\n  color: var(--color-danger);\n}\n.btn-primary[_ngcontent-%COMP%] {\n  background: var(--color-primary);\n  color: #fff;\n  border: none;\n  border-radius: 14px;\n  padding: 14px;\n  font-family: var(--font-heading);\n  font-size: 15px;\n  font-weight: 800;\n  cursor: pointer;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  gap: 8px;\n  transition: opacity .2s;\n}\n.btn-primary[_ngcontent-%COMP%]:disabled {\n  opacity: .6;\n  cursor: not-allowed;\n}\n.btn-full[_ngcontent-%COMP%] {\n  width: 100%;\n}\n.spinner[_ngcontent-%COMP%] {\n  width: 16px;\n  height: 16px;\n  border: 2px solid rgba(255, 255, 255, .3);\n  border-top-color: #fff;\n  border-radius: 50%;\n  animation: _ngcontent-%COMP%_spin .6s linear infinite;\n}\n@keyframes _ngcontent-%COMP%_spin {\n  to {\n    transform: rotate(360deg);\n  }\n}\n.auth-terms[_ngcontent-%COMP%] {\n  font-size: 12px;\n  color: var(--color-text-muted);\n  text-align: center;\n  margin: 14px 0 0;\n}\n.auth-switch[_ngcontent-%COMP%] {\n  text-align: center;\n  font-size: 14px;\n  color: var(--color-text-muted);\n  margin: 10px 0 0;\n}\n.link[_ngcontent-%COMP%] {\n  color: var(--color-primary);\n  font-weight: 700;\n  text-decoration: none;\n}\n/*# sourceMappingURL=register.component.css.map */"], changeDetection: 0 });
  }
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && \u0275setClassDebugInfo(RegisterComponent, { className: "RegisterComponent", filePath: "src/app/features/auth/register/register.component.ts", lineNumber: 160 });
})();
export {
  RegisterComponent
};
//# sourceMappingURL=chunk-OVISPID5.js.map
