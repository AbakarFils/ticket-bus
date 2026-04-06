import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
<<<<<<< HEAD
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ApiService, Customer } from '../core/api.service';
=======
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { ApiService, Customer, TripHistory } from '../core/api.service';
>>>>>>> 6a79295c (phase 3)

@Component({
  selector: 'app-customers',
  standalone: true,
<<<<<<< HEAD
  imports: [CommonModule, MatTableModule, MatButtonModule, MatCardModule, MatIconModule],
  template: `
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
      <h2 style="margin:0">Gestion des clients</h2>
      <button mat-raised-button color="primary" (click)="load()">
        <mat-icon>refresh</mat-icon> Actualiser
      </button>
    </div>

    <mat-card style="margin-bottom:16px">
      <mat-card-content style="display:flex; gap:24px">
        <span><strong>Total :</strong> {{ customers.length }}</span>
        <span style="color:green"><strong>Actifs :</strong> {{ activeCount }}</span>
        <span style="color:red"><strong>Inactifs :</strong> {{ customers.length - activeCount }}</span>
      </mat-card-content>
    </mat-card>

    <table mat-table [dataSource]="customers" style="width:100%">
=======
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatTableModule, MatIconModule,
    MatSnackBarModule, MatDialogModule
  ],
  template: `
    <h2>👤 Gestion des Clients</h2>

    <mat-card style="max-width:600px; margin-bottom:24px">
      <mat-card-header><mat-card-title>Nouveau client</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="createForm" (ngSubmit)="onCreate()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end">
          <mat-form-field><mat-label>Email</mat-label><input matInput formControlName="email"></mat-form-field>
          <mat-form-field><mat-label>Prénom</mat-label><input matInput formControlName="firstName"></mat-form-field>
          <mat-form-field><mat-label>Nom</mat-label><input matInput formControlName="lastName"></mat-form-field>
          <mat-form-field><mat-label>Téléphone</mat-label><input matInput formControlName="phone"></mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="createForm.invalid">Créer</button>
        </form>
      </mat-card-content>
    </mat-card>

    <table mat-table [dataSource]="customers" style="width:100%" *ngIf="customers.length > 0">
>>>>>>> 6a79295c (phase 3)
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef>ID</th>
        <td mat-cell *matCellDef="let c">{{ c.id }}</td>
      </ng-container>
<<<<<<< HEAD
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef>Nom</th>
        <td mat-cell *matCellDef="let c">{{ c.firstName }} {{ c.lastName }}</td>
      </ng-container>
=======
>>>>>>> 6a79295c (phase 3)
      <ng-container matColumnDef="email">
        <th mat-header-cell *matHeaderCellDef>Email</th>
        <td mat-cell *matCellDef="let c">{{ c.email }}</td>
      </ng-container>
<<<<<<< HEAD
      <ng-container matColumnDef="phone">
        <th mat-header-cell *matHeaderCellDef>Téléphone</th>
        <td mat-cell *matCellDef="let c">{{ c.phone || '—' }}</td>
=======
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef>Nom</th>
        <td mat-cell *matCellDef="let c">{{ c.firstName }} {{ c.lastName }}</td>
      </ng-container>
      <ng-container matColumnDef="phone">
        <th mat-header-cell *matHeaderCellDef>Téléphone</th>
        <td mat-cell *matCellDef="let c">{{ c.phone }}</td>
>>>>>>> 6a79295c (phase 3)
      </ng-container>
      <ng-container matColumnDef="role">
        <th mat-header-cell *matHeaderCellDef>Rôle</th>
        <td mat-cell *matCellDef="let c">{{ c.role }}</td>
      </ng-container>
<<<<<<< HEAD
      <ng-container matColumnDef="active">
        <th mat-header-cell *matHeaderCellDef>Statut</th>
        <td mat-cell *matCellDef="let c" [style.color]="c.active ? 'green' : 'red'">
          {{ c.active ? 'Actif' : 'Inactif' }}
        </td>
      </ng-container>
      <ng-container matColumnDef="createdAt">
        <th mat-header-cell *matHeaderCellDef>Créé le</th>
        <td mat-cell *matCellDef="let c">{{ c.createdAt | date:'short' }}</td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let c">
          <button mat-icon-button color="warn" (click)="deactivate(c)" [disabled]="!c.active" title="Désactiver">
            <mat-icon>block</mat-icon>
          </button>
        </td>
      </ng-container>

=======
      <ng-container matColumnDef="createdAt">
        <th mat-header-cell *matHeaderCellDef>Créé le</th>
        <td mat-cell *matCellDef="let c">{{ c.createdAt | date:'medium' }}</td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Trajets</th>
        <td mat-cell *matCellDef="let c">
          <button mat-icon-button color="primary" (click)="loadTrips(c.id)"><mat-icon>directions_bus</mat-icon></button>
        </td>
      </ng-container>
>>>>>>> 6a79295c (phase 3)
      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>

<<<<<<< HEAD
    <p *ngIf="customers.length === 0" style="color:#999; text-align:center; padding:24px">
      Aucun client trouvé
    </p>
    <p *ngIf="errorMsg" style="color:red; margin-top:8px">{{ errorMsg }}</p>
=======
    <p *ngIf="customers.length === 0" style="color:#999;text-align:center;padding:16px">Aucun client</p>

    <div *ngIf="trips.length > 0" style="margin-top:24px">
      <h3>Historique trajets — Utilisateur #{{ selectedUserId }}</h3>
      <table mat-table [dataSource]="trips" style="width:100%">
        <ng-container matColumnDef="tid"><th mat-header-cell *matHeaderCellDef>ID</th><td mat-cell *matCellDef="let t">{{ t.id }}</td></ng-container>
        <ng-container matColumnDef="ticketId"><th mat-header-cell *matHeaderCellDef>Ticket</th><td mat-cell *matCellDef="let t">{{ t.ticketId }}</td></ng-container>
        <ng-container matColumnDef="boarding"><th mat-header-cell *matHeaderCellDef>Embarquement</th><td mat-cell *matCellDef="let t">{{ t.boardingZone }}</td></ng-container>
        <ng-container matColumnDef="alighting"><th mat-header-cell *matHeaderCellDef>Débarquement</th><td mat-cell *matCellDef="let t">{{ t.alightingZone }}</td></ng-container>
        <ng-container matColumnDef="timestamp"><th mat-header-cell *matHeaderCellDef>Date</th><td mat-cell *matCellDef="let t">{{ t.timestamp | date:'medium' }}</td></ng-container>
        <tr mat-header-row *matHeaderRowDef="tripColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: tripColumns"></tr>
      </table>
    </div>
>>>>>>> 6a79295c (phase 3)
  `
})
export class CustomersComponent implements OnInit {
  customers: Customer[] = [];
<<<<<<< HEAD
  columns = ['id', 'name', 'email', 'phone', 'role', 'active', 'createdAt', 'actions'];
  errorMsg = '';

  get activeCount(): number {
    return this.customers.filter(c => c.active).length;
  }

  constructor(private api: ApiService) {}

=======
  trips: TripHistory[] = [];
  selectedUserId: number | null = null;
  columns = ['id', 'email', 'name', 'phone', 'role', 'createdAt', 'actions'];
  tripColumns = ['tid', 'ticketId', 'boarding', 'alighting', 'timestamp'];
  createForm: FormGroup;

  constructor(private api: ApiService, private fb: FormBuilder, private snack: MatSnackBar) {
    this.createForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: [''],
      lastName: [''],
      phone: ['']
    });
  }

>>>>>>> 6a79295c (phase 3)
  ngOnInit(): void {
    this.load();
  }

  load(): void {
<<<<<<< HEAD
    this.errorMsg = '';
    this.api.getCustomers().subscribe({
      next: (c) => this.customers = c,
      error: () => { this.errorMsg = 'Erreur de chargement des clients.'; }
    });
  }

  deactivate(customer: Customer): void {
    if (!confirm(`Désactiver le compte de ${customer.email} ?`)) return;
    this.api.deactivateCustomer(customer.id).subscribe({
      next: () => { customer.active = false; },
      error: () => { this.errorMsg = 'Erreur lors de la désactivation.'; }
    });
  }
}
=======
    this.api.getRecentCustomers().subscribe({
      next: c => this.customers = c,
      error: () => this.snack.open('Erreur de chargement des clients', 'OK', { duration: 3000 })
    });
  }

  onCreate(): void {
    this.api.createCustomer(this.createForm.value).subscribe({
      next: () => { this.load(); this.createForm.reset(); this.snack.open('Client créé', 'OK', { duration: 3000 }); },
      error: () => this.snack.open('Erreur de création', 'OK', { duration: 3000 })
    });
  }

  loadTrips(userId: number): void {
    this.selectedUserId = userId;
    this.api.getCustomerTrips(userId).subscribe({
      next: t => this.trips = t,
      error: () => this.snack.open('Erreur chargement trajets', 'OK', { duration: 3000 })
    });
  }
}

>>>>>>> 6a79295c (phase 3)
