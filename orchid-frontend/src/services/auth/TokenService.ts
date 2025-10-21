class AuthService {
  private accessToken: string | null = null;

  setAccessToken(token: string) {
    this.accessToken = token;
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  clearAccessToken() {
    this.accessToken = null;
  }

  // Tự động xóa token khi đóng tab (tùy chọn)
  init() {
    window.addEventListener('beforeunload', () => {
      this.clearAccessToken();
    });
  }
}

export const authService = new AuthService();