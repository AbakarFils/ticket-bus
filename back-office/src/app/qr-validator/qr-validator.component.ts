import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { ApiService } from '../core/api.service';

interface ValidationResult {
  valid: boolean;
  reason: string;
  ticketInfo: string;
  ticketId: number;
  usageCount: number;
  maxUsage: number;
  timestamp: Date;
}

interface UseResult {
  success: boolean;
  reason?: string;
  message?: string;
  usageCount?: number;
  maxUsage?: number;
  status?: string;
}

@Component({
  selector: 'app-qr-validator',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatTableModule, MatIconModule,
    MatSnackBarModule, MatTooltipModule, MatChipsModule
  ],
  template: `
    <h2>Validation QR Code Tickets</h2>

    <!-- QR Scanner Form -->
    <mat-card style="max-width:800px; margin-bottom:24px">
      <mat-card-header>
        <mat-card-title>📱 Scanner QR Code</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <form [formGroup]="scanForm" (ngSubmit)="onScan()" style="margin-bottom:16px">
          <mat-form-field style="width:100%; margin-bottom:12px">
            <mat-label>Payload QR Code (JSON)</mat-label>
            <textarea matInput 
                      formControlName="qrPayload" 
                      placeholder='{"ticketId": 123, "signature": "...", ...}'
                      rows="4"
                      style="font-family:monospace"></textarea>
            <mat-hint>Collez ici le contenu JSON du QR code scanné</mat-hint>
          </mat-form-field>
          
          <div style="display:flex; gap:12px">
            <button mat-raised-button color="primary" type="submit" 
                    [disabled]="scanForm.invalid || validating">
              <mat-icon>qr_code_scanner</mat-icon>
              {{ validating ? 'Validation...' : 'Valider QR Code' }}
            </button>
            
            <button mat-raised-button color="accent" type="button" 
                    (click)="loadSampleQr()" 
                    matTooltip="Charge un exemple de QR code pour test">
              <mat-icon>science</mat-icon>
              Exemple
            </button>
          </div>
        </form>

        <!-- Last Validation Result -->
        <div *ngIf="lastResult" 
             [style.background]="lastResult.valid ? '#e8f5e8' : '#ffeaa7'"
             style="padding:16px; border-radius:8px; margin-top:16px">
          
          <div style="display:flex; align-items:center; gap:8px; margin-bottom:8px">
            <mat-icon [style.color]="lastResult.valid ? 'green' : 'orange'">
              {{ lastResult.valid ? 'check_circle' : 'warning' }}
            </mat-icon>
            <strong>{{ lastResult.valid ? 'QR CODE VALIDE' : 'QR CODE INVALIDE' }}</strong>
          </div>
          
          <p><strong>Raison:</strong> {{ lastResult.reason }}</p>
          <p *ngIf="lastResult.ticketInfo"><strong>Ticket:</strong> {{ lastResult.ticketInfo }}</p>
          
          <div *ngIf="isResultValidForUse()" style="margin-top:12px">
            <button mat-raised-button color="warn" 
                    (click)="useTicketFromResult()"
                    [disabled]="using"
                    matTooltip="Marquer le ticket comme utilisé">
              <mat-icon>check</mat-icon>
              {{ using ? 'Utilisation...' : 'Utiliser Ticket' }}
            </button>
          </div>
        </div>

        <!-- Use Result -->
        <div *ngIf="lastUseResult" 
             [style.background]="lastUseResult.success ? '#e8f5e8' : '#ffcccc'"
             style="padding:16px; border-radius:8px; margin-top:16px">
          
          <div style="display:flex; align-items:center; gap:8px; margin-bottom:8px">
            <mat-icon [style.color]="lastUseResult.success ? 'green' : 'red'">
              {{ lastUseResult.success ? 'check_circle' : 'error' }}
            </mat-icon>
            <strong>{{ lastUseResult.success ? 'TICKET UTILISÉ' : 'ERREUR UTILISATION' }}</strong>
          </div>
          
          <p *ngIf="lastUseResult.message">{{ lastUseResult.message }}</p>
          <p *ngIf="lastUseResult.reason">{{ lastUseResult.reason }}</p>
          
          <div *ngIf="lastUseResult.success" style="display:flex; gap:16px; margin-top:8px">
            <span><strong>Usages:</strong> {{ lastUseResult.usageCount }} / {{ lastUseResult.maxUsage === 999 ? '∞' : lastUseResult.maxUsage }}</span>
            <span><strong>Statut:</strong> {{ lastUseResult.status }}</span>
          </div>
        </div>
      </mat-card-content>
    </mat-card>

    <!-- Validation History -->
    <mat-card style="max-width:1200px">
      <mat-card-header>
        <mat-card-title>📋 Historique des Validations</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <table mat-table [dataSource]="validationHistory" *ngIf="validationHistory.length > 0">
          
          <ng-container matColumnDef="timestamp">
            <th mat-header-cell *matHeaderCellDef>Heure</th>
            <td mat-cell *matCellDef="let result">{{ result.timestamp | date:'HH:mm:ss' }}</td>
          </ng-container>

          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Statut</th>
            <td mat-cell *matCellDef="let result">
              <mat-icon [style.color]="result.valid ? 'green' : 'red'">
                {{ result.valid ? 'check_circle' : 'cancel' }}
              </mat-icon>
            </td>
          </ng-container>

          <ng-container matColumnDef="ticketId">
            <th mat-header-cell *matHeaderCellDef>Ticket ID</th>
            <td mat-cell *matCellDef="let result">{{ result.ticketId > 0 ? result.ticketId : '—' }}</td>
          </ng-container>

          <ng-container matColumnDef="reason">
            <th mat-header-cell *matHeaderCellDef>Raison</th>
            <td mat-cell *matCellDef="let result">{{ result.reason }}</td>
          </ng-container>

          <ng-container matColumnDef="usage">
            <th mat-header-cell *matHeaderCellDef>Usages</th>
            <td mat-cell *matCellDef="let result">
              <span *ngIf="result.ticketId > 0">
                {{ result.usageCount }} / {{ result.maxUsage === 999 ? '∞' : result.maxUsage }}
              </span>
              <span *ngIf="result.ticketId === 0">—</span>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="historyColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: historyColumns"></tr>
        </table>

        <p *ngIf="validationHistory.length === 0" style="color:#999; text-align:center; padding:24px">
          Aucune validation effectuée
        </p>
      </mat-card-content>
    </mat-card>
  `
})
export class QrValidatorComponent implements OnInit {
  scanForm: FormGroup;
  validating = false;
  using = false;
  lastResult: ValidationResult | null = null;
  lastUseResult: UseResult | null = null;
  validationHistory: ValidationResult[] = [];
  historyColumns = ['timestamp', 'status', 'ticketId', 'reason', 'usage'];

  constructor(
    private readonly api: ApiService,
    private readonly fb: FormBuilder,
    private readonly snack: MatSnackBar
  ) {
    this.scanForm = this.fb.group({
      qrPayload: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Load history from localStorage
    const history = localStorage.getItem('qr-validation-history');
    if (history) {
      try {
        const parsedHistory = JSON.parse(history);
        this.validationHistory = parsedHistory.map((item: any) => ({
          valid: item.valid || false,
          reason: item.reason || 'Aucune raison',
          ticketInfo: item.ticketInfo || '',
          ticketId: item.ticketId || 0,
          usageCount: item.usageCount || 0,
          maxUsage: item.maxUsage || 0,
          timestamp: new Date(item.timestamp)
        }));
      } catch (e) {
        console.warn('Failed to load validation history', e);
        this.validationHistory = [];
      }
    }
  }

  onScan(): void {
    if (this.scanForm.invalid) return;

    const qrPayload = this.scanForm.value.qrPayload.trim();
    this.validating = true;
    this.lastResult = null;
    this.lastUseResult = null;

    this.api.validateQrCode(qrPayload).subscribe({
      next: (result) => {
        this.validating = false;
        this.lastResult = {
          valid: result.valid || false,
          reason: result.reason || 'Aucune raison fournie',
          ticketInfo: result.ticketInfo || '',
          ticketId: result.ticketId || 0,
          usageCount: result.usageCount || 0,
          maxUsage: result.maxUsage || 0,
          timestamp: new Date()
        };

        // Add to history only if we have a valid result
        if (this.lastResult) {
          this.validationHistory.unshift(this.lastResult);
          if (this.validationHistory.length > 50) {
            this.validationHistory = this.validationHistory.slice(0, 50);
          }
          this.saveHistory();
        }

        // Show notification
        const message = result.valid
          ? `✅ QR Code valide - ${result.ticketInfo || 'Ticket valide'}`
          : `❌ QR Code invalide - ${result.reason || 'Erreur inconnue'}`;
        this.snack.open(message, 'OK', { duration: 4000 });
      },
      error: (err) => {
        this.validating = false;
        this.snack.open('Erreur lors de la validation', 'OK', { duration: 3000 });
        console.error('Validation error:', err);
      }
    });
  }

  useTicket(ticketId: number): void {
    this.using = true;
    this.lastUseResult = null;

    this.api.useTicket(ticketId).subscribe({
      next: (result) => {
        this.using = false;
        this.lastUseResult = result;

        const message = result.success
          ? `✅ Ticket #${ticketId} utilisé`
          : `❌ Erreur: ${result.reason}`;
        this.snack.open(message, 'OK', { duration: 4000 });
      },
      error: (err) => {
        this.using = false;
        this.snack.open('Erreur lors de l\'utilisation du ticket', 'OK', { duration: 3000 });
        console.error('Use ticket error:', err);
      }
    });
  }

  useTicketFromResult(): void {
    const result = this.lastResult;
    if (result && result.valid && result.ticketId > 0) {
      this.useTicket(result.ticketId);
    }
  }

  isResultValidForUse(): boolean {
    return !!(this.lastResult && this.lastResult.valid && this.lastResult.ticketId > 0);
  }

  loadSampleQr(): void {
    const sample = {
      "ticketId": 1,
      "userId": 1,
      "nonce": "sample-nonce-123",
      "validFrom": "2026-04-06T10:00:00",
      "validUntil": "2026-04-07T10:00:00",
      "signature": "sample-signature-for-testing"
    };
    this.scanForm.patchValue({
      qrPayload: JSON.stringify(sample, null, 2)
    });
  }

  private saveHistory(): void {
    try {
      localStorage.setItem('qr-validation-history', JSON.stringify(this.validationHistory));
    } catch (e) {
      console.warn('Failed to save validation history', e);
    }
  }
}
