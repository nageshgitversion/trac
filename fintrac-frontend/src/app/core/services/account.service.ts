import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Account, AccountProjection, CreateAccountRequest, UpdateAccountRequest } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private base = `${environment.apiUrl}/accounts`;
  constructor(private http: HttpClient) {}

  list(): Observable<ApiResponse<Account[]>> {
    return this.http.get<ApiResponse<Account[]>>(this.base);
  }
  create(req: CreateAccountRequest): Observable<ApiResponse<Account>> {
    return this.http.post<ApiResponse<Account>>(this.base, req);
  }
  update(id: number, req: UpdateAccountRequest): Observable<ApiResponse<Account>> {
    return this.http.put<ApiResponse<Account>>(`${this.base}/${id}`, req);
  }
  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`);
  }
  projection(id: number): Observable<ApiResponse<AccountProjection>> {
    return this.http.get<ApiResponse<AccountProjection>>(`${this.base}/${id}/projection`);
  }
}
