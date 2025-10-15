import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TimetableService } from '../../services/timetable.service';
import { AuthService } from '../../services/auth.service';
import { TimetableItem } from '../../interfaces/timetable.interface';

interface ScheduleEvent {
  title: string;
  start: Date;
  end: Date;
  color: string;
  courseId: number;
  roomNumber: string;
  buildingName: string;
  instructorName?: string | null;
  studentCount: number;
}

@Component({
  selector: 'app-schedule',
  template: `
    <div class="schedule-container">
      <!-- Header -->
      <div class="schedule-header">
        <div class="header-content">
          <div class="header-left">
            <button class="back-btn" (click)="goBack()">
              <i class="pi pi-arrow-left"></i>
            </button>
            <div class="header-brand">
              <i class="pi pi-calendar brand-icon"></i>
              <span class="brand-text">Órarend</span>
            </div>
          </div>
          <div class="header-user">
            <span class="user-name">{{ currentUserName }}</span>
            <button class="logout-btn" (click)="onLogout()" [disabled]="loading">
              <i class="pi pi-sign-out"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="schedule-content">
        <div class="content-wrapper">
          <!-- Week Navigation -->
          <p-card class="week-nav-card">
            <div class="week-navigation">
              <button class="nav-button" (click)="previousWeek()">
                <i class="pi pi-angle-left"></i>
              </button>
              <div class="week-info">
                <h3>{{ getWeekLabel() }}</h3>
                <p>{{ getDateRangeLabel() }}</p>
              </div>
              <button class="nav-button" (click)="nextWeek()">
                <i class="pi pi-angle-right"></i>
              </button>
            </div>
          </p-card>

          <!-- Weekly Calendar -->
          <p-card class="calendar-card">
            <div class="weekly-calendar">
              <!-- Time Column -->
              <div class="time-column">
                <div class="time-header"></div>
                <div *ngFor="let hour of timeSlots" class="time-slot">
                  {{ hour }}
                </div>
              </div>

              <!-- Day Columns -->
              <div *ngFor="let day of weekDays; let i = index" class="day-column">
                <div class="day-header" [class.today]="isToday(day)">
                  <div class="day-name">{{ getDayName(i) }}</div>
                  <div class="day-date">{{ day.getDate() }}</div>
                </div>
                <div class="day-grid">
                  <div *ngFor="let hour of timeSlots" class="grid-cell"></div>

                  <!-- Events -->
                  <div *ngFor="let event of getEventsForDay(i)"
                       class="schedule-event"
                       [style.top.px]="getEventTop(event)"
                       [style.height.px]="getEventHeight(event)"
                       [style.background-color]="event.color"
                       [title]="getEventTooltip(event)">
                    <div class="event-content">
                      <div class="event-title">{{ event.title }}</div>
                      <div class="event-time">
                        {{ formatTime(event.start) }} - {{ formatTime(event.end) }}
                      </div>
                      <div class="event-location" *ngIf="getEventHeight(event) > 60">
                        <i class="pi pi-map-marker"></i> {{ event.buildingName }} - {{ event.roomNumber }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </p-card>

          <!-- Loading State -->
          <div *ngIf="loading" class="loading-overlay">
            <i class="pi pi-spin pi-spinner" style="font-size: 2rem"></i>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @import '../../../styles/variables';

    .schedule-container {
      min-height: 100vh;
      background: var(--surface-ground);
    }

    .schedule-header {
      background: var(--surface-card);
      border-bottom: 1px solid var(--surface-border);
      box-shadow: var(--shadow-sm);
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .header-content {
      max-width: 100%;
      margin: 0 auto;
      padding: 0 $spacing-xl;
      height: 64px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: $spacing-md;
    }

    .back-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border: none;
      border-radius: 50%;
      background: transparent;
      color: var(--text-color-secondary);
      cursor: pointer;
      transition: all 0.2s ease;
      font-size: 1.2rem;

      &:hover {
        background: var(--surface-hover);
        color: var(--primary-color);
      }
    }

    .header-brand {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .brand-icon {
        font-size: 1.5rem;
        color: var(--primary-color);
      }

      .brand-text {
        font-size: $font-size-lg;
        font-weight: 600;
        color: var(--text-color);
      }
    }

    .header-user {
      display: flex;
      align-items: center;
      gap: $spacing-lg;

      .user-name {
        font-size: $font-size-base;
        font-weight: 500;
        color: var(--text-color);
      }

      .logout-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        border: none;
        border-radius: 50%;
        background: transparent;
        color: var(--text-color-secondary);
        cursor: pointer;
        transition: all 0.2s ease;
        font-size: 1.2rem;

        &:hover {
          background: var(--surface-hover);
          color: var(--primary-color);
        }
      }
    }

    .schedule-content {
      padding: 0;
    }

    .content-wrapper {
      max-width: 100%;
      margin: 0 auto;
      padding: $spacing-lg;
      display: flex;
      flex-direction: column;
      gap: $spacing-lg;
    }

    .week-nav-card {
      ::ng-deep .p-card-body {
        padding: $spacing-lg;
      }
    }

    .week-navigation {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: $spacing-lg;

      .nav-button {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 50px;
        height: 50px;
        border: 2px solid var(--primary-color);
        border-radius: 50%;
        background: white;
        color: var(--primary-color);
        cursor: pointer;
        transition: all 0.3s ease;
        font-size: 1.8rem;

        &:hover {
          background: var(--primary-color);
          color: white;
          transform: scale(1.1);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        &:active {
          transform: scale(0.95);
        }
      }

      .week-info {
        flex: 1;
        text-align: center;

        h3 {
          margin: 0;
          font-size: $font-size-xl;
          font-weight: 600;
          color: var(--text-color);
        }

        p {
          margin: $spacing-xs 0 0 0;
          font-size: $font-size-sm;
          color: var(--text-color-secondary);
        }
      }
    }

    .calendar-card {
      ::ng-deep .p-card-body {
        padding: 0;
      }
    }

    .weekly-calendar {
      display: flex;
      overflow-x: auto;
      height: calc(100vh - 280px);
    }

    .time-column {
      flex-shrink: 0;
      width: 80px;
      border-right: 1px solid var(--surface-border);
    }

    .time-header {
      height: 50px;
      border-bottom: 1px solid var(--surface-border);
    }

    .time-slot {
      height: 50px;
      padding: $spacing-sm;
      border-bottom: 1px solid var(--surface-border);
      font-size: $font-size-sm;
      color: var(--text-color-secondary);
      text-align: right;
    }

    .day-column {
      flex: 1;
      min-width: 150px;
      border-right: 1px solid var(--surface-border);
      position: relative;

      &:last-child {
        border-right: none;
      }
    }

    .day-header {
      height: 50px;
      border-bottom: 2px solid var(--surface-border);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: var(--surface-section);

      &.today {
        background: var(--primary-color);
        color: white;

        .day-name, .day-date {
          color: white;
        }
      }

      .day-name {
        font-size: $font-size-sm;
        font-weight: 600;
        color: var(--text-color);
        text-transform: uppercase;
      }

      .day-date {
        font-size: $font-size-lg;
        font-weight: 700;
        color: var(--text-color);
        margin-top: 2px;
      }
    }

    .day-grid {
      position: relative;
    }

    .grid-cell {
      height: 50px;
      border-bottom: 1px solid var(--surface-border);
    }

    .schedule-event {
      position: absolute;
      left: 2px;
      right: 2px;
      border-radius: $border-radius-md;
      padding: $spacing-sm;
      color: white;
      cursor: pointer;
      transition: all 0.2s ease;
      overflow: hidden;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        z-index: 10;
      }
    }

    .event-content {
      .event-title {
        font-weight: 600;
        font-size: $font-size-sm;
        margin-bottom: 2px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .event-time {
        font-size: 0.75rem;
        opacity: 0.9;
      }

      .event-location {
        font-size: 0.7rem;
        opacity: 0.85;
        margin-top: 4px;
        display: flex;
        align-items: center;
        gap: 4px;

        i {
          font-size: 0.65rem;
        }
      }
    }

    .loading-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    @media (max-width: 768px) {
      .time-column {
        width: 60px;
      }

      .day-column {
        min-width: 100px;
      }

      .content-wrapper {
        padding: $spacing-md;
      }
    }
  `]
})
export class ScheduleComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  currentUserName: string = '';
  loading: boolean = false;
  currentWeekStart: Date = new Date();
  weekDays: Date[] = [];
  timeSlots: string[] = [];
  events: ScheduleEvent[] = [];

  constructor(
    private timetableService: TimetableService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user) {
          this.currentUserName = user.firstName || user.username;
          this.loadSchedule(user.id);
        }
      });

    this.initializeWeek();
    this.generateTimeSlots();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initializeWeek(): void {
    const now = new Date();
    const dayOfWeek = now.getDay();
    const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek; // Get Monday

    this.currentWeekStart = new Date(now);
    this.currentWeekStart.setDate(now.getDate() + diff);
    this.currentWeekStart.setHours(0, 0, 0, 0);

    this.generateWeekDays();
  }

  generateWeekDays(): void {
    this.weekDays = [];
    for (let i = 0; i < 5; i++) { // Monday to Friday
      const day = new Date(this.currentWeekStart);
      day.setDate(this.currentWeekStart.getDate() + i);
      this.weekDays.push(day);
    }
  }

  generateTimeSlots(): void {
    this.timeSlots = [];
    for (let hour = 8; hour <= 18; hour++) {
      this.timeSlots.push(`${hour.toString().padStart(2, '0')}:00`);
    }
  }

  loadSchedule(userId: number | string): void {
    this.loading = true;
    this.timetableService.getUserTimetable(userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Timetable response:', response);
          // Convert timetable items to schedule events
          this.events = this.convertTimetableToEvents(response.timetableItems);
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading schedule:', error);
          this.loading = false;
        }
      });
  }

  /**
   * Convert timetable items to schedule events
   */
  private convertTimetableToEvents(items: TimetableItem[]): ScheduleEvent[] {
    const colors = [
      '#4CAF50', '#2196F3', '#FF9800', '#9C27B0', '#F44336',
      '#00BCD4', '#FFEB3B', '#E91E63', '#3F51B5', '#FF5722'
    ];

    return items.map((item, index) => ({
      title: item.subjectName,
      start: new Date(item.startTime),
      end: new Date(item.endTime),
      color: colors[index % colors.length],
      courseId: item.courseId,
      roomNumber: item.roomNumber,
      buildingName: item.buildingName,
      instructorName: item.instructorName,
      studentCount: item.studentCount
    }));
  }

  previousWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() - 7);
    this.generateWeekDays();
    const user = this.authService.getCurrentUser();
    if (user) {
      this.loadSchedule(user.id);
    }
  }

  nextWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
    this.generateWeekDays();
    const user = this.authService.getCurrentUser();
    if (user) {
      this.loadSchedule(user.id);
    }
  }

  getWeekLabel(): string {
    const monthNames = [
      'Január', 'Február', 'Március', 'Április', 'Május', 'Június',
      'Július', 'Augusztus', 'Szeptember', 'Október', 'November', 'December'
    ];
    return monthNames[this.currentWeekStart.getMonth()];
  }

  getDateRangeLabel(): string {
    const endDate = new Date(this.currentWeekStart);
    endDate.setDate(this.currentWeekStart.getDate() + 4);
    return `${this.formatDate(this.currentWeekStart)} - ${this.formatDate(endDate)}`;
  }

  formatDate(date: Date): string {
    return `${date.getFullYear()}.${(date.getMonth() + 1).toString().padStart(2, '0')}.${date.getDate().toString().padStart(2, '0')}.`;
  }

  getDayName(index: number): string {
    const days = ['Hétfő', 'Kedd', 'Szerda', 'Csütörtök', 'Péntek'];
    return days[index];
  }

  isToday(day: Date): boolean {
    const today = new Date();
    return day.getDate() === today.getDate() &&
           day.getMonth() === today.getMonth() &&
           day.getFullYear() === today.getFullYear();
  }

  getEventsForDay(dayIndex: number): ScheduleEvent[] {
    const dayDate = this.weekDays[dayIndex];
    return this.events.filter(event => {
      return event.start.getDate() === dayDate.getDate() &&
             event.start.getMonth() === dayDate.getMonth() &&
             event.start.getFullYear() === dayDate.getFullYear();
    });
  }

  getEventTop(event: ScheduleEvent): number {
    const startHour = event.start.getHours();
    const startMinute = event.start.getMinutes();
    return ((startHour - 8) * 50 + (startMinute * 50 / 60));
  }

  getEventHeight(event: ScheduleEvent): number {
    const duration = (event.end.getTime() - event.start.getTime()) / (1000 * 60);
    return (duration * 50 / 60);
  }

  formatTime(date: Date): string {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }

  getEventTooltip(event: ScheduleEvent): string {
    const parts = [
      event.title,
      `${this.formatTime(event.start)} - ${this.formatTime(event.end)}`,
      `${event.buildingName} - ${event.roomNumber}`,
      `${event.studentCount} hallgató`
    ];

    if (event.instructorName) {
      parts.push(`Oktató: ${event.instructorName}`);
    }

    return parts.join('\n');
  }

  goBack(): void {
    const dashboardRoute = this.authService.getDashboardRoute();
    this.router.navigate([dashboardRoute]);
  }

  onLogout(): void {
    this.loading = true;
    this.authService.logout();
  }
}
