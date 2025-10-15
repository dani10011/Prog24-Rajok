export interface User {
  id: string;
  email: string;
  username: string;
  name?: string;
  neptun_code?: string;
  firstName?: string;
  lastName?: string;
  role?: string;
  role_id: number; // 1: Admin, 2: Instructor, 3: Student
  createdAt?: Date;
  lastLogin?: Date;
}

export interface UserListItem {
  id: number;
  name: string;
  roleId: number;
  roleName: string;
  email: string;
}
