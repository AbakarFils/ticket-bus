import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProductsComponent } from './products/products.component';
import { ValidationEventsComponent } from './validation-events/validation-events.component';
import { WalletsComponent } from './wallets/wallets.component';
import { TicketsComponent } from './tickets/tickets.component';
import { QrValidatorComponent } from './qr-validator/qr-validator.component';
import { FraudAlertsComponent } from './fraud-alerts/fraud-alerts.component';
import { PaymentsComponent } from './payments/payments.component';
import { CustomersComponent } from './customers/customers.component';
import { OperatorsComponent } from './operators/operators.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'products', component: ProductsComponent },
  { path: 'tickets', component: TicketsComponent },
  { path: 'qr-scanner', component: QrValidatorComponent },
  { path: 'events', component: ValidationEventsComponent },
  { path: 'wallets', component: WalletsComponent },
  { path: 'fraud-alerts', component: FraudAlertsComponent },
  { path: 'payments', component: PaymentsComponent },
  { path: 'customers', component: CustomersComponent },
  { path: 'operators', component: OperatorsComponent },
];
