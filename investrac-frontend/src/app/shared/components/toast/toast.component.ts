import { Component, ChangeDetectionStrategy } from '@angular/core';
import { NgClass } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [NgClass],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toast-container">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast" [ngClass]="'toast-' + toast.type" role="alert">
          <span class="toast-icon">{{ getIcon(toast.type) }}</span>
          <span class="toast-msg">{{ toast.message }}</span>
          <button class="toast-close" (click)="toastService.dismiss(toast.id)">✕</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 16px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 8px;
      width: calc(100% - 32px);
      max-width: 390px;
      pointer-events: none;
    }
    .toast {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 14px;
      border-radius: 14px;
      font-size: 14px;
      font-weight: 600;
      box-shadow: 0 8px 24px rgba(0,0,0,0.15);
      animation: slideDown 0.3s cubic-bezier(0.4,0,0.2,1);
      pointer-events: all;
    }
    @keyframes slideDown {
      from { opacity: 0; transform: translateY(-12px); }
      to   { opacity: 1; transform: translateY(0); }
    }
    .toast-success { background: #ECFDF5; color: #065F46; border: 1.5px solid #A7F3D0; }
    .toast-error   { background: #FEF2F2; color: #991B1B; border: 1.5px solid #FECACA; }
    .toast-warning { background: #FFFBEB; color: #92400E; border: 1.5px solid #FDE68A; }
    .toast-info    { background: #EFF6FF; color: #1E40AF; border: 1.5px solid #BFDBFE; }
    .toast-icon    { font-size: 16px; flex-shrink: 0; }
    .toast-msg     { flex: 1; }
    .toast-close   { background: none; border: none; cursor: pointer; opacity: 0.6; font-size: 12px; padding: 2px; margin-left: auto; }
  `]
})
export class ToastComponent {
  constructor(readonly toastService: ToastService) {}

  getIcon(type: string): string {
    return { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' }[type] ?? 'ℹ️';
  }
}
