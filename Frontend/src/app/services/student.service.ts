import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Student } from '../interfaces/student.interface';

@Injectable({
  providedIn: 'root'
})
export class StudentService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Get all students from the API
   * Note: JWT token is automatically added by AuthInterceptor
   */
  getStudents(): Observable<Student[]> {
    return this.http.get<any>(`${this.apiUrl}/api/Student/GetStudents`).pipe(
      map(response => {
        console.log('GetStudents response:', response);

        // Handle different response formats
        // If response is an array, return it directly
        if (Array.isArray(response)) {
          return response as Student[];
        }

        // If response has a students property, return that
        if (response.students && Array.isArray(response.students)) {
          return response.students as Student[];
        }

        // If response has data property, return that
        if (response.data && Array.isArray(response.data)) {
          return response.data as Student[];
        }

        // Otherwise, return the response as is
        return response as Student[];
      }),
      catchError(error => {
        console.error('Error fetching students:', error);
        let errorMessage = 'Failed to load students. Please try again.';

        if (error.status === 401) {
          errorMessage = 'Unauthorized. Please login again.';
        } else if (error.status === 403) {
          errorMessage = 'Access denied. You do not have permission to view students.';
        } else if (error.status === 404) {
          errorMessage = 'Students endpoint not found.';
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
