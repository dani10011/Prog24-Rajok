import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { TimetableResponse, TimetableItem, DaySchedule } from '../interfaces/timetable.interface';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class TimetableService {
  private readonly apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  /**
   * Get user's timetable from the API
   * Note: JWT token is automatically added by AuthInterceptor
   */
  getUserTimetable(userId?: string | number): Observable<TimetableResponse> {
    // If no userId provided, get from current user
    if (!userId) {
      const currentUser = this.authService.getCurrentUser();
      if (!currentUser) {
        return throwError(() => new Error('No user logged in'));
      }
      userId = currentUser.id;
    }

    const url = `${this.apiUrl}/api/Timetable/GetUserTimetable?userId=${userId}`;
    return this.http.get<TimetableResponse>(url).pipe(
      map(response => {
        console.log('Timetable response:', response);
        return response;
      }),
      catchError(error => {
        console.error('Error fetching timetable:', error);
        let errorMessage = 'Failed to load timetable. Please try again.';

        if (error.status === 401) {
          errorMessage = 'Unauthorized. Please login again.';
        } else if (error.status === 404) {
          errorMessage = 'Timetable not found.';
        } else if (error.status === 0) {
          errorMessage = 'Cannot connect to server. Please check your connection.';
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        }

        return throwError(() => new Error(errorMessage));
      })
    );
  }

  /**
   * Group timetable items by day of week
   */
  groupByDayOfWeek(items: TimetableItem[]): Map<string, TimetableItem[]> {
    const grouped = new Map<string, TimetableItem[]>();

    // Define day order
    const dayOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

    // Initialize map with all days
    dayOrder.forEach(day => grouped.set(day, []));

    // Group items by day
    items.forEach(item => {
      const day = item.dayOfWeek;
      if (!grouped.has(day)) {
        grouped.set(day, []);
      }
      grouped.get(day)!.push(item);
    });

    // Sort items within each day by start time
    grouped.forEach((dayItems, day) => {
      dayItems.sort((a, b) => {
        return new Date(a.startTime).getTime() - new Date(b.startTime).getTime();
      });
    });

    return grouped;
  }

  /**
   * Get current week's schedule with dates
   */
  getCurrentWeekSchedule(items: TimetableItem[]): DaySchedule[] {
    const grouped = this.groupByDayOfWeek(items);
    const schedule: DaySchedule[] = [];

    // Get current week's Monday
    const today = new Date();
    const currentDay = today.getDay(); // 0 = Sunday, 1 = Monday, etc.
    const diff = currentDay === 0 ? -6 : 1 - currentDay; // Adjust to get Monday
    const monday = new Date(today);
    monday.setDate(today.getDate() + diff);
    monday.setHours(0, 0, 0, 0);

    // Create schedule for each day of the week
    const dayOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
    dayOrder.forEach((dayName, index) => {
      const date = new Date(monday);
      date.setDate(monday.getDate() + index);

      schedule.push({
        date: date,
        dayOfWeek: dayName,
        items: grouped.get(dayName) || []
      });
    });

    return schedule;
  }

  /**
   * Format time from ISO string to HH:MM
   */
  formatTime(isoString: string): string {
    const date = new Date(isoString);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  /**
   * Get duration in minutes between start and end time
   */
  getDuration(startTime: string, endTime: string): number {
    const start = new Date(startTime).getTime();
    const end = new Date(endTime).getTime();
    return Math.round((end - start) / 60000); // Convert ms to minutes
  }
}
