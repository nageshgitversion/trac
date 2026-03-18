import { Component, OnInit, ChangeDetectionStrategy, signal } from '@angular/core';
import { NgClass, DecimalPipe, SlicePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PortfolioService } from '../../core/services/portfolio.service';
import { ToastService } from '../../core/services/toast.service';
import { ToastComponent } from '../../shared/components/toast/toast.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { InrFormatPipe } from '../../shared/pipes/inr-format.pipe';
import { Holding, PortfolioSummary } from '../../core/models/portfolio.model';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [NgClass, RouterLink, ToastComponent, LoadingSpinnerComponent, InrFormatPipe, DecimalPipe, SlicePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <app-toast />
      <header class="page-header">
        <h1 class="page-title">Portfolio</h1>
        <button class="btn btn-icon" (click)="sync()" [disabled]="syncing()" title="Sync prices">
          {{ syncing() ? '⏳' : '🔄' }}
        </button>
      </header>

      @if (loading()) {
        <app-loading-spinner message="Loading portfolio..." />
      } @else if (!summary()) {
        <div class="empty-state">
          <div class="empty-icon">📈</div>
          <div class="empty-title">No holdings yet</div>
          <div class="empty-body">Add your mutual funds, stocks, FDs to track returns</div>
          <button class="btn btn-primary btn-sm" style="margin-top:16px" (click)="showAdd.set(true)">
            + Add First Holding
          </button>
        </div>
      } @else {
        <!-- Summary hero -->
        <div class="port-hero card">
          <div class="ph-row">
            <div>
              <div class="ph-label">Current Value</div>
              <div class="ph-amount">{{ summary()!.totalValue | inr:true }}</div>
            </div>
            <div class="ph-return" [ngClass]="summary()!.totalReturnPercent >= 0 ? 'positive' : 'negative'">
              {{ summary()!.totalReturnPercent >= 0 ? '▲' : '▼' }}
              {{ summary()!.totalReturnPercent | number:'1.1-1' }}%
            </div>
          </div>

          <div class="ph-stats">
            <div class="ph-stat">
              <div class="ph-stat-label">Invested</div>
              <div class="ph-stat-val">{{ summary()!.totalInvested | inr:true }}</div>
            </div>
            <div class="ph-stat">
              <div class="ph-stat-label">Gain/Loss</div>
              <div class="ph-stat-val" [ngClass]="gain() >= 0 ? 'text-success' : 'text-danger'">
                {{ gain() | inr:true }}
              </div>
            </div>
            <div class="ph-stat">
              <div class="ph-stat-label">XIRR</div>
              <div class="ph-stat-val text-success">{{ summary()!.weightedXirr | number:'1.1-1' }}%</div>
            </div>
          </div>
        </div>

        <!-- Asset Allocation -->
        @if (summary()!.assetAllocation.length) {
          <div class="section-header"><span class="section-title">Asset Allocation</span></div>
          <div class="alloc-bar-wrap card card-sm">
            <div class="alloc-bar">
              @for (a of summary()!.assetAllocation; track a.type) {
                <div class="alloc-seg" [style.width]="a.percent + '%'"
                     [style.background]="a.color" [title]="a.type + ' ' + a.percent + '%'">
                </div>
              }
            </div>
            <div class="alloc-legend">
              @for (a of summary()!.assetAllocation; track a.type) {
                <div class="alloc-item">
                  <span class="alloc-dot" [style.background]="a.color"></span>
                  <span class="alloc-label">{{ a.type }}</span>
                  <span class="alloc-pct">{{ a.percent | number:'1.0-0' }}%</span>
                </div>
              }
            </div>
          </div>
        }

        <!-- Holdings list -->
        <div class="section-header">
          <span class="section-title">Holdings</span>
          <button class="section-action" (click)="showAdd.set(true)">+ Add</button>
        </div>

        <div class="holdings-list">
          @for (h of holdings(); track h.id) {
            <div class="holding-card card card-sm">
              <div class="hc-top">
                <div class="hc-type-badge" [style.background]="getTypeColor(h.type)">
                  {{ getTypeShort(h.type) }}
                </div>
                <div class="hc-info">
                  <div class="hc-name">{{ h.name }}</div>
                  @if (h.symbol) { <div class="hc-symbol">{{ h.symbol }}</div> }
                </div>
                <div class="hc-value">
                  <div class="hc-current">{{ h.currentValue | inr:true }}</div>
                  <div [ngClass]="h.currentValue >= h.invested ? 'text-success' : 'text-danger'"
                       style="font-size:12px;font-weight:700">
                    {{ getReturnPct(h) | number:'1.1-1' }}%
                  </div>
                </div>
              </div>

              <div class="hc-bottom">
                <span class="text-muted" style="font-size:12px">
                  Invested: {{ h.invested | inr }}
                </span>
                @if (h.isUpdatable && h.lastSynced) {
                  <span class="text-muted" style="font-size:11px">
                    Synced {{ h.lastSynced | slice:0:10 }}
                  </span>
                }
                <button class="del-btn" (click)="deleteHolding(h.id)">🗑️</button>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .port-hero { padding: 20px; }
    .ph-row { display: flex; justify-content: space-between; align-items: flex-start; }
    .ph-label { font-size: 12px; color: var(--color-text-muted); font-weight: 600; text-transform: uppercase; }
    .ph-amount { font-family: var(--font-heading); font-size: 30px; font-weight: 900; }
    .ph-return { font-family: var(--font-heading); font-size: 20px; font-weight: 900; padding: 6px 12px; border-radius: 12px; }
    .ph-return.positive { background: var(--color-success-light); color: var(--color-success); }
    .ph-return.negative { background: var(--color-danger-light);  color: var(--color-danger); }
    .ph-stats { display: flex; gap: 0; margin-top: 16px; }
    .ph-stat { flex: 1; text-align: center; border-right: 1px solid var(--color-border); }
    .ph-stat:last-child { border-right: none; }
    .ph-stat-label { font-size: 11px; color: var(--color-text-muted); font-weight: 600; }
    .ph-stat-val { font-family: var(--font-heading); font-size: 16px; font-weight: 900; margin-top: 2px; }
    .alloc-bar-wrap { padding: 14px; }
    .alloc-bar { display: flex; height: 10px; border-radius: 99px; overflow: hidden; gap: 2px; margin-bottom: 10px; }
    .alloc-seg { border-radius: 99px; transition: width .3s; }
    .alloc-legend { display: flex; flex-wrap: wrap; gap: 10px; }
    .alloc-item { display: flex; align-items: center; gap: 5px; font-size: 12px; }
    .alloc-dot { width: 8px; height: 8px; border-radius: 50%; }
    .alloc-label { color: var(--color-text-secondary); }
    .alloc-pct { font-weight: 700; }
    .holdings-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
    .holding-card { padding: 12px 14px; }
    .hc-top { display: flex; align-items: center; gap: 10px; }
    .hc-type-badge { padding: 4px 8px; border-radius: 8px; font-size: 10px; font-weight: 900; color: #fff; flex-shrink: 0; }
    .hc-info { flex: 1; min-width: 0; }
    .hc-name { font-weight: 700; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .hc-symbol { font-size: 11px; color: var(--color-text-muted); }
    .hc-value { text-align: right; }
    .hc-current { font-family: var(--font-heading); font-size: 15px; font-weight: 900; }
    .hc-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 8px; }
    .del-btn { background: none; border: none; cursor: pointer; font-size: 14px; opacity: 0.5; }
  `]
})
export class PortfolioComponent implements OnInit {

  summary  = this.portfolioService.summary;
  holdings = signal<Holding[]>([]);
  loading  = signal(false);
  syncing  = signal(false);
  showAdd  = signal(false);

  private readonly typeColors: Record<string, string> = {
    EQUITY_MF: '#4F46E5', STOCKS: '#7C3AED', DEBT_MF: '#06B6D4',
    NPS_PPF: '#10B981', GOLD_SGB: '#F59E0B', FD: '#3B82F6', OTHER: '#6B7280'
  };

  constructor(
    private portfolioService: PortfolioService,
    private toastService:     ToastService
  ) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.portfolioService.getSummary().subscribe({
      next: res => {
        if (res.success && res.data) this.holdings.set(res.data.holdings ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  sync(): void {
    this.syncing.set(true);
    this.portfolioService.triggerSync().subscribe({
      next: () => {
        this.syncing.set(false);
        this.toastService.success('Portfolio sync triggered — prices update after market close');
      },
      error: () => {
        this.syncing.set(false);
        this.toastService.error('Sync failed');
      }
    });
  }

  deleteHolding(id: number): void {
    if (!confirm('Remove this holding?')) return;
    this.portfolioService.deleteHolding(id).subscribe({
      next: res => {
        if (res.success) {
          this.holdings.update(h => h.filter(x => x.id !== id));
          this.toastService.success('Holding removed');
        }
      },
      error: () => this.toastService.error('Failed to delete')
    });
  }

  gain(): number {
    const s = this.summary();
    return s ? s.totalValue - s.totalInvested : 0;
  }

  getReturnPct(h: Holding): number {
    if (!h.invested || h.invested === 0) return 0;
    return ((h.currentValue - h.invested) / h.invested) * 100;
  }

  getTypeColor(type: string): string { return this.typeColors[type] ?? '#6B7280'; }

  getTypeShort(type: string): string {
    return { EQUITY_MF:'MF', STOCKS:'EQ', DEBT_MF:'DEBT', NPS_PPF:'NPS',
             GOLD_SGB:'GOLD', FD:'FD', OTHER:'OTH' }[type] ?? type.slice(0,3);
  }
}
