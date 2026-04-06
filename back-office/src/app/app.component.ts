import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatDividerModule],
  template: `
    <mat-toolbar color="primary">
      <span>🚌 TicketBus Back-office</span>
    </mat-toolbar>
    <mat-sidenav-container style="height: calc(100vh - 64px)">
      <mat-sidenav mode="side" opened style="width:220px; padding-top:8px">
        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard" routerLinkActive="active-link">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
<<<<<<< HEAD
          <mat-divider></mat-divider>
          <a mat-list-item routerLink="/customers" routerLinkActive="active-link">
            <mat-icon matListItemIcon>people</mat-icon>
            <span matListItemTitle>Clients</span>
=======
          <a mat-list-item routerLink="/products" routerLinkActive="active-link">
            <mat-icon matListItemIcon>local_offer</mat-icon>
            <span matListItemTitle>Produits</span>
          </a>
          <a mat-list-item routerLink="/tickets" routerLinkActive="active-link">
            <mat-icon matListItemIcon>confirmation_number</mat-icon>
            <span matListItemTitle>Tickets</span>
          </a>
          <a mat-list-item routerLink="/events" routerLinkActive="active-link">
            <mat-icon matListItemIcon>qr_code_scanner</mat-icon>
            <span matListItemTitle>Validations</span>
>>>>>>> 6a79295c (phase 3)
          </a>
          <a mat-list-item routerLink="/wallets" routerLinkActive="active-link">
            <mat-icon matListItemIcon>account_balance_wallet</mat-icon>
            <span matListItemTitle>Wallets</span>
          </a>
<<<<<<< HEAD
          <a mat-list-item routerLink="/products" routerLinkActive="active-link">
            <mat-icon matListItemIcon>local_offer</mat-icon>
            <span matListItemTitle>Produits</span>
          </a>
          <mat-divider></mat-divider>
          <a mat-list-item routerLink="/events" routerLinkActive="active-link">
            <mat-icon matListItemIcon>qr_code_scanner</mat-icon>
            <span matListItemTitle>Validations</span>
          </a>
          <a mat-list-item routerLink="/transactions" routerLinkActive="active-link">
            <mat-icon matListItemIcon>receipt_long</mat-icon>
            <span matListItemTitle>Transactions</span>
          </a>
          <mat-divider></mat-divider>
          <a mat-list-item routerLink="/fraud" routerLinkActive="active-link">
            <mat-icon matListItemIcon>gpp_bad</mat-icon>
            <span matListItemTitle>Fraude</span>
=======
          <a mat-list-item routerLink="/payments" routerLinkActive="active-link">
            <mat-icon matListItemIcon>payment</mat-icon>
            <span matListItemTitle>Paiements</span>
          </a>
          <a mat-list-item routerLink="/fraud-alerts" routerLinkActive="active-link">
            <mat-icon matListItemIcon>warning</mat-icon>
            <span matListItemTitle>Alertes Fraude</span>
          </a>
          <a mat-list-item routerLink="/customers" routerLinkActive="active-link">
            <mat-icon matListItemIcon>people</mat-icon>
            <span matListItemTitle>Clients</span>
          </a>
          <a mat-list-item routerLink="/operators" routerLinkActive="active-link">
            <mat-icon matListItemIcon>business</mat-icon>
            <span matListItemTitle>Opérateurs</span>
>>>>>>> 6a79295c (phase 3)
          </a>
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content style="padding:24px">
        <router-outlet></router-outlet>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`.active-link { background: rgba(0,0,0,0.08); }`]
})
export class AppComponent {}
