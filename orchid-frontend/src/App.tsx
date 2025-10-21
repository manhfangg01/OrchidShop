import { toast } from "react-toastify";
import { authApi } from "./services/auth/AuthApi";
import "react-toastify/dist/ReactToastify.css";
import { useNavigate } from "react-router-dom";

const App = () => {
  const navigate = useNavigate();
  const handleLogout = async () => {
    try {
      authApi.callLogout();
      toast.success("Đăng xuất thành công!");
      // Optional: redirect to login page
      navigate("/login", { replace: true }); // Không quay lại trang trước đó khi không bấm nút "back"
    } catch (error) {
      console.error("Lỗi khi đăng xuất:", error);
      toast.error("Đăng xuất thất bại!");
    }
  };

  return (
    <div className="app-container">
      <h1>Hello I gay</h1>

      {/* Nút Logout */}
      <button
        onClick={handleLogout}
        style={{
          padding: "10px 20px",
          backgroundColor: "#e74c3c",
          color: "white",
          border: "none",
          borderRadius: "5px",
          cursor: "pointer",
          fontSize: "16px",
          marginTop: "20px",
        }}
      >
        Đăng xuất
      </button>
    </div>
  );
};

export default App;
