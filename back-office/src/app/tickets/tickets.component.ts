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
import { ApiService, Ticket, Product, Customer } from '../core/api.service';

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

    <!-- Purchase Card -->
    <mat-card style="max-width:700px; margin-bottom:24px">
      <mat-card-header><mat-card-title>🎫 Acheter un ticket</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="purchaseForm" (ngSubmit)="onPurchase()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end">
          <mat-form-field style="min-width:250px">
            <mat-label>Client</mat-label>
            <mat-select formControlName="userId">
              <mat-option *ngFor="let c of customers" [value]="c.id">
                #{{ c.id }} — {{ c.firstName }} {{ c.lastName }} ({{ c.email }})
              </mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field style="min-width:280px">
            <mat-label>Produit</mat-label>
            <mat-select formControlName="productId">
              <mat-option *ngFor="let p of products" [value]="p.id">
                {{ p.name }} — {{ p.price | number }} XAF ({{ p.type }})
              </mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field>
            <mat-label>Paiement</mat-label>
            <mat-select formControlName="paymentMethod">
              <mat-option value="WALLET">💳 Wallet</mat-option>
              <mat-option value="CASH">💰 Espèces</mat-option>
            </mat-select>
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit"
                  [disabled]="purchaseForm.invalid || purchasing">
            <mat-icon>shopping_cart</mat-icon> Acheter
          </button>
        </form>
        <p *ngIf="purchaseSuccess" style="color:green; margin-top:8px">{{ purchaseSuccess }}</p>
        <p *ngIf="purchaseError" style="color:red; margin-top:8px">{{ purchaseError }}</p>

        <!-- QR Code payload after purchase -->
        <mat-card *ngIf="lastPurchasedTicket" style="margin-top:16px;background:#f5f5f5">
          <mat-card-header><mat-card-title>📱 QR Code généré — Ticket #{{ lastPurchasedTicket.id }}</mat-card-title></mat-card-header>
          <mat-card-content>
            <div style="display:flex;gap:24px;flex-wrap:wrap;margin-bottom:12px">
              <div><strong>Client:</strong> {{ lastPurchasedTicket.userId }}</div>
              <div><strong>Produit:</strong> {{ lastPurchasedTicket.productName }}
                <span [style.background]="typeColor(lastPurchasedTicket.productType)"
                      style="color:white;padding:1px 8px;border-radius:10px;font-size:0.8em;margin-left:4px">
                  {{ lastPurchasedTicket.productType }}
                </span>
              </div>
              <div><strong>Usages:</strong> {{ lastPurchasedTicket.maxUsage >= 999 ? '∞' : lastPurchasedTicket.maxUsage }}</div>
              <div><strong>Expire:</strong> {{ lastPurchasedTicket.validUntil | date:'medium' }}</div>
            </div>
            <details>
              <summary style="cursor:pointer;color:#1976d2">Voir le payload QR signé</summary>
              <pre style="background:#263238;color:#80cbc4;padding:12px;border-radius:8px;overflow-x:auto;font-size:0.85em;margin-top:8px">{{ lastPurchasedTicket.qrPayload | json }}</pre>
            </details>
          </mat-card-content>
        </mat-card>
      </mat-card-content>
    </mat-card>

    <!-- Search & Filter -->
    <mat-card style="max-width:700px; margin-bottom:24px">
      <mat-card-header><mat-card-title>Rechercher / Filtrer</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="searchForm" (ngSubmit)="onSearch()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end">
          <mat-form-field>
            <mat-label>User ID</mat-label>
            <input matInput formControlName="userId" type="number" placeholder="Ex: 1">
          </mat-form-field>
          <mat-form-field>
            <mat-label>Type produit</mat-label>
            <mat-select [(value)]="filterType" (selectionChange)="applyFilter()">
              <mat-option value="">Tous</mat-option>
              <mat-option value="UNIT">Unitaire</mat-option>
              <mat-option value="PACK">Pack</mat-option>
              <mat-option value="CARNET">Carnet</mat-option>
              <mat-option value="PASS">Pass</mat-option>
            </mat-select>
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="searchForm.invalid">Rechercher</button>
          <button mat-raised-button type="button" (click)="loadRecent()">Tous récents</button>
        </form>
      </mat-card-content>
    </mat-card>

    <!-- Tickets Table -->
    <table mat-table [dataSource]="filteredTickets" style="width:100%" *ngIf="filteredTickets.length > 0">
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef>ID</th>
        <td mat-cell *matCellDef="let t">{{ t.id }}</td>
      </ng-container>
      <ng-container matColumnDef="userId">
        <th mat-header-cell *matHeaderCellDef>Client</th>
        <td mat-cell *matCellDef="let t">{{ t.userId }}</td>
      </ng-container>
      <ng-container matColumnDef="productName">
        <th mat-header-cell *matHeaderCellDef>Produit</th>
        <td mat-cell *matCellDef="let t">{{ t.productName }}</td>
      </ng-container>
      <ng-container matColumnDef="productType">
        <th mat-header-cell *matHeaderCellDef>Type</th>
        <td mat-cell *matCellDef="let t">
          <span [style.background]="typeColor(t.productType)"
                style="color:white;padding:2px 8px;border-radius:10px;font-size:0.8em">
            {{ t.productType || '—' }}
          </span>
        </td>
      </ng-container>
      <ng-container matColumnDef="status">
        <th mat-header-cell *matHeaderCellDef>Statut</th>
        <td mat-cell *matCellDef="let t"
            [style.color]="t.status === 'ACTIVE' ? 'green' : t.status === 'USED' ? 'blue' : 'red'">
          {{ t.status }}
        </td>
      </ng-container>
      <ng-container matColumnDef="remaining">
        <th mat-header-cell *matHeaderCellDef>Restants</th>
        <td mat-cell *matCellDef="let t">
          {{ t.maxUsage >= 999 ? '∞' : (t.maxUsage - t.usageCount) + ' / ' + t.maxUsage }}
        </td>
      </ng-container>
      <ng-container matColumnDef="validUntil">
        <th mat-header-cell *matHeaderCellDef>Expire le</th>
        <td mat-cell *matCellDef="let t">{{ t.validUntil | date:'medium' }}</td>
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
          <button mat-icon-button color="primary"
                  (click)="printPdf(t.id)"
                  matTooltip="Imprimer PDF">
            <mat-icon>print</mat-icon>
          </button>
          <button mat-icon-button color="accent"
                  (click)="showQr(t)"
                  matTooltip="Voir QR Code">
            <mat-icon>qr_code</mat-icon>
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>

    <p *ngIf="filteredTickets.length === 0" style="color:#999; text-align:center; padding:16px">
      Aucun ticket trouvé
    </p>

    <!-- QR Code Display -->
    <mat-card *ngIf="selectedTicketForQr" style="max-width:600px; margin-top:24px">
      <mat-card-header>
        <mat-card-title>📱 QR Code - Ticket #{{ selectedTicketForQr.id }}</mat-card-title>
        <div style="margin-left:auto">
          <button mat-icon-button (click)="selectedTicketForQr = null">
            <mat-icon>close</mat-icon>
          </button>
        </div>
      </mat-card-header>
      <mat-card-content>
        <div style="display:flex; gap:24px; flex-wrap:wrap">
          <div style="text-align:center">
            <img [src]="'http://localhost:8081/api/tickets/' + selectedTicketForQr.id + '/qr-image?size=200'"
                 style="border:1px solid #ddd; border-radius:8px"
                 alt="QR Code">
            <br>
            <button mat-stroked-button 
                    (click)="downloadQrImage(selectedTicketForQr.id)"
                    style="margin-top:8px">
              <mat-icon>download</mat-icon>
              Télécharger PNG
            </button>
          </div>
          <div style="flex:1; min-width:250px">
            <h4>Détails du ticket</h4>
            <p><strong>Client:</strong> {{ selectedTicketForQr.userId }}</p>
            <p><strong>Produit:</strong> {{ selectedTicketForQr.productName }}</p>
            <p><strong>Type:</strong> 
              <span [style.background]="typeColor(selectedTicketForQr.productType)"
                    style="color:white;padding:2px 6px;border-radius:8px;font-size:0.85em">
                {{ selectedTicketForQr.productType }}
              </span>
            </p>
            <p><strong>Usages:</strong> {{ selectedTicketForQr.usageCount }} / {{ selectedTicketForQr.maxUsage >= 999 ? '∞' : selectedTicketForQr.maxUsage }}</p>
            <p><strong>Statut:</strong> 
              <span [style.color]="selectedTicketForQr.status === 'ACTIVE' ? 'green' : 'red'">
                {{ selectedTicketForQr.status }}
              </span>
            </p>
            <p><strong>Expire:</strong> {{ selectedTicketForQr.validUntil | date:'medium' }}</p>
            
            <button mat-raised-button color="primary"
                    (click)="printPdf(selectedTicketForQr.id)"
                    style="margin-right:8px">
              <mat-icon>print</mat-icon>
              Imprimer PDF
            </button>
          </div>
        </div>
        
        <details style="margin-top:16px">
          <summary style="cursor:pointer; color:#1976d2">Voir le payload QR (JSON)</summary>
          <pre style="background:#263238;color:#80cbc4;padding:12px;border-radius:8px;overflow-x:auto;font-size:0.85em;margin-top:8px">{{ selectedTicketForQr.qrPayload | json }}</pre>
        </details>
      </mat-card-content>
    </mat-card>
  `
})
export class TicketsComponent implements OnInit {
  tickets: Ticket[] = [];
  filteredTickets: Ticket[] = [];
  products: Product[] = [];
  customers: Customer[] = [];
  columns = ['id', 'userId', 'productName', 'productType', 'status', 'remaining', 'validUntil', 'actions'];
  filterType = '';

  searchForm: FormGroup;
  purchaseForm: FormGroup;
  purchasing = false;
  purchaseSuccess = '';
  purchaseError = '';
  lastPurchasedTicket: Ticket | null = null;
  selectedTicketForQr: Ticket | null = null;

  constructor(private api: ApiService, private fb: FormBuilder, private snack: MatSnackBar) {
    this.searchForm = this.fb.group({
      userId: [null, [Validators.required, Validators.min(1)]]
    });
    this.purchaseForm = this.fb.group({
      userId: [null, Validators.required],
      productId: [null, Validators.required],
      paymentMethod: ['WALLET', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadRecent();
    this.loadProducts();
    this.loadCustomers();
  }

  loadProducts(): void {
    this.api.getProducts().subscribe({
      next: (p) => this.products = p.filter(prod => prod.active),
      error: () => this.snack.open('Erreur de chargement des produits', 'OK', { duration: 3000 })
    });
  }

  loadCustomers(): void {
    this.api.getRecentCustomers().subscribe({
      next: (c) => this.customers = c.filter(cust => cust.active),
      error: () => {}
    });
  }

  typeColor(type: string): string {
    switch (type) {
      case 'UNIT': return '#9e9e9e';
      case 'PACK': return '#1976d2';
      case 'CARNET': return '#f57c00';
      case 'PASS': return '#2e7d32';
      default: return '#757575';
    }
  }

  onPurchase(): void {
    if (this.purchaseForm.invalid) return;
    this.purchasing = true;
    this.purchaseSuccess = '';
    this.purchaseError = '';
    this.lastPurchasedTicket = null;
    const { userId, productId, paymentMethod } = this.purchaseForm.value;
    this.api.purchaseTicket(userId, productId, paymentMethod).subscribe({
      next: (ticket) => {
        this.purchasing = false;
        this.lastPurchasedTicket = ticket;
        const customer = this.customers.find(c => c.id === userId);
        const customerName = customer ? `${customer.firstName} ${customer.lastName}` : `#${userId}`;
        const paymentIcon = paymentMethod === 'CASH' ? '💰' : '💳';
        this.purchaseSuccess = `✅ Ticket #${ticket.id} acheté ${paymentIcon} pour ${customerName} — ${ticket.productName} (${ticket.productType})`;
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
      next: (t) => { this.tickets = t; this.applyFilter(); },
      error: () => this.snack.open('Erreur de chargement des tickets', 'OK', { duration: 3000 })
    });
  }

  onSearch(): void {
    const userId = this.searchForm.value['userId'] as number;
    this.api.getTicketsByUser(userId).subscribe({
      next: (t) => { this.tickets = t; this.applyFilter(); },
      error: () => this.snack.open('Erreur lors de la recherche', 'OK', { duration: 3000 })
    });
  }

  applyFilter(): void {
    if (!this.filterType) {
      this.filteredTickets = this.tickets;
    } else {
      this.filteredTickets = this.tickets.filter(t => t.productType === this.filterType);
    }
  }

  revoke(ticket: Ticket): void {
    this.api.revokeTicket(ticket.id).subscribe({
      next: (t) => {
        const idx = this.tickets.findIndex(x => x.id === t.id);
        if (idx >= 0) this.tickets[idx] = t;
        this.applyFilter();
        this.snack.open(`Ticket #${t.id} révoqué`, 'OK', { duration: 3000 });
      },
      error: () => this.snack.open('Erreur lors de la révocation', 'OK', { duration: 3000 })
    });
  }

  printPdf(ticketId: number): void {
    const url = `http://localhost:8081/api/tickets/${ticketId}/print`;
    const link = document.createElement('a');
    link.href = url;
    link.download = `ticket-${ticketId}.pdf`;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.snack.open(`PDF du ticket #${ticketId} téléchargé`, 'OK', { duration: 3000 });
  }

  showQr(ticket: Ticket): void {
    this.selectedTicketForQr = ticket;
  }

  downloadQrImage(ticketId: number): void {
    const url = `http://localhost:8081/api/tickets/${ticketId}/qr-image?size=400`;
    const link = document.createElement('a');
    link.href = url;
    link.download = `qr-ticket-${ticketId}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.snack.open(`QR Code du ticket #${ticketId} téléchargé`, 'OK', { duration: 3000 });
  }
}
