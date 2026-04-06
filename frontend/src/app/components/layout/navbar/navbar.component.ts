import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { OfflineService } from '../../../services/offline.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, AsyncPipe],
  template: `
    <nav class="navbar">
      <a routerLink="/" class="brand">🚌 TicketBus</a>
      <div class="nav-links">
        <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}">Dashboard</a>
        <a routerLink="/tickets" routerLinkActive="active">Tickets</a>
        <a routerLink="/validation" routerLinkActive="active">Validation</a>
        <a routerLink="/sync" routerLinkActive="active">Sync</a>
      </div>
      <div class="nav-right">
        <span class="connectivity">
          <span class="status-dot" [class.online]="(offline.isOnline | async)" [class.offline]="!(offline.isOnline | async)"></span>
          {{ (offline.isOnline | async) ? 'Online' : 'Offline' }}
        </span>
        @if (auth.getCurrentUser(); as user) {
          <span class="user-info">{{ user.username }} <span class="badge badge-role">{{ user.role }}</span></span>
          <button class="btn btn-sm btn-secondary" (click)="auth.logout()">Logout</button>
        }
      </div>
    </nav>
  `,
  styles: [`
    .navbar { display: flex; align-items: center; gap: 1.5rem; padding: .75rem 1.5rem; background: #1a1a2e; color: #fff; position: sticky; top: 0; z-index: 100; }
    .brand { color: #fff; font-weight: 700; font-size: 1.1rem; text-decoration: none; margin-right: auto; }
    .nav-links { display: flex; gap: 1rem; }
    .nav-links a { color: #aaa; text-decoration: none; padding: .3rem .5rem; border-radius: 4px; }
    .nav-links a.active { color: #fff; background: rgba(255,255,255,.1); }
    .nav-right { display: flex; align-items: center; gap: 1rem; margin-left: auto; }
    .connectivity { font-size: .85rem; }
    .user-info { font-size: .85rem; }
    .badge-role { background: #4a90e2; color: #fff; }
  `]
})
export class NavbarComponent {
  auth = inject(AuthService);
  offline = inject(OfflineService);
}
