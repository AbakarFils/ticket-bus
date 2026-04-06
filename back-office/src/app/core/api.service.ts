import { Injectable } from '@angular/core';
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

export interface Customer {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  role: string;
  active: boolean;
  createdAt: string;
}

export interface FraudAlert {
  id: number;
  ticketId: number;
  userId: number;
  terminalId: string;
  alertType: string;
  description: string;
  resolved: boolean;
  createdAt: string;
}

export interface PaymentTransaction {
  id: number;
  transactionId: string;
  userId: number;
  amount: number;
  currency: string;
  method: string;
  status: string;
  reference: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Products
  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.base}/api/pricing/products`);
  }

  createProduct(p: Partial<Product>): Observable<Product> {
    return this.http.post<Product>(`${this.base}/api/pricing/products`, p);
  }

  // Validation Events
  getValidationEvents(): Observable<ValidationEvent[]> {
    return this.http.get<ValidationEvent[]>(`${this.base}/api/validate/events`);
  }

  getValidationEventsByTicket(ticketId: number): Observable<ValidationEvent[]> {
    return this.http.get<ValidationEvent[]>(`${this.base}/api/validate/events/ticket/${ticketId}`);
  }

  // Wallets
  getWallet(userId: number): Observable<Wallet> {
    return this.http.get<Wallet>(`${this.base}/api/wallets/${userId}`);
  }

  topUpWallet(userId: number, amount: number): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.base}/api/wallets/${userId}/topup`, { amount });
  }

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
  getBestFare(userId: number, tripsPerMonth: number): Observable<BestFareRecommendation> {
    return this.http.get<BestFareRecommendation>(
      `${this.base}/api/pricing/recommend?userId=${userId}&tripsPerMonth=${tripsPerMonth}`
    );
  }

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
}
