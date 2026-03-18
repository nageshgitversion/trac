import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Protects routes that require authentication.
 * Redirects to /auth/login if not logged in.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  if (authService.isLoggedIn()) return true;

  router.navigate(['/auth/login'], {
    queryParams: { returnUrl: state.url }
  });
  return false;
};

/**
 * Prevents logged-in users from accessing auth pages.
 * Redirects to home if already logged in.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router      = inject(Router);

  if (!authService.isLoggedIn()) return true;

  router.navigate(['/home']);
  return false;
};
