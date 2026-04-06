import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, MatListModule, MatIconModule],
  styles: [`
    nav { width: 220px; background: #fff; min-height: 100%; padding: 8px 0; box-shadow: 2px 0 4px rgba(0,0,0,.1); }
    a { display: flex; align-items: center; gap: 12px; padding: 12px 20px; text-decoration: none; color: #333; font-size: 14px; }
    a:hover, a.active { background: #e8eaf6; color: #3f51b5; border-right: 3px solid #3f51b5; }
    mat-icon { font-size: 20px; }
  `],
  template: `
    <nav>
      <a routerLink="/dashboard" routerLinkActive="active"><mat-icon>dashboard</mat-icon> Dashboard</a>
      <a routerLink="/wallet" routerLinkActive="active"><mat-icon>account_balance_wallet</mat-icon> Wallet</a>
      <a routerLink="/transactions" routerLinkActive="active"><mat-icon>receipt_long</mat-icon> Transactions</a>
      <a routerLink="/accounts" routerLinkActive="active"><mat-icon>savings</mat-icon> Accounts</a>
      <a routerLink="/portfolio" routerLinkActive="active"><mat-icon>pie_chart</mat-icon> Portfolio</a>
      <a routerLink="/settings" routerLinkActive="active"><mat-icon>settings</mat-icon> Settings</a>
    </nav>
  `
})
export class SidebarComponent {}
