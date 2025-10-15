import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Room } from '../interfaces/room';

@Injectable({
  providedIn: 'root'
})
export class RoomService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Get all rooms from the API
   * Note: JWT token is automatically added by AuthInterceptor
   */
  getRooms(): Observable<Room[]> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'ngrok-skip-browser-warning': 'true'
    });

    return this.http.get<any>(`${this.apiUrl}/api/Room/GetRooms`, { headers }).pipe(
      map(response => {
        console.log('GetRooms response:', response);

        // Handle different response formats
        // If response is an array, return it directly
        if (Array.isArray(response)) {
          return response as Room[];
        }

        // If response has a rooms property, return that
        if (response.rooms && Array.isArray(response.rooms)) {
          return response.rooms as Room[];
        }

        // If response has data property, return that
        if (response.data && Array.isArray(response.data)) {
          return response.data as Room[];
        }

        // Otherwise, return the response as is
        return response as Room[];
      }),
      catchError(error => {
        console.error('Error fetching rooms:', error);
        let errorMessage = 'Failed to load rooms. Please try again.';

        if (error.status === 401) {
          errorMessage = 'Unauthorized. Please login again.';
        } else if (error.status === 403) {
          errorMessage = 'Access denied. You do not have permission to view rooms.';
        } else if (error.status === 404) {
          errorMessage = 'Rooms endpoint not found.';
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
