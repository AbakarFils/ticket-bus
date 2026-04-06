export type UserRole = 'ADMIN' | 'CONTROLLER' | 'PASSENGER';
export interface AuthRequest { username: string; password: string; }
export interface AuthResponse { token: string; username: string; role: UserRole; }
