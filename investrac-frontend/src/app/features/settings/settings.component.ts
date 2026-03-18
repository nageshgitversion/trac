import { Component, OnInit, ChangeDetectionStrategy, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ToastComponent } from '../../shared/components/toast/toast.component';
import { ApiService } from '../../core/services/api.service';
import { UserProfile } from '../../core/models/user.model';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [NgClass, ToastComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <app-toast />
      <header class="page-header">
        <h1 class="page-title">Settings</h1>
      </header>

      <!-- Profile card -->
      <div class="profile-card card">
        <div class="profile-avatar">
          {{ initials() }}
        </div>
        <div class="profile-info">
          <div class="profile-name">{{ authService.userName() }}</div>
          <div class="profile-email">{{ profile()?.email ?? '' }}</div>
          @if (profile()?.kycVerified) {
            <span class="badge badge-success">KYC Verified ✓</span>
          } @else {
            <span class="badge badge-warning">KYC Pending</span>
          }
        </div>
      </div>

      <!-- Financial Profile -->
      <div class="section-header"><span class="section-title">Financial Profile</span></div>
      <div class="settings-group card">
        <div class="settings-row">
          <span class="sr-label">Risk Profile</span>
          <span class="sr-value">{{ profile()?.riskProfile ?? '—' }}</span>
        </div>
        <div class="divider"></div>
        <div class="settings-row">
          <span class="sr-label">Tax Regime</span>
          <span class="sr-value">{{ profile()?.taxRegime ?? '—' }} Regime</span>
        </div>
        <div class="divider"></div>
        <div class="settings-row">
          <span class="sr-label">Monthly Income</span>
          <span class="sr-value">
            {{ profile()?.monthlyIncome ? '₹' + (profile()!.monthlyIncome! / 1000).toFixed(0) + 'K' : '—' }}
          </span>
        </div>
      </div>

      <!-- App Preferences -->
      <div class="section-header"><span class="section-title">App Preferences</span></div>
      <div class="settings-group card">
        <div class="settings-row">
          <span class="sr-label">Theme</span>
          <div class="theme-toggle">
            <button class="tt-btn" [ngClass]="{'active': theme() === 'light'}" (click)="setTheme('light')">☀️ Light</button>
            <button class="tt-btn" [ngClass]="{'active': theme() === 'dark'}"  (click)="setTheme('dark')">🌙 Dark</button>
          </div>
        </div>
        <div class="divider"></div>
        <div class="settings-row">
          <div>
            <span class="sr-label">Show amounts in Lakhs</span>
            <div class="sr-sub">₹1.15L instead of ₹1,15,000</div>
          </div>
          <button class="toggle" [ngClass]="{'on': showLakhs()}" (click)="toggleLakhs()">
            <span class="toggle-thumb"></span>
          </button>
        </div>
        <div class="divider"></div>
        <div class="settings-row">
          <div>
            <span class="sr-label">AI Insights</span>
            <div class="sr-sub">Nightly financial analysis</div>
          </div>
          <button class="toggle" [ngClass]="{'on': aiInsights()}" (click)="toggleAiInsights()">
            <span class="toggle-thumb"></span>
          </button>
        </div>
      </div>

      <!-- Security -->
      <div class="section-header"><span class="section-title">Security & Account</span></div>
      <div class="settings-group card">
        <button class="settings-row settings-btn">
          <span class="sr-label">Change Password</span>
          <span class="sr-chevron">›</span>
        </button>
        <div class="divider"></div>
        <button class="settings-row settings-btn">
          <span class="sr-label">Update KYC</span>
          @if (!profile()?.kycVerified) { <span class="badge badge-warning">Required</span> }
          <span class="sr-chevron">›</span>
        </button>
        <div class="divider"></div>
        <button class="settings-row settings-btn">
          <span class="sr-label">Notification Preferences</span>
          <span class="sr-chevron">›</span>
        </button>
      </div>

      <!-- App info -->
      <div class="app-info">
        <div>INVESTRAC v1.0.0</div>
        <div>Made with ❤️ in India</div>
      </div>

      <!-- Logout -->
      <button class="btn btn-danger btn-full" style="margin-bottom:24px" (click)="logout()">
        Sign Out
      </button>
    </div>
  `,
  styles: [`
    .profile-card { display: flex; align-items: center; gap: 14px; padding: 16px; margin-bottom: 6px; }
    .profile-avatar { width: 56px; height: 56px; background: var(--color-primary); color: #fff; border-radius: 18px; display: flex; align-items: center; justify-content: center; font-family: var(--font-heading); font-size: 20px; font-weight: 900; flex-shrink: 0; }
    .profile-name { font-family: var(--font-heading); font-size: 18px; font-weight: 900; }
    .profile-email { font-size: 13px; color: var(--color-text-muted); margin: 2px 0 6px; }
    .settings-group { padding: 0; overflow: hidden; }
    .settings-row { display: flex; align-items: center; justify-content: space-between; padding: 14px 16px; width: 100%; background: none; border: none; cursor: default; }
    .settings-btn { cursor: pointer; }
    .settings-btn:hover { background: var(--color-card-alt); }
    .sr-label { font-size: 14px; font-weight: 600; }
    .sr-sub   { font-size: 12px; color: var(--color-text-muted); margin-top: 2px; }
    .sr-value { font-size: 14px; color: var(--color-text-muted); }
    .sr-chevron { font-size: 20px; color: var(--color-text-muted); }
    .theme-toggle { display: flex; gap: 6px; }
    .tt-btn { padding: 6px 12px; border-radius: 8px; border: 1.5px solid var(--color-border); background: none; font-size: 12px; font-weight: 700; cursor: pointer; }
    .tt-btn.active { background: var(--color-primary); border-color: var(--color-primary); color: #fff; }
    .toggle { width: 44px; height: 24px; background: var(--color-border); border: none; border-radius: 99px; position: relative; cursor: pointer; transition: background .2s; padding: 0; }
    .toggle.on { background: var(--color-primary); }
    .toggle-thumb { position: absolute; left: 2px; top: 2px; width: 20px; height: 20px; background: #fff; border-radius: 50%; transition: transform .2s; box-shadow: 0 1px 4px rgba(0,0,0,0.2); }
    .toggle.on .toggle-thumb { transform: translateX(20px); }
    .app-info { text-align: center; font-size: 12px; color: var(--color-text-muted); padding: 16px 0 12px; }
  `]
})
export class SettingsComponent implements OnInit {

  profile    = signal<UserProfile | null>(null);
  theme      = signal<string>('light');
  showLakhs  = signal<boolean>(true);
  aiInsights = signal<boolean>(true);

  constructor(
    readonly authService:  AuthService,
    private toastService:  ToastService,
    private apiService:    ApiService
  ) {}

  ngOnInit(): void {
    this.apiService.get<UserProfile>('/users/me').subscribe({
      next: res => { if (res.success && res.data) this.profile.set(res.data); }
    });
  }

  toggleLakhs(): void      { this.showLakhs.update(v => !v); }
  toggleAiInsights(): void { this.aiInsights.update(v => !v); }

  initials(): string {
    return (this.authService.userName() || 'U')
      .split(' ').map(n => n[0]).join('').slice(0,2).toUpperCase();
  }

  setTheme(t: string): void {
    this.theme.set(t);
    document.documentElement.setAttribute('data-theme', t);
  }

  logout(): void {
    if (confirm('Are you sure you want to sign out?')) {
      this.authService.logout();
    }
  }
}
