import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { delay, map, tap } from 'rxjs/operators';
import { User } from '../interfaces/user.interface';
import { AuthResponse } from '../interfaces/auth-response.interface';
import { LoginCredentials } from '../interfaces/login-credentials.interface';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;
  
  private isAuthenticatedSubject: BehaviorSubject<boolean>;
  public isAuthenticated$: Observable<boolean>;

  constructor(private router: Router) {
    // Initialize with stored user data if available
    const storedUser = this.getStoredUser();
    this.currentUserSubject = new BehaviorSubject<User | null>(storedUser);
    this.currentUser$ = this.currentUserSubject.asObservable();
    
    this.isAuthenticatedSubject = new BehaviorSubject<boolean>(!!storedUser);
    this.isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  }

  /**
   * Get current user value
   */
  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if user is authenticated
   */
  public get isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  /**
   * Login user with credentials
   * NOTE: This is a mock implementation. Replace with actual HTTP call to your backend API
   */
  login(credentials: LoginCredentials): Observable<AuthResponse> {
    // Mock authentication - Replace this with actual HTTP call
    // Example: return this.http.post<AuthResponse>('/api/auth/login', credentials);
    
    return of({
      success: true,
      token: 'mock-jwt-token-' + Date.now(),
      user: {
        id: '1',
        email: credentials.email,
        username: credentials.email.split('@')[0],
        firstName: 'Demo',
        lastName: 'User',
        role: 'user',
        createdAt: new Date(),
        lastLogin: new Date()
      },
      expiresIn: 3600,
      message: 'Login successful'
    }).pipe(
      delay(1000), // Simulate network delay
      tap(response => {
        if (response.success) {
          this.setSession(response);
        }
      })
    );
  }

  /**
   * Logout user and clear session
   */
  logout(): void {
    // Clear stored data
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    
    // Update subjects
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    
    // Navigate to login
    this.router.navigate(['/login']);
  }

  /**
   * Set user session after successful login
   */
  private setSession(authResponse: AuthResponse): void {
    // Store token and user data
    localStorage.setItem(this.TOKEN_KEY, authResponse.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(authResponse.user));
    
    // Update subjects
    this.currentUserSubject.next(authResponse.user);
    this.isAuthenticatedSubject.next(true);
  }

  /**
   * Get stored user from localStorage
   */
  private getStoredUser(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (userJson) {
      try {
        return JSON.parse(userJson);
      } catch (e) {
        console.error('Error parsing stored user:', e);
        return null;
      }
    }
    return null;
  }

  /**
   * Get stored token
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Check if token exists
   */
  hasToken(): boolean {
    return !!this.getToken();
  }

  /**
   * Refresh token (mock implementation)
   * Replace with actual API call
   */
  refreshToken(): Observable<AuthResponse> {
    const currentUser = this.currentUserValue;
    if (!currentUser) {
      return throwError(() => new Error('No user logged in'));
    }

    // Mock refresh - Replace with actual HTTP call
    return of({
      success: true,
      token: 'refreshed-jwt-token-' + Date.now(),
      user: currentUser,
      expiresIn: 3600,
      message: 'Token refreshed'
    }).pipe(
      delay(500),
      tap(response => {
        if (response.success) {
          localStorage.setItem(this.TOKEN_KEY, response.token);
        }
      })
    );
  }

  /**
   * Update user profile
   */
  updateUserProfile(user: Partial<User>): void {
    const currentUser = this.currentUserValue;
    if (currentUser) {
      const updatedUser = { ...currentUser, ...user };
      localStorage.setItem(this.USER_KEY, JSON.stringify(updatedUser));
      this.currentUserSubject.next(updatedUser);
    }
  }
}
