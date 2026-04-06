import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { TransactionService } from '../../core/services/transaction.service';
import { Transaction, TransactionCategory, TransactionType } from '../../core/models/transaction.model';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule, MatTableModule, MatPaginatorModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule,
    MatIconModule, MatDialogModule, MatSnackBarModule, MatCardModule
  ],
  styles: [`
    .header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
    .filters { display:flex; gap:12px; flex-wrap:wrap; margin-bottom:16px; }
    .filters mat-form-field { width:180px; }
    table { width:100%; }
    .credit { color:#4caf50; font-weight:500; }
    .debit { color:#f44336; font-weight:500; }
  `],
  template: `
    <div class="header">
      <h2>Transactions</h2>
      <button mat-raised-button color="primary" (click)="openAddDialog()">
        <mat-icon>add</mat-icon> Add Transaction
      </button>
    </div>

    <mat-card style="padding:16px;margin-bottom:16px">
      <div class="filters">
        <mat-form-field appearance="outline">
          <mat-label>Type</mat-label>
          <mat-select [(value)]="filterType" (selectionChange)="loadData()">
            <mat-option value="">All</mat-option>
            <mat-option value="CREDIT">Credit</mat-option>
            <mat-option value="DEBIT">Debit</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Category</mat-label>
          <mat-select [(value)]="filterCat" (selectionChange)="loadData()">
            <mat-option value="">All</mat-option>
            <mat-option *ngFor="let c of categories" [value]="c">{{c}}</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>From</mat-label>
          <input matInput type="date" [(ngModel)]="filterFrom" [ngModelOptions]="{standalone: true}" (change)="loadData()" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>To</mat-label>
          <input matInput type="date" [(ngModel)]="filterTo" [ngModelOptions]="{standalone: true}" (change)="loadData()" />
        </mat-form-field>
      </div>
    </mat-card>

    <mat-card>
      <table mat-table [dataSource]="transactions">
        <ng-container matColumnDef="txDate"><th mat-header-cell *matHeaderCellDef>Date</th><td mat-cell *matCellDef="let t">{{t.txDate}}</td></ng-container>
        <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let t">{{t.name}}</td></ng-container>
        <ng-container matColumnDef="category"><th mat-header-cell *matHeaderCellDef>Category</th><td mat-cell *matCellDef="let t">{{t.category}}</td></ng-container>
        <ng-container matColumnDef="type"><th mat-header-cell *matHeaderCellDef>Type</th><td mat-cell *matCellDef="let t"><span [class]="t.type === 'CREDIT' ? 'credit' : 'debit'">{{t.type}}</span></td></ng-container>
        <ng-container matColumnDef="amount"><th mat-header-cell *matHeaderCellDef>Amount</th><td mat-cell *matCellDef="let t">₹{{t.amount | number:'1.2-2'}}</td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th><td mat-cell *matCellDef="let t"><button mat-icon-button color="warn" (click)="deleteRow(t)"><mat-icon>delete</mat-icon></button></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let row; columns: cols"></tr>
      </table>
      <mat-paginator [length]="totalElements" [pageSize]="pageSize" [pageSizeOptions]="[10,20,50]" (page)="onPage($event)" />
    </mat-card>

    <!-- Add dialog (inline modal) -->
    <div *ngIf="showAddForm" style="position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.5);z-index:100;display:flex;align-items:center;justify-content:center">
      <mat-card style="width:440px;padding:24px;max-height:90vh;overflow-y:auto">
        <h3>Add Transaction</h3>
        <form [formGroup]="addForm" (ngSubmit)="submitAdd()" style="display:flex;flex-direction:column;gap:8px;margin-top:16px">
          <mat-form-field appearance="outline"><mat-label>Type</mat-label>
            <mat-select formControlName="type"><mat-option value="CREDIT">Credit</mat-option><mat-option value="DEBIT">Debit</mat-option></mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Category</mat-label>
            <mat-select formControlName="category"><mat-option *ngFor="let c of categories" [value]="c">{{c}}</mat-option></mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Name</mat-label><input matInput formControlName="name" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Amount (₹)</mat-label><input matInput type="number" formControlName="amount" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Date</mat-label><input matInput type="date" formControlName="txDate" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Note</mat-label><input matInput formControlName="note" /></mat-form-field>
          <div style="display:flex;gap:8px;margin-top:8px">
            <button mat-button type="button" (click)="showAddForm=false">Cancel</button>
            <button mat-raised-button color="primary" type="submit" [disabled]="addForm.invalid">Save</button>
          </div>
        </form>
      </mat-card>
    </div>
  `
})
export class TransactionsComponent implements OnInit {
  transactions: Transaction[] = [];
  cols = ['txDate','name','category','type','amount','actions'];
  categories: TransactionCategory[] = ['FOOD','TRANSPORT','SALARY','SHOPPING','INVESTMENT','OTHER'];
  filterType: TransactionType | '' = '';
  filterCat: TransactionCategory | '' = '';
  filterFrom = '';
  filterTo = '';
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  showAddForm = false;
  addForm!: FormGroup;

  constructor(private txService: TransactionService, private fb: FormBuilder, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.addForm = this.fb.group({
      type: ['CREDIT', Validators.required],
      category: ['OTHER', Validators.required],
      name: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      txDate: [new Date().toISOString().slice(0,10), Validators.required],
      note: ['']
    });
    this.loadData();
  }

  loadData() {
    this.txService.list({
      type: (this.filterType as TransactionType) || undefined,
      category: (this.filterCat as TransactionCategory) || undefined,
      from: this.filterFrom || undefined,
      to: this.filterTo || undefined,
      page: this.pageIndex, size: this.pageSize
    }).subscribe(r => {
      this.transactions = r.data?.content ?? [];
      this.totalElements = r.data?.totalElements ?? 0;
    });
  }

  onPage(e: PageEvent) { this.pageIndex = e.pageIndex; this.pageSize = e.pageSize; this.loadData(); }
  openAddDialog() { this.showAddForm = true; }

  submitAdd() {
    this.txService.create(this.addForm.value).subscribe({
      next: () => { this.showAddForm = false; this.addForm.reset({ type:'CREDIT', category:'OTHER', txDate: new Date().toISOString().slice(0,10) }); this.loadData(); },
      error: e => this.snackBar.open(e.error?.message || 'Error', 'OK', { duration: 3000 })
    });
  }

  deleteRow(t: Transaction) {
    this.txService.delete(t.id).subscribe({ next: () => this.loadData(), error: () => {} });
  }
}
