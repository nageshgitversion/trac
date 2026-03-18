import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

/** Strong password: min 8 chars, uppercase, lowercase, digit, special char */
function strongPasswordValidator(ctrl: AbstractControl): ValidationErrors | null {
  const val = ctrl.value as string;
  if (!val) return null;
  const ok = val.length >= 8
    && /[A-Z]/.test(val)
    && /[a-z]/.test(val)
    && /\d/.test(val)
    && /[@#$%^&+=!*()_\-]/.test(val);
  return ok ? null : { weakPassword: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgClass],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="auth-screen">
      <div class="auth-brand">
        <div class="brand-logo"><span class="brand-icon">₹</span></div>
        <h1 class="brand-name">INVESTRAC</h1>
        <p class="brand-tagline">Start your financial journey</p>
      </div>

      <form class="auth-card" [formGroup]="form" (ngSubmit)="submit()">
        <h2 class="auth-title">Create account</h2>
        <p class="auth-subtitle">Join thousands of smart investors</p>

        <!-- Name -->
        <div class="form-group">
          <label class="form-label">Full Name</label>
          <input class="form-input" [ngClass]="{ 'error': showError('name') }"
            type="text" formControlName="name" placeholder="Arjun Kumar" autocomplete="name">
          @if (showError('name')) {
            <span class="form-error">{{ getError('name') }}</span>
          }
        </div>

        <!-- Email -->
        <div class="form-group">
          <label class="form-label">Email address</label>
          <input class="form-input" [ngClass]="{ 'error': showError('email') }"
            type="email" formControlName="email" placeholder="arjun@investrac.in"
            autocomplete="email" inputmode="email">
          @if (showError('email')) {
            <span class="form-error">{{ getError('email') }}</span>
          }
        </div>

        <!-- Phone (optional) -->
        <div class="form-group">
          <label class="form-label">Mobile Number <span class="optional">(optional)</span></label>
          <div class="input-phone">
            <span class="phone-code">+91</span>
            <input class="form-input" [ngClass]="{ 'error': showError('phone') }"
              type="tel" formControlName="phone" placeholder="98765 43210"
              autocomplete="tel" inputmode="numeric" maxlength="10">
          </div>
          @if (showError('phone')) {
            <span class="form-error">{{ getError('phone') }}</span>
          }
        </div>

        <!-- Password -->
        <div class="form-group">
          <label class="form-label">Password</label>
          <div class="input-wrap">
            <input class="form-input" [ngClass]="{ 'error': showError('password') }"
              [type]="showPwd() ? 'text' : 'password'"
              formControlName="password" placeholder="Min 8 chars, uppercase, number, special"
              autocomplete="new-password">
            <button type="button" class="eye-btn" (click)="togglePwd()">
              {{ showPwd() ? '🙈' : '👁️' }}
            </button>
          </div>
          <!-- Password strength bar -->
          @if (form.get('password')?.value) {
            <div class="strength-bar">
              <div class="strength-fill" [style.width]="strengthPct() + '%'"
                   [style.background]="strengthColor()"></div>
            </div>
            <span class="strength-label" [style.color]="strengthColor()">{{ strengthLabel() }}</span>
          }
          @if (showError('password')) {
            <span class="form-error">{{ getError('password') }}</span>
          }
        </div>

        @if (errorMsg()) {
          <div class="alert alert-error">⚠️ {{ errorMsg() }}</div>
        }

        <button class="btn btn-primary btn-full" type="submit" [disabled]="loading()">
          @if (loading()) {
            <span class="spinner"></span> Creating account...
          } @else {
            Create Account
          }
        </button>

        <p class="auth-terms">
          By registering you agree to our
          <a href="#" class="link">Terms of Service</a> and
          <a href="#" class="link">Privacy Policy</a>
        </p>

        <p class="auth-switch">
          Already have an account?
          <a routerLink="/auth/login" class="link">Sign in</a>
        </p>
      </form>
    </div>
  `,
  styles: [`
    .auth-screen { min-height: 100dvh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px 20px; background: var(--color-bg); }
    .auth-brand { text-align: center; margin-bottom: 28px; }
    .brand-logo { width: 56px; height: 56px; background: linear-gradient(135deg,#4F46E5,#7C3AED); border-radius: 18px; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; box-shadow: 0 6px 20px rgba(79,70,229,.3); }
    .brand-icon { font-size: 24px; color: #fff; font-weight: 900; }
    .brand-name { font-family: var(--font-heading); font-size: 24px; font-weight: 900; color: var(--color-primary); margin: 0 0 2px; }
    .brand-tagline { font-size: 13px; color: var(--color-text-muted); margin: 0; }
    .auth-card { width: 100%; max-width: 390px; background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: 24px; padding: 24px; box-shadow: var(--shadow-card); }
    .auth-title { font-family: var(--font-heading); font-size: 20px; font-weight: 900; margin: 0 0 4px; }
    .auth-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 0 0 20px; }
    .form-group { margin-bottom: 14px; }
    .form-label { display: block; font-size: 13px; font-weight: 600; color: var(--color-text); margin-bottom: 5px; }
    .optional { font-weight: 400; color: var(--color-text-muted); }
    .form-input { width: 100%; padding: 11px 13px; background: var(--color-bg); border: 1.5px solid var(--color-border); border-radius: 11px; font-size: 15px; color: var(--color-text); outline: none; box-sizing: border-box; transition: border-color .2s; }
    .form-input:focus { border-color: var(--color-primary); }
    .form-input.error { border-color: var(--color-danger); }
    .form-error { font-size: 12px; color: var(--color-danger); font-weight: 600; margin-top: 3px; display: block; }
    .input-wrap { position: relative; }
    .input-wrap .form-input { padding-right: 44px; }
    .eye-btn { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; font-size: 18px; padding: 0; }
    .input-phone { display: flex; gap: 8px; align-items: center; }
    .phone-code { font-size: 15px; color: var(--color-text-muted); font-weight: 600; white-space: nowrap; }
    .input-phone .form-input { flex: 1; }
    .strength-bar { height: 4px; background: var(--color-border); border-radius: 4px; overflow: hidden; margin-top: 6px; }
    .strength-fill { height: 100%; border-radius: 4px; transition: width .3s, background .3s; }
    .strength-label { font-size: 11px; font-weight: 700; margin-top: 3px; display: block; }
    .alert { padding: 10px 14px; border-radius: 10px; font-size: 13px; font-weight: 600; margin-bottom: 14px; }
    .alert-error { background: var(--color-danger-light); color: var(--color-danger); }
    .btn-primary { background: var(--color-primary); color: #fff; border: none; border-radius: 14px; padding: 14px; font-family: var(--font-heading); font-size: 15px; font-weight: 800; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; transition: opacity .2s; }
    .btn-primary:disabled { opacity: .6; cursor: not-allowed; }
    .btn-full { width: 100%; }
    .spinner { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,.3); border-top-color: #fff; border-radius: 50%; animation: spin .6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .auth-terms { font-size: 12px; color: var(--color-text-muted); text-align: center; margin: 14px 0 0; }
    .auth-switch { text-align: center; font-size: 14px; color: var(--color-text-muted); margin: 10px 0 0; }
    .link { color: var(--color-primary); font-weight: 700; text-decoration: none; }
  `]
})
export class RegisterComponent {

  form:    FormGroup;
  loading  = signal(false);
  showPwd  = signal(false);
  errorMsg = signal('');

  constructor(
    private fb:           FormBuilder,
    private authService:  AuthService,
    private toastService: ToastService,
    private router:       Router
  ) {
    this.form = this.fb.group({
      name:     ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100),
                      Validators.pattern(/^[a-zA-Z\s''-]+$/)]],
      email:    ['', [Validators.required, Validators.email]],
      phone:    ['', [Validators.pattern(/^[6-9]\d{9}$/)]],
      password: ['', [Validators.required, strongPasswordValidator]]
    });
  }

  togglePwd(): void { this.showPwd.update(v => !v); }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');

    const val = this.form.value;
    this.authService.register({
      name:     val.name.trim(),
      email:    val.email.toLowerCase().trim(),
      phone:    val.phone || undefined,
      password: val.password
    }).subscribe({
      next: res => {
        this.loading.set(false);
        if (res.success) {
          this.toastService.success('Account created! Welcome to INVESTRAC 🎉');
          this.router.navigate(['/home']);
        } else {
          this.errorMsg.set(res.message || 'Registration failed.');
        }
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err.error?.message || 'Registration failed. Please try again.');
      }
    });
  }

  strengthPct(): number {
    const pwd = this.form.get('password')?.value as string || '';
    let score = 0;
    if (pwd.length >= 8)           score += 25;
    if (/[A-Z]/.test(pwd))         score += 25;
    if (/\d/.test(pwd))            score += 25;
    if (/[@#$%^&+=!*()_\-]/.test(pwd)) score += 25;
    return score;
  }

  strengthColor(): string {
    const p = this.strengthPct();
    if (p <= 25) return '#EF4444';
    if (p <= 50) return '#F59E0B';
    if (p <= 75) return '#3B82F6';
    return '#10B981';
  }

  strengthLabel(): string {
    const p = this.strengthPct();
    if (p <= 25) return 'Weak';
    if (p <= 50) return 'Fair';
    if (p <= 75) return 'Good';
    return 'Strong ✓';
  }

  showError(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && (ctrl.dirty || ctrl.touched));
  }

  getError(field: string): string {
    const ctrl = this.form.get(field);
    if (!ctrl?.errors) return '';
    const e = ctrl.errors;
    if (e['required'])      return `This field is required`;
    if (e['email'])         return 'Enter a valid email address';
    if (e['minlength'])     return `Minimum ${e['minlength'].requiredLength} characters`;
    if (e['maxlength'])     return `Maximum ${e['maxlength'].requiredLength} characters`;
    if (e['pattern'] && field === 'phone') return 'Enter 10-digit Indian mobile number';
    if (e['pattern'] && field === 'name')  return 'Name can only contain letters and spaces';
    if (e['weakPassword'])  return 'Must have uppercase, lowercase, number and special character (@#$%^&+=!*)';
    return 'Invalid value';
  }
}
