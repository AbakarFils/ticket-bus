import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { TicketService } from '../../../services/ticket.service';
import { Ticket } from '../../../models/ticket.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [RouterLink],
  template: `
    @if (ticket) {
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1.5rem">
        <h1 class="page-title" style="margin:0">{{ ticket.ticketNumber }}</h1>
        <span class="badge badge-{{ ticket.status }}" style="font-size:1rem">{{ ticket.status }}</span>
      </div>
      <div class="grid-2">
        <div class="card">
          <h2 class="section-title">Informations</h2>
          <p><strong>Passager:</strong> {{ ticket.passengerName }}</p>
          <p><strong>Email:</strong> {{ ticket.passengerEmail }}</p>
          <p><strong>Ligne:</strong> {{ ticket.routeName }}</p>
          <p><strong>Trajet:</strong> {{ ticket.departureLocation }} → {{ ticket.arrivalLocation }}</p>
          <p><strong>Prix:</strong> {{ ticket.price }} €</p>
          <p><strong>Utilisations:</strong> {{ ticket.usageCount }} / {{ ticket.maxUsageCount }}</p>
          <div style="margin-top:1rem;display:flex;gap:.5rem">
            <a routerLink="/tickets" class="btn btn-secondary btn-sm">← Retour</a>
            @if (ticket.status === 'ACTIVE') {
              <button class="btn btn-danger btn-sm" (click)="cancel()">Annuler</button>
            }
          </div>
        </div>
        <div class="card" style="text-align:center">
          <h2 class="section-title">QR Code</h2>
          @if (qrUrl) { <img [src]="qrUrl" alt="QR Code" style="max-width:250px;border-radius:8px" /> }
          <p style="font-size:.8rem;color:#999;margin-top:.5rem">Rotation toutes les 30s</p>
        </div>
      </div>
    } @else { <p>Chargement...</p> }
  `
})
export class TicketDetailComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private svc = inject(TicketService);
  private sanitizer = inject(DomSanitizer);
  ticket: Ticket | null = null;
  qrUrl: SafeUrl | null = null;
  private qrTimer: any;
  private currentQrBlob: string | null = null;
  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.svc.getTicket(id).subscribe({ next: t => { this.ticket = t; this.loadQr(id); this.qrTimer = setInterval(() => this.loadQr(id), 30000); }, error: () => {} });
  }
  loadQr(id: string) {
    this.svc.getQrCode(id).subscribe({ next: blob => {
      if (this.currentQrBlob) URL.revokeObjectURL(this.currentQrBlob);
      this.currentQrBlob = URL.createObjectURL(blob);
      this.qrUrl = this.sanitizer.bypassSecurityTrustUrl(this.currentQrBlob);
    }, error: () => {} });
  }
  cancel() {
    if (!this.ticket) return;
    this.svc.cancelTicket(this.ticket.id).subscribe({ next: t => this.ticket = t, error: () => {} });
  }
  ngOnDestroy() { if (this.qrTimer) clearInterval(this.qrTimer); if (this.currentQrBlob) URL.revokeObjectURL(this.currentQrBlob); }
}
