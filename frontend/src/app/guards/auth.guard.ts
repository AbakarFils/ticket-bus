import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  if (inject(AuthService).isAuthenticated()) return true;
  inject(Router).navigate(['/login']);
  return false;
};
