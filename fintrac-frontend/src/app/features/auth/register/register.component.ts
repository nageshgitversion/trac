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
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule],
  styles: [`
    .wrapper { display:flex; justify-content:center; align-items:center; min-height:100vh; background:#e8eaf6; }
    mat-card { width:400px; padding:32px; }
    h2 { text-align:center; margin-bottom:24px; color:#3f51b5; }
    mat-form-field { width:100%; margin-bottom:8px; }
    button[type=submit] { width:100%; margin-top:8px; }
    .error { color:#f44336; font-size:13px; margin-bottom:8px; }
    .link { text-align:center; margin-top:16px; font-size:13px; }
  `],
  template: `
    <div class="wrapper">
      <mat-card>
        <h2>Create Account</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline">
            <mat-label>Full Name</mat-label>
            <input matInput formControlName="name" />
          </mat-form-field>
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
            <span *ngIf="!loading">Register</span>
            <mat-spinner *ngIf="loading" diameter="20" style="display:inline-block" />
          </button>
        </form>
        <div class="link">Already have an account? <a routerLink="/login">Login</a></div>
      </mat-card>
    </div>
  `
})
export class RegisterComponent {
  form: FormGroup;
  loading = false;
  error = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  submit() {
    if (this.form.invalid) return;
    this.loading = true; this.error = '';
    this.authService.register(this.form.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (e) => { this.error = e.error?.message || 'Registration failed'; this.loading = false; }
    });
  }
}
