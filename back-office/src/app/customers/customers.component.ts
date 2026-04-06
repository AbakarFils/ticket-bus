import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ApiService, Customer } from '../core/api.service';

@Component({
  selector: 'app-customers',
  standalone: true,
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
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef>ID</th>
        <td mat-cell *matCellDef="let c">{{ c.id }}</td>
      </ng-container>
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef>Nom</th>
        <td mat-cell *matCellDef="let c">{{ c.firstName }} {{ c.lastName }}</td>
      </ng-container>
      <ng-container matColumnDef="email">
        <th mat-header-cell *matHeaderCellDef>Email</th>
        <td mat-cell *matCellDef="let c">{{ c.email }}</td>
      </ng-container>
      <ng-container matColumnDef="phone">
        <th mat-header-cell *matHeaderCellDef>Téléphone</th>
        <td mat-cell *matCellDef="let c">{{ c.phone || '—' }}</td>
      </ng-container>
      <ng-container matColumnDef="role">
        <th mat-header-cell *matHeaderCellDef>Rôle</th>
        <td mat-cell *matCellDef="let c">{{ c.role }}</td>
      </ng-container>
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

      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>

    <p *ngIf="customers.length === 0" style="color:#999; text-align:center; padding:24px">
      Aucun client trouvé
    </p>
    <p *ngIf="errorMsg" style="color:red; margin-top:8px">{{ errorMsg }}</p>
  `
})
export class CustomersComponent implements OnInit {
  customers: Customer[] = [];
  columns = ['id', 'name', 'email', 'phone', 'role', 'active', 'createdAt', 'actions'];
  errorMsg = '';

  get activeCount(): number {
    return this.customers.filter(c => c.active).length;
  }

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
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
