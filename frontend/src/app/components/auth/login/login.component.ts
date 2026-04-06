import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="login-wrap">
      <div class="card login-card">
        <h2>🚌 TicketBus</h2>
        <p>Connectez-vous à votre compte</p>
        @if (error) { <div class="alert alert-danger">{{ error }}</div> }
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-group">
            <label>Nom d'utilisateur</label>
            <input class="form-control" formControlName="username" />
          </div>
          <div class="form-group">
            <label>Mot de passe</label>
            <input class="form-control" type="password" formControlName="password" />
          </div>
          <button class="btn btn-primary" style="width:100%" type="submit" [disabled]="loading">
            {{ loading ? 'Connexion...' : 'Se connecter' }}
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`.login-wrap { min-height: 100vh; display: flex; align-items: center; justify-content: center; }
    .login-card { width: 100%; max-width: 380px; text-align: center; }
    h2 { font-size: 1.8rem; margin-bottom: .5rem; }
    p { color: #666; margin-bottom: 1.5rem; }
    .form-group { text-align: left; }`]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  form = this.fb.group({ username: ['', Validators.required], password: ['', Validators.required] });
  error = '';
  loading = false;
  submit() {
    if (this.form.invalid) return;
    this.loading = true; this.error = '';
    this.auth.login(this.form.value as any).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => { this.error = 'Identifiants invalides'; this.loading = false; }
    });
  }
}
