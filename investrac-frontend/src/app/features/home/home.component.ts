import { Component, OnInit, ChangeDetectionStrategy, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { WalletService } from '../../core/services/wallet.service';
import { TransactionService } from '../../core/services/transaction.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastComponent } from '../../shared/components/toast/toast.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { InrFormatPipe } from '../../shared/pipes/inr-format.pipe';
import { RelativeDatePipe } from '../../shared/pipes/relative-date.pipe';
import { Transaction } from '../../core/models/transaction.model';
import { Wallet } from '../../core/models/wallet.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, NgClass, ToastComponent, LoadingSpinnerComponent, InrFormatPipe, RelativeDatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <app-toast />

      <!-- Header -->
      <header class="page-header">
        <div>
          <p class="greeting">{{ greeting() }}</p>
          <h1 class="page-title">{{ authService.userName() }}</h1>
        </div>
        <a routerLink="/settings" class="btn btn-icon" aria-label="Settings">⚙️</a>
      </header>

      @if (loading()) {
        <app-loading-spinner message="Loading your dashboard..." />
      } @else {

        <!-- Wallet Hero Card -->
        @if (wallet()) {
          <div class="hero-card card" [ngClass]="heroClass()">
            <div class="hero-top">
              <div>
                <p class="hero-label">Wallet Balance</p>
                <h2 class="hero-amount">{{ wallet()!.balance | inr:true }}</h2>
              </div>
              <span class="hero-month">{{ wallet()!.month }}</span>
            </div>

            <!-- Progress bar -->
            <div class="progress-bar" style="margin: 14px 0 8px">
              <div class="progress-fill"
                   [style.width]="wallet()!.usedPercent + '%'"
                   [style.background]="progressColor()">
              </div>
            </div>
            <div class="hero-stats">
              <span>{{ wallet()!.usedPercent }}% used</span>
              <span class="text-muted">Free: {{ wallet()!.freeToSpend | inr:true }}</span>
            </div>

            <!-- Envelope quick view -->
            @if (wallet()!.envelopes.length) {
              <div class="envelopes">
                @for (env of wallet()!.envelopes.slice(0,4); track env.id) {
                  <div class="env-chip" [ngClass]="{'env-over': env.overBudget}">
                    <span class="env-icon">{{ env.icon }}</span>
                    <div>
                      <div class="env-name">{{ env.categoryName }}</div>
                      <div class="env-bar">
                        <div class="env-fill" [style.width]="env.usedPercent + '%'"
                             [style.background]="env.overBudget ? 'var(--color-danger)' : 'var(--color-primary)'"></div>
                      </div>
                    </div>
                    <span class="env-amount">{{ env.remaining | inr:true }}</span>
                  </div>
                }
              </div>
            }

            <div class="hero-actions">
              <a routerLink="/wallet" class="btn btn-soft btn-sm">View Wallet</a>
              <a routerLink="/transactions" class="btn btn-soft btn-sm">Add Expense</a>
            </div>
          </div>
        } @else {
          <div class="card setup-card">
            <div class="setup-icon">👛</div>
            <h3 class="fw-900">Setup Your Wallet</h3>
            <p class="text-muted" style="font-size:14px;margin:6px 0 14px">
              Add your monthly income to start tracking expenses
            </p>
            <a routerLink="/wallet" class="btn btn-primary btn-sm">Set Up Wallet →</a>
          </div>
        }

        <!-- Quick Stats -->
        <div class="section-header">
          <span class="section-title">This Month</span>
          <a routerLink="/transactions" class="section-action">See all</a>
        </div>

        <div class="stat-row">
          <div class="stat-card">
            <div class="stat-label">Income</div>
            <div class="stat-value text-success">{{ summary.income | inr:true }}</div>
            <div class="stat-sub">{{ summary.txCount.income }} transactions</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Expenses</div>
            <div class="stat-value text-danger">{{ summary.expense | inr:true }}</div>
            <div class="stat-sub">Savings: {{ summary.savingsRate }}%</div>
          </div>
        </div>
        <div class="stat-row">
          <div class="stat-card">
            <div class="stat-label">Investments</div>
            <div class="stat-value" style="color:var(--color-info)">{{ summary.investment | inr:true }}</div>
            <div class="stat-sub">This month</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Net Savings</div>
            <div class="stat-value" [ngClass]="summary.netSavings >= 0 ? 'text-success' : 'text-danger'">
              {{ summary.netSavings | inr:true }}
            </div>
            <div class="stat-sub">Income – Expense</div>
          </div>
        </div>

        <!-- Recent Transactions -->
        <div class="section-header">
          <span class="section-title">Recent</span>
          <a routerLink="/transactions" class="section-action">All transactions</a>
        </div>

        @if (recentTx().length === 0) {
          <div class="empty-state" style="padding:24px 0">
            <div class="empty-icon">💳</div>
            <div class="empty-title">No transactions yet</div>
            <div class="empty-body">Add your first expense to get started</div>
          </div>
        } @else {
          <div class="tx-list">
            @for (tx of recentTx(); track tx.id) {
              <div class="tx-item card card-sm">
                <div class="tx-icon">{{ getCategoryIcon(tx.category) }}</div>
                <div class="tx-info">
                  <div class="tx-name">{{ tx.name }}</div>
                  <div class="tx-meta">
                    <span class="badge" [ngClass]="getBadgeClass(tx.type)">{{ tx.type }}</span>
                    <span class="tx-date">{{ tx.txDate | relativeDate }}</span>
                  </div>
                </div>
                <div class="tx-amount" [ngClass]="getAmountClass(tx.type)">
                  {{ tx.type === 'INCOME' ? '+' : '-' }}{{ tx.amount | inr }}
                </div>
              </div>
            }
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .greeting { font-size: 13px; color: var(--color-text-muted); font-weight: 600; }
    .hero-card { padding: 18px; margin-bottom: 6px; }
    .hero-card.hero-ok    { border-color: var(--color-success); }
    .hero-card.hero-warn  { border-color: var(--color-warning); }
    .hero-card.hero-over  { border-color: var(--color-danger);  }
    .hero-top { display: flex; justify-content: space-between; align-items: flex-start; }
    .hero-label { font-size: 12px; color: var(--color-text-muted); font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
    .hero-amount { font-family: var(--font-heading); font-size: 30px; font-weight: 900; margin-top: 2px; }
    .hero-month { font-size: 12px; background: var(--color-primary-light); color: var(--color-primary); padding: 4px 10px; border-radius: 99px; font-weight: 700; }
    .hero-stats { display: flex; justify-content: space-between; font-size: 12px; font-weight: 600; color: var(--color-text-muted); }
    .envelopes { display: flex; flex-direction: column; gap: 8px; margin: 12px 0; }
    .env-chip { display: flex; align-items: center; gap: 10px; padding: 8px; background: var(--color-card-alt); border-radius: 10px; }
    .env-chip.env-over { background: var(--color-danger-light); }
    .env-icon { font-size: 18px; }
    .env-name { font-size: 12px; font-weight: 600; }
    .env-bar  { height: 3px; background: var(--color-border); border-radius: 3px; overflow: hidden; margin-top: 3px; }
    .env-fill { height: 100%; border-radius: 3px; transition: width .3s; }
    .env-amount { font-size: 13px; font-weight: 800; font-family: var(--font-heading); margin-left: auto; }
    .hero-actions { display: flex; gap: 8px; margin-top: 14px; }
    .setup-card { text-align: center; padding: 28px 20px; }
    .setup-icon { font-size: 40px; margin-bottom: 10px; }
    .tx-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
    .tx-item { display: flex; align-items: center; gap: 12px; }
    .tx-icon { width: 40px; height: 40px; background: var(--color-card-alt); border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
    .tx-info { flex: 1; min-width: 0; }
    .tx-name { font-weight: 600; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .tx-meta { display: flex; align-items: center; gap: 8px; margin-top: 3px; }
    .tx-date { font-size: 11px; color: var(--color-text-muted); }
    .tx-amount { font-family: var(--font-heading); font-size: 15px; font-weight: 900; white-space: nowrap; }
  `]
})
export class HomeComponent implements OnInit {

  loading  = signal(true);
  wallet   = signal<Wallet | null>(null);
  recentTx = signal<Transaction[]>([]);

  summary = { income: 0, expense: 0, investment: 0, netSavings: 0, savingsRate: 0,
               txCount: { income: 0 } };

  constructor(
    readonly authService:      AuthService,
    private walletService:     WalletService,
    private transactionService:TransactionService,
    private notificationService:NotificationService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
    this.notificationService.getUnreadCount().subscribe();
  }

  private loadDashboard(): void {
    this.loading.set(true);

    // Load wallet
    this.walletService.getCurrentWallet().subscribe({
      next: res => {
        if (res.success && res.data) this.wallet.set(res.data);
      },
      error: () => {} // No wallet yet — show setup card
    });

    // Load recent transactions
    this.transactionService.getRecent(8).subscribe({
      next: res => {
        if (res.success && res.data) this.recentTx.set(res.data);
      }
    });

    // Load monthly summary
    this.transactionService.getMonthlySummary().subscribe({
      next: res => {
        if (res.success && res.data) {
          const d = res.data;
          this.summary = {
            income: d.totalIncome, expense: d.totalExpense,
            investment: d.totalInvestment, netSavings: d.netSavings,
            savingsRate: d.savingsRatePercent,
            txCount: { income: d.expenseBreakdown?.length ?? 0 }
          };
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  greeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning 🌅';
    if (h < 17) return 'Good afternoon ☀️';
    if (h < 20) return 'Good evening 🌆';
    return 'Good night 🌙';
  }

  heroClass(): string {
    const pct = this.wallet()?.usedPercent ?? 0;
    if (pct >= 90) return 'hero-over';
    if (pct >= 70) return 'hero-warn';
    return 'hero-ok';
  }

  progressColor(): string {
    const pct = this.wallet()?.usedPercent ?? 0;
    if (pct >= 90) return 'var(--color-danger)';
    if (pct >= 70) return 'var(--color-warning)';
    return 'var(--color-success)';
  }

  getCategoryIcon(cat: string): string {
    const icons: Record<string, string> = {
      'Food & Dining': '🍕', 'Groceries': '🛒', 'Transport': '🚗',
      'Shopping': '🛍️', 'Healthcare': '💊', 'Entertainment': '🎬',
      'Income': '💰', 'Salary': '💰', 'Investment': '📈',
      'EMI': '🏠', 'SIP': '📊', 'Others': '📦'
    };
    return icons[cat] ?? '💳';
  }

  getBadgeClass(type: string): string {
    return { INCOME: 'badge-success', EXPENSE: 'badge-danger',
             INVESTMENT: 'badge-info', SAVINGS: 'badge-warning' }[type] ?? 'badge-neutral';
  }

  getAmountClass(type: string): string {
    return type === 'INCOME' ? 'amount-income' : 'amount-expense';
  }
}
