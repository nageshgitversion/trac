import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Holding, CreateHoldingRequest, UpdateHoldingRequest, PortfolioSummary } from '../models/portfolio.model';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private base = `${environment.apiUrl}/portfolio`;
  constructor(private http: HttpClient) {}

  listHoldings(): Observable<ApiResponse<Holding[]>> {
    return this.http.get<ApiResponse<Holding[]>>(`${this.base}/holdings`);
  }
  addHolding(req: CreateHoldingRequest): Observable<ApiResponse<Holding>> {
    return this.http.post<ApiResponse<Holding>>(`${this.base}/holdings`, req);
  }
  updateHolding(id: number, req: UpdateHoldingRequest): Observable<ApiResponse<Holding>> {
    return this.http.put<ApiResponse<Holding>>(`${this.base}/holdings/${id}`, req);
  }
  deleteHolding(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/holdings/${id}`);
  }
  summary(): Observable<ApiResponse<PortfolioSummary>> {
    return this.http.get<ApiResponse<PortfolioSummary>>(`${this.base}/summary`);
  }
}
