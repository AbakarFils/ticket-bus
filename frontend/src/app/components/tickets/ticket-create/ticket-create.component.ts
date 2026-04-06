import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TicketService } from '../../../services/ticket.service';

@Component({
  selector: 'app-ticket-create',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <h1 class="page-title">Créer un ticket</h1>
    <div class="card" style="max-width:600px">
      @if (error) { <div class="alert alert-danger">{{ error }}</div> }
      <form [formGroup]="form" (ngSubmit)="submit()">
        <div class="grid-2">
          <div class="form-group"><label>Nom du passager</label><input class="form-control" formControlName="passengerName" /></div>
          <div class="form-group"><label>Email</label><input class="form-control" type="email" formControlName="passengerEmail" /></div>
        </div>
        <div class="form-group"><label>Ligne / Route</label><input class="form-control" formControlName="routeName" /></div>
        <div class="grid-2">
          <div class="form-group"><label>Départ</label><input class="form-control" formControlName="departureLocation" /></div>
          <div class="form-group"><label>Arrivée</label><input class="form-control" formControlName="arrivalLocation" /></div>
        </div>
        <div class="grid-2">
          <div class="form-group"><label>Date/heure départ</label><input class="form-control" type="datetime-local" formControlName="departureTime" /></div>
          <div class="form-group"><label>Date/heure arrivée</label><input class="form-control" type="datetime-local" formControlName="arrivalTime" /></div>
        </div>
        <div class="grid-2">
          <div class="form-group"><label>Prix (€)</label><input class="form-control" type="number" formControlName="price" /></div>
          <div class="form-group"><label>Utilisations max</label><input class="form-control" type="number" formControlName="maxUsageCount" /></div>
        </div>
        <button class="btn btn-primary" type="submit" [disabled]="loading">{{ loading ? 'Création...' : 'Créer le ticket' }}</button>
      </form>
    </div>
  `
})
export class TicketCreateComponent {
  private fb = inject(FormBuilder);
  private svc = inject(TicketService);
  private router = inject(Router);
  form = this.fb.group({
    passengerName: ['', Validators.required], passengerEmail: ['', [Validators.required, Validators.email]],
    routeName: ['', Validators.required], departureLocation: ['', Validators.required],
    arrivalLocation: ['', Validators.required], departureTime: ['', Validators.required],
    arrivalTime: ['', Validators.required], price: [0, Validators.required], maxUsageCount: [1]
  });
  error = ''; loading = false;
  submit() {
    if (this.form.invalid) return;
    this.loading = true; this.error = '';
    const v = this.form.value;
    const req: any = { ...v, departureTime: new Date(v.departureTime!).toISOString(), arrivalTime: new Date(v.arrivalTime!).toISOString() };
    this.svc.createTicket(req).subscribe({
      next: t => this.router.navigate(['/tickets', t.id]),
      error: () => { this.error = 'Erreur lors de la création'; this.loading = false; }
    });
  }
}
