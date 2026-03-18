import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginRequest, RegisterRequest, AuthResponse,
  ForgotPasswordRequest, ResetPasswordRequest
} from '../models/auth.model';
import { ApiResponse } from '../models/api-response.model';

/**
 * Authentication service.
 * Manages JWT tokens, user state via Signals, and token refresh.
 *
 * Token storage:
 *   - accessToken: localStorage (short-lived — 15 min)
 *   - refreshToken: localStorage (30 days)
 *   - userState: Angular Signal (reactive)
 *
 * SECURITY:
 *   - accessToken cleared on logout
 *   - refreshToken rotated on every use (handled by backend)
 *   - Never log token values
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY   = 'investrac_access_token';
  private readonly REFRESH_KEY = 'investrac_refresh_token';
  private readonly USER_KEY    = 'investrac_user';

  // ── Reactive state with Signals ──
  private _user = signal<AuthResponse['user'] | null>(this.loadUser());
  readonly user     = this._user.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null);
  readonly userName   = computed(() => this._user()?.name ?? '');
  readonly userId     = computed(() => this._user()?.id ?? null);

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  // ── LOGIN ──
  login(req: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/login`, req)
      .pipe(
        tap(res => {
          if (res.success && res.data) {
            this.storeTokens(res.data);
          }
        })
      );
  }

  // ── REGISTER ──
  register(req: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/register`, req)
      .pipe(
        tap(res => {
          if (res.success && res.data) {
            this.storeTokens(res.data);
          }
        })
      );
  }

  // ── REFRESH TOKEN ──
  refreshToken(): Observable<ApiResponse<AuthResponse>> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('No refresh token'));
    }
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/refresh`,
        { refreshToken })
      .pipe(
        tap(res => {
          if (res.success && res.data) {
            this.storeTokens(res.data);
          } else {
            this.logout();
          }
        }),
        catchError(err => {
          this.logout();
          return throwError(() => err);
        })
      );
  }

  // ── LOGOUT ──
  logout(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      // Fire-and-forget — don't wait for server response
      this.http.post(`${environment.apiUrl}/auth/logout`,
        { refreshToken }).subscribe({ error: () => {} });
    }
    this.clearTokens();
    this.router.navigate(['/auth/login']);
  }

  // ── FORGOT / RESET PASSWORD ──
  forgotPassword(req: ForgotPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${environment.apiUrl}/auth/forgot-password`, req);
  }

  resetPassword(req: ResetPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${environment.apiUrl}/auth/reset-password`, req);
  }

  // ── TOKEN ACCESSORS ──
  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_KEY);
  }

  isTokenExpiringSoon(): boolean {
    const token = this.getAccessToken();
    if (!token) return true;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiresIn = payload.exp * 1000 - Date.now();
      return expiresIn < 2 * 60 * 1000; // Less than 2 minutes
    } catch {
      return true;
    }
  }

  // ── PRIVATE ──
  private storeTokens(auth: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, auth.accessToken);
    localStorage.setItem(this.REFRESH_KEY, auth.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(auth.user));
    this._user.set(auth.user);
  }

  private clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._user.set(null);
  }

  private loadUser(): AuthResponse['user'] | null {
    try {
      const stored = localStorage.getItem(this.USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }
}
