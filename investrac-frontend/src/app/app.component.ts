import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BottomNavComponent } from './shared/components/bottom-nav/bottom-nav.component';
import { ToastComponent } from './shared/components/toast/toast.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, BottomNavComponent, ToastComponent],
  template: `
    <div class="app-shell">
      <router-outlet />
      @if (authService.isLoggedIn()) {
        <app-bottom-nav />
      }
    </div>
  `,
  styles: [`
    :host { display: block; }
    .app-shell {
      width: 100%;
      max-width: 430px;
      min-height: 100dvh;
      margin: 0 auto;
      background: var(--color-bg);
      position: relative;
      overflow-x: hidden;
    }
  `]
})
export class AppComponent {
  constructor(readonly authService: AuthService) {}
}
