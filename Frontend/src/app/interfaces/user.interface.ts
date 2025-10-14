export interface User {
  id: string;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
  role?: string;
  role_id: number; // 1: Admin, 2: Teacher, 3: Student
  createdAt?: Date;
  lastLogin?: Date;
}
