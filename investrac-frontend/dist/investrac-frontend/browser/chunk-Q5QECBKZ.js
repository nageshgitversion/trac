import {
  RelativeDatePipe,
  TransactionService
} from "./chunk-LNTPLZMI.js";
import {
  DefaultValueAccessor,
  FormBuilder,
  FormControlName,
  FormGroupDirective,
  NgControlStatus,
  NgControlStatusGroup,
  NgSelectOption,
  NumberValueAccessor,
  ReactiveFormsModule,
  SelectControlValueAccessor,
  Validators,
  ɵNgNoValidate,
  ɵNgSelectMultipleOption
} from "./chunk-DYTDY6K6.js";
import {
  WalletService
} from "./chunk-DNKGIIYI.js";
import {
  InrFormatPipe
} from "./chunk-IDOJ465G.js";
import "./chunk-QGHRW6JC.js";
import {
  ToastComponent
} from "./chunk-TLGCWV2M.js";
import {
  NgClass,
  ToastService,
  __spreadProps,
  __spreadValues,
  signal,
  ɵsetClassDebugInfo,
  ɵɵStandaloneFeature,
  ɵɵadvance,
  ɵɵconditional,
  ɵɵdefineComponent,
  ɵɵdirectiveInject,
  ɵɵelement,
  ɵɵelementEnd,
  ɵɵelementStart,
  ɵɵgetCurrentView,
  ɵɵlistener,
  ɵɵnextContext,
  ɵɵpipe,
  ɵɵpipeBind1,
  ɵɵpipeBind2,
  ɵɵproperty,
  ɵɵpureFunction0,
  ɵɵpureFunction1,
  ɵɵrepeater,
  ɵɵrepeaterCreate,
  ɵɵrepeaterTrackByIdentity,
  ɵɵresetView,
  ɵɵrestoreView,
  ɵɵtemplate,
  ɵɵtext,
  ɵɵtextInterpolate,
  ɵɵtextInterpolate1,
  ɵɵtextInterpolate2
} from "./chunk-TWKZKYET.js";

// src/app/features/transactions/transactions.component.ts
var _forTrack0 = ($index, $item) => $item.id;
var _c0 = (a0) => ({ "active": a0 });
var _c1 = () => ["EXPENSE", "INCOME", "INVESTMENT", "SAVINGS"];
function TransactionsComponent_For_9_Template(rf, ctx) {
  if (rf & 1) {
    const _r1 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "button", 8);
    \u0275\u0275listener("click", function TransactionsComponent_For_9_Template_button_click_0_listener() {
      const t_r2 = \u0275\u0275restoreView(_r1).$implicit;
      const ctx_r2 = \u0275\u0275nextContext();
      return \u0275\u0275resetView(ctx_r2.setType(t_r2));
    });
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const t_r2 = ctx.$implicit;
    const ctx_r2 = \u0275\u0275nextContext();
    \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(2, _c0, ctx_r2.activeType() === t_r2));
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(t_r2);
  }
}
function TransactionsComponent_Conditional_10_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 6)(1, "div", 9)(2, "div", 10);
    \u0275\u0275text(3, "Income");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(4, "div", 11);
    \u0275\u0275text(5);
    \u0275\u0275pipe(6, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275element(7, "div", 12);
    \u0275\u0275elementStart(8, "div", 9)(9, "div", 10);
    \u0275\u0275text(10, "Expense");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(11, "div", 13);
    \u0275\u0275text(12);
    \u0275\u0275pipe(13, "inr");
    \u0275\u0275elementEnd()();
    \u0275\u0275element(14, "div", 12);
    \u0275\u0275elementStart(15, "div", 9)(16, "div", 10);
    \u0275\u0275text(17, "Saved");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(18, "div", 14);
    \u0275\u0275text(19);
    \u0275\u0275elementEnd()()();
  }
  if (rf & 2) {
    const ctx_r2 = \u0275\u0275nextContext();
    \u0275\u0275advance(5);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(6, 4, ctx_r2.summary().totalIncome, true));
    \u0275\u0275advance(7);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind2(13, 7, ctx_r2.summary().totalExpense, true));
    \u0275\u0275advance(6);
    \u0275\u0275property("ngClass", ctx_r2.summary().netSavings >= 0 ? "text-success" : "text-danger");
    \u0275\u0275advance();
    \u0275\u0275textInterpolate1(" ", ctx_r2.summary().savingsRatePercent, "% ");
  }
}
function TransactionsComponent_Conditional_11_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "div", 15)(1, "div", 15)(2, "div", 15);
  }
}
function TransactionsComponent_Conditional_12_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 16)(1, "div", 17);
    \u0275\u0275text(2, "\u{1F4B3}");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "div", 18);
    \u0275\u0275text(4, "No transactions");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "div", 19);
    \u0275\u0275text(6, "Tap + Add to record your first transaction");
    \u0275\u0275elementEnd()();
  }
}
function TransactionsComponent_Conditional_13_For_2_Conditional_12_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 29);
    \u0275\u0275text(1, "Failed");
    \u0275\u0275elementEnd();
  }
}
function TransactionsComponent_Conditional_13_For_2_Conditional_13_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "span", 30);
    \u0275\u0275text(1, "Pending");
    \u0275\u0275elementEnd();
  }
}
function TransactionsComponent_Conditional_13_For_2_Template(rf, ctx) {
  if (rf & 1) {
    const _r4 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "div", 21)(1, "div", 23);
    \u0275\u0275text(2);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(3, "div", 24)(4, "div", 25);
    \u0275\u0275text(5);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(6, "div", 26)(7, "span", 27);
    \u0275\u0275text(8);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(9, "span", 28);
    \u0275\u0275text(10);
    \u0275\u0275pipe(11, "relativeDate");
    \u0275\u0275elementEnd();
    \u0275\u0275template(12, TransactionsComponent_Conditional_13_For_2_Conditional_12_Template, 2, 0, "span", 29)(13, TransactionsComponent_Conditional_13_For_2_Conditional_13_Template, 2, 0, "span", 30);
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(14, "div", 31)(15, "span", 32);
    \u0275\u0275text(16);
    \u0275\u0275pipe(17, "inr");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(18, "button", 33);
    \u0275\u0275listener("click", function TransactionsComponent_Conditional_13_For_2_Template_button_click_18_listener() {
      const tx_r5 = \u0275\u0275restoreView(_r4).$implicit;
      const ctx_r2 = \u0275\u0275nextContext(2);
      return \u0275\u0275resetView(ctx_r2.deleteTransaction(tx_r5.id));
    });
    \u0275\u0275text(19, "\u{1F5D1}\uFE0F");
    \u0275\u0275elementEnd()()();
  }
  if (rf & 2) {
    const tx_r5 = ctx.$implicit;
    const ctx_r2 = \u0275\u0275nextContext(2);
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(ctx_r2.getCatIcon(tx_r5.category));
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(tx_r5.name);
    \u0275\u0275advance(3);
    \u0275\u0275textInterpolate(tx_r5.category);
    \u0275\u0275advance(2);
    \u0275\u0275textInterpolate(\u0275\u0275pipeBind1(11, 9, tx_r5.txDate));
    \u0275\u0275advance(2);
    \u0275\u0275conditional(12, tx_r5.status === "FAILED" ? 12 : -1);
    \u0275\u0275advance();
    \u0275\u0275conditional(13, tx_r5.status === "PENDING" ? 13 : -1);
    \u0275\u0275advance(2);
    \u0275\u0275property("ngClass", tx_r5.type === "INCOME" ? "amount-income" : "amount-expense");
    \u0275\u0275advance();
    \u0275\u0275textInterpolate2(" ", tx_r5.type === "INCOME" ? "+" : "-", "", \u0275\u0275pipeBind1(17, 11, tx_r5.amount), " ");
  }
}
function TransactionsComponent_Conditional_13_Conditional_3_Template(rf, ctx) {
  if (rf & 1) {
    const _r6 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "button", 34);
    \u0275\u0275listener("click", function TransactionsComponent_Conditional_13_Conditional_3_Template_button_click_0_listener() {
      \u0275\u0275restoreView(_r6);
      const ctx_r2 = \u0275\u0275nextContext(2);
      return \u0275\u0275resetView(ctx_r2.loadMore());
    });
    \u0275\u0275text(1, "Load more");
    \u0275\u0275elementEnd();
  }
}
function TransactionsComponent_Conditional_13_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 20);
    \u0275\u0275repeaterCreate(1, TransactionsComponent_Conditional_13_For_2_Template, 20, 13, "div", 21, _forTrack0);
    \u0275\u0275elementEnd();
    \u0275\u0275template(3, TransactionsComponent_Conditional_13_Conditional_3_Template, 2, 0, "button", 22);
  }
  if (rf & 2) {
    const ctx_r2 = \u0275\u0275nextContext();
    \u0275\u0275advance();
    \u0275\u0275repeater(ctx_r2.transactions());
    \u0275\u0275advance(2);
    \u0275\u0275conditional(3, !ctx_r2.page().last ? 3 : -1);
  }
}
function TransactionsComponent_Conditional_14_For_8_Template(rf, ctx) {
  if (rf & 1) {
    const _r8 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "button", 55);
    \u0275\u0275listener("click", function TransactionsComponent_Conditional_14_For_8_Template_button_click_0_listener() {
      const t_r9 = \u0275\u0275restoreView(_r8).$implicit;
      const ctx_r2 = \u0275\u0275nextContext(2);
      return \u0275\u0275resetView(ctx_r2.setFormType(t_r9));
    });
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const t_r9 = ctx.$implicit;
    const ctx_r2 = \u0275\u0275nextContext(2);
    \u0275\u0275property("ngClass", \u0275\u0275pureFunction1(2, _c0, ctx_r2.form.get("type").value === t_r9));
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(t_r9);
  }
}
function TransactionsComponent_Conditional_14_For_23_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "option", 49);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const cat_r10 = ctx.$implicit;
    \u0275\u0275property("value", cat_r10);
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(cat_r10);
  }
}
function TransactionsComponent_Conditional_14_Conditional_32_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275elementStart(0, "div", 52);
    \u0275\u0275text(1);
    \u0275\u0275elementEnd();
  }
  if (rf & 2) {
    const ctx_r2 = \u0275\u0275nextContext(2);
    \u0275\u0275advance();
    \u0275\u0275textInterpolate(ctx_r2.submitError());
  }
}
function TransactionsComponent_Conditional_14_Conditional_34_Template(rf, ctx) {
  if (rf & 1) {
    \u0275\u0275element(0, "span", 54);
  }
}
function TransactionsComponent_Conditional_14_Template(rf, ctx) {
  if (rf & 1) {
    const _r7 = \u0275\u0275getCurrentView();
    \u0275\u0275elementStart(0, "div", 35);
    \u0275\u0275listener("click", function TransactionsComponent_Conditional_14_Template_div_click_0_listener() {
      \u0275\u0275restoreView(_r7);
      const ctx_r2 = \u0275\u0275nextContext();
      return \u0275\u0275resetView(ctx_r2.closeSheet());
    });
    \u0275\u0275elementStart(1, "div", 36);
    \u0275\u0275listener("click", function TransactionsComponent_Conditional_14_Template_div_click_1_listener($event) {
      \u0275\u0275restoreView(_r7);
      return \u0275\u0275resetView($event.stopPropagation());
    });
    \u0275\u0275element(2, "div", 37);
    \u0275\u0275elementStart(3, "h3", 38);
    \u0275\u0275text(4, "Add Transaction");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(5, "form", 39);
    \u0275\u0275listener("ngSubmit", function TransactionsComponent_Conditional_14_Template_form_ngSubmit_5_listener() {
      \u0275\u0275restoreView(_r7);
      const ctx_r2 = \u0275\u0275nextContext();
      return \u0275\u0275resetView(ctx_r2.submit());
    });
    \u0275\u0275elementStart(6, "div", 40);
    \u0275\u0275repeaterCreate(7, TransactionsComponent_Conditional_14_For_8_Template, 2, 4, "button", 41, \u0275\u0275repeaterTrackByIdentity);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(9, "div", 42)(10, "label", 43);
    \u0275\u0275text(11, "Description");
    \u0275\u0275elementEnd();
    \u0275\u0275element(12, "input", 44);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(13, "div", 42)(14, "label", 43);
    \u0275\u0275text(15, "Amount (\u20B9)");
    \u0275\u0275elementEnd();
    \u0275\u0275element(16, "input", 45);
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(17, "div", 46)(18, "div", 47)(19, "label", 43);
    \u0275\u0275text(20, "Category");
    \u0275\u0275elementEnd();
    \u0275\u0275elementStart(21, "select", 48);
    \u0275\u0275repeaterCreate(22, TransactionsComponent_Conditional_14_For_23_Template, 2, 2, "option", 49, \u0275\u0275repeaterTrackByIdentity);
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(24, "div", 47)(25, "label", 43);
    \u0275\u0275text(26, "Date");
    \u0275\u0275elementEnd();
    \u0275\u0275element(27, "input", 50);
    \u0275\u0275elementEnd()();
    \u0275\u0275elementStart(28, "div", 42)(29, "label", 43);
    \u0275\u0275text(30, "Note (optional)");
    \u0275\u0275elementEnd();
    \u0275\u0275element(31, "input", 51);
    \u0275\u0275elementEnd();
    \u0275\u0275template(32, TransactionsComponent_Conditional_14_Conditional_32_Template, 2, 1, "div", 52);
    \u0275\u0275elementStart(33, "button", 53);
    \u0275\u0275template(34, TransactionsComponent_Conditional_14_Conditional_34_Template, 1, 0, "span", 54);
    \u0275\u0275text(35, " Confirm ");
    \u0275\u0275elementEnd()()()();
  }
  if (rf & 2) {
    const ctx_r2 = \u0275\u0275nextContext();
    \u0275\u0275advance(5);
    \u0275\u0275property("formGroup", ctx_r2.form);
    \u0275\u0275advance(2);
    \u0275\u0275repeater(\u0275\u0275pureFunction0(4, _c1));
    \u0275\u0275advance(15);
    \u0275\u0275repeater(ctx_r2.currentCategories());
    \u0275\u0275advance(10);
    \u0275\u0275conditional(32, ctx_r2.submitError() ? 32 : -1);
    \u0275\u0275advance();
    \u0275\u0275property("disabled", ctx_r2.submitting());
    \u0275\u0275advance();
    \u0275\u0275conditional(34, ctx_r2.submitting() ? 34 : -1);
  }
}
var CATEGORIES = {
  EXPENSE: ["Food & Dining", "Groceries", "Transport", "Shopping", "Healthcare", "Entertainment", "Utilities", "Others"],
  INCOME: ["Salary", "Freelance", "Business", "Investment Returns", "Others"],
  INVESTMENT: ["Mutual Fund", "Stocks", "FD/RD", "NPS", "PPF", "Gold", "Others"],
  SAVINGS: ["Emergency Fund", "Goal Savings", "Others"],
  TRANSFER: ["Transfer"]
};
var TransactionsComponent = class _TransactionsComponent {
  constructor(txService, walletService, toastService, fb) {
    this.txService = txService;
    this.walletService = walletService;
    this.toastService = toastService;
    this.fb = fb;
    this.transactions = signal([]);
    this.page = signal({ content: [], pageNumber: 0, pageSize: 20, totalElements: 0, totalPages: 0, first: true, last: true, empty: true });
    this.summary = signal(null);
    this.loading = signal(false);
    this.activeType = signal("ALL");
    this.openSheet = signal(false);
    this.submitting = signal(false);
    this.submitError = signal("");
    this.types = ["ALL", "EXPENSE", "INCOME", "INVESTMENT", "SAVINGS"];
    this.currentCategories = signal(CATEGORIES.EXPENSE);
    this.form = this.fb.group({
      type: ["EXPENSE"],
      name: ["", Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      category: [CATEGORIES.EXPENSE[0]],
      txDate: [(/* @__PURE__ */ new Date()).toISOString().slice(0, 10)],
      note: [""]
    });
  }
  ngOnInit() {
    this.loadTransactions();
    this.loadSummary();
  }
  loadTransactions(append = false) {
    this.loading.set(true);
    const pg = append ? this.page().pageNumber + 1 : 0;
    const type = this.activeType() !== "ALL" ? this.activeType() : void 0;
    this.txService.getTransactions({ type, page: pg }).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.page.set(res.data);
          if (append)
            this.transactions.update((t) => [...t, ...res.data.content]);
          else
            this.transactions.set(res.data.content);
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
  loadSummary() {
    this.txService.getMonthlySummary().subscribe({
      next: (res) => {
        if (res.success && res.data)
          this.summary.set(res.data);
      },
      error: () => {
      }
    });
  }
  setType(t) {
    this.activeType.set(t);
    this.loadTransactions();
  }
  loadMore() {
    this.loadTransactions(true);
  }
  setFormType(t) {
    this.form.patchValue({ type: t, category: CATEGORIES[t]?.[0] ?? "" });
    this.currentCategories.set(CATEGORIES[t] ?? []);
  }
  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.submitError.set("");
    const wallet = this.walletService.wallet();
    this.txService.create(__spreadProps(__spreadValues({}, this.form.value), { walletId: wallet?.id })).subscribe({
      next: (res) => {
        this.submitting.set(false);
        if (res.success) {
          this.closeSheet();
          this.toastService.success("Transaction added \u2705");
          this.loadTransactions();
          this.loadSummary();
        } else {
          this.submitError.set(res.message || "Failed to add transaction");
        }
      },
      error: (err) => {
        this.submitting.set(false);
        this.submitError.set(err.error?.message || "Network error");
      }
    });
  }
  deleteTransaction(id) {
    if (!confirm("Delete this transaction?"))
      return;
    this.txService.remove(id).subscribe({
      next: (res) => {
        if (res.success) {
          this.transactions.update((t) => t.filter((tx) => tx.id !== id));
          this.toastService.success("Deleted");
          this.loadSummary();
        }
      },
      error: () => this.toastService.error("Failed to delete")
    });
  }
  closeSheet() {
    this.openSheet.set(false);
    this.form.reset({
      type: "EXPENSE",
      category: CATEGORIES.EXPENSE[0],
      txDate: (/* @__PURE__ */ new Date()).toISOString().slice(0, 10)
    });
    this.currentCategories.set(CATEGORIES.EXPENSE);
  }
  getCatIcon(cat) {
    const m = {
      "Food & Dining": "\u{1F355}",
      Groceries: "\u{1F6D2}",
      Transport: "\u{1F697}",
      Shopping: "\u{1F6CD}\uFE0F",
      Healthcare: "\u{1F48A}",
      Entertainment: "\u{1F3AC}",
      Salary: "\u{1F4B0}",
      "Mutual Fund": "\u{1F4CA}",
      Stocks: "\u{1F4C8}",
      "FD/RD": "\u{1F3E6}",
      NPS: "\u{1F3DB}\uFE0F",
      Gold: "\u{1F947}",
      Others: "\u{1F4E6}"
    };
    return m[cat] ?? "\u{1F4B3}";
  }
  static {
    this.\u0275fac = function TransactionsComponent_Factory(t) {
      return new (t || _TransactionsComponent)(\u0275\u0275directiveInject(TransactionService), \u0275\u0275directiveInject(WalletService), \u0275\u0275directiveInject(ToastService), \u0275\u0275directiveInject(FormBuilder));
    };
  }
  static {
    this.\u0275cmp = /* @__PURE__ */ \u0275\u0275defineComponent({ type: _TransactionsComponent, selectors: [["app-transactions"]], standalone: true, features: [\u0275\u0275StandaloneFeature], decls: 15, vars: 3, consts: [[1, "page"], [1, "page-header"], [1, "page-title"], [1, "btn", "btn-primary", "btn-sm", 3, "click"], [1, "filter-bar"], [1, "filter-chip", 3, "ngClass"], [1, "summary-banner", "card", "card-sm"], [1, "sheet-backdrop"], [1, "filter-chip", 3, "click", "ngClass"], [1, "sb-item"], [1, "sb-label"], [1, "sb-val", "text-success"], [1, "sb-divider"], [1, "sb-val", "text-danger"], [1, "sb-val", 3, "ngClass"], [1, "skeleton", 2, "height", "60px", "margin", "8px 0"], [1, "empty-state"], [1, "empty-icon"], [1, "empty-title"], [1, "empty-body"], [1, "tx-list"], [1, "tx-row", "card", "card-sm"], [1, "btn", "btn-ghost", "btn-full"], [1, "tx-icon-wrap"], [1, "tx-info"], [1, "tx-name"], [1, "tx-sub"], [1, "tx-cat"], [1, "tx-date"], [1, "badge", "badge-danger"], [1, "badge", "badge-warning"], [1, "tx-amount-col"], [3, "ngClass"], ["aria-label", "Delete", 1, "del-btn", 3, "click"], [1, "btn", "btn-ghost", "btn-full", 3, "click"], [1, "sheet-backdrop", 3, "click"], [1, "sheet", 3, "click"], [1, "sheet-handle"], [1, "sheet-title"], [3, "ngSubmit", "formGroup"], [1, "type-tabs"], ["type", "button", 1, "type-tab", 3, "ngClass"], [1, "form-group"], [1, "form-label"], ["formControlName", "name", "placeholder", "e.g. Swiggy order, SBI FD...", 1, "form-input"], ["formControlName", "amount", "type", "number", "inputmode", "decimal", "placeholder", "0.00", 1, "form-input"], [1, "sheet-row"], [1, "form-group", 2, "flex", "1"], ["formControlName", "category", 1, "form-input"], [3, "value"], ["type", "date", "formControlName", "txDate", 1, "form-input"], ["formControlName", "note", "placeholder", "Any notes...", 1, "form-input"], [1, "alert", "alert-error"], ["type", "submit", 1, "btn", "btn-primary", "btn-full", 3, "disabled"], [1, "spinner"], ["type", "button", 1, "type-tab", 3, "click", "ngClass"]], template: function TransactionsComponent_Template(rf, ctx) {
      if (rf & 1) {
        \u0275\u0275elementStart(0, "div", 0);
        \u0275\u0275element(1, "app-toast");
        \u0275\u0275elementStart(2, "header", 1)(3, "h1", 2);
        \u0275\u0275text(4, "Transactions");
        \u0275\u0275elementEnd();
        \u0275\u0275elementStart(5, "button", 3);
        \u0275\u0275listener("click", function TransactionsComponent_Template_button_click_5_listener() {
          return ctx.openSheet.set(true);
        });
        \u0275\u0275text(6, "+ Add");
        \u0275\u0275elementEnd()();
        \u0275\u0275elementStart(7, "div", 4);
        \u0275\u0275repeaterCreate(8, TransactionsComponent_For_9_Template, 2, 4, "button", 5, \u0275\u0275repeaterTrackByIdentity);
        \u0275\u0275elementEnd();
        \u0275\u0275template(10, TransactionsComponent_Conditional_10_Template, 20, 10, "div", 6)(11, TransactionsComponent_Conditional_11_Template, 3, 0)(12, TransactionsComponent_Conditional_12_Template, 7, 0)(13, TransactionsComponent_Conditional_13_Template, 4, 1);
        \u0275\u0275elementEnd();
        \u0275\u0275template(14, TransactionsComponent_Conditional_14_Template, 36, 5, "div", 7);
      }
      if (rf & 2) {
        \u0275\u0275advance(8);
        \u0275\u0275repeater(ctx.types);
        \u0275\u0275advance(2);
        \u0275\u0275conditional(10, ctx.summary() ? 10 : -1);
        \u0275\u0275advance();
        \u0275\u0275conditional(11, ctx.loading() ? 11 : ctx.transactions().length === 0 ? 12 : 13);
        \u0275\u0275advance(3);
        \u0275\u0275conditional(14, ctx.openSheet() ? 14 : -1);
      }
    }, dependencies: [ReactiveFormsModule, \u0275NgNoValidate, NgSelectOption, \u0275NgSelectMultipleOption, DefaultValueAccessor, NumberValueAccessor, SelectControlValueAccessor, NgControlStatus, NgControlStatusGroup, FormGroupDirective, FormControlName, NgClass, ToastComponent, InrFormatPipe, RelativeDatePipe], styles: ["\n\n.filter-bar[_ngcontent-%COMP%] {\n  display: flex;\n  gap: 8px;\n  overflow-x: auto;\n  padding: 4px 0 10px;\n  scrollbar-width: none;\n}\n.filter-bar[_ngcontent-%COMP%]::-webkit-scrollbar {\n  display: none;\n}\n.filter-chip[_ngcontent-%COMP%] {\n  padding: 7px 14px;\n  border-radius: 99px;\n  border: 1.5px solid var(--color-border);\n  background: var(--color-card);\n  font-size: 12px;\n  font-weight: 700;\n  cursor: pointer;\n  white-space: nowrap;\n  color: var(--color-text-secondary);\n}\n.filter-chip.active[_ngcontent-%COMP%] {\n  background: var(--color-primary);\n  border-color: var(--color-primary);\n  color: #fff;\n}\n.summary-banner[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  margin-bottom: 12px;\n  padding: 12px 16px;\n}\n.sb-item[_ngcontent-%COMP%] {\n  flex: 1;\n  text-align: center;\n}\n.sb-label[_ngcontent-%COMP%] {\n  font-size: 11px;\n  color: var(--color-text-muted);\n  font-weight: 600;\n  text-transform: uppercase;\n}\n.sb-val[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 16px;\n  font-weight: 900;\n}\n.sb-divider[_ngcontent-%COMP%] {\n  width: 1px;\n  height: 32px;\n  background: var(--color-border);\n}\n.tx-list[_ngcontent-%COMP%] {\n  display: flex;\n  flex-direction: column;\n  gap: 8px;\n  margin-bottom: 16px;\n}\n.tx-row[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 10px;\n}\n.tx-icon-wrap[_ngcontent-%COMP%] {\n  width: 40px;\n  height: 40px;\n  background: var(--color-card-alt);\n  border-radius: 12px;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  font-size: 18px;\n  flex-shrink: 0;\n}\n.tx-info[_ngcontent-%COMP%] {\n  flex: 1;\n  min-width: 0;\n}\n.tx-name[_ngcontent-%COMP%] {\n  font-weight: 600;\n  font-size: 14px;\n  white-space: nowrap;\n  overflow: hidden;\n  text-overflow: ellipsis;\n}\n.tx-sub[_ngcontent-%COMP%] {\n  display: flex;\n  align-items: center;\n  gap: 6px;\n  margin-top: 2px;\n}\n.tx-cat[_ngcontent-%COMP%] {\n  font-size: 11px;\n  color: var(--color-text-muted);\n}\n.tx-date[_ngcontent-%COMP%] {\n  font-size: 11px;\n  color: var(--color-text-muted);\n}\n.tx-amount-col[_ngcontent-%COMP%] {\n  display: flex;\n  flex-direction: column;\n  align-items: flex-end;\n  gap: 4px;\n}\n.tx-amount-col[_ngcontent-%COMP%]   span[_ngcontent-%COMP%] {\n  font-family: var(--font-heading);\n  font-size: 15px;\n  font-weight: 900;\n}\n.del-btn[_ngcontent-%COMP%] {\n  background: none;\n  border: none;\n  cursor: pointer;\n  font-size: 14px;\n  padding: 0;\n  opacity: 0.5;\n}\n.del-btn[_ngcontent-%COMP%]:hover {\n  opacity: 1;\n}\n.alert[_ngcontent-%COMP%] {\n  padding: 10px 14px;\n  border-radius: 10px;\n  font-size: 13px;\n  font-weight: 600;\n  margin-bottom: 12px;\n}\n.alert-error[_ngcontent-%COMP%] {\n  background: var(--color-danger-light);\n  color: var(--color-danger);\n}\n.type-tabs[_ngcontent-%COMP%] {\n  display: flex;\n  gap: 6px;\n  margin-bottom: 16px;\n  overflow-x: auto;\n  scrollbar-width: none;\n}\n.type-tabs[_ngcontent-%COMP%]::-webkit-scrollbar {\n  display: none;\n}\n.type-tab[_ngcontent-%COMP%] {\n  padding: 7px 12px;\n  border-radius: 10px;\n  border: 1.5px solid var(--color-border);\n  background: var(--color-card);\n  font-size: 12px;\n  font-weight: 700;\n  cursor: pointer;\n  color: var(--color-text-secondary);\n  white-space: nowrap;\n}\n.type-tab.active[_ngcontent-%COMP%] {\n  background: var(--color-primary);\n  border-color: var(--color-primary);\n  color: #fff;\n}\n.sheet-row[_ngcontent-%COMP%] {\n  display: flex;\n  gap: 10px;\n}\n/*# sourceMappingURL=transactions.component.css.map */"], changeDetection: 0 });
  }
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && \u0275setClassDebugInfo(TransactionsComponent, { className: "TransactionsComponent", filePath: "src/app/features/transactions/transactions.component.ts", lineNumber: 202 });
})();
export {
  TransactionsComponent
};
//# sourceMappingURL=chunk-Q5QECBKZ.js.map
