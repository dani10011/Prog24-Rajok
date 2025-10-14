import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Components
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { TeacherDashboardComponent } from './components/teacher-dashboard/teacher-dashboard.component';
import { StudentDashboardComponent } from './components/student-dashboard/student-dashboard.component';

// Guards
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

const routes: Routes = [
  // Default route - redirect to login
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  // Login route (public)
  {
    path: 'login',
    component: LoginComponent
  },
  // Admin Dashboard (role_id: 1)
  {
    path: 'admin-dashboard',
    component: AdminDashboardComponent,
    canActivate: [RoleGuard],
    data: { allowedRoles: [1] }
  },
  // Teacher Dashboard (role_id: 2)
  {
    path: 'teacher-dashboard',
    component: TeacherDashboardComponent,
    canActivate: [RoleGuard],
    data: { allowedRoles: [2] }
  },
  // Student Dashboard (role_id: 3)
  {
    path: 'student-dashboard',
    component: StudentDashboardComponent,
    canActivate: [RoleGuard],
    data: { allowedRoles: [3] }
  },
  // Legacy dashboard route (protected, kept for compatibility)
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  // Wildcard route - redirect to login
  {
    path: '**',
    redirectTo: '/login'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
