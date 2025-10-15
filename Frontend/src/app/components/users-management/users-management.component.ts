import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { UserListItem } from '../../interfaces/user.interface';

@Component({
  selector: 'app-users-management',
  template: `
    <div class="users-container">
      <!-- Header -->
      <div class="users-header">
        <div class="header-content">
          <div class="header-left">
            <button class="back-btn" (click)="goBack()">
              <i class="pi pi-arrow-left"></i>
            </button>
            <div class="header-brand">
              <i class="pi pi-users brand-icon"></i>
              <span class="brand-text">Felhasználók kezelése</span>
            </div>
          </div>
          <div class="header-user">
            <span class="user-name">{{ currentUserName }}</span>
            <button class="logout-btn" (click)="onLogout()" [disabled]="loading">
              <i class="pi pi-sign-out"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="users-content">
        <div class="content-wrapper">
          <!-- Users Card -->
          <p-card class="main-card">
            <!-- Error Message -->
            <div *ngIf="errorMessage" class="error-message">
              <p-message severity="error" [text]="errorMessage"></p-message>
            </div>

            <!-- Data Table -->
            <p-table
              #usersTable
              [value]="users"
              [paginator]="true"
              [rows]="10"
              [rowsPerPageOptions]="[10, 25, 50]"
              [showCurrentPageReport]="true"
              currentPageReportTemplate="{first} - {last} / összesen {totalRecords} felhasználó"
              [loading]="loading"
              [globalFilterFields]="['name', 'email', 'roleName']"
              styleClass="p-datatable-striped p-datatable-gridlines"
              responsiveLayout="scroll"
              [rowHover]="true"
              dataKey="id"
              sortMode="multiple">

              <!-- Table Caption with Search -->
              <ng-template pTemplate="caption">
                <div class="table-caption">
                  <div class="caption-left">
                    <span class="caption-text">
                      <i class="pi pi-list"></i>
                      Összes felhasználó
                    </span>
                  </div>
                  <div class="caption-right">
                    <span class="p-input-icon-left">
                      <i class="pi pi-search"></i>
                      <input pInputText type="text"
                             (input)="usersTable.filterGlobal($any($event.target).value, 'contains')"
                             placeholder="Keresés..."
                             class="global-search" />
                    </span>
                  </div>
                </div>
              </ng-template>

              <!-- Table Header -->
              <ng-template pTemplate="header">
                <tr>
                  <th pSortableColumn="id" style="width: 80px">
                    <div class="header-content">
                      <i class="pi pi-hashtag"></i>
                      ID
                      <p-sortIcon field="id"></p-sortIcon>
                    </div>
                  </th>
                  <th pSortableColumn="name">
                    <div class="header-content">
                      <i class="pi pi-user"></i>
                      Név
                      <p-sortIcon field="name"></p-sortIcon>
                    </div>
                  </th>
                  <th pSortableColumn="email">
                    <div class="header-content">
                      <i class="pi pi-envelope"></i>
                      Email
                      <p-sortIcon field="email"></p-sortIcon>
                    </div>
                  </th>
                  <th pSortableColumn="roleName" style="width: 180px">
                    <div class="header-content">
                      <i class="pi pi-shield"></i>
                      Szerepkör
                      <p-sortIcon field="roleName"></p-sortIcon>
                    </div>
                  </th>
                  <th pSortableColumn="roleId" style="width: 120px">
                    <div class="header-content">
                      <i class="pi pi-tag"></i>
                      Szerepkör ID
                      <p-sortIcon field="roleId"></p-sortIcon>
                    </div>
                  </th>
                </tr>
                <!-- Filter Row -->
                <tr class="filter-row">
                  <th>
                    <span class="p-input-icon-left">
                      <i class="pi pi-filter-fill"></i>
                      <input pInputText type="text"
                             (input)="usersTable.filter($any($event.target).value, 'id', 'contains')"
                             placeholder="ID"
                             class="p-column-filter" />
                    </span>
                  </th>
                  <th>
                    <span class="p-input-icon-left">
                      <i class="pi pi-filter-fill"></i>
                      <input pInputText type="text"
                             (input)="usersTable.filter($any($event.target).value, 'name', 'contains')"
                             placeholder="Név"
                             class="p-column-filter" />
                    </span>
                  </th>
                  <th>
                    <span class="p-input-icon-left">
                      <i class="pi pi-filter-fill"></i>
                      <input pInputText type="text"
                             (input)="usersTable.filter($any($event.target).value, 'email', 'contains')"
                             placeholder="Email"
                             class="p-column-filter" />
                    </span>
                  </th>
                  <th>
                    <span class="p-input-icon-left">
                      <i class="pi pi-filter-fill"></i>
                      <input pInputText type="text"
                             (input)="usersTable.filter($any($event.target).value, 'roleName', 'contains')"
                             placeholder="Szerepkör"
                             class="p-column-filter" />
                    </span>
                  </th>
                  <th>
                    <span class="p-input-icon-left">
                      <i class="pi pi-filter-fill"></i>
                      <input pInputText type="text"
                             (input)="usersTable.filter($any($event.target).value, 'roleId', 'contains')"
                             placeholder="ID"
                             class="p-column-filter" />
                    </span>
                  </th>
                </tr>
              </ng-template>

              <!-- Table Body -->
              <ng-template pTemplate="body" let-user>
                <tr class="data-row">
                  <td>
                    <span class="id-badge">{{ user.id }}</span>
                  </td>
                  <td>
                    <div class="user-name-cell">
                      <i class="pi pi-user"></i>
                      <span>{{ user.name }}</span>
                    </div>
                  </td>
                  <td>
                    <div class="email-cell">
                      <i class="pi pi-envelope"></i>
                      <span>{{ user.email }}</span>
                    </div>
                  </td>
                  <td>
                    <p-tag
                      [value]="user.roleName"
                      [severity]="getRoleSeverity(user.roleId)"
                      [rounded]="true">
                    </p-tag>
                  </td>
                  <td>
                    <div class="role-id-cell">
                      <i class="pi pi-tag"></i>
                      <span>{{ user.roleId }}</span>
                    </div>
                  </td>
                </tr>
              </ng-template>

              <!-- Empty Message -->
              <ng-template pTemplate="emptymessage">
                <tr>
                  <td colspan="5">
                    <div class="empty-state">
                      <i class="pi pi-info-circle"></i>
                      <p>Nincs megjeleníthető felhasználó</p>
                    </div>
                  </td>
                </tr>
              </ng-template>

              <!-- Loading Template -->
              <ng-template pTemplate="loadingbody">
                <tr>
                  <td colspan="5">
                    <div class="loading-state">
                      <i class="pi pi-spin pi-spinner"></i>
                      <p>Felhasználók betöltése...</p>
                    </div>
                  </td>
                </tr>
              </ng-template>
            </p-table>
          </p-card>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @import '../../../styles/variables';

    .users-container {
      min-height: 100vh;
      background: var(--surface-ground);
    }

    .users-header {
      background: var(--surface-card);
      border-bottom: 1px solid var(--surface-border);
      box-shadow: var(--shadow-sm);
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .header-content {
      max-width: 100%;
      margin: 0 auto;
      padding: 0 $spacing-xl;
      height: 64px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: $spacing-md;
    }

    .back-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border: none;
      border-radius: 50%;
      background: transparent;
      color: var(--text-color-secondary);
      cursor: pointer;
      transition: all 0.2s ease;
      font-size: 1.2rem;

      &:hover {
        background: var(--surface-hover);
        color: var(--primary-color);
      }
    }

    .header-brand {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .brand-icon {
        font-size: 1.5rem;
        color: var(--primary-color);
      }

      .brand-text {
        font-size: $font-size-lg;
        font-weight: 600;
        color: var(--text-color);
      }
    }

    .header-user {
      display: flex;
      align-items: center;
      gap: $spacing-lg;

      .user-name {
        font-size: $font-size-base;
        font-weight: 500;
        color: var(--text-color);
      }

      .logout-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        border: none;
        border-radius: 50%;
        background: transparent;
        color: var(--text-color-secondary);
        cursor: pointer;
        transition: all 0.2s ease;
        font-size: 1.2rem;

        &:hover {
          background: var(--surface-hover);
          color: var(--primary-color);
        }
      }
    }

    .users-content {
      padding: 0;
    }

    .content-wrapper {
      max-width: 100%;
      margin: 0 auto;
      padding: $spacing-lg;
    }

    .main-card {
      border-radius: 12px;
      box-shadow: var(--shadow-md);
      border: none;

      ::ng-deep .p-card-body {
        padding: 1.5rem;
      }

      ::ng-deep .p-card-content {
        padding: 0;
      }
    }

    .error-message {
      margin-bottom: 1rem;
      animation: slideDown 0.3s ease-out;
    }

    @keyframes slideDown {
      from {
        opacity: 0;
        transform: translateY(-20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    // Table styles from rooms
    ::ng-deep {
      .table-caption {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.25rem;
        background: var(--surface-section);
        border-bottom: 2px solid var(--primary-color);
        border-radius: 0;
        margin: -1.5rem -1.5rem 1.5rem -1.5rem;

        .caption-left {
          .caption-text {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            color: var(--text-color);
            font-size: 1.2rem;
            font-weight: 600;

            i {
              font-size: 1.4rem;
              color: var(--primary-color);
            }
          }
        }

        .caption-right {
          .p-input-icon-left {
            i {
              left: 0.75rem;
              color: var(--text-color-secondary);
            }

            .global-search {
              width: 350px;
              padding-left: 2.5rem;
            }
          }
        }
      }

      .p-datatable {
        .p-datatable-thead {
          > tr {
            > th {
              background: var(--surface-section);
              color: var(--text-color);
              border: 1px solid var(--surface-border);
              padding: 1rem;
              font-weight: 600;
              font-size: 0.95rem;

              .header-content {
                display: flex;
                align-items: center;
                gap: 0.5rem;

                i {
                  color: var(--primary-color);
                  font-size: 1.1rem;
                }
              }
            }
          }

          .filter-row {
            th {
              background: var(--surface-card);
              padding: 0.75rem;

              .p-input-icon-left {
                width: 100%;

                i {
                  left: 0.5rem;
                  color: var(--text-color-secondary);
                  font-size: 0.85rem;
                }

                .p-column-filter {
                  width: 100%;
                  font-size: 0.9rem;
                  padding: 0.5rem 0.5rem 0.5rem 2rem;
                }
              }
            }
          }
        }

        .p-datatable-tbody {
          > tr {
            transition: all 0.2s ease;

            &:hover {
              background-color: var(--surface-hover) !important;
            }

            > td {
              border: 1px solid var(--surface-border);
              padding: 1rem;
              vertical-align: middle;

              .id-badge {
                display: inline-block;
                background: var(--primary-color);
                color: white;
                padding: 0.35rem 0.75rem;
                border-radius: 20px;
                font-weight: 600;
                font-size: 0.9rem;
                box-shadow: 0 2px 4px rgba(3, 87, 175, 0.3);
              }

              .user-name-cell,
              .email-cell,
              .role-id-cell {
                display: flex;
                align-items: center;
                gap: 0.6rem;
                color: var(--text-color);

                i {
                  color: var(--text-color-secondary);
                  font-size: 1rem;
                }

                span {
                  font-size: 0.95rem;
                }
              }

              .user-name-cell {
                font-weight: 500;

                i {
                  color: var(--primary-color);
                }
              }
            }
          }

          > tr.data-row {
            animation: fadeIn 0.3s ease-out;
          }
        }

        .empty-state,
        .loading-state {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 3rem;
          color: var(--text-color-secondary);

          i {
            font-size: 3rem;
            color: var(--text-color-muted);
            margin-bottom: 1rem;
          }

          p {
            font-size: 1.1rem;
            margin: 0;
          }
        }

        .loading-state i {
          color: var(--primary-color);
        }

        .p-paginator {
          background: var(--surface-section);
          border: 1px solid var(--surface-border);
          border-top: 2px solid var(--primary-color);
          padding: 1rem;

          .p-paginator-current {
            color: var(--text-color-secondary);
            font-weight: 500;
          }

          .p-paginator-pages {
            .p-paginator-page {
              border-radius: 6px;
              transition: all 0.2s ease;

              &:hover {
                background: var(--primary-color);
                color: white;
              }

              &.p-highlight {
                background: var(--primary-color);
                color: white;
                font-weight: 600;
              }
            }
          }
        }

        &.p-datatable-striped {
          .p-datatable-tbody > tr:nth-child(even) {
            background-color: var(--surface-section);
          }
        }
      }

      .p-tag {
        padding: 0.4rem 0.8rem;
        font-weight: 600;
        font-size: 0.9rem;
        letter-spacing: 0.3px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

        &.p-tag-success {
          background: var(--green-500);
        }

        &.p-tag-warning {
          background: var(--orange-500);
        }

        &.p-tag-info {
          background: var(--blue-500);
        }
      }
    }

    @keyframes fadeIn {
      from {
        opacity: 0;
        transform: translateY(-5px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @media (max-width: 768px) {
      .header-content {
        padding: 0 $spacing-md;
      }

      .header-brand {
        .brand-text {
          font-size: 1.2rem;
        }

        .brand-icon {
          font-size: 1.4rem;
        }
      }

      .header-user {
        .user-name {
          display: none;
        }
      }

      .content-wrapper {
        padding: $spacing-md;
      }

      ::ng-deep {
        .table-caption {
          flex-direction: column;
          gap: 1rem;

          .caption-right {
            width: 100%;

            .p-input-icon-left {
              width: 100%;

              .global-search {
                width: 100%;
              }
            }
          }
        }
      }
    }
  `]
})
export class UsersManagementComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  currentUserName: string = '';
  loading: boolean = false;
  users: UserListItem[] = [];
  errorMessage: string = '';

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user) {
          this.currentUserName = user.firstName || user.username;
        }
      });

    this.loadUsers();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';

    this.userService.getUsers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users) => {
          this.users = users;
          this.loading = false;
          console.log('Loaded users:', users);
        },
        error: (error) => {
          console.error('Error loading users:', error);
          this.errorMessage = error.message || 'Failed to load users';
          this.loading = false;
        }
      });
  }

  getRoleSeverity(roleId: number): string {
    // 1: Admin - success (green)
    // 2: Instructor - warning (orange)
    // 3: Student - info (blue)
    const severityMap: { [key: number]: string } = {
      1: 'success',
      2: 'warning',
      3: 'info'
    };
    return severityMap[roleId] || 'info';
  }

  goBack(): void {
    const dashboardRoute = this.authService.getDashboardRoute();
    this.router.navigate([dashboardRoute]);
  }

  onLogout(): void {
    this.loading = true;
    this.authService.logout();
  }
}
