import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formats a number in Indian Rupee format.
 *
 * Examples:
 *   1150000   → ₹11,50,000
 *   115000    → ₹1,15,000
 *   1860000   → ₹18.6L  (when showLakhs=true)
 *   28400000  → ₹2.84Cr (when showLakhs=true and > 10L)
 */
@Pipe({ name: 'inr', standalone: true, pure: true })
export class InrFormatPipe implements PipeTransform {

  transform(value: number | null | undefined, showLakhs = false): string {
    if (value == null) return '₹0';

    if (showLakhs) {
      if (Math.abs(value) >= 10_000_000) {
        return '₹' + (value / 10_000_000).toFixed(2).replace(/\.?0+$/, '') + 'Cr';
      }
      if (Math.abs(value) >= 100_000) {
        return '₹' + (value / 100_000).toFixed(1).replace(/\.0$/, '') + 'L';
      }
      if (Math.abs(value) >= 1_000) {
        return '₹' + (value / 1_000).toFixed(1).replace(/\.0$/, '') + 'K';
      }
    }

    // Indian numbering: xx,xx,xxx
    const abs = Math.abs(Math.round(value));
    const str = abs.toString();
    let result = '';

    if (str.length <= 3) {
      result = str;
    } else {
      const last3 = str.slice(-3);
      const remaining = str.slice(0, -3);
      result = remaining.replace(/\B(?=(\d{2})+(?!\d))/g, ',') + ',' + last3;
    }

    return (value < 0 ? '-₹' : '₹') + result;
  }
}
