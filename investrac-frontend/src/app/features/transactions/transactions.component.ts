import { Component, OnInit, ChangeDetectionStrategy, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { TransactionService } from '../../core/services/transaction.service';
import { WalletService } from '../../core/services/wallet.service';
import { ToastService } from '../../core/services/toast.service';
import { ToastComponent } from '../../shared/components/toast/toast.component';
import { InrFormatPipe } from '../../shared/pipes/inr-format.pipe';
import { RelativeDatePipe } from '../../shared/pipes/relative-date.pipe';
import { Transaction, TransactionType, MonthSummary } from '../../core/models/transaction.model';
import { PagedResponse } from '../../core/models/api-response.model';

const CATEGORIES: Record<TransactionType, string[]> = {
  EXPENSE: ['Food & Dining','Groceries','Transport','Shopping','Healthcare','Entertainment','Utilities','Others'],
  INCOME:  ['Salary','Freelance','Business','Investment Returns','Others'],
  INVESTMENT: ['Mutual Fund','Stocks','FD/RD','NPS','PPF','Gold','Others'],
  SAVINGS: ['Emergency Fund','Goal Savings','Others'],
  TRANSFER:['Transfer']
};

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [ReactiveFormsModule, NgClass, ToastComponent, InrFormatPipe, RelativeDatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <app-toast />

      <header class="page-header">
        <h1 class="page-title">Transactions</h1>
        <button class="btn btn-primary btn-sm" (click)="openSheet.set(true)">+ Add</button>
      </header>

      <!-- Filter bar -->
      <div class="filter-bar">
        @for (t of types; track t) {
          <button class="filter-chip" [ngClass]="{'active': activeType() === t}"
            (click)="setType(t)">{{ t }}</button>
        }
      </div>

      <!-- Month summary banner -->
      @if (summary()) {
        <div class="summary-banner card card-sm">
          <div class="sb-item">
            <div class="sb-label">Income</div>
            <div class="sb-val text-success">{{ summary()!.totalIncome | inr:true }}</div>
          </div>
          <div class="sb-divider"></div>
          <div class="sb-item">
            <div class="sb-label">Expense</div>
            <div class="sb-val text-danger">{{ summary()!.totalExpense | inr:true }}</div>
          </div>
          <div class="sb-divider"></div>
          <div class="sb-item">
            <div class="sb-label">Saved</div>
            <div class="sb-val" [ngClass]="summary()!.netSavings >= 0 ? 'text-success' : 'text-danger'">
              {{ summary()!.savingsRatePercent }}%
            </div>
          </div>
        </div>
      }

      <!-- Transaction list -->
      @if (loading()) {
        <div class="skeleton" style="height:60px;margin:8px 0"></div>
        <div class="skeleton" style="height:60px;margin:8px 0"></div>
        <div class="skeleton" style="height:60px;margin:8px 0"></div>
      } @else if (transactions().length === 0) {
        <div class="empty-state">
          <div class="empty-icon">💳</div>
          <div class="empty-title">No transactions</div>
          <div class="empty-body">Tap + Add to record your first transaction</div>
        </div>
      } @else {
        <div class="tx-list">
          @for (tx of transactions(); track tx.id) {
            <div class="tx-row card card-sm">
              <div class="tx-icon-wrap">{{ getCatIcon(tx.category) }}</div>
              <div class="tx-info">
                <div class="tx-name">{{ tx.name }}</div>
                <div class="tx-sub">
                  <span class="tx-cat">{{ tx.category }}</span>
                  <span class="tx-date">{{ tx.txDate | relativeDate }}</span>
                  @if (tx.status === 'FAILED') {
                    <span class="badge badge-danger">Failed</span>
                  }
                  @if (tx.status === 'PENDING') {
                    <span class="badge badge-warning">Pending</span>
                  }
                </div>
              </div>
              <div class="tx-amount-col">
                <span [ngClass]="tx.type === 'INCOME' ? 'amount-income' : 'amount-expense'">
                  {{ tx.type === 'INCOME' ? '+' : '-' }}{{ tx.amount | inr }}
                </span>
                <button class="del-btn" (click)="deleteTransaction(tx.id)" aria-label="Delete">🗑️</button>
              </div>
            </div>
          }
        </div>

        <!-- Load more -->
        @if (!page().last) {
          <button class="btn btn-ghost btn-full" (click)="loadMore()">Load more</button>
        }
      }
    </div>

    <!-- Add Transaction Sheet -->
    @if (openSheet()) {
      <div class="sheet-backdrop" (click)="closeSheet()">
        <div class="sheet" (click)="$event.stopPropagation()">
          <div class="sheet-handle"></div>
          <h3 class="sheet-title">Add Transaction</h3>

          <form [formGroup]="form" (ngSubmit)="submit()">
            <!-- Type selector -->
            <div class="type-tabs">
              @for (t of ['EXPENSE','INCOME','INVESTMENT','SAVINGS']; track t) {
                <button type="button" class="type-tab"
                  [ngClass]="{'active': form.get('type')!.value === t}"
                  (click)="setFormType(t)">{{ t }}</button>
              }
            </div>

            <div class="form-group">
              <label class="form-label">Description</label>
              <input class="form-input" formControlName="name" placeholder="e.g. Swiggy order, SBI FD..." />
            </div>

            <div class="form-group">
              <label class="form-label">Amount (₹)</label>
              <input class="form-input" formControlName="amount" type="number"
                inputmode="decimal" placeholder="0.00" />
            </div>

            <div class="sheet-row">
              <div class="form-group" style="flex:1">
                <label class="form-label">Category</label>
                <select class="form-input" formControlName="category">
                  @for (cat of currentCategories(); track cat) {
                    <option [value]="cat">{{ cat }}</option>
                  }
                </select>
              </div>
              <div class="form-group" style="flex:1">
                <label class="form-label">Date</label>
                <input class="form-input" type="date" formControlName="txDate" />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Note (optional)</label>
              <input class="form-input" formControlName="note" placeholder="Any notes..." />
            </div>

            @if (submitError()) {
              <div class="alert alert-error">{{ submitError() }}</div>
            }

            <button class="btn btn-primary btn-full" type="submit" [disabled]="submitting()">
              @if (submitting()) { <span class="spinner"></span> } Confirm
            </button>
          </form>
        </div>
      </div>
    }
  `,
  styles: [`
    .filter-bar { display: flex; gap: 8px; overflow-x: auto; padding: 4px 0 10px; scrollbar-width: none; }
    .filter-bar::-webkit-scrollbar { display: none; }
    .filter-chip { padding: 7px 14px; border-radius: 99px; border: 1.5px solid var(--color-border); background: var(--color-card); font-size: 12px; font-weight: 700; cursor: pointer; white-space: nowrap; color: var(--color-text-secondary); }
    .filter-chip.active { background: var(--color-primary); border-color: var(--color-primary); color: #fff; }
    .summary-banner { display: flex; align-items: center; margin-bottom: 12px; padding: 12px 16px; }
    .sb-item { flex: 1; text-align: center; }
    .sb-label { font-size: 11px; color: var(--color-text-muted); font-weight: 600; text-transform: uppercase; }
    .sb-val { font-family: var(--font-heading); font-size: 16px; font-weight: 900; }
    .sb-divider { width: 1px; height: 32px; background: var(--color-border); }
    .tx-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
    .tx-row { display: flex; align-items: center; gap: 10px; }
    .tx-icon-wrap { width: 40px; height: 40px; background: var(--color-card-alt); border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
    .tx-info { flex: 1; min-width: 0; }
    .tx-name { font-weight: 600; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .tx-sub { display: flex; align-items: center; gap: 6px; margin-top: 2px; }
    .tx-cat { font-size: 11px; color: var(--color-text-muted); }
    .tx-date { font-size: 11px; color: var(--color-text-muted); }
    .tx-amount-col { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
    .tx-amount-col span { font-family: var(--font-heading); font-size: 15px; font-weight: 900; }
    .del-btn { background: none; border: none; cursor: pointer; font-size: 14px; padding: 0; opacity: 0.5; }
    .del-btn:hover { opacity: 1; }
    .alert { padding: 10px 14px; border-radius: 10px; font-size: 13px; font-weight: 600; margin-bottom: 12px; }
    .alert-error { background: var(--color-danger-light); color: var(--color-danger); }
    .type-tabs { display: flex; gap: 6px; margin-bottom: 16px; overflow-x: auto; scrollbar-width: none; }
    .type-tabs::-webkit-scrollbar { display: none; }
    .type-tab { padding: 7px 12px; border-radius: 10px; border: 1.5px solid var(--color-border); background: var(--color-card); font-size: 12px; font-weight: 700; cursor: pointer; color: var(--color-text-secondary); white-space: nowrap; }
    .type-tab.active { background: var(--color-primary); border-color: var(--color-primary); color: #fff; }
    .sheet-row { display: flex; gap: 10px; }
  `]
})
export class TransactionsComponent implements OnInit {

  transactions = signal<Transaction[]>([]);
  page         = signal<PagedResponse<Transaction>>({ content:[], pageNumber:0, pageSize:20, totalElements:0, totalPages:0, first:true, last:true, empty:true });
  summary      = signal<MonthSummary | null>(null);
  loading      = signal(false);
  activeType   = signal<string>('ALL');
  openSheet    = signal(false);
  submitting   = signal(false);
  submitError  = signal('');

  types = ['ALL','EXPENSE','INCOME','INVESTMENT','SAVINGS'];
  form: FormGroup;

  currentCategories = signal<string[]>(CATEGORIES.EXPENSE);

  constructor(
    private txService:    TransactionService,
    private walletService:WalletService,
    private toastService: ToastService,
    private fb:           FormBuilder
  ) {
    this.form = this.fb.group({
      type:     ['EXPENSE'],
      name:     ['', Validators.required],
      amount:   [null, [Validators.required, Validators.min(0.01)]],
      category: [CATEGORIES.EXPENSE[0]],
      txDate:   [new Date().toISOString().slice(0,10)],
      note:     ['']
    });
  }

  ngOnInit(): void {
    this.loadTransactions();
    this.loadSummary();
  }

  private loadTransactions(append = false): void {
    this.loading.set(true);
    const pg = append ? this.page().pageNumber + 1 : 0;
    const type = this.activeType() !== 'ALL' ? this.activeType() : undefined;

    this.txService.getTransactions({ type, page: pg }).subscribe({
      next: res => {
        if (res.success && res.data) {
          this.page.set(res.data);
          if (append) this.transactions.update(t => [...t, ...res.data!.content]);
          else this.transactions.set(res.data.content);
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private loadSummary(): void {
    this.txService.getMonthlySummary().subscribe({
      next: res => { if (res.success && res.data) this.summary.set(res.data); },
      error: () => {}
    });
  }

  setType(t: string): void {
    this.activeType.set(t);
    this.loadTransactions();
  }

  loadMore(): void { this.loadTransactions(true); }

  setFormType(t: string): void {
    this.form.patchValue({ type: t, category: CATEGORIES[t as TransactionType]?.[0] ?? '' });
    this.currentCategories.set(CATEGORIES[t as TransactionType] ?? []);
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting.set(true);
    this.submitError.set('');

    const wallet = this.walletService.wallet();
    this.txService.create({ ...this.form.value, walletId: wallet?.id }).subscribe({
      next: res => {
        this.submitting.set(false);
        if (res.success) {
          this.closeSheet();
          this.toastService.success('Transaction added ✅');
          this.loadTransactions();
          this.loadSummary();
        } else {
          this.submitError.set(res.message || 'Failed to add transaction');
        }
      },
      error: err => {
        this.submitting.set(false);
        this.submitError.set(err.error?.message || 'Network error');
      }
    });
  }

  deleteTransaction(id: number): void {
    if (!confirm('Delete this transaction?')) return;
    this.txService.remove(id).subscribe({
      next: res => {
        if (res.success) {
          this.transactions.update(t => t.filter(tx => tx.id !== id));
          this.toastService.success('Deleted');
          this.loadSummary();
        }
      },
      error: () => this.toastService.error('Failed to delete')
    });
  }

  closeSheet(): void {
    this.openSheet.set(false);
    this.form.reset({
      type: 'EXPENSE', category: CATEGORIES.EXPENSE[0],
      txDate: new Date().toISOString().slice(0,10)
    });
    this.currentCategories.set(CATEGORIES.EXPENSE);
  }

  getCatIcon(cat: string): string {
    const m: Record<string,string> = {
      'Food & Dining':'🍕', Groceries:'🛒', Transport:'🚗', Shopping:'🛍️',
      Healthcare:'💊', Entertainment:'🎬', Salary:'💰', 'Mutual Fund':'📊',
      Stocks:'📈', 'FD/RD':'🏦', NPS:'🏛️', Gold:'🥇', Others:'📦'
    };
    return m[cat] ?? '💳';
  }
}
