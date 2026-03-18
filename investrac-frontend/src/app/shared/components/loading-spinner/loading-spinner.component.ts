import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="loading-wrap" [class.full-screen]="fullScreen">
      <div class="spinner-ring"></div>
      @if (message) { <p class="loading-msg">{{ message }}</p> }
    </div>
  `,
  styles: [`
    .loading-wrap {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px;
      gap: 12px;
    }
    .loading-wrap.full-screen {
      position: fixed;
      inset: 0;
      background: var(--color-bg);
      z-index: 500;
    }
    .spinner-ring {
      width: 36px; height: 36px;
      border: 3px solid var(--color-border);
      border-top-color: var(--color-primary);
      border-radius: 50%;
      animation: spin 0.7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
    .loading-msg { font-size: 14px; color: var(--color-text-muted); font-weight: 600; }
  `]
})
export class LoadingSpinnerComponent {
  @Input() fullScreen = false;
  @Input() message    = '';
}
