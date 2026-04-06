import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProductsComponent } from './products/products.component';
import { ValidationEventsComponent } from './validation-events/validation-events.component';
import { WalletsComponent } from './wallets/wallets.component';
import { CustomersComponent } from './customers/customers.component';
import { FraudComponent } from './fraud/fraud.component';
import { TransactionsComponent } from './transactions/transactions.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'products', component: ProductsComponent },
  { path: 'events', component: ValidationEventsComponent },
  { path: 'wallets', component: WalletsComponent },
  { path: 'customers', component: CustomersComponent },
  { path: 'fraud', component: FraudComponent },
  { path: 'transactions', component: TransactionsComponent },
];
