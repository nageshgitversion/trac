import {
  ApiService
} from "./chunk-QGHRW6JC.js";
import {
  computed,
  signal,
  tap,
  ɵɵdefineInjectable,
  ɵɵgetInheritedFactory
} from "./chunk-TWKZKYET.js";

// src/app/core/services/wallet.service.ts
var WalletService = class _WalletService extends ApiService {
  constructor() {
    super(...arguments);
    this._wallet = signal(null);
    this.wallet = this._wallet.asReadonly();
    this.hasWallet = computed(() => this._wallet() !== null);
    this.balance = computed(() => this._wallet()?.balance ?? 0);
    this.freeToSpend = computed(() => this._wallet()?.freeToSpend ?? 0);
    this.usedPercent = computed(() => this._wallet()?.usedPercent ?? 0);
  }
  getCurrentWallet() {
    return this.get("/wallet/current").pipe(tap((res) => {
      if (res.success && res.data)
        this._wallet.set(res.data);
    }));
  }
  createWallet(req) {
    return this.post("/wallet", req).pipe(tap((res) => {
      if (res.success && res.data)
        this._wallet.set(res.data);
    }));
  }
  topUp(amount, source) {
    return this.post("/wallet/topup", { amount, source }).pipe(tap((res) => {
      if (res.success && res.data)
        this._wallet.set(res.data);
    }));
  }
  static {
    this.\u0275fac = /* @__PURE__ */ (() => {
      let \u0275WalletService_BaseFactory;
      return function WalletService_Factory(t) {
        return (\u0275WalletService_BaseFactory || (\u0275WalletService_BaseFactory = \u0275\u0275getInheritedFactory(_WalletService)))(t || _WalletService);
      };
    })();
  }
  static {
    this.\u0275prov = /* @__PURE__ */ \u0275\u0275defineInjectable({ token: _WalletService, factory: _WalletService.\u0275fac, providedIn: "root" });
  }
};

export {
  WalletService
};
//# sourceMappingURL=chunk-DNKGIIYI.js.map
