import { Component, inject, OnInit } from '@angular/core';
import { AsyncPipe, SlicePipe } from '@angular/common';
import { OfflineService } from '../../../services/offline.service';
import { SyncService } from '../../../services/sync.service';
import { BlacklistEntry, PublicKeyInfo } from '../../../models/sync.model';

@Component({
  selector: 'app-sync-status',
  standalone: true,
  imports: [AsyncPipe, SlicePipe],
  template: `
    <h1 class="page-title">Synchronisation</h1>
    <div class="grid-2">
      <div class="card">
        <h2 class="section-title">Statut réseau</h2>
        <p><span class="status-dot" [class.online]="(offline.isOnline | async)" [class.offline]="!(offline.isOnline | async)"></span>
          {{ (offline.isOnline | async) ? 'En ligne' : 'Hors ligne' }}</p>
        <p style="margin-top:.5rem"><strong>Validations en attente:</strong> {{ pendingCount }}</p>
        <p><strong>Dernière sync:</strong> {{ lastSync || 'Jamais' }}</p>
        <button class="btn btn-primary" style="margin-top:1rem" (click)="syncNow()" [disabled]="!(offline.isOnline | async)">↻ Synchroniser maintenant</button>
        @if (synced) { <div class="alert alert-success" style="margin-top:1rem">✅ Synchronisation réussie</div> }
      </div>
      <div class="card">
        <h2 class="section-title">Cache local</h2>
        <p><strong>Blacklist en cache:</strong> {{ blacklistCount }} entrées</p>
        @if (publicKey) {
          <p><strong>Clé publique:</strong> {{ publicKey.algorithm }}</p>
          <p><strong>Émise le:</strong> {{ publicKey.issuedAt | slice:0:10 }}</p>
        }
        <button class="btn btn-secondary btn-sm" style="margin-top:1rem" (click)="fetchPublicKey()">↻ Mettre à jour la clé</button>
      </div>
    </div>
  `
})
export class SyncStatusComponent implements OnInit {
  offline = inject(OfflineService);
  private syncSvc = inject(SyncService);
  pendingCount = 0; lastSync: string | null = null; synced = false;
  blacklistCount = 0; publicKey: PublicKeyInfo | null = null;
  ngOnInit() {
    this.pendingCount = this.offline.getPendingCount();
    this.lastSync = this.offline.getLastSync();
    this.blacklistCount = this.offline.getCachedBlacklist().length;
    this.publicKey = this.offline.getCachedPublicKey();
  }
  syncNow() {
    this.offline.syncPending();
    this.syncSvc.getBlacklist(this.lastSync || undefined).subscribe({ next: b => { this.offline.cacheBlacklist(b); this.blacklistCount = b.length; }, error: () => {} });
    this.syncSvc.getPublicKey().subscribe({ next: k => { this.offline.cachePublicKey(k); this.publicKey = k; this.synced = true; }, error: () => {} });
    this.pendingCount = this.offline.getPendingCount();
    this.lastSync = new Date().toISOString();
  }
  fetchPublicKey() {
    this.syncSvc.getPublicKey().subscribe({ next: k => { this.offline.cachePublicKey(k); this.publicKey = k; }, error: () => {} });
  }
}
