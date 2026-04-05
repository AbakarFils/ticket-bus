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

export interface BestFareRecommendation {
  recommendedProduct: Product;
  estimatedCost: number;
  reason: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.base}/api/pricing/products`);
  }

  createProduct(p: Partial<Product>): Observable<Product> {
    return this.http.post<Product>(`${this.base}/api/pricing/products`, p);
  }

  getValidationEvents(): Observable<ValidationEvent[]> {
    return this.http.get<ValidationEvent[]>(`${this.base}/api/validate/events`);
  }

  getValidationEventsByTicket(ticketId: number): Observable<ValidationEvent[]> {
    return this.http.get<ValidationEvent[]>(`${this.base}/api/validate/events/ticket/${ticketId}`);
  }

  getWallet(userId: number): Observable<Wallet> {
    return this.http.get<Wallet>(`${this.base}/api/wallets/${userId}`);
  }

  topUpWallet(userId: number, amount: number): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.base}/api/wallets/${userId}/topup`, { amount });
  }

  getBestFare(userId: number, tripsPerMonth: number): Observable<BestFareRecommendation> {
    return this.http.get<BestFareRecommendation>(
      `${this.base}/api/pricing/recommend?userId=${userId}&tripsPerMonth=${tripsPerMonth}`
    );
  }
}
