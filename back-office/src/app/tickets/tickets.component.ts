import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSelectModule } from '@angular/material/select';
import { ApiService, Ticket, Product } from '../core/api.service';

@Component({
  selector: 'app-tickets',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatTableModule, MatChipsModule,
    MatIconModule, MatSnackBarModule, MatTooltipModule,
    MatSelectModule
  ],
  template: `
    <h2>Gestion des Tickets</h2>

    <mat-card style="max-width:560px; margin-bottom:24px">
      <mat-card-header><mat-card-title>🎫 Acheter un ticket</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="purchaseForm" (ngSubmit)="onPurchase()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end">
          <mat-form-field>
            <mat-label>User ID</mat-label>
            <input matInput formControlName="userId" type="number" placeholder="Ex: 1">
          </mat-form-field>
          <mat-form-field style="min-width:220px">
            <mat-label>Produit</mat-label>
            <mat-select formControlName="productId">
              <mat-option *ngFor="let p of products" [value]="p.id">
                {{ p.name }} — {{ p.price | number }} XAF ({{ p.type }})
              </mat-option>
            </mat-select>
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit"
                  [disabled]="purchaseForm.invalid || purchasing">
            <mat-icon>shopping_cart</mat-icon> Acheter
          </button>
        </form>
        <p *ngIf="purchaseSuccess" style="color:green; margin-top:8px">{{ purchaseSuccess }}</p>
        <p *ngIf="purchaseError" style="color:red; margin-top:8px">{{ purchaseError }}</p>
      </mat-card-content>
    </mat-card>

    <mat-card style="max-width:480px; margin-bottom:24px">
      <mat-card-header><mat-card-title>Rechercher par utilisateur</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="searchForm" (ngSubmit)="onSearch()" style="display:flex;gap:12px;align-items:flex-end">
          <mat-form-field>
            <mat-label>User ID</mat-label>
            <input matInput formControlName="userId" type="number" placeholder="Ex: 1">
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="searchForm.invalid">Rechercher</button>
          <button mat-raised-button type="button" (click)="loadRecent()">Tous récents</button>
        </form>
      </mat-card-content>
    </mat-card>

    <table mat-table [dataSource]="tickets" style="width:100%" *ngIf="tickets.length > 0">
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef>ID</th>
        <td mat-cell *matCellDef="let t">{{ t.id }}</td>
      </ng-container>
      <ng-container matColumnDef="userId">
        <th mat-header-cell *matHeaderCellDef>Utilisateur</th>
        <td mat-cell *matCellDef="let t">{{ t.userId }}</td>
      </ng-container>
      <ng-container matColumnDef="productName">
        <th mat-header-cell *matHeaderCellDef>Produit</th>
        <td mat-cell *matCellDef="let t">{{ t.productName }}</td>
      </ng-container>
      <ng-container matColumnDef="status">
        <th mat-header-cell *matHeaderCellDef>Statut</th>
        <td mat-cell *matCellDef="let t"
            [style.color]="t.status === 'ACTIVE' ? 'green' : t.status === 'USED' ? 'blue' : 'red'">
          {{ t.status }}
        </td>
      </ng-container>
      <ng-container matColumnDef="usageCount">
        <th mat-header-cell *matHeaderCellDef>Usages</th>
        <td mat-cell *matCellDef="let t">{{ t.usageCount }} / {{ t.maxUsage }}</td>
      </ng-container>
      <ng-container matColumnDef="validUntil">
        <th mat-header-cell *matHeaderCellDef>Expire le</th>
        <td mat-cell *matCellDef="let t">{{ t.validUntil | date:'medium' }}</td>
      </ng-container>
      <ng-container matColumnDef="createdAt">
        <th mat-header-cell *matHeaderCellDef>Créé le</th>
        <td mat-cell *matCellDef="let t">{{ t.createdAt | date:'medium' }}</td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let t">
          <button mat-icon-button color="warn"
                  (click)="revoke(t)"
                  [disabled]="t.status === 'REVOKED' || t.status === 'CANCELLED'"
                  matTooltip="Révoquer">
            <mat-icon>block</mat-icon>
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>

    <p *ngIf="tickets.length === 0" style="color:#999; text-align:center; padding:16px">
      Aucun ticket trouvé
    </p>
  `
})
export class TicketsComponent implements OnInit {
  tickets: Ticket[] = [];
  products: Product[] = [];
  columns = ['id', 'userId', 'productName', 'status', 'usageCount', 'validUntil', 'createdAt', 'actions'];

  searchForm: FormGroup;
  purchaseForm: FormGroup;
  purchasing = false;
  purchaseSuccess = '';
  purchaseError = '';

  constructor(private api: ApiService, private fb: FormBuilder, private snack: MatSnackBar) {
    this.searchForm = this.fb.group({
      userId: [null, [Validators.required, Validators.min(1)]]
    });
    this.purchaseForm = this.fb.group({
      userId: [null, [Validators.required, Validators.min(1)]],
      productId: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadRecent();
    this.loadProducts();
  }

  loadProducts(): void {
    this.api.getProducts().subscribe({
      next: (p) => this.products = p.filter(prod => prod.active),
      error: () => this.snack.open('Erreur de chargement des produits', 'OK', { duration: 3000 })
    });
  }

  onPurchase(): void {
    if (this.purchaseForm.invalid) return;
    this.purchasing = true;
    this.purchaseSuccess = '';
    this.purchaseError = '';
    const { userId, productId } = this.purchaseForm.value;
    this.api.purchaseTicket(userId, productId).subscribe({
      next: (ticket) => {
        this.purchasing = false;
        this.purchaseSuccess = `✅ Ticket #${ticket.id} acheté avec succès ! Produit: ${ticket.productName}`;
        this.purchaseForm.reset();
        this.loadRecent();
      },
      error: (err) => {
        this.purchasing = false;
        const msg = err.error?.error || 'Erreur lors de l\'achat du ticket';
        this.purchaseError = `❌ ${msg}`;
      }
    });
  }

  loadRecent(): void {
    this.api.getRecentTickets().subscribe({
      next: (t) => this.tickets = t,
      error: () => this.snack.open('Erreur de chargement des tickets', 'OK', { duration: 3000 })
    });
  }

  onSearch(): void {
    const userId = this.searchForm.value['userId'] as number;
    this.api.getTicketsByUser(userId).subscribe({
      next: (t) => this.tickets = t,
      error: () => this.snack.open('Erreur lors de la recherche', 'OK', { duration: 3000 })
    });
  }

  revoke(ticket: Ticket): void {
    this.api.revokeTicket(ticket.id).subscribe({
      next: (t) => {
        const idx = this.tickets.findIndex(x => x.id === t.id);
        if (idx >= 0) this.tickets[idx] = t;
        this.snack.open(`Ticket #${t.id} révoqué`, 'OK', { duration: 3000 });
      },
      error: () => this.snack.open('Erreur lors de la révocation', 'OK', { duration: 3000 })
    });
  }
}

