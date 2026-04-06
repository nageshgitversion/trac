import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PagedResponse } from '../models/api-response.model';
import { Transaction, CreateTransactionRequest, UpdateTransactionRequest, TransactionSummary, TransactionType, TransactionCategory } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private base = `${environment.apiUrl}/transactions`;
  constructor(private http: HttpClient) {}

  list(filters: { type?: TransactionType, category?: TransactionCategory, from?: string, to?: string, page?: number, size?: number } = {}): Observable<ApiResponse<PagedResponse<Transaction>>> {
    let params = new HttpParams();
    if (filters.type) params = params.set('type', filters.type);
    if (filters.category) params = params.set('category', filters.category);
    if (filters.from) params = params.set('from', filters.from);
    if (filters.to) params = params.set('to', filters.to);
    if (filters.page !== undefined) params = params.set('page', filters.page);
    if (filters.size !== undefined) params = params.set('size', filters.size);
    return this.http.get<ApiResponse<PagedResponse<Transaction>>>(this.base, { params });
  }

  create(req: CreateTransactionRequest): Observable<ApiResponse<Transaction>> {
    return this.http.post<ApiResponse<Transaction>>(this.base, req);
  }

  update(id: number, req: UpdateTransactionRequest): Observable<ApiResponse<Transaction>> {
    return this.http.put<ApiResponse<Transaction>>(`${this.base}/${id}`, req);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`);
  }

  summary(year: number, month: number): Observable<ApiResponse<TransactionSummary>> {
    return this.http.get<ApiResponse<TransactionSummary>>(`${this.base}/summary`, {
      params: new HttpParams().set('year', year).set('month', month)
    });
  }
}
