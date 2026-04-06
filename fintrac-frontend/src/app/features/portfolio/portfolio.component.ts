import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PortfolioService } from '../../core/services/portfolio.service';
import { Holding, HoldingType, PortfolioSummary } from '../../core/models/portfolio.model';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatTableModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule, MatSnackBarModule],
  styles: [`
    .header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
    .summary-cards { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; margin-bottom:24px; }
    .sum-card { padding:16px; text-align:center; }
    .sum-val { font-size:22px; font-weight:bold; color:#3f51b5; }
    table { width:100%; }
    .positive { color:#4caf50; }
    .negative { color:#f44336; }
  `],
  template: `
    <div class="header">
      <h2>Portfolio</h2>
      <button mat-raised-button color="primary" (click)="showAddForm=true"><mat-icon>add</mat-icon> Add Holding</button>
    </div>

    <div class="summary-cards" *ngIf="summary">
      <mat-card class="sum-card"><div style="color:#666;font-size:13px">Total Invested</div><div class="sum-val">₹{{summary.totalInvested | number:'1.2-2'}}</div></mat-card>
      <mat-card class="sum-card"><div style="color:#666;font-size:13px">Current Value</div><div class="sum-val">₹{{summary.totalCurrentValue | number:'1.2-2'}}</div></mat-card>
      <mat-card class="sum-card"><div style="color:#666;font-size:13px">Total Return</div><div class="sum-val" [class.positive]="summary.totalReturnPct >= 0" [class.negative]="summary.totalReturnPct < 0">{{summary.totalReturnPct | number:'1.2-2'}}%</div></mat-card>
    </div>

    <mat-card>
      <table mat-table [dataSource]="holdings">
        <ng-container matColumnDef="type"><th mat-header-cell *matHeaderCellDef>Type</th><td mat-cell *matCellDef="let h">{{h.type}}</td></ng-container>
        <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let h">{{h.name}} <span style="color:#9e9e9e;font-size:11px">{{h.symbol}}</span></td></ng-container>
        <ng-container matColumnDef="units"><th mat-header-cell *matHeaderCellDef>Units</th><td mat-cell *matCellDef="let h">{{h.units}}</td></ng-container>
        <ng-container matColumnDef="buyPrice"><th mat-header-cell *matHeaderCellDef>Buy ₹</th><td mat-cell *matCellDef="let h">{{h.buyPrice | number:'1.2-2'}}</td></ng-container>
        <ng-container matColumnDef="currentPrice"><th mat-header-cell *matHeaderCellDef>Current ₹</th><td mat-cell *matCellDef="let h">{{h.currentPrice | number:'1.2-2'}}</td></ng-container>
        <ng-container matColumnDef="invested"><th mat-header-cell *matHeaderCellDef>Invested ₹</th><td mat-cell *matCellDef="let h">{{h.invested | number:'1.2-2'}}</td></ng-container>
        <ng-container matColumnDef="returnPct"><th mat-header-cell *matHeaderCellDef>Return %</th><td mat-cell *matCellDef="let h"><span [class.positive]="h.returnPct >= 0" [class.negative]="h.returnPct < 0">{{h.returnPct | number:'1.2-2'}}%</span></td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th><td mat-cell *matCellDef="let h"><button mat-icon-button color="warn" (click)="deleteHolding(h)"><mat-icon>delete</mat-icon></button></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let row; columns: cols"></tr>
      </table>
    </mat-card>

    <!-- Add form overlay -->
    <div *ngIf="showAddForm" style="position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.5);z-index:100;display:flex;align-items:center;justify-content:center">
      <mat-card style="width:440px;padding:24px;max-height:90vh;overflow-y:auto">
        <h3>Add Holding</h3>
        <form [formGroup]="addForm" (ngSubmit)="submitAdd()" style="display:flex;flex-direction:column;gap:8px;margin-top:16px">
          <mat-form-field appearance="outline"><mat-label>Type</mat-label>
            <mat-select formControlName="type"><mat-option *ngFor="let t of holdingTypes" [value]="t">{{t}}</mat-option></mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Name</mat-label><input matInput formControlName="name" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Symbol (optional)</mat-label><input matInput formControlName="symbol" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Units</mat-label><input matInput type="number" formControlName="units" step="0.000001" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Buy Price (₹)</mat-label><input matInput type="number" formControlName="buyPrice" step="0.01" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Current Price (₹)</mat-label><input matInput type="number" formControlName="currentPrice" step="0.01" /></mat-form-field>
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
export class PortfolioComponent implements OnInit {
  holdings: Holding[] = [];
  summary: PortfolioSummary | null = null;
  holdingTypes: HoldingType[] = ['MF','STOCK','GOLD','FD'];
  cols = ['type','name','units','buyPrice','currentPrice','invested','returnPct','actions'];
  showAddForm = false;
  addForm!: FormGroup;

  constructor(private portfolioService: PortfolioService, private fb: FormBuilder, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.addForm = this.fb.group({
      type: ['MF', Validators.required],
      name: ['', Validators.required],
      symbol: [''],
      units: [null],
      buyPrice: [null],
      currentPrice: [null],
      note: ['']
    });
    this.loadData();
  }

  loadData() {
    this.portfolioService.listHoldings().subscribe(r => this.holdings = r.data ?? []);
    this.portfolioService.summary().subscribe(r => this.summary = r.data ?? null);
  }

  submitAdd() {
    this.portfolioService.addHolding(this.addForm.value).subscribe({
      next: () => { this.showAddForm = false; this.addForm.reset({ type: 'MF' }); this.loadData(); },
      error: e => this.snackBar.open(e.error?.message || 'Error', 'OK', { duration: 3000 })
    });
  }

  deleteHolding(h: Holding) {
    this.portfolioService.deleteHolding(h.id).subscribe({ next: () => this.loadData(), error: () => {} });
  }
}
