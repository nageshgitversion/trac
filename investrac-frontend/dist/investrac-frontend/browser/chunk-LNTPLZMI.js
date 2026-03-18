import {
  ApiService
} from "./chunk-QGHRW6JC.js";
import {
  signal,
  tap,
  ɵɵdefineInjectable,
  ɵɵdefinePipe,
  ɵɵgetInheritedFactory
} from "./chunk-TWKZKYET.js";

// src/app/shared/pipes/relative-date.pipe.ts
var RelativeDatePipe = class _RelativeDatePipe {
  transform(dateStr) {
    if (!dateStr)
      return "";
    const date = /* @__PURE__ */ new Date(dateStr + "T00:00:00");
    const today = /* @__PURE__ */ new Date();
    today.setHours(0, 0, 0, 0);
    const diff = Math.floor((today.getTime() - date.getTime()) / 864e5);
    if (diff === 0)
      return "Today";
    if (diff === 1)
      return "Yesterday";
    if (diff < 7)
      return `${diff} days ago`;
    return date.toLocaleDateString("en-IN", { day: "numeric", month: "short" });
  }
  static {
    this.\u0275fac = function RelativeDatePipe_Factory(t) {
      return new (t || _RelativeDatePipe)();
    };
  }
  static {
    this.\u0275pipe = /* @__PURE__ */ \u0275\u0275definePipe({ name: "relativeDate", type: _RelativeDatePipe, pure: true, standalone: true });
  }
};

// src/app/core/services/transaction.service.ts
var TransactionService = class _TransactionService extends ApiService {
  constructor() {
    super(...arguments);
    this._recent = signal([]);
    this.recent = this._recent.asReadonly();
  }
  getTransactions(filters) {
    return this.get("/transactions", filters);
  }
  getRecent(limit = 10) {
    return this.get("/transactions/recent", { limit }).pipe(tap((res) => {
      if (res.success && res.data)
        this._recent.set(res.data);
    }));
  }
  getMonthlySummary(year, month) {
    return this.get("/transactions/summary", { year, month });
  }
  create(req) {
    return this.post("/transactions", req);
  }
  update(id, req) {
    return this.put(`/transactions/${id}`, req);
  }
  remove(id) {
    return this.delete(`/transactions/${id}`);
  }
  static {
    this.\u0275fac = /* @__PURE__ */ (() => {
      let \u0275TransactionService_BaseFactory;
      return function TransactionService_Factory(t) {
        return (\u0275TransactionService_BaseFactory || (\u0275TransactionService_BaseFactory = \u0275\u0275getInheritedFactory(_TransactionService)))(t || _TransactionService);
      };
    })();
  }
  static {
    this.\u0275prov = /* @__PURE__ */ \u0275\u0275defineInjectable({ token: _TransactionService, factory: _TransactionService.\u0275fac, providedIn: "root" });
  }
};

export {
  RelativeDatePipe,
  TransactionService
};
//# sourceMappingURL=chunk-LNTPLZMI.js.map
