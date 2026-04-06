export type ValidationStatus = 'VALID' | 'INVALID' | 'SUSPECT' | 'OFFLINE_PENDING';
export interface ValidationEvent {
  id: string; validatorDeviceId: string; validationTime: string;
  location: string; latitude?: number; longitude?: number;
  status: ValidationStatus; rejectionReason?: string; synced: boolean; createdAt: string;
}
export interface ValidationRequest { qrCodeData: string; deviceId: string; location: string; latitude?: number; longitude?: number; validationTime: string; }
export interface ValidationResponse { valid: boolean; status: ValidationStatus; message: string; ticketDetails?: any; }
