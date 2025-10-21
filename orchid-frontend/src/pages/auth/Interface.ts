export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignUpRequest {
  username: string;
  password: string;
  fullName: string;
}

export interface LoginResponse {
  accessToken: string;
  accessTokenValidity: number; // seconds
}

export interface AccountResponse{
  id:number;
  email:string;
  fullName:string;
  role:string;
}