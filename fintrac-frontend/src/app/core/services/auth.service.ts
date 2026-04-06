import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, RegisterRequest, AuthResponse } from '../models/auth.model';
import { ApiResponse } from '../models/api-response.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'ft_token';
  private readonly USER_KEY = 'ft_user';

  private _user = signal<AuthResponse | null>(this.loadUser());
  readonly user = this._user.asReadonly();
  readonly userName = computed(() => this._user()?.name ?? '');
  readonly userId = computed(() => this._user()?.userId ?? null);

  constructor(private http: HttpClient, private router: Router) {}

  login(req: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/login`, req)
      .pipe(tap(res => { if (res.success && res.data) this.store(res.data); }));
  }

  register(req: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/register`, req)
      .pipe(tap(res => { if (res.success && res.data) this.store(res.data); }));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._user.set(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean { return !!this.getToken(); }
  getToken(): string | null { return localStorage.getItem(this.TOKEN_KEY); }

  private store(auth: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, auth.accessToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(auth));
    this._user.set(auth);
  }

  private loadUser(): AuthResponse | null {
    try {
      const s = localStorage.getItem(this.USER_KEY);
      return s ? JSON.parse(s) : null;
    } catch { return null; }
  }
}
