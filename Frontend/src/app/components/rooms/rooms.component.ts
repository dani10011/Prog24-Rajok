import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { RoomService } from '../../services/room.service';
import { CourseService } from '../../services/course.service';
import { SubjectService } from '../../services/subject.service';
import { AuthService } from '../../services/auth.service';
import { Room } from '../../interfaces/room';
import { User } from '../../interfaces/user.interface';
import { Subject as SubjectModel } from '../../interfaces/subject';
import { Course, CreateCourseRequest } from '../../interfaces/course';

@Component({
  selector: 'app-rooms',
  templateUrl: './rooms.component.html',
  styleUrls: ['./rooms.component.scss']
})
export class RoomsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  currentUser: User | null = null;
  currentUserName: string = '';
  loading: boolean = false;
  rooms: Room[] = [];
  errorMessage: string = '';

  // Booking modal properties
  displayBookingModal: boolean = false;
  selectedRoom: Room | null = null;
  bookingForm!: FormGroup;
  subjects: SubjectModel[] = [];
  loadingSubjects = false;
  submittingBooking = false;
  bookingError = '';
  instructorCourses: (Course & { displayName: string })[] = [];
  loadingCourses = false;

  constructor(
    private roomService: RoomService,
    private courseService: CourseService,
    private subjectService: SubjectService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.initializeBookingForm();
  }

  ngOnInit(): void {
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user) {
          this.currentUser = user;
          this.currentUserName = user.firstName || user.username;
          // Load instructor courses when user is available
          this.loadInstructorCourses();
        }
      });

    // Load subjects for booking form
    this.loadSubjects();

    // Load rooms
    this.loadRooms();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Initialize booking form
   */
  private initializeBookingForm(): void {
    this.bookingForm = this.fb.group({
      subjectId: [null, Validators.required],
      courseId: [null, Validators.required],
      startTime: [null, Validators.required],
      endTime: [null, Validators.required],
      nameSuffix: ['']
    });
  }

  /**
   * Load rooms from API
   */
  loadRooms(): void {
    this.loading = true;
    this.errorMessage = '';

    this.roomService.getRooms()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (rooms) => {
          this.rooms = rooms;
          this.loading = false;
          console.log('Loaded rooms:', rooms);
        },
        error: (error) => {
          console.error('Error loading rooms:', error);
          this.errorMessage = error.message || 'Failed to load rooms';
          this.loading = false;
        }
      });
  }

  /**
   * Load subjects from API
   */
  loadSubjects(): void {
    this.loadingSubjects = true;

    this.subjectService.getSubjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (subjects) => {
          this.subjects = subjects;
          this.loadingSubjects = false;
          console.log('Loaded subjects:', subjects);
        },
        error: (error) => {
          console.error('Error loading subjects:', error);
          this.loadingSubjects = false;
        }
      });
  }

  /**
   * Load instructor's courses
   */
  loadInstructorCourses(): void {
    if (!this.currentUser) {
      console.warn('Cannot load instructor courses: currentUser is null');
      return;
    }

    this.loadingCourses = true;

    // Parse user ID to number
    const userId = parseInt(this.currentUser.id, 10);

    console.log('Loading courses for instructor ID:', userId);

    this.courseService.getCoursesByUserId(userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (courses) => {
          console.log('Raw courses from API:', courses);

          // Add displayName for dropdown
          this.instructorCourses = courses.map(course => ({
            ...course,
            displayName: `${course.subjectName} - ${course.dayOfWeek} ${course.startTime}-${course.endTime}`
          }));
          this.loadingCourses = false;
          console.log('Loaded instructor courses:', this.instructorCourses);
        },
        error: (error) => {
          console.error('Error loading instructor courses:', error);
          this.loadingCourses = false;
          this.bookingError = 'Nem sikerült betölteni az órákat. Ellenőrizze a kapcsolatot.';
        }
      });
  }

  /**
   * Handle course selection - auto-fill times and subject
   */
  onCourseSelected(event: any): void {
    const courseId = event.value;
    if (!courseId) {
      // Clear form if deselected
      this.bookingForm.patchValue({
        subjectId: null,
        startTime: null,
        endTime: null
      });
      return;
    }

    const selectedCourse = this.instructorCourses.find(c => c.courseId === courseId);
    if (!selectedCourse) {
      return;
    }

    console.log('Selected course:', selectedCourse);

    // Find subject ID by matching subject name
    const matchingSubject = this.subjects.find(s =>
      s.name === selectedCourse.subjectName ||
      s.name.toLowerCase() === selectedCourse.subjectName.toLowerCase()
    );

    if (matchingSubject) {
      console.log('Found matching subject:', matchingSubject);
    } else {
      console.warn('Could not find matching subject for:', selectedCourse.subjectName);
    }

    // Create dates based on the next occurrence of the day of week
    const now = new Date();
    const targetDay = this.getDayOfWeekNumber(selectedCourse.dayOfWeek);
    const currentDay = now.getDay();

    // Calculate days until next occurrence
    let daysUntil = targetDay - currentDay;
    if (daysUntil <= 0) {
      daysUntil += 7; // Next week
    }

    const targetDate = new Date(now);
    targetDate.setDate(now.getDate() + daysUntil);

    // Parse time from format like "08:00" or "08:00:00"
    const startTimeParts = selectedCourse.startTime.split(':');
    const endTimeParts = selectedCourse.endTime.split(':');

    const startDateTime = new Date(targetDate);
    startDateTime.setHours(parseInt(startTimeParts[0], 10));
    startDateTime.setMinutes(parseInt(startTimeParts[1], 10));
    startDateTime.setSeconds(0);

    const endDateTime = new Date(targetDate);
    endDateTime.setHours(parseInt(endTimeParts[0], 10));
    endDateTime.setMinutes(parseInt(endTimeParts[1], 10));
    endDateTime.setSeconds(0);

    // Update form with all auto-filled values
    this.bookingForm.patchValue({
      subjectId: matchingSubject?.id || null,
      startTime: startDateTime,
      endTime: endDateTime
    });
  }

  /**
   * Get day of week number (0-6, Sunday-Saturday)
   */
  private getDayOfWeekNumber(dayName: string): number {
    const days: { [key: string]: number } = {
      'Vasárnap': 0,
      'Hétfő': 1,
      'Kedd': 2,
      'Szerda': 3,
      'Csütörtök': 4,
      'Péntek': 5,
      'Szombat': 6
    };
    return days[dayName] || 1; // Default to Monday if not found
  }

  /**
   * Get selected course's subject name for display
   */
  getSelectedCourseName(): string {
    const courseId = this.bookingForm.get('courseId')?.value;
    if (!courseId) return '';

    const course = this.instructorCourses.find(c => c.courseId === courseId);
    return course?.subjectName || '';
  }

  /**
   * Get room type label
   */
  getRoomTypeLabel(room: Room): string {
    return room.is_Computer_Room ? 'Számítógépes terem' : 'Tanterem';
  }

  /**
   * Navigate back to dashboard
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
   * Show booking modal for selected room
   */
  showBookingModal(room: Room): void {
    console.log('showBookingModal called with room:', room);
    console.log('Current instructor courses:', this.instructorCourses);
    console.log('Loading courses:', this.loadingCourses);

    this.selectedRoom = room;
    this.bookingError = '';
    this.bookingForm.reset();
    this.displayBookingModal = true;

    console.log('Modal should now be visible. displayBookingModal:', this.displayBookingModal);
  }

  /**
   * Hide booking modal
   */
  hideBookingModal(): void {
    this.displayBookingModal = false;
    this.selectedRoom = null;
    this.bookingForm.reset();
    this.bookingError = '';
  }

  /**
   * Confirm booking - call CreateCourse API
   */
  confirmBooking(): void {
    console.log('confirmBooking called');
    console.log('Form valid:', this.bookingForm.valid);
    console.log('Form value:', this.bookingForm.value);
    console.log('Form errors:', this.getFormValidationErrors());

    if (!this.selectedRoom) {
      this.bookingError = 'Nincs kiválasztott terem!';
      return;
    }

    if (!this.currentUser) {
      this.bookingError = 'Felhasználói információ hiányzik!';
      return;
    }

    if (this.bookingForm.invalid) {
      this.bookingError = 'Kérjük, töltse ki az összes kötelező mezőt!';
      return;
    }

    this.submittingBooking = true;
    this.bookingError = '';

    const formValue = this.bookingForm.value;

    // Parse user ID to number for API
    const instructorId = parseInt(this.currentUser.id, 10);

    // Create the request object
    const request: CreateCourseRequest = {
      subjectId: formValue.subjectId,
      instructorId: instructorId,
      startTime: this.formatDateTimeForApi(formValue.startTime),
      endTime: this.formatDateTimeForApi(formValue.endTime),
      roomId: this.selectedRoom.id,
      nameSuffix: formValue.nameSuffix || undefined
    };

    console.log('Creating course with request:', request);
    console.log('API URL:', 'POST /api/Course/CreateCourse');

    this.courseService.createCourse(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Course created successfully:', response);
          this.submittingBooking = false;
          alert(`Foglalás sikeresen létrehozva a(z) ${this.selectedRoom!.room_Number} teremben!`);
          this.hideBookingModal();

          // Optionally reload rooms
          // this.loadRooms();
        },
        error: (error) => {
          console.error('Error creating course:', error);
          console.error('Error details:', {
            status: error.status,
            statusText: error.statusText,
            message: error.message,
            error: error.error
          });
          this.bookingError = error.message || 'Hiba történt a foglalás során.';
          this.submittingBooking = false;
        }
      });
  }

  /**
   * Get form validation errors for debugging
   */
  private getFormValidationErrors(): any {
    const errors: any = {};
    Object.keys(this.bookingForm.controls).forEach(key => {
      const controlErrors = this.bookingForm.get(key)?.errors;
      if (controlErrors) {
        errors[key] = controlErrors;
      }
    });
    return errors;
  }

  /**
   * Format Date object to ISO string for API
   */
  private formatDateTimeForApi(date: Date): string {
    if (!date) return '';

    // Ensure date is a Date object
    const dateObj = date instanceof Date ? date : new Date(date);

    // Return ISO string format
    return dateObj.toISOString();
  }
}
