import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Product {
  id: number;
  name: string;
  type: string;
  price: number;
  maxUsage: number;
  durationDays: number | null;
  operatorId: number | null;
  zoneCode: string | null;
  active: boolean;
}

export interface ValidationEvent {
  id: number;
  ticketId: number;
  terminalId: string;
  location: string;
  timestamp: string;
  offline: boolean;
  result: string;
}

export interface Wallet {
  id: number;
  userId: number;
  balance: number;
  currency: string;
  monthlyBudget: number | null;
  monthlySpent: number | null;
  alertThresholdPercent: number | null;
  budgetAlertTriggered: boolean;
}

export interface WalletTransaction {
  id: number;
  userId: number;
  amount: number;
  type: string;
  reference: string;
  description: string;
  timestamp: string;
}

export interface WalletBudget {
  userId: number;
  dailyLimit: number | null;
  weeklyLimit: number | null;
  monthlyLimit: number | null;
  dailySpent: number;
  weeklySpent: number;
  monthlySpent: number;
}

export interface BestFareRecommendation {
  recommendedProduct: Product;
  estimatedCost: number;
  reason: string;
}

<<<<<<< HEAD
export interface Customer {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  role: string;
  active: boolean;
  createdAt: string;
=======
export interface Ticket {
  id: number;
  userId: number;
  productId: number;
  productName: string;
  nonce: string;
  validFrom: string;
  validUntil: string;
  signature: string;
  usageCount: number;
  maxUsage: number;
  status: string;
  createdAt: string;
  qrPayload: string;
>>>>>>> 6a79295c (phase 3)
}

export interface FraudAlert {
  id: number;
  ticketId: number;
<<<<<<< HEAD
  userId: number;
  terminalId: string;
  alertType: string;
  description: string;
=======
  alertType: string;
  description: string;
  terminalId: string;
  location: string;
>>>>>>> 6a79295c (phase 3)
  resolved: boolean;
  createdAt: string;
}

export interface PaymentTransaction {
  id: number;
<<<<<<< HEAD
  transactionId: string;
  userId: number;
  amount: number;
  currency: string;
  method: string;
  status: string;
  reference: string;
=======
  userId: number;
  amount: number;
  currency: string;
  type: string;
  status: string;
  transactionRef: string;
  externalRef: string;
  paymentMethod: string;
  description: string;
>>>>>>> 6a79295c (phase 3)
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient, private ngZone: NgZone) {}

<<<<<<< HEAD
  // Products
=======
  // --- Products ---
>>>>>>> 6a79295c (phase 3)
  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.base}/api/pricing/products`);
  }

  createProduct(p: Partial<Product>): Observable<Product> {
    return this.http.post<Product>(`${this.base}/api/pricing/products`, p);
  }

<<<<<<< HEAD
  // Validation Events
=======
  // --- Validation Events ---
>>>>>>> 6a79295c (phase 3)
  getValidationEvents(): Observable<ValidationEvent[]> {
    return this.http.get<ValidationEvent[]>(`${this.base}/api/validate/events`);
  }

  getValidationEventsByTicket(ticketId: number): Observable<ValidationEvent[]> {
    return this.http.get<ValidationEvent[]>(`${this.base}/api/validate/events/ticket/${ticketId}`);
  }

<<<<<<< HEAD
  // Wallets
=======
  // --- Wallets ---
>>>>>>> 6a79295c (phase 3)
  getWallet(userId: number): Observable<Wallet> {
    return this.http.get<Wallet>(`${this.base}/api/wallets/${userId}`);
  }

  getAllWallets(): Observable<Wallet[]> {
    return this.http.get<Wallet[]>(`${this.base}/api/wallets`);
  }

  topUpWallet(userId: number, amount: number): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.base}/api/wallets/${userId}/topup`, { amount });
  }

<<<<<<< HEAD
  getWalletTransactions(userId: number): Observable<WalletTransaction[]> {
    return this.http.get<WalletTransaction[]>(`${this.base}/api/wallets/${userId}/transactions`);
  }

  getWalletBudget(userId: number): Observable<WalletBudget> {
    return this.http.get<WalletBudget>(`${this.base}/api/wallets/${userId}/budget`);
  }

  setWalletBudget(userId: number, budget: Partial<WalletBudget>): Observable<WalletBudget> {
    return this.http.post<WalletBudget>(`${this.base}/api/wallets/${userId}/budget`, budget);
  }

  // Pricing
=======
  setMonthlyBudget(userId: number, monthlyBudget: number, alertThresholdPercent: number): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.base}/api/wallets/${userId}/budget`, {
      monthlyBudget, alertThresholdPercent
    });
  }

  // --- Pricing ---
>>>>>>> 6a79295c (phase 3)
  getBestFare(userId: number, tripsPerMonth: number): Observable<BestFareRecommendation> {
    return this.http.get<BestFareRecommendation>(
      `${this.base}/api/pricing/recommend?userId=${userId}&tripsPerMonth=${tripsPerMonth}`
    );
  }

<<<<<<< HEAD
  // Customers
  getCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.base}/api/customers`);
  }

  deactivateCustomer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/api/customers/${id}`);
  }

  // Fraud & Blacklist
  getFraudAlerts(): Observable<FraudAlert[]> {
    return this.http.get<FraudAlert[]>(`${this.base}/api/fraud/alerts`);
  }

  resolveFraudAlert(id: number): Observable<FraudAlert> {
    return this.http.put<FraudAlert>(`${this.base}/api/fraud/alerts/${id}/resolve`, {});
  }

  blacklistTicket(ticketId: number, reason: string, blacklistedBy: string): Observable<any> {
    return this.http.post(`${this.base}/api/fraud/blacklist`, { ticketId, reason, blacklistedBy });
  }

  // Payment Transactions
  getAllPaymentTransactions(): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.base}/api/payments/transactions`);
  }

  refundPayment(transactionId: string): Observable<any> {
    return this.http.post(`${this.base}/api/payments/refund/${transactionId}`, {});
  }
=======
  // --- Tickets ---
  getRecentTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.base}/api/tickets/recent`);
  }

  getTicketsByUser(userId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.base}/api/tickets/user/${userId}`);
  }

  getTicket(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.base}/api/tickets/${id}`);
  }

  revokeTicket(id: number): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.base}/api/tickets/${id}/revoke`, {});
  }

  purchaseTicket(userId: number, productId: number): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.base}/api/tickets/purchase`, { userId, productId });
  }

  // --- Fraud Alerts ---
  getFraudAlerts(): Observable<FraudAlert[]> {
    return this.http.get<FraudAlert[]>(`${this.base}/api/validate/fraud-alerts`);
  }

  getUnresolvedFraudAlerts(): Observable<FraudAlert[]> {
    return this.http.get<FraudAlert[]>(`${this.base}/api/validate/fraud-alerts/unresolved`);
  }

  resolveFraudAlert(id: number): Observable<FraudAlert> {
    return this.http.put<FraudAlert>(`${this.base}/api/validate/fraud-alerts/${id}/resolve`, {});
  }

  // --- Payments ---
  getRecentPayments(): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.base}/api/payments/recent`);
  }

  getPaymentsByUser(userId: number): Observable<PaymentTransaction[]> {
    return this.http.get<PaymentTransaction[]>(`${this.base}/api/payments/user/${userId}`);
  }

  initiatePayment(request: { userId: number; amount: number; currency?: string; paymentMethod?: string; type?: string }): Observable<any> {
    return this.http.post(`${this.base}/api/payments/initiate`, request);
  }

  refundPayment(transactionRef: string): Observable<any> {
    return this.http.post(`${this.base}/api/payments/refund/${transactionRef}`, {});
  }

  // --- Customers ---
  getRecentCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.base}/api/customers`);
  }

  getCustomer(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.base}/api/customers/${id}`);
  }

  createCustomer(c: Partial<Customer>): Observable<Customer> {
    return this.http.post<Customer>(`${this.base}/api/customers`, c);
  }

  updateCustomerProfile(id: number, updates: Partial<Customer>): Observable<Customer> {
    return this.http.put<Customer>(`${this.base}/api/customers/${id}/profile`, updates);
  }

  getCustomerTrips(id: number): Observable<TripHistory[]> {
    return this.http.get<TripHistory[]>(`${this.base}/api/customers/${id}/trips`);
  }

  // --- Operators ---
  getOperators(): Observable<TicketOperator[]> {
    return this.http.get<TicketOperator[]>(`${this.base}/api/pricing/operators`);
  }

  createOperator(op: Partial<TicketOperator>): Observable<TicketOperator> {
    return this.http.post<TicketOperator>(`${this.base}/api/pricing/operators`, op);
  }

  updateOperator(id: number, op: Partial<TicketOperator>): Observable<TicketOperator> {
    return this.http.put<TicketOperator>(`${this.base}/api/pricing/operators/${id}`, op);
  }

  // --- Zones ---
  getZones(): Observable<TransportZone[]> {
    return this.http.get<TransportZone[]>(`${this.base}/api/pricing/zones`);
  }

  getZonesByOperator(operatorId: number): Observable<TransportZone[]> {
    return this.http.get<TransportZone[]>(`${this.base}/api/pricing/zones/operator/${operatorId}`);
  }

  createZone(z: Partial<TransportZone>): Observable<TransportZone> {
    return this.http.post<TransportZone>(`${this.base}/api/pricing/zones`, z);
  }

  // --- Audit ---
  getAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.base}/api/audit/logs`);
  }

  getAuditLogsByService(serviceName: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.base}/api/audit/logs/service/${serviceName}`);
  }

  getAuditLogsByUser(userId: number): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.base}/api/audit/logs/user/${userId}`);
  }

  getAnomalyStats(): Observable<any> {
    return this.http.get(`${this.base}/api/audit/anomalies/stats`);
  }

  // --- SSE: Real-time fraud alerts ---
  streamFraudAlerts(): Observable<FraudAlert> {
    return new Observable(observer => {
      const url = `${this.base}/api/validate/fraud-alerts/stream`;
      const eventSource = new EventSource(url);
      eventSource.addEventListener('fraud-alert', (event: any) => {
        this.ngZone.run(() => {
          try {
            observer.next(JSON.parse(event.data));
          } catch (e) {
            observer.error(e);
          }
        });
      });
      eventSource.onerror = (err) => {
        this.ngZone.run(() => observer.error(err));
      };
      return () => eventSource.close();
    });
  }
}

// --- Phase 3 Interfaces ---

export interface Customer {
  id: number;
  keycloakId: string | null;
  email: string;
  firstName: string | null;
  lastName: string | null;
  phone: string | null;
  address: string | null;
  role: string;
  operatorId: number | null;
  active: boolean;
  createdAt: string;
}

export interface TripHistory {
  id: number;
  userId: number;
  ticketId: number;
  validationEventId: number;
  boardingZone: string;
  alightingZone: string;
  operatorId: number;
  timestamp: string;
}

export interface TicketOperator {
  id: number;
  code: string;
  name: string;
  contactEmail: string;
  phone: string;
  logoUrl: string;
  active: boolean;
  createdAt: string;
}

export interface TransportZone {
  id: number;
  code: string;
  name: string;
  operatorId: number | null;
  description: string;
  active: boolean;
}

export interface AuditLog {
  id: number;
  timestamp: string;
  serviceName: string;
  action: string;
  entityType: string;
  entityId: number;
  userId: number;
  operatorId: number;
  details: string;
  ipAddress: string;
>>>>>>> 6a79295c (phase 3)
}
