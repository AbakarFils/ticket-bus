import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BlacklistEntry, PublicKeyInfo, SyncResponse } from '../models/sync.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class SyncService {
  private http = inject(HttpClient);
  private api = inject(ApiService);
  uploadOffline(deviceId: string, events: any[]): Observable<SyncResponse> {
    return this.http.post<SyncResponse>(`${this.api.baseUrl}/sync/upload`, { deviceId, validationEvents: events });
  }
  getBlacklist(since?: string): Observable<BlacklistEntry[]> {
    return this.http.get<BlacklistEntry[]>(`${this.api.baseUrl}/sync/blacklist${since ? '?since=' + since : ''}`);
  }
  getPublicKey(): Observable<PublicKeyInfo> { return this.http.get<PublicKeyInfo>(`${this.api.baseUrl}/sync/public-key`); }
}
