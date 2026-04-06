import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthRequest, AuthResponse, UserRole } from '../models/user.model';
import { ApiService } from './api.service';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private api = inject(ApiService);
  private router = inject(Router);
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getStoredUser());
  currentUser$ = this.currentUserSubject.asObservable();

  login(req: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api.baseUrl}/auth/login`, req).pipe(
      tap(res => { localStorage.setItem(this.TOKEN_KEY, res.token); localStorage.setItem(this.USER_KEY, JSON.stringify(res)); this.currentUserSubject.next(res); })
    );
  }
  logout(): void { localStorage.removeItem(this.TOKEN_KEY); localStorage.removeItem(this.USER_KEY); this.currentUserSubject.next(null); this.router.navigate(['/login']); }
  getToken(): string | null { return localStorage.getItem(this.TOKEN_KEY); }
  isAuthenticated(): boolean { return !!this.getToken(); }
  getCurrentUser(): AuthResponse | null { return this.currentUserSubject.value; }
  getRole(): UserRole | null { return this.getCurrentUser()?.role ?? null; }
  private getStoredUser(): AuthResponse | null { const d = localStorage.getItem(this.USER_KEY); return d ? JSON.parse(d) : null; }
}
