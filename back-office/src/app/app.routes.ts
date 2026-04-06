import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProductsComponent } from './products/products.component';
import { ValidationEventsComponent } from './validation-events/validation-events.component';
import { WalletsComponent } from './wallets/wallets.component';
<<<<<<< HEAD
import { CustomersComponent } from './customers/customers.component';
import { FraudComponent } from './fraud/fraud.component';
import { TransactionsComponent } from './transactions/transactions.component';
=======
import { TicketsComponent } from './tickets/tickets.component';
import { FraudAlertsComponent } from './fraud-alerts/fraud-alerts.component';
import { PaymentsComponent } from './payments/payments.component';
import { CustomersComponent } from './customers/customers.component';
import { OperatorsComponent } from './operators/operators.component';
>>>>>>> 6a79295c (phase 3)

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'products', component: ProductsComponent },
  { path: 'tickets', component: TicketsComponent },
  { path: 'events', component: ValidationEventsComponent },
  { path: 'wallets', component: WalletsComponent },
<<<<<<< HEAD
  { path: 'customers', component: CustomersComponent },
  { path: 'fraud', component: FraudComponent },
  { path: 'transactions', component: TransactionsComponent },
=======
  { path: 'fraud-alerts', component: FraudAlertsComponent },
  { path: 'payments', component: PaymentsComponent },
  { path: 'customers', component: CustomersComponent },
  { path: 'operators', component: OperatorsComponent },
>>>>>>> 6a79295c (phase 3)
];
