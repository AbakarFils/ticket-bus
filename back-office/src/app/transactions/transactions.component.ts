import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ApiService, PaymentTransaction } from '../core/api.service';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatCardModule, MatIconModule],
  template: `
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
      <h2 style="margin:0">Transactions de paiement</h2>
      <button mat-raised-button color="primary" (click)="load()">
        <mat-icon>refresh</mat-icon> Actualiser
      </button>
    </div>

    <mat-card style="margin-bottom:16px">
      <mat-card-content style="display:flex; gap:32px">
        <span><strong>Total :</strong> {{ transactions.length }}</span>
        <span style="color:green"><strong>Succès :</strong> {{ successCount }}</span>
        <span style="color:blue"><strong>Remboursés :</strong> {{ refundedCount }}</span>
        <span><strong>Volume :</strong> {{ totalVolume | number }} XAF</span>
      </mat-card-content>
    </mat-card>

    <table mat-table [dataSource]="transactions" style="width:100%">
      <ng-container matColumnDef="transactionId">
        <th mat-header-cell *matHeaderCellDef>ID</th>
        <td mat-cell *matCellDef="let t" style="font-family:monospace; font-size:0.8em">
          {{ t.transactionId | slice:0:12 }}…
        </td>
      </ng-container>
      <ng-container matColumnDef="userId">
        <th mat-header-cell *matHeaderCellDef>Utilisateur</th>
        <td mat-cell *matCellDef="let t">{{ t.userId }}</td>
      </ng-container>
      <ng-container matColumnDef="amount">
        <th mat-header-cell *matHeaderCellDef>Montant</th>
        <td mat-cell *matCellDef="let t">{{ t.amount | number }} {{ t.currency }}</td>
      </ng-container>
      <ng-container matColumnDef="method">
        <th mat-header-cell *matHeaderCellDef>Méthode</th>
        <td mat-cell *matCellDef="let t">{{ t.method }}</td>
      </ng-container>
      <ng-container matColumnDef="status">
        <th mat-header-cell *matHeaderCellDef>Statut</th>
        <td mat-cell *matCellDef="let t" [style.color]="statusColor(t.status)">{{ t.status }}</td>
      </ng-container>
      <ng-container matColumnDef="createdAt">
        <th mat-header-cell *matHeaderCellDef>Date</th>
        <td mat-cell *matCellDef="let t">{{ t.createdAt | date:'short' }}</td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let t">
          <button mat-button color="warn" (click)="refund(t)" [disabled]="t.status !== 'SUCCESS'">
            Rembourser
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>

    <p *ngIf="transactions.length === 0" style="color:#999; text-align:center; padding:24px">
      Aucune transaction
    </p>
    <p *ngIf="msg" [style.color]="msgColor" style="margin-top:8px">{{ msg }}</p>
  `
})
export class TransactionsComponent implements OnInit {
  transactions: PaymentTransaction[] = [];
  columns = ['transactionId', 'userId', 'amount', 'method', 'status', 'createdAt', 'actions'];
  msg = '';
  msgColor = 'green';

  get successCount(): number {
    return this.transactions.filter(t => t.status === 'SUCCESS').length;
  }

  get refundedCount(): number {
    return this.transactions.filter(t => t.status === 'REFUNDED').length;
  }

  get totalVolume(): number {
    return this.transactions
      .filter(t => t.status === 'SUCCESS')
      .reduce((sum, t) => sum + t.amount, 0);
  }

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.getAllPaymentTransactions().subscribe({
      next: (t) => this.transactions = t,
      error: () => {}
    });
  }

  refund(tx: PaymentTransaction): void {
    if (!confirm(`Rembourser ${tx.amount} ${tx.currency} ?`)) return;
    this.api.refundPayment(tx.transactionId).subscribe({
      next: () => {
        tx.status = 'REFUNDED';
        this.msg = 'Remboursement effectué.';
        this.msgColor = 'green';
      },
      error: (err: any) => {
        this.msg = 'Erreur : ' + (err?.error?.error || err.message);
        this.msgColor = 'red';
      }
    });
  }

  statusColor(status: string): string {
    switch (status) {
      case 'SUCCESS': return 'green';
      case 'REFUNDED': return 'blue';
      case 'FAILED': return 'red';
      default: return 'orange';
    }
  }
}
