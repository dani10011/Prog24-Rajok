export interface User {
  id: string;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
  role?: string;
  createdAt?: Date;
  lastLogin?: Date;
}
