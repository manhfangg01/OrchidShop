import axios from "axios";
import type {
  LoginRequest,
  LoginResponse,
  SignUpRequest,
  AccountResponse, // ← bạn cần định nghĩa interface này
} from "../../pages/auth/Interface";

const API_BASE_URL = "http://localhost:8080/api/auth";

// Tạo một instance Axios riêng cho các API cần Bearer Token
const authAxios = axios.create();

// Interceptor: tự động thêm Authorization header nếu có accessToken trong localStorage
authAxios.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken"); // hoặc lấy từ state (Redux/Zustand)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// publicAxios: không bao giờ gửi token
const publicAxios = axios.create();

export const authApi = {
  // === Auth APIs (không cần token) ===
  callLogin: (data: LoginRequest) =>
    publicAxios.post<LoginResponse>(`${API_BASE_URL}/login`, data, {
      withCredentials: true, // để nhận refresh_token cookie
    }),
  callSignUp: (data: SignUpRequest) => publicAxios.post<void>(`${API_BASE_URL}/signup`, data),

  callLogout: () =>
    axios.post<void>(`${API_BASE_URL}/logout`, null, {
      withCredentials: true, // gửi cookie refresh_token để xóa ở server
    }),
  callRefresh: () =>
    axios.post<LoginResponse>(`${API_BASE_URL}/refresh`, null, {
      withCredentials: true, // gửi cookie refresh_token để đổi lấy access_token mới
    }),
  // === Protected APIs (cần Bearer Token) ===
  callGetAccount: () => authAxios.get<AccountResponse>(API_BASE_URL), // tự động có Authorization header
};
