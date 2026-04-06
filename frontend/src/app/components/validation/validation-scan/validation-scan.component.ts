import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AsyncPipe } from '@angular/common';
import { ValidationService } from '../../../services/validation.service';
import { OfflineService } from '../../../services/offline.service';
import { ValidationResponse } from '../../../models/validation-event.model';

@Component({
  selector: 'app-validation-scan',
  standalone: true,
  imports: [FormsModule, AsyncPipe],
  template: `
    <h1 class="page-title">Valider un ticket</h1>
    @if (!(offline.isOnline | async)) {
      <div class="alert alert-warning">⚠️ Mode hors ligne — les validations seront mises en file d'attente</div>
    }
    <div class="card" style="max-width:600px">
      <div class="form-group"><label>Données QR (JSON)</label>
        <textarea class="form-control" [(ngModel)]="qrData" rows="5" placeholder='{"ticketNumber":"...","signature":"..."}'></textarea>
      </div>
      <div class="grid-2">
        <div class="form-group"><label>ID Appareil</label><input class="form-control" [(ngModel)]="deviceId" /></div>
        <div class="form-group"><label>Lieu</label><input class="form-control" [(ngModel)]="location" /></div>
      </div>
      <button class="btn btn-primary" (click)="validate()" [disabled]="loading">{{ loading ? 'Validation...' : 'Valider' }}</button>
    </div>
    @if (result) {
      <div class="card" style="max-width:600px;margin-top:1rem">
        <div class="alert" [class.alert-success]="result.valid" [class.alert-danger]="!result.valid && result.status !== 'SUSPECT'" [class.alert-warning]="result.status === 'SUSPECT'">
          <strong>{{ result.valid ? '✅ VALIDE' : result.status === 'SUSPECT' ? '⚠️ SUSPECT' : '❌ INVALIDE' }}</strong>
          — {{ result.message }}
        </div>
        @if (result.ticketDetails) {
          <p><strong>Passager:</strong> {{ result.ticketDetails.passengerName }}</p>
          <p><strong>Ligne:</strong> {{ result.ticketDetails.routeName }}</p>
        }
      </div>
    }
  `
})
export class ValidationScanComponent {
  offline = inject(OfflineService);
  private svc = inject(ValidationService);
  qrData = ''; deviceId = localStorage.getItem('device_id') || 'web-controller'; location = '';
  result: ValidationResponse | null = null; loading = false;
  validate() {
    if (!this.qrData.trim()) return;
    localStorage.setItem('device_id', this.deviceId);
    if (!this.offline.isOnline.value) {
      this.offline.addPending({ qrData: this.qrData, deviceId: this.deviceId, location: this.location, time: new Date().toISOString() });
      this.result = { valid: false, status: 'OFFLINE_PENDING', message: 'Validation mise en file d\'attente (hors ligne)' };
      return;
    }
    this.loading = true;
    this.svc.validateTicket({ qrCodeData: this.qrData, deviceId: this.deviceId, location: this.location, validationTime: new Date().toISOString() }).subscribe({
      next: r => { this.result = r; this.loading = false; },
      error: () => { this.result = { valid: false, status: 'INVALID', message: 'Erreur de validation' }; this.loading = false; }
    });
  }
}
