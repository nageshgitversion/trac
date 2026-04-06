import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { WalletService } from '../../core/services/wallet.service';
import { WalletResponse } from '../../core/models/wallet.model';

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSnackBarModule],
  styles: [`
    h2 { margin-bottom: 24px; }
    .balance-card { margin-bottom: 24px; padding: 24px; text-align: center; }
    .balance { font-size: 36px; font-weight: bold; color: #3f51b5; }
    .forms { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    mat-form-field { width: 100%; }
    button { width: 100%; }
  `],
  template: `
    <h2>Wallet</h2>
    <mat-card class="balance-card">
      <div style="color:#666;margin-bottom:8px">Current Balance ({{ wallet?.currency }})</div>
      <div class="balance">₹{{ wallet?.balance | number:'1.2-2' }}</div>
      <div style="color:#999;font-size:12px;margin-top:8px">Last updated: {{ wallet?.updatedAt | date:'medium' }}</div>
    </mat-card>
    <div class="forms">
      <mat-card style="padding:20px">
        <h3 style="margin-bottom:16px;color:#4caf50">Add Funds</h3>
        <form [formGroup]="creditForm" (ngSubmit)="credit()">
          <mat-form-field appearance="outline">
            <mat-label>Amount (₹)</mat-label>
            <input matInput type="number" formControlName="amount" min="0.01" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Note (optional)</mat-label>
            <input matInput formControlName="note" />
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="creditForm.invalid">Credit</button>
        </form>
      </mat-card>
      <mat-card style="padding:20px">
        <h3 style="margin-bottom:16px;color:#f44336">Deduct Funds</h3>
        <form [formGroup]="debitForm" (ngSubmit)="debit()">
          <mat-form-field appearance="outline">
            <mat-label>Amount (₹)</mat-label>
            <input matInput type="number" formControlName="amount" min="0.01" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Note (optional)</mat-label>
            <input matInput formControlName="note" />
          </mat-form-field>
          <button mat-raised-button color="warn" type="submit" [disabled]="debitForm.invalid">Debit</button>
        </form>
      </mat-card>
    </div>
  `
})
export class WalletComponent implements OnInit {
  wallet: WalletResponse | null = null;
  creditForm: FormGroup;
  debitForm: FormGroup;

  constructor(private walletService: WalletService, private fb: FormBuilder, private snackBar: MatSnackBar) {
    this.creditForm = this.fb.group({ amount: [null, [Validators.required, Validators.min(0.01)]], note: [''] });
    this.debitForm = this.fb.group({ amount: [null, [Validators.required, Validators.min(0.01)]], note: [''] });
  }

  ngOnInit() { this.load(); }
  load() { this.walletService.getWallet().subscribe(r => this.wallet = r.data ?? null); }

  credit() {
    this.walletService.credit(this.creditForm.value).subscribe({
      next: r => { this.wallet = r.data ?? null; this.creditForm.reset(); this.snackBar.open('Funds added!', 'OK', { duration: 2000 }); },
      error: e => this.snackBar.open(e.error?.message || 'Error', 'OK', { duration: 3000 })
    });
  }

  debit() {
    this.walletService.debit(this.debitForm.value).subscribe({
      next: r => { this.wallet = r.data ?? null; this.debitForm.reset(); this.snackBar.open('Funds deducted!', 'OK', { duration: 2000 }); },
      error: e => this.snackBar.open(e.error?.message || 'Error', 'OK', { duration: 3000 })
    });
  }
}
