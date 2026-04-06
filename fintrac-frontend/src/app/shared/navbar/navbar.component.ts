import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, MatToolbarModule, MatButtonModule, MatIconModule, RouterLink],
  template: `
    <mat-toolbar color="primary" style="justify-content:space-between">
      <span style="font-weight:bold;font-size:20px">💰 FinTrac</span>
      <div style="display:flex;align-items:center;gap:12px">
        <span>{{ authService.userName() }}</span>
        <button mat-icon-button (click)="logout()" title="Logout">
          <mat-icon>logout</mat-icon>
        </button>
      </div>
    </mat-toolbar>
  `
})
export class NavbarComponent {
  authService = inject(AuthService);
  constructor(private router: Router) {}
  logout() { this.authService.logout(); }
}
