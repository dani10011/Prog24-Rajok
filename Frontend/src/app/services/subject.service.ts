import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Subject } from '../interfaces/subject';

@Injectable({
  providedIn: 'root'
})
export class SubjectService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Get all subjects from the API
   * Note: JWT token is automatically added by AuthInterceptor
   */
  getSubjects(): Observable<Subject[]> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'ngrok-skip-browser-warning': 'true'
    });

    return this.http.get<any>(`${this.apiUrl}/api/Subject/GetSubjects`, { headers }).pipe(
      map(response => {
        console.log('GetSubjects response:', response);

        // Handle different response formats
        // If response is an array, return it directly
        if (Array.isArray(response)) {
          return response as Subject[];
        }

        // If response has a subjects property, return that
        if (response.subjects && Array.isArray(response.subjects)) {
          return response.subjects as Subject[];
        }

        // If response has data property, return that
        if (response.data && Array.isArray(response.data)) {
          return response.data as Subject[];
        }

        // Otherwise, return the response as is
        return response as Subject[];
      }),
      catchError(error => {
        console.error('Error fetching subjects:', error);
        let errorMessage = 'Failed to load subjects. Please try again.';

        if (error.status === 401) {
          errorMessage = 'Unauthorized. Please login again.';
        } else if (error.status === 403) {
          errorMessage = 'Access denied. You do not have permission to view subjects.';
        } else if (error.status === 404) {
          errorMessage = 'Subjects endpoint not found.';
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
