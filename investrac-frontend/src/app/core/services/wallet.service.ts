import { Injectable, signal, computed } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse } from '../models/api-response.model';
import { Wallet, CreateWalletRequest } from '../models/wallet.model';

@Injectable({ providedIn: 'root' })
export class WalletService extends ApiService {

  private _wallet = signal<Wallet | null>(null);
  readonly wallet      = this._wallet.asReadonly();
  readonly hasWallet   = computed(() => this._wallet() !== null);
  readonly balance     = computed(() => this._wallet()?.balance ?? 0);
  readonly freeToSpend = computed(() => this._wallet()?.freeToSpend ?? 0);
  readonly usedPercent = computed(() => this._wallet()?.usedPercent ?? 0);

  getCurrentWallet(): Observable<ApiResponse<Wallet>> {
    return this.get<Wallet>('/wallet/current')
      .pipe(tap(res => {
        if (res.success && res.data) this._wallet.set(res.data);
      }));
  }

  createWallet(req: CreateWalletRequest): Observable<ApiResponse<Wallet>> {
    return this.post<Wallet>('/wallet', req)
      .pipe(tap(res => {
        if (res.success && res.data) this._wallet.set(res.data);
      }));
  }

  topUp(amount: number, source?: string): Observable<ApiResponse<Wallet>> {
    return this.post<Wallet>('/wallet/topup', { amount, source })
      .pipe(tap(res => {
        if (res.success && res.data) this._wallet.set(res.data);
      }));
  }
}
