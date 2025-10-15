import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map, forkJoin, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { CourseStudent, Course, ScheduleEvent } from '../interfaces/course-student.interface';

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'ngrok-skip-browser-warning': 'true'
    });
  }

  getCourseStudents(studentId: number | string): Observable<CourseStudent[]> {
    const id = typeof studentId === 'string' ? parseInt(studentId, 10) : studentId;
    return this.http.get<CourseStudent[]>(
      `${this.apiUrl}/api/CourseStudent/GetCourseStudents`,
      { headers: this.getHeaders() }
    ).pipe(
      map(courses => courses.filter(cs => cs.student_id === id))
    );
  }

  getCourse(courseId: number): Observable<Course> {
    return this.http.get<Course>(
      `${this.apiUrl}/api/Course/${courseId}`,
      { headers: this.getHeaders() }
    );
  }

  getStudentSchedule(studentId: number | string, weekStart: Date): Observable<ScheduleEvent[]> {
    return this.getCourseStudents(studentId).pipe(
      map(courseStudents => {
        // Generate schedule events for the week
        const events: ScheduleEvent[] = [];
        const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a'];

        courseStudents.forEach((cs, index) => {
          // Create mock schedule events (this should be replaced with real course data)
          // For now, creating events for demonstration
          const dayOfWeek = index % 5; // Monday to Friday
          const startHour = 8 + (index % 4) * 2;

          const eventStart = new Date(weekStart);
          eventStart.setDate(weekStart.getDate() + dayOfWeek + 1); // +1 because week starts on Monday
          eventStart.setHours(startHour, 0, 0, 0);

          const eventEnd = new Date(eventStart);
          eventEnd.setHours(startHour + 2, 0, 0, 0);

          const mockCourse: Course = {
            id: cs.course_id,
            name: `Kurzus ${cs.course_id}`,
            day_of_week: dayOfWeek,
            start_time: `${startHour}:00`,
            end_time: `${startHour + 2}:00`,
            color: colors[index % colors.length]
          };

          events.push({
            id: cs.course_id,
            title: mockCourse.name,
            start: eventStart,
            end: eventEnd,
            course: mockCourse,
            color: mockCourse.color
          });
        });

        return events;
      })
    );
  }
}
