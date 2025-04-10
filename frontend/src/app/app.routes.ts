import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { SignupComponent } from './pages/signup/signup.component';
import { LoginComponent } from './pages/login/login.component';
import { HomeComponent } from './pages/home/home.component';
import {ProfileComponent} from './pages/profile/profile.component';
import {SocialComponent} from './pages/social/social.component';
import {ClubPageComponent} from './pages/club-page/club-page.component';
import {ClubComponent} from './components/club/club.component';
import {AdminDashboardComponent} from './components/admin-dashboard/admin-dashboard.component';
import {AgentDashboardComponent} from './components/agent-dashboard/agent-dashboard.component';
import {AuthGuard} from './guards/auth.guard';
import {AgentGuard} from './guards/agent.guard';
import {AdminGuard} from './guards/admin.guard';
import {AuditLogsComponent} from './components/audit-logs/audit-logs.component';
import {RootGuard} from './guards/root.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'social', component: SocialComponent },
  { path: 'clubs', component: ClubPageComponent},
  { path: 'club/:id', component: ClubComponent },
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
