import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse, PagedResponse } from '../models/api-response.model';
import { Transaction, CreateTransactionRequest, MonthSummary } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService extends ApiService {

  // Cached recent transactions for home screen
  private _recent = signal<Transaction[]>([]);
  readonly recent = this._recent.asReadonly();

  getTransactions(filters: {
    type?: string;
    category?: string;
    from?: string;
    to?: string;
    search?: string;
    page?: number;
    size?: number;
  }): Observable<ApiResponse<PagedResponse<Transaction>>> {
    return this.get<PagedResponse<Transaction>>('/transactions', filters);
  }

  getRecent(limit = 10): Observable<ApiResponse<Transaction[]>> {
    return this.get<Transaction[]>('/transactions/recent', { limit })
      .pipe(tap(res => {
        if (res.success && res.data) this._recent.set(res.data);
      }));
  }

  getMonthlySummary(year?: number, month?: number): Observable<ApiResponse<MonthSummary>> {
    return this.get<MonthSummary>('/transactions/summary', { year, month });
  }

  create(req: CreateTransactionRequest): Observable<ApiResponse<Transaction>> {
    return this.post<Transaction>('/transactions', req);
  }

  update(id: number, req: Partial<CreateTransactionRequest>): Observable<ApiResponse<Transaction>> {
    return this.put<Transaction>(`/transactions/${id}`, req);
  }

  remove(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`/transactions/${id}`);
  }
}
