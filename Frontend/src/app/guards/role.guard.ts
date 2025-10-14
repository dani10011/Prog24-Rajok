import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    // First check if user is authenticated
    if (!this.authService.isAuthenticated) {
      console.log('RoleGuard: User not authenticated, redirecting to login');
      return this.router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url }
      });
    }

    // Get allowed roles from route data
    const allowedRoles = route.data['allowedRoles'] as number[] | undefined;

    // If no roles specified, allow access (acts as regular auth guard)
    if (!allowedRoles || allowedRoles.length === 0) {
      return true;
    }

    // Check if user has one of the allowed roles
    const userRoleId = this.authService.getUserRoleId();
    if (userRoleId !== null && allowedRoles.includes(userRoleId)) {
      return true;
    }

    // User doesn't have required role, redirect to their appropriate dashboard
    console.log('RoleGuard: User does not have required role, redirecting to appropriate dashboard');
    const dashboardRoute = this.authService.getDashboardRoute();
    return this.router.createUrlTree([dashboardRoute]);
  }
}
