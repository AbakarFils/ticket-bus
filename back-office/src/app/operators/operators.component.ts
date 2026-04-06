import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService, TicketOperator, TransportZone } from '../core/api.service';

@Component({
  selector: 'app-operators',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatTableModule, MatIconModule,
    MatSelectModule, MatSnackBarModule
  ],
  template: `
    <h2>🏢 Opérateurs & Zones</h2>

    <div style="display:flex;gap:24px;flex-wrap:wrap">
      <!-- Opérateurs -->
      <mat-card style="flex:1;min-width:400px">
        <mat-card-header><mat-card-title>Opérateurs</mat-card-title></mat-card-header>
        <mat-card-content>
          <form [formGroup]="opForm" (ngSubmit)="createOp()" style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:16px">
            <mat-form-field><mat-label>Code</mat-label><input matInput formControlName="code"></mat-form-field>
            <mat-form-field><mat-label>Nom</mat-label><input matInput formControlName="name"></mat-form-field>
            <mat-form-field><mat-label>Email</mat-label><input matInput formControlName="contactEmail"></mat-form-field>
            <button mat-raised-button color="primary" type="submit" [disabled]="opForm.invalid">Ajouter</button>
          </form>
          <table mat-table [dataSource]="operators" style="width:100%" *ngIf="operators.length > 0">
            <ng-container matColumnDef="id"><th mat-header-cell *matHeaderCellDef>ID</th><td mat-cell *matCellDef="let o">{{o.id}}</td></ng-container>
            <ng-container matColumnDef="code"><th mat-header-cell *matHeaderCellDef>Code</th><td mat-cell *matCellDef="let o">{{o.code}}</td></ng-container>
            <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Nom</th><td mat-cell *matCellDef="let o">{{o.name}}</td></ng-container>
            <ng-container matColumnDef="contactEmail"><th mat-header-cell *matHeaderCellDef>Email</th><td mat-cell *matCellDef="let o">{{o.contactEmail}}</td></ng-container>
            <ng-container matColumnDef="active"><th mat-header-cell *matHeaderCellDef>Actif</th><td mat-cell *matCellDef="let o" [style.color]="o.active ? 'green' : 'red'">{{o.active ? '✅' : '❌'}}</td></ng-container>
            <tr mat-header-row *matHeaderRowDef="opCols"></tr>
            <tr mat-row *matRowDef="let row; columns: opCols"></tr>
          </table>
          <p *ngIf="operators.length === 0" style="color:#999;text-align:center">Aucun opérateur</p>
        </mat-card-content>
      </mat-card>

      <!-- Zones -->
      <mat-card style="flex:1;min-width:400px">
        <mat-card-header><mat-card-title>Zones de transport</mat-card-title></mat-card-header>
        <mat-card-content>
          <form [formGroup]="zoneForm" (ngSubmit)="createZone()" style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:16px">
            <mat-form-field><mat-label>Code</mat-label><input matInput formControlName="code"></mat-form-field>
            <mat-form-field><mat-label>Nom</mat-label><input matInput formControlName="name"></mat-form-field>
            <mat-form-field>
              <mat-label>Opérateur</mat-label>
              <mat-select formControlName="operatorId">
                <mat-option *ngFor="let o of operators" [value]="o.id">{{o.name}}</mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field><mat-label>Description</mat-label><input matInput formControlName="description"></mat-form-field>
            <button mat-raised-button color="accent" type="submit" [disabled]="zoneForm.invalid">Ajouter</button>
          </form>
          <table mat-table [dataSource]="zones" style="width:100%" *ngIf="zones.length > 0">
            <ng-container matColumnDef="id"><th mat-header-cell *matHeaderCellDef>ID</th><td mat-cell *matCellDef="let z">{{z.id}}</td></ng-container>
            <ng-container matColumnDef="code"><th mat-header-cell *matHeaderCellDef>Code</th><td mat-cell *matCellDef="let z">{{z.code}}</td></ng-container>
            <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Nom</th><td mat-cell *matCellDef="let z">{{z.name}}</td></ng-container>
            <ng-container matColumnDef="description"><th mat-header-cell *matHeaderCellDef>Description</th><td mat-cell *matCellDef="let z">{{z.description}}</td></ng-container>
            <ng-container matColumnDef="active"><th mat-header-cell *matHeaderCellDef>Actif</th><td mat-cell *matCellDef="let z" [style.color]="z.active ? 'green' : 'red'">{{z.active ? '✅' : '❌'}}</td></ng-container>
            <tr mat-header-row *matHeaderRowDef="zoneCols"></tr>
            <tr mat-row *matRowDef="let row; columns: zoneCols"></tr>
          </table>
          <p *ngIf="zones.length === 0" style="color:#999;text-align:center">Aucune zone</p>
        </mat-card-content>
      </mat-card>
    </div>
  `
})
export class OperatorsComponent implements OnInit {
  operators: TicketOperator[] = [];
  zones: TransportZone[] = [];
  opCols = ['id', 'code', 'name', 'contactEmail', 'active'];
  zoneCols = ['id', 'code', 'name', 'description', 'active'];
  opForm: FormGroup;
  zoneForm: FormGroup;

  constructor(private api: ApiService, private fb: FormBuilder, private snack: MatSnackBar) {
    this.opForm = this.fb.group({ code: ['', Validators.required], name: ['', Validators.required], contactEmail: [''] });
    this.zoneForm = this.fb.group({ code: ['', Validators.required], name: ['', Validators.required], operatorId: [null], description: [''] });
  }

  ngOnInit(): void { this.loadOps(); this.loadZones(); }

  loadOps(): void { this.api.getOperators().subscribe({ next: o => this.operators = o, error: () => {} }); }
  loadZones(): void { this.api.getZones().subscribe({ next: z => this.zones = z, error: () => {} }); }

  createOp(): void {
    this.api.createOperator(this.opForm.value).subscribe({
      next: () => { this.loadOps(); this.opForm.reset(); this.snack.open('Opérateur créé', 'OK', { duration: 3000 }); },
      error: () => this.snack.open('Erreur', 'OK', { duration: 3000 })
    });
  }

  createZone(): void {
    this.api.createZone(this.zoneForm.value).subscribe({
      next: () => { this.loadZones(); this.zoneForm.reset(); this.snack.open('Zone créée', 'OK', { duration: 3000 }); },
      error: () => this.snack.open('Erreur', 'OK', { duration: 3000 })
    });
  }
}

