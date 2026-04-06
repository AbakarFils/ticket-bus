export type TicketStatus = 'ACTIVE' | 'USED' | 'EXPIRED' | 'CANCELLED' | 'BLACKLISTED';
export interface Ticket {
  id: string; ticketNumber: string; passengerName: string; passengerEmail: string;
  routeName: string; departureLocation: string; arrivalLocation: string;
  departureTime: string; arrivalTime: string; price: number; status: TicketStatus;
  nonce: string; usageCount: number; maxUsageCount: number;
  activationWindowStart?: string; activationWindowEnd?: string;
  qrCodeData: string; createdAt: string; updatedAt: string;
}
export interface TicketRequest {
  passengerName: string; passengerEmail: string; routeName: string;
  departureLocation: string; arrivalLocation: string;
  departureTime: string; arrivalTime: string; price: number;
  activationWindowStart?: string; activationWindowEnd?: string; maxUsageCount?: number;
}
export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; }
