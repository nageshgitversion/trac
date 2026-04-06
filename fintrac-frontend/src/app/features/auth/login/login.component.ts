import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule],
  styles: [`
    .login-wrapper { display:flex; justify-content:center; align-items:center; min-height:100vh; background:#e8eaf6; }
    mat-card { width:380px; padding:32px; }
    h2 { text-align:center; margin-bottom:24px; color:#3f51b5; }
    mat-form-field { width:100%; margin-bottom:8px; }
    button[type=submit] { width:100%; margin-top:8px; }
    .error { color:#f44336; font-size:13px; margin-bottom:8px; }
    .register-link { text-align:center; margin-top:16px; font-size:13px; }
  `],
  template: `
    <div class="login-wrapper">
      <mat-card>
        <h2>💰 FinTrac Login</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline">
            <mat-label>Email</mat-label>
            <input matInput type="email" formControlName="email" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Password</mat-label>
            <input matInput type="password" formControlName="password" />
          </mat-form-field>
          <div *ngIf="error" class="error">{{ error }}</div>
          <button mat-raised-button color="primary" type="submit" [disabled]="loading || form.invalid">
            <span *ngIf="!loading">Login</span>
            <mat-spinner *ngIf="loading" diameter="20" style="display:inline-block" />
          </button>
        </form>
        <div class="register-link">Don't have an account? <a routerLink="/register">Register</a></div>
      </mat-card>
    </div>
  `
})
export class LoginComponent {
  form: FormGroup;
  loading = false;
  error = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  submit() {
    if (this.form.invalid) return;
    this.loading = true; this.error = '';
    this.authService.login(this.form.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (e) => { this.error = e.error?.message || 'Login failed'; this.loading = false; }
    });
  }
}
