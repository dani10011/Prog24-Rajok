import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { UserListItem } from '../interfaces/user.interface';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Get all users from the API
   * Note: JWT token is automatically added by AuthInterceptor
   */
  getUsers(): Observable<UserListItem[]> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'ngrok-skip-browser-warning': 'true'
    });

    return this.http.get<any>(`${this.apiUrl}/api/User/GetUsers`, { headers }).pipe(
      map(response => {
        console.log('GetUsers response:', response);

        // Handle different response formats
        // If response is an array, return it directly
        if (Array.isArray(response)) {
          return response as UserListItem[];
        }

        // If response has a users property, return that
        if (response.users && Array.isArray(response.users)) {
          return response.users as UserListItem[];
        }

        // If response has data property, return that
        if (response.data && Array.isArray(response.data)) {
          return response.data as UserListItem[];
        }

        // Otherwise, return the response as is
        return response as UserListItem[];
      }),
      catchError(error => {
        console.error('Error fetching users:', error);
        let errorMessage = 'Failed to load users. Please try again.';

        if (error.status === 401) {
          errorMessage = 'Unauthorized. Please login again.';
        } else if (error.status === 403) {
          errorMessage = 'Access denied. You do not have permission to view users.';
        } else if (error.status === 404) {
          errorMessage = 'Users endpoint not found.';
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
