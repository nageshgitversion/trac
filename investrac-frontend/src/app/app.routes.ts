import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // Default
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },

  // Auth routes (no guard — but guestGuard redirects logged-in users)
  {
    path: 'auth',
    canActivate: [guestGuard],
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/register/register.component').then(m => m.RegisterComponent)
      },
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
      }
    ]
  },

  // Protected routes (authGuard applied)
  {
    path: 'home',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'transactions',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/transactions/transactions.component').then(m => m.TransactionsComponent)
  },
  {
    path: 'wallet',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/wallet/wallet.component').then(m => m.WalletComponent)
  },
  {
    path: 'portfolio',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/portfolio/portfolio.component').then(m => m.PortfolioComponent)
  },
  {
    path: 'settings',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/settings/settings.component').then(m => m.SettingsComponent)
  },

  // 404
  {
    path: '**',
    redirectTo: 'home'
  }
];
