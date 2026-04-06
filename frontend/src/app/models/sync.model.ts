export interface BlacklistEntry { id: string; ticketNumber: string; reason: string; blacklistedAt: string; active: boolean; }
export interface PublicKeyInfo { key: string; algorithm: string; issuedAt: string; }
export interface SyncResponse { blacklistUpdates: BlacklistEntry[]; publicKey: PublicKeyInfo; processedEvents: number; }
