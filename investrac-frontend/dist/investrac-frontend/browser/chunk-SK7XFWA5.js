import {
  Router
} from "./chunk-VO5CVMHZ.js";
import {
  HttpClient,
  catchError,
  computed,
  environment,
  signal,
  tap,
  throwError,
  ɵɵdefineInjectable,
  ɵɵinject
} from "./chunk-TWKZKYET.js";

// src/app/core/services/auth.service.ts
var AuthService = class _AuthService {
  constructor(http, router) {
    this.http = http;
    this.router = router;
    this.TOKEN_KEY = "investrac_access_token";
    this.REFRESH_KEY = "investrac_refresh_token";
    this.USER_KEY = "investrac_user";
    this._user = signal(this.loadUser());
    this.user = this._user.asReadonly();
    this.isLoggedIn = computed(() => this._user() !== null);
    this.userName = computed(() => this._user()?.name ?? "");
    this.userId = computed(() => this._user()?.id ?? null);
  }
  // ── LOGIN ──
  login(req) {
    return this.http.post(`${environment.apiUrl}/auth/login`, req).pipe(tap((res) => {
      if (res.success && res.data) {
        this.storeTokens(res.data);
      }
    }));
  }
  // ── REGISTER ──
  register(req) {
    return this.http.post(`${environment.apiUrl}/auth/register`, req).pipe(tap((res) => {
      if (res.success && res.data) {
        this.storeTokens(res.data);
      }
    }));
  }
  // ── REFRESH TOKEN ──
  refreshToken() {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error("No refresh token"));
    }
    return this.http.post(`${environment.apiUrl}/auth/refresh`, { refreshToken }).pipe(tap((res) => {
      if (res.success && res.data) {
        this.storeTokens(res.data);
      } else {
        this.logout();
      }
    }), catchError((err) => {
      this.logout();
      return throwError(() => err);
    }));
  }
  // ── LOGOUT ──
  logout() {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${environment.apiUrl}/auth/logout`, { refreshToken }).subscribe({ error: () => {
      } });
    }
    this.clearTokens();
    this.router.navigate(["/auth/login"]);
  }
  // ── FORGOT / RESET PASSWORD ──
  forgotPassword(req) {
    return this.http.post(`${environment.apiUrl}/auth/forgot-password`, req);
  }
  resetPassword(req) {
    return this.http.post(`${environment.apiUrl}/auth/reset-password`, req);
  }
  // ── TOKEN ACCESSORS ──
  getAccessToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  }
  getRefreshToken() {
    return localStorage.getItem(this.REFRESH_KEY);
  }
  isTokenExpiringSoon() {
    const token = this.getAccessToken();
    if (!token)
      return true;
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      const expiresIn = payload.exp * 1e3 - Date.now();
      return expiresIn < 2 * 60 * 1e3;
    } catch {
      return true;
    }
  }
  // ── PRIVATE ──
  storeTokens(auth) {
    localStorage.setItem(this.TOKEN_KEY, auth.accessToken);
    localStorage.setItem(this.REFRESH_KEY, auth.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(auth.user));
    this._user.set(auth.user);
  }
  clearTokens() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._user.set(null);
  }
  loadUser() {
    try {
      const stored = localStorage.getItem(this.USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }
  static {
    this.\u0275fac = function AuthService_Factory(t) {
      return new (t || _AuthService)(\u0275\u0275inject(HttpClient), \u0275\u0275inject(Router));
    };
  }
  static {
    this.\u0275prov = /* @__PURE__ */ \u0275\u0275defineInjectable({ token: _AuthService, factory: _AuthService.\u0275fac, providedIn: "root" });
  }
};

export {
  AuthService
};
//# sourceMappingURL=chunk-SK7XFWA5.js.map
