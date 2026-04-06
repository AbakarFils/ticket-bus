import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./components/auth/login/login.component').then(m => m.LoginComponent) },
  { path: '', loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent), canActivate: [authGuard] },
  { path: 'tickets', loadComponent: () => import('./components/tickets/ticket-list/ticket-list.component').then(m => m.TicketListComponent), canActivate: [authGuard] },
  { path: 'tickets/create', loadComponent: () => import('./components/tickets/ticket-create/ticket-create.component').then(m => m.TicketCreateComponent), canActivate: [authGuard] },
  { path: 'tickets/:id', loadComponent: () => import('./components/tickets/ticket-detail/ticket-detail.component').then(m => m.TicketDetailComponent), canActivate: [authGuard] },
  { path: 'validation', loadComponent: () => import('./components/validation/validation-scan/validation-scan.component').then(m => m.ValidationScanComponent), canActivate: [authGuard] },
  { path: 'validation/events', loadComponent: () => import('./components/validation/validation-events/validation-events.component').then(m => m.ValidationEventsComponent), canActivate: [authGuard] },
  { path: 'sync', loadComponent: () => import('./components/sync/sync-status/sync-status.component').then(m => m.SyncStatusComponent), canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
