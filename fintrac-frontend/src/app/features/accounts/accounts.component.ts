import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { AccountService } from '../../core/services/account.service';
import { Account, AccountProjection, AccountType } from '../../core/models/account.model';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule, MatSnackBarModule, MatChipsModule],
  styles: [`
    .header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
    .accounts-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(280px,1fr)); gap:16px; }
    .account-card { padding:20px; cursor:pointer; transition:box-shadow .2s; }
    .account-card:hover { box-shadow:0 4px 12px rgba(0,0,0,.15); }
    .acc-type { font-size:12px; background:#e8eaf6; color:#3f51b5; padding:2px 8px; border-radius:12px; display:inline-block; margin-bottom:8px; }
    .acc-name { font-size:18px; font-weight:500; margin-bottom:4px; }
    .acc-principal { font-size:22px; color:#3f51b5; font-weight:bold; }
  `],
  template: `
    <div class="header">
      <h2>Accounts</h2>
      <button mat-raised-button color="primary" (click)="showAddForm=true"><mat-icon>add</mat-icon> Add Account</button>
    </div>
    <div class="accounts-grid">
      <mat-card class="account-card" *ngFor="let a of accounts" (click)="viewProjection(a)">
        <div class="acc-type">{{a.type}}</div>
        <div class="acc-name">{{a.name}}</div>
        <div class="acc-principal">₹{{a.principal | number:'1.2-2'}}</div>
        <div style="color:#666;font-size:13px;margin-top:4px">
          <span *ngIf="a.interestRate">{{a.interestRate}}% p.a.</span>
          <span *ngIf="a.tenureMonths"> · {{a.tenureMonths}} months</span>
        </div>
        <div style="margin-top:8px">
          <span [style.color]="a.status === 'ACTIVE' ? '#4caf50' : '#9e9e9e'" style="font-size:12px">● {{a.status}}</span>
          <button mat-icon-button color="warn" style="float:right" (click)="$event.stopPropagation(); deleteAccount(a)"><mat-icon>delete</mat-icon></button>
        </div>
      </mat-card>
    </div>

    <!-- Add form overlay -->
    <div *ngIf="showAddForm" style="position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.5);z-index:100;display:flex;align-items:center;justify-content:center">
      <mat-card style="width:440px;padding:24px;max-height:90vh;overflow-y:auto">
        <h3>Add Account</h3>
        <form [formGroup]="addForm" (ngSubmit)="submitAdd()" style="display:flex;flex-direction:column;gap:8px;margin-top:16px">
          <mat-form-field appearance="outline"><mat-label>Type</mat-label>
            <mat-select formControlName="type"><mat-option *ngFor="let t of accountTypes" [value]="t">{{t}}</mat-option></mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Name</mat-label><input matInput formControlName="name" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Principal (₹)</mat-label><input matInput type="number" formControlName="principal" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Interest Rate (%)</mat-label><input matInput type="number" formControlName="interestRate" step="0.1" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Tenure (months)</mat-label><input matInput type="number" formControlName="tenureMonths" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Start Date</mat-label><input matInput type="date" formControlName="startDate" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Note</mat-label><input matInput formControlName="note" /></mat-form-field>
          <div style="display:flex;gap:8px;margin-top:8px">
            <button mat-button type="button" (click)="showAddForm=false">Cancel</button>
            <button mat-raised-button color="primary" type="submit" [disabled]="addForm.invalid">Save</button>
          </div>
        </form>
      </mat-card>
    </div>

    <!-- Projection overlay -->
    <div *ngIf="projection" style="position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.5);z-index:100;display:flex;align-items:center;justify-content:center">
      <mat-card style="width:380px;padding:24px">
        <h3>Projection — {{selectedAccount?.name}}</h3>
        <div style="margin-top:16px;display:flex;flex-direction:column;gap:12px">
          <div><span style="color:#666">Maturity Amount:</span> <strong>₹{{projection.maturityAmount | number:'1.2-2'}}</strong></div>
          <div><span style="color:#666">Total Interest:</span> <strong>₹{{projection.totalInterest | number:'1.2-2'}}</strong></div>
          <div *ngIf="projection.monthlyEmi"><span style="color:#666">Monthly EMI:</span> <strong>₹{{projection.monthlyEmi | number:'1.2-2'}}</strong></div>
        </div>
        <button mat-button style="margin-top:16px" (click)="projection=null">Close</button>
      </mat-card>
    </div>
  `
})
export class AccountsComponent implements OnInit {
  accounts: Account[] = [];
  accountTypes: AccountType[] = ['FD','RD','SIP','LOAN'];
  showAddForm = false;
  addForm!: FormGroup;
  projection: AccountProjection | null = null;
  selectedAccount: Account | null = null;

  constructor(private accountService: AccountService, private fb: FormBuilder, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.addForm = this.fb.group({
      type: ['FD', Validators.required],
      name: ['', Validators.required],
      principal: [null, [Validators.required, Validators.min(1)]],
      interestRate: [null],
      tenureMonths: [null],
      startDate: [new Date().toISOString().slice(0,10)],
      note: ['']
    });
    this.loadData();
  }

  loadData() { this.accountService.list().subscribe(r => this.accounts = r.data ?? []); }

  submitAdd() {
    this.accountService.create(this.addForm.value).subscribe({
      next: () => { this.showAddForm = false; this.addForm.reset({ type:'FD', startDate: new Date().toISOString().slice(0,10) }); this.loadData(); },
      error: e => this.snackBar.open(e.error?.message || 'Error', 'OK', { duration: 3000 })
    });
  }

  viewProjection(a: Account) {
    this.selectedAccount = a;
    this.accountService.projection(a.id).subscribe(r => this.projection = r.data ?? null);
  }

  deleteAccount(a: Account) {
    this.accountService.delete(a.id).subscribe({ next: () => this.loadData(), error: () => {} });
  }
}
