import {
  ɵɵdefinePipe
} from "./chunk-TWKZKYET.js";

// src/app/shared/pipes/inr-format.pipe.ts
var InrFormatPipe = class _InrFormatPipe {
  transform(value, showLakhs = false) {
    if (value == null)
      return "\u20B90";
    if (showLakhs) {
      if (Math.abs(value) >= 1e7) {
        return "\u20B9" + (value / 1e7).toFixed(2).replace(/\.?0+$/, "") + "Cr";
      }
      if (Math.abs(value) >= 1e5) {
        return "\u20B9" + (value / 1e5).toFixed(1).replace(/\.0$/, "") + "L";
      }
      if (Math.abs(value) >= 1e3) {
        return "\u20B9" + (value / 1e3).toFixed(1).replace(/\.0$/, "") + "K";
      }
    }
    const abs = Math.abs(Math.round(value));
    const str = abs.toString();
    let result = "";
    if (str.length <= 3) {
      result = str;
    } else {
      const last3 = str.slice(-3);
      const remaining = str.slice(0, -3);
      result = remaining.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + "," + last3;
    }
    return (value < 0 ? "-\u20B9" : "\u20B9") + result;
  }
  static {
    this.\u0275fac = function InrFormatPipe_Factory(t) {
      return new (t || _InrFormatPipe)();
    };
  }
  static {
    this.\u0275pipe = /* @__PURE__ */ \u0275\u0275definePipe({ name: "inr", type: _InrFormatPipe, pure: true, standalone: true });
  }
};

export {
  InrFormatPipe
};
//# sourceMappingURL=chunk-IDOJ465G.js.map
