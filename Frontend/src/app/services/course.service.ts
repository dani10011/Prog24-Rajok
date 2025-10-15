import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Course, CreateCourseRequest } from '../interfaces/course';

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Get courses by user ID from the API
   * Note: JWT token is automatically added by AuthInterceptor
   */
  getCoursesByUserId(userId: number): Observable<Course[]> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'ngrok-skip-browser-warning': 'true'
    });

    return this.http.get<any>(`${this.apiUrl}/api/Course/GetCoursesByUserId/${userId}`, { headers }).pipe(
      map(response => {
        console.log('GetCoursesByUserId response:', response);

        // Handle different response formats
        // If response is an array, return it directly
        if (Array.isArray(response)) {
          return response as Course[];
        }

        // If response has a courses property, return that
        if (response.courses && Array.isArray(response.courses)) {
          return response.courses as Course[];
        }

        // If response has data property, return that
        if (response.data && Array.isArray(response.data)) {
          return response.data as Course[];
        }

        // Otherwise, return the response as is
        return response as Course[];
      }),
      catchError(error => {
        console.error('Error fetching courses:', error);
        let errorMessage = 'Failed to load courses. Please try again.';

        if (error.status === 401) {
          errorMessage = 'Unauthorized. Please login again.';
        } else if (error.status === 403) {
          errorMessage = 'Access denied. You do not have permission to view courses.';
        } else if (error.status === 404) {
          errorMessage = 'Courses endpoint not found.';
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
   * Create a new course (room booking)
   * Note: JWT token is automatically added by AuthInterceptor
   */
  createCourse(request: CreateCourseRequest): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'ngrok-skip-browser-warning': 'true'
    });

    return this.http.post<any>(`${this.apiUrl}/api/Course/CreateCourse`, request, { headers }).pipe(
      map(response => {
        console.log('CreateCourse response:', response);
        return response;
      }),
      catchError(error => {
        console.error('Error creating course:', error);
        let errorMessage = 'Failed to create course. Please try again.';

        if (error.status === 401) {
          errorMessage = 'Unauthorized. Please login again.';
        } else if (error.status === 403) {
          errorMessage = 'Access denied. You do not have permission to create courses.';
        } else if (error.status === 400) {
          errorMessage = error.error?.message || 'Invalid course data. Please check all fields.';
        } else if (error.status === 404) {
          errorMessage = 'Course endpoint not found.';
        } else if (error.status === 0) {
          errorMessage = 'Cannot connect to server. Please check your connection.';
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        }

        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
