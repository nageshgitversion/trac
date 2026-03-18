import { Component, OnInit, ChangeDetectionStrategy, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { WalletService } from '../../core/services/wallet.service';
import { ToastService } from '../../core/services/toast.service';
import { ToastComponent } from '../../shared/components/toast/toast.component';
import { InrFormatPipe } from '../../shared/pipes/inr-format.pipe';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { Wallet, WalletEnvelope } from '../../core/models/wallet.model';

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, NgClass, ToastComponent, InrFormatPipe, LoadingSpinnerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <app-toast />
      <header class="page-header">
        <h1 class="page-title">Wallet</h1>
        @if (wallet()) {
          <button class="btn btn-soft btn-sm" (click)="showTopUp.set(!showTopUp())">
            {{ showTopUp() ? '✕' : '+ Top Up' }}
          </button>
        }
      </header>

      @if (loading()) {
        <app-loading-spinner message="Loading wallet..." />
      } @else if (!wallet()) {
        <!-- Setup wallet -->
        <div class="setup-hero card">
          <div class="setup-emoji">👛</div>
          <h2 class="setup-title">Set Up Your Monthly Wallet</h2>
          <p class="setup-body">Add your income and we'll help you budget smartly</p>

          <form [formGroup]="setupForm" (ngSubmit)="createWallet()">
            <div class="form-group">
              <label class="form-label">Monthly Income (₹)</label>
              <input class="form-input" type="number" inputmode="decimal"
                formControlName="income" placeholder="115000" min="1" />
            </div>

            <div class="section-header" style="margin-top:16px">
              <span class="section-title" style="font-size:14px">Envelope Budgets (optional)</span>
            </div>
            <div class="envelope-setup">
              @for (env of defaultEnvelopes; track env.key) {
                <div class="env-input">
                  <span class="env-icon">{{ env.icon }}</span>
                  <label class="env-label">{{ env.name }}</label>
                  <input class="form-input env-amt" type="number" inputmode="decimal"
                    [placeholder]="env.suggested.toString()"
                    (input)="setEnvelopeBudget(env.key, $event)" />
                </div>
              }
            </div>

            <button class="btn btn-primary btn-full" style="margin-top:20px"
              type="submit" [disabled]="setupForm.invalid || creating()">
              @if (creating()) { <span class="spinner"></span> } Activate Wallet
            </button>
          </form>
        </div>
      } @else {
        <!-- Top-up sheet -->
        @if (showTopUp()) {
          <div class="topup-card card" style="margin-bottom:12px">
            <h3 style="font-family:var(--font-heading);font-weight:900;margin-bottom:12px">Top Up Wallet</h3>
            <div class="form-group">
              <label class="form-label">Amount (₹)</label>
              <input class="form-input" type="number" inputmode="decimal"
                [(ngModel)]="topUpAmount" placeholder="5000" />
            </div>
            <button class="btn btn-success btn-full" (click)="doTopUp()" [disabled]="!topUpAmount || topping()">
              @if (topping()) { <span class="spinner"></span> } Add ₹{{ topUpAmount | inr }}
            </button>
          </div>
        }

        <!-- Balance Card -->
        <div class="wallet-hero card">
          <div class="wh-row">
            <div>
              <div class="wh-label">Available Balance</div>
              <div class="wh-amount">{{ wallet()!.balance | inr:true }}</div>
            </div>
            <div class="wh-month">{{ wallet()!.month }}</div>
          </div>

          <div class="progress-bar" style="margin: 14px 0 6px">
            <div class="progress-fill"
                 [style.width]="wallet()!.usedPercent + '%'"
                 [style.background]="usedColor()">
            </div>
          </div>
          <div class="wh-stats">
            <span>{{ wallet()!.usedPercent }}% used</span>
            <span>Free to spend: {{ wallet()!.freeToSpend | inr:true }}</span>
          </div>
        </div>

        <!-- Summary Row -->
        <div class="stat-row" style="margin-top:10px">
          <div class="stat-card">
            <div class="stat-label">Income</div>
            <div class="stat-value text-success">{{ wallet()!.income | inr:true }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Committed</div>
            <div class="stat-value text-danger">{{ wallet()!.committed | inr:true }}</div>
            <div class="stat-sub">EMI + SIP</div>
          </div>
        </div>

        <!-- Envelopes -->
        @if (wallet()!.envelopes.length) {
          <div class="section-header">
            <span class="section-title">Envelopes</span>
          </div>

          <div class="envelopes-grid">
            @for (env of wallet()!.envelopes; track env.id) {
              <div class="env-card card" [ngClass]="{'env-over': env.overBudget}">
                <div class="env-top">
                  <span class="env-ico">{{ env.icon }}</span>
                  <div class="env-info">
                    <div class="env-name">{{ env.categoryName }}</div>
                    <div class="env-budget">Budget: {{ env.budget | inr }}</div>
                  </div>
                  @if (env.overBudget) {
                    <span class="badge badge-danger">Over</span>
                  }
                </div>
                <div class="progress-bar" style="margin: 8px 0 4px">
                  <div class="progress-fill"
                       [style.width]="env.usedPercent + '%'"
                       [style.background]="env.overBudget ? 'var(--color-danger)' : 'var(--color-primary)'">
                  </div>
                </div>
                <div class="env-row">
                  <span class="text-muted" style="font-size:12px">Spent: {{ env.spent | inr }}</span>
                  <span [ngClass]="env.overBudget ? 'text-danger' : 'text-success'"
                        style="font-size:13px;font-weight:800">
                    {{ env.remaining | inr }}
                  </span>
                </div>
              </div>
            }
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .setup-hero { text-align: center; padding: 28px 20px; }
    .setup-emoji { font-size: 48px; margin-bottom: 12px; }
    .setup-title { font-family: var(--font-heading); font-size: 20px; font-weight: 900; margin-bottom: 6px; }
    .setup-body  { font-size: 14px; color: var(--color-text-muted); margin-bottom: 24px; }
    .envelope-setup { display: flex; flex-direction: column; gap: 8px; }
    .env-input { display: flex; align-items: center; gap: 10px; }
    .env-icon  { font-size: 20px; width: 24px; text-align: center; }
    .env-label { font-size: 13px; font-weight: 600; flex: 1; }
    .env-amt   { width: 100px; padding: 8px 10px; font-size: 14px; }
    .wallet-hero { padding: 20px; }
    .wh-row { display: flex; justify-content: space-between; align-items: flex-start; }
    .wh-label { font-size: 12px; color: var(--color-text-muted); font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
    .wh-amount { font-family: var(--font-heading); font-size: 32px; font-weight: 900; }
    .wh-month { background: var(--color-primary-light); color: var(--color-primary); padding: 4px 10px; border-radius: 99px; font-size: 12px; font-weight: 700; }
    .wh-stats { display: flex; justify-content: space-between; font-size: 12px; color: var(--color-text-muted); font-weight: 600; }
    .envelopes-grid { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
    .env-card { padding: 14px; }
    .env-card.env-over { border-color: var(--color-danger); background: var(--color-danger-light); }
    .env-top  { display: flex; align-items: center; gap: 10px; }
    .env-ico  { font-size: 22px; }
    .env-info { flex: 1; }
    .env-name { font-weight: 700; font-size: 14px; }
    .env-budget { font-size: 12px; color: var(--color-text-muted); }
    .env-row { display: flex; justify-content: space-between; align-items: center; }
  `]
})
export class WalletComponent implements OnInit {

  wallet    = this.walletService.wallet;
  loading   = signal(false);
  creating  = signal(false);
  showTopUp = signal(false);
  topping   = signal(false);
  topUpAmount: number | null = null;

  setupForm: FormGroup;
  envelopeBudgets: Record<string, number> = {};

  defaultEnvelopes = [
    { key:'food',      name:'Food & Dining', icon:'🍕', suggested: 8000 },
    { key:'groceries', name:'Groceries',     icon:'🛒', suggested: 6000 },
    { key:'transport', name:'Transport',     icon:'🚗', suggested: 3000 },
    { key:'shopping',  name:'Shopping',      icon:'🛍️', suggested: 4000 },
    { key:'ent',       name:'Entertainment', icon:'🎬', suggested: 2000 },
    { key:'health',    name:'Healthcare',    icon:'💊', suggested: 1500 },
  ];

  constructor(
    private walletService: WalletService,
    private toastService:  ToastService,
    private fb:            FormBuilder
  ) {
    this.setupForm = this.fb.group({
      income: [null, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.loading.set(true);
    this.walletService.getCurrentWallet().subscribe({
      next: () => this.loading.set(false),
      error: () => this.loading.set(false)
    });
  }

  setEnvelopeBudget(key: string, event: Event): void {
    const val = parseFloat((event.target as HTMLInputElement).value);
    if (val > 0) this.envelopeBudgets[key] = val;
    else delete this.envelopeBudgets[key];
  }

  createWallet(): void {
    if (this.setupForm.invalid) return;
    this.creating.set(true);
    const month = new Date().toISOString().slice(0, 7);

    this.walletService.createWallet({
      month,
      income: this.setupForm.value.income,
      envelopes: Object.keys(this.envelopeBudgets).length ? this.envelopeBudgets : undefined
    }).subscribe({
      next: res => {
        this.creating.set(false);
        if (res.success) this.toastService.success('Wallet activated for ' + month + ' 🎉');
        else this.toastService.error(res.message || 'Failed to create wallet');
      },
      error: err => {
        this.creating.set(false);
        this.toastService.error(err.error?.message || 'Network error');
      }
    });
  }

  doTopUp(): void {
    if (!this.topUpAmount || this.topUpAmount <= 0) return;
    this.topping.set(true);
    this.walletService.topUp(this.topUpAmount).subscribe({
      next: res => {
        this.topping.set(false);
        if (res.success) {
          this.showTopUp.set(false);
          this.topUpAmount = null;
          this.toastService.success('Wallet topped up ✅');
        }
      },
      error: () => {
        this.topping.set(false);
        this.toastService.error('Top-up failed');
      }
    });
  }

  usedColor(): string {
    const p = this.wallet()?.usedPercent ?? 0;
    if (p >= 90) return 'var(--color-danger)';
    if (p >= 70) return 'var(--color-warning)';
    return 'var(--color-success)';
  }
}
