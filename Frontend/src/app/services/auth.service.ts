import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { User } from '../interfaces/user.interface';
import { AuthResponse } from '../interfaces/auth-response.interface';
import { LoginCredentials } from '../interfaces/login-credentials.interface';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly apiUrl = environment.apiUrl;

  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;

  private isAuthenticatedSubject: BehaviorSubject<boolean>;
  public isAuthenticated$: Observable<boolean>;

  constructor(
    private router: Router,
    private http: HttpClient
  ) {
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
   */
  login(credentials: LoginCredentials): Observable<AuthResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'ngrok-skip-browser-warning': 'true'
    });

    return this.http.post<any>(`${this.apiUrl}/api/Auth/Login`, credentials, { headers }).pipe(
      map(response => {
        // Debug: log the backend response
        console.log('=== BACKEND RESPONSE ===');
        console.log('Full response:', JSON.stringify(response, null, 2));
        console.log('=======================');

        const token = response.token;

        if (!token) {
          throw new Error('No token received from backend');
        }

        // Extract data from JWT token
        const roleId = this.extractRoleIdFromToken(token);
        const userIdFromToken = this.extractUserIdFromToken(token);
        const emailFromToken = this.extractEmailFromToken(token);
        const nameFromToken = this.extractNameFromToken(token);

        // Prefer backend response data, fallback to token data
        const userId = response.userId || userIdFromToken || credentials.email;
        const email = response.email || emailFromToken || credentials.email;
        const name = response.name || nameFromToken || email;
        const neptunCode = response.neptunCode || '';

        const authResponse: AuthResponse = {
          success: true,
          token: token,
          user: {
            id: userId,
            email: email,
            username: email ? email.split('@')[0] : 'unknown',
            name: name,
            neptun_code: neptunCode,
            role_id: roleId,
            role: this.getRoleName(roleId),
            createdAt: new Date(),
            lastLogin: new Date()
          },
          expiresIn: response.expiresIn || 3600,
          message: response.message || 'Login successful'
        };

        console.log('=== TRANSFORMED AUTH RESPONSE ===');
        console.log('User ID:', authResponse.user.id);
        console.log('User email:', authResponse.user.email);
        console.log('User name:', authResponse.user.name);
        console.log('User role_id:', authResponse.user.role_id);
        console.log('User role:', authResponse.user.role);
        console.log('================================');

        return authResponse;
      }),
      tap(response => {
        if (response.success) {
          this.setSession(response);
          console.log('Session set. Dashboard route:', this.getDashboardRoute());
        }
      }),
      catchError(error => {
        console.error('Login error:', error);
        let errorMessage = 'An error occurred during login. Please try again.';

        if (error.status === 401) {
          errorMessage = 'Invalid email or password.';
        } else if (error.status === 404) {
          errorMessage = 'Login service not found. Please contact support.';
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
   * Get role name from role_id
   */
  private getRoleName(roleId: number): string {
    switch (roleId) {
      case 1:
        return 'admin';
      case 2:
        return 'instructor';
      case 3:
        return 'student';
      default:
        return 'unknown';
    }
  }

  /**
   * Decode JWT token and extract payload
   */
  private decodeToken(token: string): any {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        console.error('Invalid JWT token format');
        return null;
      }

      const payload = parts[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded);
    } catch (error) {
      console.error('Error decoding JWT token:', error);
      return null;
    }
  }

  /**
   * Extract role ID from JWT token
   */
  private extractRoleIdFromToken(token: string): number {
    const payload = this.decodeToken(token);

    if (!payload) {
      console.error('Failed to decode token payload');
      return 3; // Default to student
    }

    console.log('JWT Payload:', payload);

    // Check for role claim (Microsoft Identity format)
    const roleClaim = payload['http://schemas.microsoft.com/ws/2008/06/identity/claims/role'];

    if (roleClaim) {
      console.log('Role claim found:', roleClaim);

      // Map role string to role_id
      switch (roleClaim.toLowerCase()) {
        case 'admin':
          return 1;
        case 'instructor':
          return 2;
        case 'student':
          return 3;
        default:
          console.warn('Unknown role:', roleClaim);
          return 3; // Default to student
      }
    }

    // Fallback: check for standard 'role' claim
    if (payload.role) {
      console.log('Standard role claim found:', payload.role);

      switch (payload.role.toLowerCase()) {
        case 'admin':
          return 1;
        case 'instructor':
          return 2;
        case 'student':
          return 3;
        default:
          return 3;
      }
    }

    console.warn('No role claim found in token, defaulting to student');
    return 3;
  }

  /**
   * Extract user ID from JWT token
   */
  private extractUserIdFromToken(token: string): string | null {
    const payload = this.decodeToken(token);

    if (!payload) {
      return null;
    }

    // Check for nameidentifier claim (Microsoft Identity format)
    const userIdClaim = payload['http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier'];

    if (userIdClaim) {
      return userIdClaim;
    }

    // Fallback to standard claims
    return payload.sub || payload.userId || payload.id || null;
  }

  /**
   * Extract email from JWT token
   */
  private extractEmailFromToken(token: string): string | null {
    const payload = this.decodeToken(token);

    if (!payload) {
      return null;
    }

    // Check for email claim (Microsoft Identity format)
    const emailClaim = payload['http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress'];

    if (emailClaim) {
      return emailClaim;
    }

    // Fallback to standard claims
    return payload.email || null;
  }

  /**
   * Extract name from JWT token
   */
  private extractNameFromToken(token: string): string | null {
    const payload = this.decodeToken(token);

    if (!payload) {
      return null;
    }

    // Check for name claim (Microsoft Identity format)
    const nameClaim = payload['http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name'];

    if (nameClaim) {
      return nameClaim;
    }

    // Fallback to standard claims
    return payload.name || null;
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
   * Refresh token
   */
  refreshToken(): Observable<AuthResponse> {
    const currentUser = this.currentUserValue;
    const token = this.getToken();

    if (!currentUser || !token) {
      return throwError(() => new Error('No user logged in'));
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      'ngrok-skip-browser-warning': 'true'
    });

    return this.http.post<any>(`${this.apiUrl}/api/Auth/RefreshToken`, {}, { headers }).pipe(
      map(response => ({
        success: true,
        token: response.token,
        user: currentUser,
        expiresIn: response.expiresIn || 3600,
        message: 'Token refreshed'
      })),
      tap(response => {
        if (response.success) {
          localStorage.setItem(this.TOKEN_KEY, response.token);
        }
      }),
      catchError(error => {
        console.error('Token refresh error:', error);
        // If refresh fails, logout user
        this.logout();
        return throwError(() => new Error('Session expired. Please login again.'));
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

  /**
   * Get user role ID
   */
  getUserRoleId(): number | null {
    return this.currentUserValue?.role_id ?? null;
  }

  /**
   * Check if user is admin (role_id = 1)
   */
  isAdmin(): boolean {
    return this.getUserRoleId() === 1;
  }

  /**
   * Check if user is instructor (role_id = 2)
   */
  isInstructor(): boolean {
    return this.getUserRoleId() === 2;
  }

  /**
   * Check if user is teacher (alias for isInstructor)
   * @deprecated Use isInstructor() instead
   */
  isTeacher(): boolean {
    return this.isInstructor();
  }

  /**
   * Check if user is student (role_id = 3)
   */
  isStudent(): boolean {
    return this.getUserRoleId() === 3;
  }

  /**
   * Check if user has specific role
   */
  hasRole(roleId: number): boolean {
    return this.getUserRoleId() === roleId;
  }

  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    return this.currentUserValue;
  }

  /**
   * Get dashboard route based on user role
   */
  getDashboardRoute(): string {
    const roleId = this.getUserRoleId();
    switch (roleId) {
      case 1:
        return '/admin-dashboard';
      case 2:
        return '/instructor-dashboard';
      case 3:
        return '/student-dashboard';
      default:
        return '/login';
    }
  }
}
