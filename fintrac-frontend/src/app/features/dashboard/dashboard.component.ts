import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { WalletService } from '../../core/services/wallet.service';
import { TransactionService } from '../../core/services/transaction.service';
import { PortfolioService } from '../../core/services/portfolio.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, CurrencyPipe],
  styles: [`
    h2 { margin-bottom: 24px; }
    .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; }
    mat-card { padding: 20px; }
    .card-title { font-size: 13px; color: #666; margin-bottom: 8px; }
    .card-value { font-size: 24px; font-weight: bold; color: #3f51b5; }
    .card-icon { float: right; color: #9e9e9e; }
  `],
  template: `
    <h2>Dashboard</h2>
    <div class="cards">
      <mat-card>
        <mat-icon class="card-icon">account_balance_wallet</mat-icon>
        <div class="card-title">Wallet Balance</div>
        <div class="card-value">₹{{ walletBalance | number:'1.2-2' }}</div>
      </mat-card>
      <mat-card>
        <mat-icon class="card-icon">trending_up</mat-icon>
        <div class="card-title">Total Invested</div>
        <div class="card-value">₹{{ totalInvested | number:'1.2-2' }}</div>
      </mat-card>
      <mat-card>
        <mat-icon class="card-icon">receipt_long</mat-icon>
        <div class="card-title">Income This Month</div>
        <div class="card-value">₹{{ monthlyIncome | number:'1.2-2' }}</div>
      </mat-card>
      <mat-card>
        <mat-icon class="card-icon">savings</mat-icon>
        <div class="card-title">Net Savings This Month</div>
        <div class="card-value" [style.color]="netSavings >= 0 ? '#4caf50' : '#f44336'">₹{{ netSavings | number:'1.2-2' }}</div>
      </mat-card>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  walletBalance = 0;
  totalInvested = 0;
  monthlyIncome = 0;
  netSavings = 0;

  constructor(
    private walletService: WalletService,
    private txService: TransactionService,
    private portfolioService: PortfolioService
  ) {}

  ngOnInit(): void {
    const now = new Date();
    forkJoin({
      wallet: this.walletService.getWallet(),
      summary: this.txService.summary(now.getFullYear(), now.getMonth() + 1),
      portfolio: this.portfolioService.summary()
    }).subscribe(({ wallet, summary, portfolio }) => {
      this.walletBalance = wallet.data?.balance ?? 0;
      this.monthlyIncome = summary.data?.totalIncome ?? 0;
      this.netSavings = summary.data?.netSavings ?? 0;
      this.totalInvested = portfolio.data?.totalInvested ?? 0;
    });
  }
}
