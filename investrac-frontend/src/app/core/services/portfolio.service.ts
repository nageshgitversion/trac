import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse } from '../models/api-response.model';
import { Holding, PortfolioSummary, CreateHoldingRequest } from '../models/portfolio.model';

@Injectable({ providedIn: 'root' })
export class PortfolioService extends ApiService {

  private _summary = signal<PortfolioSummary | null>(null);
  readonly summary = this._summary.asReadonly();

  getSummary(): Observable<ApiResponse<PortfolioSummary>> {
    return this.get<PortfolioSummary>('/portfolio')
      .pipe(tap(res => {
        if (res.success && res.data) this._summary.set(res.data);
      }));
  }

  getHoldings(): Observable<ApiResponse<Holding[]>> {
    return this.get<Holding[]>('/portfolio/holdings');
  }

  createHolding(req: CreateHoldingRequest): Observable<ApiResponse<Holding>> {
    return this.post<Holding>('/portfolio/holdings', req);
  }

  updateHolding(id: number, req: Partial<CreateHoldingRequest>): Observable<ApiResponse<Holding>> {
    return this.put<Holding>(`/portfolio/holdings/${id}`, req);
  }

  deleteHolding(id: number): Observable<ApiResponse<void>> {
    return this.delete<void>(`/portfolio/holdings/${id}`);
  }

  triggerSync(): Observable<ApiResponse<void>> {
    return this.post<void>('/portfolio/sync');
  }
}
