import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  template: `
    <h2>Settings</h2>
    <mat-card style="padding:24px;max-width:500px">
      <div style="display:flex;align-items:center;gap:16px;margin-bottom:24px">
        <mat-icon style="font-size:48px;width:48px;height:48px;color:#3f51b5">account_circle</mat-icon>
        <div>
          <div style="font-size:20px;font-weight:500">{{ authService.userName() }}</div>
          <div style="color:#666">{{ authService.user()?.email }}</div>
        </div>
      </div>
      <div style="color:#666;font-size:14px">User ID: {{ authService.userId() }}</div>
    </mat-card>
  `
})
export class SettingsComponent {
  authService = inject(AuthService);
}
