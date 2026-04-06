import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { WalletOperationRequest, WalletResponse } from '../models/wallet.model';

@Injectable({ providedIn: 'root' })
export class WalletService {
  private base = `${environment.apiUrl}/wallet`;
  constructor(private http: HttpClient) {}

  getWallet(): Observable<ApiResponse<WalletResponse>> {
    return this.http.get<ApiResponse<WalletResponse>>(this.base);
  }
  credit(req: WalletOperationRequest): Observable<ApiResponse<WalletResponse>> {
    return this.http.post<ApiResponse<WalletResponse>>(`${this.base}/credit`, req);
  }
  debit(req: WalletOperationRequest): Observable<ApiResponse<WalletResponse>> {
    return this.http.post<ApiResponse<WalletResponse>>(`${this.base}/debit`, req);
  }
}
