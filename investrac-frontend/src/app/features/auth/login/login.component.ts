import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgClass],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="auth-screen">
      <!-- Brand -->
      <div class="auth-brand">
        <div class="brand-logo">
          <span class="brand-icon">₹</span>
        </div>
        <h1 class="brand-name">INVESTRAC</h1>
        <p class="brand-tagline">Your intelligent investment tracker</p>
      </div>

      <!-- Form -->
      <form class="auth-card" [formGroup]="form" (ngSubmit)="submit()">
        <h2 class="auth-title">Welcome back</h2>
        <p class="auth-subtitle">Sign in to your account</p>

        <div class="form-group">
          <label class="form-label">Email address</label>
          <input
            class="form-input"
            [ngClass]="{ 'error': showError('email') }"
            type="email"
            formControlName="email"
            placeholder="arjun@investrac.in"
            autocomplete="email"
            inputmode="email">
          @if (showError('email')) {
            <span class="form-error">{{ getError('email') }}</span>
          }
        </div>

        <div class="form-group">
          <label class="form-label">Password</label>
          <div class="input-wrap">
            <input
              class="form-input"
              [ngClass]="{ 'error': showError('password') }"
              [type]="showPassword() ? 'text' : 'password'"
              formControlName="password"
              placeholder="Enter your password"
              autocomplete="current-password">
            <button type="button" class="eye-btn" (click)="togglePassword()">
              {{ showPassword() ? '🙈' : '👁️' }}
            </button>
          </div>
          @if (showError('password')) {
            <span class="form-error">{{ getError('password') }}</span>
          }
        </div>

        <a routerLink="/auth/forgot-password" class="forgot-link">Forgot password?</a>

        @if (errorMsg()) {
          <div class="alert alert-error">⚠️ {{ errorMsg() }}</div>
        }

        <button
          class="btn btn-primary btn-full"
          type="submit"
          [disabled]="loading()">
          @if (loading()) {
            <span class="spinner"></span> Signing in...
          } @else {
            Sign In
          }
        </button>

        <p class="auth-switch">
          Don't have an account?
          <a routerLink="/auth/register" class="link">Create account</a>
        </p>
      </form>
    </div>
  `,
  styles: [`
    .auth-screen {
      min-height: 100dvh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24px 20px;
      background: var(--color-bg);
    }
    .auth-brand {
      text-align: center;
      margin-bottom: 32px;
    }
    .brand-logo {
      width: 64px; height: 64px;
      background: linear-gradient(135deg, #4F46E5, #7C3AED);
      border-radius: 20px;
      display: flex; align-items: center; justify-content: center;
      margin: 0 auto 12px;
      box-shadow: 0 8px 24px rgba(79,70,229,0.3);
    }
    .brand-icon { font-size: 28px; color: #fff; font-weight: 900; }
    .brand-name { font-family: var(--font-heading); font-size: 26px; font-weight: 900; color: var(--color-primary); margin: 0 0 4px; }
    .brand-tagline { font-size: 14px; color: var(--color-text-muted); margin: 0; }
    .auth-card { width: 100%; max-width: 390px; background: var(--color-card); border: 1.5px solid var(--color-border); border-radius: 24px; padding: 28px 24px; box-shadow: var(--shadow-card); }
    .auth-title { font-family: var(--font-heading); font-size: 22px; font-weight: 900; margin: 0 0 4px; }
    .auth-subtitle { font-size: 14px; color: var(--color-text-muted); margin: 0 0 24px; }
    .form-group { margin-bottom: 16px; }
    .form-label { display: block; font-size: 13px; font-weight: 600; color: var(--color-text); margin-bottom: 6px; }
    .form-input { width: 100%; padding: 12px 14px; background: var(--color-bg); border: 1.5px solid var(--color-border); border-radius: 12px; font-size: 15px; color: var(--color-text); outline: none; box-sizing: border-box; transition: border-color .2s; }
    .form-input:focus { border-color: var(--color-primary); }
    .form-input.error { border-color: var(--color-danger); }
    .form-error { font-size: 12px; color: var(--color-danger); font-weight: 600; margin-top: 4px; display: block; }
    .input-wrap { position: relative; }
    .input-wrap .form-input { padding-right: 44px; }
    .eye-btn { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; font-size: 18px; padding: 0; }
    .forgot-link { display: block; text-align: right; font-size: 13px; color: var(--color-primary); font-weight: 600; text-decoration: none; margin: -4px 0 20px; }
    .alert { padding: 10px 14px; border-radius: 10px; font-size: 13px; font-weight: 600; margin-bottom: 16px; }
    .alert-error { background: var(--color-danger-light); color: var(--color-danger); }
    .btn-primary { background: var(--color-primary); color: #fff; border: none; border-radius: 14px; padding: 14px; font-family: var(--font-heading); font-size: 15px; font-weight: 800; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; transition: opacity .2s; }
    .btn-primary:disabled { opacity: .6; cursor: not-allowed; }
    .btn-full { width: 100%; }
    .spinner { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,.3); border-top-color: #fff; border-radius: 50%; animation: spin .6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .auth-switch { text-align: center; font-size: 14px; color: var(--color-text-muted); margin: 20px 0 0; }
    .link { color: var(--color-primary); font-weight: 700; text-decoration: none; }
  `]
})
export class LoginComponent {

  form: FormGroup;
  loading       = signal(false);
  showPassword_ = signal(false);
  errorMsg      = signal('');

  constructor(
    private fb:          FormBuilder,
    private authService: AuthService,
    private toastService:ToastService,
    private router:      Router
  ) {
    this.form = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  showPassword = this.showPassword_;

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');

    this.authService.login(this.form.value).subscribe({
      next: res => {
        this.loading.set(false);
        if (res.success) {
          this.toastService.success(`Welcome back, ${res.data?.user.name}!`);
          this.router.navigate(['/home']);
        } else {
          this.errorMsg.set(res.message || 'Login failed. Please try again.');
        }
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(
          err.error?.message || 'Network error. Please check your connection.'
        );
      }
    });
  }

  togglePassword(): void {
    this.showPassword_.update(v => !v);
  }

  showError(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && (ctrl.dirty || ctrl.touched));
  }

  getError(field: string): string {
    const ctrl = this.form.get(field);
    if (!ctrl?.errors) return '';
    if (ctrl.errors['required']) return `${field.charAt(0).toUpperCase() + field.slice(1)} is required`;
    if (ctrl.errors['email'])    return 'Enter a valid email address';
    if (ctrl.errors['minlength']) return 'Password must be at least 6 characters';
    return 'Invalid value';
  }
}
