import { User } from './user.interface';

export interface AuthResponse {
  success: boolean;
  token: string;
  user: User;
  expiresIn?: number;
  message?: string;
}

export interface AuthError {
  success: false;
  message: string;
  errors?: string[];
}
