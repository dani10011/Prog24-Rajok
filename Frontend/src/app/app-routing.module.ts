import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Components
import { LoginComponent } from './components/login/login.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { TeacherDashboardComponent } from './components/teacher-dashboard/teacher-dashboard.component';
import { StudentDashboardComponent } from './components/student-dashboard/student-dashboard.component';
import { ScheduleComponent } from './components/schedule/schedule.component';
import { StudentsListComponent } from './components/students-list/students-list.component';
import { RoomsComponent } from './components/rooms/rooms.component';
import { CoursesComponent } from './components/courses/courses.component';
import { UsersManagementComponent } from './components/users-management/users-management.component';
import { RoomsManagementComponent } from './components/rooms-management/rooms-management.component';
import { SubjectsManagementComponent } from './components/subjects-management/subjects-management.component';

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
  // Instructor Dashboard (role_id: 2)
  {
    path: 'instructor-dashboard',
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
  // Schedule (protected route for students)
  {
    path: 'schedule',
    component: ScheduleComponent,
    canActivate: [AuthGuard]
  },
  // Students List (protected route for instructors)
  {
    path: 'students',
    component: StudentsListComponent,
    canActivate: [RoleGuard],
    data: { allowedRoles: [2] }
  },
  // Rooms (protected route for instructors)
  {
    path: 'rooms',
    component: RoomsComponent,
    canActivate: [RoleGuard],
    data: { allowedRoles: [2] }
  },
  // Courses (protected route for authenticated users - both students and instructors)
  {
    path: 'courses',
    component: CoursesComponent,
    canActivate: [AuthGuard]
  },
  // Users Management (protected route for admins only)
  {
    path: 'users-management',
    component: UsersManagementComponent,
    canActivate: [RoleGuard],
    data: { allowedRoles: [1] }
  },
  // Rooms Management (protected route for admins only)
  {
    path: 'rooms-management',
    component: RoomsManagementComponent,
    canActivate: [RoleGuard],
    data: { allowedRoles: [1] }
  },
  // Subjects Management (protected route for admins only)
  {
    path: 'subjects-management',
    component: SubjectsManagementComponent,
    canActivate: [RoleGuard],
    data: { allowedRoles: [1] }
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
