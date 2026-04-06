import { Injectable, inject, OnDestroy } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { SyncService } from './sync.service';
import { BlacklistEntry, PublicKeyInfo } from '../models/sync.model';

@Injectable({ providedIn: 'root' })
export class OfflineService implements OnDestroy {
  private readonly PENDING_KEY = 'pending_validations';
  private readonly BLACKLIST_KEY = 'cached_blacklist';
  private readonly PUBLIC_KEY_KEY = 'cached_public_key';
  private readonly LAST_SYNC_KEY = 'last_sync_time';
  private syncService = inject(SyncService);

  isOnline = new BehaviorSubject<boolean>(navigator.onLine);
  private onlineHandler = () => { this.isOnline.next(true); this.syncPending(); };
  private offlineHandler = () => this.isOnline.next(false);

  constructor() {
    window.addEventListener('online', this.onlineHandler);
    window.addEventListener('offline', this.offlineHandler);
  }
  ngOnDestroy(): void {
    window.removeEventListener('online', this.onlineHandler);
    window.removeEventListener('offline', this.offlineHandler);
  }
  addPending(event: any): void { const p = this.getPending(); p.push(event); localStorage.setItem(this.PENDING_KEY, JSON.stringify(p)); }
  getPending(): any[] { const d = localStorage.getItem(this.PENDING_KEY); return d ? JSON.parse(d) : []; }
  getPendingCount(): number { return this.getPending().length; }
  clearPending(): void { localStorage.removeItem(this.PENDING_KEY); }
  syncPending(): void {
    const p = this.getPending();
    if (!p.length || !this.isOnline.value) return;
    this.syncService.uploadOffline('web-client', p).subscribe({
      next: () => { this.clearPending(); localStorage.setItem(this.LAST_SYNC_KEY, new Date().toISOString()); },
      error: () => {}
    });
  }
  getLastSync(): string | null { return localStorage.getItem(this.LAST_SYNC_KEY); }
  cacheBlacklist(e: BlacklistEntry[]): void { localStorage.setItem(this.BLACKLIST_KEY, JSON.stringify(e)); }
  getCachedBlacklist(): BlacklistEntry[] { const d = localStorage.getItem(this.BLACKLIST_KEY); return d ? JSON.parse(d) : []; }
  cachePublicKey(k: PublicKeyInfo): void { localStorage.setItem(this.PUBLIC_KEY_KEY, JSON.stringify(k)); }
  getCachedPublicKey(): PublicKeyInfo | null { const d = localStorage.getItem(this.PUBLIC_KEY_KEY); return d ? JSON.parse(d) : null; }
}
