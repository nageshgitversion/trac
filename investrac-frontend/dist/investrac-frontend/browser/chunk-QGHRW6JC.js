import {
  HttpClient,
  HttpParams,
  environment,
  ɵɵdefineInjectable,
  ɵɵinject
} from "./chunk-TWKZKYET.js";

// src/app/core/services/api.service.ts
var ApiService = class _ApiService {
  constructor(http) {
    this.http = http;
    this.baseUrl = environment.apiUrl;
  }
  get(path, params) {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([key, val]) => {
        if (val !== null && val !== void 0 && val !== "") {
          httpParams = httpParams.set(key, String(val));
        }
      });
    }
    return this.http.get(`${this.baseUrl}${path}`, { params: httpParams });
  }
  post(path, body) {
    return this.http.post(`${this.baseUrl}${path}`, body);
  }
  put(path, body) {
    return this.http.put(`${this.baseUrl}${path}`, body);
  }
  patch(path, body) {
    return this.http.patch(`${this.baseUrl}${path}`, body);
  }
  delete(path) {
    return this.http.delete(`${this.baseUrl}${path}`);
  }
  static {
    this.\u0275fac = function ApiService_Factory(t) {
      return new (t || _ApiService)(\u0275\u0275inject(HttpClient));
    };
  }
  static {
    this.\u0275prov = /* @__PURE__ */ \u0275\u0275defineInjectable({ token: _ApiService, factory: _ApiService.\u0275fac, providedIn: "root" });
  }
};

export {
  ApiService
};
//# sourceMappingURL=chunk-QGHRW6JC.js.map
