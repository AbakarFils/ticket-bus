import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { ApiService, Product } from '../core/api.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatTableModule, MatChipsModule
  ],
  template: `
    <h2>Produits tarifaires</h2>

    <mat-card style="margin-bottom:24px">
      <mat-card-header><mat-card-title>Nouveau produit</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="onSubmit()" style="display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end">
          <mat-form-field>
            <mat-label>Nom</mat-label>
            <input matInput formControlName="name" placeholder="Ex: Pass journée">
          </mat-form-field>
          <mat-form-field>
            <mat-label>Type</mat-label>
            <mat-select formControlName="type">
              <mat-option value="UNIT">Unitaire</mat-option>
              <mat-option value="PACK">Pack</mat-option>
              <mat-option value="CARNET">Carnet</mat-option>
              <mat-option value="PASS">Pass</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field>
            <mat-label>Prix (XAF)</mat-label>
            <input matInput formControlName="price" type="number" placeholder="500">
          </mat-form-field>
          <mat-form-field>
            <mat-label>Usages max</mat-label>
            <input matInput formControlName="maxUsage" type="number" placeholder="1">
          </mat-form-field>
          <mat-form-field>
            <mat-label>Durée (jours)</mat-label>
            <input matInput formControlName="durationDays" type="number" placeholder="Vide si non applicable">
          </mat-form-field>
          <mat-form-field *ngIf="form.get('type')?.value === 'PASS' || form.get('type')?.value === 'CARNET'" style="min-width:250px">
            <mat-label>Description</mat-label>
            <input matInput formControlName="description" placeholder="Ex: Voyages illimités pendant 30 jours">
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Créer</button>
        </form>
        <p *ngIf="successMsg" style="color:green">{{ successMsg }}</p>
        <p *ngIf="errorMsg" style="color:red">{{ errorMsg }}</p>
      </mat-card-content>
    </mat-card>

    <table mat-table [dataSource]="products" style="width:100%">
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef>Nom</th>
        <td mat-cell *matCellDef="let p">{{ p.name }}</td>
      </ng-container>
      <ng-container matColumnDef="type">
        <th mat-header-cell *matHeaderCellDef>Type</th>
        <td mat-cell *matCellDef="let p">
          <span [style.background]="typeColor(p.type)"
                style="color:white;padding:2px 10px;border-radius:12px;font-size:0.85em">
            {{ p.type }}
          </span>
        </td>
      </ng-container>
      <ng-container matColumnDef="price">
        <th mat-header-cell *matHeaderCellDef>Prix</th>
        <td mat-cell *matCellDef="let p">{{ p.price | number }} XAF</td>
      </ng-container>
      <ng-container matColumnDef="maxUsage">
        <th mat-header-cell *matHeaderCellDef>Usages max</th>
        <td mat-cell *matCellDef="let p">{{ p.maxUsage >= 999 ? '∞' : p.maxUsage }}</td>
      </ng-container>
      <ng-container matColumnDef="durationDays">
        <th mat-header-cell *matHeaderCellDef>Durée (j)</th>
        <td mat-cell *matCellDef="let p">{{ p.durationDays ?? '—' }}</td>
      </ng-container>
      <ng-container matColumnDef="description">
        <th mat-header-cell *matHeaderCellDef>Description</th>
        <td mat-cell *matCellDef="let p">{{ p.description ?? '—' }}</td>
      </ng-container>
      <ng-container matColumnDef="active">
        <th mat-header-cell *matHeaderCellDef>Actif</th>
        <td mat-cell *matCellDef="let p" [style.color]="p.active ? 'green' : 'red'">
          {{ p.active ? 'Oui' : 'Non' }}
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>
  `
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  columns = ['name', 'type', 'price', 'maxUsage', 'durationDays', 'description', 'active'];
  successMsg = '';
  errorMsg = '';

  form: FormGroup;

  constructor(private api: ApiService, private fb: FormBuilder) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      type: ['UNIT', Validators.required],
      price: [null, [Validators.required, Validators.min(1)]],
      maxUsage: [1, [Validators.required, Validators.min(1)]],
      durationDays: [null],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.api.getProducts().subscribe({
      next: (p) => this.products = p,
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

  onSubmit(): void {
    if (this.form.invalid) return;
    this.successMsg = '';
    this.errorMsg = '';
    this.api.createProduct(this.form.value).subscribe({
      next: () => {
        this.successMsg = 'Produit créé avec succès.';
        this.form.reset({ type: 'UNIT', maxUsage: 1 });
        this.loadProducts();
      },
      error: () => { this.errorMsg = 'Erreur lors de la création.'; }
    });
  }
}
