import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { SignupComponent } from './pages/signup/signup.component';
import { LoginComponent } from './pages/login/login.component';
import { HomeComponent } from './pages/home/home.component';
import {ProfileComponent} from './pages/profile/profile.component';
import {ClubPageComponent} from './pages/club-page/club-page.component';
import {ClubComponent} from './components/club/club.component';
import {AdminDashboardComponent} from './components/admin-dashboard/admin-dashboard.component';
import {AgentDashboardComponent} from './components/agent-dashboard/agent-dashboard.component';
import {AuthGuard} from './guards/auth.guard';
import {AgentGuard} from './guards/agent.guard';
import {AdminGuard} from './guards/admin.guard';
import {AuditLogsComponent} from './pages/audit-logs/audit-logs.component';
import {RootGuard} from './guards/root.guard';
import {LobbyDetailComponent} from './pages/lobby-detail/lobby-detail.component';
import {LobbyHomeComponent} from './pages/lobby-home/lobby-home.component';
import {SupportComponent} from './pages/support/support.component';
import {TicketDetailComponent} from './components/ticket-detail/ticket-detail.component';
import {FriendsComponent} from './pages/friends/friends.component';
import {ResetPasswordComponent} from './components/reset-password/reset-password.component';
import {ForgotPasswordComponent} from './components/forgot-password/forgot-password.component';
import {VerifyEmailComponent} from './components/verify-email/verify-email.component';
import {GamesComponent} from './pages/games/games.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'verify-email', component: VerifyEmailComponent},
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'friends', component:FriendsComponent, canActivate: [AuthGuard] },
  { path: 'clubs', component: ClubPageComponent, canActivate: [AuthGuard] },
  { path: 'club/:id', component: ClubComponent, canActivate: [AuthGuard] },
  { path: 'games', component: GamesComponent, canActivate: [AuthGuard] },
  { path: 'lobby', component: LobbyHomeComponent, canActivate: [AuthGuard] },
  { path: 'support', component: SupportComponent},
  { path: 'ticket/:id', component: TicketDetailComponent},
  { path: 'lobby/:id', component: LobbyDetailComponent, canActivate: [AuthGuard] },
  { path: "admin", component: AdminDashboardComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: "agent", component: AgentDashboardComponent, canActivate: [AuthGuard, AgentGuard] },
  { path: "audit-logs", component: AuditLogsComponent, canActivate: [AuthGuard, RootGuard] },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
