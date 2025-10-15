import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { StudentService } from '../../services/student.service';
import { AuthService } from '../../services/auth.service';
import { Student } from '../../interfaces/student.interface';

@Component({
  selector: 'app-students-list',
  templateUrl: './students-list.component.html',
  styleUrls: ['./students-list.component.scss']
})
export class StudentsListComponent implements OnInit, OnDestroy {
  students: Student[] = [];
  loading = false;
  errorMessage = '';

  // Pagination
  first = 0;
  rows = 10;
  rowsPerPageOptions = [5, 10, 25, 50];

  private destroy$ = new Subject<void>();

  constructor(
    private studentService: StudentService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStudents();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load students from API
   */
  loadStudents(): void {
    this.loading = true;
    this.errorMessage = '';

    this.studentService.getStudents()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (students) => {
          this.students = students;
          this.loading = false;
          console.log('Loaded students:', students);
        },
        error: (error) => {
          console.error('Error loading students:', error);
          this.errorMessage = error.message || 'Failed to load students';
          this.loading = false;
        }
      });
  }

  /**
   * Get display name for student
   */
  getStudentDisplayName(student: Student): string {
    if (student.name) {
      return student.name;
    }
    if (student.firstName && student.lastName) {
      return `${student.lastName} ${student.firstName}`;
    }
    if (student.firstName) {
      return student.firstName;
    }
    if (student.lastName) {
      return student.lastName;
    }
    return student.email.split('@')[0];
  }

  /**
   * Go back to dashboard
   */
  goBack(): void {
    const dashboardRoute = this.authService.getDashboardRoute();
    this.router.navigate([dashboardRoute]);
  }

  /**
   * Handle logout
   */
  onLogout(): void {
    this.loading = true;
    this.authService.logout();
  }

  /**
   * Get current user name
   */
  getCurrentUserName(): string {
    const user = this.authService.getCurrentUser();
    if (!user) return '';
    return user.name || user.firstName || user.username || user.email.split('@')[0];
  }
}
